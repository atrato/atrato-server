package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

import com.google.common.base.Preconditions;

import io.atrato.server.config.AtratoConfiguration;
import io.atrato.server.config.TransientConfiguration;

/**
 * Created by david on 1/15/17.
 */
public class ConfigurationsInfo
{
  private AtratoConfiguration conf;

  public ConfigurationsInfo()
  {
  }

  public ConfigurationsInfo(AtratoConfiguration conf)
  {
    this.conf = conf;
  }

  public Collection<AtratoConfiguration.Entry> getConfiguration()
  {
    return conf.getEntries();
  }

  // for temporary
  public void setConfiguration(Collection<AtratoConfiguration.Entry> entries)
  {
    Preconditions.checkArgument(conf == null);
    conf = new TransientConfiguration();
    for (AtratoConfiguration.Entry entry : entries) {
      conf.set(entry.getName(), entry.getValue(), entry.getDescription());
    }
  }
}
