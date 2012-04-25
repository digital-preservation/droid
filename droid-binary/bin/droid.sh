#!/bin/sh

# DROID launch script for UNIX/Linux/Mac systems
# ==============================================

# Settings:
# =========

# Default work dir: droidUserDir
# ------------------------------
# This is where droid will place user settings
# If not set, it will default to a directory called ".droid6" 
# under the user's home directory.
# It can be configured using this property, or by an environment
# variable of the same name.
droidUserDir=""

# Default work dir: droidTempDir
# ------------------------------
# This is where droid will create temporary files.
# If not set, it will default to a directory called "tmp" 
# under the droid user directory.
# It can be configured using this property, or by an environment
# variable of the same name.
droidTempDir=""

# Default log dir: droidLogDir
# ----------------------------
# This is where droid will write its log files.
# If not set, it will default to a folder called "logs"
# under the droidWorkDir.
# It can be configured using this property, or by an environment
# variable of the same name.
droidLogDir=""


# Log configuration: log4j
# ------------------------
# This is the location of the lo4j configuration file to use.
# By default, it will use a file called "log4j.properties"
# which is found under the droidWorkDir.
# It can be configured using this setting, or by an environment
# variable called log4j.configuration
log4j=""


# Default console logging level
# -----------------------------
# This allows you to set the default logging level used by DROID
# when logging to the command line console.  If not set,
# it defaults to INFO level logging, unless running in quiet
# mode from the command-line, in which case the log level is
# overridden to be ERROR.
logLevel=""

# Max memory: droidMemory
# -----------------------
# The maximum memory for DROID to use in megabytes.
droidMemory="512m"



# Run DROID:
# ==========

# Collect settings into runtime options for droid:
OPTIONS=""

# Detect if we are running on a mac or not:
os=`uname`
if [ "Darwin" = "$os" ]; then
    OPTIONS=$OPTIONS" -Xdock:name=DROID"
    OPTIONS=$OPTIONS" -Dcom.apple.mrj.application.growbox.intrudes=false"
    OPTIONS=$OPTIONS" -Dcom.apple.mrj.application.live-resize=true"
fi

# Build command line options from the settings above:
if [ -n "$droidMemory" ]; then
    OPTIONS=$OPTIONS" -Xmx$droidMemory"
fi
if [ -n "$droidUserDir" ]; then
    OPTIONS=$OPTIONS" -DdroidUserDir=$droidUserDir"
fi
if [ -n "$droidTempDir" ]; then
    OPTIONS=$OPTIONS" -DdroidTempDir=$droidTempDir"
fi
if [ -n "$droidLogDir" ]; then
    OPTIONS=$OPTIONS" -DdroidLogDir=$droidLogDir"
fi
if [ -n "$log4j" ]; then
    OPTIONS=$OPTIONS" -Dlog4j.configuration=$log4j"
fi
if [ -n "$logLevel" ]; then
    OPTIONS=$OPTIONS" -DconsoleLogThreshold=$logLevel"
fi

# echo "Running DROID with these options: $OPTIONS $@"

# Run the command line or user interface version with the options:
if [ $# -gt 0 ]; then
    java $OPTIONS -jar droid-command-line-6.0.jar "$@"
else
    java $OPTIONS -jar droid-ui-6.0.jar
fi
