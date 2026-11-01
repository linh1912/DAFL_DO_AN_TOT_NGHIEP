# ÁNH XẠ KIỂM THỬ — Đặc tả CN-01..CN-08 ↔ Bộ test Selenium (repo2)

> Bộ test tự động: **83 test method / 10 lớp** trong `automation/src/test/java/vn/dafl/tests/`.
> Mọi locator bám `data-testid` thật của FE bản CN cuối (đã đối chiếu tự động với mã nguồn FE — 0 testid lệch).
> Chạy: app DAFL ở `http://localhost:3000`, sau đó `cd automation && run_test_headless.bat` (hoặc `mvnw.cmd test -Dheadless=true`).
> Tài khoản demo: `demo@dafl.vn` / `Password123!`. Voucher seed: DAFL10, DAFL20, FREESHIP, VIP50K, FORDEV.

## Tổng hợp theo lớp

| Lớp test | Số test | Phạm vi |
|---|---|---|
| AuthTest | 16 | CN-01 đăng ký, CN-02 đăng nhập/JWT/đăng xuất |
| CatalogAndSearchTest | 10 | CN-04 danh mục, tìm kiếm, lọc, sắp xếp, phân trang |
| ProductDetailTest | 7 | CN-05 chi tiết sản phẩm |
| CartManagementTest | 9 | CN-06 giỏ hàng khách + gộp giỏ + voucher |
| CheckoutAndOrderTest | 7 | CN-07 thanh toán/đặt hàng, PayOS, phí ship |
| OrderCancellationTest | 3 | CN-08 hủy đơn + hoàn tồn kho |
| ProfileAndAddressTest | 9 | CN-03 hồ sơ, đổi mật khẩu, sổ địa chỉ |
| SocialLoginTest | 4 | Social login mock (Google/Facebook) |
| EndToEndShoppingFlowTest | 1 | Luồng đầu-cuối CN-04 → CN-08 |
| NegativeAndExceptionScenariosTest | 17 | Âm tính/ngoại lệ toàn hệ thống |
| **Tổng** | **83** | |

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

## Social login (mock — chỉ môi trường Development/Testing)

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| TC-SOC-01 | SocialLoginTest.mockGoogleLoginCreatesAccountAndSignsIn | `social-google-btn` → `social-mock-panel` → `social-mock-confirm-btn`; header "Google Test User"; có JWT |
| TC-SOC-02 | SocialLoginTest.mockFacebookLoginSignsIn | `social-facebook-btn`; header "Facebook Test User" |
| TC-SOC-03 | SocialLoginTest.cancelMockLoginShowsError | `social-mock-cancel-btn` → `social-error`; chưa đăng nhập |
| TC-SOC-06 | SocialLoginTest.socialButtonsAlsoAvailableOnRegisterTab | khối social hiện ở cả tab Đăng ký |

## Luồng đầu-cuối

| Mã TC | Lớp.phương thức | Testid/khẳng định chính |
|---|---|---|
| E2E | EndToEndShoppingFlowTest.guestToCheckoutEndToEndFlow | khách thêm 2 SP → thanh toán bị đưa qua đăng nhập → gộp giỏ → điền `shipping-*` → COD → `order-success-code` dạng DH → chi tiết đơn "Chờ xác nhận" + `order-shipping-info` |

## Kiểm thử hiệu năng (JMeter) — đã đổi sang JWT

`performance/dafl-loadtest.jmx`: bỏ phụ thuộc cookie `sid` (Cookie Manager đã tắt), sampler đăng nhập trích `$.data.token` bằng JSON Post Processor và Header Manager gắn `Authorization: Bearer ${jwtToken}` cho toàn bộ sampler cần xác thực; body giỏ/checkout không còn `userId`, checkout gửi kèm khối `shipping` lồng nhau + `paymentMethod`. **Ngưỡng Quality Gate giữ nguyên: lỗi ≤ 2%, thời gian phản hồi TB ≤ 1000 ms.**
