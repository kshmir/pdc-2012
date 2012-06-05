#!/bin/bash

for f in lib/*.jar
do
  if [[ $f =~ lib/gdata-([a-z-]*)-(.*)\.jar ]]; then
      n=${BASH_REMATCH[1]}
      v=${BASH_REMATCH[2]}

      echo "installing mvn artifact $n $v"
      mvn install:install-file -DgroupId=com.google.gdata \
         -DartifactId=$n -Dversion=$v -Dfile=$f -Dpackaging=jar \
         -DgeneratePom=true
  fi
done
