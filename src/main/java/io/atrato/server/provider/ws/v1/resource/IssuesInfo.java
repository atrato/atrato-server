package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

import io.atrato.server.Issue;

/**
 * Created by david on 1/29/17.
 */
public class IssuesInfo
{
  Collection<Issue> issues;

  public IssuesInfo(Collection<Issue> issues)
  {
    this.issues = issues;
  }

  public Collection<Issue> getIssues()
  {
    return issues;
  }
}
