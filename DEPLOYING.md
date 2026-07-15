# Deploying

The Gradle build is self-contained:

1. Downloads a Gradle-managed Node.js/npm toolchain
2. Runs `npm ci` in `src/main/client`
3. Builds the React app with `npm run build`
4. Emits frontend assets into `src/main/resources/static`
5. Compiles, tests, and packages the Spring Boot app

The build does not require `npm` to be installed on the host or build image.

## Railway

The default Railway Java buildpack can build this app with Gradle because `build.gradle` downloads Node.js/npm automatically.

Use this build command:

```bash
./gradlew bootJar
```

Use this start command:

```bash
java -jar build/libs/scenarios-for-spring-demo-0.0.1-SNAPSHOT.jar
```

## Local Production Run

Build the production application from the repository root:

```bash
./gradlew build
```

Run the packaged application:

```bash
java -jar build/libs/scenarios-for-spring-demo-0.0.1-SNAPSHOT.jar
```

Then open:

```text
http://localhost:8080/
```

For a faster package without running tests:

```bash
./gradlew bootJar
java -jar build/libs/scenarios-for-spring-demo-0.0.1-SNAPSHOT.jar
```

`processResources` depends on `buildFrontend`, so the packaged jar includes the latest React production build automatically.
