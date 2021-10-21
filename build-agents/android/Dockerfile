FROM jetbrains/teamcity-agent:EAP-linux

ARG commandLineToolsVersion=7583922

USER root
RUN mkdir -p /opt/sdk/cmdline-tools/latest && chown -R buildagent /opt/sdk
USER buildagent

WORKDIR /tmp
RUN curl -L https://dl.google.com/android/repository/commandlinetools-linux-${commandLineToolsVersion}_latest.zip -o sdk.zip
RUN unzip sdk.zip && mv cmdline-tools/* /opt/sdk/cmdline-tools/latest
RUN rm -rf cmdline-tools sdk.zip
ENV PATH=${PATH}:/opt/sdk/cmdline-tools/latest/bin
ENV ANDROID_SDK_ROOT=/opt/sdk
RUN yes | sdkmanager --licenses

WORKDIR /