package io.atrato.server.provider.ws.v1;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atrato.server.AtratoServer;
import io.atrato.server.provider.ws.v1.resource.IssuesInfo;

/**
 * Created by david on 1/29/17.
 */
public class IssuesProvider
{
  private static final Logger LOG = LoggerFactory.getLogger(ConfigProvider.class);
  public static final IssuesProvider INSTANCE = new IssuesProvider();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public IssuesInfo getIssues()
  {
    return new IssuesInfo(AtratoServer.getIssues());
  }
}