@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

echo ========================================
echo        Pack version4_integrate.jar
echo ========================================
echo.

if exist out\* rmdir /s /q out
mkdir out

set CP=lib\gson-2.10.1.jar;lib\json-simple-1.1.1.jar;lib\jackson-databind-2.15.2.jar;lib\jackson-core-2.15.2.jar;lib\jackson-annotations-2.15.2.jar;lib\flatlaf-3.4.1.jar

echo [1/2] Compiling...
(for /r src %%i in (*.java) do echo %%i) > filelist.txt
javac -d out -cp "%CP%" -sourcepath "src" @filelist.txt
if errorlevel 1 (
    echo Compile FAILED!
    exit /b 1
)

echo [2/2] Creating jar...
(
echo Main-Class: Authentication_Module.Main
echo Class-Path: lib/gson-2.10.1.jar lib/json-simple-1.1.1.jar lib/jackson-databind-2.15.2.jar lib/jackson-core-2.15.2.jar lib/jackson-annotations-2.15.2.jar lib/flatlaf-3.4.1.jar
) > manifest.txt

jar cfm version4_integrate.jar manifest.txt -C out .
if errorlevel 1 (
    echo Pack FAILED!
    exit /b 1
)

echo.
echo SUCCESS: version4_integrate.jar
echo Run: java -jar version4_integrate.jar
echo.
