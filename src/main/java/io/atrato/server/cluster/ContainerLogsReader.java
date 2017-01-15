package io.atrato.server.cluster;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import io.atrato.server.util.LineReader;

/**
 * Created by david on 1/12/17.
 */
public interface ContainerLogsReader extends Closeable
{
  Map<String, Long> getFileSizes();

  LineReader getLogFileReader(String name, long offset) throws IOException;
}
