FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
VOLUME /tmp
ARG DEPENDENCY=build
RUN echo ${DEPENDENCY}
COPY build/libs/biddingsystem-java-0.0.1.jar biddingsystem-java.jar
ENTRYPOINT ["java","-jar","biddingsystem-java.jar", "0"]