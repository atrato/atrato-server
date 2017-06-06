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
