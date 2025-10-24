# Postgres Pipeline Reporter — Development

This document helps contributors build, test and iterate on the plugin locally.

## Prerequisites

- Java 11 (as declared in the POM `minimumJavaVersion`)
- Maven 3.6+
- A running Jenkins instance for integration testing (optional but recommended). You can use the `mvn hpi:run` goal for a lightweight dev server.

## Building

Build the plugin and package the HPI:

```bash
mvn -DskipTests package
```

The HPI will be available at `target/postgres-pipeline-reporter.hpi`.

## Running Jenkins for development

Run an embedded Jenkins with your plugin loaded:

```bash
mvn hpi:run
```

This starts a Jenkins instance on `http://localhost:8080` with the plugin built from your workspace. Use a separate browser profile to avoid interfering with your regular Jenkins.

## Tests

The project may include unit tests. Run tests with:

```bash
mvn test
```

For plugin/integration tests that need a Jenkins runtime, prefer the Jenkins test harness or `mvn hpi:run`.

## Linting / formatting

Follow the project's conventions. The parent POM includes checks like spotbugs/spotless depending on configuration. Run:

```bash
mvn verify
```

## Working with the PostgreSQL driver

Two options exist:

- Bundle the JDBC driver in the HPI (current default). The driver JAR will be placed in `WEB-INF/lib` inside the HPI.
- Depend on a provider plugin (recommended for large installations). Change the POM to mark the driver dependency as `provided` and document the admin requirement to install the provider plugin on the Jenkins instance.

If you change to `provided`, ensure the target Jenkins instance has the driver/provider installed and test with `mvn hpi:run`.

## Debugging classloader/driver issues

If you encounter `ClassNotFoundException` or driver registration problems:

- Check `target/postgres-pipeline-reporter.hpi` contains `WEB-INF/lib/postgresql-*.jar`.
- The plugin includes a `DriverShim` wrapper which registers and deregisters the driver when needed to reduce classloader leaks. Ensure it logs messages on plugin activity.

## Releasing

Follow the normal Jenkins plugin release process if you publish to Update Center. Typically this includes creating a release tag, building, and publishing the HPI to your distribution channel.

## Useful commands summary

```bash
# build
mvn -DskipTests package

# run a dev Jenkins
mvn hpi:run

# run unit tests
mvn test
```
