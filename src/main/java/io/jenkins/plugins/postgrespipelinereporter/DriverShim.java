package io.jenkins.plugins.postgrespipelinereporter;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * A thin wrapper around a JDBC Driver to avoid classloader issues when registering
 * drivers coming from a plugin classloader. The shim delegates all calls to the
 * wrapped driver instance but uses the shim class (which is in the plugin classloader)
 * as the registered driver so it can be unregistered cleanly.
 */
public class DriverShim implements Driver {
    private final Driver wrapped;

    public DriverShim(Driver driver) {
        this.wrapped = driver;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return wrapped.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return wrapped.acceptsURL(url);
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return wrapped.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return wrapped.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return wrapped.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return wrapped.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return wrapped.getParentLogger();
    }
}
