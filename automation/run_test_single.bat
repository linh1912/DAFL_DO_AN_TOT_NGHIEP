@echo off
chcp 65001 > nul
echo ========================================================
echo   DAFL AUTOMATION - DO AN CONTINUOUS TESTING
echo   Che do: Chay 1 LOP TEST tuy chon + tao bao cao Allure
echo   Cach dung: run_test_single.bat TenLop [web ^| admin]
echo   Vi du: run_test_single.bat AuthTest web
echo          run_test_single.bat AdminCrudTest admin
echo ========================================================
cd /d "%~dp0"

set CLASS_NAME=%~1
set SIDE=%~2
if "%CLASS_NAME%"=="" goto USAGE
if "%SIDE%"=="" set SIDE=web
if /i "%SIDE%"=="web" goto SIDE_OK
if /i "%SIDE%"=="admin" goto SIDE_OK
echo [LOI] Tham so thu 2 phai la web hoac admin.
pause
exit /b 1
:USAGE
echo [LOI] Thieu ten lop test. Vi du: run_test_single.bat AuthTest web
pause
exit /b 1
:SIDE_OK
if /i "%SIDE%"=="web" set "APP_URL=http://localhost:3000"
if /i "%SIDE%"=="admin" set "APP_URL=http://localhost:3001"
rem Tim file lop de quy trong thu muc cua side (web chia theo nhom chuc nang)
rem va suy FQCN tu duong dan file; khong thay thi bao loi o NOCLASS.
if exist "%TEMP%\dafl-fqcn.txt" del "%TEMP%\dafl-fqcn.txt"
powershell -NoProfile -Command "$f = Get-ChildItem -Recurse -Filter '%CLASS_NAME%.java' 'src\test\java\vn\dafl\tests\%SIDE%' | Select-Object -First 1; if ($f) { $r = $f.FullName.Substring($f.FullName.IndexOf('vn\dafl\tests\')) -replace '\\', '.' -replace '\.java$', ''; Set-Content -Path '%TEMP%\dafl-fqcn.txt' -Value $r -NoNewline }"
if not exist "%TEMP%\dafl-fqcn.txt" goto NOCLASS
set /p FQCN=<"%TEMP%\dafl-fqcn.txt"
del "%TEMP%\dafl-fqcn.txt"
if "%FQCN%"=="" goto NOCLASS

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

rem ===== Buoc 1: Kiem tra SUT co dang chay khong =====
set HTTP_CODE=
for /f %%i in ('curl -s -o nul -w "%%{http_code}" %APP_URL% 2^>nul') do set HTTP_CODE=%%i
if not "%HTTP_CODE%"=="200" (
    echo [LOI] SUT khong tra ve 200 - ma hien tai: %HTTP_CODE%.
    echo Hay chay Docker truoc: cd C:\Users\Acer\DAFL ^&^& docker compose up -d
    echo Sau do chay lai file nay.
    pause
    exit /b 1
)
echo [OK] SUT dang chay - %APP_URL% tra ve HTTP 200.

rem ===== Buoc 2: Ghi ten lop vao testng-single.xml =====
powershell -NoProfile -Command "(Get-Content testng-single.xml) -replace 'vn\.dafl\.tests\.[A-Za-z0-9_.]+', '%FQCN%' | Set-Content testng-single.xml"
echo [INFO] Se chay lop: %FQCN%
findstr /C:"class name=" testng-single.xml

rem ===== Buoc 3: Xoa ket qua Allure cu de bao cao chi co lop nay =====
if exist "target\allure-results" rmdir /s /q "target\allure-results"

rem ===== Buoc 4: Chay test =====
echo [1/2] Dang chay lop %CLASS_NAME% - Chrome co giao dien...
call "%~dp0mvnw.cmd" test -DsuiteXmlFile=testng-single.xml -Dheadless=false -Daction.delay.ms=700 "-Dapp.url=%APP_URL%"

rem ===== Buoc 5: Tao bao cao Allure HTML =====
if not exist "target\allure-results" (
    echo [LOI] Khong tim thay target\allure-results - test chua chay hoac chua sinh ket qua.
    pause
    exit /b 1
)
echo [2/2] Dang tao bao cao Allure...
call "%~dp0mvnw.cmd" allure:report
if not exist "target\site\allure-maven-plugin\index.html" (
    echo [LOI] Tao bao cao that bai. Kiem tra lai log Maven o tren.
    pause
    exit /b 1
)
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set TS=%%i
xcopy /e /i /q "target\site\allure-maven-plugin" "..\bao-cao\allure-single-%TS%"
echo [OK] Bao cao Allure da tao xong:
echo      %~dp0..\bao-cao\allure-single-%TS%\index.html
start "" "..\bao-cao\allure-single-%TS%\index.html"
pause
exit /b 0

:NOCLASS
echo [LOI] Khong tim thay lop %CLASS_NAME% trong thu muc %SIDE%.
echo Cac lop web (go ten lop, khong can thu muc con - script tu tim file): AuthTest, AuthExtendedTest, CatalogAndSearchTest, CatalogExtendedTest, ProductDetailTest, ProductDetailExtendedTest, CartManagementTest, CartExtendedTest, CheckoutAndOrderTest, CheckoutExtendedTest, CheckoutPaymentTest, OrderCancellationTest, OrdersExtendedTest, ProfileAndAddressTest, ProfileExtendedTest, MembershipTest, MembershipExtendedTest, HomeHeroTest, HomePageTest, HomeHeaderTest, NewsletterTest, NotFoundPageTest, NotificationsFooterTest, Wave15FeaturesTest, EndToEndShoppingFlowTest, NegativeAndExceptionScenariosTest
echo Cac lop admin: AdminAndNewsletterTest, AdminCrudTest, AdminDetailTest, AdminUploadTest, AdminWave13Test, AdminReviewTest
pause
exit /b 1

:VALIDATE_JAVA_HOME
if not defined JAVA_HOME exit /b 1
if exist "%JAVA_HOME%\bin\java.exe" if exist "%JAVA_HOME%\bin\javac.exe" exit /b 0
exit /b 1
