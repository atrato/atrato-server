package io.atrato.server.provider.ws.v1.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by david on 1/7/17.
 */
public class AppPackagesInfo
{
  private final List<AppPackageInfo> appPackages = new ArrayList<>();

  public Collection<AppPackageInfo> getAppPackages()
  {
    return appPackages;
  }

  public void addAppPackage(AppPackageInfo appPackageInfo)
  {
    appPackages.add(appPackageInfo);
  }
}
