package io.atrato.server.provider.ws.v1;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atrato.server.AtratoServer;
import io.atrato.server.Issue;
import io.atrato.server.config.AtratoConfiguration;
import io.atrato.server.provider.ws.v1.resource.ConfigurationsInfo;
import io.atrato.server.provider.ws.v1.resource.NullInfo;
import io.atrato.server.provider.ws.v1.resource.StringValueInfo;

/**
 * Created by david on 1/15/17.
 */
public class ConfigProvider
{
  private static final Logger LOG = LoggerFactory.getLogger(ConfigProvider.class);
  public static final ConfigProvider INSTANCE = new ConfigProvider();

  /* TODO: need to make this injection work in the future
  @Inject
  private AtratoConfiguration conf;
  */

  private ConfigProvider()
  {
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ConfigurationsInfo getConfiguration()
  {
    return new ConfigurationsInfo(AtratoServer.getConfiguration());
  }

  @GET
  @Path("{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public AtratoConfiguration.Entry getConfigEntry(@PathParam("name") String name)
  {
    return AtratoServer.getConfiguration().get(name);
  }

  @PUT
  @Path("{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NullInfo setConfigEntry(AtratoConfiguration.Entry content, @PathParam("name") String name) throws IOException
  {
    AtratoConfiguration conf = AtratoServer.getConfiguration();
    conf.set(name, content.getValue(), content.getDescription());
    conf.save(false);
    if (name.startsWith(AtratoServer.CONFIG_KEY_PREFIX)) {
      AtratoServer.addIssue(new Issue(Issue.IssueKey.RESTART_NEEDED, "Restarted needed because config has been changed"));
    }
    return NullInfo.INSTANCE;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NullInfo setConfigEntries(ConfigurationsInfo content) throws IOException
  {
    boolean restartNeeded = false;
    AtratoConfiguration conf = AtratoServer.getConfiguration();
    for (AtratoConfiguration.Entry entry : content.getConfiguration()) {
      conf.set(entry.getName(), entry.getValue(), entry.getDescription());
      if (entry.getName().startsWith(AtratoServer.CONFIG_KEY_PREFIX)) {
        restartNeeded = true;
      }
    }
    conf.save(false);
    if (restartNeeded) {
      AtratoServer.addIssue(new Issue(Issue.IssueKey.RESTART_NEEDED, "Restarted needed because config has been changed"));
    }
    return NullInfo.INSTANCE;
  }


  @GET
  @Path("hadoopLocation")
  @Produces(MediaType.APPLICATION_JSON)
  public StringValueInfo getHadoopLocation() throws IOException
  {
    LOG.info("hadoop location: {}", AtratoServer.getHadoopLocation());
    return new StringValueInfo(AtratoServer.getHadoopLocation());
  }

  @PUT
  @Path("hadoopLocation")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NullInfo setHadoopLocation(StringValueInfo value) throws IOException
  {
    String hadoopLocation = value.getValue();
    if (!new File(hadoopLocation).canExecute()) {
      throw new ProcessingException(hadoopLocation + " is not executable");
    }
    AtratoServer.setHadoopLocation(hadoopLocation);
    return NullInfo.INSTANCE;
  }

  @GET
  @Path("configLocation")
  @Produces(MediaType.APPLICATION_JSON)
  public StringValueInfo getConfigLocation() throws IOException
  {
    return new StringValueInfo(AtratoServer.getConfigLocation());
  }

  @PUT
  @Path("configLocation")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public NullInfo setConfigLocation(StringValueInfo value) throws IOException
  {
    String configLocation = value.getValue();
    // TODO: sanity check for configLocation
    AtratoServer.setConfigLocation(configLocation);
    return NullInfo.INSTANCE;
  }

  @POST
  @Path("restart")
  @Produces(MediaType.APPLICATION_JSON)
  public NullInfo restart() throws IOException
  {
    new Thread()
    {
      @Override
      public void run()
      {
        try {
          Thread.sleep(2000);
          //TODO: We will need a guard script to start the server after exit
          System.exit(0);
        } catch (InterruptedException ex) {
          LOG.info("Restart aborted");
        }
      }
    }.run();
    return NullInfo.INSTANCE;
  }
}
