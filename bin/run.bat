@SETLOCAL enabledelayedexpansion

@SET BASENAME=%~dp0

@FOR %%a IN ("%BASENAME:~0,-1%") DO @SET ROOTDIR=%%~dpa

@SET CLASSPATH=%ROOTDIR%resources
@FOR %%i IN ("%ROOTDIR%*.jar") DO @SET CLASSPATH=%CLASSPATH%;%%i

@FOR %%i IN ("%ROOTDIR%lib\*.jar") DO @CALL SET CLASSPATH=!CLASSPATH!;%%i

@SET JAVABIN=java
@IF NOT "%JAVA_HOME%"=="" @SET JAVABIN=%JAVA_HOME%\bin\java

@"%JAVABIN%" -Dfile.encoding=UTF-8 -cp "%CLASSPATH%" templating.TemplateMachine %*
