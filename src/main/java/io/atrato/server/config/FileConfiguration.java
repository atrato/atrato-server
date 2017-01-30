package io.atrato.server.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * Created by david on 12/24/16.
 */
public class FileConfiguration extends AtratoConfigurationBase
{
  private final File file;
  private final XmlMapper mapper = new XmlMapper();
  private boolean dirty = false;

  public FileConfiguration(String filename)
  {
    this.file = new File(filename);
  }

  public FileConfiguration(File file)
  {
    this.file = file;
  }

  @Override
  public void load() throws IOException, ConfigurationException
  {
    if (!this.file.exists()) {
      return;
    }
    HashMap<String, Object> map = mapper.readValue(this.file, HashMap.class);
    List<Object> configs;
    try {
      configs = (List<Object>)map.get("configuration");
    } catch (ClassCastException ex) {
      throw new ConfigurationException("Configuration must be a list");
    }
    if (configs == null) {
      throw new ConfigurationException("There is no configuration object");
    }
    for (Object obj : configs) {
      try {
        Map<String, String> entryMap = (Map<String, String>)obj;
        String name = entryMap.get("name");
        String value = entryMap.get("value");
        String description = entryMap.get("description");
        if (name == null) {
          throw new ConfigurationException("name is required");
        }
        configEntries.put(name, new Entry(name, value, description));
      } catch (ClassCastException ex) {
        throw new ConfigurationException("Configuration entry must be an object");
      }
    }
  }

  @Override
  public void set(String name, String value, String description)
  {
    super.set(name, value, description);
    dirty = true;
  }

  @Override
  public void delete(String name)
  {
    super.delete(name);
    dirty = true;
  }

  @Override
  public void save(boolean force) throws IOException
  {
    if (dirty || force) {
      mapper.writer().withDefaultPrettyPrinter().writeValue(this.file, new ConfigurationForJackson(configEntries.values()));
      dirty = false;
    }
  }

  @Override
  public void close() throws IOException
  {
  }

}
