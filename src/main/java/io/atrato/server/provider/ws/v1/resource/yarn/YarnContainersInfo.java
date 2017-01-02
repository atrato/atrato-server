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
