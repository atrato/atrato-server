package io.atrato.server.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import com.datatorrent.stram.client.StramClientUtils;

/**
 * Created by david on 1/10/17.
 */
public class JDBCConfiguration extends AtratoConfigurationBase
{
  private Connection connection;
  private Set<String> changedKeys = new HashSet<>();

  public JDBCConfiguration(String url) throws SQLException
  {
    connection = DriverManager.getConnection(url);
  }

  public void createNew() throws SQLException, IOException
  {
    InputStream is = this.getClass().getResourceAsStream("/jdbc-config-schema.sql");
    StringWriter sw = new StringWriter();
    IOUtils.copy(is, sw);
    String sql = sw.toString();
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
      connection.commit();
    }
  }

  @Override
  public void set(String name, String value, String description)
  {
    super.set(name, value, description);
    changedKeys.add(name);
  }

  @Override
  public void delete(String name)
  {
    super.delete(name);
    changedKeys.add(name);
  }

  @Override
  public void load() throws IOException, ConfigurationException
  {
    try {
      realLoad();
    } catch (SQLException ex) {
      try {
        createNew();
        realLoad();
      } catch (Exception ex1) {
        throw new IOException(ex1);
      }
    }
  }

  private void realLoad() throws SQLException, ConfigurationException
  {
    String sql = "SELECT name, value, description FROM configuration";
    try (Statement stmt = connection.createStatement()) {
      ResultSet resultSet = stmt.executeQuery(sql);
      while (resultSet.next()) {
        String name = resultSet.getString("name");
        String value = resultSet.getString("value");
        String description = resultSet.getString("description");
        configEntries.put(name, new Entry(name, value, description));
      }
    }
  }

  @Override
  public void save(boolean force) throws IOException
  {
    Collection<Entry> entries = configEntries.values();
    try {
      connection.setAutoCommit(false);
      if (force) {
        try (Statement stmt = connection.createStatement()) {
          stmt.execute("DELETE FROM configuration");
        }
        try (PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO configuration VALUES (?, ?, ?)")) {
          for (Entry entry : entries) {
            insertStatement.setString(1, entry.getName());
            insertStatement.setString(2, entry.getValue());
            insertStatement.setString(3, entry.getDescription());
            insertStatement.executeUpdate();
          }
        }
      } else {
        try (PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM configuration WHERE name=?");
            PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO configuration VALUES (?, ?, ?)")) {
          for (Iterator<String> it = changedKeys.iterator(); it.hasNext(); ) {
            String name = it.next();
            Entry entry = configEntries.get(name);

            // note that there is no good portable way to do "UPSERT" so that's why we are doing a DELETE and then INSERT
            deleteStatement.setString(1, name);
            deleteStatement.executeUpdate();
            if (entry != null) {
              insertStatement.setString(1, entry.getName());
              insertStatement.setString(2, entry.getValue());
              insertStatement.setString(3, entry.getDescription());
              insertStatement.executeUpdate();
            }

            it.remove();
          }
        }

      }
      connection.commit();

    } catch (SQLException ex) {
      throw new IOException(ex);
    }

    XmlMapper mapper = new XmlMapper();
    mapper.writer().withDefaultPrettyPrinter()
        .writeValue(new File(StramClientUtils.getConfigDir(), StramClientUtils.DT_SITE_XML_FILE), new ConfigurationForJackson(entries));
  }

  @Override
  public void close() throws IOException
  {
    try {
      connection.close();
    } catch (SQLException ex) {
      throw new IOException(ex);
    }
  }
}
