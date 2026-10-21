#!/usr/bin/env bash

set -e
set -x

if [ "x$GRADLE_s" = "x" ]; then
  GRADLE_s=""
fi

if [ "x$GRADLE_opts" = "x" ]; then
  GRADLE_opts=""
fi

if [ "x$GRADLE_VERSION" = "x" ]; then
  GRADLE_VERSION="8.5"
fi

if [ "x$GRADLE_USER_HOME" = "x" ]; then
  GRADLE_USER_HOME="$HOME/.gradle"
fi

if [ "x$GRADLE_INSTALL" = "x" ]; then
  GRADLE_INSTALL="$HOME/.gradle/wrapper/dists"
fi

# Use the wrapper to run gradle
exec "$GRADLE_INSTALL/gradle-$GRADLE_VERSION/bin/gradle" $GRADLE_opts "$@"
