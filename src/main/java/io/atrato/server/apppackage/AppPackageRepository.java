package io.atrato.server.apppackage;

import java.util.Collection;

import io.atrato.server.provider.ws.v1.resource.AppPackageInfo;

/**
 * Created by david on 1/7/17.
 */
public interface AppPackageRepository
{
  AppPackageInfo getAppPackageInfo(String owner, String name, String version);

  Collection<AppPackageInfo> getAppPackages(String owner, String name);

  Collection<AppPackageInfo> getAppPackages(String owner);

  Collection<AppPackageInfo> getAppPackagesAccessibleBy(String user);

  void addAppPackage(AppPackageInfo appPackageInfo);
}
