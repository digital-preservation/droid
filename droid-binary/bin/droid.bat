@REM
@REM Copyright (c) 2012, The National Archives <pronom@nationalarchives.gsi.gov.uk>
@REM All rights reserved.
@REM
@REM Redistribution and use in source and binary forms, with or without
@REM modification, are permitted provided that the following
@REM conditions are met:
@REM
@REM  * Redistributions of source code must retain the above copyright
@REM    notice, this list of conditions and the following disclaimer.
@REM
@REM  * Redistributions in binary form must reproduce the above copyright
@REM    notice, this list of conditions and the following disclaimer in the
@REM    documentation and/or other materials provided with the distribution.
@REM
@REM  * Neither the name of the The National Archives nor the
@REM    names of its contributors may be used to endorse or promote products
@REM    derived from this software without specific prior written permission.
@REM
@REM THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
@REM AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
@REM IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
@REM PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
@REM CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
@REM EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
@REM PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
@REM PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
@REM LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
@REM NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
@REM SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
@REM

@ECHO OFF

REM DROID launch script for Windows systems
REM ========================================

REM Settings:
REM =========

REM Default user dir: 
REM -----------------
REM This is where droid will place user settings 
REM If not set, it will default to a directory called ".droid6"
REM under the user's home directory.
REM Also configure this property using the environment variable: droidUserDir
REM Remove the "REM " from the line below and set the working dir path after the "=".
REM SET droidUser=


REM Default temporary file dir:
REM ---------------------------
REM This is where droid will place temporary working files,
REM including decompressed archival files and working profile databases.
REM If not set, it will default to the droidUserDir 
REM (by default, under the user's home directory)
REM Also configure this property using the environment variable: droidTempDir
REM Remove the "REM " from the line below and set the working dir path after the "=".
REM SET droidTemp=


REM Default log dir: 
REM ----------------
REM This is where droid will write its log files.
REM If not set, it will default to a folder called "logs"
REM under the droidUserDir.
REM Also configure this property using the environment variable: droidLogDir
REM Remove the "REM " from the line below and set the log dir path after the "=".
REM SET droidLog=


REM Log configuration: 
REM ------------------
REM This is the location of the lo4j configuration file to use.
REM By default, it will use a file called "log4j.properties"
REM which is found under the droidUserDir.
REM Also configure this property using the environment variable: log4j.configuration
REM Remove the "REM " from the line below and set the log config file path after the "=".
REM SET log4j=


REM Default console logging level:
REM ------------------------------
REM This allows you to set the default logging level used by
REM DROID when logging to the command line console.  If not set,
REM it defaults to INFO level logging, unless running in quiet
REM mode from the command-line, in which case the log level is
REM overridden to be ERROR.
REM SET logLevel=INFO


REM Maximum memory:
REM ---------------
REM This is the maximum memory DROID can use in megabytes.
REM Remove the "REM " from the line below and set the maximum memory after the "=".
REM Also configure this property using the environment variable: droidMemory.
REM SET droidMemory=512



REM Assemble options
REM ================
REM default to using 512 megabytes of memory if no other settings provided:
SET DROIDOPTIONS="-Xmx512m"

IF "%droidMemory%"=="" GOTO UserDir
SET DROIDOPTIONS="-Xmx%droidMemory%m"

:UserDir
IF "%droidUser%"=="" GOTO TempDir
SET DROIDOPTIONS=%DROIDOPTIONS% "-DdroidUserDir=%droidUser%"

:TempDir
IF "%droidTemp%"=="" GOTO LogOptions
SET DROIDOPTIONS=%DROIDOPTIONS% "-DdroidTempDir=%droidTemp%"

:LogOptions
IF "%droidLog%"=="" GOTO Log4JConfig
SET DROIDOPTIONS=%DROIDOPTIONS% "-DdroidLogDir=%droidLog%"

:Log4JConfig
IF "%log4j%"=="" GOTO LogLevel
SET DROIDOPTIONS=%DROIDOPTIONS% "-Dlog4j.configuration=%log4j%"

:LogLevel
IF "%logLevel%"=="" GOTO RunDROID
SET DROIDOPTIONS=%DROIDOPTIONS% "-DconsoleLogThreshold=%logLevel%"


REM Run DROID:
REM ==========
:RunDROID

REM ECHO Running DROID with the following options: %DROIDOPTIONS%

REM Choose whether to run the command line or gui version of DROID:
IF "%1"=="" GOTO NOPARAM

:PARAM
REM has command line parameters passed - run command line version:
java %DROIDOPTIONS% -jar droid-command-line-6.1.jar %*

GOTO end

:NOPARAM
REM no command line parameters passed - run GUI version:
start javaw %DROIDOPTIONS% -jar droid-ui-6.1.jar

:END



