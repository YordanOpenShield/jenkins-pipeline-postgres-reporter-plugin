# Postgres Pipeline Reporter

This Jenkins plugin records pipeline run metadata (job name, build number, status, duration and start time) into a PostgreSQL database for later reporting and analysis.

See the full documentation in the `docs/` folder:

- `docs/USAGE.md` — installation, configuration and troubleshooting
- `docs/DEVELOPMENT.md` — build, test and development instructions

Quick start

1. Build the plugin:

```bash
mvn -DskipTests package
```

2. Upload the generated `target/postgres-pipeline-reporter.hpi` via Jenkins `Manage Plugins` → `Advanced` → `Upload Plugin` and restart Jenkins if needed.

3. Configure the plugin under `Manage Jenkins` → `Configure System` → `Postgres Pipeline Reporter` with your DB URL and credentials. Create the `jenkins_pipeline_runs` table in your DB as shown in `docs/USAGE.md`.

## License

Licensed under MIT, see [LICENSE.md](LICENSE.md)

