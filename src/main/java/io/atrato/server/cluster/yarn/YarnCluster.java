package io.atrato.server.cluster.yarn;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptId;
import org.apache.hadoop.yarn.api.records.ApplicationAttemptReport;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ContainerReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.ConverterUtils;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import io.atrato.server.cluster.Cluster;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnContainerInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnContainerLogsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnContainersInfo;

import com.datatorrent.stram.StramClient;

/**
 * Created by david on 12/30/16.
 */
public class YarnCluster implements Cluster
{
  private static final Set<String> APPLICATION_TYPES = Sets.newHashSet(StramClient.YARN_APPLICATION_TYPE, StramClient.YARN_APPLICATION_TYPE_DEPRECATED);
  private static final int REFRESH_INTERVAL_SECONDS = 1;
  private ObjectPool<YarnClient> yarnClientPool = new GenericObjectPool<>(new YarnClientFactory());
  private static final Object DUMMY_OBJECT = new Object();

  private final LoadingCache<Object, List<ApplicationReport>> appListCache = CacheBuilder.newBuilder()
      .concurrencyLevel(2)
      .expireAfterWrite(REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
      .build(new CacheLoader<Object, List<ApplicationReport>>()
      {
        public List<ApplicationReport> load(Object key) throws Exception
        {
          YarnClient yarnClient = borrowYarnClient();
          try {
            return yarnClient.getApplications(APPLICATION_TYPES);
          } finally {
            returnYarnClient(yarnClient);
          }
        }
      });

  private static final Logger LOG = LoggerFactory.getLogger(YarnCluster.class);

  private static class YarnClientFactory extends BasePooledObjectFactory<YarnClient>
  {
    @Override
    public YarnClient create() throws Exception
    {
      YarnClient yarnClient = YarnClient.createYarnClient();
      yarnClient.init(new YarnConfiguration());
      yarnClient.start();
      return yarnClient;
    }

    @Override
    public PooledObject<YarnClient> wrap(YarnClient yarnClient)
    {
      return new DefaultPooledObject<>(yarnClient);
    }

    @Override
    public void destroyObject(PooledObject<YarnClient> p) throws Exception
    {
      p.getObject().stop();
    }
  }

  public YarnClient borrowYarnClient()
  {
    try {
      return yarnClientPool.borrowObject();
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  public void returnYarnClient(YarnClient yarnClient)
  {
    try {
      yarnClientPool.returnObject(yarnClient);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  public List<ApplicationReport> getApplications()
  {
    try {
      return appListCache.get(DUMMY_OBJECT);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  @Override
  public ApplicationsInfo getApplicationsInfo()
  {
    return new YarnApplicationsInfo(getApplications());
  }

  @Override
  public ApplicationInfo getApplicationInfo(String appId)
  {
    return new YarnApplicationsInfo(getApplications()).getApplication(appId);
  }

  @Override
  public ContainersInfo getContainersInfo(String appId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      ApplicationReport applicationReport = yarnClient.getApplicationReport(ConverterUtils.toApplicationId(appId));
      List<ContainerReport> containers = yarnClient.getContainers(applicationReport.getCurrentApplicationAttemptId());
      return new YarnContainersInfo(containers);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public ContainerInfo getContainerInfo(String appId, String containerId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      ContainerReport containerReport = yarnClient.getContainerReport(ConverterUtils.toContainerId(containerId));
      return new YarnContainerInfo(containerReport);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public ApplicationAttemptsInfo getApplicationAttemptsInfo(String appId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      List<ApplicationAttemptReport> attemptReports = yarnClient.getApplicationAttempts(ConverterUtils.toApplicationId(appId));
      return new YarnApplicationAttemptsInfo(appId, attemptReports);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public ApplicationAttemptInfo getApplicationAttemptInfo(String appId, String attemptId)
  {
    return getApplicationAttemptsInfo(appId).getAttempt(attemptId);
  }

  @Override
  public ContainersInfo getContainersInfo(String appId, String attemptId)
  {
    ApplicationAttemptId id;
    if (StringUtils.isNumeric(attemptId)) {
      id = ApplicationAttemptId.newInstance(ConverterUtils.toApplicationId(appId), Integer.valueOf(attemptId));
    } else {
      id = ConverterUtils.toApplicationAttemptId(attemptId);
    }
    YarnClient yarnClient = borrowYarnClient();
    try {
      List<ContainerReport> containers = yarnClient.getContainers(id);
      return new YarnContainersInfo(containers);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public ContainerLogsInfo getContainerLogsInfo(String appId, String containerId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      ContainerReport containerReport = yarnClient.getContainerReport(ConverterUtils.toContainerId(containerId));
      String logUrl = containerReport.getLogUrl();
      URL url = new URL(logUrl);
      String content;
      try (InputStream is = url.openStream();
          ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        IOUtils.copy(is, baos);
        content = baos.toString();
      }

      YarnContainerLogsInfo logs = new YarnContainerLogsInfo();
      // parse content

      Pattern pattern = Pattern.compile("<p>\\s*<a href=\"(\\S+)\">(.*) : Total file length is (\\d+) bytes.</a>");
      Matcher m = pattern.matcher(content);
      while (m.find()) {
        String logName = m.group(2);
        long bytes = Long.valueOf(m.group(3));
        logs.addLog(logName, bytes);
      }

      return logs;
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public void killApplication(String appId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      yarnClient.killApplication(ConverterUtils.toApplicationId(appId));
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

}
