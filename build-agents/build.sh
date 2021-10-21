#!/bin/bash

cd android || exit
docker build . -t jastenewname/android-build-agent
docker push jastenewname/android-build-agent
cd ..

cd flutter || exit
docker build . -t jastenewname/flutter-build-agent
docker push jastenewname/flutter-build-agent
cd ..