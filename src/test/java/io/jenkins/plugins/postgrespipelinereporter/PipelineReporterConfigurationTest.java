package io.jenkins.plugins.postgrespipelinereporter;

import static org.junit.jupiter.api.Assertions.*;

import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PostgresReporterConfigurationTest {

    /**
     * Tries to exercise enough code paths to catch common mistakes:
     * <ul>
     * <li>missing {@code load}
     * <li>missing {@code save}
     * <li>misnamed or absent getter/setter
     * <li>misnamed {@code textbox}
     * </ul>
     */
    @Test
    void uiAndStorage(JenkinsRule jenkins) throws Throwable {
        assertNull(PostgresReporterGlobalConfiguration.get().getDbUrl(), "not set initially");
        try (JenkinsRule.WebClient client = jenkins.createWebClient()) {
            HtmlForm config = client.goTo("configure").getFormByName("config");
            HtmlTextInput textbox = config.getInputByName("_.dbUrl");
            textbox.setText("jdbc:postgresql://localhost:5432/jenkins");
            jenkins.submit(config);
            assertEquals("jdbc:postgresql://localhost:5432/jenkins", PostgresReporterGlobalConfiguration.get().getDbUrl(), "global config page let us edit it");
        }

        jenkins.restart();

        assertEquals("jdbc:postgresql://localhost:5432/jenkins", PostgresReporterGlobalConfiguration.get().getDbUrl(), "still there after restart of Jenkins");
    }
}
