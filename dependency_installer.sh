#!/bin/bash

mvn install:install-file -DgroupId=user-agent-utils \
          -DartifactId=user-agent-utils -Dversion=1.6 -Dfile=lib/UserAgentUtils-1.6.jar -Dpackaging=jar \
          -DgeneratePom=true

