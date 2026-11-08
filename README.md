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
│   ├── src/test/java/vn/dafl/tests/  ← 14 lớp test, 130 test case (ánh xạ TC-CN ở TEST-MAPPING.md)
│   │   ├── web/  ← 11 lớp web bán hàng, 99 test
│   │   │   ├── AuthTest (18) · CatalogAndSearchTest (11) · ProductDetailTest (9)
│   │   │   ├── CartManagementTest (9) · CheckoutAndOrderTest (10) · OrderCancellationTest (3)
│   │   │   ├── ProfileAndAddressTest (9) · EndToEndShoppingFlowTest (1)
│   │   │   ├── NegativeAndExceptionScenariosTest (17 case âm/ngoại lệ)
│   │   │   └── HomeHeroTest (10) · NotFoundPageTest (2)
│   │   └── admin/  ← 3 lớp admin, 31 test (chạy trên app Admin riêng localhost:3001)
│   │       ├── AdminAndNewsletterTest (14) · AdminDetailTest (9)  (chỉ đọc)
│   │       └── AdminCrudTest (8)  (CRUD thật, chỉ đụng dữ liệu do test tạo, tự dọn)
│   ├── testng.xml (cả 2 khối Web + Admin, 130 test) · testng-web.xml (99 test) · testng-admin.xml (31 test)
│   ├── testng-flow.xml · testng-negative.xml
│   └── run_test_with_gui.bat · run_test_headless.bat  (tự lo JDK 17 + JAVA_HOME + Wrapper)
│       run_test_web.bat (chỉ test web) · run_test_admin.bat (chỉ test admin)
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
run_test_with_gui.bat        :: toan bo suite testng.xml (130 test: 99 web + 31 admin), Chrome co giao dien
run_test_headless.bat        :: toan bo suite, che do headless (cho CI)
run_test_web.bat             :: chi test web ban hang (testng-web.xml, 99 test)
run_test_admin.bat           :: chi test admin (testng-admin.xml, 31 test) — can ca 2 service
                               :3000 + :3001 dang chay va tai khoan admin@dafl.vn da duoc seed
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

## Chạy bằng Jenkins

Xem `ci/jenkins/quick-setup/README.md` để dựng Jenkins bằng Docker (tự tạo 3 job: `DAFL-Pipeline`, `DAFL-Automation`, `DAFL-Performance`), hoặc xem hướng dẫn đầy đủ trong `docs/HUONG-DAN-TUNG-BUOC.md`.

## Tài liệu

- `docs/HUONG-DAN-TUNG-BUOC.md` — cài đặt → kiểm chứng → chạy automation/JMeter/Jenkins → demo.
- `docs/TEST-CASES-DAFL.md` / `.csv` — đặc tả test case của đồ án.
- `docs/KIEM-TRA-JENKINSFILE.md` — biên bản kiểm tra pipeline theo 10 tiêu chí.
- `docs/GIAI-THICH-MO-HINH-REPO.md` — vì sao đồ án dùng 1 repo kiểm thử và SUT tách riêng (câu hỏi thường gặp khi bảo vệ).

## Phạm vi

Repo chỉ chứa phần thuộc đồ án (kiểm thử + pipeline + tài liệu). Mã nguồn SUT và các script cài đặt môi trường thuộc về phía SUT/máy cá nhân, không nằm trong repo này.
