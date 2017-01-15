package io.atrato.server.provider.ws.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.atrato.server.provider.ws.v1.resource.AppPackageInfo;
import io.atrato.server.provider.ws.v1.resource.AppPackagesInfo;

/**
 * Created by david on 1/6/17.
 */
public class AppPackagesProvider
{
  public static final AppPackagesProvider INSTANCE = new AppPackagesProvider();

  @GET
  public AppPackagesInfo getAllAppPackages()
  {
    return null;
  }

  @GET
  @Path("{user}")
  public AppPackageInfo getUserAppPackages(@PathParam("user") String user)
  {
    return null;
  }

  @GET
  @Path("{user}/{name}")
  public AppPackageInfo getAppPackageVersions(@PathParam("user") String user, @PathParam("name") String name)
  {
    return null;
  }

  @GET
  @Path("{user}/{name}/{version}")
  public AppPackageInfo getAppPackage(@PathParam("user") String user, @PathParam("name") String name, @PathParam("version") String version)
  {
    return null;
  }
}
