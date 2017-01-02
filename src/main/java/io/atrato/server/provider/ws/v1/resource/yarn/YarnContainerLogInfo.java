package io.atrato.server.provider.ws.v1.resource.yarn;

import io.atrato.server.provider.ws.v1.resource.ContainerLogInfo;

/**
 * Created by david on 1/7/17.
 */
public class YarnContainerLogInfo implements ContainerLogInfo
{
  private final String name;
  private final long size;

  public YarnContainerLogInfo(String name, long size)
  {
    this.name = name;
    this.size = size;
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public long getSize()
  {
    return size;
  }
}
