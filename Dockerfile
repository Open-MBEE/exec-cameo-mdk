FROM openjdk:8u212-jdk-stretch

RUN apt-get update && apt-get install --no-install-recommends -y \
    openjfx \
    xvfb \
    xauth && \
    rm -rf /var/lib/apt/lists/*


