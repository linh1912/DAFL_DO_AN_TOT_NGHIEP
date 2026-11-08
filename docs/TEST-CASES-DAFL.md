# DANH SÁCH TEST CASE — DAFL (FE + BE)

> Tổng: 30 test case trong bảng đặc tả dưới đây (tài liệu đặc tả gốc).
> LƯU Ý CẬP NHẬT (bản app cuối theo đặc tả CN-01..CN-08 + đợt hoàn thiện giao diện 2–4): bộ kiểm thử tự động Selenium hiện có **130 test method trong 15 lớp** (102 web + 28 admin) — bảng ánh xạ chính thức TC-CN ↔ lớp/phương thức ↔ data-testid nằm ở file `TEST-MAPPING.md` ở thư mục gốc repo. Đợt 10: 3 test bản tin NL-01..03 chuyển từ admin sang lớp web `NewsletterTest`; ACR-06/07 đổi trạng thái + hủy 1 đơn 'Chờ xử lý' có sẵn thay vì đặt đơn mới qua web bán hàng; test admin chỉ chạy trên app Admin riêng `localhost:3001`, không mở `:3000` nữa. So với bản 101 test (đợt v8): thêm AdminDetailTest (9: modal xem chi tiết admin Spark), AdminAndNewsletterTest +5 (tìm kiếm/lọc/Export/topbar search), AuthTest +2 (đăng nhập admin bằng tên `admin` ở localhost:3001, không còn nút Google/Facebook), HomeHeroTest +3 (modal thông báo, dark mode), CheckoutAndOrderTest +1 (PayOS không còn tab nhúng/iframe), CatalogAndSearchTest +1 (nút `Áp dụng` lọc giá). Trước đó (bản 83 test): thêm HomeHeroTest (7: banner hero 5 + Flash Sale 2), NotFoundPageTest (2: trang 404), AdminAndNewsletterTest (9: admin chỉ đọc 6 + newsletter 3), ProductDetailTest +2 (đánh giá sao), CheckoutAndOrderTest +2 (nút Đặt hàng sticky, tab đơn có số lượng); sửa locator `filterByStatus` (tab thay cho `<select>`) và 2 test còn dùng tên sản phẩm seed cũ "Khăn ướt" (đổi sang "Mặt nạ giấy"). Bảng dưới đây giữ lại làm dấu vết đặc tả vòng đời trước (một số trường hợp như nút điền sẵn tài khoản demo, đăng ký được đăng nhập ngay, lỗi tại auth-error-msg... đã thay đổi hành vi ở bản CN). Cột 'Tự động hóa' cho biết test đã được viết thành Selenium/JMeter (mã TC_UI tương ứng trong automation), đang làm thủ công, hay nằm trong kế hoạch tự động hóa.
> Dữ liệu kiểm thử lấy từ dữ liệu seed thật của Backend (DbInitializer.cs): tài khoản demo demo@dafl.vn, voucher DAFL10 (10%%, trần 100.000đ), DAFL20 (cố định 20.000đ), 17+ danh mục sản phẩm.

| Mã TC | Module | Tên test case | Loại | Ưu tiên | Kết quả mong đợi (tóm tắt) | Tự động hóa |
|---|---|---|---|---|---|---|
| TC_AUTH_01 | Xác thực | Mở Auth Modal từ menu tài khoản | Positive | High | Auth Modal hiển thị, có đủ ô email + mật khẩu ở tab Đăng nhập | Đã auto (TC_UI_05) |
| TC_AUTH_02 | Xác thực | Đăng nhập thành công bằng tài khoản demo (nút điền sẵn) | Positive | Blocker | Modal đóng; mở lại menu tài khoản thấy nút Đăng xuất; toast 'Đăng nhập thành công' | Đã auto (TC_UI_07) |
| TC_AUTH_03 | Xác thực | Đăng nhập thành công nhập tay đúng email/mật khẩu | Positive | High | Đăng nhập thành công; thông tin họ tên/email được điền vào form checkout | Đã auto (TC_UI_15) |
| TC_AUTH_04 | Xác thực | Đăng nhập sai mật khẩu | Negative | Blocker | Hiện thông báo lỗi tại auth-error-msg (nội dung khác rỗng); modal vẫn mở; không đăng nhập được | Đã auto (TC_UI_08) |
| TC_AUTH_05 | Xác thực | Email demo đúng nhưng mật khẩu sai bị từ chối | Negative | High | BE trả lỗi, FE hiện auth-error-msg; không dùng fallback để đăng nhập | Đã auto (TC_UI_09) |
| TC_AUTH_06 | Xác thực | Đăng ký tài khoản mới hợp lệ | Positive | High | Tạo tài khoản thành công và được đăng nhập; không báo lỗi | Đã auto (TC_UI_10) |
| TC_AUTH_07 | Xác thực | Đăng ký với mật khẩu không đạt chính sách | Negative | High | Báo lỗi đúng từng quy tắc: 'Mật khẩu phải có tối thiểu 8 ký tự.' / '...ít nhất 1 ký tự chữ hoa (A-Z).' / '...í | Đã auto (TC_UI_11, TC_UI_12, TC_UI_13) |
| TC_AUTH_08 | Xác thực | Đăng ký với mật khẩu xác nhận không khớp | Negative | Medium | Báo lỗi 'Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại.'; không tạo tài khoản | Đã auto (TC_UI_14) |
| TC_AUTH_09 | Xác thực | Đăng xuất | Positive | Medium | Trở về trạng thái chưa đăng nhập; menu hiện nút Đăng nhập | Đã auto (TC_UI_16) |
| TC_HOME_01 | Trang chủ & Tìm kiếm | Trang chủ tải và hiển thị thương hiệu | Positive | Blocker | Logo/thương hiệu chứa 'DAFL' hiển thị (brand-logo); trang tải không lỗi | Đã auto (TC_UI_01) |
| TC_HOME_02 | Trang chủ & Tìm kiếm | Ô tìm kiếm nhập được dữ liệu và nút đổi theme hiển thị | Positive | Medium | Ô tìm kiếm giữ giá trị 'Áo'; nút đổi giao diện sáng/tối hiển thị | Đã auto (TC_UI_02) |
| TC_HOME_03 | Trang chủ & Tìm kiếm | Tìm kiếm sản phẩm theo từ khóa có kết quả | Positive | High | Chỉ hiển thị sản phẩm liên quan; số thẻ sản phẩm > 0 | Một phần (trong TC_UI_06) |
| TC_HOME_04 | Trang chủ & Tìm kiếm | Tìm kiếm từ khóa không tồn tại | Negative | Medium | Không có thẻ sản phẩm nào; hiển thị trạng thái rỗng (empty state) nếu có | Đã auto (TC_UI_17) |
| TC_HOME_05 | Trang chủ & Tìm kiếm | Lọc sản phẩm theo danh mục | Positive | High | Lọc đúng sản phẩm thuộc danh mục; về 'Tất cả' hiển thị lại đầy đủ | Đã auto (TC_UI_18) |
| TC_CAT_01 | Danh mục sản phẩm | Danh sách sản phẩm hiển thị khi mở trang chủ | Positive | Critical | Có ít nhất 1 thẻ sản phẩm hiển thị, mỗi thẻ có nút thêm vào giỏ | Đã auto (TC_UI_03) |
| TC_CART_01 | Giỏ hàng | Mở giỏ hàng từ Header | Positive | Critical | Ngăn giỏ hàng (cart-drawer) hiển thị | Đã auto (TC_UI_04) |
| TC_CART_02 | Giỏ hàng | Thêm sản phẩm vào giỏ | Positive | Blocker | Giỏ tự mở; sản phẩm xuất hiện trong giỏ; badge số lượng cập nhật | Một phần (trong TC_UI_06) |
| TC_CART_03 | Giỏ hàng | Áp dụng voucher phần trăm hợp lệ | Positive | High | Hiển thị dòng giảm giá voucher (cart-voucher-discount); tổng tiền giảm đúng 10% (không vượt trần 100.000đ) | Đã auto (TC_UI_19) |
| TC_CART_04 | Giỏ hàng | Áp dụng voucher cố định hợp lệ | Positive | Medium | Tổng tiền giảm đúng 20.000đ | Đã auto (TC_UI_21) |
| TC_CART_05 | Giỏ hàng | Áp dụng voucher không tồn tại | Negative | High | Báo lỗi voucher không hợp lệ; tổng tiền không đổi | Đã auto (TC_UI_20) |
| TC_CO_01 | Thanh toán | Chặn thanh toán khi chưa đăng nhập (Auth Guard) | Negative | Blocker | Auth Modal bật lên yêu cầu đăng nhập thay vì tạo đơn | Đã auto (trong TC_UI_06) |
| TC_CO_02 | Thanh toán | E2E đặt hàng + giả lập PayOS thành công | Positive | Blocker | Hiện màn hình 'Thanh toán thành công' (order-paid-view); bấm 'Tiếp tục mua sắm' quay về trang chủ | Đã auto (TC_UI_06) |
| TC_CO_03 | Thanh toán | Form checkout thiếu thông tin bắt buộc | Negative | High | Không tạo đơn; trường thiếu được báo lỗi/ngăn submit | Đã auto (TC_UI_22) |
| TC_CO_04 | Thanh toán | Sau đăng nhập từ Auth Guard quay lại đúng giỏ hàng | Positive | High | Modal đóng; giỏ hàng mở lại với đúng sản phẩm đã chọn trước đó | Đã auto (trong TC_UI_06) |
| TC_API_01 | API Backend | Health check Backend | Positive | High | HTTP 200; body có status='Healthy', database='PostgreSQL' | JMeter (GET Health API) |
| TC_API_02 | API Backend | Lấy danh sách sản phẩm + tìm kiếm | Positive | High | HTTP 200; danh sách không rỗng; kết quả search chỉ chứa sản phẩm liên quan | JMeter (GET Products / Search) |
| TC_API_03 | API Backend | Lấy danh mục và voucher | Positive | Medium | HTTP 200; categories ≥ 17; vouchers chứa DAFL10, DAFL20 | JMeter (GET Categories / Vouchers) |
| TC_API_04 | API Backend | Đăng nhập API đúng/sai thông tin | Positive + Negative | High | Lần 1: HTTP 200 + success=true, có thông tin user; Lần 2: bị từ chối (success=false/401) | JMeter (POST Login API - case đúng) |
| TC_API_05 | API Backend | Áp dụng voucher qua API | Positive + Negative | Medium | Lần 1 thành công với số tiền giảm đúng quy tắc 10% (trần 100.000đ); lần 2 bị từ chối | Thủ công (Postman) |
| TC_PERF_01 | Hiệu năng | Smoke performance các API đọc chính | Positive | High | Có file .jtl hợp lệ; error rate ≤ 2% và avg response time ≤ 1000ms thì gate PASS | Đã auto (JMeter + Gate) |

Chi tiết đầy đủ từng bước + dữ liệu kiểm thử: file `TEST-CASES-DAFL.xlsx` / `test-cases-dafl.csv` cùng thư mục.