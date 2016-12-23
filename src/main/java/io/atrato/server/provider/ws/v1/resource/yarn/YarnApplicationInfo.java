package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Set;

import org.apache.hadoop.yarn.api.records.ApplicationReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnApplicationInfo implements ApplicationInfo
{
  private final ApplicationReport yarnApplicationReport;

  public YarnApplicationInfo(ApplicationReport yarnApplicationReport)
  {
    this.yarnApplicationReport = yarnApplicationReport;
  }

  @Override
  public String getId()
  {
    return yarnApplicationReport.getApplicationId().toString();
  }

  @Override
  public String getName()
  {
    return yarnApplicationReport.getName();
  }

  @Override
  public String getState()
  {
    return yarnApplicationReport.getYarnApplicationState().toString();
  }

  @Override
  public String getUser()
  {
    return yarnApplicationReport.getUser();
  }

  public Set<String> getTags()
  {
    return yarnApplicationReport.getApplicationTags();
  }

  public String getTrackingUrl()
  {
    return yarnApplicationReport.getTrackingUrl();
  }

  public String getQueue()
  {
    return yarnApplicationReport.getQueue();
  }

}
