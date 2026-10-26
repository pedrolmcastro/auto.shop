# syntax=docker/dockerfile:1

FROM eclipse-temurin:25-jdk-noble AS tool

RUN groupadd --gid 10001 app \
    && useradd --create-home --gid app --uid 10001 app \
    && install -d -g app -o app /home/app/.gradle /workspace/api
WORKDIR /workspace/api
USER 10001:10001

COPY --chown=app:app . .
RUN chmod 755 gradlew

ENTRYPOINT ["./gradlew", "--no-daemon"]
CMD ["build"]

FROM tool AS build

RUN --mount=gid=10001,target=/home/app/.gradle,type=cache,uid=10001 \
    ./gradlew --no-daemon bootJar

FROM build AS checks

CMD ["check"]

FROM eclipse-temurin:25-jre-noble AS runtime

RUN groupadd --gid 10001 app \
    && useradd --create-home --gid app --uid 10001 app
WORKDIR /app
COPY --chown=app:app --from=build /workspace/api/build/libs/app.jar ./app.jar
USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
