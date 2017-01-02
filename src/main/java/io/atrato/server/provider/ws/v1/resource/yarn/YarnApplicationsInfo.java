package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.yarn.api.records.ApplicationReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnApplicationsInfo implements ApplicationsInfo
{
  private final Map<String, ApplicationInfo> applications = new TreeMap<>();

  public YarnApplicationsInfo(List<ApplicationReport> applicationReportListList)
  {
    for (ApplicationReport applicationReport : applicationReportListList) {
      applications.put(applicationReport.getApplicationId().toString(), new YarnApplicationInfo(applicationReport));
    }
  }

  @Override
  public Collection<ApplicationInfo> getApplications()
  {
    return applications.values();
  }

  @Override
  public ApplicationInfo getApplication(String appId)
  {
    return applications.get(appId);
  }
}
