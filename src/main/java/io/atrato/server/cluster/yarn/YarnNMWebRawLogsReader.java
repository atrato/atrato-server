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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.ConverterUtils;

import com.sun.jersey.api.client.WebResource;
import com.datatorrent.stram.util.WebServicesClient;

import io.atrato.server.util.LineReader;

/**
 * Created by david on 1/14/17.
 */
public class YarnNMWebRawLogsReader extends YarnContainerLogsReader
{
  private static final String URL_PATTERN = "(https?://[^/]+)/";
  private static final String LOG_FILE_PATTERN = "<TR><TD><A HREF=\".+\">(.+)&nbsp;</TD><TD ALIGN=right>(\\d+) bytes&nbsp;</TD><TD>.*</TD></TR>";
  private String logUrl;
  private Map<String, Long> fileSizes = new HashMap<>();

  public YarnNMWebRawLogsReader(YarnConfiguration conf, ApplicationReport applicationReport, ContainerReport containerReport) throws IOException
  {
    String containerLogUrl = containerReport.getLogUrl();
    if (containerLogUrl.startsWith("//")) {
      containerLogUrl = "http:" + containerLogUrl;
    }

    String logDirs = conf.get(YarnConfiguration.NM_LOG_DIRS);
    if (logDirs.startsWith("${yarn.log.dir}/")) {
      logDirs = logDirs.substring(16);
    } else if (logDirs.endsWith("/containers")) {
      // Bigtop/EMR
      logDirs = "containers";
    } else {
      throw new IllegalStateException("Cannot resolve log dir with "
          + YarnConfiguration.NM_LOG_DIRS + "=" + logDirs);
    }

    Matcher m = Pattern.compile(URL_PATTERN).matcher(containerLogUrl);
    if (!m.find()) {
      throw new IllegalStateException("Cannot get base URL from " + containerLogUrl);
    }

    String baseUrl = m.group(1);
    logUrl = baseUrl + "/logs/" + logDirs + "/" + applicationReport.getApplicationId().toString() + "/" + containerReport.getContainerId().toString();

    WebServicesClient webServicesClient = new WebServicesClient();
    WebResource wr = webServicesClient.getClient().resource(logUrl);
    WebResource.Builder builder = wr.getRequestBuilder();

    try (InputStream is = webServicesClient.process(builder, InputStream.class, new WebServicesClient.GetWebServicesHandler<InputStream>());
        BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
      Pattern htmlPattern = Pattern.compile(LOG_FILE_PATTERN);
      String line;
      while ((line = br.readLine()) != null) {
        m = htmlPattern.matcher(line);
        if (m.find()) {
          long size = Long.parseLong(m.group(2));
          fileSizes.put(m.group(1), size);
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
    if (!fileSizes.containsKey(name)) {
      throw new FileNotFoundException();
    }

    String url = logUrl + "/" + name;
    WebServicesClient webServicesClient = new WebServicesClient();
    WebResource wr = webServicesClient.getClient().resource(url);
    WebResource.Builder builder = wr.getRequestBuilder();
    if (offset > 0) {
      builder.header("Range", offset + "-");
    } else if (offset < 0) {
      builder.header("Range", (fileSizes.get(name) + offset) + "-");
    }
    InputStream is = webServicesClient
        .process(builder, InputStream.class, new WebServicesClient.GetWebServicesHandler<InputStream>());

    return new LineReader(new BufferedReader(new InputStreamReader(is)));
  }

  @Override
  public void close() throws IOException
  {
  }

  public static void main(String[] args) throws Exception
  {
    YarnConfiguration conf = new YarnConfiguration();
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(conf);
    yarnClient.start();
    ApplicationReport ar = yarnClient.getApplicationReport(ConverterUtils.toApplicationId("application_1484376589867_0002"));
    ContainerReport cr = yarnClient.getContainerReport(ConverterUtils.toContainerId("container_1484376589867_0002_01_000001"));
    YarnNMWebRawLogsReader reader = new YarnNMWebRawLogsReader(conf, ar, cr);
    System.out.println(reader.getFileSizes());
    LineReader logFileReader = reader.getLogFileReader("dt.log", 0);
    String line;
    while ((line = logFileReader.readLine()) != null) {
      System.out.println("##### " + line);
    }
  }

}
