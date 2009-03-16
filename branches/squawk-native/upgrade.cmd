on error goto error
call compile
ftp -s:upgrade-ftp.script
goto done
:error
:done