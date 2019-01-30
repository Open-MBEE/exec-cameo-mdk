FROM circleci/openjdk:8

RUN sudo mkdir /usr/lib/jvm/oracle-java-8
RUN sudo curl --retry 5 -L -b "oraclelicense=a" https://download.oracle.com/otn-pub/java/jdk/8u201-b09/42970487e3af4f5aa5bca3f542482c60/jdk-8u201-linux-x64.tar.gz | sudo tar -xz --strip-components=1 -C /usr/lib/jvm/oracle-java-8

RUN sudo update-alternatives --install /usr/bin/java  java  /usr/lib/jvm/oracle-java-8/bin/java  2000
RUN sudo update-alternatives --install /usr/bin/javac javac /usr/lib/jvm/oracle-java-8/bin/javac 2000
RUN sudo update-alternatives --auto java
RUN sudo update-alternatives --auto javac

ENV JAVA_HOME /usr/lib/jvm/oracle-java-8
ENV PATH $JAVA_HOME/bin:$PATH
	
