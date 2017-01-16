package io.atrato.server.provider.ws.v1.resource;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by david on 1/6/17.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class NullInfo
{
  public static final NullInfo INSTANCE = new NullInfo();

  private NullInfo()
  {
  }
}
