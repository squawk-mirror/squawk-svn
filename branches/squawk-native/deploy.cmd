#call d user-suite -endian:big NewWPILibJ
call d user-suite -endian:big  ../rollingthunder/WPILibJ
ftp -s:deploy-ftp.script
