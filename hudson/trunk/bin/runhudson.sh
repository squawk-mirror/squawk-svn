#!/bin/csh

setenv HUDSON_HOME /scratch/dw29446/hudson/hudson_home

/usr/java/bin/java -jar $HUDSON_HOME/hudson.war &
