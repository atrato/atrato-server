package io.atrato.server.provider.ws.v1.resource.yarn;

import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnApplicationAttemptInfo implements ApplicationAttemptInfo
{
  private final ApplicationAttemptReport applicationAttemptReport;

  public YarnApplicationAttemptInfo(ApplicationAttemptReport applicationAttemptReport)
  {
    this.applicationAttemptReport = applicationAttemptReport;
  }

  @Override
  public String getId()
  {
    return this.applicationAttemptReport.getApplicationAttemptId().toString();
  }

  @Override
  public String getState()
  {
    return this.applicationAttemptReport.getYarnApplicationAttemptState().toString();
  }
}


