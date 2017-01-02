package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.util.ConverterUtils;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;

/**
 * Created by david on 12/31/16.
 */
public class YarnApplicationAttemptsInfo implements ApplicationAttemptsInfo
{
  private final Map<String, ApplicationAttemptInfo> attempts = new TreeMap<>();
  private final String appId;

  public YarnApplicationAttemptsInfo(String appId, List<ApplicationAttemptReport> attemptReports)
  {
    this.appId = appId;
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
    if (StringUtils.isNumeric(attemptId)) {
      attemptId = ApplicationAttemptId.newInstance(ConverterUtils.toApplicationId(appId), Integer.valueOf(attemptId)).toString();
    }
    return attempts.get(attemptId);
  }
}
