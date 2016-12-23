package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

/**
 * Created by david on 12/30/16.
 */
public interface ApplicationsInfo
{
  Collection<ApplicationInfo> getApplications();
  ApplicationInfo getApplication(String appId);
}
