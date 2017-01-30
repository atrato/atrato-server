package io.atrato.server.provider.ws.v1.resource;

/**
 * Created by david on 1/29/17.
 */
public class StringValueInfo
{
  private String value;

  public StringValueInfo()
  {
  }

  public StringValueInfo(String value)
  {
    this.value = value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
