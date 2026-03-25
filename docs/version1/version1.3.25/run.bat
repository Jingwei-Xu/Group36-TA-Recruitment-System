@echo off

setlocal enabledelayedexpansion



REM ===============================

REM TA Management Swing App Runner

REM javac + classpath (Windows)

REM ===============================



cd /d "%~dp0"



set "APP_MAIN=edu.ebu6304.app.TAManagementApp"

set "SRC_DIR=src\main\java"

set "OUT_DIR=out\classes"

set "LIB_DIR=lib"

set "GSON_JAR=%LIB_DIR%\gson-2.11.0.jar"

set "DATA_DIR=D:\Code\Program\EBU6304\3.29\data"



if not exist "%SRC_DIR%" (

  echo [ERROR] Source directory not found: %SRC_DIR%

  pause

  exit /b 1

)



if not exist "%DATA_DIR%" (

  echo [ERROR] Data directory not found: %DATA_DIR%

  pause

  exit /b 1

)



where javac >nul 2>nul

if errorlevel 1 (

  echo [ERROR] javac not found. Please install JDK and add it to PATH.

  pause

  exit /b 1

)



if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"



if not exist "%GSON_JAR%" (

  echo [INFO] gson jar not found, downloading...

  powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar' -OutFile '%GSON_JAR%' -UseBasicParsing; exit 0 } catch { Write-Host $_.Exception.Message; exit 1 }"

  if errorlevel 1 (

    echo [ERROR] Failed to download gson jar.

    echo Please manually place gson-2.11.0.jar at: %GSON_JAR%

    pause

    exit /b 1

  )

)



if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"

mkdir "%OUT_DIR%"



echo [INFO] Collecting Java source files...

dir /s /b "%SRC_DIR%\*.java" > "%TEMP%\ta_app_sources.txt"



echo [INFO] Compiling...

javac -encoding UTF-8 -cp "%GSON_JAR%" -d "%OUT_DIR%" @"%TEMP%\ta_app_sources.txt"

if errorlevel 1 (

  echo [ERROR] Compilation failed.

  del "%TEMP%\ta_app_sources.txt" >nul 2>nul

  pause

  exit /b 1

)



del "%TEMP%\ta_app_sources.txt" >nul 2>nul



echo [INFO] Launching application...

java -cp "%OUT_DIR%;%GSON_JAR%" %APP_MAIN% "%DATA_DIR%"



if errorlevel 1 (

  echo [ERROR] Application exited with error.

  pause

  exit /b 1

)



echo [INFO] Application exited normally.

endlocal

