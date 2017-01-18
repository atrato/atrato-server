package io.atrato.server.provider.ws.v1;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.atrato.server.AtratoServer;
import io.atrato.server.config.AtratoConfiguration;
import io.atrato.server.provider.ws.v1.resource.ConfigurationsInfo;
import io.atrato.server.provider.ws.v1.resource.NullInfo;

/**
 * Created by david on 1/15/17.
 */
public class ConfigProvider
{
  public static final ConfigProvider INSTANCE = new ConfigProvider();

  @Inject //TODO: need to make this injection work in the future
  private AtratoConfiguration conf;

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
    return NullInfo.INSTANCE;
  }
}
