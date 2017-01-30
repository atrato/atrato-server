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
