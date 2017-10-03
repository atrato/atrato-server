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
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.yarn.api.records.ContainerReport;

import com.sun.jersey.api.client.WebResource;

import com.datatorrent.stram.util.WebServicesClient;

import io.atrato.server.util.LineReader;

/**
 * Created by david on 1/14/17.
 */
public class YarnNMHtmlLogsReader extends YarnContainerLogsReader
{
  private Map<String, Long> fileSizes = new HashMap<>();
  private final String logUrl;

  public YarnNMHtmlLogsReader(ContainerReport containerReport) throws IOException
  {
    String content;
    this.logUrl = containerReport.getLogUrl();
    WebServicesClient webServicesClient = new WebServicesClient();
    WebResource wr = webServicesClient.getClient().resource(logUrl);
    WebResource.Builder builder = wr.getRequestBuilder();

    try (InputStream is = webServicesClient.process(builder, InputStream.class, new WebServicesClient.GetWebServicesHandler<InputStream>());
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      IOUtils.copy(is, baos);
      content = baos.toString();
    }

    // parse content
    Pattern pattern = Pattern.compile("<p>\\s*<a href=\"([^\\s?]+)(?:\\?start=[-]*\\d+)?\">(.*) : Total file length is (\\d+) bytes.</a>");
    Matcher m = pattern.matcher(content);
    while (m.find()) {
      //String location = m.group(1);
      String logName = m.group(2);
      long bytes = Long.valueOf(m.group(3));
      fileSizes.put(logName, bytes);
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
    String location = this.logUrl + "/" + name + "/?start=" + offset;
    WebServicesClient webServicesClient = new WebServicesClient();
    WebResource wr = webServicesClient.getClient().resource(location);
    WebResource.Builder builder = wr.getRequestBuilder();

    InputStream is = webServicesClient
        .process(builder, InputStream.class, new WebServicesClient.GetWebServicesHandler<InputStream>());
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
    return new LineReader(bufferedReader)
    {
      boolean hasStarted = false;
      boolean hasEnded = false;

      @Override
      protected boolean hasStarted()
      {
        return hasStarted;
      }

      @Override
      protected boolean hasEnded()
      {
        return hasEnded;
      }

      @Override
      public String process(String line)
      {
        if (hasEnded) {
          return null;
        } else if (!hasStarted) {
          int beginPre = line.indexOf("<pre>");
          if (beginPre != -1) {
            line = line.substring(beginPre + 5);
            hasStarted = true;
          }
        }
        int endPre = line.indexOf("</pre>");
        if (endPre != -1) {
          line = line.substring(0, endPre);
          hasEnded = true;
        }
        return StringEscapeUtils.unescapeHtml4(line);
      }
    };
  }

  @Override
  public void close() throws IOException
  {
  }
}
