package io.atrato.server.cluster;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;

/**
 * Created by david on 12/30/16.
 */
public interface Cluster
{
  ApplicationsInfo getApplicationsResource();

  ApplicationInfo getApplicationResource(String appId);

  ContainersInfo getContainersResource(String appId);

  ContainerInfo getContainerResource(String containerId);

  ApplicationAttemptsInfo getApplicationAttemptsResource(String appId);

  ApplicationAttemptInfo getApplicationAttemptResource(String attemptId);
}
