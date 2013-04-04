@echo off
rem
rem This script set classpath
rem 	Usage: classpath [run|build] [set] [queit]
rem By default it sets CLASSPATH to execute direct java invocations (all included)
rem option build prepares LOCALCLASSPATH to use in build.bat
rem option build prepares LOCALCLASSPATH to use in run.bat
rem by using set CLASSPATH also be set by run|build
rem by using queit no echo of set CLASSOATH well be visible
rem


set LOCALCLASSPATH=
for %%i in (lib\dsig_globus\*.jar) do call lcp.bat %%i
for %%i in (lib\opensaml\*.jar) do call lcp.bat %%i
for %%i in (lib\servlet_api\*.jar) do call lcp.bat %%i
for %%i in (lib\xbeans\*.jar) do call lcp.bat %%i
for %%i in (lib\xpp3\*.jar) do call lcp.bat %%i
for %%i in (lib\util_concurrent\*.jar) do call lcp.bat %%i
for %%i in (lib\junit\*.jar) do call lcp.bat %%i
for %%i in (generated\xmlbeans_typelib\*.jar) do call lcp.bat %%i


REM check options on how to set classpath

if "%1" == "build" goto build_classpath
if "%1" == "run" goto run_classpath
if "%1" == "clean" goto clean_classpath

REM otherwise set user classpath

set _BC=build\classes
set LOCALCLASSPATH=%_BC%\dsig_globus;%_BC%\xbeans;%_BC%\samples;%_BC%\tests;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\generated;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\xpola;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\common;%_BC%\client;%_BC%\server;%_BC%\rpc;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\xwsif;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\xservo;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\xwsdl;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\xs;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\puretls;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\ws_secconv;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\dispatcher;%LOCALCLASSPATH%
set LOCALCLASSPATH=%_BC%\msg_box;%LOCALCLASSPATH%
REM set LOCALCLASSPATH=classes.cpr;%LOCALCLASSPATH%
REM does not work on Windows XP: for %%i in (build\classes\*) do call lcp.bat %%i

set CLASSPATH=%LOCALCLASSPATH%

if "%1" == "quiet" goto end

echo %CLASSPATH%


goto end

:clean_classpath
set CLASSPATH=
set LOCALCLASSPATH=

if "%2" == "quiet" goto end

echo set CLASSPATH=%CLASSPATH%
echo set LOCALCLASSPATH=%LOCALCLASSPATH%

goto end

:build_classpath
REM for %%i in (lib\ant\*.jar) do call lib\ant\lcp.bat %%i
REM for %%i in (lib\jakarta-regexp\*.jar) do call lib\ant\lcp.bat %%i
if exist %JAVA_HOME%\lib\tools.jar set LOCALCLASSPATH=%LOCALCLASSPATH%;%JAVA_HOME%\lib\tools.jar

goto extra_args

:run_classpath
set LOCALCLASSPATH=build\classes;build\samples;build\tests;%LOCALCLASSPATH%

:extra_args


if not "%2" == "set" goto check_echo

set CLASSPATH=%LOCALCLASSPATH%

:check_echo

if "%2" == "quiet" goto end
if "%3" == "quiet" goto end

echo %LOCALCLASSPATH%

goto end

:end
