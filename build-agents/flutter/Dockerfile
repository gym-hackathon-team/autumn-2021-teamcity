FROM jastenewname/android-build-agent

ARG platformVersion=30
ARG buildToolsVersion=29.0.2
ARG flutterVersion=2.5.3

USER root
RUN mkdir /opt/flutter && chown -R buildagent /opt/flutter
USER buildagent

RUN sdkmanager "platform-tools" "platforms;android-${platformVersion}" "build-tools;${buildToolsVersion}"

WORKDIR /tmp
RUN curl -L https://storage.googleapis.com/flutter_infra_release/releases/stable/linux/flutter_linux_${flutterVersion}-stable.tar.xz -o flutter.tar.xz
RUN tar xf flutter.tar.xz -C /opt
RUN rm -f flutter.tar.xz
ENV PATH=${PATH}:/opt/flutter/bin
RUN flutter precache web

WORKDIR /