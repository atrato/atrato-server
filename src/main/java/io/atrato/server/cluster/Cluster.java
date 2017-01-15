package io.atrato.server.cluster;

import java.io.IOException;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;

/**
 * Created by david on 12/30/16.
 */
public interface Cluster
{
  ApplicationsInfo getApplicationsInfo();

  ApplicationInfo getApplicationInfo(String appId);

  ContainersInfo getContainersInfo(String appId);

  ContainerInfo getContainerInfo(String appId, String containerId);

  ApplicationAttemptsInfo getApplicationAttemptsInfo(String appId);

  ApplicationAttemptInfo getApplicationAttemptInfo(String appId, String attemptId);

  ContainersInfo getContainersInfo(String appId, String attemptId);

  ContainerLogsInfo getContainerLogsInfo(String appId, String containerId);

  ContainerLogInfo getContainerLogInfo(String appId, String containerId, String name);

  ContainerLogsReader getContainerLogsReader(String appId, String containerId) throws IOException;

  void killApplication(String appId);
}
