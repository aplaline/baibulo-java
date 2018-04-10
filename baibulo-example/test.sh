#!/bin/sh

#curl --verbose -X PUT -H 'Version: 1' --data-binary '@pom.xml' http://localhost:8080/baibulo-example/assets/pom.xml
curl -v -X GET -H 'Version: 1' http://localhost:8080/baibulo-example/assets/pom.xml

