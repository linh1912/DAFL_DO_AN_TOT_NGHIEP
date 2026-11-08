# ÁNH XẠ KIỂM THỬ — Đặc tả CN-01..CN-08 ↔ Bộ test Selenium (repo2)

> Bộ test tự động: **105 test method / 13 lớp** trong `automation/src/test/java/vn/dafl/tests/`.
> Mọi locator bám `data-testid` thật của FE bản cuối (đã đối chiếu tự động với mã nguồn FE — 0 testid lệch; 2 locator cũ đã sửa: `filterByStatus` dùng tab thay cho `<select>`, 2 test dùng tên SP seed cũ "Khăn ướt" đổi sang "Mặt nạ giấy").
> Chạy: app DAFL ở `http://localhost:3000`, sau đó `cd automation && run_test_headless.bat` (hoặc `mvnw.cmd test -Dheadless=true`).
> Tài khoản demo: `demo@dafl.vn` / `Password123!`. Tài khoản Super Admin (chỉ test đọc ở /admin): `admin@dafl.vn` / `Password123!` — seed chỉ tạo khi bảng Users trống; nếu chưa có thì các test ADM-03..ADM-06 tự bỏ qua (Skip). Voucher seed: DAFL10, DAFL20, FREESHIP, VIP50K, FORDEV.

## Tổng hợp theo lớp

| Lớp test | Số test | Phạm vi |
|---|---|---|
| AuthTest | 16 | CN-01 đăng ký, CN-02 đăng nhập/JWT/đăng xuất |
| CatalogAndSearchTest | 10 | CN-04 danh mục, tìm kiếm, lọc, sắp xếp, phân trang |
| ProductDetailTest | 9 | CN-05 chi tiết sản phẩm + đánh giá sao (đợt 4) |
| CartManagementTest | 9 | CN-06 giỏ hàng khách + gộp giỏ + voucher |
| CheckoutAndOrderTest | 9 | CN-07 thanh toán/đặt hàng, PayOS, phí ship + nút Đặt hàng sticky, tab đơn có số lượng |
| OrderCancellationTest | 3 | CN-08 hủy đơn + hoàn tồn kho |
| ProfileAndAddressTest | 9 | CN-03 hồ sơ, đổi mật khẩu, sổ địa chỉ |
| EndToEndShoppingFlowTest | 1 | Luồng đầu-cuối CN-04 → CN-08 |
| NegativeAndExceptionScenariosTest | 17 | Âm tính/ngoại lệ toàn hệ thống |
| HomeHeroTest | 7 | Banner hero + Flash Sale (đợt 2–4) |
| NotFoundPageTest | 2 | Trang 404 (đợt 3–4) |
| AdminAndNewsletterTest | 9 | Trang Admin chỉ đọc + biểu đồ doanh thu + đăng ký bản tin (đợt 3–4) |
| **Tổng** | **101** | |

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
| NF-01 | NotFoundPageTest.unknownRouteShowsNotFoundPage | `not-found-page` + nút "Về trang chủ" đưa về trang chủ |
| NF-02 | NotFoundPageTest.unknownProductShowsProductNotFoundPage | `/product/khong-ton-tai` → `product-not-found-page` ("404 - Không tìm thấy sản phẩm") |
| ADM-01 | AdminAndNewsletterTest.guestOpeningAdminIsRedirectedToLogin | khách mở `/admin` → về `/login` |
| ADM-02 | AdminAndNewsletterTest.customerOpeningAdminSeesForbiddenPageWithGuidance | tài khoản thường → `admin-forbidden` + "Cách cấp quyền Super Admin" + email đang đăng nhập |
| ADM-03 | AdminAndNewsletterTest.adminSeesEightMenuItems | Super Admin thấy đủ 8 `admin-nav-*` đúng thứ tự (skip nếu chưa seed admin) |
| ADM-04 | AdminAndNewsletterTest.adminDashboardLoads | mục Tổng quan tải được |
| ADM-05 | AdminAndNewsletterTest.adminCanBrowseOrdersProductsCustomersReadOnly | chỉ đọc: `admin-order-search`, `admin-product-search`, `admin-customer-search` |
| ADM-06 | AdminAndNewsletterTest.adminRevenueChartLoadsAndRangeButtonsWork | `admin-revenue-range-7/30` đổi khoảng (GET `/api/admin/reports/revenue`); có đơn thì `admin-revenue-chart` vẽ SVG, chưa có đơn thì dòng "Chưa có doanh thu..." |
| NL-01 | AdminAndNewsletterTest.newsletterSubscribeSuccess | email mới → `.newsletter-success` "Cảm ơn bạn đã đăng ký nhận bản tin DAFL." |
| NL-02 | AdminAndNewsletterTest.newsletterInvalidEmailShowsError | email sai định dạng → `.newsletter-error` "Vui lòng nhập email đúng định dạng." |
| NL-03 | AdminAndNewsletterTest.newsletterDuplicateEmailShowsAlreadyRegistered | đăng ký lại email cũ → vẫn khối cảm ơn, không lỗi, không trùng lặp DB |
| CN-05 (đợt 4) | ProductDetailTest.productDetailShowsReviewsSection | `product-reviews-section` luôn hiện; có đánh giá → `product-rating-summary` + `product-review-item`, chưa có → "Chưa có đánh giá nào" |
| CN-05 (đợt 4) | ProductDetailTest.seededProductShowsSampleReviewWithAverageRating | Son kem 3CE → 1 đánh giá mẫu, tóm tắt "5.0", Trần Hoàng Nam (skip nếu DB hiện tại không có đánh giá mẫu) |
| CN-07 (đợt 3) | CheckoutAndOrderTest.checkoutSubmitButtonHasStickyClassAndOrderTotal | nút Đặt hàng có class `checkout-submit-btn` (sticky mobile), ghi "Đặt hàng" |
| CN-08 (đợt 2–3) | CheckoutAndOrderTest.orderStatusTabsShowCountsAndFilterByTab | tab `order-status-tab-*` hiện số lượng "(n)"; bấm tab lọc đúng danh sách (đã sửa `filterByStatus` từ `<select>` sang tab) |

> Không viết test đổi mật khẩu admin tự khôi phục (rủi ro kẹt mật khẩu tạm nếu suite dừng giữa chừng), không test đổi trạng thái đơn / xóa SP / khóa khách / điều chỉnh điểm qua UI (tránh phá dữ liệu seed dùng chung).

## Kiểm thử hiệu năng (JMeter) — đã đổi sang JWT

`performance/dafl-loadtest.jmx`: bỏ phụ thuộc cookie `sid` (Cookie Manager đã tắt), sampler đăng nhập trích `$.data.token` bằng JSON Post Processor và Header Manager gắn `Authorization: Bearer ${jwtToken}` cho toàn bộ sampler cần xác thực; body giỏ/checkout không còn `userId`, checkout gửi kèm khối `shipping` lồng nhau + `paymentMethod`. **Ngưỡng Quality Gate giữ nguyên: lỗi ≤ 2%, thời gian phản hồi TB ≤ 1000 ms.**
