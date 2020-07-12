@SETLOCAL enabledelayedexpansion

@SET BASENAME=%~dp0

@FOR %%a IN ("%BASENAME:~0,-1%") DO @SET ROOTDIR=%%~dpa

@SET CLASSPATH=%ROOTDIR%resources
@for %%i in (%ROOTDIR%*.jar) DO @SET CLASSPATH=%CLASSPATH%;%%i

@for %%i in (%ROOTDIR%lib\*.jar) DO @CALL SET CLASSPATH=!CLASSPATH!;%%i

@java -cp %CLASSPATH% templating.Templating %*
pause