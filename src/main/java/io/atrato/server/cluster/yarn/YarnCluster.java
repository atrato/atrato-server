package io.atrato.server.cluster.yarn;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
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

import io.atrato.server.cluster.Cluster;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnContainersInfo;

/**
 * Created by david on 12/30/16.
 */
public class YarnCluster implements Cluster
{
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
            return yarnClient.getApplications();
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
  public ApplicationsInfo getApplicationsResource()
  {
    return new YarnApplicationsInfo(getApplications());
  }

  @Override
  public ApplicationInfo getApplicationResource(String appId)
  {
    return new YarnApplicationsInfo(getApplications()).getApplication(appId);
  }

  @Override
  public ContainersInfo getContainersResource(String appId)
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
  public ContainerInfo getContainerResource(String containerId)
  {
    return null;
  }

  @Override
  public ApplicationAttemptsInfo getApplicationAttemptsResource(String appId)
  {
    YarnClient yarnClient = borrowYarnClient();
    try {
      List<ApplicationAttemptReport> attemptReports = yarnClient.getApplicationAttempts(ConverterUtils.toApplicationId(appId));
      return new YarnApplicationAttemptsInfo(attemptReports);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    } finally {
      returnYarnClient(yarnClient);
    }
  }

  @Override
  public ApplicationAttemptInfo getApplicationAttemptResource(String attemptId)
  {
    return null;
  }
}
