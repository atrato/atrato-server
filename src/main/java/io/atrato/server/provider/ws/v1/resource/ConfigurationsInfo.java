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
