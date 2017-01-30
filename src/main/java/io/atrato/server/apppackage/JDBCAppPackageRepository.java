package io.atrato.server.apppackage;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import io.atrato.server.provider.ws.v1.resource.AppPackageInfo;

/**
 * Created by david on 1/10/17.
 */
public class JDBCAppPackageRepository implements AppPackageRepository
{
  private Connection connection;

  public JDBCAppPackageRepository(String url) throws SQLException
  {
    connection = DriverManager.getConnection(url);
  }

  public void createNew() throws SQLException, IOException
  {
    InputStream is = this.getClass().getResourceAsStream("/jdbc-app-package-schema.sql");
    StringWriter sw = new StringWriter();
    IOUtils.copy(is, sw);
    String sql = sw.toString();
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate(sql);
      connection.commit();
    }
  }

  @Override
  public AppPackageInfo getAppPackageInfo(String owner, String name, String version)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackages(String owner, String name)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackages(String owner)
  {
    return null;
  }

  @Override
  public Collection<AppPackageInfo> getAppPackagesAccessibleBy(String user)
  {
    return null;
  }

  @Override
  public void saveAppPackage(AppPackageInfo appPackageInfo, InputStream fileStream)
  {

  }
}
