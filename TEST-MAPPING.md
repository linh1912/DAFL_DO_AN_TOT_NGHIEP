# ÁNH XẠ KIỂM THỬ — Đặc tả CN-01..CN-08 ↔ Bộ test Selenium (repo2)

> Bộ test tự động: **321 test method / 32 lớp**, chia 2 thư mục riêng: `automation/src/test/java/vn/dafl/tests/web/` (26 lớp web bán hàng, 275 test) và `automation/src/test/java/vn/dafl/tests/admin/` (6 lớp admin, 46 test).
> Mọi locator bám `data-testid` thật của FE bản cuối (đã đối chiếu tự động với mã nguồn FE — 0 testid lệch; 2 locator cũ đã sửa: `filterByStatus` dùng tab thay cho `<select>`, 2 test dùng tên SP seed cũ "Khăn ướt" đổi sang "Mặt nạ giấy").
> Chạy: app DAFL ở `http://localhost:3000`, sau đó `cd automation && run_test_headless.bat` (hoặc `mvnw.cmd test -Dheadless=true`). Chỉ chạy web: `run_test_web.bat` (suite `testng-web.xml`, 275 test — chỉ cần web bán hàng `http://localhost:3000`); chỉ chạy admin: `run_test_admin.bat` (suite `testng-admin.xml`, 46 test — chỉ cần app Admin riêng `http://localhost:3001` đang chạy + tài khoản admin đã seed, KHÔNG cần `:3000` nữa từ đợt 10).
> Tài khoản demo: `demo@dafl.vn` / `Password123!`. Tài khoản Super Admin: `admin@dafl.vn` / `Admin@123` (đăng nhập được bằng tên `admin` ở trang Admin riêng localhost:3001) — DbInitializer tự tạo nếu thiếu; nếu chưa có thì các test ADM-03..ADM-14, ADT-01..ADT-09, ACR-01..ACR-08, ADM-W13-01..ADM-W13-04, UP-01..UP-03 tự bỏ qua (Skip). Voucher seed: DAFL10, DAFL20, FREESHIP, VIP50K, FORDEV.
> Đợt 13: xem mục "Tính năng mới đợt 13" ở cuối bảng ánh xạ (trang Hội viên, quên mật khẩu, sửa/xóa voucher Draft, điểm khách đọc lại được, ghi nhận hoàn tiền, lọc ngày + Export nhật ký, đăng nhập admin không nút quay lại).
> Đợt redesign form admin + upload ảnh: xem mục "Đợt redesign form admin + upload ảnh" ở cuối bảng ánh xạ (drawer form sản phẩm, dropdown danh mục custom, toast admin-toast, upload ảnh UP-01..UP-03).
> Đợt 15: xem mục "Tính năng mới đợt 15" ở cuối bảng ánh xạ (gửi đánh giá sản phẩm RV-01..04, dùng điểm khi thanh toán PT-01..04, ngày sinh + voucher sinh nhật BD-01..04).
> Đợt 15.1 (bước 3): xem mục "Đợt 15.1 — test auto từ file test case manual v15.1" ở cuối bảng ánh xạ (156 test mới / 12 lớp web, cover đủ 176 case loại (c) trong 338 case manual; mã RV-05..10, PT-05..08, BD-05..09 do nhóm viết test gán nối tiếp RV/PT/BD đợt 15 — file manual không ghi sẵn các mã này).
> Đợt 16: xem mục "Đợt 16 — Quản lý đánh giá admin (RVM-01..08)" ở cuối bảng ánh xạ (mục "Đánh giá" thứ 9 trên Admin riêng :3001 — bảng, lọc sao + tìm kiếm, Export, phân trang, modal chi tiết, xóa qua hộp xác nhận).
> Ghi chú về báo cáo Allure: báo cáo Allure hiển thị tên test, nhóm chức năng (Feature/Story) và tên suite bằng tiếng Việt (tên suite lấy từ thuộc tính `name` trong các file `automation/testng-web.xml`, `testng-admin.xml`, `testng.xml`). Menu giao diện Allure (Overview/Suites/Graphs...) vẫn tiếng Anh vì Allure không có bản dịch tiếng Việt.

## Tổng hợp theo lớp

| Lớp test | Số test | Phạm vi |
|---|---|---|
| web/AuthTest | 19 | CN-01 đăng ký, CN-02 đăng nhập/JWT/đăng xuất + đăng nhập admin bằng tên `admin` ở :3001, không còn nút social + link 'Quên mật khẩu' ở form đăng nhập shop (đợt 13, FP-01) |
| web/CatalogAndSearchTest | 12 | CN-04 danh mục, tìm kiếm, lọc, sắp xếp, phân trang + nút 'Áp dụng' lọc giá |
| web/ProductDetailTest | 9 | CN-05 chi tiết sản phẩm + đánh giá sao (đợt 4) |
| web/CartManagementTest | 9 | CN-06 giỏ hàng khách + gộp giỏ + voucher |
| web/CheckoutAndOrderTest | 10 | CN-07 thanh toán/đặt hàng, PayOS, phí ship + nút Đặt hàng sticky, tab đơn có số lượng + PayOS không còn tab nhúng/iframe |
| web/OrderCancellationTest | 3 | CN-08 hủy đơn + hoàn tồn kho |
| web/ProfileAndAddressTest | 9 | CN-03 hồ sơ, đổi mật khẩu, sổ địa chỉ |
| web/EndToEndShoppingFlowTest | 1 | Luồng đầu-cuối CN-04 → CN-08 |
| web/NegativeAndExceptionScenariosTest | 17 | Âm tính/ngoại lệ toàn hệ thống |
| web/HomeHeroTest | 10 | Banner hero + Flash Sale (đợt 2–4) + modal thông báo + dark mode (đợt 8) |
| web/NotFoundPageTest | 2 | Trang 404 (đợt 3–4) |
| web/NewsletterTest | 3 | Đăng ký bản tin ở footer web bán hàng NL-01..03 (chuyển từ admin/AdminAndNewsletterTest sang web đợt 10) |
| web/MembershipTest | 3 | Trang Hội viên riêng /membership (MEM-01/02) + 15 link footer (FTR-01) — đợt 13 |
| web/Wave15FeaturesTest | 12 | Tính năng mới đợt 15: gửi đánh giá sản phẩm (RV-01..04), dùng điểm khi thanh toán (PT-01..04), ngày sinh + voucher sinh nhật (BD-01..04) |
| web/AuthExtendedTest | 19 | Đăng ký/đăng nhập mở rộng: toggle mật khẩu, giữ dữ liệu khi trùng email, quên mật khẩu 3 bước (SCR_01 8 + SCR_02 11 case) — đợt 15.1 |
| web/ProfileExtendedTest | 9 | Hồ sơ/sổ địa chỉ mở rộng (SCR_03 10 case) — đợt 15.1 |
| web/CatalogExtendedTest | 15 | Danh mục/tìm kiếm/lọc/sắp xếp mở rộng (SCR_04 18 case) — đợt 15.1 |
| web/HomePageTest | 12 | Trang chủ mở rộng: banner, danh mục nổi bật, bán chạy, gợi ý, Flash Sale (SCR_09 13 case) — đợt 15.1 |
| web/HomeHeaderTest | 9 | Header mở rộng: tìm kiếm, giỏ, menu tài khoản, dark mode (SCR_09 9 case) — đợt 15.1 |
| web/ProductDetailExtendedTest | 10 | Chi tiết SP mở rộng + gửi đánh giá RV-05..10 (SCR_05 9 case) — đợt 15.1 |
| web/CartExtendedTest | 12 | Giỏ hàng mở rộng + voucher (SCR_06 11 case) — đợt 15.1 |
| web/CheckoutExtendedTest | 19 | Checkout mở rộng: form giao hàng 8 trường, validation, lưu địa chỉ mặc định, khối tính tiền, giới hạn COD 2.000.000đ (SCR_07) — đợt 15.1 |
| web/CheckoutPaymentTest | 11 | Thanh toán mở rộng: freeship trước giảm giá, voucher khi đặt hàng, view PayOS, /mock-payos, /payment-success, dùng điểm PT-05..08 (SCR_07) — đợt 15.1 |
| web/OrdersExtendedTest | 13 | Đơn hàng mở rộng: lọc tab, phân trang, 'Thanh toán ngay', 'Mua lại', hộp hủy đơn 5 lý do (SCR_08 16 case) — đợt 15.1 |
| web/MembershipExtendedTest | 16 | Hội viên mở rộng + voucher sinh nhật BD-05..09 (SCR_10 17 case) — đợt 15.1 |
| web/NotificationsFooterTest | 11 | Thông báo + footer 15 link thật (SCR_11 24 case, 14 link footer gộp 1 test) — đợt 15.1 |
| admin/AdminAndNewsletterTest | 14 | Trang Admin chỉ đọc + biểu đồ doanh thu (đợt 3–4) + tìm kiếm/lọc/Export (đợt 8); đợt 12: o search topbar đã bỏ (tìm kiếm ở toolbar của mục), toolbar chuẩn + phân trang 10 dòng/trang + form mở trong modal (ADM-11 viết lại, ADM-12..ADM-14 mới); 3 test bản tin NL-01..03 đã chuyển sang web/NewsletterTest (đợt 10) |
| admin/AdminDetailTest | 9 | Modal xem chi tiết admin Spark: sản phẩm, danh mục, khuyến mãi, hạng, đơn, khách, trả hàng + mở form sửa từ modal (đợt 8, chỉ đọc) |
| admin/AdminCrudTest | 8 | CRUD thật trên Admin riêng :3001 (đợt 9): tạo/sửa/xóa SP + danh mục, tạo/kích hoạt/tắt voucher, đổi trạng thái + hủy 1 đơn 'Chờ xử lý' có sẵn (đợt 10 — không khôi phục được trạng thái cũ), điều chỉnh điểm demo — chỉ đụng dữ liệu do test tạo, tự dọn sau khi chạy (trừ ACR-06/07); đợt 13: ACR-05 thêm sửa/xóa voucher Draft qua hộp xác nhận, ACR-08 đọc lại số dư điểm trước/sau và khẳng định tăng đúng +5; đợt redesign form admin: ACR-01..05 viết lại theo drawer + dropdown custom + toast (ACR-01 kèm giá gốc/mô tả/highlight, ACR-02 kèm badge % giảm, ACR-04 kèm slug tự sinh + toggle, ACR-05 kèm form voucher đầy đủ + nút tạo mã ngẫu nhiên) |
| admin/AdminWave13Test | 4 | Tính năng mới đợt 13 trên Admin riêng (ADM-W13-01..04): đăng nhập admin không nút quay lại, link 'Quên mật khẩu' ở admin, nút 'Ghi nhận đã hoàn tiền' (chỉ mở modal xem), lọc ngày + Export nhật ký hệ thống |
| admin/AdminUploadTest | 3 | Upload ảnh sản phẩm (đợt redesign form admin, UP-01..03): 1 ảnh qua input file thật → có thumbnail trong bảng + chi tiết; file sai định dạng (.txt) và ảnh > 5MB bị từ chối inline; 2 ảnh + đặt ảnh thứ 2 làm ảnh chính |
| admin/AdminReviewTest | 8 | Quản lý đánh giá admin (đợt 16, RVM-01..08): mục "Đánh giá" thứ 9 — bảng Sản phẩm/Khách/Sao/Nội dung/Ngày, lọc sao + tìm kiếm, Export CSV, phân trang, modal chi tiết, xóa qua hộp xác nhận (không nút sửa/tạo); RVM-06 xóa 1 đánh giá thật, không khôi phục được |
| **Tổng** | **321** | **275 web + 46 admin** |

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
| ADM-01 | AdminAndNewsletterTest.guestOpeningAdminIsRedirectedToLogin | khách mở `/admin` → về `/login` |
| ADM-02 | AdminAndNewsletterTest.customerOpeningAdminSeesForbiddenPageWithGuidance | tài khoản thường → `admin-forbidden` + "Cách cấp quyền Super Admin" + email đang đăng nhập |
| ADM-03 | AdminAndNewsletterTest.adminSeesEightMenuItems | Super Admin thấy đủ 8 `admin-nav-*` đúng thứ tự (skip nếu chưa seed admin) |
| ADM-04 | AdminAndNewsletterTest.adminDashboardLoads | mục Tổng quan tải được |
| ADM-05 | AdminAndNewsletterTest.adminCanBrowseOrdersProductsCustomersReadOnly | chỉ đọc: `admin-order-search`, `admin-product-search`, `admin-customer-search` |
| ADM-06 | AdminAndNewsletterTest.adminRevenueChartLoadsAndRangeButtonsWork | `admin-revenue-range-7/30` đổi khoảng (GET `/api/admin/reports/revenue`); có đơn thì `admin-revenue-chart` vẽ SVG, chưa có đơn thì dòng "Chưa có doanh thu..." |
| ADM-07 (đợt 8) | AdminAndNewsletterTest.adminProductSearchFiltersRows | `admin-product-search` loc dung ('Mặt nạ' ra dong, tu khoa la ra 0 dong + 'Không có sản phẩm nào.') |
| ADM-08 (đợt 8) | AdminAndNewsletterTest.adminCustomerSearchFiltersRows | `admin-customer-search` loc dung (email demo ra dong, email la ra 0 dong + 'Không có khách hàng nào.') |
| ADM-09 (đợt 8) | AdminAndNewsletterTest.adminOrderStatusFilterShowsOnlyCancelled | `admin-order-status-filter` chon 'Đã hủy' + nut 'Tìm' → chi don da huy hoac 'Không có đơn hàng nào.' |
| ADM-10 (đợt 8) | AdminAndNewsletterTest.adminExportButtonIsClickableWithoutError | nut 'Export' bam duoc, trang admin van dung yen (xuat CSV client-side, khong khang dinh file tai ve) |
| ADM-11 (đợt 12, viết lại) | AdminAndNewsletterTest.adminToolbarSearchFiltersProductsAndTopbarSearchRemoved | o search topbar đã bỏ (khẳng định âm: topbar không còn ô tìm kiếm) — tìm kiếm ở toolbar của mục Sản phẩm (`admin-product-search`) vẫn lọc đúng ('Mặt nạ' ra dòng) |
| ADM-12 (đợt 12) | AdminAndNewsletterTest.adminProductsPaginationNextPrevChangesPage | phân trang bảng Sản phẩm (`admin-products-pagination`, 10 dòng/trang): trang 1 tối đa 10 dòng, bấm 'Sau' sang trang 2 rồi 'Trước' về trang 1, 'Trang X / Y' đổi theo (skip nếu ≤ 10 dòng) |
| ADM-13 (đợt 12, viết lại đợt redesign form admin) | AdminAndNewsletterTest.adminAddProductOpensFormModalAndCloseReturnsToTable | bấm '+ Thêm sản phẩm' ở toolbar → form mở trong DRAWER (`admin-product-form-modal`, tiêu đề 'Thêm sản phẩm') có dropzone upload ảnh (`admin-product-image-dropzone`) + nút 'Hủy' (`admin-product-cancel-btn`) / 'Lưu' (`admin-product-save-btn`) ở footer; đóng bằng nút 'Hủy' → quay lại bảng, không tạo dữ liệu |
| ADM-14 (đợt 12) | AdminAndNewsletterTest.adminProductsToolbarHasAddResetRefreshButtons | toolbar chuẩn mục Sản phẩm: nút '+ Thêm sản phẩm' ở trái + nút icon 'Đặt lại' (`admin-products-reset-btn`) / 'Làm mới' (`admin-products-refresh-btn`) ở phải; bấm 'Đặt lại' xóa từ khóa và về trang 1; mục Danh mục cũng có đủ 2 nút icon |
| ADT-01 (đợt 8) | AdminDetailTest.productDetailModalShowsCorrectProduct | nut mat `Xem chi tiết sản phẩm *` → `admin-product-detail` hien dung ten SP + 'Giá bán', dong duoc |
| ADT-02 (đợt 8) | AdminDetailTest.categoryDetailModalShowsCorrectCategory | nut mat danh muc → `admin-category-detail` hien dung ten danh muc, dong duoc |
| ADT-03 (đợt 8) | AdminDetailTest.promoDetailModalShowsCorrectCode | nut mat khuyen mai → `admin-promo-detail` hien dung ma voucher, dong duoc |
| ADT-04 (đợt 8) | AdminDetailTest.tierDetailModalShowsCorrectTier | nut mat hang → `admin-tier-detail` hien dung ten hang, dong duoc |
| ADT-05 (đợt 8) | AdminDetailTest.orderDetailModalShowsOrderCode | nut mat don → `admin-order-detail` hien dung ma don (skip neu DB chua co don) |
| ADT-06 (đợt 8) | AdminDetailTest.customerDetailModalShowsCorrectCustomer | nut mat khach → `admin-customer-detail` hien dung ten khach, dong duoc |
| ADT-07 (đợt 8) | AdminDetailTest.returnDetailModalShowsOrderCode | nut 'Duyệt / Từ chối' → `admin-return-detail` hien ma don, chi dong (khong duyet/tu choi; skip neu chua co yeu cau tra hang) |
| ADT-08 (đợt 12, sửa) | AdminDetailTest.productDetailEditButtonOpensEditFormWithoutSaving | tu modal chi tiet SP, nut 'Sửa sản phẩm' → form sua mo trong modal (`admin-product-form-modal`, tieu de 'Sửa sản phẩm', o ten dien san ten SP — tieu de/ten khong con trong text cua form); dong form khong luu |
| ADT-09 (đợt 8) | AdminDetailTest.categoryDetailModalClosesWithFooterButton | modal chi tiet danh muc dong duoc bang nut 'Đóng' o footer |
| NL-01 | NewsletterTest.newsletterSubscribeSuccess | email mới → `.newsletter-success` "Cảm ơn bạn đã đăng ký nhận bản tin DAFL." |
| NL-02 | NewsletterTest.newsletterInvalidEmailShowsError | email sai định dạng → `.newsletter-error` "Vui lòng nhập email đúng định dạng." |
| NL-03 | NewsletterTest.newsletterDuplicateEmailShowsAlreadyRegistered | đăng ký lại email cũ → vẫn khối cảm ơn, không lỗi, không trùng lặp DB |
| ACR-01 (đợt 9, viết lại đợt redesign form admin) | AdminCrudTest.adminCreateProductShowsInTableAndSearchable | tạo SP tên `TESTSP-<timestamp>` qua drawer form mới (giá 99.000đ, giá gốc 120.000đ, tồn 10, mô tả) → toast `admin-toast` + tìm theo tên thấy 1 dòng đúng giá 99.000đ + giá gốc 120.000đ + dòng mới highlight (`sa-row-highlight`); tự xóa SP sau test |
| ACR-02 (đợt 9, viết lại đợt redesign form admin) | AdminCrudTest.adminEditProductChangesNameAndPrice | tạo SP (99.000đ / giá gốc 120.000đ) rồi sửa tên + giá 150.000đ + giá gốc 200.000đ → tìm tên mới thấy đúng giá mới, tìm tên cũ ra 0 dòng; mở lại form sửa: badge % giảm (`admin-product-discount-badge`) hiện 25%, ô giá hiển thị "150.000" chuẩn hóa về 150000 khi bỏ dấu chấm; tự xóa SP sau test |
| ACR-03 (đợt 9) | AdminCrudTest.adminDeleteProductRemovesFromTable | tạo SP rồi xóa qua hộp xác nhận chung (`admin-confirm-modal` + `admin-confirm-yes-btn`) → toast + tìm lại ra 0 dòng + 'Không có sản phẩm nào.' |
| ACR-04 (đợt 9, viết lại đợt redesign form admin) | AdminCrudTest.adminCategoryCreateEditDelete | tạo danh mục `TESTDM-<timestamp>` (mô tả + thứ tự 5) → dòng mới hiện slug tự sinh (`testdm-<timestamp>`) + mô tả; mở form sửa thấy toggle trạng thái (`admin-category-status-toggle`) → sửa tên → xóa qua hộp xác nhận; mỗi bước kiểm tra qua tìm kiếm (sau xóa: 0 dòng + 'Chưa có danh mục.') |
| ACR-05 (đợt 9, viết lại đợt 13, viết lại đợt redesign form admin) | AdminCrudTest.adminPromoCreateActivateDisable | voucher 1: tạo `TEST<timestamp>` qua form mới (giảm 10%, đơn tối thiểu 0, hạn 2027-12-31) → Draft (có nút `admin-promo-edit-btn`/`admin-promo-delete-btn`) → sửa mô tả + giá trị 15% + đơn tối thiểu 100.000đ + trần giảm 50.000đ + hạn 2028-06-30 + tổng lượt 100 → vẫn Draft → xóa qua hộp xác nhận `admin-confirm-modal` + nút `admin-confirm-yes-btn` → tìm lại 0 dòng + 'Chưa có khuyến mãi.'; voucher 2: nút tạo mã ngẫu nhiên (`admin-promo-generate-code-btn`) sinh mã + loại giảm tiền cố định 50.000đ (segmented `admin-promo-discount-type-fixed`) → kích hoạt thành Active (mất nút sửa/xóa) → tắt có lý do thành Disabled, để lại (vô hiệu) |
| ACR-06 (đợt 10) | AdminCrudTest.adminChangePendingOrderStatusToConfirmed | đổi trạng thái 1 đơn 'Chờ xử lý' CÓ SẴN trong DB sang 'Đã xác nhận' → bảng đơn hiện 'Đã xác nhận' (không đặt đơn qua :3000; trạng thái cũ không khôi phục được; skip nếu DB không có đơn chờ xử lý) |
| ACR-07 (đợt 10) | AdminCrudTest.adminCancelPendingOrder | hủy 1 đơn 'Chờ xử lý' CÓ SẴN trong DB, bắt buộc có lý do → bảng đơn hiện 'Đã hủy' (không đặt đơn qua :3000; trạng thái cũ không khôi phục được; skip nếu DB không có đơn chờ xử lý) |
| ACR-08 (đợt 9, viết lại đợt 13) | AdminCrudTest.adminAdjustPointsForDemoCustomer | đọc số dư điểm demo TRƯỚC qua modal chi tiết khách (`admin-customer-detail-points`) → cộng 5 điểm → thông báo thành công + ô điểm/lý do được xóa trắng → đọc lại SAU, khẳng định tăng đúng +5; hoàn tác -5 sau test (tổng kết bằng 0). Lưu ý testid cột Điểm ở bảng Khách hàng là `admin-customer-points-<id>` (id khách, không phải email) |
| UP-01 (đợt redesign form admin) | AdminUploadTest.adminCreateProductWithOneUploadedImage | tạo SP `TESTSP-IMG-<timestamp>` qua drawer form mới, upload 1 ảnh PNG qua input file thật (`admin-product-image-input`, sendKeys đường dẫn tuyệt đối) → thumbnail `admin-product-image-thumb-0` hiện, không lỗi → lưu → toast + tìm thấy SP, dòng có thumbnail ảnh trong bảng + modal chi tiết có `<img>`; tự xóa SP sau test |
| UP-02 (đợt redesign form admin) | AdminUploadTest.adminUploadInvalidAndOversizeImageIsRejectedInline | upload file `.txt` → lỗi inline (`admin-product-image-error` có nội dung) + 0 thumbnail → bấm Hủy (không lưu); upload ảnh PNG > 5MB (tạo lúc chạy test bằng padding, không commit file lớn) → lỗi inline + 0 thumbnail → bấm Hủy (không lưu) |
| UP-03 (đợt redesign form admin) | AdminUploadTest.adminSecondUploadedImageCanBeSetAsMain | tạo SP upload 2 ảnh (`test-product-1.png`, `test-product-2.png`) → đủ 2 thumbnail → đặt ảnh thứ 2 làm ảnh chính (`admin-product-image-set-main-btn-1`, nhãn 'Ảnh chính' chuyển sang thumbnail 0) → lưu → mở lại form sửa vẫn đủ 2 thumbnail + nhãn 'Ảnh chính' vẫn ở thumbnail 0 + modal chi tiết hiện ảnh chính có src trùng src thumbnail 0 khi mở lại form sửa (backend đổi tên file thành UUID nên không khẳng định bằng tên file gốc); tự xóa SP sau test |
| CN-05 (đợt 4) | ProductDetailTest.productDetailShowsReviewsSection | `product-reviews-section` luôn hiện; có đánh giá → `product-rating-summary` + `product-review-item`, chưa có → "Chưa có đánh giá nào" |
| CN-05 (đợt 4) | ProductDetailTest.seededProductShowsSampleReviewWithAverageRating | Son kem 3CE → 1 đánh giá mẫu, tóm tắt "5.0", Trần Hoàng Nam (skip nếu DB hiện tại không có đánh giá mẫu) |
| CN-07 (đợt 3) | CheckoutAndOrderTest.checkoutSubmitButtonHasStickyClassAndOrderTotal | nút Đặt hàng có class `checkout-submit-btn` (sticky mobile), ghi "Đặt hàng" |
| CN-08 (đợt 2–3) | CheckoutAndOrderTest.orderStatusTabsShowCountsAndFilterByTab | tab `order-status-tab-*` hiện số lượng "(n)"; bấm tab lọc đúng danh sách (đã sửa `filterByStatus` từ `<select>` sang tab) |

> Không viết test đổi mật khẩu admin tự khôi phục (rủi ro kẹt mật khẩu tạm nếu suite dừng giữa chừng). Test CRUD (ACR-01..ACR-08) chỉ đụng dữ liệu do chính test tạo (tên/mã kèm timestamp) và tự dọn sau khi chạy — trừ ACR-06/07 đổi/hủy 1 đơn 'Chờ xử lý' có sẵn trong DB (không khôi phục được trạng thái cũ); không test khóa/mở khóa tài khoản người khác, không duyệt/từ chối yêu cầu trả hàng của người khác. Từ đợt 13, admin ĐÃ có sửa/xóa voucher Draft (chỉ Draft) qua hộp xác nhận chung `admin-confirm-modal` — xem ACR-05.

## Tính năng mới đợt 13 (quên mật khẩu, hội viên, footer, hoàn tiền, nhật ký, đăng nhập admin)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| FP-01 | AuthTest.shopLoginShowsForgotPasswordLinkAndOpensForm | xem dòng CN-02 ở trên: link `forgot-password-link` ở shop, chỉ kiểm UI, không gửi mail thật |
| FP-02 | AdminWave13Test.adminLoginShowsForgotPasswordLinkAndOpensForm | link `forgot-password-link` ở form đăng nhập admin riêng → mở `forgot-password-form` → nút `forgot-password-back-btn` về lại form đăng nhập; chỉ kiểm UI, không gửi mail thật (mã 6 số gửi qua SMTP thật chỉ kiểm bằng tay) |
| MEM-01 | MembershipTest.loggedInMembershipPageShowsTierPointsHistoryAndVouchers | đăng nhập demo → menu tài khoản `dropdown-membership-btn` → `/membership`: `membership-page`, `membership-tier`, `membership-points-balance`, `membership-benefits`, `membership-history`, `membership-vouchers`, `membership-refresh-btn` (bấm làm mới vẫn hiện đủ) |
| MEM-02 | MembershipTest.membershipPageRedirectsToLoginWhenLoggedOut | chưa đăng nhập mở thẳng `/membership` → về `/login` (guard ở App.tsx; giống TC-CN02-03 cho /orders, /profile) |
| FTR-01 | MembershipTest.footerLinksHaveRealHrefsAndTrackOrderGoesToOrders | đủ 15 link `footer-link-*` (skincare, makeup, hair, body, flash-sale, guide, shipping, returns, track-order, faq, about, shops, magazine, member, contact) đều có href thật cùng SUT, không phải `#`; `footer-link-member` → `/membership`; bấm `footer-link-track-order` → `/orders` |
| ADM-W13-01 | AdminWave13Test.adminLoginPageHasNoBackButtonAndCenteredWrapper | trang đăng nhập admin riêng: `auth-close-btn` VẮNG MẶT, khung `auth-page-wrapper` (class `auth-page-wrapper`, căn giữa) hiện, tiêu đề 'Đăng nhập quản trị' |
| ADM-W13-02 | AdminWave13Test.adminLoginShowsForgotPasswordLinkAndOpensForm | xem FP-02 ở trên |
| ADM-W13-03 | AdminWave13Test.refundRecordButtonOnlyOnApprovedPendingRefundAndOpensModal | tab Trả hàng: nút `admin-return-record-refund-btn` chỉ xét khi có dữ liệu thật (skip nếu DB không có yêu cầu Approved + PendingRefund + refundId); dòng có nút phải chứa 'Approved' + 'PendingRefund'; bấm mở `admin-refund-record-modal` (ô `admin-refund-transaction-input` + nút `admin-refund-record-confirm-btn`) rồi đóng, KHÔNG nhập mã, KHÔNG xác nhận hoàn tiền thật |
| ADM-W13-04 | AdminWave13Test.auditLogDateFilterAndExportControlsWork | cuối tab Trả hàng, mục 'Nhật ký hệ thống': ô `admin-log-date-from` / `admin-log-date-to` + nút `admin-log-filter-btn` (lọc khoảng 2026-01-01..2026-12-31, mục vẫn hiện) + nút `admin-log-export-btn` (bấm xong trang admin vẫn đứng yên; chỉ bấm, không khẳng định file tải về — giống ADM-10) |
| ACR-05 (đợt 13) | AdminCrudTest.adminPromoCreateActivateDisable | xem dòng ACR-05 ở trên: sửa/xóa voucher Draft qua `admin-confirm-modal` |
| ACR-08 (đợt 13) | AdminCrudTest.adminAdjustPointsForDemoCustomer | xem dòng ACR-08 ở trên: đọc điểm trước/sau qua `admin-customer-detail-points`, khẳng định +5 |

> Lưu ý testid thật trong code đợt 13 (đừng dùng tên viết tắt): form quên mật khẩu dùng `forgot-password-email-input` / `forgot-password-submit-btn` / `forgot-password-back-btn` / `forgot-password-success` / `forgot-password-notice`; form đặt lại dùng `reset-password-form`, `reset-code-input`, `reset-new-password-input`, `reset-confirm-password-input`, `reset-password-submit-btn`. Cột Điểm ở bảng Khách hàng dùng `admin-customer-points-<id>` (id khách). Nút sửa/xóa voucher lặp lại theo dòng — test định vị theo dòng qua aria-label `Sửa khuyến mãi <mã>` / `Xóa khuyến mãi <mã>`, không dùng testid trần.

## Đợt redesign form admin + upload ảnh (drawer/modal form mới)

Form sản phẩm giờ là DRAWER (vẫn testid `admin-product-form-modal`); dropdown danh mục là custom (nút trigger `admin-product-category-select`, KHÔNG còn `<select>`); ô giá bán tự định dạng dấu chấm nghìn khi gõ (assert giá trị ô phải chuẩn hóa bằng cách bỏ dấu `.`); thông báo sau lưu/xóa ở form mới là toast `admin-toast` (hàm `waitForFeedback()` chấp nhận cả `admin-toast` lẫn `.sa-notice` cũ, nhưng test khẳng định chặt `admin-toast`); hàng mới tạo highlight bằng class `sa-row-highlight` (không phải testid); xóa SP/danh mục qua hộp xác nhận chung `admin-confirm-modal`; khuyến mãi KHÔNG có upload ảnh (backend không có field ảnh).

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| ADM-13 (viết lại) | AdminAndNewsletterTest.adminAddProductOpensFormModalAndCloseReturnsToTable | xem dòng ADM-13 ở trên: drawer + dropzone + nút Hủy/Lưu ở footer, đóng bằng nút Hủy |
| ACR-01 (viết lại) | AdminCrudTest.adminCreateProductShowsInTableAndSearchable | xem dòng ACR-01 ở trên: giá gốc + mô tả + toast + highlight |
| ACR-02 (viết lại) | AdminCrudTest.adminEditProductChangesNameAndPrice | xem dòng ACR-02 ở trên: badge % giảm + ô giá chuẩn hóa bỏ dấu chấm |
| ACR-04 (viết lại) | AdminCrudTest.adminCategoryCreateEditDelete | xem dòng ACR-04 ở trên: slug tự sinh + toggle trạng thái |
| ACR-05 (viết lại) | AdminCrudTest.adminPromoCreateActivateDisable | xem dòng ACR-05 ở trên: form voucher đầy đủ + nút tạo mã ngẫu nhiên + segmented loại giảm |
| UP-01 | AdminUploadTest.adminCreateProductWithOneUploadedImage | xem dòng UP-01 ở trên |
| UP-02 | AdminUploadTest.adminUploadInvalidAndOversizeImageIsRejectedInline | xem dòng UP-02 ở trên |
| UP-03 | AdminUploadTest.adminSecondUploadedImageCanBeSetAsMain | xem dòng UP-03 ở trên |

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
| TC_SCR1_UI_003 | AuthExtendedTest.registerPasswordTogglesWorkIndependently |  |
| TC_SCR1_UI_004 | AuthExtendedTest.switchingTabsClearsPreviousErrors |  |
| TC_SCR1_VAL_005 | AuthExtendedTest.registerWithAllFieldsEmptyShowsAllErrors |  |
| TC_SCR1_VAL_006 | AuthExtendedTest.registerWithTooLongNameShowsError |  |
| TC_SCR1_VAL_009 | AuthExtendedTest.registerWithInvalidPhoneVariantsShowsError |  |
| TC_SCR1_VAL_016 | AuthExtendedTest.registerWithEmptyEmailShowsError |  |
| TC_SCR1_VAL_017 | AuthExtendedTest.registerWithEmptyConfirmPasswordShowsError |  |
| TC_SCR1_FUNC_019 | AuthExtendedTest.duplicateEmailKeepsEnteredDataExceptPasswords |  |
| TC_SCR2_UI_002 | AuthExtendedTest.loginFormHasNoDemoOrOtpEntry |  |
| TC_SCR2_SEC_015 | AuthExtendedTest.protectedPageReturnsAfterLogin |  |
| TC_SCR2_VAL_017 | AuthExtendedTest.loginWithUsernameAdminOnShopShowsFormatError |  |
| TC_SCR2_FUNC_018 | AuthExtendedTest.forgotPasswordStep1EmptyOrInvalidEmailShowsError |  |
| TC_SCR2_FUNC_019 | AuthExtendedTest.forgotPasswordWithUnregisteredEmailShowsGenericMessage |  |
| TC_SCR2_VAL_020 | AuthExtendedTest.resetPasswordWithShortCodeShowsError |  |
| TC_SCR2_VAL_021 | AuthExtendedTest.resetPasswordWithWeakNewPasswordShowsError |  |
| TC_SCR2_VAL_022 | AuthExtendedTest.resetPasswordWithMismatchedConfirmShowsError |  |
| TC_SCR2_FUNC_023 | AuthExtendedTest.resetPasswordWithWrongCodeShowsErrorAndOldPasswordStillWorks |  |
| TC_SCR2_FUNC_024 | AuthExtendedTest.resetPasswordWrongCodeFiveTimesDestroysCode |  |
| TC_SCR2_FUNC_027 | AuthExtendedTest.forgotPasswordResendLimitExceedsThreePerHour |  |
| TC_SCR6_UI_002 | CartExtendedTest.cartLineShowsAllComponentsAndTotals |  |
| TC_SCR6_FUNC_008 | CartExtendedTest.directQuantityInputFiltersNonDigits |  |
| TC_SCR6_FUNC_016, TC_SCR6_UI_014 | CartExtendedTest.cartTotalFormulaWithVoucherAndShipping |  |
| TC_SCR6_UI_017 | CartExtendedTest.freeshipProgressShowsMissingAmount |  |
| TC_SCR6_UI_017 | CartExtendedTest.freeshipProgressShowsFreeShipAchieved |  |
| TC_SCR6_VAL_021 | CartExtendedTest.applyVip50kBelowMinOrderShowsError |  |
| TC_SCR6_FUNC_023 | CartExtendedTest.applyDafl10ReducesTotalByTenPercent |  |
| TC_SCR6_FUNC_025 | CartExtendedTest.applyFreeshipVoucherMakesShippingFree |  |
| TC_SCR6_FUNC_026 | CartExtendedTest.applyVip50kReducesTotalBy50k |  |
| TC_SCR6_FUNC_027 | CartExtendedTest.applyFordevVoucherBringsTotalTo2000 |  |
| TC_SCR6_UI_028 | CartExtendedTest.voucherInputUppercaseAndChipAppliesCode |  |
| TC_SCR6_UI_029 | CartExtendedTest.removeVoucherRestoresOriginalTotal |  |
| TC_SCR4_UI_001 | CatalogExtendedTest.productCardShowsDiscountBadgeRatingOriginalPriceAndStock |  |
| TC_SCR4_FUNC_005 | CatalogExtendedTest.clearCategoryButtonOnlyShowsAfterSelectingCategory |  |
| TC_SCR4_FUNC_006 | CatalogExtendedTest.searchCategoryByNameShowsOnlyMatchingPills |  |
| TC_SCR4_FUNC_007 | CatalogExtendedTest.searchCategoryByDescriptionShowsMatchingCategory |  |
| TC_SCR4_FUNC_008 | CatalogExtendedTest.searchCategoryNoMatchShowsEmptyStateAndShowAllRestores |  |
| TC_SCR4_FUNC_009 | CatalogExtendedTest.clearButtonInCategorySearchRestoresAllPills |  |
| TC_SCR4_FUNC_010 | CatalogExtendedTest.productTypeGroupInSidebarFiltersByCategory |  |
| TC_SCR4_FUNC_011, TC_SCR4_FUNC_013, TC_SCR4_FUNC_014, TC_SCR4_FUNC_015 | CatalogExtendedTest.remainingPricePresetsFillRangeFilterImmediatelyAndMarkActive |  |
| TC_SCR4_FUNC_017 | CatalogExtendedTest.typingPriceManuallyDeselectsActivePreset |  |
| TC_SCR4_FUNC_019 | CatalogExtendedTest.searchKeywordIsCaseInsensitive |  |
| TC_SCR4_FUNC_020 | CatalogExtendedTest.clearFiltersAfterEmptySearchRestoresFullList |  |
| TC_SCR4_FUNC_023 | CatalogExtendedTest.defaultSortIsNewest |  |
| TC_SCR4_FUNC_026 | CatalogExtendedTest.categoryFilterPersistsAcrossPages |  |
| TC_SCR4_FUNC_027 | CatalogExtendedTest.resultCountBadgeUpdatesWithFilterAndPage |  |
| TC_SCR4_FUNC_029 | CatalogExtendedTest.discontinuedProductNotShownInList |  |
| TC_SCR7_UI_001 | CheckoutExtendedTest.shippingFormDisplaysAllEightFieldsAndNoteCounter |  |
| TC_SCR7_UI_002 | CheckoutExtendedTest.savedAddressListShowsDefaultBadgeAndAddButton |  |
| TC_SCR7_UI_003 | CheckoutExtendedTest.addressCascadeLocksLowerLevelsAndResetsOnProvinceChange |  |
| TC_SCR7_UI_004 | CheckoutExtendedTest.emptyFormSubmitShowsErrorsAndFocusesFirstError |  |
| TC_SCR7_FUNC_006 | CheckoutExtendedTest.saveAsDefaultCheckboxCreatesDefaultAddressAfterOrder |  |
| TC_SCR7_VAL_007 | CheckoutExtendedTest.emptyRecipientShowsError |  |
| TC_SCR7_VAL_008 | CheckoutExtendedTest.oneCharRecipientShowsError |  |
| TC_SCR7_VAL_009 | CheckoutExtendedTest.emptyPhoneShowsError |  |
| TC_SCR7_VAL_011 | CheckoutExtendedTest.emptyEmailShowsError |  |
| TC_SCR7_VAL_012 | CheckoutExtendedTest.invalidEmailFormatShowsError |  |
| TC_SCR7_VAL_013 | CheckoutExtendedTest.missingProvinceShowsError |  |
| TC_SCR7_VAL_014 | CheckoutExtendedTest.missingDistrictShowsError |  |
| TC_SCR7_VAL_015 | CheckoutExtendedTest.missingWardShowsError |  |
| TC_SCR7_VAL_017 | CheckoutExtendedTest.shortDetailAddressShowsError |  |
| TC_SCR7_UI_026 | CheckoutExtendedTest.pricingSummaryAndPaymentMethodsDisplayedWithoutEmbeddedGateway |  |
| TC_SCR7_UI_033 | CheckoutExtendedTest.submitButtonLabelChangesByPaymentMethodAndLoginState |  |
| TC_SCR7_FUNC_024 | CheckoutExtendedTest.codStillAvailableAtExactlyTwoMillion |  |
| TC_SCR7_FUNC_025 | CheckoutExtendedTest.codDisabledAboveTwoMillionWithNoteAndAutoSwitchToPayos |  |
| TC_SCR7_FUNC_052 | CheckoutExtendedTest.directCheckoutUrlRedirectsToCart |  |
| TC_SCR7_FUNC_020 | CheckoutPaymentTest.discountDoesNotRemoveFreeShipping |  |
| TC_SCR7_FUNC_029 | CheckoutPaymentTest.voucherPercent10DiscountIsCorrectOnOrder |  |
| TC_SCR7_FUNC_030 | CheckoutPaymentTest.voucherFreeshipMakesShippingFreeOnOrder |  |
| TC_SCR7_UI_046 | CheckoutPaymentTest.payosPaymentViewShowsTimerAmountAndCopyBackButtons |  |
| TC_SCR7_FUNC_048 | CheckoutPaymentTest.simulatePayosCheckBeforeTransferShowsWaitingToast |  |
| TC_SCR7_FUNC_049 | CheckoutPaymentTest.mockPayosPageHasSingleConfirmButtonAndConfirmsPayment |  |
| TC_SCR7_FUNC_050 | CheckoutPaymentTest.paymentSuccessPageShowsOrderSummaryAndLinks |  |
| TC_SCR7_FUNC_056 | CheckoutPaymentTest.toggleOffAndOnKeepsUsedPoints | PT-05 |
| TC_SCR7_VAL_058 | CheckoutPaymentTest.pointsInputFiltersNonDigitsAndKeepsValue | PT-06 |
| TC_SCR7_FUNC_060 | CheckoutPaymentTest.turningPointsOffResetsPointsAndRestoresTotal | PT-07 |
| TC_SCR7_FUNC_061 | CheckoutPaymentTest.orderWithPointsDeductsBalanceAndLogsHistory | PT-08 |
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
| TC_SCR10_FUNC_002, TC_SCR10_UI_012 | MembershipExtendedTest.membershipTierBadgesAndNextTierThreshold |  |
| TC_SCR10_FUNC_003 | MembershipExtendedTest.membershipBenefitsShowCurrentTierEarnRate |  |
| TC_SCR10_FUNC_004 | MembershipExtendedTest.membershipBenefitsShowPointsValueAndExpiry |  |
| TC_SCR10_FUNC_005 | MembershipExtendedTest.membershipBenefitsShowMaxPointsPercentForCurrentTier |  |
| TC_SCR10_FUNC_006 | MembershipExtendedTest.membershipBenefitsShowFreeShipForCurrentTier |  |
| TC_SCR10_FUNC_007 | MembershipExtendedTest.membershipPointsBalanceShowsPointsAndConvertedValue |  |
| TC_SCR10_UI_008 | MembershipExtendedTest.membershipProgressToNextTierShowsRemainingOrTopTier |  |
| TC_SCR10_UI_010 | MembershipExtendedTest.membershipEmptyHistoryShowsEmptyMessageForNewAccount |  |
| TC_SCR10_FUNC_011 | MembershipExtendedTest.membershipVouchersListOrEmptyState |  |
| TC_SCR10_UI_017 | MembershipExtendedTest.membershipBirthdateInputPrefilledWithSavedDate |  |
| TC_SCR10_VAL_019 | MembershipExtendedTest.membershipSaveBirthdateTwiceWithin90DaysShowsError | BD-09 |
| TC_SCR10_VAL_025 | MembershipExtendedTest.membershipSaveInvalidBirthdateShowsError |  |
| TC_SCR10_FUNC_020 | MembershipExtendedTest.claimBirthdayVoucherSuccessShowsMessageAndNewVoucher | BD-05 |
| TC_SCR10_FUNC_022 | MembershipExtendedTest.claimBirthdayVoucherWithoutBirthdateShowsError | BD-06 |
| TC_SCR10_FUNC_023 | MembershipExtendedTest.claimBirthdayVoucherOutsideBirthdayWindowShowsError | BD-07 |
| TC_SCR10_FUNC_024 | MembershipExtendedTest.claimBirthdayVoucherSecondTimeThisYearShowsError | BD-08 |
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
| TC_SCR8_FUNC_004 | OrdersExtendedTest.filterOrdersByPendingPaymentTab |  |
| TC_SCR8_UI_012 | OrdersExtendedTest.ordersPaginationShowsTenPerPage |  |
| TC_SCR8_UI_019 | OrdersExtendedTest.freshAccountSeesEmptyOrdersState |  |
| TC_SCR8_FUNC_014 | OrdersExtendedTest.payNowButtonOnlyOnPendingPaymentOrder |  |
| TC_SCR8_FUNC_015 | OrdersExtendedTest.reorderCancelledOrderAddsItemsBackToCart |  |
| TC_SCR8_UI_013 | OrdersExtendedTest.orderCardShowsAtMostThreeItemsWithRemainderLine |  |
| TC_SCR8_UI_017 | OrdersExtendedTest.orderDetailShowsTimelineShippingInfoAndMoneyTable |  |
| TC_SCR8_FUNC_018 | OrdersExtendedTest.orderDetailMatchesPlacedOrderData |  |
| TC_SCR8_UI_021 | OrdersExtendedTest.cancelDialogShowsCorrectContent |  |
| TC_SCR8_FUNC_022, TC_SCR8_FUNC_023, TC_SCR8_FUNC_024, TC_SCR8_FUNC_025 | OrdersExtendedTest.cancelOrderWithEachPresetReasonShowsReasonInDetail |  |
| TC_SCR8_FUNC_026 | OrdersExtendedTest.cancelOrderWithCustomReasonShowsCustomReasonInDetail |  |
| TC_SCR8_FUNC_028 | OrdersExtendedTest.dismissCancelDialogKeepsOrderCancellable |  |
| TC_SCR8_VAL_027 | OrdersExtendedTest.customCancelReasonEmptyShowsError |  |
| TC_SCR5_UI_004 | ProductDetailExtendedTest.trustBadgesDisplayedUnderGallery |  |
| TC_SCR5_FUNC_005 | ProductDetailExtendedTest.variantSectionHasOnlyOneDefaultOption |  |
| TC_SCR5_UI_006 | ProductDetailExtendedTest.variantOptionDisabledWhenOutOfStock |  |
| TC_SCR5_FUNC_013 | ProductDetailExtendedTest.buyNowAsGuestGoesToLoginWithReturnRoute |  |
| TC_SCR5_FUNC_015 | ProductDetailExtendedTest.reviewsSectionShowsSummaryDistributionAndItems | RV-09 |
| TC_SCR5_FUNC_016 | ProductDetailExtendedTest.reviewFormDisplayedOnProductWithoutReviews | RV-10 |
| TC_SCR5_VAL_023 | ProductDetailExtendedTest.submitReviewWithTooLongCommentShowsError | RV-05 |
| TC_SCR5_FUNC_024 | ProductDetailExtendedTest.submitReviewSuccessfullyWhenLoggedIn | RV-06 |
| TC_SCR5_FUNC_025 | ProductDetailExtendedTest.guestSubmitReviewWithoutNameDefaultsToGuestName | RV-07 |
| TC_SCR5_FUNC_028 | ProductDetailExtendedTest.averageRatingAndCountUpdateAfterSubmittingReview | RV-08 |
| TC_SCR3_VAL_003, TC_SCR3_VAL_004 | ProfileExtendedTest.profileNameTooShortOrTooLongShowsError |  |
| TC_SCR3_VAL_005 | ProfileExtendedTest.profilePhoneInvalidFormatShowsError |  |
| TC_SCR3_VAL_006 | ProfileExtendedTest.profilePhoneCanBeLeftEmptyAndSaved |  |
| TC_SCR3_VAL_008 | ProfileExtendedTest.changePasswordSameAsCurrentShowsError |  |
| TC_SCR3_VAL_009 | ProfileExtendedTest.changePasswordWeakNewPasswordShowsError |  |
| TC_SCR3_VAL_010 | ProfileExtendedTest.changePasswordMismatchedConfirmShowsError |  |
| TC_SCR3_VAL_011 | ProfileExtendedTest.addressFormMissingOrInvalidFieldsShowFieldErrors |  |
| TC_SCR3_VAL_012 | ProfileExtendedTest.addressFormMissingLocationShowsLocationErrors |  |
| TC_SCR3_FUNC_020 | ProfileExtendedTest.cancelDeleteAddressKeepsAddress |  |

## Kiểm thử hiệu năng (JMeter) — đã đổi sang JWT

`performance/dafl-loadtest.jmx`: bỏ phụ thuộc cookie `sid` (Cookie Manager đã tắt), sampler đăng nhập trích `$.data.token` bằng JSON Post Processor và Header Manager gắn `Authorization: Bearer ${jwtToken}` cho toàn bộ sampler cần xác thực; body giỏ/checkout không còn `userId`, checkout gửi kèm khối `shipping` lồng nhau + `paymentMethod`. **Ngưỡng Quality Gate giữ nguyên: lỗi ≤ 2%, thời gian phản hồi TB ≤ 1000 ms.**

## Đợt 16 — Quản lý đánh giá admin (RVM-01..08)

Mục "Đánh giá" thứ 9 trên Admin riêng (`http://localhost:3001`, lớp `admin/AdminReviewTest`, 8 test): bảng Sản phẩm/Khách/Sao/Nội dung (cắt 80 ký tự)/Ngày, lọc theo số sao + tìm kiếm, nút Export CSV, phân trang 10 dòng/trang, modal xem chi tiết + nút Xóa qua hộp xác nhận chung (`admin-confirm-modal`); không có nút sửa/tạo. Backend: `GET /api/admin/evaluations` (lọc productId/rating/search, phân trang) + `DELETE /api/admin/evaluations/{id}` (chỉ SuperAdmin, xóa CỨNG vì entity `Evaluation` không có cờ ẩn/xóa, xóa xong tính lại Rating = trung bình làm tròn 1 số thập phân, 0 đánh giá → Rating = 0, ghi AuditLog). Chạy bằng `run_test_admin.bat` / `run_test_admin_allure.bat` (chỉ :3001, không cần :3000).

| Mã test | Mô tả | Ghi chú dữ liệu |
|---|---|---|
| RVM-01 | Mục "Đánh giá" hiện trong menu admin + bảng tải được, đủ cột Sản phẩm/Khách/Sao/Nội dung/Ngày | Chạy khi đã seed tài khoản admin |
| RVM-02 | Tìm kiếm theo tên sản phẩm/khách lọc đúng dòng | Skip nếu bảng trống (không có đánh giá nào) |
| RVM-03 | Lọc theo số sao 5 sao chỉ hiện đánh giá 5 sao | Skip nếu không có đánh giá 5 sao |
| RVM-04 | Mở modal xem chi tiết 1 đánh giá (đủ tên SP/khách/sao/nội dung đầy đủ/ngày), đóng được | Nội dung trong modal hiện đủ, không bị cắt |
| RVM-05 | Nội dung dài bị cắt 80 ký tự trong bảng | Cần 1 đánh giá có nội dung > 80 ký tự, nếu không có thì Skip |
| RVM-06 | Xóa 1 đánh giá qua hộp xác nhận (bấm xác nhận → dòng biến mất khỏi bảng) | Chỉ chạy khi ≥ 2 đánh giá; **xóa 1 đánh giá thật, không khôi phục được** (xóa cứng + tính lại Rating SP) |
| RVM-07 | Nút Export bấm được, trang admin vẫn đứng yên | Chỉ kiểm tra nút bấm được, không assert nội dung file (xuất CSV client-side, giống ADM-10) |
| RVM-08 | Phân trang 10 dòng/trang: trang 1 tối đa 10 dòng, bấm 'Sau' sang trang 2 rồi 'Trước' về trang 1 | Số trang theo tổng thật; skip nếu ≤ 10 dòng (giống ADM-12) |

> **Tổng mới: web 275 / admin 46 / tổng 321 test / 32 lớp (web 26 + admin 6).** Lưu ý: RVM-06 là test duy nhất trong lớp đụng dữ liệu thật (xóa 1 đánh giá có sẵn, không khôi phục được — giống ACR-06/07 đổi/hủy đơn có sẵn); các test còn lại chỉ đọc. Tên phương thức trong `AdminReviewTest` map 1-1 theo mã RVM-01..08.
