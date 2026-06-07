@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

title Arcade Fitness Planner - Database Initialization
color 0B

echo ============================================
echo  Arcade Fitness Planner - Database Setup
echo ============================================
echo.

REM ------------------------------------------------------------------
REM  Locate psql
REM ------------------------------------------------------------------
set "PSQL=C:\Program Files\PostgreSQL\16\bin\psql.exe"
if not exist "%PSQL%" (
    echo [ERROR] PostgreSQL 16 not found at: %PSQL%
    echo Install PostgreSQL 16 or update the path in this script.
    pause
    exit /b 1
)

REM ------------------------------------------------------------------
REM  Parse .env
REM ------------------------------------------------------------------
if not exist ".env" (
    echo [ERROR] .env file not found in %~dp0
    pause
    exit /b 1
)

for /f "usebackq delims=" %%a in (".env") do (
    set "LINE=%%a"
    if not "!LINE!"=="" if "!LINE:~0,1!" neq "#" (
        for /f "tokens=1,* delims==" %%b in ("!LINE!") do (
            set "KEY=%%b"
            set "VAL=%%c"
            if "!KEY!"=="DATABASE_URL" set "DATABASE_URL=!VAL!"
        )
    )
)

if not defined DATABASE_URL (
    echo [ERROR] DATABASE_URL not defined in .env
    pause
    exit /b 1
)

REM ------------------------------------------------------------------
REM  Parse DATABASE_URL: postgresql://user:pass@host:port/dbname
REM ------------------------------------------------------------------
set "REST=%DATABASE_URL:*://=%"

for /f "tokens=1,* delims=:" %%a in ("%REST%") do ( set "DB_USER=%%a" & set "REST2=%%b" )
for /f "tokens=1,* delims=@" %%a in ("%REST2%") do ( set "DB_PASS=%%a" & set "REST3=%%b" )
for /f "tokens=1,* delims=:" %%a in ("%REST3%") do ( set "DB_HOST=%%a" & set "REST4=%%b" )
for /f "tokens=1,* delims=/" %%a in ("%REST4%") do ( set "DB_PORT=%%a" & set "DB_NAME=%%b" )

echo Configuration loaded from .env:
echo   Host     : %DB_HOST%
echo   Port     : %DB_PORT%
echo   Database : %DB_NAME%
echo   App User : %DB_USER%
echo.

REM ------------------------------------------------------------------
REM  Step 1 — Verify PostgreSQL is reachable
REM ------------------------------------------------------------------
echo [1/4] Checking PostgreSQL server...
"%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -c "SELECT 1;" >nul 2>&1
if errorlevel 1 (
    echo PostgreSQL requires the superuser password.
    set /p "PG_SUPERPASS=Enter postgres superuser password: "
    set "PGPASSWORD=!PG_SUPERPASS!"
    "%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -c "SELECT 1;" >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Could not connect to PostgreSQL as 'postgres'.
        pause
        exit /b 1
    )
)
echo [OK] PostgreSQL is responding.
echo.

REM ------------------------------------------------------------------
REM  Step 2 — Ensure application user exists (create if missing)
REM ------------------------------------------------------------------
echo [2/4] Ensuring application user '%DB_USER%'...
"%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -tAc "SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = '%DB_USER%';" | findstr "1" >nul
if errorlevel 1 (
    "%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -q -c "CREATE USER %DB_USER% WITH PASSWORD '%DB_PASS%';"
    if errorlevel 1 (
        echo [ERROR] Failed to create user '%DB_USER%'.
        pause
        exit /b 1
    )
    echo [OK] User '%DB_USER%' created.
) else (
    "%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -q -c "ALTER USER %DB_USER% WITH PASSWORD '%DB_PASS%';"
    echo [OK] User '%DB_USER%' already exists - password updated from .env.
)
echo.

REM ------------------------------------------------------------------
REM  Step 3 — Ensure database exists with correct ownership
REM ------------------------------------------------------------------
echo [3/4] Ensuring database '%DB_NAME%'...
"%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '%DB_NAME%';" | findstr "1" >nul
if errorlevel 1 (
    "%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -q -c "CREATE DATABASE %DB_NAME% OWNER %DB_USER%;"
    if errorlevel 1 (
        echo [ERROR] Failed to create database '%DB_NAME%'.
        pause
        exit /b 1
    )
    echo [OK] Database '%DB_NAME%' created with owner '%DB_USER%'.
) else (
    "%PSQL%" -h %DB_HOST% -p %DB_PORT% -U postgres -d postgres -q -c "ALTER DATABASE %DB_NAME% OWNER TO %DB_USER%;"
    echo [OK] Database '%DB_NAME%' already exists - ownership set to '%DB_USER%'.
)
echo.

REM ------------------------------------------------------------------
REM  Step 4 — Apply schema as application user
REM ------------------------------------------------------------------
echo [4/4] Applying schema from database\schema.sql...
set "PGPASSWORD=%DB_PASS%"
"%PSQL%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -q -f "database\schema.sql"
if errorlevel 1 (
    echo [ERROR] Schema initialization failed.
    pause
    exit /b 1
)
echo [OK] Schema applied - 7 tables, indexes, and seed data created.
echo.

REM ------------------------------------------------------------------
REM  Done
REM ------------------------------------------------------------------
set "PGPASSWORD="

echo ============================================
echo  Database initialization complete!
echo ============================================
echo  User:     %DB_USER%
echo  Password: (from .env / DATABASE_URL)
echo  Database: %DB_NAME%
echo  Host:     %DB_HOST%:%DB_PORT%
echo ============================================
echo.
pause
