on error goto error
call d -verbose -comp:vxworks -prod -mac -o2 rom -strip:d -lnt -endian:big -metadata cldc imp debugger
ftp -s:upgrade-ftp.script
goto done
:error
:done