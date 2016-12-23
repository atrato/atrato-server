package io.atrato.server.provider.ws.v1.resource;

import java.util.Collection;

/**
 * Created by david on 12/30/16.
 */
public interface ApplicationAttemptsInfo
{
  Collection<ApplicationAttemptInfo> getAttempts();

  ApplicationAttemptInfo getAttempt(String attemptId);

}
