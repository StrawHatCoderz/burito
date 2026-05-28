#!/bin/sh
cd "$(dirname "$0")/backend" && exec ./gradlew "$@"
