package io.atrato.server.config;

import java.io.IOException;

/**
 * Created by david on 12/22/16.
 */
public interface AtratoConfiguration
{
  class Entry
  {
    private final String name;
    private final String value;
    private final String description;

    Entry(String name, String value, String description)
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
  }

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
   */
  void save() throws IOException;

}
