#!/bin/bash -e

GROUP=$1
if [ "$GROUP" == "" ]; then
  echo "Usage: base-suite.sh <group>"
  exit
fi

DIR=`readlink -f $0 | xargs dirname`
cd $DIR

TESTSUITE_FILE="base-suite"
TESTS=`cat $TESTSUITE_FILE | grep ",$GROUP" | sed "s/,$GROUP//"`
TESTS=`echo $TESTS | sed 's/ /|/g'`

if [ "$TESTS" == "" ]; then
  echo "Undefined group '$GROUP'"
  exit 1
fi

echo "%regex[org.keycloak.testsuite.($TESTS).*]"