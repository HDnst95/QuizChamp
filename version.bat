@echo off
setlocal enabledelayedexpansion

:: Function to increment version numbers
set "version=%1"
set "part=%2"
for /f "tokens=1-3 delims=." %%a in ("%version%") do (
    set "major=%%a"
    set "minor=%%b"
    set "patch=%%c"
)

if "%part%"=="major" (
    set /a major+=1
    set minor=0
    set patch=0
) else if "%part%"=="minor" (
    set /a minor+=1
    set patch=0
) else if "%part%"=="patch" (
    set /a patch+=1
) else (
    echo Invalid part: %part%
    exit /b 1
)

set "new_version=%major%.%minor%.%patch%"

:: Tag the new version in Git
git tag -a "%new_version%" -m "Release %new_version%"
git push origin "%new_version%"

echo Version updated to %new_version%
pause