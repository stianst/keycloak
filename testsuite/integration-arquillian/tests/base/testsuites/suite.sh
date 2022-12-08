#!/bin/bash -e

GROUP="$1"
if [ "$GROUP" == "" ]; then
  echo "Usage: suite.sh database|fips"
  exit
fi

DIR=`readlink -f $0 | xargs dirname`

TESTS=`cat $DIR/$GROUP-suite`

echo "$TESTS" | sed 's/\n/,/g'
