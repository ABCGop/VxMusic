@echo off
echo Building and signing APK...

REM Clean the project
call gradlew clean

REM Build the release APK for both flavors
echo Building FOSS release APK...
call gradlew assembleFossRelease

echo Building Full release APK...
call gradlew assembleFullRelease

REM Show the location of generated APKs
echo.
echo APKs generated at:
echo - app\build\outputs\apk\foss\release\
echo - app\build\outputs\apk\full\release\

REM List the generated APK files
dir "app\build\outputs\apk\foss\release\*.apk" /b 2>nul
dir "app\build\outputs\apk\full\release\*.apk" /b 2>nul

echo.
echo Build completed successfully!
pause