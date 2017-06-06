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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.util.ConverterUtils;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import io.atrato.server.cluster.Cluster;
import io.atrato.server.cluster.ContainerLogsReader;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationInfo;
import io.atrato.server.provider.ws.v1.resource.ApplicationsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogInfo;
import io.atrato.server.provider.ws.v1.resource.ContainerLogsInfo;
import io.atrato.server.provider.ws.v1.resource.ContainersInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationAttemptsInfo;
import io.atrato.server.provider.ws.v1.resource.yarn.YarnApplicationsInfo;
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

  private final LoadingCache<Object, List<ApplicationReport>> appListCache = buildCache(new CacheLoader<Object, List<ApplicationReport>>()
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

  private final LoadingCache<String, List<ContainerReport>> appIdToContainerListCache = buildCache(new CacheLoader<String, List<ContainerReport>>()
  {
    public List<ContainerReport> load(String appId) throws Exception
    {
      YarnClient yarnClient = borrowYarnClient();
      try {
        ApplicationReport applicationReport = yarnClient.getApplicationReport(ConverterUtils.toApplicationId(appId));
        return yarnClient.getContainers(applicationReport.getCurrentApplicationAttemptId());
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      } finally {
        returnYarnClient(yarnClient);
      }
    }
  });

  private final LoadingCache<String, List<ApplicationAttemptReport>> appIdAttemptsCache = buildCache(new CacheLoader<String, List<ApplicationAttemptReport>>()
  {
    @Override
    public List<ApplicationAttemptReport> load(String appId) throws Exception
    {
      YarnClient yarnClient = borrowYarnClient();
      try {
        return yarnClient.getApplicationAttempts(ConverterUtils.toApplicationId(appId));
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      } finally {
        returnYarnClient(yarnClient);
      }
    }
  });

  private final LoadingCache<ApplicationAttemptId, List<ContainerReport>> attemptIdToContainersCache = buildCache(new CacheLoader<ApplicationAttemptId, List<ContainerReport>>()
  {
    @Override
    public List<ContainerReport> load(ApplicationAttemptId attemptId) throws Exception
    {
      YarnClient yarnClient = borrowYarnClient();
      try {
        return yarnClient.getContainers(attemptId);
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      } finally {
        returnYarnClient(yarnClient);
      }
    }
  });

  private final LoadingCache<String, ApplicationReport> applicationReportCache = buildCache(new CacheLoader<String, ApplicationReport>()
  {
    @Override
    public ApplicationReport load(String appId) throws Exception
    {
      YarnClient yarnClient = borrowYarnClient();
      try {
        return yarnClient.getApplicationReport(ConverterUtils.toApplicationId(appId));
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      } finally {
        returnYarnClient(yarnClient);
      }
    }
  });

  private final LoadingCache<String, ContainerReport> containerReportCache = buildCache(new CacheLoader<String, ContainerReport>()
  {
    @Override
    public ContainerReport load(String containerId) throws Exception
    {
      YarnClient yarnClient = borrowYarnClient();
      try {
        return yarnClient.getContainerReport(ConverterUtils.toContainerId(containerId));
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      } finally {
        returnYarnClient(yarnClient);
      }
    }
  });

  private static final Logger LOG = LoggerFactory.getLogger(YarnCluster.class);

  private static <K, V> LoadingCache<K, V> buildCache(CacheLoader<K, V> cacheLoader)
  {
    return CacheBuilder.newBuilder()
        .concurrencyLevel(2)
        .expireAfterWrite(REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS)
        .build(cacheLoader);
  }

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

  public List<ContainerReport> getContainers(String appId)
  {
    try {
      return appIdToContainerListCache.get(appId);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  public ApplicationReport getApplicationReport(String appId)
  {
    try {
      return applicationReportCache.get(appId);
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  public ContainerReport getContainerReport(String containerId)
  {
    try {
      return containerReportCache.get(containerId);
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
    return new YarnContainersInfo(getContainers(appId));
  }

  @Override
  public ContainerInfo getContainerInfo(String appId, String containerId)
  {
    return new YarnContainersInfo(getContainers(appId)).getContainer(containerId);
  }

  @Override
  public ApplicationAttemptsInfo getApplicationAttemptsInfo(String appId)
  {
    try {
      return new YarnApplicationAttemptsInfo(appId, appIdAttemptsCache.get(appId));
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
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
    try {
      return new YarnContainersInfo(attemptIdToContainersCache.get(id));
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  @Override
  public ContainerLogsInfo getContainerLogsInfo(String appId, String containerId)
  {
    YarnContainerLogsInfo logsInfo = new YarnContainerLogsInfo();
    try {
      YarnContainerLogsReader reader = YarnContainerLogsReader.create(this, appId, containerId);
      Map<String, Long> fileSizes = reader.getFileSizes();
      for (Map.Entry<String, Long> entry : fileSizes.entrySet()) {
        logsInfo.addLog(entry.getKey(), entry.getValue());
      }
    } catch (FileNotFoundException ex) {
      // fall through
    }
    return logsInfo;
  }

  @Override
  public ContainerLogInfo getContainerLogInfo(String appId, String containerId, String name)
  {
    return getContainerLogsInfo(appId, containerId).getLog(name);
  }

  @Override
  public ContainerLogsReader getContainerLogsReader(String appId, String containerId) throws IOException
  {
    return YarnContainerLogsReader.create(this, appId, containerId);
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

  public static boolean isApplicationTerminatedState(YarnApplicationState state)
  {
    return state == YarnApplicationState.FAILED ||
        state == YarnApplicationState.FINISHED ||
        state == YarnApplicationState.KILLED;
  }

}
