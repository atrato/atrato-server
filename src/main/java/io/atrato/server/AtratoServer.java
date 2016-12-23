package io.atrato.server;

import java.io.IOException;
import java.net.InetSocketAddress;
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

import com.datatorrent.common.util.JacksonObjectMapperProvider;
import com.datatorrent.stram.util.VersionInfo;

import io.atrato.server.cluster.Cluster;
import io.atrato.server.cluster.yarn.YarnCluster;
import io.atrato.server.config.AtratoConfiguration;
import io.atrato.server.config.ConfigurationException;
import io.atrato.server.config.FileConfiguration;

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
  private static final String CONFIG_KEY_ADDRESS = CONFIG_KEY_PREFIX + "address";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private AtratoConfiguration atratoConfiguration;

  private static String groupId = "io.atrato";
  private static String artifactId = "atrato-server";
  private static Class<?> classInJar = AtratoServer.class;
  private static String gitPropertiesResource = artifactId + ".git.properties";

  public static final VersionInfo ATRATO_SERVER_VERSION = new VersionInfo(classInJar, groupId, artifactId, gitPropertiesResource);

  void init(String[] args) throws ParseException, IOException, ConfigurationException
  {
    Options options = new Options();
    options.addOption("address", true, "Address to listen to. Default is " + DEFAULT_HOST + ":" + DEFAULT_PORT);
    options.addOption("configLocation", true, "Configuration agent. Default is ");
    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String address = cmd.getOptionValue("address");
    if (address != null) {

      Pattern pattern = Pattern.compile("(.+:)?(\\d+)");
      Matcher matcher = pattern.matcher(address);

      if (matcher.find()) {
        String hostString = matcher.group(1);
        if (hostString != null) {
          host = hostString.substring(0, hostString.length() - 1);
        }
        port = Integer.valueOf(matcher.group(2));
      } else {
        throw new ParseException("address must be in this format: [host:]port");
      }

    }

    atratoConfiguration = new FileConfiguration("/tmp/atrato.json");
    atratoConfiguration.load();
  }

  void run() throws Exception
  {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    String staticResourceBase = atratoConfiguration.getValue(CONFIG_KEY_STATIC_RESOURCE_BASE);
    if (staticResourceBase != null) {
      context.setResourceBase(staticResourceBase);
    } else {
      staticResourceBase = "/home/david";
      context.setResourceBase("/home/david");
    }

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
