#!/bin/bash

if [ ! -d ssl ] ; then
  mkdir ssl || exit 1
fi

if [ ! -e ssl/ssl.crt ] || [ ! -e ssl/ssl.key ] ; then
  rm -f ssl/ssl.crt ssl/ssl.key
  openssl req -newkey rsa:4096 -x509 -sha256 -days 3650 -nodes -out ssl/ssl.crt -keyout ssl/ssl.key -batch
fi

if [ ! -e ssl/dhparam.pem ] ; then
  openssl dhparam -out ssl/dhparam.pem 2048
fi

GID="$(cut -d: -f3 < <(getent group docker))"
GID=${GID} docker-compose up -d || exit 2