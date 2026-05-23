@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

echo ========================================
echo        TA Application System Build
echo ========================================
echo.

REM Clean and create output directory
if exist out\* (
    echo [1/3] Cleaning old files...
    rmdir /s /q out
)
mkdir out

REM Set classpath
set CP=lib\gson-2.10.1.jar;lib\json-simple-1.1.1.jar;lib\jackson-databind-2.15.2.jar;lib\jackson-core-2.15.2.jar;lib\jackson-annotations-2.15.2.jar;lib\flatlaf-3.4.1.jar

REM Generate file list
echo [2/3] Searching Java files...
set COUNT=0
(for /r src %%i in (*.java) do (
    echo %%i
    set /a COUNT+=1
)) > filelist.txt

echo        Found %COUNT% files

REM Compile
echo [3/3] Compiling...
echo ----------------------------------------

javac -d out -cp "%CP%" -sourcepath "src" @filelist.txt 2>&1

if errorlevel 1 (
    echo ----------------------------------------
    echo.
    echo Build FAILED!
    pause
    exit /b 1
) else (
    echo ----------------------------------------
    echo.
    echo ========================================
    echo        Build SUCCESSFUL!
    echo ========================================
    echo.
    echo Run the program: run.bat
    echo.
    pause
)
