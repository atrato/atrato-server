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
package io.atrato.server.provider.ws.v1.resource.yarn;

import org.apache.hadoop.yarn.api.records.ContainerReport;

import io.atrato.server.provider.ws.v1.resource.ContainerInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnContainerInfo implements ContainerInfo
{
  private final ContainerReport containerReport;

  public YarnContainerInfo(ContainerReport containerReport)
  {
    this.containerReport = containerReport;
  }

  @Override
  public String getId()
  {
    return this.containerReport.getContainerId().toString();
  }

  @Override
  public String getState()
  {
    return this.containerReport.getContainerState().toString();
  }

  @Override
  public long getCreationTime()
  {
    return this.containerReport.getCreationTime();
  }

  @Override
  public long getFinishTime()
  {
    return this.containerReport.getFinishTime();
  }

  @Override
  public int getExitStatus()
  {
    return this.containerReport.getContainerExitStatus();
  }

  @Override
  public String getDiagnosticsInfo()
  {
    return this.containerReport.getDiagnosticsInfo();
  }

  @Override
  public String getAssignedNode()
  {
    return this.containerReport.getAssignedNode().toString();
  }

}
