package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import io.atrato.server.provider.ws.v1.resource.ContainerLogInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogsInfo;

/**
 * Created by david on 1/7/17.
 */
public class YarnContainerLogsInfo implements ContainerLogsInfo
{
  private final Map<String, ContainerLogInfo> logs = new TreeMap<>();

  public void addLog(String name, long size)
  {
    logs.put(name, new YarnContainerLogInfo(name, size));
  }

  @Override
  public Collection<ContainerLogInfo> getLogs()
  {
    return logs.values();
  }

  @Override
  public ContainerLogInfo getLog(String name)
  {
    return logs.get(name);
  }
}
