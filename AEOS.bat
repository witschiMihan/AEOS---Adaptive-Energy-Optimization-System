@echo off
REM ============================================================
REM AEOS - Standalone Desktop Application Launcher
REM Runs the compiled Swing desktop application
REM Author: Witschi B. Mihan
REM Date: January 14, 2026
REM ============================================================

setlocal enabledelayedexpansion

REM Set title
title AEOS - Adaptive Energy Optimization System

REM Get the directory where this batch file is located
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

REM ============================================================
REM Check if Java is installed
REM ============================================================
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo ============================================================
    echo ERROR: Java is not installed or not in PATH
    echo ============================================================
    echo.
    echo Please install Java 21 from:
    echo https://adoptium.net/temurin/releases/
    echo.
    echo Then add Java to your PATH environment variable.
    echo.
    pause
    exit /b 1
)

REM ============================================================
REM Display startup message
REM ============================================================
cls
echo.
echo ============================================================
echo AEOS - Adaptive Energy Optimization System
echo Desktop Application v2.0
echo ============================================================
echo.
echo Initializing application...
echo Please wait...
echo.

REM ============================================================
REM Check if compiled classes exist in bin folder
REM ============================================================
if exist "bin\gui\EnergyApp.class" (
    echo Using pre-compiled classes...
    echo.
    echo Launching AEOS...
    cd /d "%SCRIPT_DIR%"
    java -cp bin gui.EnergyApp
    if %errorlevel% neq 0 (
        echo.
        echo ============================================================
        echo ERROR: Failed to start AEOS application
        echo Error Code: %errorlevel%
        echo ============================================================
        pause
    )
) else (
    echo Pre-compiled classes not found.
    echo.
    echo Please compile the project first:
    echo   mvn compile
    echo.
    echo This will create compiled classes in the bin folder.
    echo.
    pause
    exit /b 1
)

endlocal
