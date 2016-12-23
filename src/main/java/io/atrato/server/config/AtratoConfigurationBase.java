package io.atrato.server.config;

/**
 * Created by david on 12/26/16.
 */
public abstract class AtratoConfigurationBase implements AtratoConfiguration
{
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
