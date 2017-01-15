package io.atrato.server.apppackage;

import java.util.Collection;

import io.atrato.server.provider.ws.v1.resource.AppPackageInfo;

/**
 * Created by david on 1/10/17.
 */
public class JDBCAppPackageRepository implements AppPackageRepository
{
  @Override
  public AppPackageInfo getAppPackageInfo(String owner, String name, String version)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackages(String owner, String name)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackages(String owner)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackagesAccessibleBy(String user)
  {
    return null;
  }

  @Override
  public void addAppPackage(AppPackageInfo appPackageInfo)
  {

  }
}
