package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

/**
 * Created by david on 1/6/17.
 */
public interface ContainerLogsInfo
{
  Collection<ContainerLogInfo> getLogs();

  ContainerLogInfo getLog(String name);
}
