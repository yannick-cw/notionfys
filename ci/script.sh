#!/bin/bash

# Exit on any failure
set -e

sbt assembly

VERSION=`sbt version | tail -n 1 | awk '{print $2}'`

if [ $TRAVIS_OS_NAME = windows ]; then
  find target -iname "*.jar" -exec cp {} notionfys.jar \;
  ci/windows.bat
elif [ $TRAVIS_OS_NAME = osx ]; then
  native-image --verbose --no-fallback -jar "target/scala-2.13/notionfys-assembly-$VERSION.jar" notionfys
else
  native-image --verbose --static --no-fallback -jar "target/scala-2.13/notionfys-assembly-$VERSION.jar" notionfys
fi

mkdir release

if [ $TRAVIS_OS_NAME = windows ]; then
  mv notionfys.exe release/notionfy.exe
else
  mv notionfys "release/notionfy_$TRAVIS_OS_NAME"
fi

