package io.atrato.server.provider.ws.v1.resource;

import com.datatorrent.stram.util.VersionInfo;

import io.atrato.server.AtratoServer;

/**
 * Created by david on 12/30/16.
 */
public class AboutInfo
{
  public static final AboutInfo INSTANCE = new AboutInfo();

  public String getJavaVersion()
  {
    return System.getProperty("java.version");
  }

  public VersionInfo getAtratoServerVersionInfo()
  {
    return AtratoServer.ATRATO_SERVER_VERSION;
  }

  public VersionInfo getApexVersionInfo()
  {
    return VersionInfo.APEX_VERSION;
  }

}
