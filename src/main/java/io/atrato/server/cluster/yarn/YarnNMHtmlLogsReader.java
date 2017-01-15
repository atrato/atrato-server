package io.atrato.server.cluster.yarn;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.yarn.api.records.ContainerReport;

import io.atrato.server.util.LineReader;

/**
 * Created by david on 1/14/17.
 */
public class YarnNMHtmlLogsReader extends YarnContainerLogsReader
{
  private Map<String, Long> fileSizes = new HashMap<>();
  private Map<String, String> fileLocations = new HashMap<>();

  public YarnNMHtmlLogsReader(ContainerReport containerReport) throws IOException
  {
    String logUrl = containerReport.getLogUrl();
    URL url = new URL(logUrl);
    String content;
    try (InputStream is = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      IOUtils.copy(is, baos);
      content = baos.toString();
    }

    // parse content
    Pattern pattern = Pattern.compile("<p>\\s*<a href=\"([^\\s?]+)(?:\\?start=\\d+)?\">(.*) : Total file length is (\\d+) bytes.</a>");
    Matcher m = pattern.matcher(content);
    while (m.find()) {
      String location = m.group(1);
      String logName = m.group(2);
      long bytes = Long.valueOf(m.group(3));
      fileSizes.put(logName, bytes);
      fileLocations.put(logName, location);
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
    String location = fileLocations.get(name);
    if (location == null) {
      throw new FileNotFoundException();
    }
    URL url = new URL(location);
    InputStream is = url.openStream();
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
