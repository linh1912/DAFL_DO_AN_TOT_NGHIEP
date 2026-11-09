# HƯỚNG DẪN CÀI ĐẶT VÀ VẬN HÀNH HỆ THỐNG KIỂM THỬ DAFL

Tài liệu này hướng dẫn cài đặt và vận hành toàn bộ hệ thống của đồ án **Continuous Testing với Quality Gate tự động** trên một máy tính Windows cá nhân, theo đúng thứ tự thực hiện: cài công cụ → lấy mã nguồn → chạy hệ thống được kiểm thử (SUT) → chạy kiểm thử tự động → chạy kiểm thử hiệu năng → chạy toàn bộ quy trình trên Jenkins.

Hệ thống gồm 4 phần:

| Phần | Vai trò | Nằm ở đâu |
|---|---|---|
| SUT — ứng dụng DAFL | Ứng dụng thương mại điện tử được kiểm thử (Frontend + Backend + PostgreSQL + Kafka), chạy bằng Docker | Repo `sonismyname/DAFL` (nhánh `develop`) |
| Automation | Bộ kiểm thử tự động giao diện bằng Java + Selenium + TestNG | Thư mục `automation/` trong repo đồ án |
| Performance | Kịch bản kiểm thử hiệu năng bằng Apache JMeter, viết theo Swagger của Backend | Thư mục `performance/` trong repo đồ án |
| Jenkins + Quality Gate | Điều phối quy trình kiểm thử liên tục và ra quyết định PASS/FAIL | 3 file `Jenkinsfile*` + thư mục `quality-gate/` trong repo đồ án |

Địa chỉ các thành phần sau khi chạy: Frontend `http://localhost:3000`, Backend `http://localhost:5000` (trang Swagger: `http://localhost:5000/swagger`), Jenkins `http://localhost:8080`.

---

## PHẦN A — CÀI ĐẶT CÔNG CỤ TRÊN MÁY TÍNH

### A.1. Yêu cầu cấu hình máy

- Windows 10 hoặc 11, 64-bit.
- RAM tối thiểu 8 GB (khuyến nghị 16 GB vì Docker chạy đồng thời nhiều container).
- Ổ cứng trống tối thiểu 15 GB cho Docker image và thư viện.
- CPU đã bật ảo hóa (Virtualization). Kiểm tra: mở **Task Manager → Performance → CPU**, dòng *Virtualization* phải hiển thị **Enabled**. Nếu là *Disabled*, cần khởi động lại máy vào BIOS/UEFI để bật (mục *Intel VT-x* hoặc *SVM Mode*).
- Có kết nối Internet trong quá trình cài đặt và lần chạy đầu tiên (để tải image Docker và thư viện Maven).

### A.2. Cài đặt Docker Desktop

Docker Desktop là thành phần bắt buộc: toàn bộ SUT và Jenkins đều chạy trong Docker.

1. Truy cập `https://www.docker.com/products/docker-desktop/` và tải bản **Docker Desktop for Windows**.
2. Chạy file `Docker Desktop Installer.exe`. Trong màn hình *Configuration*, giữ nguyên tùy chọn **Use WSL 2 instead of Hyper-V** được chọn, sau đó bấm **Ok** và chờ cài đặt hoàn tất.
3. Nếu trình cài đặt yêu cầu khởi động lại máy, hãy khởi động lại.
4. Sau khi máy lên, mở **Docker Desktop** từ Start Menu. Chờ đến khi biểu tượng cá voi ở góc dưới bên trái chuyển sang trạng thái **Engine running** (màu xanh). Lần đầu khởi động có thể mất 1–3 phút.
5. Kiểm chứng bằng PowerShell (mở Start Menu, gõ `powershell`):

```powershell
docker --version
docker compose version
docker run hello-world
```

Lệnh cuối phải in ra dòng `Hello from Docker!` — điều đó chứng tỏ Docker đã cài đúng và chạy được container.

**Xử lý lỗi thường gặp:** nếu Docker Desktop báo lỗi liên quan WSL, mở PowerShell bằng quyền Admin (chuột phải → *Run as administrator*) và chạy lần lượt hai lệnh sau, sau đó khởi động lại máy:

```powershell
wsl --install
wsl --update
```

### A.3. Cài đặt Git

1. Truy cập `https://git-scm.com/download/win`, tải và chạy trình cài đặt.
2. Giữ nguyên các tùy chọn mặc định trong suốt quá trình cài (chỉ cần bấm **Next** liên tục), cuối cùng bấm **Install**.
3. Kiểm chứng: mở cửa sổ PowerShell **mới** và chạy `git --version`. Kết quả dạng `git version 2.x.x` là đạt.

### A.4. Cài đặt JDK 17 và thiết lập JAVA_HOME

Bộ automation yêu cầu **JDK 17** (bắt buộc là JDK, không phải JRE, vì cần cả trình biên dịch `javac`).

1. Truy cập `https://adoptium.net/temurin/releases/?version=17`, chọn đúng bản **Windows x64, file cài đặt `.msi`**, tải về và chạy.
2. Trong trình cài đặt, đến màn hình *Custom Setup* cần chú ý mở rộng mục **Set JAVA_HOME variable** và **Add to PATH**, chọn **Will be installed on local hard drive** cho cả hai mục này. Đây là bước quan trọng nhất — bỏ qua bước này là nguyên nhân phổ biến của lỗi `The JAVA_HOME environment variable is not defined correctly` khi chạy Maven sau này.
3. Cài xong, **đóng tất cả cửa sổ dòng lệnh đang mở**, mở cửa sổ PowerShell **mới** và kiểm chứng:

```powershell
java -version
javac -version
$env:JAVA_HOME
```

Kết quả đúng phải là `openjdk version "17.x.x"` ở hai lệnh đầu, và lệnh thứ ba in ra đường dẫn dạng `C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot`.

4. **Trường hợp `$env:JAVA_HOME` không in ra gì** (đặt biến bằng tay): mở **Settings → System → About → Advanced system settings → Environment Variables**. Ở khung *System variables*, bấm **New**: *Variable name* nhập `JAVA_HOME`, *Variable value* nhập đường dẫn thư mục JDK ở trên. Tiếp theo chọn biến **Path** → **Edit** → **New** → nhập `%JAVA_HOME%\bin`. Bấm OK ở tất cả cửa sổ, mở lại PowerShell và kiểm chứng lại như bước 3.

### A.5. Cài đặt IntelliJ IDEA (dùng để chạy và xem kiểm thử)

1. Truy cập `https://www.jetbrains.com/idea/download/`, tải bản **Community Edition** (miễn phí) cho Windows và cài đặt với tùy chọn mặc định.
2. Mở IntelliJ lần đầu, có thể bỏ qua các màn hình giới thiệu (chọn *Skip* / *Do not import settings*).

---

## PHẦN B — LẤY MÃ NGUỒN VỀ MÁY

### B.1. Tạo thư mục làm việc và clone hai repo

1. Tạo một thư mục làm việc, ví dụ `D:\DoAnTotNghiep`, mở PowerShell tại thư mục đó (trong File Explorer, bấm vào thanh địa chỉ, gõ `powershell`, Enter).
2. Clone mã nguồn của hệ thống được kiểm thử (SUT):

```powershell
git clone -b develop https://github.com/sonismyname/DAFL.git
```

3. Clone repo của đồ án (chứa automation, performance, quality-gate và các Jenkinsfile):

```powershell
git clone https://github.com/xuanlepham/DAFL_DO_AN_TOT_NGHIEP.git
```

4. Sau khi clone xong, cấu trúc thư mục làm việc gồm:

```text
D:\DoAnTotNghiep\
├── DAFL\                        ← SUT (Frontend, Backend, docker-compose.yml)
└── DAFL_DO_AN_TOT_NGHIEP\       ← Repo đồ án
    ├── automation\              ← Bộ kiểm thử Selenium (project Maven)
    ├── performance\             ← Kịch bản JMeter: dafl-loadtest.jmx
    ├── quality-gate\            ← Script Quality Gate (.sh cho Jenkins, .py cho Windows)
    ├── ci\jenkins\quick-setup\  ← Bộ dựng Jenkins bằng Docker
    ├── Jenkinsfile              ← Pipeline tổng hợp
    ├── Jenkinsfile.auto         ← Pipeline riêng cho automation
    └── Jenkinsfile.pt           ← Pipeline riêng cho performance
```

---

## PHẦN C — CHẠY HỆ THỐNG ĐƯỢC KIỂM THỬ (SUT) BẰNG DOCKER

### C.1. Khởi động toàn bộ SUT

1. Mở Docker Desktop và chờ **Engine running**.
2. Mở PowerShell, di chuyển vào thư mục SUT và chạy lệnh dựng hệ thống:

```powershell
cd D:\DoAnTotNghiep\DAFL
docker compose up -d --build
```

Giải thích lệnh: `docker compose` đọc file `docker-compose.yml` trong thư mục; `--build` yêu cầu build image cho Backend (từ `src/Backend/Dockerfile`, biên dịch bằng .NET SDK 8 rồi chạy trên ASP.NET 8 runtime) và Frontend (từ `src/Frontend/Dockerfile`, build bằng Node 20 rồi phục vụ bằng Nginx); `-d` cho các container chạy nền. Các image có sẵn trên Internet (PostgreSQL 16, Apache Kafka 3.7) được Docker tự tải về. **Lần đầu tiên chạy lệnh này mất khoảng 5–15 phút** tùy tốc độ mạng; các lần sau chỉ mất dưới 1 phút vì image đã có sẵn trên máy.

3. Theo dõi trạng thái các container:

```powershell
docker compose ps
```

Cần thấy đủ các dịch vụ ở trạng thái `Up` (và `healthy` với những dịch vụ có kiểm tra sức khỏe): `dafl-frontend`, `dafl-backend`, `dafl-postgres`, `dafl-kafka`. Nếu một dịch vụ chưa lên, xem log bằng lệnh `docker compose logs backend` (thay `backend` bằng tên dịch vụ cần xem).

### C.2. Kiểm chứng SUT hoạt động

1. Mở trình duyệt vào `http://localhost:3000` — phải thấy trang chủ cửa hàng DAFL với danh sách sản phẩm.
2. Mở `http://localhost:5000/health` — phải nhận được JSON có nội dung `"status": "Healthy"`.
3. Mở `http://localhost:5000/swagger` — đây là tài liệu API của Backend; toàn bộ kịch bản JMeter ở Phần E được viết dựa trên danh sách endpoint tại trang này.
4. Thử đăng nhập vào Frontend bằng tài khoản demo được khởi tạo sẵn trong cơ sở dữ liệu: email `demo@dafl.vn`, mật khẩu `Password123!` (hoặc bấm nút dùng tài khoản demo ngay trong hộp thoại đăng nhập). Thử thêm một sản phẩm vào giỏ hàng để chắc chắn luồng mua hàng cơ bản hoạt động.

### C.3. Dừng SUT

```powershell
docker compose stop     # dung cac container, giu nguyen du lieu
docker compose down     # dung va xoa container (du lieu DB van giu trong volume)
```

Lưu ý: lệnh `docker compose down -v` sẽ xóa cả volume chứa dữ liệu PostgreSQL — chỉ dùng khi muốn hệ thống trở về trạng thái sạch hoàn toàn (tài khoản demo và dữ liệu mẫu sẽ được khởi tạo lại ở lần `up` tiếp theo). Về các lần mở máy sau: nhờ chính sách `restart: unless-stopped` đã khai báo trong file compose, nếu các container **đang chạy** khi bạn tắt máy hoặc đóng Docker Desktop thì lần sau chỉ cần mở Docker Desktop là chúng tự chạy lại. Ngược lại, nếu bạn đã chủ động dừng bằng `docker compose stop` hoặc `docker compose down` thì cần tự bật lại bằng lệnh `docker compose start` (sau `stop`) hoặc `docker compose up -d` (sau `down`) — và riêng lần đầu tiên sau khi clone code thì bắt buộc chạy lệnh ở mục C.1.

---

## PHẦN D — CHẠY KIỂM THỬ TỰ ĐỘNG (AUTOMATION)

Điều kiện trước khi chạy: SUT đang chạy (Phần C) và trang `http://localhost:3000` mở được. Có ba cách chạy; cả ba cho cùng một kết quả.

### D.1. Cách 1 — Chạy bằng IntelliJ IDEA (trực quan, phù hợp để trình diễn)

1. Mở IntelliJ IDEA, chọn **File → Open**, trỏ tới thư mục `D:\DoAnTotNghiep\DAFL_DO_AN_TOT_NGHIEP\automation` và bấm **OK**. Đây là thư mục chứa file `pom.xml` nên IntelliJ sẽ nhận diện đây là một project Maven.
2. Chờ IntelliJ tải thư viện: góc dưới bên phải xuất hiện tiến trình *Resolving Maven dependencies* — lần đầu mất vài phút. Nếu IntelliJ hỏi về JDK, vào **File → Project Structure → Project**, mục *SDK* chọn phiên bản **17** (nếu danh sách trống, chọn *Add SDK → Download JDK → Version 17 → Eclipse Temurin*).
3. Chạy một lớp kiểm thử: mở thư mục `src/test/java/vn/dafl/tests/web` (test web bán hàng) hoặc `src/test/java/vn/dafl/tests/admin` (test admin), mở file `EndToEndShoppingFlowTest.java`, bấm nút **Run ▶** màu xanh ở lề trái cạnh tên lớp. Một cửa sổ Chrome sẽ tự mở và thực hiện toàn bộ luồng mua hàng: đăng nhập tài khoản demo → chọn hai sản phẩm → thêm vào giỏ → áp mã giảm giá DAFL20 → điền form thanh toán → thanh toán qua cổng PayOS giả lập → kiểm tra đơn hàng trong lịch sử.
4. Chạy toàn bộ bộ kiểm thử: trong cây thư mục bên trái, chuột phải vào file **`testng.xml`** ở thư mục gốc của project `automation` → **Run 'testng.xml'**. Muốn chỉ chạy phần web bán hàng hoặc chỉ chạy phần admin, dùng lần lượt file `testng-web.xml` (107 test) và `testng-admin.xml` (35 test — chỉ cần service admin `http://localhost:3001` đang chạy, không cần `:3000`). Muốn chỉ chạy luồng E2E hoặc chỉ chạy các trường hợp âm, dùng lần lượt file `testng-flow.xml` và `testng-negative.xml`.
5. Đọc kết quả ở panel **Run** phía dưới IntelliJ: cây test hiển thị màu xanh (pass) / đỏ (fail) kèm thời gian chạy. Khi có test fail, mở rộng node đỏ để xem thông điệp lỗi và vị trí dòng code.

### D.2. Cách 2 — Chạy bằng dòng lệnh (PowerShell)

Project đã kèm sẵn **Maven Wrapper** (`mvnw.cmd`) nên không cần cài Maven riêng; lần chạy đầu tiên Wrapper sẽ tự tải đúng phiên bản Maven 3.9.16 về máy.

```powershell
cd D:\DoAnTotNghiep\DAFL_DO_AN_TOT_NGHIEP\automation

# Chay toan bo bo kiem thu (file testng.xml), Chrome co giao dien
.\mvnw.cmd test

# Chay khong giao dien (headless) - nhanh hon, dung trong CI
.\mvnw.cmd test -Dheadless=true

# Chi chay test web ban hang (107 test)
.\mvnw.cmd test -DsuiteXmlFile=testng-web.xml -Dheadless=false

# Chi chay test admin (35 test) - chi can service admin :3001 dang chay
.\mvnw.cmd test -DsuiteXmlFile=testng-admin.xml -Dheadless=false

# Chi chay nhom truong hop am / ngoai le
.\mvnw.cmd test -DsuiteXmlFile=testng-negative.xml -Dheadless=true

# Chi chay mot lop kiem thu cu the
.\mvnw.cmd test "-Dtest=vn.dafl.tests.web.AuthTest" "-DsuiteXmlFile="
```

Khi chạy xong, dòng tổng kết trên console có dạng `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`. Báo cáo chi tiết dạng XML/HTML nằm trong thư mục `target\surefire-reports`. Để xem báo cáo Allure dạng web, chạy lệnh `.\mvnw.cmd allure:serve` (lệnh này tự mở trình duyệt hiển thị báo cáo; nhấn `Ctrl+C` để dừng).

### D.3. Cách 3 — Chạy bằng file .bat có sẵn

Trong thư mục `automation` có năm file phục vụ việc chạy nhanh:

- `run_test_with_gui.bat` — chạy toàn bộ bộ kiểm thử với Chrome có giao diện.
- `run_test_headless.bat` — chạy toàn bộ bộ kiểm thử ở chế độ headless.
- `run_test_web.bat` — chỉ chạy test web bán hàng (`testng-web.xml`, 107 test).
- `run_test_admin.bat` — chỉ chạy test admin (`testng-admin.xml`, 35 test); chỉ cần service `:3001` đang chạy và tài khoản `admin@dafl.vn` đã được seed (nếu chưa sẵn, các test admin tự bỏ qua thay vì fail).
- `run_test_web_allure.bat` — chỉ chạy test web bán hàng rồi tạo báo cáo Allure HTML: kiểm tra SUT web `:3000` trả HTTP 200 → gọi `run_test_web.bat` chạy 107 test web → chạy `mvnw.cmd allure:report` để sinh báo cáo từ `target/allure-results` → mở `target/site/allure-maven-plugin/index.html`. Báo cáo HTML thật chỉ có sau khi chạy test trên máy có SUT đang chạy.

Nhấp đúp vào file để chạy. Nội dung file tự thực hiện các bước: kiểm tra và xác định `JAVA_HOME` trỏ đúng JDK, gọi Maven Wrapper kèm các tham số tương đương Cách 2. Cách này tiện khi cần kiểm tra nhanh mà không mở IntelliJ.

### D.4. Phạm vi bộ kiểm thử

Tổng cộng **142 test case** trong 17 lớp, chia 2 thư mục riêng: `src/test/java/vn/dafl/tests/web/` (13 lớp web bán hàng, 107 test) và `src/test/java/vn/dafl/tests/admin/` (4 lớp admin, 35 test — chạy trên app Admin riêng `localhost:3001`, không cần `:3000`): `AuthTest` (19 — đăng nhập JWT, đăng ký không OTP, lỗi từng trường, checklist mật khẩu, token hết hạn, đăng nhập admin bằng tên `admin` ở localhost:3001, không còn nút Google/Facebook, link 'Quên mật khẩu' ở form đăng nhập shop — đợt 13, FP-01), `CatalogAndSearchTest` (12 — hiển thị, tìm kiếm, lọc danh mục/giá, sắp xếp, phân trang 12/trang, từ khóa SQL injection, nút `Áp dụng` lọc giá, khoảng giá có sẵn bấm 1 chạm), `ProductDetailTest` (9 — chi tiết sản phẩm, số lượng biên theo tồn kho, mua ngay, hết hàng, 404), `CartManagementTest` (9 — giỏ khách không cần đăng nhập, gộp giỏ khi đăng nhập, xác nhận xóa, kẹp số lượng theo tồn kho, voucher), `CheckoutAndOrderTest` (10 — đặt COD, PayOS giả lập, khách bị đưa qua đăng nhập rồi quay lại, địa chỉ đã lưu, chống đặt trùng, phí ship 25.000đ/miễn phí từ 299.000đ, PayOS không còn tab nhúng/iframe), `OrderCancellationTest` (3 — hủy đơn ở Chờ xác nhận, hoàn tồn kho, lọc theo trạng thái), `ProfileAndAddressTest` (9 — cập nhật hồ sơ, trùng SĐT, đổi mật khẩu, sổ địa chỉ tối đa 5 và mặc định), `HomeHeroTest` (10 — banner hero + Flash Sale + modal thông báo + dark mode), `NotFoundPageTest` (2 — trang 404), `AdminAndNewsletterTest` (14 — trang Admin chỉ đọc + biểu đồ doanh thu + tìm kiếm/lọc/Export; đợt 12: o search topbar đã bỏ, toolbar chuẩn + phân trang 10 dòng/trang + form mở trong modal; 3 test bản tin NL-01..03 đã chuyển sang `NewsletterTest`), `AdminDetailTest` (9 — modal xem chi tiết admin Spark, chỉ đọc), `AdminCrudTest` (8 — CRUD thật trên Admin: tạo/sửa/xóa sản phẩm + danh mục, tạo/kích hoạt/tắt voucher, đổi trạng thái + hủy 1 đơn 'Chờ xử lý' có sẵn (không khôi phục được trạng thái cũ), điều chỉnh điểm cho demo — đợt 13: ACR-05 thêm sửa/xóa voucher Draft qua hộp xác nhận, ACR-08 đọc lại số dư điểm trước/sau và khẳng định tăng đúng +5; chỉ đụng dữ liệu do test tạo và tự dọn sau khi chạy — trừ ACR-06/07), `NewsletterTest` (3 — đăng ký bản tin ở footer web bán hàng), `MembershipTest` (3 — đợt 13: trang Hội viên riêng `/membership` MEM-01/02 + 15 link footer FTR-01), `AdminWave13Test` (4 — đợt 13: đăng nhập admin không nút quay lại ADM-W13-01, link 'Quên mật khẩu' ở admin ADM-W13-02, nút 'Ghi nhận đã hoàn tiền' ADM-W13-03, lọc ngày + Export nhật ký ADM-W13-04), `EndToEndShoppingFlowTest` (1 — luồng mua hàng đầu-cuối của khách), `NegativeAndExceptionScenariosTest` (17 — các trường hợp âm: sai mật khẩu, mật khẩu không đạt policy nghiêm ngặt, voucher bị Backend từ chối khi đặt hàng, lỗi khối giao hàng shipping-*...). Bảng ánh xạ đầy đủ TC-CN ↔ lớp/phương thức kiểm thử nằm trong `TEST-MAPPING.md` (mục "Tính năng mới đợt 13" liệt kê riêng các test mới); đặc tả nghiệp vụ chi tiết nằm trong tài liệu đặc tả CN-01..CN-08 của đồ án.

---

## PHẦN E — CHẠY KIỂM THỬ HIỆU NĂNG BẰNG JMETER

Kịch bản `performance/dafl-loadtest.jmx` chứa 25 sampler bám theo Swagger của Backend (Phần C.2, mục 3), chia 2 luồng: luồng chính (duyệt sản phẩm → đăng nhập → giỏ hàng → áp voucher → checkout → trạng thái thanh toán → lịch sử đơn) và luồng phụ trợ (đăng ký, cập nhật hồ sơ, đánh giá sản phẩm, thông báo). Các sampler xâu chuỗi dữ liệu thật bằng JSON Extractor (lấy JWT `token` + `userId` sau đăng nhập — token được gắn vào header `Authorization: Bearer` cho mọi sampler cần xác thực theo bản CN-02, `productId` từ danh sách sản phẩm, `orderCode` sau checkout). Thông số đích được truyền qua biến: `target_host`, `target_port`, `threads` (số người dùng ảo, mặc định 10), `rampup`, `duration` (thời lượng, mặc định 60 giây).

### E.1. Cách 1 — Chạy JMeter bằng Docker (không cần cài JMeter)

```powershell
cd D:\DoAnTotNghiep\DAFL_DO_AN_TOT_NGHIEP\performance
mkdir results -Force

docker run --rm -v "${PWD}:/jmeter" -w /jmeter justb4/jmeter:5.5 `
  -n -t dafl-loadtest.jmx `
  -Jtarget_host=host.docker.internal -Jtarget_port=5000 `
  -Jthreads=10 -Jduration=60 `
  -l results/result.jtl -e -o results/dashboard
```

Lưu ý quan trọng về địa chỉ đích: khi JMeter chạy **trong container Docker trên Windows**, giá trị `localhost` sẽ trỏ vào chính container JMeter chứ không phải máy thật, vì vậy phải dùng `host.docker.internal` để gọi tới Backend đang chạy ở cổng 5000 của máy thật. (Trong Jenkins, JMeter chạy trên cùng mạng Docker với Backend nên địa chỉ đích được truyền là `dafl-backend:8080`.)

### E.2. Cách 2 — Chạy bằng JMeter cài trên máy (để xem và chỉnh sửa kịch bản)

1. Tải Apache JMeter (file `.zip` bản binary) tại `https://jmeter.apache.org/download_jmeter.cgi`, giải nén ra thư mục, ví dụ `D:\apache-jmeter-5.6.3`. JMeter chạy trên nền Java nên máy cần có JDK như Phần A.4.
2. Chạy file `bin\jmeter.bat` để mở giao diện, chọn **File → Open**, mở file `dafl-loadtest.jmx`.
3. Trong node *User Defined Variables* ở đầu cây kịch bản, đặt `target_host` là `localhost` và `target_port` là `5000` (vì lần này JMeter chạy trực tiếp trên máy thật).
4. Bấm **Run ▶** để xem từng sampler trả về kết quả tại node *View Results Tree*. Giao diện đồ họa chỉ nên dùng để xem/chỉnh sửa kịch bản và chạy thử tải nhẹ; khi đo hiệu năng chính thức cần chạy ở chế độ dòng lệnh như Cách 1 hoặc qua Jenkins để số liệu không bị ảnh hưởng bởi giao diện.

### E.3. Đánh giá kết quả bằng Quality Gate (chạy thủ công)

Sau khi có file `results/result.jtl`, áp dụng cổng chất lượng ngay tại thư mục `performance`:

```powershell
python ..\quality-gate\check_performance_gate.py results\result.jtl 2.0 1000
```

Hai ngưỡng mặc định của đồ án: **tỉ lệ lỗi tối đa 2%** và **thời gian phản hồi trung bình tối đa 1000 ms**. Script in ra các chỉ số đo được (error rate, average, p95, throughput) và kết luận `QUALITY GATE: PASS` hoặc `QUALITY GATE: FAIL` kèm lý do. Báo cáo HTML chi tiết của JMeter nằm trong thư mục `results/dashboard` (mở file `index.html`).

---

## PHẦN F — DỰNG JENKINS BẰNG DOCKER VÀ CHẠY PIPELINE

### F.1. Khởi động Jenkins

Bộ dựng Jenkins nằm trong thư mục `ci/jenkins/quick-setup` của repo đồ án, gồm một `Dockerfile` (image Jenkins gốc cộng thêm Docker CLI, các plugin phục vụ pipeline, Allure commandline) và một file compose (gắn thư mục dữ liệu, gắn Docker socket của máy thật để Jenkins điều khiển được Docker, và nạp file cấu hình `jenkins.yaml` theo cơ chế Configuration-as-Code).

```powershell
cd D:\DoAnTotNghiep\DAFL_DO_AN_TOT_NGHIEP\ci\jenkins\quick-setup
docker compose -f docker-compose.jenkins.yml up -d --build
```

Lần đầu chạy mất vài phút để build image. Kiểm tra container đã lên bằng `docker ps` (container tên `dafl-jenkins`, cổng `8080`).

### F.2. Đăng nhập và kiểm tra các job có sẵn

1. Mở trình duyệt vào `http://localhost:8080`, đăng nhập bằng tài khoản quản trị được khai báo trong file `jenkins.yaml`: **admin / adminpassword123**. Ngay sau lần đăng nhập đầu tiên, đổi mật khẩu tại **Manage Jenkins → Users → admin → Security**.
2. Tại trang chủ Jenkins phải thấy sẵn 3 job do cơ chế Configuration-as-Code tự tạo: **DAFL-Pipeline** (pipeline tổng hợp), **DAFL-Automation** (chỉ kiểm thử chức năng), **DAFL-Performance** (chỉ kiểm thử hiệu năng). Nếu chưa thấy job, vào **Manage Jenkins → Configuration as Code → Reload existing configuration**, hoặc khởi động lại container bằng lệnh `docker restart dafl-jenkins` rồi chờ khoảng 1 phút.

### F.3. Chạy thử pipeline automation

1. Mở job **DAFL-Automation**, bấm **Build Now**.
2. Trong *Build History*, bấm vào số build vừa tạo → **Console Output** để xem log trực tiếp. Pipeline tự thực hiện: lấy code mới nhất từ repo đồ án → clone SUT và dựng bằng Docker → chờ healthcheck xác nhận Frontend/Backend sẵn sàng → chạy toàn bộ kiểm thử Selenium trong container → đánh giá **Functional Quality Gate** (có bất kỳ test nào fail là pipeline fail).
3. Sau khi build xong, xem kết quả tại các mục **Test Result** (danh sách test pass/fail của JUnit), **Allure Report** (báo cáo trực quan, có ảnh chụp màn hình ở những test fail), và **Stage View** trên trang job để thấy trạng thái từng stage.

### F.4. Chạy thử pipeline performance và pipeline tổng hợp

- Job **DAFL-Performance**: thực hiện dựng SUT → chạy JMeter (container `justb4/jmeter:5.5`, đích `dafl-backend:8080` trên mạng Docker nội bộ) → **Performance Quality Gate** đọc file `result.jtl` và đối chiếu hai ngưỡng 2% / 1000 ms. Báo cáo hiệu năng dạng HTML nằm ở mục **JMeter Performance Dashboard** của build.
- Job **DAFL-Pipeline**: quy trình đầy đủ nhất gồm 8 stage — Checkout → Build & Prepare Test Environment → Functional Regression → Functional Quality Gate → Performance Test → Performance Quality Gate → Deploy Production (chỉ chạy khi cả hai Quality Gate đều PASS) → Verify Status. Đây là job dùng để trình diễn toàn bộ quy trình Continuous Testing.

### F.5. Vòng lặp làm việc hằng ngày

Sau khi hệ thống đã dựng xong, chu trình làm việc chỉ gồm ba thao tác: **sửa code** (automation, file .jmx hoặc Jenkinsfile) → **commit và push lên repo đồ án, nhánh `main`** → **vào Jenkins bấm Build Now**. Các job thuộc dạng *Pipeline from SCM* nên mỗi lần build Jenkins tự lấy phiên bản code và Jenkinsfile mới nhất từ repo; không cần cấu hình lại job hay build lại image cho phần kiểm thử.

---

## PHẦN G — BẢNG XỬ LÝ SỰ CỐ THƯỜNG GẶP

| Hiện tượng | Nguyên nhân thường gặp | Cách xử lý |
|---|---|---|
| Docker Desktop báo lỗi WSL / không khởi động được | WSL chưa cài hoặc chưa cập nhật | Mở PowerShell (Admin), chạy `wsl --install` rồi `wsl --update`, khởi động lại máy (mục A.2) |
| `docker compose up` báo cổng đã bị chiếm (3000/5000/8080) | Có ứng dụng khác đang dùng cổng | Tìm tiến trình bằng `netstat -ano \| findstr :3000` và tắt ứng dụng đó; hoặc đổi cổng cho SUT bằng cách tạo file `.env` cạnh `docker-compose.yml` với nội dung `FRONTEND_PORT=3001` / `BACKEND_PORT=5001` |
| Mở `localhost:3000` không thấy trang | SUT chưa chạy hoặc Frontend chưa build xong | Kiểm tra `docker compose ps` trong thư mục DAFL; xem `docker compose logs frontend`; chờ thêm 1–2 phút ở lần đầu |
| Chạy Maven báo `The JAVA_HOME environment variable is not defined correctly` | JAVA_HOME trống, hoặc trỏ vào JRE / thư mục JDK đã xóa | Làm lại mục A.4: kiểm tra `$env:JAVA_HOME` phải trỏ tới thư mục JDK có chứa `bin\javac.exe`; nhớ mở cửa sổ dòng lệnh mới sau khi đặt biến |
| IntelliJ báo đỏ toàn bộ import Selenium/TestNG | Chưa import xong project Maven hoặc sai SDK | Chuột phải vào `pom.xml` → *Add as Maven Project* (hoặc bấm nút Reload trong cửa sổ Maven); kiểm tra Project SDK là 17 (mục D.1) |
| Test Selenium fail hàng loạt ngay từ bước đầu | SUT chưa sẵn sàng hoặc URL đích sai | Mở thử `localhost:3000` bằng trình duyệt trước; đảm bảo tham số `-Dapp.url` (nếu có dùng) đúng địa chỉ SUT |
| JMeter chạy bằng Docker ra toàn lỗi kết nối | Đang để `target_host` là `localhost` trong container | Đổi thành `host.docker.internal` như mục E.1 |
| Quality Gate báo FAIL dù test “có vẻ” chạy được | Một trong hai ngưỡng bị vượt: error rate > 2% hoặc avg response time > 1000 ms | Đọc dòng lý do script in ra; xem chi tiết tại `results/dashboard/index.html` để biết endpoint nào chậm/lỗi |
| Jenkins không có sẵn 3 job | Cấu hình JCasC chưa được nạp | Reload tại **Manage Jenkins → Configuration as Code**, hoặc `docker restart dafl-jenkins` (mục F.2) |
| Job Jenkins fail ở Stage 1 (Checkout) | URL repo hoặc nhánh trong job không đúng | Job phải trỏ tới repo `DAFL_DO_AN_TOT_NGHIEP`, nhánh `main` (khai trong `ci/jenkins/quick-setup/jenkins.yaml`) |
| Pipeline Jenkins chạy Selenium/JMeter rất chậm ở lần đầu | Docker đang tải các image chạy test (`markhobson/maven-chrome`, `justb4/jmeter`) | Hiện tượng chỉ xảy ra ở build đầu tiên; các build sau dùng image đã có trên máy |

## PHẦN H — DỌN DẸP SAU KHI LÀM XONG

- Dừng SUT: trong thư mục `DAFL`, chạy `docker compose down`.
- Dừng Jenkins: trong thư mục `ci/jenkins/quick-setup`, chạy `docker compose -f docker-compose.jenkins.yml down`.
- Dữ liệu của Jenkins (job, lịch sử build) nằm trong volume `dafl_jenkins_data` và dữ liệu của SUT nằm trong volume của PostgreSQL — các volume này vẫn giữ nguyên qua các lần dừng/mở Docker Desktop, chỉ mất khi chủ động xóa bằng lệnh `docker volume rm` hoặc `docker compose down -v`.
