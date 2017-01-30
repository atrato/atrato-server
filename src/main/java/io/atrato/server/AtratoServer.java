package io.atrato.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Throwables;

import com.datatorrent.common.util.JacksonObjectMapperProvider;
import com.datatorrent.stram.security.StramUserLogin;
import com.datatorrent.stram.util.VersionInfo;

import io.atrato.server.apppackage.AppPackageRepository;
import io.atrato.server.apppackage.JDBCAppPackageRepository;
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
  private static final Logger LOG = LoggerFactory.getLogger(AtratoServer.class);

  @Provider
  public static class JsonProvider extends JacksonObjectMapperProvider {}

  private static final String DEFAULT_HOST = "0.0.0.0";
  private static final int DEFAULT_PORT = 8800;

  public static final String CONFIG_KEY_PREFIX = "atrato.server.";
  private static final String CONFIG_KEY_STATIC_RESOURCE_BASE = CONFIG_KEY_PREFIX + "staticResourceBase";
  private static final String CONFIG_KEY_LISTEN_ADDRESS = CONFIG_KEY_PREFIX + "listenAddress";
  private static final String CONFIG_KEY_SECURITY_PREFIX = CONFIG_KEY_PREFIX + "security.";
  private static final String CONFIG_KEY_SECURITY_KERBEROS_PRINCIPAL = CONFIG_KEY_SECURITY_PREFIX + "kerberos.principal";
  private static final String CONFIG_KEY_SECURITY_KERBEROS_KEYTAB = CONFIG_KEY_SECURITY_PREFIX + "kerberos.keytab";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private static String atratoHomeDir;
  private static String staticResourceBaseDir;

  private static String hadoopLocation;
  private static String configLocation;
  private static AtratoConfiguration configuration;
  private static AppPackageRepository appPackageRepository;
  private static Cluster cluster;

  private static String groupId = "io.atrato";
  private static String artifactId = "atrato-server";
  private static Class<?> classInJar = AtratoServer.class;
  private static String gitPropertiesResource = artifactId + ".git.properties";
  private static List<Issue> issues = new ArrayList<>();

  private static final String CMD_OPTION_LISTEN_ADDRESS = "listenAddress";
  private static final String CMD_OPTION_CONFIG_LOCATION = "configLocation";
  private static final String CMD_OPTION_KERBEROS_PRINCIPAL = "kerberosPrincipal";
  private static final String CMD_OPTION_KERBEROS_KEYTAB = "kerberosKeytab";
  private static final String CMD_OPTION_APP_PACKAGE_REPOSITORY_LOCATION = "appPackageRepositoryLocation";

  private static final String ENV_ATRATO_HOME = "ATRATO_HOME";
  private static final String ENV_ATRATO_HADOOP_CMD = "ATRATO_HADOOP_CMD";
  private static final String ENV_ATRATO_CONFIG_LOCATION = "ATRATO_CONFIG_LOCATION";

  private static final String DEFAULT_CONFIG_LOCATION = "jdbc:derby:${ATRATO_HOME}/db;create=true";
  private static final String DEFAULT_APP_PACKAGE_REPOSITORY_LOCATION = "jdbc:derby:${ATRATO_HOME}/db;create=true";

  public static final VersionInfo ATRATO_SERVER_VERSION = new VersionInfo(classInJar, groupId, artifactId, gitPropertiesResource);

  public static AtratoConfiguration getConfiguration()
  {
    return configuration;
  }

  public static AppPackageRepository getAppPackageRepository()
  {
    return appPackageRepository;
  }

  public static Cluster getCluster()
  {
    return cluster;
  }

  public static void addIssue(Issue issue)
  {
    issues.add(issue);
  }

  public static List<Issue> getIssues()
  {
    return issues;
  }

  public static void setHadoopLocation(String hadoopLocation) throws IOException
  {
    if (!hadoopLocation.equals(getHadoopLocation())) {
      AtratoServer.hadoopLocation = hadoopLocation;
      saveCustomEnvFile();
      addIssue(new Issue(Issue.IssueKey.RESTART_NEEDED, "Restarted needed because Hadoop location has been changed"));
    }
  }

  public static String getHadoopLocation()
  {
    return hadoopLocation;
  }

  public static void setConfigLocation(String configLocation) throws IOException
  {
    if (!configLocation.equals(getConfigLocation())) {
      AtratoServer.configLocation = configLocation;
      saveCustomEnvFile();
      addIssue(new Issue(Issue.IssueKey.RESTART_NEEDED, "Restarted needed because config location has been changed"));
    }
  }

  public static String getConfigLocation()
  {
    return configLocation;
  }

  private static void saveCustomEnvFile() throws IOException
  {
    try (PrintWriter writer = new PrintWriter(atratoHomeDir + "/conf/env-custom.sh", "UTF-8")) {
      writer.println("export " + ENV_ATRATO_HADOOP_CMD + "=\"" + hadoopLocation + "\"");
      writer.println("export " + ENV_ATRATO_CONFIG_LOCATION + "=\"" + configLocation + "\"");
    }
  }


  void init(String[] args) throws ParseException, IOException, ConfigurationException
  {
    Options options = new Options();
    options.addOption(CMD_OPTION_LISTEN_ADDRESS, true, "Address to listen to. Default is " + DEFAULT_HOST + ":" + DEFAULT_PORT);
    options.addOption(CMD_OPTION_KERBEROS_PRINCIPAL, true, "Kerberos Principal");
    options.addOption(CMD_OPTION_KERBEROS_KEYTAB, true, "Kerberos Keytab");
    options.addOption(CMD_OPTION_CONFIG_LOCATION, true, "Configuration location url. Default is " + DEFAULT_CONFIG_LOCATION);
    options.addOption(CMD_OPTION_APP_PACKAGE_REPOSITORY_LOCATION, true, "App Package Repository location url. Default is " + DEFAULT_APP_PACKAGE_REPOSITORY_LOCATION);

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd = parser.parse(options, args);

    String listenAddress = cmd.getOptionValue(CMD_OPTION_LISTEN_ADDRESS);
    String kerberosPrincipal = cmd.getOptionValue(CMD_OPTION_KERBEROS_PRINCIPAL);
    String kerberosKeytab = cmd.getOptionValue(CMD_OPTION_KERBEROS_KEYTAB);
    configLocation = cmd.getOptionValue(CMD_OPTION_CONFIG_LOCATION);
    String appPackageRepositoryLocation = cmd.getOptionValue(CMD_OPTION_APP_PACKAGE_REPOSITORY_LOCATION);

    atratoHomeDir = System.getenv(ENV_ATRATO_HOME);
    if (atratoHomeDir == null) {
      LOG.info(ENV_ATRATO_HOME + " is not set. Assuming development mode.");
      atratoHomeDir = System.getProperty("user.dir") + "/target/atrato_home";
    }
    createDirectories();

    hadoopLocation = System.getenv(ENV_ATRATO_HADOOP_CMD);

    if (configLocation == null) {
      String configLocationEnv = System.getenv("ATRATO_CONFIG_LOCATION");
      configLocation = configLocationEnv == null ? DEFAULT_CONFIG_LOCATION : configLocationEnv;
    }
    configLocation = configLocation.replace("${ATRATO_HOME}", atratoHomeDir);

    if (appPackageRepositoryLocation == null) {
      appPackageRepositoryLocation = DEFAULT_APP_PACKAGE_REPOSITORY_LOCATION;
    }
    appPackageRepositoryLocation = appPackageRepositoryLocation.replace("${ATRATO_HOME}", atratoHomeDir);

    System.setProperty("derby.stream.error.file", atratoHomeDir + "/logs/derby.log");

    try {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    } catch (ClassNotFoundException ex) {
      throw Throwables.propagate(ex);
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

    if (appPackageRepositoryLocation.startsWith("jdbc:")) {
      try {
        appPackageRepository = new JDBCAppPackageRepository(appPackageRepositoryLocation);
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      }
    } else {
      throw new ParseException("Only jdbc url is supported for appPackageRepositoryLocation for now");
    }

    cluster = new YarnCluster();

    if (listenAddress == null) {
      AtratoConfiguration.Entry listenAddressConfigEntry = configuration.get(CONFIG_KEY_LISTEN_ADDRESS);
      if (listenAddressConfigEntry != null) {
        listenAddress = listenAddressConfigEntry.getValue();
      }
    }

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

    if (kerberosPrincipal == null || kerberosKeytab == null) {
      AtratoConfiguration.Entry kerberosPrincipalConfigEntry = configuration.get(CONFIG_KEY_SECURITY_KERBEROS_PRINCIPAL);
      AtratoConfiguration.Entry kerberosKeytabConfigEntry = configuration.get(CONFIG_KEY_SECURITY_KERBEROS_KEYTAB);
      if (kerberosPrincipalConfigEntry != null) {
        kerberosPrincipal = kerberosPrincipalConfigEntry.getValue();
      }
      if (kerberosKeytabConfigEntry != null) {
        kerberosKeytab = kerberosKeytabConfigEntry.getValue();
      }
    }
    if (kerberosPrincipal != null && kerberosKeytab != null) {
      StramUserLogin.authenticate(kerberosPrincipal, kerberosKeytab);
    }
    staticResourceBaseDir = configuration.getValue(CONFIG_KEY_STATIC_RESOURCE_BASE);
    if (staticResourceBaseDir == null) {
      staticResourceBaseDir = atratoHomeDir + "/htdocs";
    }
  }

  private void createDirectories() throws IOException
  {
    Files.createDirectories(FileSystems.getDefault().getPath(atratoHomeDir, "htdocs"));
    Files.createDirectories(FileSystems.getDefault().getPath(atratoHomeDir, "conf"));
    Files.createDirectories(FileSystems.getDefault().getPath(atratoHomeDir, "logs"));
  }

  void run() throws Exception
  {
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    context.setResourceBase(staticResourceBaseDir);

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

    ServletHolder staticFilesServlet = context.addServlet(DefaultServlet.class, "/");
    staticFilesServlet.setInitOrder(10);

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
    LOG.info("Starting with classpath: {}", System.getProperty("java.class.path"));
    LOG.info("Working directory: {}", System.getProperty("user.dir"));
    Map<String, String> envs = System.getenv();
    LOG.info("\nDumping System Env: begin");
    for (Map.Entry<String, String> env : envs.entrySet()) {
      LOG.info("System env: key=" + env.getKey() + ", val=" + env.getValue());
    }
    LOG.info("Dumping System Env: end");

    AtratoServer as = new AtratoServer();
    as.init(args);
    as.run();
  }
}
