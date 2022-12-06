#!/bin/bash -e

DIR=`readlink -f $0 | xargs dirname`
cd $DIR

TESTS_DIR="../src/test/java/org/keycloak/testsuite"
TESTSUITE_FILE="base-suite"

TESTS=`echo $TESTS | sed 's/ /|/g'`

# Check all testsuite categories are defined in testsuite
for i in `ls -d $TESTS_DIR/*/ | sed "s|$TESTS_DIR||g" | sed "s|/||g"`; do
  if ( ! cat $TESTSUITE_FILE | grep "^$i," >/dev/null ); then
    echo "Test category '$i' not defined in base-suite file"
    exit 1
  fi
done