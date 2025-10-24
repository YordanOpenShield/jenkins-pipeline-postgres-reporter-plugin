# Postgres Pipeline Reporter — Usage

This document shows how to install, configure and use the Postgres Pipeline Reporter Jenkins plugin.

## Purpose

The plugin records pipeline run information (job name, build number, status, duration and start time) into a PostgreSQL database for reporting and analytics.

## Installation

1. Build the plugin HPI (or download a released HPI):

   ```bash
   mvn -DskipTests package
   ```

2. In your Jenkins instance, go to `Manage Jenkins` → `Manage Plugins` → `Advanced` → `Upload Plugin` and upload the generated `target/postgres-pipeline-reporter.hpi` file. Restart Jenkins if prompted.

3. Alternatively, install via the Jenkins Update Center if the plugin is published there.

## Configuration

After installation, configure the plugin under `Manage Jenkins` → `Configure System` → `Postgres Pipeline Reporter` (or the plugin's global configuration page).

Required values:

- `DB URL`: JDBC URL to your PostgreSQL instance, e.g. `jdbc:postgresql://dbhost:5432/jenkins`
- `DB User`: username with INSERT privileges on the target schema/table
- `DB Password`: password for the DB user

The plugin will attempt to connect to the database on pipeline completion and insert a row into `jenkins_pipeline_runs`.

## Database schema

By default the plugin inserts into a table named `jenkins_pipeline_runs`. Create the table with a SQL statement similar to:

```sql
CREATE TABLE jenkins_pipeline_runs (
  id serial PRIMARY KEY,
  job_name text NOT NULL,
  build_number integer NOT NULL,
  status text,
  duration_ms bigint,
  start_time timestamp with time zone
);
```

Adjust column types or add indexes as needed.

## Example Pipeline

No pipeline changes are required — the plugin listens to pipeline run completion and records data automatically. A minimal example Jenkinsfile:

```groovy
pipeline {
  agent any
  stages {
    stage('Build') { steps { echo 'building' } }
  }
}
```

## Troubleshooting

- If you see `ClassNotFoundException: org.postgresql.Driver` in the Jenkins logs, ensure the plugin HPI contains `WEB-INF/lib/postgresql-*.jar`. The HPI produced by the project includes the driver by default.
- If connection fails, check the DB URL, credentials and network connectivity from the Jenkins host.
- Check Jenkins logs and the plugin log lines prefixed with `[PostgresReporter]` for diagnostic messages.

## Security considerations

- Store DB credentials securely — the plugin uses Jenkins global configuration where credentials should be handled via Jenkins credentials store when available.
- Limit DB user privileges to only the actions required by the plugin (INSERT/SELECT as needed).

## Uninstall

Remove the plugin from `Manage Plugins` or delete the HPI from the Jenkins `plugins` directory and restart Jenkins. Also consider cleaning up inserted data if desired.
