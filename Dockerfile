ARG DOCKER_URL
ARG DOCKER_ORG

FROM ${DOCKER_URL}/${DOCKER_ORG}/common-img:latest

ARG ARTIFACT_ORG
ENV PROJECT_NAME edd-core-web

# Custom build here
COPY src src
COPY test test
COPY deps.edn deps.edn

RUN set -e && clojure -A:test:unit

ARG BUILD_ID
RUN echo "Building b${BUILD_ID}" &&\
    set -e && clj -A:jar  \
      --app-group-id ${ARTIFACT_ORG} \
      --app-artifact-id ${PROJECT_NAME} \
      --app-version "1.${BUILD_ID}"

RUN echo "Building b${BUILD_ID}" &&\
    set -e && clj -A:jar  \
      --app-group-id ${ARTIFACT_ORG} \
      --app-artifact-id ${PROJECT_NAME} \
      --app-version "1.${BUILD_ID}"


RUN ls -la

COPY --chown=build:build modules modules
RUN set -e &&\
    root=$PWD &&\
    for i in "$(ls modules)"; do  \
      cd modules/$i; \
      clj -A:jar  \
           --app-group-id ${ARTIFACT_ORG} \
           --app-artifact-id $i \
           --app-version "1.${BUILD_ID}"; \
      cp pom.xml /dist/release-libs/$i-1.${BUILD_ID}.jar.pom.xml; \
      cp target/$i-1.${BUILD_ID}.jar /dist/release-libs/$i-1.${BUILD_ID}.jar; \
      cd $root; \
    done


RUN ls -la target


RUN cp pom.xml /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar.pom.xml
RUN cp target/${PROJECT_NAME}-1.${BUILD_ID}.jar /dist/release-libs/${PROJECT_NAME}-1.${BUILD_ID}.jar

RUN cat pom.xml
