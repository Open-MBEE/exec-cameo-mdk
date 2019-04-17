FROM openjdk:8u212-jdk-stretch
LABEL maintainer="David Willard <willard@jpl.nasa.gov>"
RUN apt-get update && apt-get install --no-install-recommends -y \
    openjfx \
    xvfb \
    sudo \
    xauth && \
    rm -rf /var/lib/apt/lists/*

RUN useradd -m -u 1003 -U jenkins
RUN echo "jenkins ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers
