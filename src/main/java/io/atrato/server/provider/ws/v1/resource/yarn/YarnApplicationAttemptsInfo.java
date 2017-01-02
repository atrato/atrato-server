package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;

/**
 * Created by david on 12/31/16.
 */
public class YarnApplicationAttemptsInfo implements ApplicationAttemptsInfo
{
  private Map<String, ApplicationAttemptInfo> attempts = new TreeMap<>();

  public YarnApplicationAttemptsInfo(List<ApplicationAttemptReport> attemptReports)
  {
    for (ApplicationAttemptReport attemptReport : attemptReports) {
      attempts.put(attemptReport.getApplicationAttemptId().toString(), new YarnApplicationAttemptInfo(attemptReport));
    }
  }

  @Override
  public Collection<ApplicationAttemptInfo> getAttempts()
  {
    return attempts.values();
  }

  @Override
  public ApplicationAttemptInfo getAttempt(String attemptId)
  {
    return attempts.get(attemptId);
  }
}
