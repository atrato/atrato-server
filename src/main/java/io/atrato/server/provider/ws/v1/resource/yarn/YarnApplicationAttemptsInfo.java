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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.util.ConverterUtils;

import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;

/**
 * Created by david on 12/31/16.
 */
public class YarnApplicationAttemptsInfo implements ApplicationAttemptsInfo
{
  private final Map<String, ApplicationAttemptInfo> attempts = new TreeMap<>();
  private final String appId;

  public YarnApplicationAttemptsInfo(String appId, List<ApplicationAttemptReport> attemptReports)
  {
    this.appId = appId;
    for (ApplicationAttemptReport attemptReport : attemptReports) {
      attempts.put(attemptReport.getApplicationAttemptId().toString(), new YarnApplicationAttemptInfo(attemptReport));
    }
  }

  @Override
  public Collection<ApplicationAttemptInfo> getAttempts()
  {
    return attempts.values();
  }

  @Override
  public ApplicationAttemptInfo getAttempt(String attemptId)
  {
    if (StringUtils.isNumeric(attemptId)) {
      attemptId = ApplicationAttemptId.newInstance(ConverterUtils.toApplicationId(appId), Integer.valueOf(attemptId)).toString();
    }
    return attempts.get(attemptId);
  }
}
