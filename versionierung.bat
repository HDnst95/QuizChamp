@echo off
setlocal enabledelayedexpansion

REM Überprüfe, ob genügend Argumente übergeben wurden
if "%~3"=="" (
    echo Usage: increment_version.bat [major] [minor] [patch]
    echo Example: increment_version.bat 1 4 0
    exit /b 1
)

REM Argumente für Major, Minor, Patch entgegennehmen
set "MAJOR=%1"
set "MINOR=%2"
set "PATCH=%3"

REM Pfad zur build.gradle Datei
set "GRADLE_FILE=app\build.gradle"

REM Extrahiere aktuelle versionCode
for /f "tokens=2 delims= " %%a in ('findstr /r /c:"versionCode " "%GRADLE_FILE%"') do (
    set "CURRENT_VERSION_CODE=%%a"
)

REM Erhöhe versionCode um 1
set /a NEW_VERSION_CODE=CURRENT_VERSION_CODE+1

REM Neue VersionName zusammenstellen
set "NEW_VERSION_NAME=%MAJOR%.%MINOR%.%PATCH%"

REM Erstelle eine temporäre Datei für die modifizierte build.gradle Datei
set "TEMP_FILE=%GRADLE_FILE%.tmp"

REM Aktualisiere versionCode und versionName in der build.gradle Datei
(for /f "delims=" %%i in (%GRADLE_FILE%) do (
    set "line=%%i"
    echo !line! | findstr /c:"versionCode " >nul
    if !errorlevel! equ 0 (
        echo         versionCode %NEW_VERSION_CODE%
    ) else (
        echo !line! | findstr /c:"versionName " >nul
        if !errorlevel! equ 0 (
            echo         versionName "%NEW_VERSION_NAME%"
        ) else (
            echo !line!
        )
    )
)) > "%TEMP_FILE%"

REM Ersetze die alte build.gradle Datei mit der neuen Datei
move /y "%TEMP_FILE%" "%GRADLE_FILE%"

echo Updated versionCode to %NEW_VERSION_CODE% and versionName to %NEW_VERSION_NAME% in %GRADLE_FILE%
endlocal
