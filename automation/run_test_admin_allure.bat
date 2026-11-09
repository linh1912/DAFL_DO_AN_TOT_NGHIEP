@echo off
chcp 65001 > nul
echo ========================================================
echo   DAFL AUTOMATION - DO AN CONTINUOUS TESTING
echo   Che do: Chay test ADMIN (testng-admin.xml) + tao bao cao Allure
echo   Target SUT: http://localhost:3001 - 5 lop admin, 38 test
echo   Bao cao HTML: automation\target\site\allure-maven-plugin\index.html
echo   Luu y: bao cao nay dung chung duong dan voi ban web,
echo   chay admin se thay the bao cao web truoc do (va nguoc lai).
echo ========================================================
cd /d "%~dp0"

rem ===== Buoc 0: Kiem tra SUT admin :3001 co dang chay khong =====
set HTTP_CODE=
for /f %%i in ('curl -s -o nul -w "%%{http_code}" http://localhost:3001 2^>nul') do set HTTP_CODE=%%i
if not "%HTTP_CODE%"=="200" (
    echo [LOI] Admin :3001 khong tra ve 200 - ma hien tai: %HTTP_CODE%.
    echo Hay chay Docker truoc:  cd C:\Users\Acer\DAFL ^&^& docker compose up -d
    echo Sau do chay lai file nay.
    pause
    exit /b 1
)
echo [OK] Admin :3001 dang chay (HTTP 200).

rem ===== Buoc 1: Chay test admin (dung lai script cu, co san JAVA_HOME + Maven Wrapper) =====
echo [1/2] Dang chay 38 test admin (Chrome co giao dien)...
call "%~dp0run_test_admin.bat"

rem ===== Buoc 2: Tao bao cao Allure HTML tu ket qua test =====
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
echo [OK] Bao cao Allure da tao xong:
echo      %~dp0target\site\allure-maven-plugin\index.html
start "" "target\site\allure-maven-plugin\index.html"
pause
exit /b 0
