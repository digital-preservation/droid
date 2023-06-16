@echo off
REM Infer DROID_HOME from script location
SET DROID_HOME=%~dp0

"${JRE_BIN_PATH}java" -jar droid-tools-${project.version}.jar %*
