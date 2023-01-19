@REM
@REM Copyright (c) 2016, The National Archives <pronom@nationalarchives.gov.uk>
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

@echo off

SET DROID_HOME=%~dp0

start "" "%DROID_HOME%jre\bin\java.exe" java -jar droid-tools-${project.version}.jar %*