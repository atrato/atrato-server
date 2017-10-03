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
package io.atrato.server.cluster.yarn;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atrato.server.cluster.ContainerLogsReader;

/**
 * Created by david on 1/12/17.
 */
public abstract class YarnContainerLogsReader implements ContainerLogsReader
{
  private static final Logger LOG = LoggerFactory.getLogger(YarnContainerLogsReader.class);

  public static YarnContainerLogsReader create(YarnCluster cluster, String appId, String containerId) throws FileNotFoundException
  {
    ApplicationReport applicationReport = cluster.getApplicationReport(appId);
    ContainerReport containerReport = cluster.getContainerReport(containerId);

    if (YarnCluster.isApplicationTerminatedState(applicationReport.getYarnApplicationState())) {
      // check yarn aggregated log first if application is terminated
      try {
        return new YarnAggregatedContainerLogsReader(new YarnConfiguration(), applicationReport, containerReport);
      } catch (IOException ex) {
        // fall through
      }
    }
    try {
      return new YarnNMWebRawLogsReader(new YarnConfiguration(), applicationReport, containerReport);
    } catch (Exception ex) {
      LOG.warn("Cannot read raw logs", ex); // for debugging
      // fall through
    }
    try {
      return new YarnNMHtmlLogsReader(containerReport);
    } catch (Exception ex) {
      LOG.warn("Cannot read html logs", ex); // for debugging
      // fall through
    }
    throw new FileNotFoundException("Container logs cannot be found");
  }

}
