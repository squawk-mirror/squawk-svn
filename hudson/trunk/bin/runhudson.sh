#!/bin/csh

setenv HUDSON_HOME .

/usr/bin/java -server -jar hudson.war &
