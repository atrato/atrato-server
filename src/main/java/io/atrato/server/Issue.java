package io.atrato.server;

/**
 * Created by david on 1/29/17.
 */
public class Issue
{
  public enum IssueKey
  {
    RESTART_NEEDED
  }

  private IssueKey key;
  private String message;

  public Issue(IssueKey key, String message)
  {
    this.key = key;
    this.message = message;
  }

  public IssueKey getKey()
  {
    return key;
  }

  public String getMessage()
  {
    return message;
  }
}
