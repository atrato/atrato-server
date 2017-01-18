package io.atrato.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ext.Provider;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Throwables;

import com.datatorrent.common.util.JacksonObjectMapperProvider;
import com.datatorrent.stram.security.StramUserLogin;
import com.datatorrent.stram.util.VersionInfo;

import io.atrato.server.cluster.Cluster;
import io.atrato.server.cluster.yarn.YarnCluster;
import io.atrato.server.config.AtratoConfiguration;
import io.atrato.server.config.ConfigurationException;
import io.atrato.server.config.FileConfiguration;
import io.atrato.server.config.JDBCConfiguration;

/**
 * Created by david on 12/22/16.
 */
public class AtratoServer
{
  @Provider
  public static class JsonProvider extends JacksonObjectMapperProvider {}

  private static final String DEFAULT_HOST = "0.0.0.0";
  private static final int DEFAULT_PORT = 8800;

  private static final String CONFIG_KEY_PREFIX = "atrato.server.";
  private static final String CONFIG_KEY_STATIC_RESOURCE_BASE = CONFIG_KEY_PREFIX + "staticResourceBase";
  private static final String CONFIG_KEY_LISTEN_ADDRESS = CONFIG_KEY_PREFIX + "listenAddress";
  private static final String CONFIG_KEY_SECURITY_PREFIX = CONFIG_KEY_PREFIX + "security.";
  private static final String CONFIG_KEY_SECURITY_KERBEROS_PRINCIPAL = CONFIG_KEY_SECURITY_PREFIX + "kerberos.principal";
  private static final String CONFIG_KEY_SECURITY_KERBEROS_KEYTAB = CONFIG_KEY_SECURITY_PREFIX + "kerberos.keytab";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private static AtratoConfiguration configuration;
  private static Cluster cluster;

  private static String groupId = "io.atrato";
  private static String artifactId = "atrato-server";
  private static Class<?> classInJar = AtratoServer.class;
  private static String gitPropertiesResource = artifactId + ".git.properties";

  private static final String CMD_OPTION_LISTEN_ADDRESS = "listenAddress";
  private static final String CMD_OPTION_CONFIG_LOCATION = "configLocation";

  private static final String DEFAULT_CONFIG_LOCATION = "jdbc:derby:atrato;create=true";
  public static final VersionInfo ATRATO_SERVER_VERSION = new VersionInfo(classInJar, groupId, artifactId, gitPropertiesResource);

  public static AtratoConfiguration getConfiguration()
  {
    return configuration;
  }

  public static Cluster getCluster()
  {
    return cluster;
  }

  void init(String[] args) throws ParseException, IOException, ConfigurationException
  {
    Options options = new Options();
    options.addOption(CMD_OPTION_LISTEN_ADDRESS, true, "Address to listen to. Default is " + DEFAULT_HOST + ":" + DEFAULT_PORT);
    options.addOption(CMD_OPTION_CONFIG_LOCATION, true, "Configuration location url. Default is " + DEFAULT_CONFIG_LOCATION);
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String listenAddress = cmd.getOptionValue(CMD_OPTION_LISTEN_ADDRESS);
    if (listenAddress != null) {

      Pattern pattern = Pattern.compile("(.+:)?(\\d+)");
      Matcher matcher = pattern.matcher(listenAddress);

      if (matcher.find()) {
        String hostString = matcher.group(1);
        if (hostString != null) {
          host = hostString.substring(0, hostString.length() - 1);
        }
        port = Integer.valueOf(matcher.group(2));
      } else {
        throw new ParseException("listenAddress must be in this format: [host:]port");
      }

    }

    String configLocation = cmd.getOptionValue(CMD_OPTION_CONFIG_LOCATION);
    if (configLocation == null) {
      configLocation = DEFAULT_CONFIG_LOCATION;
    }

    if (configLocation.startsWith("file:")) {
      try {
        Path path = Paths.get(new URL(configLocation).toURI());
        configuration = new FileConfiguration(path.toString());
      } catch (URISyntaxException ex) {
        throw new ParseException("configLocation is not a valid url");
      }
    } else if (configLocation.startsWith("jdbc:")) {
      try {
        configuration = new JDBCConfiguration(configLocation);
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      }
    } else {
      throw new ParseException("configLocation only supports file and jdbc urls");
    }
    configuration.load();
    cluster = new YarnCluster();

    AtratoConfiguration.Entry kerberosPrincipal = configuration.get(CONFIG_KEY_SECURITY_KERBEROS_PRINCIPAL);
    AtratoConfiguration.Entry kerberosKeyTab = configuration.get(CONFIG_KEY_SECURITY_KERBEROS_KEYTAB);

    if (kerberosPrincipal != null && kerberosKeyTab != null) {
      StramUserLogin.authenticate(kerberosPrincipal.getValue(), kerberosKeyTab.getValue());
    }
  }

  void run() throws Exception
  {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    String staticResourceBase = configuration.getValue(CONFIG_KEY_STATIC_RESOURCE_BASE);
    if (staticResourceBase != null) {
      context.setResourceBase(staticResourceBase);
    } else {
      staticResourceBase = "/home/david";
      context.setResourceBase("/home/david");
    }

    // for some reason, this is not working and it always returns 404. that's why I'm not using injection for now
    //ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig()));
    //context.addServlet(jerseyServlet, "/ws/*");

    ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/ws/*");

    InetSocketAddress address = new InetSocketAddress(host, port);
    Server jettyServer = new Server(address);
    jettyServer.setHandler(context);

    jerseyServlet.setInitOrder(0);

    jerseyServlet.setInitParameter(
        "jersey.config.server.provider.classnames",
        io.atrato.server.provider.ws.v1.RootProvider.class.getCanonicalName());

    if (staticResourceBase != null) {
      ServletHolder staticFilesServlet = context.addServlet(DefaultServlet.class, "/");
      staticFilesServlet.setInitOrder(10);
    }

    try {
      jettyServer.start();
      jettyServer.join();
    } finally {
      jettyServer.destroy();
    }
  }

  private ResourceConfig resourceConfig()
  {
    return new ResourceConfig().register(new AbstractBinder()
    {
      @Override
      protected void configure()
      {
        bind(new YarnCluster()).to(Cluster.class);
      }
    });
  }

  public static void main(String[] args) throws Exception
  {
    AtratoServer as = new AtratoServer();
    as.init(args);
    as.run();
  }
}
