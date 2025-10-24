package io.jenkins.plugins.postgrespipelinereporter;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
public class PostgresReporterGlobalConfiguration extends GlobalConfiguration {

    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public PostgresReporterGlobalConfiguration() {
        load(); // load saved configuration
    }

    public static PostgresReporterGlobalConfiguration get() {
        return GlobalConfiguration.all().get(PostgresReporterGlobalConfiguration.class);
    }

    public String getDbUrl() { return dbUrl; }
    @DataBoundSetter public void setDbUrl(String dbUrl) { this.dbUrl = dbUrl; save(); }

    public String getDbUser() { return dbUser; }
    @DataBoundSetter public void setDbUser(String dbUser) { this.dbUser = dbUser; save(); }

    public String getDbPassword() { return dbPassword; }
    @DataBoundSetter public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; save(); }
}