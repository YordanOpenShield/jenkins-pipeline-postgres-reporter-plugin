package io.jenkins.plugins.postgrespipelinereporter;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import hudson.model.listeners.RunListener;
import hudson.model.CauseAction;
import hudson.model.Cause;
import jenkins.model.JenkinsLocationConfiguration;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.regex.Pattern;

import org.jdbi.v3.core.Jdbi;

@Extension
public class PipelineRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onCompleted(Run<?, ?> run, TaskListener listener) {
        try {
            PostgresReporterGlobalConfiguration cfg = PostgresReporterGlobalConfiguration.get();
            if (cfg == null || cfg.getDbUrl() == null || cfg.getDbUser() == null || cfg.getDbPassword() == null) {
                listener.getLogger().println("[PostgresReporter] Missing DB configuration");
                return;
            }

            String url = cfg.getDbUrl();
            String dbTable = cfg.getDbTable();
            String user = cfg.getDbUser();
            String password = cfg.getDbPassword();

            boolean registeredShim = false;
            Driver shim = null;
            try {
                // Check whether any registered driver accepts this URL
                boolean hasDriver = false;
                for (java.util.Enumeration<Driver> e = DriverManager.getDrivers(); e.hasMoreElements(); ) {
                    Driver d = e.nextElement();
                    try {
                        if (d.acceptsURL(url)) {
                            hasDriver = true;
                            break;
                        }
                    } catch (Exception ignored) {
                        // ignore drivers that can't accept the URL
                    }
                }

                if (!hasDriver) {
                    // Try to load the Postgres driver from this plugin's classloader and register a shim
                    try {
                        Class<?> drvClass = Class.forName("org.postgresql.Driver", true, this.getClass().getClassLoader());
                        Object drvObj = drvClass.getDeclaredConstructor().newInstance();
                        if (drvObj instanceof Driver) {
                            shim = new DriverShim((Driver) drvObj);
                            DriverManager.registerDriver(shim);
                            registeredShim = true;
                            listener.getLogger().println("[PostgresReporter] Registered JDBC driver shim for org.postgresql.Driver");
                        } else {
                            listener.getLogger().println("[PostgresReporter] Loaded org.postgresql.Driver but it does not implement java.sql.Driver");
                        }
                    } catch (ClassNotFoundException cnfe) {
                        listener.getLogger().println("[PostgresReporter] PostgreSQL driver class not found: " + cnfe.getMessage());
                        throw cnfe;
                    }
                }

                // Use JDBI for simple inserts and create table if not exists
                Jdbi jdbi = Jdbi.create(url, user, password);
                String table = cfg.getDbTable();

                try {
                    jdbi.useHandle(handle -> {
                        // gather data to insert
                        String folder = "(root)";
                        String jobName = "(unknown)";
                        if (run.getParent() != null) {
                            String full = run.getParent().getFullName();
                            jobName = run.getParent().getName();
                            if (full != null && full.contains("/")) {
                                folder = full.substring(0, full.lastIndexOf('/'));
                            }
                        }

                        int buildNumber = run.getNumber(); // primitive int

                        // determine causer (who triggered the build)
                        String causer = "(unknown)";
                        CauseAction causeAction = run.getAction(CauseAction.class);
                        if (causeAction != null && causeAction.getCauses() != null && !causeAction.getCauses().isEmpty()) {
                            Cause c = causeAction.getCauses().get(0);
                            try {
                                if (c instanceof Cause.UserIdCause) {
                                    causer = ((Cause.UserIdCause) c).getUserName();
                                } else {
                                    causer = c.getShortDescription();
                                }
                            } catch (Exception ignored) {
                                causer = c.toString();
                            }
                        }

                        // Jenkins root URL
                        String jenkinsUrl = Jenkins.get().getRootUrl();
                        if (jenkinsUrl == null) {
                            jenkinsUrl = JenkinsLocationConfiguration.get().getUrl();
                            if (jenkinsUrl == null) {
                                jenkinsUrl = "";
                            }
                        }

                        String status = "UNKNOWN";
                        if (run.getResult() != null && run.getResult().toString() != null) {
                            status = run.getResult().toString();
                        }
                        long duration = run.getDuration(); // primitive long, never null
                        long startTime = run.getStartTimeInMillis(); // primitive long

                        handle.createUpdate("INSERT INTO " + table + " (folder, job_name, build_number, causer, jenkins_url, status, duration_ms, start_time) VALUES (:folder, :job_name, :build_number, :causer, :jenkins_url, :status, :duration_ms, :start_time)")
                                .bind("folder", folder)
                                .bind("job_name", jobName)
                                .bind("build_number", buildNumber)
                                .bind("causer", causer)
                                .bind("jenkins_url", jenkinsUrl)
                                .bind("status", status)
                                .bind("duration_ms", duration)
                                .bind("start_time", new Timestamp(startTime))
                                .execute();
                    });
                } catch (Exception e) {
                    listener.getLogger().println("[PostgresReporter] Failed to execute SQL statement: " + e.getMessage());
                    e.printStackTrace(listener.getLogger());
                    return;
                }
            } finally {
                // Unregister shim driver if we registered one to avoid classloader leaks
                if (registeredShim && shim != null) {
                    try {
                        DriverManager.deregisterDriver(shim);
                        listener.getLogger().println("[PostgresReporter] Deregistered JDBC driver shim");
                    } catch (Exception e) {
                        listener.getLogger().println("[PostgresReporter] Failed to deregister JDBC driver shim: " + e.getMessage());
                    }
                }
            }

            listener.getLogger().println("[PostgresReporter] Build data saved to Postgres.");
        } catch (Exception e) {
            listener.getLogger().println("[PostgresReporter] Failed to push data: " + e.getMessage());
            e.printStackTrace(listener.getLogger());
        }
    }
}
