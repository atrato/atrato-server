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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.logaggregation.AggregatedLogFormat;
import org.apache.hadoop.yarn.logaggregation.LogAggregationUtils;
import org.apache.hadoop.yarn.util.ConverterUtils;

import io.atrato.server.util.LineReader;

/**
 * Created by david on 1/13/17.
 */
public class YarnAggregatedContainerLogsReader extends YarnContainerLogsReader
{
  private List<Path> matchedNodeFiles = new ArrayList<>();
  private Map<String, Long> fileSizes = new HashMap<>();
  private String containerId;
  private Configuration conf;
  private AggregatedLogFormat.LogReader currentReader;

  public YarnAggregatedContainerLogsReader(Configuration conf, ApplicationReport applicationReport, ContainerReport containerReport)
      throws IOException
  {
    this.containerId = containerReport.getContainerId().toString();
    this.conf = conf;

    Path remoteRootLogDir = new Path(conf.get(
        YarnConfiguration.NM_REMOTE_APP_LOG_DIR,
        YarnConfiguration.DEFAULT_NM_REMOTE_APP_LOG_DIR));
    String suffix = LogAggregationUtils.getRemoteNodeLogDirSuffix(conf);
    Path remoteAppLogDir = LogAggregationUtils.getRemoteAppLogDir(
        remoteRootLogDir, applicationReport.getApplicationId(), applicationReport.getUser(),
        suffix);
    RemoteIterator<FileStatus> nodeFiles;
    Path qualifiedLogDir =
        FileContext.getFileContext(conf).makeQualified(remoteAppLogDir);
    nodeFiles = FileContext.getFileContext(qualifiedLogDir.toUri(), conf)
        .listStatus(remoteAppLogDir);

    while (nodeFiles.hasNext()) {
      FileStatus thisNodeFile = nodeFiles.next();
      String fileName = thisNodeFile.getPath().getName();

      if (fileName.contains(LogAggregationUtils.getNodeString(containerReport.getAssignedNode()))
          && !fileName.endsWith(LogAggregationUtils.TMP_FILE_SUFFIX)) {
        AggregatedLogFormat.LogReader reader = new AggregatedLogFormat.LogReader(conf, thisNodeFile.getPath());
        try {
          final AggregatedLogFormat.LogKey containerKey = new AggregatedLogFormat.LogKey(containerId);
          AggregatedLogFormat.LogKey key = new AggregatedLogFormat.LogKey();
          DataInputStream valueStream = reader.next(key);
          while (valueStream != null && !key.equals(containerKey)) {
            valueStream = reader.next(key);
          }

          if (valueStream != null) {
            matchedNodeFiles.add(thisNodeFile.getPath());
            AggregatedLogFormat.ContainerLogsReader containerLogsReader = new AggregatedLogFormat.ContainerLogsReader(valueStream);

            while (containerLogsReader.nextLog() != null) {
              String name = containerLogsReader.getCurrentLogType();
              long size = containerLogsReader.getCurrentLogLength();
              fileSizes.put(name, size);
            }
          }
        } finally {
          reader.close();
        }
      }
    }
  }

  @Override
  public Map<String, Long> getFileSizes()
  {
    return fileSizes;
  }

  @Override
  public LineReader getLogFileReader(String name, long offset) throws IOException
  {
    if (fileSizes.containsKey(name)) {
      for (Path nodeFile : matchedNodeFiles) {
        AggregatedLogFormat.LogReader reader = new AggregatedLogFormat.LogReader(conf, nodeFile);
        try {
          final AggregatedLogFormat.LogKey containerKey = new AggregatedLogFormat.LogKey(containerId);
          AggregatedLogFormat.LogKey key = new AggregatedLogFormat.LogKey();
          DataInputStream valueStream = reader.next(key);
          while (valueStream != null && !key.equals(containerKey)) {
            valueStream = reader.next(key);
          }

          if (valueStream != null) {
            AggregatedLogFormat.ContainerLogsReader containerLogsReader = new AggregatedLogFormat.ContainerLogsReader(valueStream);

            while (containerLogsReader.nextLog() != null) {
              String logType = containerLogsReader.getCurrentLogType();
              if (logType.equals(name)) {
                currentReader = reader;
                BoundedInputStream is = new BoundedInputStream(valueStream, containerLogsReader.getCurrentLogLength());
                is.skip(offset);
                return new LineReader(new BufferedReader(new InputStreamReader(is)));
              }
            }
          }
        } catch (Throwable ex) {
          reader.close();
          throw ex;
        }
      }
    }
    throw new FileNotFoundException();
  }

  @Override
  public void close() throws IOException
  {
    if (currentReader != null) {
      currentReader.close();
    }
  }

  public static void main(String[] args) throws Exception
  {
    System.setProperty("hadoop.home.dir", "/home/david/hadoop");
    String appId = "application_1484376589867_0001";
    String containerId = "container_1484376589867_0001_01_000001";
    Configuration conf = new YarnConfiguration();
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(conf);
    yarnClient.start();
    ApplicationReport applicationReport = yarnClient.getApplicationReport(ConverterUtils.toApplicationId(appId));
    ContainerReport containerReport = yarnClient.getContainerReport(ConverterUtils.toContainerId(containerId));
    YarnAggregatedContainerLogsReader reader = new YarnAggregatedContainerLogsReader(conf, applicationReport, containerReport);

    System.out.println(reader.getFileSizes());
    try (LineReader lineReader = reader.getLogFileReader("dt.log", 0)) {
      while (true) {
        String line = lineReader.readLine();
        if (line == null) {
          break;
        }
        System.out.println("####  " + line);
      }
    }
  }
}
