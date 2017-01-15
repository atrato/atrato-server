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
