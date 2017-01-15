package io.atrato.server.provider.ws.v1.resource;

/**
 * Created by david on 12/30/16.
 */
public interface ContainerInfo
{
  String getId();

  String getState();

  long getCreationTime();

  long getFinishTime();

  int getExitStatus();

  String getDiagnosticsInfo();

  String getAssignedNode();
}
