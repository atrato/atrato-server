package io.atrato.server.cluster.yarn;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import io.atrato.server.cluster.ContainerLogsReader;

/**
 * Created by david on 1/12/17.
 */
public abstract class YarnContainerLogsReader implements ContainerLogsReader
{
  public static YarnContainerLogsReader create(YarnCluster cluster, String appId, String containerId) throws FileNotFoundException
  {
    ApplicationReport applicationReport = cluster.getApplicationReport(appId);
    ContainerReport containerReport = cluster.getContainerReport(containerId);

    if (YarnCluster.isApplicationTerminatedState(applicationReport.getYarnApplicationState())) {
      // check yarn aggregated log first if application is terminated
      try {
        return new YarnAggregatedContainerLogsReader(new YarnConfiguration(), applicationReport, containerReport);
      } catch (IOException ex) {
        // fall through
      }
    }
    try {
      return new YarnNMWebRawLogsReader(new YarnConfiguration(), applicationReport, containerReport);
    } catch (IOException ex) {
      // fall through
    }
    try {
      return new YarnNMHtmlLogsReader(containerReport);
    } catch (IOException ex) {
      // fall through
    }
    throw new FileNotFoundException("Container logs cannot be found");
  }

}
