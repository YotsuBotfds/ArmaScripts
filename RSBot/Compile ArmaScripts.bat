@ECHO OFF
ECHO Deleting last ArmaScripts folder
RMDIR /Q /S C:\Users\Owner\Documents\RSBot\Scripts\Sources\ArmaScripts\
ECHO Copying new files
XCOPY /Q /S /Y C:\Users\Owner\Desktop\ArmaScripts\RSBot C:\Users\Owner\Documents\RSBot\Scripts\Sources\
PAUSE