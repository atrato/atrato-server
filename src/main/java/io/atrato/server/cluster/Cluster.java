/**
 * Copyright (c) 2017 Atrato, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
