@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "__MVNW_ARG0_NAME__=%~nx0")
@SET ___MVNW_INTF=.%%~f0

@IF EXIST %USERPROFILE%\.m2\wrapper\dists (
  SET "MAVEN_USER_HOME=%USERPROFILE%\.m2"
) ELSE IF EXIST %APPDATA%\.m2\wrapper\dists (
  SET "MAVEN_USER_HOME=%APPDATA%\.m2"
) ELSE (
  SET "MAVEN_USER_HOME=%USERPROFILE%\.m2"
)

@SET "MAVEN_WRAPPER_JAR=%MAVEN_USER_HOME%\wrapper\dists\apache-maven-3.9.8-bin\*\bin\mvn"
@IF NOT EXIST "%MAVEN_WRAPPER_JAR%" (
  @CALL :mvnw_dl
)

@SET MAVEN_CMD_LINE_ARGS=%*
@CALL "%MAVEN_WRAPPER_JAR%" %MAVEN_CMD_LINE_ARGS%
@GOTO :EOF

:mvnw_dl
@ECHO Downloading Maven wrapper...
@POWERSHELL -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.8/apache-maven-3.9.8-bin.zip' -OutFile '%TEMP%\apache-maven-3.9.8-bin.zip'; }"
@GOTO :EOF
