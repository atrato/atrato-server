/**
 * Copyright (c) 2017 Atrato, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
