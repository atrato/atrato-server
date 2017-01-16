package io.atrato.server.config;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Created by david on 12/22/16.
 */
public interface AtratoConfiguration extends Closeable
{
  class Entry
  {
    private String name;
    private String value;
    private String description;

    public Entry()
    {
    }

    public Entry(String name, String value, String description)
    {
      this.name = name;
      this.value = value;
      this.description = description;
    }

    public String getName()
    {
      return name;
    }

    public String getValue()
    {
      return value;
    }

    public String getDescription()
    {
      return description;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public void setDescription(String description)
    {
      this.description = description;
    }
  }

  Collection<Entry> getEntries();

  /**
   *
   */
  void load() throws IOException, ConfigurationException;

  /**
   *
   * @param name
   * @return
   */
  Entry get(String name);

  /**
   *
   * @param name
   * @return
   */
  String getValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  String getValue(String name, String defaultValue);

  /**
   *
   * @param name
   * @return
   */
  int getIntValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  int getIntValue(String name, int defaultValue);

  /**
   *
   * @param name
   * @return
   */
  long getLongValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  long getLongValue(String name, long defaultValue);

  /**
   *
   * @param name
   * @return
   */
  float getFloatValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  float getFloatValue(String name, float defaultValue);

  /**
   *
   * @param name
   * @return
   */
  double getDoubleValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  double getDoubleValue(String name, double defaultValue);

  /**
   *
   * @param name
   * @return
   */
  boolean getBooleanValue(String name);

  /**
   *
   * @param name
   * @param defaultValue
   * @return
   */
  boolean getBooleanValue(String name, boolean defaultValue);

  /**
   *
   * @param name
   * @param value
   * @param description
   */
  void set(String name, String value, String description);

  /**
   *
   * @param name
   */
  void delete(String name);

  /**
   * @param force
   */
  void save(boolean force) throws IOException;

}
