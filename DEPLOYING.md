# Deploying

Build the production application from the repository root:

```bash
./gradlew build
```

This runs the frontend build and packages the Spring Boot application. The Gradle build:

1. Runs `npm install` in `src/main/client`
2. Builds the React app with `npm run build`
3. Emits frontend assets into `src/main/resources/static`
4. Compiles, tests, and packages the Spring Boot app

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
