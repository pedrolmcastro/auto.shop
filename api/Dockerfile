# syntax=docker/dockerfile:1

FROM eclipse-temurin:25.0.5_8-jdk-noble AS build

RUN groupadd --gid 10001 app \
    && useradd --create-home --gid app --uid 10001 app \
    && install -d -g app -o app /home/app/.gradle /workspace/api
WORKDIR /workspace/api
USER 10001:10001

COPY --chown=app:app . .
RUN chmod 755 gradlew

RUN --mount=gid=10001,target=/home/app/.gradle,type=cache,uid=10001 \
    ./gradlew --no-daemon bootJar

FROM eclipse-temurin:25.0.5_8-jre-noble AS runtime

RUN apt-get update \
    && apt-get install -y --no-install-recommends openssl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 app \
    && useradd --create-home --gid app --uid 10001 app \
    && install -d -m 700 -o app -g app /app/secrets
WORKDIR /app
COPY --from=build /workspace/api/build/libs/app.jar ./app.jar
COPY --chmod=755 scripts/docker-app-entrypoint.sh /usr/local/bin/docker-app-entrypoint

USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["docker-app-entrypoint"]
