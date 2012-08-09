#!/bin/sh
#
# Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following
# conditions are met:
#
#  * Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
#
#  * Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
#
#  * Neither the name of the The National Archives nor the
#    names of its contributors may be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
# PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
# EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
# PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
# PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
# LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
# NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#


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
    java $OPTIONS -jar droid-command-line-6.1.jar "$@"
else
    java $OPTIONS -jar droid-ui-6.1.jar
fi
