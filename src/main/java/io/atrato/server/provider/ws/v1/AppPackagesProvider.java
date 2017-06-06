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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import io.atrato.server.provider.ws.v1.resource.AppPackageInfo;
import io.atrato.server.provider.ws.v1.resource.AppPackagesInfo;

/**
 * Created by david on 1/6/17.
 */
public class AppPackagesProvider
{
  public static final AppPackagesProvider INSTANCE = new AppPackagesProvider();

  @GET
  public AppPackagesInfo getAllAppPackages()
  {
    return null;
  }

  @GET
  @Path("{user}")
  public AppPackageInfo getUserAppPackages(@PathParam("user") String user)
  {
    return null;
  }

  @GET
  @Path("{user}/{name}")
  public AppPackageInfo getAppPackageVersions(@PathParam("user") String user, @PathParam("name") String name)
  {
    return null;
  }

  @GET
  @Path("{user}/{name}/{version}")
  public AppPackageInfo getAppPackage(@PathParam("user") String user, @PathParam("name") String name, @PathParam("version") String version)
  {
    return null;
  }
}
