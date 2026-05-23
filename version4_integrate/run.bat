@echo off
chcp 65001 >nul 2>&1
cd /d "%~dp0"

setlocal enabledelayedexpansion
set "CONFIG_ARK_API_KEY="
if exist "config\ai_config.txt" (
    for /f "usebackq tokens=1,* delims==" %%a in ("config\ai_config.txt") do (
        set "line=%%a"
        set "value=%%b"
        for /f "tokens=* delims= " %%v in ("!value!") do set "value=%%v"
        if /i "!line!"=="ARK_API_KEY" (
            if not "!value!"=="" (
                if not "!value!"=="your_api_key_here" (
                    set "CONFIG_ARK_API_KEY=!value!"
                )
            )
        )
    )
)

if defined CONFIG_ARK_API_KEY (
    endlocal & set "ARK_API_KEY=%CONFIG_ARK_API_KEY%"
) else (
    endlocal & set "ARK_API_KEY=ark-ff1b9021-71b7-4d0a-9ec1-5755196fbab0-713eb"
)

set CP=out;lib\gson-2.10.1.jar;lib\json-simple-1.1.1.jar;lib\jackson-databind-2.15.2.jar;lib\jackson-core-2.15.2.jar;lib\jackson-annotations-2.15.2.jar;lib\flatlaf-3.4.1.jar

java -cp "%CP%" Authentication_Module.Main
