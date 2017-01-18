package io.atrato.server.provider.ws.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import com.google.common.base.Throwables;
import com.google.common.collect.EvictingQueue;
import com.sun.jersey.api.client.WebResource;

import com.datatorrent.stram.client.StramAgent;
import com.datatorrent.stram.util.WebServicesClient;

import io.atrato.server.AtratoServer;
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
import io.atrato.server.provider.ws.v1.resource.NullInfo;
import io.atrato.server.util.LineReader;

/**
 * Created by david on 12/26/16.
 */
public class ApplicationsProvider
{
  public static final ApplicationsProvider INSTANCE = new ApplicationsProvider();

  private static final String PATH_CONTAINERS = "containers";
  private static final String PATH_ATTEMPTS = "attempts";
  private static final String PATH_KILL = "kill";

  @Inject //TODO: need to make this injection work in the future
  private Cluster cluster;

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationsProvider.class);

  private ThreadLocal<StramAgent> stramAgents = new ThreadLocal<StramAgent>()
  {
    @Override
    protected StramAgent initialValue()
    {
      try {
        Configuration conf = new Configuration();
        FileSystem fs = FileSystem.get(conf);
        return new StramAgent(fs, conf);
      } catch (Exception ex) {
        throw Throwables.propagate(ex);
      }
    }
  };

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ApplicationsInfo getApplications()
  {
    ApplicationsInfo applicationsInfo = AtratoServer.getCluster().getApplicationsInfo();
    LOG.info("Applications are {}", applicationsInfo.getApplications());
    return applicationsInfo;
  }

  @GET
  @Path("{appId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ApplicationInfo getApplication(@PathParam("appId") String appId)
  {
    return AtratoServer.getCluster().getApplicationInfo(appId);
  }

  @GET
  @Path("{appId}/" + PATH_CONTAINERS)
  @Produces(MediaType.APPLICATION_JSON)
  public ContainersInfo getContainers(@PathParam("appId") String appId)
  {
    return AtratoServer.getCluster().getContainersInfo(appId);
  }

  @GET
  @Path("{appId}/" + PATH_CONTAINERS + "/{containerId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ContainerInfo getContainer(@PathParam("appId") String appId, @PathParam("containerId") String containerId)
  {
    return AtratoServer.getCluster().getContainerInfo(appId, containerId);
  }

  @GET
  @Path("{appId}/" + PATH_CONTAINERS + "/{containerId}/logs")
  @Produces(MediaType.APPLICATION_JSON)
  public ContainerLogsInfo getContainerLogs(@PathParam("appId") String appId, @PathParam("containerId") String containerId)
  {
    return AtratoServer.getCluster().getContainerLogsInfo(appId, containerId);
  }

  @GET
  @Path("{appId}/" + PATH_CONTAINERS + "/{containerId}/logs/{name}")
  public Response getContainerLog(@PathParam("appId") String appId, @PathParam("containerId") String containerId,
      @PathParam("name") String logName, @QueryParam("start") Long start, @QueryParam("lines") Long lines,
      @QueryParam("searchTerm") String search, @QueryParam("regex") Boolean regex,
      @QueryParam("beforeContext") Integer beforeContext, @QueryParam("afterContext") Integer afterContext)
  {
    ContainerLogInfo containerLogInfo = AtratoServer.getCluster().getContainerLogInfo(appId, containerId, logName);
    return getContainerLogResponse(appId, containerId, logName, start == null ? 0 : start, lines == null ? Long.MAX_VALUE : lines,
        search, BooleanUtils.isTrue(regex),
        beforeContext == null ? 0 : beforeContext, afterContext == null ? 0 : afterContext);
  }

  private Response getContainerLogResponse(final String appId, final String containerId, final String logName,
      final long startOffset, final long lines,
      final String searchTerm, final boolean regex, final int beforeContext, final int afterContext)
  {
    StreamingOutput output = new StreamingOutput()
    {
      @Override
      public void write(OutputStream os) throws IOException, WebApplicationException
      {
        try (ContainerLogsReader containerLogsReader = AtratoServer.getCluster().getContainerLogsReader(appId, containerId);
            LineReader logFileReader = containerLogsReader.getLogFileReader(logName, startOffset)) {
          EvictingQueue<String> beforeLines = EvictingQueue.create(beforeContext + 1);
          String line;
          long linesOutput = 0;
          int afterLines = 0;
          Pattern pattern = null;
          if (searchTerm != null && regex) {
            pattern = Pattern.compile(searchTerm);
          }
          while ((line = logFileReader.readLine()) != null) {
            if (searchTerm == null) {
              linesOutput++;
              os.write(line.getBytes());
              os.write('\n');
            } else {
              boolean matched;
              beforeLines.add(line);
              if (regex) {
                Matcher matcher = pattern.matcher(line);
                matched = matcher.find();
              } else {
                matched = line.contains(searchTerm);
              }
              if (matched) {
                for (String beforeLine : beforeLines) {
                  os.write(beforeLine.getBytes());
                  os.write('\n');
                  linesOutput++;
                }
                beforeLines.clear();
                afterLines = afterContext;
              } else if (afterLines > 0) {
                os.write(line.getBytes());
                os.write('\n');
                linesOutput++;
                afterLines--;
              }
            }
            if (linesOutput >= lines) {
              break;
            }
          }
        }
      }
    };
    return Response.ok(output).build();
  }


  @GET
  @Path("{appId}/" + PATH_ATTEMPTS)
  @Produces(MediaType.APPLICATION_JSON)
  public ApplicationAttemptsInfo getAttempts(@PathParam("appId") String appId)
  {
    return AtratoServer.getCluster().getApplicationAttemptsInfo(appId);
  }

  @GET
  @Path("{appId}/" + PATH_ATTEMPTS + "/{attemptId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ApplicationAttemptInfo getAttempt(@PathParam("appId") String appId, @PathParam("attemptId") String attemptId)
  {
    return AtratoServer.getCluster().getApplicationAttemptInfo(appId, attemptId);
  }

  @GET
  @Path("{appId}/" + PATH_ATTEMPTS + "/{attemptId}/" + PATH_CONTAINERS)
  @Produces(MediaType.APPLICATION_JSON)
  public ContainersInfo getAttemptContainers(@PathParam("appId") String appId, @PathParam("attemptId") String attemptId)
  {
    return cluster.getContainersInfo(appId, attemptId);
  }

  @GET
  @Path("{appId}/" + PATH_ATTEMPTS + "/{attemptId}/" + PATH_CONTAINERS + "/{containerId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ContainerInfo getAttemptContainer(@PathParam("appId") String appId, @PathParam("attemptId") String attemptId,
      @PathParam("containerId") String containerId)
  {
    ContainersInfo containersInfo = cluster.getContainersInfo(appId, attemptId);
    if (containersInfo == null) {
      throw new NotFoundException();
    }
    return containersInfo.getContainer(containerId);
  }

  @GET
  @Path("{appId}/" + PATH_ATTEMPTS + "/{attemptId}/" + PATH_CONTAINERS + "/{containerId}/logs")
  @Produces(MediaType.APPLICATION_JSON)
  public ContainerLogsInfo getAttemptContainerLogs(@PathParam("appId") String appId, @PathParam("attemptId") String attemptId,
      @PathParam("containerId") String containerId)
  {
    getAttemptContainer(appId, attemptId, containerId); // throws NotFoundException if not found
    return cluster.getContainerLogsInfo(appId, containerId);
  }

  @GET
  @Path("{appId}/" + PATH_ATTEMPTS + "/{attemptId}/" + PATH_CONTAINERS + "/{containerId}/logs/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttemptContainerLog(@PathParam("appId") String appId, @PathParam("attemptId") String attemptId,
      @PathParam("containerId") String containerId,
      @PathParam("name") String logName, @QueryParam("start") Long start, @QueryParam("lines") Long lines,
      @QueryParam("searchTerm") String search, @QueryParam("regex") Boolean regex,
      @QueryParam("beforeContext") Integer beforeContext, @QueryParam("afterContext") Integer afterContext)
  {
    ContainerLogsInfo containerLogsInfo = getAttemptContainerLogs(appId, attemptId, containerId);
    return getContainerLogResponse(appId, containerId, logName, start == null ? 0 : start, lines == null ? Long.MAX_VALUE : lines,
        search, BooleanUtils.isTrue(regex),
        beforeContext == null ? 0 : beforeContext, afterContext == null ? 0 : afterContext);
  }

  @POST
  @Path("{appId}/" + PATH_KILL)
  public NullInfo killApplication(@PathParam("appId") String appId)
  {
    cluster.killApplication(appId);
    return NullInfo.INSTANCE;
  }

  private Response stramProxy(String appId, String cmd, UriInfo uriInfo, WebServicesClient.WebServicesHandler<InputStream> handler)
  {
    StramAgent stramAgent = stramAgents.get();
    try {
      WebServicesClient webServicesClient = new WebServicesClient();
      StramAgent.StramUriSpec uriSpec = new StramAgent.StramUriSpec().path(cmd);
      for (Map.Entry<String, List<String>> entry : uriInfo.getQueryParameters().entrySet()) {
        uriSpec.queryParam(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
      }

      final InputStream input = stramAgent.issueStramWebRequest(webServicesClient, appId, uriSpec, InputStream.class, handler);
      StreamingOutput output = new StreamingOutput()
      {
        @Override
        public void write(OutputStream os) throws IOException, WebApplicationException
        {
          IOUtils.copy(input, os);
          IOUtils.closeQuietly(input);
        }
      };
      return Response.ok(output).build();
    } catch (StramAgent.AppNotFoundException e) {
      throw new NotFoundException();
    } catch (Exception ex) {
      throw Throwables.propagate(ex);
    }
  }

  @GET
  @Path("{appId}/{cmd:.+}")
  public Response stramGetProxy(@PathParam("appId") String appId, @PathParam("cmd") String cmd, @Context UriInfo uriInfo) throws IOException
  {
    return stramProxy(appId, cmd, uriInfo, new WebServicesClient.GetWebServicesHandler<InputStream>());
  }

  @PUT
  @Path("{appId}/{cmd:.+}")
  public Response stramPutProxy(@PathParam("appId") String appId, @PathParam("cmd") String cmd, final InputStream payloadStream, @Context UriInfo uriInfo)
  {
    return stramProxy(appId, cmd, uriInfo, new WebServicesClient.WebServicesHandler<InputStream>()
    {
      @Override
      public InputStream process(WebResource.Builder webResource, Class<InputStream> clazz)
      {
        return webResource.put(InputStream.class, payloadStream);
      }
    });
  }

  @POST
  @Path("{appId}/{cmd:.+}")
  public Response stramPostProxy(@PathParam("appId") String appId, @PathParam("cmd") String cmd, final InputStream payloadStream, @Context UriInfo uriInfo)
  {
    return stramProxy(appId, cmd, uriInfo, new WebServicesClient.WebServicesHandler<InputStream>()
    {
      @Override
      public InputStream process(WebResource.Builder webResource, Class<InputStream> clazz)
      {
        return webResource.post(InputStream.class, payloadStream);
      }
    });
  }

  @DELETE
  @Path("{appId}/{cmd:.+}")
  public Response stramDeleteProxy(@PathParam("appId") String appId, @PathParam("cmd") String cmd, @Context UriInfo uriInfo)
  {
    return stramProxy(appId, cmd, uriInfo, new WebServicesClient.DeleteWebServicesHandler<InputStream>());
  }
}
