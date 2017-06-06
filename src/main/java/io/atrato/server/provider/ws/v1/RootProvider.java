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
package io.atrato.server.provider.ws.v1;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import io.atrato.server.provider.ws.v1.resource.AboutInfo;

import javax.ws.rs.core.MediaType;

/**
 * Created by david on 12/26/16.
 */
@Singleton
@Path("/v1")
public class RootProvider
{
  @Path("applications")
  public ApplicationsProvider getApplicationsProvider()
  {
    return ApplicationsProvider.INSTANCE;
  }

  @Path("appPackages")
  public AppPackagesProvider getAppPackagesProvider()
  {
    return AppPackagesProvider.INSTANCE;
  }

  @Path("config")
  public ConfigProvider getConfigProvider()
  {
    return ConfigProvider.INSTANCE;
  }

  @Path("issues")
  public IssuesProvider getIssuesProvider()
  {
    return IssuesProvider.INSTANCE;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("about")
  public AboutInfo getAboutResource()
  {
    return AboutInfo.INSTANCE;
  }

}
