package io.jenkins.plugins.postgrespipelinereporter;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;
import org.jdbi.v3.core.Jdbi;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Extension
public class PostgresReporterGlobalConfiguration extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(PostgresReporterGlobalConfiguration.class.getName());
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]+$");

    private String dbUrl;
    private String dbTable;
    private String dbUser;
    private String dbPassword;

    public PostgresReporterGlobalConfiguration() {
        load(); // load saved configuration
    }

    public static PostgresReporterGlobalConfiguration get() {
        return GlobalConfiguration.all().get(PostgresReporterGlobalConfiguration.class);
    }

    public String getDbUrl() { return dbUrl; }
    @DataBoundSetter public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; save(); createTableIfNeeded(); }

    public String getDbTable() { return dbTable; }
    @DataBoundSetter public void setDbTable(String dbTable) { this.dbTable = dbTable; save(); createTableIfNeeded(); }

    public String getDbUser() { return dbUser; }
    @DataBoundSetter public void setDbUser(String dbUser) { this.dbUser = dbUser; save(); createTableIfNeeded(); }

    public String getDbPassword() { return dbPassword; }
    @DataBoundSetter public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; save(); createTableIfNeeded(); }

    /**
     * Create the configured table if it does not already exist. This runs when configuration is applied.
     */
    private void createTableIfNeeded() {
        if (dbUrl == null || dbUrl.isEmpty() || dbUser == null || dbUser.isEmpty()) {
            LOGGER.fine("DB configuration incomplete, skipping table creation");
            return;
        }

        String table = dbTable;
        if (table == null || !TABLE_NAME_PATTERN.matcher(table).matches()) {
            table = "jenkins_pipeline_runs";
        }
        this.dbTable = table; // ensure valid table name is stored

        try {
            Jdbi jdbi = Jdbi.create(dbUrl, dbUser, dbPassword);
            final String finalTable = table;
            jdbi.useHandle(handle -> {
                String createSql = "CREATE TABLE IF NOT EXISTS " + finalTable + " (" +
                        "id serial PRIMARY KEY, " +
                        "folder text, " +
                        "job_name text, " +
                        "build_number integer, " +
                        "causer text, " +
                        "jenkins_url text, " +
                        "status text, " +
                        "duration_ms bigint, " +
                        "start_time timestamp" +
                        ")";
                handle.execute(createSql);
            });
            LOGGER.info("PostgresReporter: ensured table exists: " + table);
        } catch (Exception e) {
            LOGGER.warning("PostgresReporter: failed to create or verify table '" + table + "': " + e.getMessage());
        }
    }
}