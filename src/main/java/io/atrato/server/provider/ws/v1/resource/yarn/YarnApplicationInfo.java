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
package io.atrato.server.provider.ws.v1.resource.yarn;

import java.util.Set;

import org.apache.hadoop.yarn.api.records.ApplicationReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnApplicationInfo implements ApplicationInfo
{
  private final ApplicationReport yarnApplicationReport;

  public YarnApplicationInfo(ApplicationReport yarnApplicationReport)
  {
    this.yarnApplicationReport = yarnApplicationReport;
  }

  @Override
  public String getId()
  {
    return yarnApplicationReport.getApplicationId().toString();
  }

  @Override
  public String getName()
  {
    return yarnApplicationReport.getName();
  }

  @Override
  public String getState()
  {
    return yarnApplicationReport.getYarnApplicationState().toString();
  }

  @Override
  public String getOwner()
  {
    return yarnApplicationReport.getUser();
  }

  public Set<String> getTags()
  {
    return yarnApplicationReport.getApplicationTags();
  }

  public String getTrackingUrl()
  {
    return yarnApplicationReport.getTrackingUrl();
  }

  public String getQueue()
  {
    return yarnApplicationReport.getQueue();
  }

}
