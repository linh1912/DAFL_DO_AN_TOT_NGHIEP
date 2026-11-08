@echo off
chcp 65001 > nul
echo ========================================================
echo   DAFL AUTOMATION - DO AN CONTINUOUS TESTING
echo   Che do: Co giao dien Chrome - chi chay test WEB (testng-web.xml)
echo   Target SUT: http://localhost:3000 (can SUT dang chay) - chi 11 lop web, 99 test
echo ========================================================
cd /d "%~dp0"

rem ===== Buoc 0: Xac dinh JAVA_HOME (bat buoc la JDK, co javac) =====
call :VALIDATE_JAVA_HOME
if not errorlevel 1 goto JAVA_OK
for %%V in ("Eclipse Adoptium" "Java" "Microsoft" "BellSoft" "Amazon Corretto" "Zulu") do (
    for /d %%D in ("C:\Program Files\%%~V\jdk*") do (
        set "JAVA_HOME=%%D"
        call :VALIDATE_JAVA_HOME
        if not errorlevel 1 goto JAVA_OK
    )
)
java -version >nul 2>nul
if errorlevel 1 goto INSTALL_JAVA
for /f "tokens=1,* delims==" %%A in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.home"') do set "JAVA_HOME=%%B"
if "%JAVA_HOME:~0,1%"==" " set "JAVA_HOME=%JAVA_HOME:~1%"
call :VALIDATE_JAVA_HOME
if not errorlevel 1 goto JAVA_OK
echo [LOI] Khong xac dinh duoc JAVA_HOME hop le (can JDK day du).
pause
exit /b 1
:INSTALL_JAVA
echo [JAVA] Chua tim thay Java - dang tu cai Eclipse Temurin JDK 17 qua winget...
where winget >nul 2>nul
if errorlevel 1 (
    echo [LOI] May khong co winget. Hay cai JDK 17 tu https://adoptium.net roi chay lai.
    pause
    exit /b 1
)
winget install --id EclipseAdoptium.Temurin.17.JDK -e --silent --accept-package-agreements --accept-source-agreements
for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set "JAVA_HOME=%%D"
call :VALIDATE_JAVA_HOME
if not errorlevel 1 goto JAVA_OK
echo [LOI] Da cai Java nhung chua xac dinh duoc JAVA_HOME. Hay mo lai cua so va chay lai.
pause
exit /b 1
:JAVA_OK
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo [JAVA] JAVA_HOME = %JAVA_HOME%

rem ===== Chay test bang Maven Wrapper =====
call "%~dp0mvnw.cmd" test -DsuiteXmlFile=testng-web.xml -Dheadless=false -Daction.delay.ms=700 "-Dapp.url=http://localhost:3000" %*
pause
exit /b 0

:VALIDATE_JAVA_HOME
if not defined JAVA_HOME exit /b 1
if exist "%JAVA_HOME%\bin\java.exe" if exist "%JAVA_HOME%\bin\javac.exe" exit /b 0
exit /b 1
