#!/bin/bash -e

GROUP="$1"
if [ "$GROUP" == "" ]; then
  echo "Usage: suite.sh database|fips"
  exit
fi

DIR=`readlink -f $0 | xargs dirname`
cd "$DIR"

TESTSUITE_FILE="$GROUP-suite"

TESTS=`cat $TESTSUITE_FILE`

echo "$TESTS" | sed 's/ /,/g'
