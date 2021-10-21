#!/bin/bash

cd android || exit
docker build . -t jastenewname/android-build-agent
docker push jastenewname/android-build-agent
cd ..