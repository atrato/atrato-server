package io.atrato.server.provider.ws.v1;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.atrato.server.provider.ws.v1.resource.AboutInfo;

import javax.ws.rs.core.MediaType;

/**
 * Created by david on 12/26/16.
 */
@Singleton
@Path("/v1")
public class RootProvider
{
  @Path("applications")
  public ApplicationsProvider getApplicationsProvider()
  {
    return ApplicationsProvider.INSTANCE;
  }

  @Path("appPackages")
  public AppPackagesProvider getAppPackagesProvider()
  {
    return AppPackagesProvider.INSTANCE;
  }

  @Path("config")
  public ConfigProvider getConfigProvider()
  {
    return ConfigProvider.INSTANCE;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("about")
  public AboutInfo getAboutResource()
  {
    return AboutInfo.INSTANCE;
  }

}
