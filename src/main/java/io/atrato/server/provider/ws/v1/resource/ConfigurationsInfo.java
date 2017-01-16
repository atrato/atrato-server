package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

import io.atrato.server.config.AtratoConfiguration;

/**
 * Created by david on 1/15/17.
 */
public class ConfigurationsInfo
{
  private AtratoConfiguration conf;

  public ConfigurationsInfo(AtratoConfiguration conf)
  {
    this.conf = conf;
  }

  public Collection<AtratoConfiguration.Entry> getConfiguration()
  {
    return conf.getEntries();
  }
}
