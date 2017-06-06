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

import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnApplicationAttemptInfo implements ApplicationAttemptInfo
{
  private final ApplicationAttemptReport applicationAttemptReport;

  public YarnApplicationAttemptInfo(ApplicationAttemptReport applicationAttemptReport)
  {
    this.applicationAttemptReport = applicationAttemptReport;
  }

  @Override
  public String getId()
  {
    return this.applicationAttemptReport.getApplicationAttemptId().toString();
  }

  @Override
  public String getState()
  {
    return this.applicationAttemptReport.getYarnApplicationAttemptState().toString();
  }
}


