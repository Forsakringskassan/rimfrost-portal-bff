# portal-bff

Backend-for-frontend for the handläggare portal. Proxies task data from the OUL service, serves the remotes config for module federation, and exposes mock handläggare data behind a feature flag.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_** Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:9001/q/dev/>.

The application runs on port **9001** by default (matches the old TypeScript BFF).

To run the full build locally (mirrors CI, skips Docker):

```shell script
./mvnw verify -Dquarkus.container-image.build=false
```

## Environment variables

| Variable | Default | Description |
|---|---|---|
| `BE_OUL_URL` | `http://localhost:8889` | Base URL for the OUL backend |
| `PORTAL_REMOTES_CONFIG_PATH` | _(classpath)_ | Path to a `remotes.json` override (e.g. a mounted ConfigMap in Kubernetes). Falls back to the bundled `src/main/resources/remotes.json`. |
| `PORTAL_MOCK_HANDLAGGARE` | `true` | Enables the mock `GET /handlaggare` response. Set to `false` in environments where real handläggare data is available. |

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Packaging and running as Docker

Build a Docker image _rimfrost/rimfrost-portal-bff:latest_:

```shell script
./mvnw clean package
```

Launch container:

```shell script
docker run -p 9001:9001 \
  -e BE_OUL_URL=http://host.docker.internal:8889 \
  rimfrost/rimfrost-portal-bff
```

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/rimfrost-portal-bff-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/route-manifest` | Returns `remotes.json` — the module federation remote registry. Reads from `PORTAL_REMOTES_CONFIG_PATH` if set, otherwise from the bundled classpath resource. |
| `GET` | `/handlaggare` | Returns mock handläggare data when `PORTAL_MOCK_HANDLAGGARE=true`, otherwise `503`. |
| `POST` | `/tasks` | Fetches all operative tasks for a handläggare from OUL and transforms them to the portal model. |
| `POST` | `/tasks/getNext` | Assigns the next available task to a handläggare via OUL and returns the transformed result. |

Health: <http://localhost:9001/q/health>
