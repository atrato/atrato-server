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
package io.atrato.server.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;

/**
 * Created by david on 1/14/17.
 */
public class LineReader implements Closeable
{
  private BufferedReader reader;

  public LineReader(BufferedReader reader)
  {
    this.reader = reader;
  }

  public String readLine() throws IOException
  {
    if (hasEnded()) {
      return null;
    }
    while (true) {
      String line = reader.readLine();
      line = process(line);
      if (line == null || ("".equals(line) && hasEnded())) {
        return null;
      }

      if (hasStarted()) {
        return line;
      }
    }
  }

  protected boolean hasStarted()
  {
    return true;
  }

  protected boolean hasEnded()
  {
    return false;
  }

  public String process(String line)
  {
    return line;
  }

  @Override
  public void close() throws IOException
  {
    this.reader.close();
  }
}
