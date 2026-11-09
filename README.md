# DAFL — Đồ án tốt nghiệp: Continuous Testing với Quality Gate tự động

**Đề tài:** Xây dựng và đánh giá quy trình Continuous Testing có Quality Gate tự động.

Repo này chứa **tầng kiểm thử** của đồ án (không chứa mã nguồn BE/FE). Hệ thống được kiểm thử (SUT) là ứng dụng thương mại điện tử **DAFL** — lấy từ repo gốc `sonismyname/DAFL`, được kiểm thử theo kiểu **hộp đen** qua giao diện web `http://localhost:3000` và API `http://localhost:5000`. Các pipeline trong repo này tự clone SUT vào thư mục `sut/` khi chạy.

Phần automation được **viết dựa trên mã nguồn FE/BE thật của DAFL** (mọi `data-testid`, thông điệp lỗi, dữ liệu seed đều trích từ SUT) và tổ chức theo mô hình Page Object Model.

## Cấu trúc thư mục

```text
DAFL_DO_AN_TOT_NGHIEP/
├── automation/            ← [1] Kiểm thử tự động (Java + Selenium WebDriver + TestNG + Allure, mô hình POM)
│   ├── pom.xml · mvnw · mvnw.cmd · .mvn/wrapper/   (Maven Wrapper — máy chỉ cần JDK 17)
│   ├── src/main/java/vn/dafl/
│   │   ├── commons/       ← BasePage · BaseTest · GlobalConstants
│   │   ├── driver/        ← DriverManager (WebDriver theo thread)
│   │   ├── pageUIs/       ← locator theo 7 màn hình (trích từ data-testid của FE)
│   │   ├── pageObjects/   ← 7 page object tương ứng
│   │   └── reportConfig/  ← AllureTestListener (chụp màn hình khi test fail)
│   ├── src/test/java/vn/dafl/tests/  ← 17 lớp test, 142 test case (ánh xạ TC-CN ở TEST-MAPPING.md)
│   │   ├── web/  ← 13 lớp web bán hàng, 107 test
│   │   │   ├── AuthTest (19) · CatalogAndSearchTest (12) · ProductDetailTest (9)
│   │   │   ├── CartManagementTest (9) · CheckoutAndOrderTest (10) · OrderCancellationTest (3)
│   │   │   ├── ProfileAndAddressTest (9) · EndToEndShoppingFlowTest (1)
│   │   │   ├── NegativeAndExceptionScenariosTest (17 case âm/ngoại lệ)
│   │   │   └── HomeHeroTest (10) · NotFoundPageTest (2) · NewsletterTest (3) · MembershipTest (3)
│   │   └── admin/  ← 4 lớp admin, 35 test (chạy trên app Admin riêng localhost:3001, KHÔNG cần :3000)
│   │       ├── AdminAndNewsletterTest (14) · AdminDetailTest (9)  (chỉ đọc)
│   │       ├── AdminCrudTest (8)  (CRUD thật: tạo/sửa/xóa SP + danh mục, voucher, đổi trạng thái + hủy 1 đơn 'Chờ xử lý' có sẵn, điều chỉnh điểm; chỉ đụng dữ liệu do test tạo, tự dọn — trừ ACR-06/07)
│   │       └── AdminWave13Test (4)  (đợt 13: đăng nhập admin không nút quay lại, quên mật khẩu ở admin, ghi nhận hoàn tiền, lọc ngày + Export nhật ký)
│   ├── testng.xml (cả 2 khối Web + Admin, 142 test) · testng-web.xml (107 test) · testng-admin.xml (35 test)
│   ├── testng-flow.xml · testng-negative.xml
│   └── run_test_with_gui.bat · run_test_headless.bat  (tự lo JDK 17 + JAVA_HOME + Wrapper)
│       run_test_web.bat (chỉ test web) · run_test_admin.bat (chỉ test admin)
│       run_test_web_allure.bat (chỉ test web + tạo báo cáo Allure HTML)
├── performance/           ← [2] Kiểm thử hiệu năng (Apache JMeter): dafl-loadtest.jmx — viết theo Swagger/OpenAPI của Backend (25 sampler, xác thực JWT Bearer: đăng nhập lấy token rồi gắn header Authorization)
├── quality-gate/          ← [3] Quality Gate: check_performance_gate.sh (Jenkins/Linux) + .py (Windows)
├── ci/jenkins/quick-setup/ ← Jenkins dựng sẵn bằng Docker + JCasC, tự tạo 3 job
├── Jenkinsfile            ← Pipeline TỔNG HỢP: dựng SUT → automation → performance → 2 Quality Gate → deploy khi PASS
├── Jenkinsfile.auto       ← Pipeline riêng AUTOMATION
├── Jenkinsfile.pt         ← Pipeline riêng PERFORMANCE + Quality Gate hiệu năng
└── docs/                  ← Tài liệu: hướng dẫn từng bước, đặc tả test case, kiểm tra Jenkinsfile, giải thích mô hình repo
```

## Quality Gate

- **Cổng chức năng:** bộ test automation có bất kỳ test nào FAIL → pipeline FAIL.
- **Cổng hiệu năng:** tỉ lệ lỗi ≤ **2%** và thời gian phản hồi trung bình ≤ **1000 ms** (đọc từ kết quả JMeter `.jtl` bằng script trong `quality-gate/`).

## Chạy nhanh trên Windows

Điều kiện: DAFL (SUT) đang chạy tại `localhost:3000`, máy có Internet (lần đầu, để tải JDK/Maven/thư viện nếu thiếu).

```bat
cd automation
run_test_with_gui.bat        :: toan bo suite testng.xml (142 test: 107 web + 35 admin), Chrome co giao dien
run_test_headless.bat        :: toan bo suite, che do headless (cho CI)
run_test_web.bat             :: chi test web ban hang (testng-web.xml, 107 test)
run_test_admin.bat           :: chi test admin (testng-admin.xml, 35 test) — chi can service
                               admin :3001 dang chay va tai khoan admin@dafl.vn da duoc seed
run_test_web_allure.bat      :: chi test web ban hang + tao bao cao Allure HTML (xem muc Bao cao Allure)
```

Chạy từng suite bằng Maven Wrapper:

```bat
cd automation
mvnw.cmd test -Dheadless=false -Dapp.url=http://localhost:3000
mvnw.cmd test -DsuiteXmlFile=testng-web.xml -Dheadless=false
mvnw.cmd test -DsuiteXmlFile=testng-admin.xml -Dheadless=false
mvnw.cmd test -DsuiteXmlFile=testng-flow.xml -Dheadless=true
mvnw.cmd test -DsuiteXmlFile=testng-negative.xml -Dheadless=true
```

## Báo cáo Allure (test web)

File `automation/run_test_web_allure.bat` chạy test web rồi tạo báo cáo Allure HTML:

1. Kiểm tra SUT web `:3000` có trả HTTP 200 không — chưa chạy thì dừng và báo lỗi (chạy Docker trước: `docker compose up -d`).
2. Gọi `run_test_web.bat` chạy 107 test web (suite `testng-web.xml`, Chrome có giao diện).
3. Chạy `mvnw.cmd allure:report` để sinh báo cáo từ `target/allure-results` (TestNG đã đăng ký `AllureTestListener`, pom có `allure-maven` 2.12.0).
4. Mở `target/site/allure-maven-plugin/index.html` trên trình duyệt.

```bat
cd automation
run_test_web_allure.bat
```

Báo cáo HTML thật chỉ có sau khi chạy test trên máy có SUT đang chạy. Test quên mật khẩu (FP-01/FP-02) chỉ kiểm tra UI của link/form, không gửi mail thật nên không xuất hiện trong báo cáo dưới dạng gửi mail.

## Chạy bằng Jenkins

Xem `ci/jenkins/quick-setup/README.md` để dựng Jenkins bằng Docker (tự tạo 3 job: `DAFL-Pipeline`, `DAFL-Automation`, `DAFL-Performance`), hoặc xem hướng dẫn đầy đủ trong `docs/HUONG-DAN-TUNG-BUOC.md`.

## Tài liệu

- `docs/HUONG-DAN-TUNG-BUOC.md` — cài đặt → kiểm chứng → chạy automation/JMeter/Jenkins → demo.
- `docs/TEST-CASES-DAFL.md` / `.csv` — đặc tả test case của đồ án.
- `docs/KIEM-TRA-JENKINSFILE.md` — biên bản kiểm tra pipeline theo 10 tiêu chí.
- `docs/GIAI-THICH-MO-HINH-REPO.md` — vì sao đồ án dùng 1 repo kiểm thử và SUT tách riêng (câu hỏi thường gặp khi bảo vệ).

## Phạm vi

Repo chỉ chứa phần thuộc đồ án (kiểm thử + pipeline + tài liệu). Mã nguồn SUT và các script cài đặt môi trường thuộc về phía SUT/máy cá nhân, không nằm trong repo này.

## Tách Web / Admin (chạy riêng từng phần)

Bộ test Selenium đã tách thành 2 khối theo thư mục
`automation/src/test/java/vn/dafl/tests/`:

- **Web** (`web/`) — test shop bán hàng trên `http://localhost:3000`.
- **Admin** (`admin/`) — test trang quản trị trên app admin riêng
  `http://localhost:3001`, chỉ cần service admin chạy + tài khoản
  `admin@dafl.vn` đã seed, **không cần `:3000`**.

Số lượng test từng khối thay đổi theo thời gian — xem `TEST-MAPPING.md` để
biết số liệu mới nhất.

### Chạy riêng từng khối

```bat
cd automation
run_test_web.bat      :: chi test web (testng-web.xml)
run_test_admin.bat    :: chi test admin (testng-admin.xml)
run_test_with_gui.bat :: toan bo suite (testng.xml = web + admin)
```

Hoặc bằng Maven Wrapper:

```bat
cd automation
mvnw.cmd test -DsuiteXmlFile=testng-web.xml -Dheadless=false
mvnw.cmd test -DsuiteXmlFile=testng-admin.xml -Dheadless=false
```

### Build / test FE riêng từng phần (repo SUT, đợt 13)

Ở repo SUT (`linh1912/DAFL`), frontend đã tách hẳn thành 3 thư mục dưới `src/`
(npm workspaces, chạy `npm install` 1 lần ở `src/`):

- `src/Frontend/` — app shop bán hàng (`Dockerfile`, cổng 3000).
- `src/Admin/` — app quản trị riêng (`Dockerfile`, cổng 3001).
- `src/Shared/` — code dùng chung cho cả 2 app (form đăng nhập `AuthModal`,
  API client, style đăng nhập).

Script chạy ở `src/` (workspace root): `npm run build:web` (shop),
`npm run build:admin` (admin), `npm run test:web` (vitest shop),
`npm run test:admin` (vitest admin). Backend vẫn là 1 API chung, chưa tách.
Chi tiết xem `README.md` của repo SUT.

### CI (Jenkins) — 2 stage test nối tiếp

`Jenkinsfile` (pipeline tổng hợp) và `Jenkinsfile.auto` (pipeline automation)
đều tách stage test chức năng cũ thành 2 stage **nối tiếp** (không song song,
vì 2 stage chung 1 workspace Jenkins — chạy song song sẽ đụng nhau ở
`target/surefire-reports`):

1. `Functional Regression - Web` — `mvn clean test -DsuiteXmlFile=testng-web.xml`
2. `Functional Regression - Admin` — `mvn test -DsuiteXmlFile=testng-admin.xml`

Stage Admin **không** `mvn clean` và không xóa `target/` để
`target/surefire-reports` giữ kết quả của cả 2 suite cho stage
`Functional Quality Gate` phía sau kiểm tra (nếu Admin clean thì kết quả web
bị xóa và Quality Gate chỉ còn thấy admin). Mỗi stage giữ nguyên khối post
thu thập junit / archive artifact / Allure và `|| true`. Logic Quality Gate,
deploy và các stage khác không đổi.

Lưu ý nhỏ: khối post junit của stage Admin dùng cùng glob
`automation/target/surefire-reports/*.xml` nên sẽ thu thập lại cả file XML
của suite web — Jenkins có thể liệt kê test web 2 lần trong "Test Result",
nhưng trạng thái pass/fail không đổi và Quality Gate vẫn kiểm tra đúng cả 2
suite.

`Jenkinsfile.pt` (pipeline performance) không có stage test chức năng nên
giữ nguyên, không tách.

### Backend KHÔNG tách

Cả 2 app (shop `:3000` và admin `:3001`) dùng chung **1 backend API**
(`dafl-backend`, `http://localhost:5000`) và chung 1 database. Tách backend
thành 2 service là việc khác, chưa làm.
