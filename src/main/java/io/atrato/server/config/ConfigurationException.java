package io.atrato.server.config;

/**
 * Created by david on 12/26/16.
 */
public class ConfigurationException extends Exception
{
  private Throwable cause;

  public ConfigurationException(String message)
  {
    super(message);
  }

  public ConfigurationException(Throwable cause)
  {
    this.cause = cause;
  }
}
