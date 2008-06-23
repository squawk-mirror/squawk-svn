#!/bin/sh
#
./squawk com.sun.squawk.Test
STAT=$?
# status got trunctated from 12345 to 57
if [ $STAT -eq 57 ]; then
	echo good  $STAT
	exit 0
else
	echo bad $STAT
	exit $STAT
fi
