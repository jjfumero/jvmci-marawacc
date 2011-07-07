#!/bin/bash
if [ -z "${JDK7}" ]; then
  echo "JDK7 is not defined."
  exit 1;
fi
if [ -z "${MAXINE}" ]; then
  echo "MAXINE is not defined. It must point to a maxine repository directory."
  exit 1;
fi
if [ -z "${GRAAL}" ]; then
  echo "GRAAL is not defined. It must point to a maxine repository directory."
  exit 1;
fi
if [ -z "${DACAPO}" ]; then
  echo "DACAPO is not defined. It must point to a Dacapo benchmark directory."
  exit 1;
fi
ant -f create_examples.xml
COMMAND="${JDK7}/bin/java -client -d64 -graal -Xms1g -Xmx2g -esa -G:Extend -Xcomp -XX:CompileOnly=examples $* -jar examples.jar"
echo $COMMAND
$COMMAND
