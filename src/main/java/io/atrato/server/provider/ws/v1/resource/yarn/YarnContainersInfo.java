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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.yarn.api.records.ContainerReport;

import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;

/**
 * Created by david on 12/31/16.
 */
public class YarnContainersInfo implements ContainersInfo
{
  private Map<String, ContainerInfo> containers = new TreeMap<>();

  public YarnContainersInfo(List<ContainerReport> containerReports)
  {
    for (ContainerReport containerReport : containerReports) {
      containers.put(containerReport.getContainerId().toString(), new YarnContainerInfo(containerReport));
    }
  }
  @Override
  public Collection<ContainerInfo> getContainers()
  {
    return containers.values();
  }

  @Override
  public ContainerInfo getContainer(String containerId)
  {
    return containers.get(containerId);
  }
}
