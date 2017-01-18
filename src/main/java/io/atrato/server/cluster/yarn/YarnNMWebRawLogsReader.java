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

import org.apache.hadoop.conf.Configuration;
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
  private String logUrl;
  private Map<String, Long> fileSizes = new HashMap<>();

  public YarnNMWebRawLogsReader(Configuration conf, ApplicationReport applicationReport, ContainerReport containerReport) throws IOException
  {
    String logDirs = conf.get(YarnConfiguration.NM_LOG_DIRS);
    String containerLogUrl = containerReport.getLogUrl();
    if (logDirs != null && logDirs.startsWith("${yarn.log.dir}/")) {
      logDirs = logDirs.substring(16);
      Pattern pattern = Pattern.compile("(https?://[^/]+)/");
      Matcher matcher = pattern.matcher(containerLogUrl);
      if (!matcher.find()) {
        throw new IOException("Cannot get nm web url");
      }
      String nmUrlPrefix = matcher.group(1);
      String applicationId = applicationReport.getApplicationId().toString();
      String containerId = containerReport.getContainerId().toString();
      logUrl = nmUrlPrefix + "/logs/" + logDirs + "/" + applicationId + "/" + containerId;

      WebServicesClient webServicesClient = new WebServicesClient();
      WebResource wr = webServicesClient.getClient().resource(logUrl);
      WebResource.Builder builder = wr.getRequestBuilder();

      try (InputStream is = webServicesClient.process(builder, InputStream.class, new WebServicesClient.GetWebServicesHandler<InputStream>());
          BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
        Pattern htmlPattern = Pattern.compile("<TR><TD><A HREF=\".+\">(.+)&nbsp;</TD><TD ALIGN=right>(\\d+) bytes&nbsp;</TD><TD>.*</TD></TR>");
        String line;
        while ((line = br.readLine()) != null) {
          Matcher m = htmlPattern.matcher(line);
          if (m.find()) {
            String name = m.group(1);
            long size = Long.parseLong(m.group(2));
            fileSizes.put(name, size);
          }
        }
      }
    } else {
      throw new IOException("Cannot get location of web raw log");
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
