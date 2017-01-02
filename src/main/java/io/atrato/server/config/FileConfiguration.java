package io.atrato.server.config;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by david on 12/24/16.
 */
public class FileConfiguration extends AtratoConfigurationBase
{
  private final File file;
  private final ObjectMapper mapper = new ObjectMapper();
  private final Map<String, Entry> entries = new HashMap<>();

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
        entries.put(name, new Entry(name, value, description));
      } catch (ClassCastException ex) {
        throw new ConfigurationException("Configuration entry must be an object");
      }
    }
  }

  @Override
  public void set(String name, String value, String description)
  {
    entries.put(name, new Entry(name, value, description));
  }

  @Override
  public void save() throws IOException
  {
    Map<String, Collection<Entry>> m = new HashMap<>();
    m.put("configuration", entries.values());
    mapper.writeValue(this.file, m);
  }

  @Override
  public AtratoConfiguration.Entry get(String name)
  {
    return entries.get(name);
  }

}
