# ÁNH XẠ KIỂM THỬ — Đặc tả CN-01..CN-08 ↔ Bộ test Selenium (repo2)

> Bộ test tự động: **580 test method / 35 lớp**, chia 2 thư mục riêng: `automation/src/test/java/vn/dafl/tests/web/` (17 lớp web bán hàng, 275 test — chia thư mục con theo nhóm chức năng: `auth/` đăng nhập/đăng ký, `catalog/` danh mục + chi tiết SP, `cart/` giỏ hàng, `checkout/` đặt hàng/thanh toán, `orders/` đơn hàng, `profile/` hồ sơ/địa chỉ, `membership/` hội viên, `home/` trang chủ + bản tin + 404 + footer; 3 lớp xuyên chức năng giữ ở gốc `web/`: `EndToEndShoppingFlowTest`, `NegativeAndExceptionScenariosTest`, `Wave15FeaturesTest`) và `automation/src/test/java/vn/dafl/tests/admin/` (18 lớp admin, 305 test — chia thư mục con theo phân hệ từ đợt 17: `catalog/` sản phẩm + danh mục + upload, `promotions/` khuyến mãi, `orders/` đơn hàng + trả hàng, `customers/` khách hàng + hội viên, `reviews/` đánh giá, `system/` truy cập + hồ sơ + tính năng đợt 13 + lớp mới đợt 18).
> Mọi locator bám `data-testid` thật của FE bản cuối (đã đối chiếu tự động với mã nguồn FE — 0 testid lệch; 2 locator cũ đã sửa: `filterByStatus` dùng tab thay cho `<select>`, 2 test dùng tên SP seed cũ "Khăn ướt" đổi sang "Mặt nạ giấy").
> Chạy: app DAFL ở `http://localhost:3000`, sau đó `cd automation && run_test_headless.bat` (hoặc `mvnw.cmd test -Dheadless=true`). Chỉ chạy web: `run_test_web.bat` (suite `testng-web.xml`, 275 test — chỉ cần web bán hàng `http://localhost:3000`); chỉ chạy admin: `run_test_admin.bat` (suite `testng-admin.xml`, 305 test — chỉ cần app Admin riêng `http://localhost:3001` đang chạy + tài khoản admin đã seed, KHÔNG cần `:3000` nữa từ đợt 10).
> Tài khoản demo: `demo@dafl.vn` / `Password123!`. Tài khoản Super Admin: `admin@dafl.vn` / `Admin@123` (đăng nhập được bằng tên `admin` ở trang Admin riêng localhost:3001) — DbInitializer tự tạo nếu thiếu; nếu chưa có thì các test ADM-03..ADM-14, ADT-01..ADT-09, ACR-01..ACR-08, ADM-W13-01..ADM-W13-04, UP-01..UP-03, RVM-01..RVM-09, ADM17-01..ADM17-17 tự bỏ qua (Skip). Voucher seed: DAFL10, DAFL20, FREESHIP, VIP50K, FORDEV.
> Đợt 13: xem mục "Tính năng mới đợt 13" ở cuối bảng ánh xạ (trang Hội viên, quên mật khẩu, sửa/xóa voucher Draft, điểm khách đọc lại được, ghi nhận hoàn tiền, lọc ngày + Export nhật ký, đăng nhập admin không nút quay lại).
> Đợt redesign form admin + upload ảnh: xem mục "Đợt redesign form admin + upload ảnh" ở cuối bảng ánh xạ (drawer form sản phẩm, dropdown danh mục custom, toast admin-toast, upload ảnh UP-01..UP-03).
> Đợt 15: xem mục "Tính năng mới đợt 15" ở cuối bảng ánh xạ (gửi đánh giá sản phẩm RV-01..04, dùng điểm khi thanh toán PT-01..04, ngày sinh + voucher sinh nhật BD-01..04).
> Đợt 15.1 (bước 3): xem mục "Đợt 15.1 — test auto từ file test case manual v15.1" ở cuối bảng ánh xạ (156 test mới / 12 lớp web (ngày 2026-11-10 gộp cả 8 lớp mở rộng vào file gốc: Auth, Profile, Catalog, ProductDetail, Cart, Checkout (+ CheckoutPaymentTest), Orders, Membership), cover đủ 176 case loại (c) trong 338 case manual; mã RV-05..10, PT-05..08, BD-05..09 do nhóm viết test gán nối tiếp RV/PT/BD đợt 15 — file manual không ghi sẵn các mã này).
> Đợt 16: xem mục "Đợt 16 — Quản lý đánh giá admin (RVM-01..08)" ở cuối bảng ánh xạ (mục "Đánh giá" thứ 9 trên Admin riêng :3001 — bảng, lọc sao + tìm kiếm, Export, phân trang, modal chi tiết, xóa qua hộp xác nhận).
> Đợt 17: xem mục "Đợt 17 — Giao diện + nghiệp vụ admin mới (ADM17-01..17)" ở cuối bảng ánh xạ (form thêm/sửa thành trang riêng 2 cột, cột Mã + STT, cột Trạng thái dạng Switch, dropdown lọc Trạng thái, popup chặn xóa dòng đang Hoạt động, menu tài khoản + trang Hồ sơ/đổi mật khẩu; test admin chia thư mục con theo phân hệ).
> Ghi chú về báo cáo Allure: báo cáo Allure hiển thị tên test, nhóm chức năng (Feature/Story) và tên suite bằng tiếng Việt (tên suite lấy từ thuộc tính `name` trong các file `automation/testng-web.xml`, `testng-admin.xml`, `testng.xml`). Menu giao diện Allure (Overview/Suites/Graphs...) vẫn tiếng Anh vì Allure không có bản dịch tiếng Việt.

## Tổng hợp theo lớp

| Lớp test | Số test | Phạm vi |
|---|---|---|
| web/AuthTest | 38 | CN-01 đăng ký, CN-02 đăng nhập/JWT/đăng xuất + đăng nhập admin bằng tên `admin` ở :3001, không còn nút social + link 'Quên mật khẩu' ở form đăng nhập shop (đợt 13, FP-01) + 19 test mở rộng gộp từ AuthExtendedTest (SCR_01 8 + SCR_02 11 case, BƯỚC 3) — 1 file duy nhất cho đăng nhập/đăng ký từ 2026-11-10 |
| web/CatalogAndSearchTest | 27 | CN-04 danh mục, tìm kiếm, lọc, sắp xếp, phân trang + nút 'Áp dụng' lọc giá + 15 test mở rộng gộp từ CatalogExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/ProductDetailTest | 19 | CN-05 chi tiết sản phẩm + đánh giá sao (đợt 4) + 10 test mở rộng gộp từ ProductDetailExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/CartManagementTest | 21 | CN-06 giỏ hàng khách + gộp giỏ + voucher + 12 test mở rộng gộp từ CartExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/CheckoutAndOrderTest | 40 | CN-07 thanh toán/đặt hàng, PayOS, phí ship + nút Đặt hàng sticky, tab đơn có số lượng + PayOS không còn tab nhúng/iframe + 19 test từ CheckoutExtendedTest + 11 test từ CheckoutPaymentTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/OrderCancellationTest | 16 | CN-08 hủy đơn + hoàn tồn kho + 13 test mở rộng gộp từ OrdersExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/ProfileAndAddressTest | 18 | CN-03 hồ sơ, đổi mật khẩu, sổ địa chỉ + 9 test mở rộng gộp từ ProfileExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/EndToEndShoppingFlowTest | 1 | Luồng đầu-cuối CN-04 → CN-08 |
| web/NegativeAndExceptionScenariosTest | 17 | Âm tính/ngoại lệ toàn hệ thống |
| web/HomeHeroTest | 10 | Banner hero + Flash Sale (đợt 2–4) + modal thông báo + dark mode (đợt 8) |
| web/NotFoundPageTest | 2 | Trang 404 (đợt 3–4) |
| web/NewsletterTest | 3 | Đăng ký bản tin ở footer web bán hàng NL-01..03 (chuyển từ admin (lớp cũ AdminAndNewsletterTest, nay system/AdminAccessTest) sang web đợt 10) |
| web/MembershipTest | 19 | Trang Hội viên riêng /membership (MEM-01/02) + 15 link footer (FTR-01) — đợt 13 + 16 test mở rộng gộp từ MembershipExtendedTest (BƯỚC 3) — 1 file duy nhất từ 2026-11-10 |
| web/Wave15FeaturesTest | 12 | Tính năng mới đợt 15: gửi đánh giá sản phẩm (RV-01..04), dùng điểm khi thanh toán (PT-01..04), ngày sinh + voucher sinh nhật (BD-01..04) |
| web/HomePageTest | 12 | Trang chủ mở rộng: banner, danh mục nổi bật, bán chạy, gợi ý, Flash Sale (SCR_09 13 case) — đợt 15.1 |
| web/HomeHeaderTest | 9 | Header mở rộng: tìm kiếm, giỏ, menu tài khoản, dark mode (SCR_09 9 case) — đợt 15.1 |
| web/NotificationsFooterTest | 11 | Thông báo + footer 15 link thật (SCR_11 24 case, 14 link footer gộp 1 test) — đợt 15.1 |
| admin/system/AdminAccessTest | 6 | Truy cập Admin riêng :3001 (đợt 3–4): khách → /login, tài khoản thường → trang 403 + hướng dẫn cấp quyền, Super Admin thấy đủ 10 mục menu theo thứ tự mới đợt 18 (dashboard, orders, returns, promotions, products, categories, reviews, customers, membership, logs) + Tổng quan mới (h1 'Tổng quan', KPI Doanh thu/Đơn hàng/Khách hàng mới/Tỷ lệ hủy-trả + khối 'Cần xử lý ngay', không còn biểu đồ cũ/nút 7-30 ngày) + chỉ đọc (ADM-01..06) |
| admin/system/AdminProfileTest | 11 | Đợt 19 (+9, ADM19-42..50): popover đóng khi bấm ngoài, Back + Cancel không lưu, thẻ xem 4 trường, validate hồ sơ, số điện thoại trống lưu được rồi khôi phục trong `finally`, đổi mật khẩu validate 8 nhánh (chỉ assert lỗi, KHÔNG đổi mật khẩu thật), Cancel đổi mật khẩu, dòng gợi ý, pagehead ẩn. Trước đó: đợt 17 (ADM17-12/13): menu tài khoản topbar → trang Hồ sơ (sửa họ tên, tên mới hiện ở topbar, tự khôi phục tên cũ) + đổi mật khẩu admin (sai mật khẩu hiện tại → lỗi inline; đúng → đổi sang mật khẩu tạm rồi đổi lại về Admin@123 ngay trong finally); đợt 18 wave D: đổi mật khẩu là Drawer FormShell [Hủy]+[Đổi mật khẩu] (xem ADM18-11) |
| admin/system/AdminWave13Test | 11 | Đợt 19 (+8, ADM19-34..41): toolbar/bảng nhật ký 5 cột/phân trang 10 dòng, STT trang 2 bắt đầu từ 11, tìm kiếm client-side, lọc ngày (nháp → Tìm → Đặt lại), log mới sau khi điều chỉnh điểm, badge tone theo hành động, tối đa 20 dòng. Trước đó: tính năng mới đợt 13 trên Admin riêng (ADM-W13-01/02/04): đăng nhập admin không nút quay lại, link 'Quên mật khẩu' ở admin, lọc ngày nhật ký hệ thống (đợt 18: bỏ phần Export CSV — Export đã bỏ hẳn; nút 'Lọc' cũng đã bỏ, khoảng ngày áp dụng khi bấm nút Tìm của toolbar Nhật ký); ADM-W13-03 (ghi nhận hoàn tiền) chuyển sang admin/orders/AdminOrderTest |
| admin/catalog/AdminCatalogBrowseTest | 17 | Đợt 19 (+8, ADM19-104..111): tìm kiếm/lọc theo danh mục + trạng thái, STT liên tục, phân trang, mở form xem từ dòng + nút mắt, EmptyState. Trước đó: chỉ đọc + duyệt danh mục (đợt 3–4, 8, 12): tìm kiếm/lọc, toolbar chuẩn, phân trang, mở form thêm SP (ADM-07, ADM-11..ADM-14), xem chi tiết SP/danh mục + mở form sửa từ form xem (ADT-01/02/08/09); đợt 18: form là Drawer (fs-drawer, bảng vẫn ở lại trong DOM), toolbar Thêm mới cuối phải + không nút Làm mới/Export, form xem mode 'view' (readonly + badge trạng thái + footer chỉ [Đóng]+[Chỉnh sửa]) |
| admin/catalog/AdminProductTest | 19 | Đợt 19 (+8, ADM19-81..88): bảng SP 10 cột + STT, toolbar/tìm kiếm/lọc, Drawer xem SP, Switch trạng thái, xóa SP qua popup chặn, phân trang. Trước đó: CRUD SP thật trên Admin riêng :3001 (ACR-01/02/03, đợt 9; đợt 18 viết lại theo form Drawer + tiền '99.000₫' + highlight 2 giây kiểm ngay sau lưu + cột Mã + Switch + popup chặn xóa ConfirmDialog 'Không thể xóa sản phẩm') + 8 test mới đợt 17 (ADM17-01/04/07/08/09/11/14/15): mã trùng, mã disable khi sửa, popup chặn xóa, Switch ('Ẩn sản phẩm?'/'Hiển thị sản phẩm?'), lọc Trạng thái, STT liên tục, mã SP- tự sinh |
| admin/catalog/AdminCategoryTest | 19 | Đợt 19 (+15, ADM19-89..103): bảng DM 7 cột + STT/Mã, toolbar/tìm kiếm/lọc trạng thái, Drawer xem DM, Switch, tạo/sửa/xóa DM thật (tự tạo + tự xóa), popup chặn xóa DM đang Hoạt động, phân trang. Trước đó: CRUD danh mục thật (ACR-04, đợt 9) + 3 test mới đợt 17 (ADM17-02/05/16): mã trùng, mã disable khi sửa, mã DM- tự sinh; đợt 18 wave D: form là Drawer rộng 680px (fs-drawer, --fs-width), popup chặn xóa ConfirmDialog 'Không thể xóa danh mục' |
| admin/catalog/AdminUploadTest | 8 | Đợt 19 (+5, ADM19-112..116): dán link ảnh, đặt ảnh chính, đổi thứ tự thumbnail, kéo-thả thay thế (qua input file), giới hạn 8 ảnh. **UP-02 VIẾT LẠI đợt 19** (xem mục Đợt 19): bản cũ assert sai — lỗi validate file (.txt/>5MB) KHÔNG hiện ở `admin-product-image-error` (chỉ hiện trên thumbnail lỗi + toast), và file lỗi VẪN tạo 1 thumbnail (trạng thái lỗi) nên `getProductImageThumbCount() == 0` cũng sai; bản mới assert theo code thật (toast 'Định dạng ảnh không hỗ trợ'/'Ảnh quá lớn' + nội dung lỗi trên thumbnail + nút 'Thử lại' + 0 thumbnail ready + 1 thumbnail lỗi). Test trùng ADM19-63 đã xóa (UP-02 mới cover FUNC_070/071). Trước đó: upload ảnh sản phẩm (đợt redesign form admin, UP-01..03): 1 ảnh qua input file thật → có thumbnail trong bảng + form xem; 2 ảnh + đặt ảnh thứ 2 làm ảnh chính (so src thumb-0 form xem với src thumb-0 form sửa); đợt 18: form là Drawer |
| admin/promotions/AdminPromoTest | 20 | Đợt 19 (+16, ADM19-159..174): bảng KM 10 cột + STT, toolbar, Drawer 680px, tạo voucher + kích hoạt ngay, Switch bật qua C4, xóa voucher Draft (Hủy thì không xóa), popup chặn xóa A5 + A5→C3, lọc trạng thái server-side, tìm kiếm client-side, bảng lượt dùng, phân trang, voucher Hết hạn chỉ hiện badge, chặn bật KM hết hạn C5 + C5→form sửa đổi hạn sang 31/12/2027. Trước đó: voucher thật (ACR-05, đợt 9/13; ADT-03 xem chi tiết theo view mode đợt 18: mode 'view', badge Nháp/Đang chạy/Đã tắt/Hết hạn, footer [Đóng]+[Chỉnh sửa]) + 2 test mới đợt 17 (ADM17-03/10): mã trùng (không phân biệt hoa thường), Switch Active→Disabled mở ConfirmDialog 'Tắt khuyến mãi?' bắt buộc lý do (lỗi 'Vui lòng bổ sung thông tin bắt buộc.'); đợt 18 wave D: form là Drawer rộng 680px, kích hoạt/tắt qua toast 'Đã bật/tắt khuyến mãi', tiền '50.000₫' |
| admin/orders/AdminOrderTest | 49 | Đợt 19 (+42, ADM19-117..158): toolbar/bảng/lọc/tìm kiếm đơn, Drawer chi tiết đơn, 12 test chuyển trạng thái thật (Pending→Paid/Expired, Paid→Confirmed/Preparing, Confirmed→Preparing/Shipping kèm nhà vận chuyển GHN, Preparing→Shipping, Shipping→Delivered, Delivered→Completed/ReturnRequested, ReturnRequested→Delivered — mỗi test tối đa 1 đơn, tự skip khi thiếu đơn ở trạng thái nguồn), validate vận chuyển, ghi chú khi đổi trạng thái, đơn terminal, hủy đơn hiện lý do + lịch sử, nút Hủy ẩn từ Shipping trở đi; trả hàng 12 test (toolbar/bảng/phân trang/lọc server-side/tìm kiếm client-side, Drawer trả hàng, duyệt E1, từ chối E2 bắt buộc lý do + đơn gốc về Delivered, đóng Drawer giữ Pending, nút ghi nhận hoàn tiền ẩn khi không có refundId, ghi nhận hoàn tiền thật → Refunded, validate mã giao dịch 2/101 ký tự + Hủy giữ PendingRefund). Trước đó: đơn hàng + trả hàng: đổi trạng thái + hủy 1 đơn 'Chờ xử lý' có sẵn (ACR-06/07, đợt 10 — không khôi phục được trạng thái cũ; đợt 18 wave D: ACR-06 đổi sang 'Đã xác nhận' qua popup D2 'Xác nhận đơn?' [Hủy]+[Xác nhận đơn], ACR-07 hủy qua popup D1 [Giữ đơn hàng]+[Hủy đơn hàng] + toast 'Đã cập nhật/hủy đơn hàng'), lọc đơn đã hủy (ADM-09) + KHÔNG còn Export (ADM-10 viết lại thành khẳng định không có nút Export, đợt 18), ghi nhận hoàn tiền (ADM-W13-03, chỉ mở modal xem), xem chi tiết đơn + trả hàng (ADT-05/07, đợt 18: footer đơn có điều kiện 1–2 nút; footer trả hàng chỉ [Từ chối]+[Duyệt]) |
| admin/customers/AdminCustomerTest | 21 | Đợt 19 (+15, ADM19-185..199): bảng KH + avatar chữ cái, lọc hạng/lọc trạng thái, tìm kiếm, Drawer xem KH, sửa KH thật (PUT /api/admin/customers/{id}) + khôi phục trong `finally`, khóa/mở khóa KH (khóa demo thì mở lại trong `finally`), phân trang, STT liên tục. Trước đó: khách hàng + hội viên: tìm kiếm khách (ADM-08), điều chỉnh điểm demo +5 rồi hoàn tác -5 (ACR-08; đợt 18 wave D: điểm đọc ở cột Điểm trong bảng, điều chỉnh qua popup riêng F2 'Xác nhận cộng điểm?' / F3 'Xác nhận trừ điểm?' + toast 'Đã điều chỉnh điểm'), xem chi tiết khách + hạng (ADT-04/06; đợt 18: ADT-06 theo view mode — không còn số dư điểm chi tiết/đơn gần đây trong form xem) + 2 test mới đợt 17 (ADM17-06/17): form sửa khách là Drawer rộng 680px, ô Mã/Email disable (không bấm Lưu để không đổi dữ liệu demo — PUT /api/admin/customers/{id} đã có từ đợt 17), mã KH- hiển thị đúng định dạng |
| admin/reviews/AdminReviewTest | 19 | Đợt 19 (+11, ADM19-200..210): bảng đánh giá + lọc sao/tìm kiếm, Drawer chi tiết, xóa 1–3 đánh giá thật (chỉ khi ≥ 2 đánh giá, tối đa 3/lượt chạy), ẩn/hiện KHÔNG viết được (xem mục Đợt 19 — C8/C9 chưa có API), phân trang, STT liên tục, nội dung cắt 80 ký tự. Trước đó: quản lý đánh giá admin (đợt 16, RVM-01..06 + RVM-08; đợt 18 wave D thêm RVM-09): mục "Đánh giá" thứ 9 — bảng STT/Sản phẩm/Khách/Sao/Nội dung/Ngày (đợt 17 thêm cột STT), lọc sao + tìm kiếm, phân trang 'Hiển thị {from}–{to} / {total} kết quả' + 'Trang X / Y' (không nút số trang, đợt 18), form xem đánh giá, xóa qua ConfirmDialog 'Xóa đánh giá?' (đúng 2 nút + dòng 'không thể hoàn tác.', toast 'Đã xóa đánh giá'); RVM-06 xóa 1 đánh giá thật, không khôi phục được; RVM-09: cột Sao dùng icon Star Lucide (5 svg, số sao đầy = rating, aria-label '{n} trên 5 sao', không còn ký tự '★'); **RVM-07 (Export CSV) ĐÃ XÓA đợt 18 — Export bỏ hẳn** |
| admin/system/AdminDot18Test | 16 | **MỚI đợt 18 (ADM18-01..13) + 3 test mới wave D (ADM18-14..16):** ConfirmDialog Hủy không gọi API / xác nhận xóa SP (ADM18-01/02), khóa KH bắt buộc lý do + bộ đếm `n/300` (ADM18-03, không khóa thật), badge menu + điều hướng (ADM18-04), **menu phẳng wave D** — tiêu đề nhóm là `<div>` không bấm được, tên dạng tiêu đề, không badge tổng nhóm (ADM18-05 viết lại), view mode SP/DM/VC/KH readonly + [Chỉnh sửa] giữ dữ liệu + **Drawer rộng (SP 760px, DM/KM/KH 680px)** (ADM18-06..09), footer chi tiết đơn có điều kiện + đóng bằng nút footer `[Đóng]` riêng (ADM18-10), Drawer đổi mật khẩu đúng 2 nút (ADM18-11), không còn nút Export ở 4 toolbar (ADM18-12), hủy đơn bắt buộc lý do + nút trái D1 `[Giữ đơn hàng]` (ADM18-13, không hủy thật), popup đổi trạng thái D2 `Xác nhận đơn?` / D3 `Cập nhật trạng thái?` chỉ mở xem rồi Hủy (ADM18-14, không đổi thật), đăng xuất hỏi popup G3 (ADM18-15), toolbar icon Lucide Search/RotateCcw không emoji (ADM18-16) |
| admin/system/AdminLoginTest | 9 | **MỚI đợt 19 (ADM19-01..09):** form đăng nhập admin chỉ cơ bản (không Google/Facebook, không tab đăng ký, không gợi ý tài khoản), map `admin`/`ADMIN`/` admin ` → admin@dafl.vn, đăng nhập bằng email đầy đủ + F5 giữ phiên, validate client 3 nhánh, sai mật khẩu chỉ 1 lỗi chung `Email hoặc mật khẩu không đúng`, icon hiện/ẩn mật khẩu, trang 403 + đổi tài khoản, route `/` và `/xyz` → `/admin`, topbar không nút Tạo mới/tìm kiếm/breadcrumb |
| admin/system/AdminDashboardTest | 24 | **MỚI đợt 19 (ADM19-10..33):** Tổng quan — menu active + aria-current, menu không icon, 5 KPI + so sánh kỳ trước, bộ lọc thời gian 5 nút segmented, DateRangePicker sai → `Khoảng thời gian không hợp lệ.`, lưu khoảng vào sessionStorage `dafl-admin-dashboard-range`, nút Làm mới, donut 5 trạng thái đơn, điều hướng từ KPI/hàng hành động, đơn gần đây, sắp hết hàng, phân bổ hạng, KM đang chạy |
| admin/catalog/AdminProductFormTest | 15 | **MỚI đợt 19 (ADM19-51..65):** form SP Drawer 760px — đủ trường, mã tự sinh SP-, validate tên/giá/tồn kho/danh mục, tiền ₫ dấu chấm nghìn, xem trước ảnh chính, dirty-close `Thoát mà chưa lưu?`, sửa SP (mã disable) + Hủy không lưu, xem SP readonly |
| admin/catalog/AdminCategoryFormTest | 15 | **MỚI đợt 19 (ADM19-66..80):** form DM Drawer 680px — đủ trường, mã tự sinh DM-, slug tự sinh, iconUrl, validate tên/slug/thứ tự, dirty-close, sửa DM (mã disable) + Hủy không lưu, xem DM readonly |
| admin/promotions/AdminPromoFormTest | 10 | **MỚI đợt 19 (ADM19-175..184):** form KM Drawer 680px — đủ trường, mã tự uppercase + nút sinh mã ngẫu nhiên 8 ký tự, validate mã/giá trị/phạm vi, form sửa khóa trường, dirty-close `Thoát mà chưa lưu?`, sửa giá trị 0 bị chặn; các test validate chỉ assert lỗi rồi đóng, không lưu |
| admin/customers/AdminMembershipTest | 16 | **MỚI đợt 19 (ADM19-211..226):** Hội viên — bảng hạng + ngưỡng, lưu cấu hình hạng qua popup F1 (ngưỡng tính động lúc chạy, skip nếu không tăng dần được), đặt lại cấu hình (KHÔNG xóa lỗi inline — xem mục Đợt 19), cộng/trừ điểm demo qua F2/F3 + hoàn tác trong `finally`, validate cộng/trừ điểm, bảng lịch sử điểm |
| **Tổng** | **580** | **275 web + 305 admin** |

## CN-01 — Đăng ký tài khoản

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-CN01-01 | AuthTest.registerNewAccountThenLogin | `register-success-msg` → chuyển tab đăng nhập → đăng nhập ngay được (không OTP) |
| TC-CN01-02 | AuthTest.registerFieldValidationErrors | `register-name-error`, `register-email-error`, `register-phone-error` |
| TC-CN01-03 | AuthTest.registerPasswordPolicyChecklist | `password-rule-len/upper/digit/special` chuyển xanh (✓) |
| CN-01 E2 | AuthTest.registerDuplicateEmailShowsError | `register-email-error` chứa "Email đã được sử dụng" |
| CN-01 E3 | AuthTest.registerDuplicatePhoneShowsError | `register-phone-error` chứa "Số điện thoại đã được sử dụng" |
| — | AuthTest.registerWithInvalidEmailFormat | `register-email-error`, không hiện `register-success-msg` |
| CN-01 E5 | Negative.registerWithMismatchedConfirmPassword | `register-confirm-password-error` chứa "không khớp" |
| NEG-REG-01..04 | Negative.registerWithTooShortPassword / MissingUppercase / MissingSpecialChar / MissingDigit | `register-password-error` + rule tương ứng chưa xanh |
| NEG-REG-06 | Negative.registerWithInvalidPhoneFormat | `register-phone-error` (SĐT 9 số) |

## CN-02 — Đăng nhập / đăng xuất (JWT)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-CN02-01 | AuthTest.loginWithDemoCredentialsSuccessfully | header hiện tên demo; `localStorage.dafl_token` có JWT |
| CN-02 E1 | AuthTest.loginWithWrongPasswordShowsError | `login-password-error` = "Email hoặc mật khẩu không đúng" |
| CN-02.3 | AuthTest.logoutSuccessfully | token bị xóa; menu về "Đăng nhập" |
| TC-CN02-04 | AuthTest.logoutThenBackDoesNotRevealPrivatePage | Back sau đăng xuất không thấy trang riêng tư |
| TC-CN02-03 | AuthTest.protectedPagesRedirectToLoginWhenLoggedOut | /orders, /profile → /login |
| TC-CN02-05 | AuthTest.expiredTokenRedirectsToLoginWithNotice | sửa `dafl_token` → /login + `session-expired-notice` |
| NEG-AUTH-01..03 | Negative.loginWithUnregisteredEmail / EmptyFields / InvalidEmailFormat | `login-password-error`, `login-email-error` |
| — | AuthTest.switchBetweenLoginAndRegisterTabs / closeModalAndReopen / toggleLoginPasswordVisibility / loginFailsThenSucceedsInSameModalSession | `auth-tab-login`, `auth-tab-register`, `auth-close-btn`, `toggle-login-password-btn` |
| CN-02 (đợt 8) | AuthTest.adminLoginWithUsernameAdminOnAdminOnlyApp | trang Admin riêng `localhost:3001/login`, nhập `admin` → map `admin@dafl.vn`, tiêu đề `Đăng nhập quản trị`, vào được menu admin (skip nếu service :3001 chưa chạy) |
| CN-02 (đợt 8) | AuthTest.shopLoginFormHasNoSocialLoginButtons | form đăng nhập shop không còn nút/chữ Google/Facebook (đã bỏ hẳn đăng nhập mạng xã hội) |
| FP-01 (đợt 13) | AuthTest.shopLoginShowsForgotPasswordLinkAndOpensForm | link `forgot-password-link` ("Quên mật khẩu?") ở form đăng nhập shop → bấm mở `forgot-password-form` (ô `forgot-password-email-input` + nút `forgot-password-submit-btn`) → nút `forgot-password-back-btn` về lại form đăng nhập; chỉ kiểm UI, không gửi mail thật |

## CN-03 — Hồ sơ & sổ địa chỉ

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-CN03-01 | ProfileAndAddressTest.updateProfileSuccessfully | `profile-form`, `profile-success-msg` "Cập nhật thành công"; email chỉ đọc |
| TC-CN03-02 | ProfileAndAddressTest.updateProfileWithDuplicatePhoneShowsError | `profile-phone-error` chứa "Số điện thoại đã được sử dụng" |
| CN-03.2 | ProfileAndAddressTest.changePasswordWithWrongCurrentShowsError | `change-password-current-error` chứa "Mật khẩu hiện tại không đúng" |
| TC-CN03-04 | ProfileAndAddressTest.changePasswordThenLoginWithNewPassword | `change-password-success` → đăng nhập lại bằng mật khẩu mới |
| TC-CN03-05 | ProfileAndAddressTest.firstAddressBecomesDefaultAutomatically | `address-default-badge-{id}` ở địa chỉ đầu tiên |
| CN-03.3 | ProfileAndAddressTest.setAnotherAddressAsDefault | `address-set-default-btn-{id}` |
| CN-03.3 | ProfileAndAddressTest.editAddressUpdatesInformation | `address-edit-btn-{id}`, `address-form` |
| CN-03.3 | ProfileAndAddressTest.deleteDefaultAddressPromotesRemaining | `address-delete-btn-{id}` + `address-delete-confirm-btn` |
| TC-CN03-03 | ProfileAndAddressTest.addressBookRejectsSixthAddress | `address-limit-error` khi đã có 5 địa chỉ |

## CN-04 — Danh mục, tìm kiếm, lọc, sắp xếp, phân trang

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| CN-04.1 | CatalogAndSearchTest.homePageDisplaysProducts | `product-card-{id}` ≤ 12 ở trang 1 |
| CN-04.2 | CatalogAndSearchTest.searchByProductKeyword | `search-input` (header) |
| CN-04.2 | CatalogAndSearchTest.searchWithNoMatchingKeyword | `catalog-empty-state` |
| TC-CN04-04 | CatalogAndSearchTest.searchWithSqlInjectionKeywordIsTreatedAsPlainText | từ khóa `' OR 1=1 --` → 0 kết quả + `catalog-empty-state` |
| CN-04.3 | CatalogAndSearchTest.filterByFirstCategory | `category-pill-{id}`, `category-pill-all` |
| TC-CN04-01 | CatalogAndSearchTest.paginationShowsTwelvePerPageAndNavigates | `catalog-pagination`, `pagination-page-2`, `pagination-prev-btn` |
| TC-CN04-02 | CatalogAndSearchTest.sortByPriceAscendingAndDescending | `sort-select` (price-asc/price-desc) |
| TC-CN04-03 | CatalogAndSearchTest.priceRangeFilterAndInvalidRangeError | `price-from-input`, `price-to-input`, `price-filter-error` |
| TC-CN04-04 (đợt 8) | CatalogAndSearchTest.priceApplyButtonAppliesRange | nut `price-apply-btn` ap dung khoang gia da nhap; khoang sai + nut Ap dung thi bao loi |
| TC-CN04-05 (đợt 11) | CatalogAndSearchTest.pricePresetAppliesRange | bam khoang gia co san `price-preset-1` (100K - 300K) dien san Tu/Den + loc dung khoang |
| CN-04/05 | CatalogAndSearchTest.outOfStockProductShowsBadge | `product-stock-{id}` = "Hết hàng" + nút thêm bị khóa (Skip nếu seed chưa có) |
| CN-05 | CatalogAndSearchTest.openProductDetailAndBack | `product-detail-page`, `product-detail-back-btn` |

## CN-05 — Chi tiết sản phẩm

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| CN-05.1 | ProductDetailTest.productDetailDisplaysFullInformation | `detail-qty-value` = 1; đọc được "(Còn X sản phẩm có sẵn)" |
| TC-CN05-01 | ProductDetailTest.quantityCannotGoBelowOne | `detail-qty-minus` bị khóa ở mức 1 |
| TC-CN05-02 | ProductDetailTest.quantityClampsAtAvailableStock | `detail-qty-plus` kẹp tại tồn kho (Skip nếu tồn kho > 25) |
| CN-05.2 | ProductDetailTest.addToCartFromDetailWithChosenQuantity | `detail-add-to-cart-btn`, badge = 3 |
| CN-05.2 | ProductDetailTest.buyNowAddsItemAndGoesToCheckout | `buy-now-btn` → /cart + `checkout-form` |
| TC-CN05-03 | ProductDetailTest.outOfStockProductDetailDisablesPurchase | `detail-out-of-stock-notice`; 2 nút mua bị khóa (Skip nếu seed không có) |
| TC-CN05-04 | ProductDetailTest.nonExistentProductShows404 | `product-not-found-page` với id không tồn tại |

## CN-06 — Giỏ hàng (khách + tài khoản)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-CN06-01 | CartManagementTest.addProductToCartAsGuest | thêm giỏ KHÔNG hiện auth modal; `cart-item-{id}` |
| CN-06.1 | CartManagementTest.addSameProductTwiceAccumulates | `cart-badge-count` = 2, số lượng cộng dồn |
| CN-06.2 | CartManagementTest.increaseQuantityUpdatesTotal | `cart-item-plus-{id}`, `cart-raw-subtotal` |
| TC-CN06-02 | CartManagementTest.decreaseQuantityFromOneAsksConfirmation | `cart-remove-confirm-dialog`, `cart-remove-confirm-btn` |
| CN-06.3 | CartManagementTest.removeItemRequiresConfirmation | `cart-item-remove-{id}`, `cart-remove-cancel-btn` giữ nguyên giỏ |
| CN-06 E2 | CartManagementTest.setQuantityBeyondStockClampsToStock | `cart-item-qty-{id}` (input) bị kẹp về tồn kho |
| CN-07 voucher | CartManagementTest.applyVoucherReducesTotal | `voucher-code-input`, `applied-voucher-banner`, `cart-final-total` giảm đúng 20.000đ (DAFL20) |
| TC-CN06-03 | CartManagementTest.guestCartMergesIntoAccountCartOnLogin | gộp giỏ: A = 1 (server) + 1 (khách) = 2, B = 1 |
| CN-06.4 | CartManagementTest.accountCartPersistsAfterLogoutAndRelogin | giỏ server còn nguyên sau đăng nhập lại |

## CN-07 — Thanh toán & đặt hàng

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-CN07-01 | CheckoutAndOrderTest.placeOrderSuccessfully | `shipping-*`, `cart-checkout-btn`, `order-success-code` khớp `DH\d{8}-\d{4}`, trạng thái "Chờ xác nhận" |
| O1 PayOS | CheckoutAndOrderTest.payWithMockPayosAndSeeOrderInHistory | `payment-method-payos`, `checkout-payment-view`, `simulate-payos-success-btn`, `order-paid-view` |
| O1 PayOS (đợt 8) | CheckoutAndOrderTest.payosPageHasNoEmbeddedGatewayIframe | trang thanh toan chi con 'Quét mã VietQR tại chỗ', KHONG con tab 'Nhúng Cổng PayOS', KHONG con `<iframe>`; mo phong thanh cong van duoc |
| TC-CN07-02 | CheckoutAndOrderTest.checkoutRequiresLoginAndReturnsAfterLogin | khách bấm `cart-checkout-btn` → /login → quay lại /cart, giỏ còn nguyên |
| CN-07.1 | CheckoutAndOrderTest.placeOrderUsingSavedAddressFromAddressBook | `shipping-address-list`, `shipping-address-option-{id}` |
| TC-CN07-03 | CheckoutAndOrderTest.doubleClickSubmitCreatesOnlyOneOrder | double-click `cart-checkout-btn` → đúng 1 đơn (tài khoản mới) |
| CN-07 E8 | CheckoutAndOrderTest.orderNoteLongerThan200CharsIsRejected | `shipping-note-input` 201 ký tự → `shipping-note-error` |
| O1 phí ship | CheckoutAndOrderTest.shippingFeeIs25kBelowThresholdAndFreeAbove | `cart-shipping-fee`: "25.000" dưới 299.000đ; "Miễn phí" từ 299.000đ |
| TC-CN07-04 | Negative.voucherRejectedAtCheckoutWhenCartDropsBelowMinimum | `checkout-error-msg` chứa "Đơn hàng tối thiểu cần đạt" (BE từ chối) |
| NEG-VCH-01/02 | Negative.applyInvalidVoucherCode / applyEmptyVoucherCode | `voucher-error-msg` = "Mã giảm giá không tồn tại hoặc đã hết hiệu lực" |
| NEG-CHK-01/02 | Negative.checkoutWithInvalidPhone / checkoutWithEmptyShippingDetail | `shipping-phone-error`, `shipping-detail-error` |
| NEG-CHK-03 | Negative.checkoutWithEmptyCart | `cart-empty-state`, không có `cart-checkout-btn` |

## CN-08 — Đơn hàng & hủy đơn

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| CN-08.1 | CheckoutAndOrderTest.placeOrderSuccessfully (phần sau) | `orders-list`, `order-item-{mãDH}`, `order-detail-view`, `order-detail-status` |
| CN-08.1 | OrderCancellationTest.filterOrdersByStatus | `order-status-filter` (Đã hủy / Chờ xác nhận) |
| TC-CN08-02 | Negative.cannotSeeAnotherUsersOrder | tài khoản khác không thấy `order-item-{mãDH}` của demo |
| TC-CN08-03 | OrderCancellationTest.cancelPendingOrderSuccessfullyAndRestocks | `cancel-order-btn` → `cancel-order-dialog` → `cancel-reason-select` → `cancel-confirm-btn` → `cancel-success-msg`; tồn kho hoàn lại qua trang chi tiết SP |
| CN-08.3 | OrderCancellationTest.cancelButtonDisappearsAfterCancellation | mở lại chi tiết đơn đã hủy: không còn `cancel-order-btn` |

## Luồng đầu-cuối

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| E2E | EndToEndShoppingFlowTest.guestToCheckoutEndToEndFlow | khách thêm 2 SP → thanh toán bị đưa qua đăng nhập → gộp giỏ → điền `shipping-*` → COD → `order-success-code` dạng DH → chi tiết đơn "Chờ xác nhận" + `order-shipping-info` |

## Tính năng mới đợt hoàn thiện giao diện 2–4 (banner, Flash Sale, 404, Admin, newsletter, đánh giá, doanh thu)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| HERO-01 | HomeHeroTest.heroCarouselDisplaysThreeSlidesWithTextBox | `hero-carousel`, `hero-slide-0/1/2`, `hero-dot-0/1/2`; slide active có `.hero-text-box` + tiêu đề 1 trong 3 tiêu đề đã biết |
| HERO-02 | HomeHeroTest.heroPrevNextButtonsNavigateSlides | `hero-prev-btn` / `hero-next-btn` đổi slide đúng vòng 0 → 1 → 0 |
| HERO-03 | HomeHeroTest.heroDotsNavigateToSlide | `hero-dot-2` → slide 2, `hero-dot-1` → slide 1 |
| HERO-04 | HomeHeroTest.heroAutoAdvancesToNextSlide | banner tự chạy 4s/slide: slide active tự đổi trong 12 giây |
| HERO-05 | HomeHeroTest.homeShowsCosmeticsCategoryIconsBrandStripAndSections | 6 `category-icon-*`, `brand-strip`, `best-seller-section`, `suggested-section` |
| FS-01 | HomeHeroTest.flashSaleSectionDisplaysDiscountedProductsAndCountdown | `flash-sale-section`, `flash-sale-countdown` dạng HH:MM:SS, 1–8 `flash-sale-item-*` |
| FS-02 | HomeHeroTest.flashSaleCountdownTicksDown | đếm ngược giảm thật sau 3 giây (tới 23:59:59 hôm nay) |
| HERO-08 (đợt 8) | HomeHeroTest.notificationModalOpensWithListOrEmptyState | `notif-bell-btn` → `notifications-modal`; có item `notification-item-*` hoặc dòng `Chưa có thông báo`; đóng được |
| HERO-09 (đợt 8) | HomeHeroTest.notificationModalHasMarkAllReadButton | modal thông báo có nút `mark-all-read-btn` cùng hàng tiêu đề |
| HERO-10 (đợt 8) | HomeHeroTest.darkModeToggleSwitchesTheme | `theme-toggle-btn` đổi `data-theme` light ↔ dark và đổi lại được |
| NF-01 | NotFoundPageTest.unknownRouteShowsNotFoundPage | `not-found-page` + nút "Về trang chủ" đưa về trang chủ |
| NF-02 | NotFoundPageTest.unknownProductShowsProductNotFoundPage | `/product/khong-ton-tai` → `product-not-found-page` ("404 - Không tìm thấy sản phẩm") |
| ADM-01 | system.AdminAccessTest.guestOpeningAdminIsRedirectedToLogin | khách mở `/admin` → về `/login` |
| ADM-02 | system.AdminAccessTest.customerOpeningAdminSeesForbiddenPageWithGuidance | tài khoản thường → `admin-forbidden` + "Cách cấp quyền Super Admin" + email đang đăng nhập |
| ADM-03 | system.AdminAccessTest.adminSeesTenMenuItems | Super Admin thấy đủ 10 `admin-nav-*` đúng thứ tự (đợt 17.1 tách mục Nhật ký hệ thống khỏi Trả hàng; skip nếu chưa seed admin) |
| ADM-04 | system.AdminAccessTest.adminDashboardLoads | mục Tổng quan tải được |
| ADM-05 | system.AdminAccessTest.adminCanBrowseOrdersProductsCustomersReadOnly | chỉ đọc: `admin-order-search`, `admin-product-search`, `admin-customer-search` |
| ADM-06 | system.AdminAccessTest.adminRevenueChartLoadsAndRangeButtonsWork | `admin-revenue-range-7/30` đổi khoảng (GET `/api/admin/reports/revenue`); có đơn thì `admin-revenue-chart` vẽ SVG, chưa có đơn thì dòng "Chưa có doanh thu..." |
| ADM-07 (đợt 8) | catalog.AdminCatalogBrowseTest.adminProductSearchFiltersRows | `admin-product-search` loc dung ('Mặt nạ' ra dong, tu khoa la ra 0 dong + 'Không có sản phẩm nào.') |
| ADM-08 (đợt 8) | catalog.AdminCatalogBrowseTest.adminCustomerSearchFiltersRows | `admin-customer-search` loc dung (email demo ra dong, email la ra 0 dong + 'Không có khách hàng nào.') |
| ADM-09 (đợt 8) | catalog.AdminCatalogBrowseTest.adminOrderStatusFilterShowsOnlyCancelled | `admin-order-status-filter` chon 'Đã hủy' + nut 'Tìm' → chi don da huy hoac 'Không có đơn hàng nào.' |
| ADM-10 (đợt 8) | catalog.AdminCatalogBrowseTest.adminExportButtonIsClickableWithoutError | nut 'Export' bam duoc, trang admin van dung yen (xuat CSV client-side, khong khang dinh file tai ve) |
| ADM-11 (đợt 12, viết lại) | catalog.AdminCatalogBrowseTest.adminToolbarSearchFiltersProductsAndTopbarSearchRemoved | o search topbar đã bỏ (khẳng định âm: topbar không còn ô tìm kiếm) — tìm kiếm ở toolbar của mục Sản phẩm (`admin-product-search`) vẫn lọc đúng ('Mặt nạ' ra dòng) |
| ADM-12 (đợt 12) | catalog.AdminCatalogBrowseTest.adminProductsPaginationNextPrevChangesPage | phân trang bảng Sản phẩm (`admin-products-pagination`, 10 dòng/trang): trang 1 tối đa 10 dòng, bấm 'Sau' sang trang 2 rồi 'Trước' về trang 1, 'Trang X / Y' đổi theo (skip nếu ≤ 10 dòng) |
| ADM-13 (đợt 12, viết lại đợt redesign form admin) | catalog.AdminCatalogBrowseTest.adminAddProductOpensFormModalAndCloseReturnsToTable | bấm '+ Thêm sản phẩm' ở toolbar → form mở thành TRANG RIÊNG 2 cột (`admin-product-form-modal`, class `sa-form-page`, tiêu đề 'Thêm sản phẩm', bảng danh sách bị thay khỏi DOM) có dropzone upload ảnh (`admin-product-image-dropzone`) + nút 'Hủy' (`admin-product-cancel-btn`) / 'Lưu' (`admin-product-save-btn`) ở footer; đóng bằng nút 'Hủy' (hoặc 'Quay lại' `admin-form-back-btn`) → quay lại bảng, không tạo dữ liệu |
| ADM-14 (đợt 12) | catalog.AdminCatalogBrowseTest.adminProductsToolbarHasAddResetRefreshButtons | toolbar chuẩn mục Sản phẩm: nút '+ Thêm sản phẩm' ở trái + nút icon 'Đặt lại' (`admin-products-reset-btn`) / 'Làm mới' (`admin-products-refresh-btn`) ở phải; bấm 'Đặt lại' xóa từ khóa và về trang 1; mục Danh mục cũng có đủ 2 nút icon |
| ADT-01 (đợt 8) | catalog.AdminCatalogBrowseTest.productDetailModalShowsCorrectProduct | nut mat `Xem chi tiết sản phẩm *` → `admin-product-detail` hien dung ten SP + 'Giá bán', dong duoc |
| ADT-02 (đợt 8) | catalog.AdminCatalogBrowseTest.categoryDetailModalShowsCorrectCategory | nut mat danh muc → `admin-category-detail` hien dung ten danh muc, dong duoc |
| ADT-03 (đợt 8) | promotions.AdminPromoTest.promoDetailModalShowsCorrectCode | nut mat khuyen mai → `admin-promo-detail` hien dung ma voucher, dong duoc |
| ADT-04 (đợt 8) | customers.AdminCustomerTest.tierDetailModalShowsCorrectTier | nut mat hang → `admin-tier-detail` hien dung ten hang, dong duoc |
| ADT-05 (đợt 8) | orders.AdminOrderTest.orderDetailModalShowsOrderCode | nut mat don → `admin-order-detail` hien dung ma don (skip neu DB chua co don) |
| ADT-06 (đợt 8) | customers.AdminCustomerTest.customerDetailModalShowsCorrectCustomer | nut mat khach → `admin-customer-detail` hien dung ten khach, dong duoc |
| ADT-07 (đợt 8) | orders.AdminOrderTest.returnDetailModalShowsOrderCode | nut 'Duyệt / Từ chối' → `admin-return-detail` hien ma don, chi dong (khong duyet/tu choi; skip neu chua co yeu cau tra hang) |
| ADT-08 (đợt 12, sửa) | catalog.AdminCatalogBrowseTest.productDetailEditButtonOpensEditFormWithoutSaving | tu modal chi tiet SP, nut 'Sửa sản phẩm' → form sua mo trong modal (`admin-product-form-modal`, tieu de 'Sửa sản phẩm', o ten dien san ten SP — tieu de/ten khong con trong text cua form); dong form khong luu |
| ADT-09 (đợt 8) | catalog.AdminCatalogBrowseTest.categoryDetailModalClosesWithFooterButton | modal chi tiet danh muc dong duoc bang nut 'Đóng' o footer |
| NL-01 | NewsletterTest.newsletterSubscribeSuccess | email mới → `.newsletter-success` "Cảm ơn bạn đã đăng ký nhận bản tin DAFL." |
| NL-02 | NewsletterTest.newsletterInvalidEmailShowsError | email sai định dạng → `.newsletter-error` "Vui lòng nhập email đúng định dạng." |
| NL-03 | NewsletterTest.newsletterDuplicateEmailShowsAlreadyRegistered | đăng ký lại email cũ → vẫn khối cảm ơn, không lỗi, không trùng lặp DB |
| ACR-01 (đợt 9, viết lại đợt redesign form admin) | catalog.AdminProductTest.adminCreateProductShowsInTableAndSearchable | tạo SP tên `TESTSP-<timestamp>` qua form trang riêng 2 cột mới (đợt 17, giá 99.000đ, giá gốc 120.000đ, tồn 10, mô tả) → toast `admin-toast` + tìm theo tên thấy 1 dòng đúng giá 99.000đ + giá gốc 120.000đ + dòng mới highlight (`sa-row-highlight`); tự xóa SP sau test |
| ACR-02 (đợt 9, viết lại đợt redesign form admin) | catalog.AdminProductTest.adminEditProductChangesNameAndPrice | tạo SP (99.000đ / giá gốc 120.000đ) rồi sửa tên + giá 150.000đ + giá gốc 200.000đ qua form trang riêng (ô Mã bị disable — xem ADM17-04) → tìm tên mới thấy đúng giá mới, tìm tên cũ ra 0 dòng; mở lại form sửa: badge % giảm (`admin-product-discount-badge`) hiện 25%, ô giá hiển thị "150.000" chuẩn hóa về 150000 khi bỏ dấu chấm; tự xóa SP sau test |
| ACR-03 (đợt 9) | catalog.AdminProductTest.adminDeleteProductRemovesFromTable | tạo SP (mới tạo đang Hoạt động) rồi xóa: popup chặn `admin-delete-blocked-modal` hiện → bấm "Chuyển sang Ngừng hoạt động" → hộp xác nhận chung (`admin-confirm-modal` + `admin-confirm-yes-btn`) → xóa được → toast + tìm lại ra 0 dòng + 'Không có sản phẩm nào.' |
| ACR-04 (đợt 9, viết lại đợt redesign form admin) | catalog.AdminCategoryTest.adminCategoryCreateEditDelete | tạo danh mục `TESTDM-<timestamp>` (mô tả + thứ tự 5) → dòng mới hiện slug tự sinh (`testdm-<timestamp>`) + mô tả + mã DM- tự sinh; mở form sửa thấy Switch trạng thái (`admin-category-status-switch-{id}`, role='switch') → sửa tên → xóa qua hộp xác nhận; mỗi bước kiểm tra qua tìm kiếm (sau xóa: 0 dòng + 'Chưa có danh mục.') |
| ACR-05 (đợt 9, viết lại đợt 13, viết lại đợt redesign form admin) | promotions.AdminPromoTest.adminPromoCreateActivateDisable | voucher 1: tạo `TEST<timestamp>` qua form mới (giảm 10%, đơn tối thiểu 0, hạn 2027-12-31) → Draft (có nút `admin-promo-edit-btn`/`admin-promo-delete-btn`) → sửa mô tả + giá trị 15% + đơn tối thiểu 100.000đ + trần giảm 50.000đ + hạn 2028-06-30 + tổng lượt 100 → vẫn Draft → xóa qua hộp xác nhận `admin-confirm-modal` + nút `admin-confirm-yes-btn` → tìm lại 0 dòng + 'Chưa có khuyến mãi.'; voucher 2: nút tạo mã ngẫu nhiên (`admin-promo-generate-code-btn`) sinh mã + loại giảm tiền cố định 50.000đ (segmented `admin-promo-discount-type-fixed`) → kích hoạt thành Active (mất nút sửa/xóa, Switch `admin-promo-status-switch-{id}` bật) → gạt Switch mở modal nhập lý do, nhập lý do thành Disabled (Switch tắt), để lại (vô hiệu) |
| ACR-06 (đợt 10) | orders.AdminOrderTest.adminChangePendingOrderStatusToConfirmed | đổi trạng thái 1 đơn 'Chờ xử lý' CÓ SẴN trong DB sang 'Đã xác nhận' → bảng đơn hiện 'Đã xác nhận' (không đặt đơn qua :3000; trạng thái cũ không khôi phục được; skip nếu DB không có đơn chờ xử lý) |
| ACR-07 (đợt 10) | orders.AdminOrderTest.adminCancelPendingOrder | hủy 1 đơn 'Chờ xử lý' CÓ SẴN trong DB, bắt buộc có lý do → bảng đơn hiện 'Đã hủy' (không đặt đơn qua :3000; trạng thái cũ không khôi phục được; skip nếu DB không có đơn chờ xử lý) |
| ACR-08 (đợt 9, viết lại đợt 13) | customers.AdminCustomerTest.adminAdjustPointsForDemoCustomer | đọc số dư điểm demo TRƯỚC qua modal chi tiết khách (`admin-customer-detail-points`) → cộng 5 điểm → thông báo thành công + ô điểm/lý do được xóa trắng → đọc lại SAU, khẳng định tăng đúng +5; hoàn tác -5 sau test (tổng kết bằng 0). Lưu ý testid cột Điểm ở bảng Khách hàng là `admin-customer-points-<id>` (id khách, không phải email) |
| UP-01 (đợt redesign form admin) | catalog.AdminUploadTest.adminCreateProductWithOneUploadedImage | tạo SP `TESTSP-IMG-<timestamp>` qua form trang riêng 2 cột mới (đợt 17), upload 1 ảnh PNG qua input file thật (`admin-product-image-input`, sendKeys đường dẫn tuyệt đối) → thumbnail `admin-product-image-thumb-0` hiện, không lỗi → lưu → toast + tìm thấy SP, dòng có thumbnail ảnh trong bảng + modal chi tiết có `<img>`; tự xóa SP sau test |
| UP-02 (đợt redesign form admin) | catalog.AdminUploadTest.adminUploadInvalidAndOversizeImageIsRejectedInline | upload file `.txt` → lỗi inline (`admin-product-image-error` có nội dung) + 0 thumbnail → bấm Hủy (không lưu); upload ảnh PNG > 5MB (tạo lúc chạy test bằng padding, không commit file lớn) → lỗi inline + 0 thumbnail → bấm Hủy (không lưu) |
| UP-03 (đợt redesign form admin) | catalog.AdminUploadTest.adminSecondUploadedImageCanBeSetAsMain | tạo SP upload 2 ảnh (`test-product-1.png`, `test-product-2.png`) → đủ 2 thumbnail → đặt ảnh thứ 2 làm ảnh chính (`admin-product-image-set-main-btn-1`, nhãn 'Ảnh chính' chuyển sang thumbnail 0) → lưu → mở lại form sửa vẫn đủ 2 thumbnail + nhãn 'Ảnh chính' vẫn ở thumbnail 0 + modal chi tiết hiện ảnh chính có src trùng src thumbnail 0 khi mở lại form sửa (backend đổi tên file thành UUID nên không khẳng định bằng tên file gốc); tự xóa SP sau test |
| CN-05 (đợt 4) | ProductDetailTest.productDetailShowsReviewsSection | `product-reviews-section` luôn hiện; có đánh giá → `product-rating-summary` + `product-review-item`, chưa có → "Chưa có đánh giá nào" |
| CN-05 (đợt 4) | ProductDetailTest.seededProductShowsSampleReviewWithAverageRating | Son kem 3CE → 1 đánh giá mẫu, tóm tắt "5.0", Trần Hoàng Nam (skip nếu DB hiện tại không có đánh giá mẫu) |
| CN-07 (đợt 3) | CheckoutAndOrderTest.checkoutSubmitButtonHasStickyClassAndOrderTotal | nút Đặt hàng có class `checkout-submit-btn` (sticky mobile), ghi "Đặt hàng" |
| CN-08 (đợt 2–3) | CheckoutAndOrderTest.orderStatusTabsShowCountsAndFilterByTab | tab `order-status-tab-*` hiện số lượng "(n)"; bấm tab lọc đúng danh sách (đã sửa `filterByStatus` từ `<select>` sang tab) |

> Đổi mật khẩu admin: từ đợt 17 đã có test ADM17-13 tự đổi mật khẩu admin sang tạm `Admin@456` rồi đổi lại về `Admin@123` ngay trong `finally` (kể cả khi test fail giữa chừng); rủi ro còn lại duy nhất là JVM/suite chết đúng giữa lúc mật khẩu đang là tạm — khôi phục bằng tay bằng cách đăng nhập với `Admin@456` rồi đổi lại `Admin@123` ở trang Hồ sơ. Test CRUD (ACR-01..ACR-08) chỉ đụng dữ liệu do chính test tạo (tên/mã kèm timestamp) và tự dọn sau khi chạy — trừ ACR-06/07 đổi/hủy 1 đơn 'Chờ xử lý' có sẵn trong DB (không khôi phục được trạng thái cũ); không test khóa/mở khóa tài khoản người khác, không duyệt/từ chối yêu cầu trả hàng của người khác. Từ đợt 13, admin ĐÃ có sửa/xóa voucher Draft (chỉ Draft) qua hộp xác nhận chung `admin-confirm-modal` — xem ACR-05.

## Tính năng mới đợt 13 (quên mật khẩu, hội viên, footer, hoàn tiền, nhật ký, đăng nhập admin)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| FP-01 | AuthTest.shopLoginShowsForgotPasswordLinkAndOpensForm | xem dòng CN-02 ở trên: link `forgot-password-link` ở shop, chỉ kiểm UI, không gửi mail thật |
| FP-02 | system.AdminWave13Test.adminLoginShowsForgotPasswordLinkAndOpensForm | link `forgot-password-link` ở form đăng nhập admin riêng → mở `forgot-password-form` → nút `forgot-password-back-btn` về lại form đăng nhập; chỉ kiểm UI, không gửi mail thật (mã 6 số gửi qua SMTP thật chỉ kiểm bằng tay) |
| MEM-01 | MembershipTest.loggedInMembershipPageShowsTierPointsHistoryAndVouchers | đăng nhập demo → menu tài khoản `dropdown-membership-btn` → `/membership`: `membership-page`, `membership-tier`, `membership-points-balance`, `membership-benefits`, `membership-history`, `membership-vouchers`, `membership-refresh-btn` (bấm làm mới vẫn hiện đủ) |
| MEM-02 | MembershipTest.membershipPageRedirectsToLoginWhenLoggedOut | chưa đăng nhập mở thẳng `/membership` → về `/login` (guard ở App.tsx; giống TC-CN02-03 cho /orders, /profile) |
| FTR-01 | MembershipTest.footerLinksHaveRealHrefsAndTrackOrderGoesToOrders | đủ 15 link `footer-link-*` (skincare, makeup, hair, body, flash-sale, guide, shipping, returns, track-order, faq, about, shops, magazine, member, contact) đều có href thật cùng SUT, không phải `#`; `footer-link-member` → `/membership`; bấm `footer-link-track-order` → `/orders` |
| ADM-W13-01 | system.AdminWave13Test.adminLoginPageHasNoBackButtonAndCenteredWrapper | trang đăng nhập admin riêng: `auth-close-btn` VẮNG MẶT, khung `auth-page-wrapper` (class `auth-page-wrapper`, căn giữa) hiện, tiêu đề 'Đăng nhập quản trị' |
| ADM-W13-02 | system.AdminWave13Test.adminLoginShowsForgotPasswordLinkAndOpensForm | xem FP-02 ở trên |
| ADM-W13-03 | orders.AdminOrderTest.refundRecordButtonOnlyOnApprovedPendingRefundAndOpensModal | tab Trả hàng: nút `admin-return-record-refund-btn` chỉ xét khi có dữ liệu thật (skip nếu DB không có yêu cầu Approved + PendingRefund + refundId); dòng có nút phải chứa 'Approved' + 'PendingRefund'; bấm mở `admin-refund-record-modal` (ô `admin-refund-transaction-input` + nút `admin-refund-record-confirm-btn`) rồi đóng, KHÔNG nhập mã, KHÔNG xác nhận hoàn tiền thật |
| ADM-W13-04 | system.AdminWave13Test.auditLogDateFilterAndExportControlsWork | cuối tab Trả hàng, mục 'Nhật ký hệ thống': ô `admin-log-date-from` / `admin-log-date-to` + nút `admin-log-filter-btn` (lọc khoảng 2026-01-01..2026-12-31, mục vẫn hiện) + nút `admin-log-export-btn` (bấm xong trang admin vẫn đứng yên; chỉ bấm, không khẳng định file tải về — giống ADM-10) |
| ACR-05 (đợt 13) | promotions.AdminPromoTest.adminPromoCreateActivateDisable | xem dòng ACR-05 ở trên: sửa/xóa voucher Draft qua `admin-confirm-modal` |
| ACR-08 (đợt 13) | customers.AdminCustomerTest.adminAdjustPointsForDemoCustomer | xem dòng ACR-08 ở trên: đọc điểm trước/sau qua `admin-customer-detail-points`, khẳng định +5 |

> Lưu ý testid thật trong code đợt 13 (đừng dùng tên viết tắt): form quên mật khẩu dùng `forgot-password-email-input` / `forgot-password-submit-btn` / `forgot-password-back-btn` / `forgot-password-success` / `forgot-password-notice`; form đặt lại dùng `reset-password-form`, `reset-code-input`, `reset-new-password-input`, `reset-confirm-password-input`, `reset-password-submit-btn`. Cột Điểm ở bảng Khách hàng dùng `admin-customer-points-<id>` (id khách). Nút sửa/xóa voucher lặp lại theo dòng — test định vị theo dòng qua aria-label `Sửa khuyến mãi <mã>` / `Xóa khuyến mãi <mã>`, không dùng testid trần.

## Đợt redesign form admin + upload ảnh (drawer/modal form mới)

Form sản phẩm giờ là DRAWER (vẫn testid `admin-product-form-modal`); dropdown danh mục là custom (nút trigger `admin-product-category-select`, KHÔNG còn `<select>`); ô giá bán tự định dạng dấu chấm nghìn khi gõ (assert giá trị ô phải chuẩn hóa bằng cách bỏ dấu `.`); thông báo sau lưu/xóa ở form mới là toast `admin-toast` (hàm `waitForFeedback()` chấp nhận cả `admin-toast` lẫn `.sa-notice` cũ, nhưng test khẳng định chặt `admin-toast`); hàng mới tạo highlight bằng class `sa-row-highlight` (không phải testid); xóa SP/danh mục qua hộp xác nhận chung `admin-confirm-modal`; khuyến mãi KHÔNG có upload ảnh (backend không có field ảnh).

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| ADM-13 (viết lại) | catalog.AdminCatalogBrowseTest.adminAddProductOpensFormModalAndCloseReturnsToTable | xem dòng ADM-13 ở trên: trang riêng 2 cột + dropzone + nút Hủy/Lưu ở footer, đóng bằng nút Hủy |
| ACR-01 (viết lại) | catalog.AdminProductTest.adminCreateProductShowsInTableAndSearchable | xem dòng ACR-01 ở trên: form trang riêng + giá gốc + mô tả + toast + highlight |
| ACR-02 (viết lại) | catalog.AdminProductTest.adminEditProductChangesNameAndPrice | xem dòng ACR-02 ở trên: badge % giảm + ô giá chuẩn hóa bỏ dấu chấm |
| ACR-04 (viết lại) | catalog.AdminCategoryTest.adminCategoryCreateEditDelete | xem dòng ACR-04 ở trên: slug tự sinh + Switch trạng thái |
| ACR-05 (viết lại) | promotions.AdminPromoTest.adminPromoCreateActivateDisable | xem dòng ACR-05 ở trên: form voucher đầy đủ + nút tạo mã ngẫu nhiên + segmented loại giảm + tắt voucher bằng Switch + modal lý do |
| UP-01 | catalog.AdminUploadTest.adminCreateProductWithOneUploadedImage | xem dòng UP-01 ở trên |
| UP-02 | catalog.AdminUploadTest.adminUploadInvalidAndOversizeImageIsRejectedInline | xem dòng UP-02 ở trên |
| UP-03 | catalog.AdminUploadTest.adminSecondUploadedImageCanBeSetAsMain | xem dòng UP-03 ở trên |

Testid mới đợt này (FE bắt buộc dùng đúng các tên này — xem `AdminPageUI.java` mục "Dot redesign form admin + upload anh"):

| Nhóm | Testid |
|---|---|
| Sản phẩm | `admin-product-original-price-input`, `admin-product-description-input`, `admin-product-name-count`, `admin-product-description-count`, `admin-product-discount-badge`, `admin-product-status-toggle`, `admin-product-preview`, `admin-product-stock-minus-btn`, `admin-product-stock-plus-btn`, `admin-product-image-dropzone`, `admin-product-image-input`, `admin-product-image-link-toggle`, `admin-product-image-url-input`, `admin-product-image-thumb-<index>`, `admin-product-image-set-main-btn-<index>`, `admin-product-image-remove-btn-<index>`, `admin-product-image-progress-<index>`, `admin-product-image-error`, `admin-product-cancel-btn`, `admin-product-<field>-error`, `admin-toast` |
| Danh mục | `admin-category-slug-input`, `admin-category-description-input`, `admin-category-sort-order-input`, `admin-category-status-toggle`, `admin-category-image-dropzone`, `admin-category-image-input`, `admin-category-image-thumb`, `admin-category-image-remove-btn`, `admin-category-cancel-btn`, `admin-category-save-btn` |
| Khuyến mãi | `admin-promo-generate-code-btn`, `admin-promo-discount-type-fixed`, `admin-promo-discount-type-percent`, `admin-promo-value-input`, `admin-promo-min-order-input`, `admin-promo-max-discount-input`, `admin-promo-max-uses-input`, `admin-promo-expiry-input`, `admin-promo-status-toggle` |

> Lưu ý đợt redesign: (1) Nút 'Lưu' form danh mục đã có testid `admin-category-save-btn` (FE thêm đợt redesign) nên test dùng locator theo testid (`ADMIN_CATEGORY_SAVE_BTN`). (2) Option của dropdown danh mục custom KHÔNG có testid trong hợp đồng nên test chọn theo text hiển thị (`role='option'`, dự phòng `<li>` / `<button>` trong phạm vi drawer) — xem `AdminPageObject.selectProductCategory`. (3) Mô tả voucher KHÔNG có testid trong hợp đồng nên vẫn dùng locator theo nhãn `<span>` (`ADMIN_PROMO_DESCRIPTION_INPUT`). (4) Ngày hết hạn khuyến mãi là `<input type="date">` — test nhập định dạng yyyy-MM-dd. (5) UP-03 khẳng định ảnh chính bằng nhãn 'Ảnh chính' trên thumbnail 0 (trước/sau khi lưu) + so sánh src ảnh chi tiết với src thumbnail 0 khi mở lại form sửa, vì endpoint `POST /api/admin/upload` đổi tên file thành UUID nên không khẳng định bằng tên file gốc được. (6) File ảnh test thật ở `automation/src/test/resources/upload/` (`test-product-1.png`, `test-product-2.png` — PNG 1x1 hợp lệ — và `invalid-file.txt`); file ảnh > 5MB được tạo lúc chạy test bằng padding, không commit file lớn vào repo.

## Tính năng mới đợt 15 (gửi đánh giá, dùng điểm, ngày sinh + voucher sinh nhật)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| RV-01 | Wave15FeaturesTest.reviewFormDisplaysAllFieldsOnProductDetail | mở chi tiết SP đầu tiên: `review-form`, đủ 5 nút `review-star-1..5`, `review-name-input`, `review-comment-input`, `review-submit-btn` |
| RV-02 | Wave15FeaturesTest.submitReviewWithoutStarShowsError | điền sẵn tên + bình luận, bấm gửi khi chưa chọn sao -> lỗi đúng nguyên văn "Vui lòng chọn số sao đánh giá." (`review-error-msg`) |
| RV-03 | Wave15FeaturesTest.submitReviewWithoutCommentShowsError | chọn 5 sao + điền tên, để trống bình luận rồi gửi -> lỗi đúng nguyên văn "Vui lòng nhập nội dung đánh giá." |
| RV-04 | Wave15FeaturesTest.submitValidReviewAppearsInListAfterReload | chọn 5 sao + bình luận hợp lệ (kèm timestamp) rồi gửi -> "Cảm ơn bạn đã gửi đánh giá cho sản phẩm!" (`review-success-msg`); tải lại trang, đánh giá mới có trong danh sách. TẠO DỮ LIỆU THẬT trên SP seed, không xóa được qua UI |
| PT-01 | Wave15FeaturesTest.guestCartHasNoUsePointsSection | chưa đăng nhập mở thẳng /cart -> KHÔNG có `use-points-section` |
| PT-02 | Wave15FeaturesTest.loggedInCartToggleFillsMaxPointsAndDiscountMatches | đăng nhập demo, đọc số dư điểm qua /membership (0 điểm thì Skip); thêm 1 SP rồi mở /cart, bật `use-points-toggle` -> `points-to-use-input` tự điền tối đa (> 0, ≤ số dư), `cart-points-discount` đúng bằng số điểm × 1.000đ; xong tắt toggle để dọn |
| PT-03 | Wave15FeaturesTest.pointsInputOverMaxClampsToMaxWithError | như PT-02 (0 điểm thì Skip); nhập 999999 vào `points-to-use-input` -> bị kẹp về trần {m} + lỗi đúng mẫu "Chỉ dùng tối đa {m} điểm cho đơn này." (`points-error-msg`; {m} trích từ chính câu lỗi để không khẳng định cứng số); xong tắt toggle để dọn |
| PT-04 | Wave15FeaturesTest.voucherFordevDisablesUsePointsSection | đăng nhập demo, thêm 1 SP, mở /cart, áp mã seed FORDEV -> `use-points-section` vẫn hiện nhưng `use-points-toggle` bị disable + ghi chú "Không dùng điểm cùng mã FORDEV."; áp mã thất bại thì Skip (không khẳng định sai); xong gỡ voucher để dọn |
| BD-01 | Wave15FeaturesTest.birthdateSectionDisplaysAllControlsOnMembership | đăng nhập mở /membership: `birthdate-section`, `birthdate-input`, `birthdate-save-btn`, `claim-birthday-voucher-btn` đều hiện |
| BD-02 | Wave15FeaturesTest.saveBirthdateWhenNotSetShowsSuccess | chỉ chạy khi `birthdate-input` đang trống (đã có giá trị thì Skip — BE khóa sửa 90 ngày); nhập 1998-05-20 rồi lưu -> "Đã cập nhật ngày sinh." (`birthdate-success-msg`) |
| BD-03 | Wave15FeaturesTest.claimBirthdayVoucherAsRegularTierShowsError | hạng demo là Thường (không phải Thường thì Skip) bấm nhận voucher -> lỗi đúng nguyên văn "Voucher sinh nhật chỉ dành cho hạng Vàng trở lên." (`birthday-voucher-error-msg`); không tạo dữ liệu |
| BD-04 | Wave15FeaturesTest.claimBirthdayVoucherTwiceSecondTimeShowsError | chỉ chạy khi đủ điều kiện (hạng Vàng trở lên + đã có ngày sinh + sinh nhật trong 30 ngày quanh hôm nay), còn lại Skip; bấm nhận 2 lần liên tiếp -> lần 2 phải hiện `birthday-voucher-error-msg` (không khẳng định cứng câu lỗi vì phụ thuộc trạng thái thật: đã nhận trong năm / ngoài cửa sổ 30 ngày); lần 1 có thể tạo 1 voucher sinh nhật thật cho demo (1 lần/năm) |

> Lưu ý đợt 15: (1) Form đánh giá được test ở chế độ KHÁCH (không đăng nhập) vì có ô `review-name-input`; nếu FE thật chỉ hiện form khi đã đăng nhập thì RV-01..RV-04 fail và cần thêm bước đăng nhập demo trước khi mở chi tiết. (2) Ô ngày sinh là `<input type="date">` — test gõ theo thứ tự ddMMyyyy (Chrome hiển thị vi-VN), nếu gõ phím không ăn thì đặt bằng native setter + sự kiện input/change (xem `MembershipPageObject.setBirthdate`). (3) Số dư điểm đọc từ `membership-points-balance` — test lấy cụm số ĐẦU TIÊN trong dòng (xem `MembershipPageObject.getAvailablePoints`); nếu FE đổi định dạng dòng số dư (vd thêm số khác đứng trước) thì hàm này đọc sai. (4) PT-02/PT-03/PT-04 tự dọn trạng thái giỏ demo (tắt toggle, gỡ voucher) và tự gỡ voucher cũ trước khi áp mã, để không phụ thuộc thứ tự chạy; nhưng RV-04 vẫn để lại 1 đánh giá thật trên SP seed.

## Đợt 15.1 — test auto từ file test case manual v15.1 (338 case → 156 test mới)

Đối chiếu 338 case manual web v15.1 (`dafl-test-manual-web-v15.1.xlsx`, 11 sheet SCR_01..SCR_11) với bộ Selenium 157 test: **(a) đã có test 105 case / (b) không tự động được 57 case / (c) thiếu 176 case**. Bảng đối chiếu đầy đủ: `~/workspace/manual-v15-draft/MAPPING_MANUAL_TO_AUTO.md` (1 dòng/case).

156 test mới dưới đây cover đủ **176/176 case loại (c)** (mỗi test đều ghi mã case manual `TC_SCRx_...` trong `@Test(description=...)`; test gộp nhiều case biến thể dữ liệu thì liệt kê đủ các mã). 2 case được cover 1 PHẦN bởi test mới (phần còn lại đã có test cũ): `TC_SCR5_FUNC_016` (phần mới 'form đánh giá luôn hiển thị' — RV-10; phần hiển thị mục đánh giá khi chưa có đánh giá vẫn do `ProductDetailTest.productDetailShowsReviewsSection` cover) và `TC_SCR6_UI_014` (xem description của test tương ứng).

Mã RV-05..10 / PT-05..08 / BD-05..09 **không có sẵn trong file manual** — do nhóm viết test gán nối tiếp mã đợt 15 (RV-01..04, PT-01..04, BD-01..04): RV-05=TC_SCR5_VAL_023, RV-06=TC_SCR5_FUNC_024, RV-07=TC_SCR5_FUNC_025, RV-08=TC_SCR5_FUNC_028, RV-09=TC_SCR5_FUNC_015, RV-10=TC_SCR5_FUNC_016; PT-05=TC_SCR7_FUNC_056, PT-06=TC_SCR7_VAL_058, PT-07=TC_SCR7_FUNC_060, PT-08=TC_SCR7_FUNC_061; BD-05=TC_SCR10_FUNC_020, BD-06=TC_SCR10_FUNC_022, BD-07=TC_SCR10_FUNC_023, BD-08=TC_SCR10_FUNC_024, BD-09=TC_SCR10_VAL_019. Trong nhóm (c) chỉ có 4 case điểm nên không có PT-09/PT-10.

> Lưu ý đợt 15.1 (phát hiện khi đối chiếu code thật): (1) Kỳ vọng manual TC_SCR4_FUNC_027 SAI so với code — badge online chỉ hiện 'Hiển thị {n} sản phẩm', dạng 'Hiển thị {n} / {tổng}' chỉ ở chế độ fallback/offline; test viết theo hành vi thật, cần sửa kỳ vọng trong file manual/inventory. (2) Test cũ `CheckoutAndOrderTest.payWithMockPayosAndSeeOrderInHistory` (TC O1 PayOS, SCR7_047) khẳng định bấm `simulate-payos-success-btn` xong ra `order-paid-view` — SAI ở chế độ demo: nút mô phỏng chỉ gọi `GET /api/payments/status`, demo trả Pending nên FE chỉ hiện toast 'Đang chờ thanh toán'; đường Paid duy nhất ở demo là qua webhook `/mock-payos` (xem test mới SCR7_049/050). Test mới SCR7_048 khẳng định đúng hành vi demo. **ĐÃ SỬA 2026-11-10 (sau đợt 15.1): test 047 và test `payosPageHasNoEmbeddedGatewayIframe` (cùng khẳng định sai) đổi kỳ vọng thành toast 'Đang chờ thanh toán' + vẫn ở màn hình thanh toán + không hiện order-paid-view.** (3) Locator cũ `ANY_PRODUCT_CARD` ở trang chủ đếm cả thẻ flash/best/suggest nên 2 test cũ `homePageDisplaysProducts` / `filterByFirstCategory` có thể fail khi chạy E2E thật. **ĐÃ SỬA 2026-11-10: `ANY_PRODUCT_CARD` giờ chỉ đếm thẻ trong `.products-grid` (3 mục kia dùng hậu tố -flash/-best/-suggest).** (4) Các mức tiền gốc trong manual không ghép được từ giá seed (mọi giá đều là bội của 1.000đ): 305.000đ → 306.000đ, 250.000đ → 248.000đ, 2.000.001đ → 2.001.000đ (mức thay thế vẫn kích hoạt đúng luật cần kiểm); COD đúng 2.000.000đ ghép được (prod-1 ×1 + prod-2 ×5 + prod-6 ×4). (5) Test tự skip (SkipException) khi thiếu dữ liệu/trạng thái: PT-02/03 + PT-05..08 khi demo 0 điểm; BD-02 khi đã có ngày sinh; BD-05/08 khi demo đã nhận voucher sinh nhật năm nay; TC_SCR2_FUNC_027 (giới hạn gửi lại mã reset — inventory không ghi chuỗi lỗi/HTTP/testid); TC_SCR4_FUNC_026 (không danh mục nào > 12 SP để sang trang 2); TC_SCR4_FUNC_029 (seed không có SP IsActive=false). (6) Dữ liệu thật các test tạo ra khi chạy E2E: 12 đơn COD (phân trang), 4 đơn hủy, 1 đơn trừ 50 điểm demo (chỉ khi demo có điểm), 1 địa chỉ mặc định mới trên demo, 1 đơn PayOS Paid qua webhook giả, đánh giá thật trên SP seed (RV-06/07/08, kèm timestamp).

| Mã TC (manual) | Lớp.phương thức | Mã test |
|---|---|---|
| TC_SCR1_UI_003 | AuthTest.registerPasswordTogglesWorkIndependently |  |
| TC_SCR1_UI_004 | AuthTest.switchingTabsClearsPreviousErrors |  |
| TC_SCR1_VAL_005 | AuthTest.registerWithAllFieldsEmptyShowsAllErrors |  |
| TC_SCR1_VAL_006 | AuthTest.registerWithTooLongNameShowsError |  |
| TC_SCR1_VAL_009 | AuthTest.registerWithInvalidPhoneVariantsShowsError |  |
| TC_SCR1_VAL_016 | AuthTest.registerWithEmptyEmailShowsError |  |
| TC_SCR1_VAL_017 | AuthTest.registerWithEmptyConfirmPasswordShowsError |  |
| TC_SCR1_FUNC_019 | AuthTest.duplicateEmailKeepsEnteredDataExceptPasswords |  |
| TC_SCR2_UI_002 | AuthTest.loginFormHasNoDemoOrOtpEntry |  |
| TC_SCR2_SEC_015 | AuthTest.protectedPageReturnsAfterLogin |  |
| TC_SCR2_VAL_017 | AuthTest.loginWithUsernameAdminOnShopShowsFormatError |  |
| TC_SCR2_FUNC_018 | AuthTest.forgotPasswordStep1EmptyOrInvalidEmailShowsError |  |
| TC_SCR2_FUNC_019 | AuthTest.forgotPasswordWithUnregisteredEmailShowsGenericMessage |  |
| TC_SCR2_VAL_020 | AuthTest.resetPasswordWithShortCodeShowsError |  |
| TC_SCR2_VAL_021 | AuthTest.resetPasswordWithWeakNewPasswordShowsError |  |
| TC_SCR2_VAL_022 | AuthTest.resetPasswordWithMismatchedConfirmShowsError |  |
| TC_SCR2_FUNC_023 | AuthTest.resetPasswordWithWrongCodeShowsErrorAndOldPasswordStillWorks |  |
| TC_SCR2_FUNC_024 | AuthTest.resetPasswordWrongCodeFiveTimesDestroysCode |  |
| TC_SCR2_FUNC_027 | AuthTest.forgotPasswordResendLimitExceedsThreePerHour |  |
| TC_SCR6_UI_002 | CartManagementTest.cartLineShowsAllComponentsAndTotals |  |
| TC_SCR6_FUNC_008 | CartManagementTest.directQuantityInputFiltersNonDigits |  |
| TC_SCR6_FUNC_016, TC_SCR6_UI_014 | CartManagementTest.cartTotalFormulaWithVoucherAndShipping |  |
| TC_SCR6_UI_017 | CartManagementTest.freeshipProgressShowsMissingAmount |  |
| TC_SCR6_UI_017 | CartManagementTest.freeshipProgressShowsFreeShipAchieved |  |
| TC_SCR6_VAL_021 | CartManagementTest.applyVip50kBelowMinOrderShowsError |  |
| TC_SCR6_FUNC_023 | CartManagementTest.applyDafl10ReducesTotalByTenPercent |  |
| TC_SCR6_FUNC_025 | CartManagementTest.applyFreeshipVoucherMakesShippingFree |  |
| TC_SCR6_FUNC_026 | CartManagementTest.applyVip50kReducesTotalBy50k |  |
| TC_SCR6_FUNC_027 | CartManagementTest.applyFordevVoucherBringsTotalTo2000 |  |
| TC_SCR6_UI_028 | CartManagementTest.voucherInputUppercaseAndChipAppliesCode |  |
| TC_SCR6_UI_029 | CartManagementTest.removeVoucherRestoresOriginalTotal |  |
| TC_SCR4_UI_001 | CatalogAndSearchTest.productCardShowsDiscountBadgeRatingOriginalPriceAndStock |  |
| TC_SCR4_FUNC_005 | CatalogAndSearchTest.clearCategoryButtonOnlyShowsAfterSelectingCategory |  |
| TC_SCR4_FUNC_006 | CatalogAndSearchTest.searchCategoryByNameShowsOnlyMatchingPills |  |
| TC_SCR4_FUNC_007 | CatalogAndSearchTest.searchCategoryByDescriptionShowsMatchingCategory |  |
| TC_SCR4_FUNC_008 | CatalogAndSearchTest.searchCategoryNoMatchShowsEmptyStateAndShowAllRestores |  |
| TC_SCR4_FUNC_009 | CatalogAndSearchTest.clearButtonInCategorySearchRestoresAllPills |  |
| TC_SCR4_FUNC_010 | CatalogAndSearchTest.productTypeGroupInSidebarFiltersByCategory |  |
| TC_SCR4_FUNC_011, TC_SCR4_FUNC_013, TC_SCR4_FUNC_014, TC_SCR4_FUNC_015 | CatalogAndSearchTest.remainingPricePresetsFillRangeFilterImmediatelyAndMarkActive |  |
| TC_SCR4_FUNC_017 | CatalogAndSearchTest.typingPriceManuallyDeselectsActivePreset |  |
| TC_SCR4_FUNC_019 | CatalogAndSearchTest.searchKeywordIsCaseInsensitive |  |
| TC_SCR4_FUNC_020 | CatalogAndSearchTest.clearFiltersAfterEmptySearchRestoresFullList |  |
| TC_SCR4_FUNC_023 | CatalogAndSearchTest.defaultSortIsNewest |  |
| TC_SCR4_FUNC_026 | CatalogAndSearchTest.categoryFilterPersistsAcrossPages |  |
| TC_SCR4_FUNC_027 | CatalogAndSearchTest.resultCountBadgeUpdatesWithFilterAndPage |  |
| TC_SCR4_FUNC_029 | CatalogAndSearchTest.discontinuedProductNotShownInList |  |
| TC_SCR7_UI_001 | CheckoutAndOrderTest.shippingFormDisplaysAllEightFieldsAndNoteCounter |  |
| TC_SCR7_UI_002 | CheckoutAndOrderTest.savedAddressListShowsDefaultBadgeAndAddButton |  |
| TC_SCR7_UI_003 | CheckoutAndOrderTest.addressCascadeLocksLowerLevelsAndResetsOnProvinceChange |  |
| TC_SCR7_UI_004 | CheckoutAndOrderTest.emptyFormSubmitShowsErrorsAndFocusesFirstError |  |
| TC_SCR7_FUNC_006 | CheckoutAndOrderTest.saveAsDefaultCheckboxCreatesDefaultAddressAfterOrder |  |
| TC_SCR7_VAL_007 | CheckoutAndOrderTest.emptyRecipientShowsError |  |
| TC_SCR7_VAL_008 | CheckoutAndOrderTest.oneCharRecipientShowsError |  |
| TC_SCR7_VAL_009 | CheckoutAndOrderTest.emptyPhoneShowsError |  |
| TC_SCR7_VAL_011 | CheckoutAndOrderTest.emptyEmailShowsError |  |
| TC_SCR7_VAL_012 | CheckoutAndOrderTest.invalidEmailFormatShowsError |  |
| TC_SCR7_VAL_013 | CheckoutAndOrderTest.missingProvinceShowsError |  |
| TC_SCR7_VAL_014 | CheckoutAndOrderTest.missingDistrictShowsError |  |
| TC_SCR7_VAL_015 | CheckoutAndOrderTest.missingWardShowsError |  |
| TC_SCR7_VAL_017 | CheckoutAndOrderTest.shortDetailAddressShowsError |  |
| TC_SCR7_UI_026 | CheckoutAndOrderTest.pricingSummaryAndPaymentMethodsDisplayedWithoutEmbeddedGateway |  |
| TC_SCR7_UI_033 | CheckoutAndOrderTest.submitButtonLabelChangesByPaymentMethodAndLoginState |  |
| TC_SCR7_FUNC_024 | CheckoutAndOrderTest.codStillAvailableAtExactlyTwoMillion |  |
| TC_SCR7_FUNC_025 | CheckoutAndOrderTest.codDisabledAboveTwoMillionWithNoteAndAutoSwitchToPayos |  |
| TC_SCR7_FUNC_052 | CheckoutAndOrderTest.directCheckoutUrlRedirectsToCart |  |
| TC_SCR7_FUNC_020 | CheckoutAndOrderTest.discountDoesNotRemoveFreeShipping |  |
| TC_SCR7_FUNC_029 | CheckoutAndOrderTest.voucherPercent10DiscountIsCorrectOnOrder |  |
| TC_SCR7_FUNC_030 | CheckoutAndOrderTest.voucherFreeshipMakesShippingFreeOnOrder |  |
| TC_SCR7_UI_046 | CheckoutAndOrderTest.payosPaymentViewShowsTimerAmountAndCopyBackButtons |  |
| TC_SCR7_FUNC_048 | CheckoutAndOrderTest.simulatePayosCheckBeforeTransferShowsWaitingToast |  |
| TC_SCR7_FUNC_049 | CheckoutAndOrderTest.mockPayosPageHasSingleConfirmButtonAndConfirmsPayment |  |
| TC_SCR7_FUNC_050 | CheckoutAndOrderTest.paymentSuccessPageShowsOrderSummaryAndLinks |  |
| TC_SCR7_FUNC_056 | CheckoutAndOrderTest.toggleOffAndOnKeepsUsedPoints | PT-05 |
| TC_SCR7_VAL_058 | CheckoutAndOrderTest.pointsInputFiltersNonDigitsAndKeepsValue | PT-06 |
| TC_SCR7_FUNC_060 | CheckoutAndOrderTest.turningPointsOffResetsPointsAndRestoresTotal | PT-07 |
| TC_SCR7_FUNC_061 | CheckoutAndOrderTest.orderWithPointsDeductsBalanceAndLogsHistory | PT-08 |
| TC_SCR9_UI_021 | HomeHeaderTest.topbarShowsAllThreeMessages |  |
| TC_SCR9_FUNC_022 | HomeHeaderTest.brandLogoNavigatesBackToHome |  |
| TC_SCR9_UI_023 | HomeHeaderTest.searchBoxHasPlaceholderClearButtonAndNoSubmitButton |  |
| TC_SCR9_FUNC_024 | HomeHeaderTest.shopsAndMagazineMenuItemsNavigateHome |  |
| TC_SCR9_FUNC_025 | HomeHeaderTest.adminNavLinkOnlyShowsForAdminAndLeadsToWebNotFoundPage |  |
| TC_SCR9_UI_026 | HomeHeaderTest.notificationBadgeHiddenWhenNoUnreadNotifications |  |
| TC_SCR9_UI_026 | HomeHeaderTest.notificationBadgeShowsUnreadCountForDemoAccount |  |
| TC_SCR9_UI_027 | HomeHeaderTest.accountDropdownShowsCorrectItemsForGuestAndLoggedInUser |  |
| TC_SCR9_UI_028 | HomeHeaderTest.cartBadgeHiddenWhenEmptyShowsCountAfterAddingAndOpensCart |  |
| TC_SCR9_UI_001, TC_SCR9_UI_002, TC_SCR9_UI_003 | HomePageTest.heroSlidesDisplayCorrectContentPerSlide |  |
| TC_SCR9_UI_004 | HomePageTest.heroAutoAdvancesAboutEveryFourSecondsAndWrapsAround |  |
| TC_SCR9_FUNC_007 | HomePageTest.heroCtaScrollsToProductsSection |  |
| TC_SCR9_UI_010 | HomePageTest.trustStripShowsAllFourCommitments |  |
| TC_SCR9_FUNC_012 | HomePageTest.categoryIconFiltersProductsAndScrollsToGrid |  |
| TC_SCR9_UI_013 | HomePageTest.brandStripShowsSixBrandsAsPlainTextWithoutNavigation |  |
| TC_SCR9_FUNC_014 | HomePageTest.flashSaleShowsUpToEightProductsOrderedByDiscountDesc |  |
| TC_SCR9_FUNC_016 | HomePageTest.flashSaleHidesWhenCategorySelectedAndReturnsWhenCleared |  |
| TC_SCR9_FUNC_017 | HomePageTest.bestSellerSectionShowsTopEightByRatingThenReviewCount |  |
| TC_SCR9_FUNC_018 | HomePageTest.suggestedSectionShowsRemainingProductsNotInBestSellers |  |
| TC_SCR9_UI_019 | HomePageTest.featuredShopsSectionShowsFourShops |  |
| TC_SCR9_FUNC_020 | HomePageTest.shopCardClearsCategoryFilterAndScrollsToGrid |  |
| TC_SCR10_FUNC_002, TC_SCR10_UI_012 | MembershipTest.membershipTierBadgesAndNextTierThreshold |  |
| TC_SCR10_FUNC_003 | MembershipTest.membershipBenefitsShowCurrentTierEarnRate |  |
| TC_SCR10_FUNC_004 | MembershipTest.membershipBenefitsShowPointsValueAndExpiry |  |
| TC_SCR10_FUNC_005 | MembershipTest.membershipBenefitsShowMaxPointsPercentForCurrentTier |  |
| TC_SCR10_FUNC_006 | MembershipTest.membershipBenefitsShowFreeShipForCurrentTier |  |
| TC_SCR10_FUNC_007 | MembershipTest.membershipPointsBalanceShowsPointsAndConvertedValue |  |
| TC_SCR10_UI_008 | MembershipTest.membershipProgressToNextTierShowsRemainingOrTopTier |  |
| TC_SCR10_UI_010 | MembershipTest.membershipEmptyHistoryShowsEmptyMessageForNewAccount |  |
| TC_SCR10_FUNC_011 | MembershipTest.membershipVouchersListOrEmptyState |  |
| TC_SCR10_UI_017 | MembershipTest.membershipBirthdateInputPrefilledWithSavedDate |  |
| TC_SCR10_VAL_019 | MembershipTest.membershipSaveBirthdateTwiceWithin90DaysShowsError | BD-09 |
| TC_SCR10_VAL_025 | MembershipTest.membershipSaveInvalidBirthdateShowsError |  |
| TC_SCR10_FUNC_020 | MembershipTest.claimBirthdayVoucherSuccessShowsMessageAndNewVoucher | BD-05 |
| TC_SCR10_FUNC_022 | MembershipTest.claimBirthdayVoucherWithoutBirthdateShowsError | BD-06 |
| TC_SCR10_FUNC_023 | MembershipTest.claimBirthdayVoucherOutsideBirthdayWindowShowsError | BD-07 |
| TC_SCR10_FUNC_024 | MembershipTest.claimBirthdayVoucherSecondTimeThisYearShowsError | BD-08 |
| TC_SCR11_UI_002 | NotificationsFooterTest.notificationModalShowsUnreadBadgeCount |  |
| TC_SCR11_FUNC_003 | NotificationsFooterTest.notificationMarkAllReadClearsUnreadDotsAndBadges |  |
| TC_SCR11_UI_004 | NotificationsFooterTest.notificationEmptyStateShowsForNewAccount |  |
| TC_SCR11_FUNC_005 | NotificationsFooterTest.notificationClickItemMarksOnlyThatItemRead |  |
| TC_SCR11_FUNC_006 | NotificationsFooterTest.notificationsRouteOpensModalAndReturnsToHome |  |
| TC_SCR11_FUNC_007, TC_SCR11_FUNC_008, TC_SCR11_FUNC_009, TC_SCR11_FUNC_010, TC_SCR11_FUNC_011, TC_SCR11_FUNC_012, TC_SCR11_FUNC_013, TC_SCR11_FUNC_014, TC_SCR11_FUNC_016, TC_SCR11_FUNC_017, TC_SCR11_FUNC_018, TC_SCR11_FUNC_019, TC_SCR11_FUNC_020, TC_SCR11_FUNC_021 | NotificationsFooterTest.footerLinksNavigateToExpectedRoutes |  |
| TC_SCR11_UI_022 | NotificationsFooterTest.footerShowsBrandHotlineAndBottomBar |  |
| TC_SCR11_UI_026 | NotificationsFooterTest.themeToggleShowsCorrectLabelAndTitle |  |
| TC_SCR11_FUNC_027 | NotificationsFooterTest.darkModePersistsInLocalStorageAfterReload |  |
| TC_SCR11_FUNC_028 | NotificationsFooterTest.darkModeToggleChangesBackgroundColor |  |
| TC_SCR11_FUNC_031 | NotificationsFooterTest.notFoundPageButtonsGoHomeAndBack |  |
| TC_SCR8_FUNC_004 | OrderCancellationTest.filterOrdersByPendingPaymentTab |  |
| TC_SCR8_UI_012 | OrderCancellationTest.ordersPaginationShowsTenPerPage |  |
| TC_SCR8_UI_019 | OrderCancellationTest.freshAccountSeesEmptyOrdersState |  |
| TC_SCR8_FUNC_014 | OrderCancellationTest.payNowButtonOnlyOnPendingPaymentOrder |  |
| TC_SCR8_FUNC_015 | OrderCancellationTest.reorderCancelledOrderAddsItemsBackToCart |  |
| TC_SCR8_UI_013 | OrderCancellationTest.orderCardShowsAtMostThreeItemsWithRemainderLine |  |
| TC_SCR8_UI_017 | OrderCancellationTest.orderDetailShowsTimelineShippingInfoAndMoneyTable |  |
| TC_SCR8_FUNC_018 | OrderCancellationTest.orderDetailMatchesPlacedOrderData |  |
| TC_SCR8_UI_021 | OrderCancellationTest.cancelDialogShowsCorrectContent |  |
| TC_SCR8_FUNC_022, TC_SCR8_FUNC_023, TC_SCR8_FUNC_024, TC_SCR8_FUNC_025 | OrderCancellationTest.cancelOrderWithEachPresetReasonShowsReasonInDetail |  |
| TC_SCR8_FUNC_026 | OrderCancellationTest.cancelOrderWithCustomReasonShowsCustomReasonInDetail |  |
| TC_SCR8_FUNC_028 | OrderCancellationTest.dismissCancelDialogKeepsOrderCancellable |  |
| TC_SCR8_VAL_027 | OrderCancellationTest.customCancelReasonEmptyShowsError |  |
| TC_SCR5_UI_004 | ProductDetailTest.trustBadgesDisplayedUnderGallery |  |
| TC_SCR5_FUNC_005 | ProductDetailTest.variantSectionHasOnlyOneDefaultOption |  |
| TC_SCR5_UI_006 | ProductDetailTest.variantOptionDisabledWhenOutOfStock |  |
| TC_SCR5_FUNC_013 | ProductDetailTest.buyNowAsGuestGoesToLoginWithReturnRoute |  |
| TC_SCR5_FUNC_015 | ProductDetailTest.reviewsSectionShowsSummaryDistributionAndItems | RV-09 |
| TC_SCR5_FUNC_016 | ProductDetailTest.reviewFormDisplayedOnProductWithoutReviews | RV-10 |
| TC_SCR5_VAL_023 | ProductDetailTest.submitReviewWithTooLongCommentShowsError | RV-05 |
| TC_SCR5_FUNC_024 | ProductDetailTest.submitReviewSuccessfullyWhenLoggedIn | RV-06 |
| TC_SCR5_FUNC_025 | ProductDetailTest.guestSubmitReviewWithoutNameDefaultsToGuestName | RV-07 |
| TC_SCR5_FUNC_028 | ProductDetailTest.averageRatingAndCountUpdateAfterSubmittingReview | RV-08 |
| TC_SCR3_VAL_003, TC_SCR3_VAL_004 | ProfileAndAddressTest.profileNameTooShortOrTooLongShowsError |  |
| TC_SCR3_VAL_005 | ProfileAndAddressTest.profilePhoneInvalidFormatShowsError |  |
| TC_SCR3_VAL_006 | ProfileAndAddressTest.profilePhoneCanBeLeftEmptyAndSaved |  |
| TC_SCR3_VAL_008 | ProfileAndAddressTest.changePasswordSameAsCurrentShowsError |  |
| TC_SCR3_VAL_009 | ProfileAndAddressTest.changePasswordWeakNewPasswordShowsError |  |
| TC_SCR3_VAL_010 | ProfileAndAddressTest.changePasswordMismatchedConfirmShowsError |  |
| TC_SCR3_VAL_011 | ProfileAndAddressTest.addressFormMissingOrInvalidFieldsShowFieldErrors |  |
| TC_SCR3_VAL_012 | ProfileAndAddressTest.addressFormMissingLocationShowsLocationErrors |  |
| TC_SCR3_FUNC_020 | ProfileAndAddressTest.cancelDeleteAddressKeepsAddress |  |

## Kiểm thử hiệu năng (JMeter) — đã đổi sang JWT

`performance/dafl-loadtest.jmx`: bỏ phụ thuộc cookie `sid` (Cookie Manager đã tắt), sampler đăng nhập trích `$.data.token` bằng JSON Post Processor và Header Manager gắn `Authorization: Bearer ${jwtToken}` cho toàn bộ sampler cần xác thực; body giỏ/checkout không còn `userId`, checkout gửi kèm khối `shipping` lồng nhau + `paymentMethod`. **Ngưỡng Quality Gate giữ nguyên: lỗi ≤ 2%, thời gian phản hồi TB ≤ 1000 ms.**

## Đợt 16 — Quản lý đánh giá admin (RVM-01..08)

Mục "Đánh giá" thứ 9 trên Admin riêng (`http://localhost:3001`, lớp `admin/reviews/AdminReviewTest`, 8 test): bảng STT/Sản phẩm/Khách/Sao/Nội dung (cắt 80 ký tự)/Ngày (đợt 17 thêm cột STT), lọc theo số sao + tìm kiếm, nút Export CSV, phân trang 10 dòng/trang, modal xem chi tiết + nút Xóa qua hộp xác nhận kiểu Hình 5 (tiêu đề 'Bạn có chắc muốn xóa không?' + 'Thao tác này không thể hoàn tác.'); không có nút sửa/tạo. Backend: `GET /api/admin/evaluations` (lọc productId/rating/search, phân trang) + `DELETE /api/admin/evaluations/{id}` (chỉ SuperAdmin, xóa CỨNG vì entity `Evaluation` không có cờ ẩn/xóa, xóa xong tính lại Rating = trung bình làm tròn 1 số thập phân, 0 đánh giá → Rating = 0, ghi AuditLog). Chạy bằng `run_test_admin.bat` / `run_test_admin_allure.bat` (chỉ :3001, không cần :3000).

| Mã test | Mô tả | Ghi chú dữ liệu |
|---|---|---|
| RVM-01 | Mục "Đánh giá" hiện trong menu admin + bảng tải được, đủ 7 cột (STT đầu tiên, rồi Sản phẩm/Khách/Sao/Nội dung/Ngày) | Chạy khi đã seed tài khoản admin |
| RVM-02 | Tìm kiếm theo tên sản phẩm/khách lọc đúng dòng | Skip nếu bảng trống (không có đánh giá nào) |
| RVM-03 | Lọc theo số sao 5 sao chỉ hiện đánh giá 5 sao | Skip nếu không có đánh giá 5 sao |
| RVM-04 | Mở modal xem chi tiết 1 đánh giá (đủ tên SP/khách/sao/nội dung đầy đủ/ngày), đóng được | Nội dung trong modal hiện đủ, không bị cắt |
| RVM-05 | Nội dung dài bị cắt 80 ký tự trong bảng | Cần 1 đánh giá có nội dung > 80 ký tự, nếu không có thì Skip |
| RVM-06 | Xóa 1 đánh giá qua hộp xác nhận kiểu Hình 5 (khẳng định tiêu đề "Bạn có chắc muốn xóa không?" + "không thể hoàn tác", bấm xác nhận → dòng biến mất khỏi bảng) | Chỉ chạy khi ≥ 2 đánh giá; **xóa 1 đánh giá thật, không khôi phục được** (xóa cứng + tính lại Rating SP) |
| RVM-07 | Nút Export bấm được, trang admin vẫn đứng yên | Chỉ kiểm tra nút bấm được, không assert nội dung file (xuất CSV client-side, giống ADM-10) |
| RVM-08 | Phân trang 10 dòng/trang: trang 1 tối đa 10 dòng, bấm 'Sau' sang trang 2 rồi 'Trước' về trang 1 | Số trang theo tổng thật; skip nếu ≤ 10 dòng (giống ADM-12) |

> **Tổng mới (đợt 17): web 275 / admin 63 / tổng 338 test / 28 lớp (web 17 + admin 11).** Lưu ý: RVM-06 là test duy nhất trong lớp đụng dữ liệu thật (xóa 1 đánh giá có sẵn, không khôi phục được — giống ACR-06/07 đổi/hủy đơn có sẵn); các test còn lại chỉ đọc. Tên phương thức trong `reviews/AdminReviewTest` map 1-1 theo mã RVM-01..08.

## Đợt 17 — Giao diện + nghiệp vụ admin mới (ADM17-01..17) + chia thư mục test admin theo phân hệ

Giao diện admin mới (SRS kiểu Vietcombank): form thêm/sửa thành **trang riêng 2 cột** (`sa-form-page`, `role="dialog"`, không overlay, bảng danh sách bị thay khỏi DOM khi form mở, autofocus ô đầu tiên, container giữ testid cũ `admin-*-form-modal`); mọi bảng thêm **cột STT** (liên tục theo trang: trang 2 bắt đầu 11) + **cột Mã** (`admin-product-code` / `admin-category-code` / `admin-customer-code`); cột Trạng thái dạng **Switch** (`<button role="switch" aria-checked="true|false" data-testid="admin-<loai>-status-switch-{id}">`, chỉ núm gạt, không nhãn chữ; bấm mở popup "Xác nhận đổi trạng thái", Hủy → không gọi API; voucher Active→Disabled mở modal nhập lý do bắt buộc `admin-promo-disable-reason`); **dropdown Trạng thái** ở toolbar (`admin-status-filter`: SP/DM `""`/`true`/`false` lọc client-side, voucher `""`/`Draft`/`Active`/`Disabled`/`Expired` lọc server-side nên phải bấm nút "Lọc", khách `""`/`active`/`locked`); **popup chặn xóa** dòng đang Hoạt động (`admin-delete-blocked-modal`: "Không thể xoá {tên} đang hoạt động. Vui lòng chuyển trạng thái sang Ngừng hoạt động trước." + nút "Chuyển sang Ngừng hoạt động" / "Đóng"); popup xóa kiểu Hình 5 (tiêu đề "Bạn có chắc muốn xóa không?" + "Thao tác này không thể hoàn tác."); topbar có **chip tài khoản** (`admin-user-menu-btn`) → dropdown (`admin-user-menu`: "Thông tin tài khoản" / "Đăng xuất") → **trang Hồ sơ** (`admin-profile-page`: sửa họ tên, đổi mật khẩu). Chuỗi validate nguyên văn: trường bắt buộc trống → "Vui lòng bổ sung thông tin bắt buộc."; email sai → "Email không đúng định dạng."; SĐT sai → "Số điện thoại không đúng định dạng."; trùng mã → **cả toast cả message đỏ inline dưới ô Mã**, cùng nội dung: SP "Mã sản phẩm đã tồn tại.", DM "Mã danh mục đã tồn tại.", voucher "Mã khuyến mãi đã tồn tại (không phân biệt hoa thường).". Ô Mã bị disable khi sửa (SP/DM/voucher); mã + email khách luôn disable. Mã để trống tự sinh (SP-/DM-; KH-xxxx khi đăng ký).

**Chia thư mục test admin (đợt 17, số test không đổi trong bước chia):** 6 lớp phẳng cũ (`AdminAndNewsletterTest` 14, `AdminDetailTest` 9, `AdminCrudTest` 8, `AdminWave13Test` 4, `AdminUploadTest` 3, `AdminReviewTest` 8) tách thành 11 lớp theo phân hệ — `catalog/` (AdminCatalogBrowseTest 9, AdminProductTest 11, AdminCategoryTest 4, AdminUploadTest 3), `promotions/` (AdminPromoTest 4), `orders/` (AdminOrderTest 7), `customers/` (AdminCustomerTest 6), `reviews/` (AdminReviewTest 8), `system/` (AdminAccessTest 6, AdminProfileTest 2, AdminWave13Test 3). Mỗi lớp chỉ lo 1 phân hệ; giữ nguyên mọi test ID + logic kiểm chứng. Cập nhật kèm: `testng-admin.xml` + `testng.xml` (11 FQCN mới), `run_test_single.bat` (cơ chế tự suy FQCN từ đường dẫn file đã có sẵn cho web — dùng được luôn cho admin, chỉ cập nhật danh sách lớp trong thông báo NOCLASS + ví dụ; vẫn gõ tên lớp trần như `AdminProductTest admin`).

| Mã test | Lớp.phương thức | Mô tả / khẳng định chính | Ghi chú dữ liệu |
|---|---|---|---|
| ADM17-01 | catalog.AdminProductTest.adminCreateProductDuplicateCodeShowsToastAndInlineError | Tạo SP mã `DUP-<timestamp>` rồi tạo SP khác cùng mã → toast + message đỏ dưới ô Mã (`admin-product-code-error`) đều ghi "Mã sản phẩm đã tồn tại."; đóng form (Hủy, xác nhận "Thoát" nếu form dirty) | Xóa SP thứ nhất sau test; SP thứ hai không được tạo |
| ADM17-02 | catalog.AdminCategoryTest.adminCreateCategoryDuplicateCodeShowsToastAndInlineError | Như ADM17-01 cho danh mục, chuỗi "Mã danh mục đã tồn tại." | Xóa danh mục thứ nhất sau test |
| ADM17-03 | promotions.AdminPromoTest.adminCreatePromoDuplicateCodeShowsToastAndInlineError | Tạo voucher mã `DUP<timestamp>` rồi tạo voucher khác nhập mã thường (`toLowerCase`) → toast + message đỏ dưới ô Mã đều ghi "Mã khuyến mãi đã tồn tại (không phân biệt hoa thường)." (trùng mã không phân biệt hoa thường) | Xóa voucher thứ nhất sau test (voucher Draft mới xóa được) |
| ADM17-04 | catalog.AdminProductTest.adminEditProductCodeInputIsDisabled | Mở form sửa SP vừa tạo → ô Mã (`admin-product-code-input`) bị disable, ô Tên vẫn sửa được, lưu → tên mới hiện ở bảng | Tự xóa SP sau test |
| ADM17-05 | catalog.AdminCategoryTest.adminEditCategoryCodeInputIsDisabled | Như ADM17-04 cho danh mục (ô Mã disable, tên sửa được) | Tự xóa danh mục sau test |
| ADM17-06 | customers.AdminCustomerTest.adminEditCustomerCodeAndEmailAreDisabled | Mở form sửa khách demo → ô Mã (`admin-customer-code-input`) + ô Email đều disable; chỉ kiểm tra + bấm Hủy, **không bấm Lưu** | **Đợt 18 cập nhật ghi chú: `PUT /api/admin/customers/{id}` đã có từ đợt 17** (ghi chú cũ "chưa có PUT" đã lỗi thời) — test vẫn chỉ mở form kiểm tra, không lưu để không đổi dữ liệu demo |
| ADM17-07 | catalog.AdminProductTest.adminDeleteActiveProductShowsBlockedPopupAndConvertDeletes | Bấm Xóa trên SP đang Hoạt động → popup chặn `admin-delete-blocked-modal` đúng tiêu đề + nội dung "Không thể xoá {tên} đang hoạt động..." → bấm "Chuyển sang Ngừng hoạt động" → popup xác nhận đổi trạng thái → hộp xác nhận xóa (Hình 5) → SP biến mất khỏi bảng | Tự tạo SP, tự dọn |
| ADM17-08 | catalog.AdminProductTest.adminDeleteBlockedPopupCloseKeepsProduct | Như ADM17-07 nhưng bấm "Đóng" ở popup chặn → SP vẫn còn trong bảng (tìm lại thấy) | Tự tạo SP, tự xóa sau test |
| ADM17-09 | catalog.AdminProductTest.adminRowStatusSwitchConfirmAndCancel | Gạt Switch của SP: bấm Hủy ở popup "Xác nhận đổi trạng thái" → `aria-checked` giữ nguyên (không gọi API); bấm Xác nhận → trạng thái đổi (aria-checked đảo); gạt lại về trạng thái ban đầu | Tự tạo SP, tự xóa sau test |
| ADM17-10 | promotions.AdminPromoTest.adminPromoSwitchToDisabledRequiresReason | Voucher Active: gạt Switch → modal nhập lý do (`admin-promo-disable-reason`) hiện; để trống bấm xác nhận → lỗi đỏ "Vui lòng bổ sung thông tin bắt buộc." (`admin-promo-disable-reason-error`); nhập lý do → thành Disabled (Switch tắt, dòng hiện lý do) | Voucher Active để lại ở trạng thái Disabled (vô hiệu) |
| ADM17-11 | catalog.AdminProductTest.adminStatusFilterShowsOnlyInactiveThenAll | Dropdown `admin-status-filter` chọn `false` (Ngừng hoạt động) → bảng chỉ còn dòng ngừng hoạt động (mọi Switch tắt); chọn `""` (Tất cả) → bảng trở lại (cả dòng đang hoạt động) | Cần ít nhất 1 SP ngừng hoạt động + 1 SP hoạt động, nếu không thì Skip |
| ADM17-12 | system.AdminProfileTest.adminUserMenuOpensProfileAndEditNameUpdatesTopbar | Bấm chip tài khoản → dropdown hiện 2 mục → "Thông tin tài khoản" → trang Hồ sơ hiện đúng tên admin → sửa họ tên + lưu → toast + tên mới hiện ở topbar; khôi phục tên cũ trong `finally` | Đổi tên admin thật rồi khôi phục ngay trong test |
| ADM17-13 | system.AdminProfileTest.adminChangePasswordWrongCurrentShowsErrorThenRestore | Mở form đổi mật khẩu: nhập sai mật khẩu hiện tại → lỗi inline "Mật khẩu hiện tại không đúng" (poll tối đa 5s); nhập đúng → đổi sang mật khẩu tạm `Admin@456` → app về `/login` → đăng nhập lại bằng mật khẩu tạm → đổi lại về `Admin@123` → đăng nhập lại | **Đổi mật khẩu admin thật, tự khôi phục trong `finally` (kể cả khi test fail giữa chừng). Rủi ro còn lại: nếu JVM/suite chết đúng giữa lúc mật khẩu đang là tạm thì mật khẩu admin kẹt ở `Admin@456` — khôi phục bằng tay: đăng nhập bằng `Admin@456` rồi đổi lại `Admin@123` ở trang Hồ sơ.** |
| ADM17-14 | catalog.AdminProductTest.adminProductTableSttContinuesOnPage2 | Bảng SP có > 10 dòng → sang trang 2, STT dòng đầu = 11 | Skip nếu ≤ 10 dòng |
| ADM17-15 | catalog.AdminProductTest.adminProductCodeAutoGeneratedWithPrefix | Tạo SP để trống ô Mã → dòng mới trong bảng có mã bắt đầu `SP-` | Tự xóa SP sau test |
| ADM17-16 | catalog.AdminCategoryTest.adminCategoryCodeAutoGeneratedWithPrefix | Tạo danh mục để trống ô Mã → dòng mới có mã bắt đầu `DM-` | Tự xóa danh mục sau test |
| ADM17-17 | customers.AdminCustomerTest.adminCustomerCodeFormatShownInTable | Dòng khách demo trong bảng có mã bắt đầu `KH-` (ô `admin-customer-code`) | Skip nếu mã demo hiển thị "—" (khách cũ chưa có mã) |

> **Tổng mới (đợt 17): web 275 / admin 63 / tổng 338 test / 28 lớp (web 17 + admin 11).** Cập nhật kèm trong đợt 17 (test cũ): ADM-03 thấy đủ 9 mục menu (thêm mục Đánh giá); ADM-13 + ACR-01/02/04/05 + UP-01..03 theo form trang riêng 2 cột (không còn Drawer/Modal); ACR-03 xóa SP mới tạo đi qua popup chặn (SP mới đang Hoạt động); ACR-05 tắt voucher bằng Switch + modal nhập lý do (không còn nút "Tắt khuyến mãi" riêng); ADT-02 khẳng định thêm dòng "Trạng thái" trong modal chi tiết danh mục; ADT-08 mở form sửa từ modal chi tiết (form trang riêng); RVM-01 bảng có 7 cột (STT đầu tiên); RVM-06 xóa qua hộp xác nhận kiểu Hình 5. Test đổi mật khẩu admin (ADM17-13) là test đầu tiên trong bộ tự đổi mật khẩu admin rồi khôi phục — ghi chú "Không viết test đổi mật khẩu admin tự khôi phục" ở các đợt trước nay đã thay bằng test này với cơ chế khôi phục trong `finally`.

> **Đợt 17.1 (tinh chỉnh giao diện admin, số test không đổi: web 275 / admin 63 / tổng 338):** menu sidebar bỏ hết icon (chỉ chữ); nút "+ Thêm mới" chuyển sang cuối nhóm phải toolbar (sau Export); trang Hồ sơ + 4 form trang riêng không còn tiêu đề trang trùng lặp (pagehead ẩn khi form mở); form/hồ sơ rộng 1180px, tiêu đề form 1.5rem; Trả hàng và Nhật ký hệ thống tách thành 2 mục menu riêng (`admin-nav-returns` / `admin-nav-logs`, tab `logs` mới, audit-log tải riêng khi mở tab). Cập nhật test cũ: ADM-03 thấy đủ 10 mục menu (đổi tên `adminSeesTenMenuItems`); ADM-W13-04 mở tab `logs` (heading audit-log giờ là `<h1>` pagehead); Vitest tách test "Trả hàng / Nhật ký" thành 2 test riêng (trả hàng + nhật ký).
> Đợt 18 wave D: xem mục "Selenium admin đợt 18 wave D" ở cuối bảng ánh xạ (menu phẳng, form chỉ Drawer rộng, popup spec A–H: D2/D3, F2/F3, G3, B1 bộ đếm 300, XOA không phân biệt hoa thường; 2 testid trùng đã đổi tên trong code web; +4 test Selenium mới ADM18-14..16 + RVM-09).

## Đợt 18 — Redesign admin (ConfirmDialog, view mode, menu nhóm, bỏ Export) + bản vá 18.1

Giao diện admin làm lại theo 3 prompt của Matthew (2026-11-10): **form về Drawer 560–640px (SP) / Modal tối đa 560px (DM/KM/KH)** thay form trang riêng 2 cột đợt 17 (bảng danh sách vẫn ở lại trong DOM khi form mở); **mọi form có 3 mode** `create|edit|view` (`data-form-mode`, container `admin-*-form-modal` / `admin-customer-form-page`); view mode: mọi ô readonly/disabled, trạng thái hiện badge, trống hiển thị `—`, footer chỉ `[Đóng]` + `[Chỉnh sửa]`, bấm `[Chỉnh sửa]` sang edit giữ nguyên dữ liệu; dirty-close qua ConfirmDialog `[Ở lại]` + `[Thoát]`; **ConfirmDialog + `confirmAction`** thay mọi popup cũ (testid `confirm-dialog`, `confirm-dialog-title/message/reason-input/reason-error/type-input/confirm-btn/cancel-btn/error`; footer **chỉ đúng 2 nút, không bao giờ 3** — chặn xóa `[Hủy]` + `[Chuyển sang Ngừng hoạt động]`, khóa KH / tắt voucher / hủy đơn bắt buộc lý do, xóa có dòng `Hành động này không thể hoàn tác.`); **toast service `notify` + `messages.vi`** thay banner inline `.sa-notice` (tối đa 3 toast, chống trùng, tự ẩn 4s/6s/8s, tiền hiển thị `99.000₫`); **PageToolbar chuẩn** (`{prefix}-search-input/-search-btn/-reset-btn/-add-btn`, Thêm mới cuối phải, không nút Làm mới); **bỏ hẳn Export** khỏi mọi toolbar admin (8 nút + `exportCurrentTab`/`downloadCsv`); **menu gom nhóm dropdown** (Tổng quan trên cùng không tiêu đề nhóm → BÁN HÀNG → SẢN PHẨM → KHÁCH HÀNG → HỆ THỐNG; tiêu đề nhóm là `<button>` có `aria-expanded` + chevron, mặc định mở hết, nhớ trạng thái `localStorage` key `dafl_admin_nav_groups`, nhóm chứa mục active tự mở; badge cam nhạt 12px tối đa `99+`, ẩn khi 0: Đơn = chờ xác nhận từ `GET /api/admin/dashboard` `pendingOrders`, Trả = đếm `Pending` trong list trả hàng, Đánh giá = tạo trong 7 ngày qua — Evaluation **không có cờ đã xử lý**); **dashboard mới** là component riêng (h1 `Tổng quan`, KPI + khối `Cần xử lý ngay`, không còn biểu đồ cũ/nút 7-30 ngày); **phân trang chung** (`Hiển thị {from}–{to} / {total} kết quả` + `Trang X / Y`, không nút số trang); **EmptyState** không dấu chấm cuối câu (`Chưa có sản phẩm nào`, `Không tìm thấy đơn hàng`...); **footer chi tiết đơn có điều kiện** (hủy được: `[Hủy đơn]`+`[Đổi trạng thái]`; không hủy được: `[Đóng]`+`[Đổi trạng thái]`; terminal: chỉ `[Đóng]`); **modal chi tiết trả hàng** chỉ `[Từ chối]`+`[Duyệt]` (đóng X/ESC, không nút Hủy); **đổi mật khẩu** sang FormShell modal `[Hủy]`+`[Đổi mật khẩu]`; **form xem KH không còn** số dư điểm chi tiết/đơn gần đây (điểm vẫn có ở cột Điểm trong bảng).

**Vitest admin đợt 18: 119 test / 6 file, 119 pass / 0 fail** (trước đợt 18: 92 test, 47 fail / 45 pass). Viết lại `AdminPage.test.tsx` (37 test: toolbar mới, form Drawer/Modal + view mode, ConfirmDialog, toast, phân trang mới, dashboard mới), `AdminDot17.test.tsx` (21 test: Switch + chặn xóa + trùng mã theo ConfirmDialog), `AdminReviews.test.tsx` (6 test: toolbar mới, xóa qua ConfirmDialog, phân trang mới); **mới `AdminDot18.test.tsx` (28 test)**: messages.vi (`fmt`, `httpErrorMessage` mapping §4, `shortName`), ConfirmDialog component (đúng 2 nút, autofocus Cancel, `requireReason`, `requireTypeToConfirm` với `typeExpected "XOA"`, ESC, loading), ToastHost + notify (tối đa 3, chống trùng, tự ẩn đúng thời gian, fake timers), menu nhóm + badge (thu/mở, localStorage, badge tổng khi thu gọn, ẩn khi 0, `99+` khi > 99, đánh giá mới = 7 ngày, mục active tự mở nhóm), form xem khuyến mãi, footer đơn có điều kiện, modal trả hàng, modal đổi mật khẩu, hội viên (lưu hạng + điều chỉnh điểm qua ConfirmDialog). Giữ nguyên `AdminDashboard.test.tsx` (21 test, dashboard mới W5) + `AdminApp.test.tsx` (6 test). **Vitest web không đụng: 136/136 pass.** `npm run build:admin` PASS.

**Bản vá 18.1 (Matthew yêu cầu sau khi áp đợt 18 lên Acer; main agent sửa trực tiếp trong cây fork-check, W4 chỉ viết test theo trạng thái mới):** (1) MENU — mục đang chọn bị khung viền xanh mặc định của trình duyệt: `AdminPage.css` thêm `.sa-nav-btn:focus { outline: none; }` + `.sa-nav-btn:focus-visible { outline: 2px solid var(--sa-green-bright); outline-offset: 1px; }` (bấm chuột không còn viền, chỉ đi bằng bàn phím mới hiện viền xanh chanh). Test Vitest mới (4 test, trong `AdminDot18.test.tsx` mục F): assert 2 rule CSS tồn tại đúng nội dung trong `AdminPage.css` (đọc file source — **giới hạn jsdom: jsdom không áp stylesheet vào computed style nên không đo được outline thật sau click/focus**; test bổ sung ở mức làm được: nút menu mang class `.sa-nav-btn`, bấm chuột điều hướng đúng tab, focus bàn phím đặt đúng `activeElement`, không sinh inline outline; kiểm tra bằng mắt trên Acer vẫn cần làm sau rebuild). (2) DASHBOARD FULL MÀN HÌNH — `dashboard.css` đổi `.sa-dashboard` từ `max-width: 1440px; margin: 0 auto;` thành `max-width: none; margin: 0;` (ghi đè prompt dashboard cũ, theo yêu cầu trực tiếp của Matthew). Test Vitest mới (3 test, trong `AdminDashboard.test.tsx` mục "full màn hình"): assert rule `.sa-dashboard` chứa `max-width: none` + `margin: 0`, không còn khai báo `max-width: 1440px` nào, container `admin-dashboard` mang class `.sa-dashboard` và không bị inline style giới hạn bề rộng, KPI/biểu đồ/bảng đều render trong container đó (**giới hạn jsdom tương tự: không đo được chiều rộng thật/khoảng lề 2 bên ở màn rộng — kiểm tra bằng mắt trên Acer**). Không có test cũ nào còn kỳ vọng dashboard 1440px căn giữa nên không phải sửa thêm. Sau bản vá 18.1: **Vitest admin 126 test / 6 file, 126 pass / 0 fail** (119 + 7 test mới).

**Selenium admin đợt 18 wave D: 79 test / 12 lớp** (trước wave D: 75 test — **+4 test mới, 0 test xóa**). Cập nhật `AdminPageUI.java` + `AdminPageObject.java` theo UI wave D (menu PHẲNG: xóa locator/hàm badge tổng nhóm + dropdown; form CHỈ Drawer rộng: `isFormDrawer` + `getFormDrawerWidth` qua `--fs-width`, xóa `isFormModal`; ConfirmDialog thêm `confirm-dialog-reason-counter` / `confirm-dialog-link`; reviewStars icon Lucide; toolbar icon Lucide; testid mới `admin-promo-view-edit-btn` / `admin-order-detail-footer-close-btn`). Sửa PO bắt buộc theo spec A–H: `changeOrderStatusTo` chấp nhận D2 `Xác nhận đơn?` cho đích `Đã xác nhận` (ACR-06 trước đây assert sai `Cập nhật trạng thái?` sẽ fail), `adjustPointsForUser` chấp nhận F2 `Xác nhận cộng điểm?` / F3 `Xác nhận trừ điểm?` theo dấu số điểm (ACR-08 trước đây assert sai `Xác nhận điều chỉnh điểm?` sẽ fail). `testng-admin.xml` + `testng.xml` giữ nguyên 12 lớp (không thêm lớp mới); echo số lượng trong `run_test_admin.bat` + `run_test_admin_allure.bat` cập nhật 75 → 79 test (giữ CRLF). `javac` toàn bộ Java PASS; mọi class trong 3 file `testng-*.xml` trỏ đúng file thật; grep `"3000"` trong test admin = 0.

| Mã test | Lớp.phương thức | Mô tả / khẳng định chính | Ghi chú dữ liệu |
|---|---|---|---|
| ADM18-01 | system.AdminDot18Test.confirmDialogCancelDeleteKeepsProduct | Xóa SP đã Ngừng hoạt động → ConfirmDialog `Xóa sản phẩm?` (đúng 2 nút, có dòng `không thể hoàn tác.`) → bấm `[Hủy]` thì SP vẫn còn, không gọi API xóa, không có toast mới | Tự tạo SP, tự xóa sau test |
| ADM18-02 | system.AdminDot18Test.confirmDialogConfirmDeleteRemovesProduct | Như ADM18-01 nhưng bấm `[Xóa sản phẩm]` → SP biến mất + toast `Đã xóa sản phẩm` | Tự tạo SP, đã xóa trong test |
| ADM18-03 | system.AdminDot18Test.lockCustomerRequiresReason | Khóa tài khoản demo → ConfirmDialog `Khóa tài khoản?` có ô lý do + bộ đếm `0/300` → gõ `Spam` thành `4/300`; xác nhận khi để trống → lỗi `Vui lòng nhập lý do khóa.`, dialog vẫn mở, Switch vẫn bật; bấm `[Hủy]` thì không khóa | **KHÔNG khóa thật** — chỉ mở dialog rồi Hủy |
| ADM18-04 | system.AdminDot18Test.navBadgesFormatAndNavigate | Badge Đơn/Trả/Đánh giá (nếu hiện) chỉ là số hoặc `99+`, ẩn khi = 0; bấm từng mục điều hướng đúng tab | Chỉ đọc |
| ADM18-05 | system.AdminDot18Test.navFlatMenuGroupTitlesAreNonClickableDivs | **Viết lại wave D (xóa test dropdown cũ):** tiêu đề nhóm là `<div>` không bấm được, tên dạng tiêu đề (`Bán hàng`/`Quản lý sản phẩm`/`Quản lý khách hàng`/`Hệ thống`), không aria-expanded/chevron, không badge tổng nhóm; cả 10 mục luôn hiện; bấm `Xem` đơn chờ xác nhận ở dashboard đi thẳng tab Đơn hàng | Chỉ đọc; phần vào Đơn từ dashboard skip nếu không có đơn chờ xác nhận (nút `Xem` bị tắt) |
| ADM18-06 | system.AdminDot18Test.productViewModeIsReadonlyAndEditKeepsData | Form xem SP: mode `view`, tên readonly, badge `Đang bán`, footer chỉ `[Đóng]`+`[Chỉnh sửa]`; bấm `[Chỉnh sửa]` sang edit giữ nguyên tên; đóng không lưu; **Drawer rộng 760px** | Tự tạo SP, tự xóa sau test |
| ADM18-07 | system.AdminDot18Test.categoryViewModeIsReadonlyAndEditKeepsData | Như ADM18-06 cho danh mục (badge `Hiển thị`); **Drawer rộng 680px** | Tự tạo DM, tự xóa sau test |
| ADM18-08 | system.AdminDot18Test.promoViewModeIsReadonlyAndEditKeepsData | Form xem voucher: mode `view`, tiêu đề `Chi tiết khuyến mãi <mã>`, mã readonly, badge `Nháp`, footer chỉ `[Đóng]`+`[Chỉnh sửa]`; sang edit mã vẫn readonly; **Drawer rộng 680px** | Tự tạo voucher Draft, tự xóa sau test |
| ADM18-09 | system.AdminDot18Test.customerViewModeIsReadonlyWithoutRecentOrders | Form xem KH demo: mode `view`, tên readonly, footer chỉ `[Đóng]`+`[Chỉnh sửa]`, KHÔNG có mục đơn gần đây; đóng bằng `[Đóng]`; **Drawer rộng 680px** | Chỉ đọc (khách demo) |
| ADM18-10 | system.AdminDot18Test.orderDetailFooterDependsOnStatus | Footer chi tiết đơn có điều kiện: đơn `Chờ xử lý` đúng 2 nút `[Hủy đơn]`+`[Đổi trạng thái]`; đơn `Hoàn tất` (terminal) chỉ 1 nút `[Đóng]` (testid riêng `admin-order-detail-footer-close-btn`), đóng bằng chính nút footer này | Chỉ đọc; skip phần nào không có đơn tương ứng |
| ADM18-11 | system.AdminDot18Test.changePasswordModalHasExactlyTwoFooterButtons | Drawer đổi mật khẩu (FormShell): footer chỉ đúng 2 nút `[Hủy]`+`[Đổi mật khẩu]`; đóng bằng nút X, không đổi mật khẩu | Chỉ mở drawer rồi đóng |
| ADM18-12 | system.AdminDot18Test.noExportButtonOnAnyAdminToolbar | KHÔNG còn nút Export ở toolbar Sản phẩm, Đơn hàng, Đánh giá, Nhật ký hệ thống | Chỉ đọc |
| ADM18-13 | system.AdminDot18Test.cancelOrderRequiresReason | Hủy đơn `Chờ xử lý` có sẵn: mở popup `Hủy đơn hàng?` (nút trái D1 `[Giữ đơn hàng]`), xác nhận khi để trống lý do → lỗi `Vui lòng nhập lý do hủy đơn.`, dialog vẫn mở; bấm `[Giữ đơn hàng]` thì đơn vẫn `Chờ xử lý` | **KHÔNG hủy thật**; skip nếu không có đơn chờ xử lý |
| ADM18-14 | system.AdminDot18Test.orderStatusConfirmPopupsD2AndD3CancelSafely | **MỚI wave D:** đơn `Chờ xử lý` → đích `Đã xác nhận` mở popup D2 `Xác nhận đơn?` `[Hủy][Xác nhận đơn]`; đích `Đã thanh toán` mở popup D3 `Cập nhật trạng thái?` `[Hủy][Cập nhật]`; bấm Hủy xong đơn vẫn `Chờ xử lý` | **KHÔNG đổi thật** — chỉ mở popup rồi Hủy; skip nếu không có đơn chờ xử lý |
| ADM18-15 | system.AdminDot18Test.logoutAsksConfirmDialogBeforeLeaving | **MỚI wave D:** bấm `Đăng xuất` ở menu tài khoản → popup G3 `Đăng xuất?` `[Hủy][Đăng xuất]`; bấm Hủy thì vẫn ở trang admin; bấm lại rồi xác nhận thì về trang `/login` | Đăng xuất thật ở cuối test (mỗi test tự đăng nhập lại) |
| ADM18-16 | system.AdminDot18Test.adminToolbarUsesLucideIconsNotEmoji | **MỚI wave D:** toolbar Sản phẩm — nút Tìm chứa `svg.lucide-search`, nút Đặt lại chứa `svg.lucide-rotate-ccw`; toàn bộ toolbar không còn emoji 🔍/↺ | Chỉ đọc |
| RVM-09 | reviews.AdminReviewTest.reviewStarsRenderAsLucideIcons | **MỚI wave D:** cột Sao dùng icon Star Lucide — dòng đầu có đúng 5 `svg.lucide-star`, số sao đầy (fill `#f59e0b`) bằng rating, aria-label `{n} trên 5 sao`, không còn ký tự `★`; modal chi tiết cũng có đúng số sao đầy | Chỉ đọc; skip nếu bảng trống |

**Test đã XÓA đợt 18:** `RVM-07` (`reviews.AdminReviewTest.exportReviewsButtonClickableWithoutError` — Export CSV đánh giá; Export bỏ hẳn khỏi mọi toolbar admin) + **phần Export trong ADM-W13-04** (`system.AdminWave13Test` — chỉ giữ phần lọc ngày, thêm khẳng định không có nút Export ở toolbar Nhật ký) + **ADM-10 Export đơn hàng viết lại** thành `adminOrdersToolbarHasNoExportButton` (khẳng định KHÔNG có nút Export) + test dashboard cũ trong `AdminPage.test.tsx` (`Doanh thu: Super Admin thấy biểu đồ cột + nút chọn 7/30 ngày + top SP bán chạy` — biểu đồ cũ + nút 7/30 ngày đã bỏ; dashboard mới cover bởi `AdminDashboard.test.tsx` 21 test) + test Export CSV nhật ký trong `AdminPage.test.tsx` (`Nhật ký: Export CSV xuất đúng danh sách nhật ký đã lọc`).

**Test KHÔNG viết được đợt 18 + lý do:** (1) **Xóa tài khoản KH với ConfirmDialog `typeExpected "XOA"`** — không có UI xóa KH và không có endpoint `DELETE /api/admin/customers` trong BE (MapDelete chỉ có cart/items, addresses, admin products/categories/promotions/evaluations); **không viết test Selenium** cho XOA xóa KH; chỉ viết **unit test component ConfirmDialog với `typeExpected "XOA"`** trong Vitest `AdminDot18.test.tsx` (khóa nút xác nhận tới khi gõ đúng `XOA`). (2) **Badge `99+` (> 99 đơn chờ xác nhận) ở Selenium** — cần 100 đơn thật chờ xử lý trong DB nên không tái hiện được; chỉ cover ở Vitest (`AdminDot18.test.tsx`, mock `pendingOrders = 150` → `99+`); Selenium ADM18-04 chỉ khẳng định định dạng badge (số hoặc `99+`) khi badge hiện.

**Lưu ý khác đợt 18 wave D (đã sửa 2 testid trùng trong code web — chỉ 2 chỗ này, không sửa logic web khác):** `admin-promo-edit-btn` trùng giữa nút Sửa ở dòng voucher Draft và nút `[Chỉnh sửa]` ở footer form xem KM → đổi nút footer thành **`admin-promo-view-edit-btn`** (nút dòng giữ nguyên); `admin-order-detail-close-btn` trùng giữa nút X của FormShell và nút `[Đóng]` ở footer chi tiết đơn → đổi nút footer thành **`admin-order-detail-footer-close-btn`** (nút X giữ nguyên). Test Vitest + Selenium đã cập nhật theo tên mới; `getAllByTestId('admin-promo-edit-btn')` trong `AdminPage.test.tsx` vẫn = 1 (chỉ còn nút dòng). **Chưa chạy E2E thật trên máy Acer** (Selenium đợt 18 wave D mới chỉ kiểm chứng `javac` + đếm `@Test` + XML resolve + grep `3000` trên máy Muse; cần Matthew chạy `run_test_admin.bat` trên Acer sau khi rebuild admin đợt 18/18.1).

## Đợt 19 — test auto từ file test case manual admin v18 (476 case → 226 test mới)

Bước 3 cho admin, theo đúng cách BƯỚC 3 đã làm cho web: đối chiếu file `dafl-test-manual-admin-v18.xlsx` (476 case = 171 PASS / 1 FAIL / 304 UNTESTED, 12 sheet SCR_01..SCR_12) với test Selenium hiện có → viết test mới cho các gap. Bản thảo mapping ở `~/workspace/manual-admin-v18-auto/` (`mapping_SCR_01..12.json`).

**Bảng mapping manual → auto (theo từng SCR):**

| Sheet | Tổng case | ĐÃ CÓ auto (covered) | CHỈ LÀM TAY (manual-only) | THIẾU auto (gap) |
|---|---|---|---|---|
| SCR_01 Đăng nhập | 18 | 3 | 3 | 12 |
| SCR_02 Tổng quan | 65 | 4 | 35 | 26 |
| SCR_03 Đơn hàng | 57 | 7 | 5 | 45 |
| SCR_04 Sản phẩm | 81 | 24 | 6 | 51 |
| SCR_05 Danh mục | 55 | 7 | 0 | 48 |
| SCR_06 Khuyến mãi | 40 | 7 | 1 | 32 |
| SCR_07 Khách hàng | 33 | 7 | 4 | 22 |
| SCR_08 Đánh giá | 24 | 7 | 3 | 14 |
| SCR_09 Hội viên | 28 | 1 | 1 | 26 |
| SCR_10 Trả hàng | 28 | 1 | 3 | 24 |
| SCR_11 Nhật ký | 15 | 0 | 2 | 13 |
| SCR_12 Hồ sơ | 32 | 11 | 3 | 18 |
| **Tổng** | **476** | **79** | **66** | **331** |

Quy tắc mapping: covered = test Selenium hiện có assert đúng hành vi đó (PASS Vitest KHÔNG tính); manual-only = visual/responsive/kiểm bằng mắt, lỗi/trạng thái không ép được, giới hạn đã biết, 6 popup chưa có API, các case [CẦN XÁC MINH]; gap = viết được auto trên :3001. 331 gap gom thành 230 tên test đề xuất → viết được **226 test mới ADM19-01..226** (1 test trùng đã gộp vào UP-02 viết lại, xem dưới).

**Test mới theo lớp (mã ADM19 sau khi đánh số lại toàn cục — xem ghi chú tích hợp):**

| Lớp | Số test mới | Mã ADM19 |
|---|---|---|
| system/AdminLoginTest (MỚI) | 9 | 01..09 |
| system/AdminDashboardTest (MỚI) | 24 | 10..33 |
| system/AdminWave13Test (+8) | 8 | 34..41 |
| system/AdminProfileTest (+9) | 9 | 42..50 |
| catalog/AdminProductFormTest (MỚI) | 15 | 51..65 |
| catalog/AdminCategoryFormTest (MỚI) | 15 | 66..80 |
| catalog/AdminProductTest (+8) | 8 | 81..88 |
| catalog/AdminCategoryTest (+15) | 15 | 89..103 |
| catalog/AdminCatalogBrowseTest (+8) | 8 | 104..111 |
| catalog/AdminUploadTest (+5) | 5 | 112..116 |
| orders/AdminOrderTest (+42) | 42 | 117..158 |
| promotions/AdminPromoTest (+16) | 16 | 159..174 |
| promotions/AdminPromoFormTest (MỚI) | 10 | 175..184 |
| customers/AdminCustomerTest (+15) | 15 | 185..199 |
| reviews/AdminReviewTest (+11) | 11 | 200..210 |
| customers/AdminMembershipTest (MỚI) | 16 | 211..226 |

**Ghi chú tích hợp (coordinator làm, không tin báo cáo worker):** (1) Mỗi nhóm viết (W1 System / W2 Catalog / W3 Orders+Promos / W4 Customers+Reviews+Membership) chỉ sửa file test được giao + snippet `locators_W{1..4}.java`; coordinator gộp **113 hằng By mới vào `AdminPageUI.java` + 285 method mới vào `AdminPageObject.java`** (thêm `import org.openqa.selenium.Keys`). (2) **29 hằng của W4 bị worker để comment** (tưởng đã có sẵn) — đã khôi phục vào UI (4 hằng trùng W1/W3 thì giữ bản W1/W3). (3) Xung đột tên method giữa các snippet, đã chọn 1 bản: `getOrderStatusFilterValue`/`getCustomerTierFilterValue` giữ bản W1 (`getSelectedValue` cho `<select>`), `getProductRatingCellText` giữ bản W2 (có guard trả `""` khi không thấy dòng), `isConfirmDialogPresent` giữ bản W3 (`isElementDisplayed`, nhanh vì `getWebElement` không chờ + không có implicit wait), `getStatusFilterOptionTexts` giữ bản W2 (dùng `Select` API); `ADMIN_TABLE_HEADERS` W2/W3 giống nhau, giữ 1. (4) **Mã ADM19 trùng nhau giữa 4 nhóm** (mỗi nhóm tự đánh 01..xx) — đã đánh số lại toàn cục theo thứ tự file: W1 giữ 01..50, W2 → 51..116, W3 → 117..184, W4 → 185..226; mã duy nhất 01..226, không trùng, không thiếu (mã trong thân test như `"Trừ điểm test ADM19-35"` chỉ là chuỗi lý do, không phải mã test, giữ nguyên). (5) **UP-02 VIẾT LẠI + xóa ADM19-63:** UP-02 cũ assert sai so với code thật — lỗi validate file (.txt/>5MB) KHÔNG hiện ở `admin-product-image-error` (`addFiles` trong `FormControls.tsx` không gọi `setDropError` cho lỗi validate, chỉ gọi cho trường hợp vượt số ảnh tối đa; testid `${prefix}-error` chỉ render khi `dropError` khác null) và file lỗi VẪN tạo 1 thumbnail trạng thái lỗi nên `getProductImageThumbCount() == 0` cũng sai; bản mới assert theo code thật (toast + nội dung lỗi trên thumbnail + nút `Thử lại` + 0 thumbnail ready + 1 thumbnail lỗi) và cover luôn TC_SCRA04_FUNC_070/071, nên test trùng ADM19-63 đã xóa. (6) Sửa 1 lỗi biên dịch trong test W3: lambda ở `AdminPromoTest` bắt biến `code` không effectively-final → dùng biến `final` trung gian. (7) `testng-admin.xml` + `testng.xml` thêm 6 lớp mới (18 lớp admin); echo trong `run_test_admin.bat` + `run_test_admin_allure.bat` cập nhật 79 → 305 test (giữ CRLF); `run_test_single.bat` bổ sung 6 lớp mới vào dòng gợi ý lớp admin (giữ CRLF, script vẫn tự tìm file theo tên lớp). (8) Lưu ý thẩm mỹ: các khối method tích hợp bị thụt lề thừa 4 khoảng trắng (do snippet đã thụt sẵn) — chỉ xấu format, không ảnh hưởng biên dịch/chạy.

**Test KHÔNG viết được trong đợt 19 + lý do:** 6 popup chưa có API (A7 xóa tài khoản KH, C8/C9 ẩn-hiện đánh giá, D4 thu tiền COD, E3 nhận hàng trả, H2 xóa hàng loạt, G6 xung đột dữ liệu) — không nút trigger, chỉ ghi nhận ở case manual. Tắt Toggle khi TẠO SP/DM (FUNC_036 SP / FUNC_023 DM): `CreateProductCommand`/`CreateCategoryCommand` không có trường `IsActive`, handler hardcode `IsActive = true` — cần BE thêm field mới viết được. Chọn 2 file cùng lúc vào input icon DM (FUNC_051): input single-file, Selenium không gửi được 2 file. Bỏ trống danh mục khi tạo SP (VAL_030 đúng như mô tả): form tự chọn sẵn DM đầu tiên, không có cách bỏ chọn qua UI — test đã thích ứng (assert danh mục KHÔNG có lỗi). Trạng thái rỗng 0 đơn / 0 trả hàng / 0 nhật ký / 0 đánh giá: không ép được DB về 0 qua UI/API. FreeShip giá trị 0 (SCR_06 FUNC_039), tìm đơn theo tên SP (SCR_03 FUNC_022), dropdown đổi trạng thái thiếu "Đã hủy" (FUNC_049), hủy đơn đã thanh toán → chờ hoàn tiền (FUNC_056), ghi nhận hoàn tiền lần 2 (SCR_10 FUNC_027): đều [CẦN XÁC MINH], chưa assert. G4 phiên hết hạn / 401 ở gate / mất mạng khi đăng nhập (SCR_01 SEC_014/015, VAL_017): không ép được tin cậy (phải đợi JWT 1 giờ hết hạn hoặc ngắt mạng). Badge `99+`, KPI doanh thu, biểu đồ doanh thu, tự làm mới 5 phút, đo CSS/responsive nhiều viewport (SCR_02): manual-only theo mapping (BUG_DAFL_003 vẫn mở). Trùng SĐT khi sửa khách (SCR_07 VAL_027) và lưu hồ sơ bằng SĐT đã tồn tại (SCR_12 VAL_015): BE `UpdateCustomerCommandHandler`/`PUT /api/admin/profile` chưa rõ có kiểm tra trùng SĐT không — chưa assert, cần chạy tay. Copy ID nhật ký (SCR_11 UI_014): đọc clipboard headless không ổn định.

**Lưu ý khi chạy (test đổi/xóa dữ liệu thật + self-skip):** Chuyển trạng thái đơn (12 test ADM19-117..158): mỗi test tiêu thụ tối đa 1 đơn ở trạng thái nguồn, tự skip khi thiếu đơn; chạy lại trên DB ít đơn thì các test sau skip theo (không fail). Duyệt/từ chối trả hàng + ghi nhận hoàn tiền (ADM19-37/38/41): tiêu thụ yêu cầu trả hàng thật, hoàn tiền không hoàn tác được. ADM17-13 (đổi mật khẩu admin): đổi sang `Admin@456` rồi đổi lại `Admin@123` trong `finally` — chạy chết giữa chừng thì mật khẩu kẹt ở `Admin@456`. RVM-06 + ADM19-200..210: xóa tối đa 3 đánh giá thật/lượt chạy, chỉ khi ≥ 2 đánh giá. Khóa tài khoản demo (ADM19-07): mở khóa lại trong `finally` — chết giữa chừng thì demo bị khóa (mở lại tay ở mục Khách hàng). Điều chỉnh điểm demo (ADM19-39/40, ACR-08): +5 rồi −5 trong `finally`. Cấu hình hạng (ADM19-31/39): ngưỡng tính động lúc chạy, skip nếu không tăng dần được; F1/F2/F3 đều hoàn tác trong `finally`. Sửa KH thật (ADM19-185..199): khôi phục trong `finally`; nếu địa chỉ gốc của demo đang trống thì không khôi phục được qua UI (trống = giữ nguyên). ADM19-46: để trống SĐT rồi khôi phục trong `finally`. Phân trang trang 2: skip khi ≤ 10 dòng. Đổi mật khẩu validate 8 nhánh (ADM19-47): chỉ assert lỗi, KHÔNG đổi mật khẩu thật.

**Phát hiện đã đối chiếu code thật trong lúc tích hợp:** (1) **UP-02 cũ assert sai — ĐÃ SỬA** (xem trên). (2) Claim của worker W2 "mọi lần lưu form sửa SP đều tắt SP vì `saveProduct` không gửi `isActive`" là **SAI — đã bác bỏ**: `AdminForms.tsx` `handleSubmit` gửi `isActive` trong payload `onSave`, và `saveProduct` trong `AdminPage.tsx` đưa `isActive: payload.isActive` vào body PUT (dòng `body2`); không sửa gì. (3) Claim của worker W4 "ngưỡng Vàng 16.000.000đ > Kim cương 15.000.000đ" **không khớp code/seed**: `MembershipService.DefaultConfigs()` seed Vàng 5.000.000đ / Kim cương 15.000.000đ (tăng dần đúng); 16.000.000đ là giá trị ví dụ sai trong file mapping gốc, không phải bug sản phẩm — test ADM19-31/39 đã tính ngưỡng động lúc chạy nên không ảnh hưởng. (4) **Đã xác nhận đúng code:** nút `Đặt lại cấu hình` (`resetMembers`, `AdminPage.tsx:770`) chỉ khôi phục `tierDraft`, KHÔNG xóa `tiersError` — bấm đặt lại xong lỗi inline vẫn hiện (test ADM19-32 assert đúng hành vi này); lỗi độ dài khi sửa KH (tên 1 ký tự / địa chỉ 501 ký tự) chỉ hiện ở mức form, KHÔNG có toast + KHÔNG có lỗi inline dưới ô (vì `toFormSaveError` chỉ map lỗi trùng mã qua `duplicateFieldOf`) — test ADM19-13 assert đúng hành vi này.

**Kiểm chứng độc lập (máy Muse, 2026-11-10):** `javac` toàn bộ Java PASS (0 lỗi, sau khi bổ sung 20 method bị sót do regex trích snippet lần 1 không nhận kiểu trả về có dấu chấm/`private`, và sửa 1 lambda); đếm lại `@Test`: **tổng 580 (web 275/17 lớp + admin 305/18 lớp)**; `testng.xml` 35 lớp + `testng-admin.xml` 18 lớp resolve đủ, không thiếu, không trùng; grep `"3000"` trong test admin = 0; đối chiếu testid mới với code admin thật: 54 testid, 53 khớp literal + 1 động (`admin-category-image-error` = `${prefix}-error` với prefix `admin-category-image`, chỉ assert ở luồng dán link sai — đúng chỗ `setDropError` được gọi); không file test web nào bị đụng (mtime xác nhận); brace/parens cân bằng. **Chưa chạy E2E thật trên máy Acer** (cần Matthew chạy `run_test_admin_allure.bat` sau khi rebuild admin đợt 18 + áp zip đợt 19; nhớ đổi PayOS về placeholder trước khi chạy).

> **Tổng mới (đợt 19): web 275 / admin 305 / tổng 580 test / 35 lớp (web 17 + admin 18). Vitest không đổi: admin 142/142 pass (6 file), web 136/136 pass.**
