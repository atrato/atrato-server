package io.atrato.server.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Created by david on 12/26/16.
 */
public abstract class AtratoConfigurationBase implements AtratoConfiguration
{
  protected Map<String, Entry> configEntries = new HashMap<>();

  // for jackson serializing to hadoop configuration format
  @JacksonXmlRootElement(localName = "configuration")
  public static class ConfigurationForJackson
  {
    private Collection<Entry> entries;

    public ConfigurationForJackson(Collection<Entry> entries)
    {
      this.entries = entries;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "property")
    public Collection<Entry> getEntries()
    {
      return entries;
    }
  }

  @Override
  public Entry get(String name)
  {
    return configEntries.get(name);
  }

  @Override
  public void set(String name, String value, String description)
  {
    configEntries.put(name, new Entry(name, value, description));
  }

  @Override
  public void delete(String name)
  {
    configEntries.remove(name);
  }

  @Override
  public Collection<Entry> getEntries()
  {
    return Collections.unmodifiableCollection(configEntries.values());
  }

  @Override
  public String getValue(String name)
  {
    return getValue(name, null);
  }

  @Override
  public String getValue(String name, String defaultValue)
  {
    Entry entry = get(name);
    return entry == null ? defaultValue : entry.getValue();
  }

  @Override
  public int getIntValue(String name)
  {
    return getIntValue(name, 0);
  }

  @Override
  public int getIntValue(String name, int defaultValue)
  {
    String value = getValue(name);
    return value == null ? defaultValue : Integer.valueOf(value);
  }

  @Override
  public long getLongValue(String name)
  {
    return getLongValue(name, 0);
  }

  @Override
  public long getLongValue(String name, long defaultValue)
  {
    String value = getValue(name);
    return value == null ? defaultValue : Long.valueOf(value);
  }

  @Override
  public float getFloatValue(String name)
  {
    return getFloatValue(name, 0);
  }

  @Override
  public float getFloatValue(String name, float defaultValue)
  {
    String value = getValue(name);
    return value == null ? defaultValue : Float.valueOf(value);
  }

  @Override
  public double getDoubleValue(String name)
  {
    return getDoubleValue(name, 0);
  }

  @Override
  public double getDoubleValue(String name, double defaultValue)
  {
    String value = getValue(name);
    return value == null ? defaultValue : Double.valueOf(value);
  }

  @Override
  public boolean getBooleanValue(String name)
  {
    return getBooleanValue(name, false);
  }

  @Override
  public boolean getBooleanValue(String name, boolean defaultValue)
  {
    String value = getValue(name);
    return value == null ? defaultValue : Boolean.valueOf(value);
  }
}
