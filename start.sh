#!/bin/bash

if [ ! -d ssl ] ; then
  mkdir ssl
fi

if [ ! -a ssl/ssl.crt ] || [ ! -a ssl/ssl.key ] ; then
  rm -f ssl/ssl.crt ssl/ssl.key
  openssl req -newkey rsa:4096 -x509 -sha256 -days 3650 -nodes -out ssl/ssl.crt -keyout ssl/ssl.key -batch
fi

if [ ! -a ssl/dhparam.pem ] ; then
  openssl dhparam -out ssl/dhparam.pem 2048
fi

GID="$(cut -d: -f3 < <(getent group docker))"
GID=${GID} docker-compose up -d