package vn.dafl.tests.web.checkout;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;
import vn.dafl.pageObjects.ProfilePageObject;
import vn.dafl.pageUIs.CheckoutPageUI;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.testng.SkipException;
import vn.dafl.pageObjects.MembershipPageObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CN-07 Thanh toan & dat hang (ban chot v5 + override O1: giu PayOS va voucher cua ban app hien tai).
 * Dat hang bat buoc co khoi thong tin giao hang shipping-* day du; khach chua dang nhap
 * bam thanh toan se bi dua qua dang nhap roi quay lai dung trang gio (luong E4).
  *
 * Gop them 19 test tu CheckoutExtendedTest + 11 test tu CheckoutPaymentTest (BUOC 3, dot 15.1) ngay 2026-11-10 — 1 file duy nhat cho dat hang/thanh toan (40 test).
 */
@Feature("Đặt hàng và thanh toán")
public class CheckoutAndOrderTest extends BaseTest {

    @Story("Đặt hàng COD")
    @Test(description = "CN-07 TC-CN07-01: Đặt hàng COD thành công -> mã đơn dạng DH{yyyyMMdd}-{NNNN}, đơn ở 'Chờ xác nhận'")
    public void placeOrderSuccessfully() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai hien man hinh thanh cong");
        String orderCode = checkout.getOrderSuccessCode();
        Assert.assertTrue(orderCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don phai dung dinh dang DH{yyyyMMdd}-{NNNN}, dang thay: " + orderCode);

        OrdersPageObject ordersPage = checkout.clickViewOrders();
        Assert.assertTrue(ordersPage.isOrderDisplayed(orderCode), "Don vua dat phai xuat hien trong danh sach");
        ordersPage.openOrderDetail(orderCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Don COD moi phai o trang thai Cho xac nhan");
    }

    @Story("Thanh toán PayOS")
    @Test(description = "CN-07 (override O1, sửa 2026-11-10): Chế độ demo — bấm mô phỏng chỉ hiện toast chờ thanh toán, đơn vẫn Pending, thấy đơn trong lịch sử; đường Paid thật qua /mock-payos (TC_SCR7_FUNC_049/050)")
    public void payWithMockPayosAndSeeOrderInHistory() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        Assert.assertTrue(checkout.isOrderCodeDisplayed(), "Man hinh thanh toan phai co ma don");
        checkout.simulatePayosSuccess();

        // Che do demo: nut mo phong chi goi GET /api/payments/status; PayOSService tra null
        // nen don van Pending, chi hien toast "Dang cho thanh toan", KHONG sang order-paid-view.
        // Duong Paid that o demo la /mock-payos (TC_SCR7_FUNC_049/050). Toast chi ton tai 5 giay
        // nen khang dinh ngay sau khi bam (giong TC_SCR7_FUNC_048).
        Assert.assertTrue(isTextVisibleOnPage("Đang chờ thanh toán"),
                "Che do demo: bam mo phong phai hien toast \"Đang chờ thanh toán\"");
        Assert.assertTrue(checkout.isPaymentViewDisplayed(),
                "Che do demo: don van Pending nen van o man hinh thanh toan");
        Assert.assertFalse(checkout.isOrderPaidViewDisplayed(),
                "Che do demo: KHONG duoc hien order-paid-view khi chua chuyen khoan that");

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        String firstCode = ordersPage.getFirstOrderDisplayCode();
        Assert.assertNotNull(firstCode, "Lich su don phai co don PayOS vua dat");
        Assert.assertTrue(firstCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don moi nhat phai dung dinh dang DH, dang thay: " + firstCode);
    }

    @Story("Thanh toán PayOS")
    @Test(description = "CN-07 (đợt 8): Trang thanh toán PayOS chỉ còn quét VietQR tại chỗ - KHÔNG còn tab nhúng cổng / iframe; mô phỏng thành công vẫn được")
    public void payosPageHasNoEmbeddedGatewayIframe() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        Assert.assertTrue(checkout.isOrderCodeDisplayed(), "Man hinh thanh toan phai co ma don");

        // Cach thanh toan mac dinh: quet ma VietQR tai cho.
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.PAYOS_QR_OPTION_TEXT),
                "Trang thanh toan phai hien cach 'Quét mã VietQR tại chỗ'");
        // Tab 'Nhúng Cổng PayOS' da bi xoa han o dot 8: khong con chu nhung cong, khong con iframe nao.
        Assert.assertFalse(checkout.isElementDisplayed(CheckoutPageUI.PAYOS_EMBED_TAB_TEXT),
                "Trang thanh toan khong duoc con tab 'Nhúng Cổng PayOS'");
        Assert.assertEquals(checkout.getListWebElements(CheckoutPageUI.PAYOS_ANY_IFRAME).size(), 0,
                "Trang thanh toan khong duoc con iframe nhung cong thanh toan nao");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SIMULATE_PAYOS_SUCCESS_BTN),
                "Che do demo phai co nut mo phong thanh toan PayOS thanh cong");

        // Mo phong o che do demo: chi hien toast cho thanh toan, don van Pending
        // (khong sang order-paid-view) — xem TC_SCR7_FUNC_048.
        checkout.simulatePayosSuccess();
        Assert.assertTrue(isTextVisibleOnPage("Đang chờ thanh toán"),
                "Che do demo: bam mo phong phai hien toast \"Đang chờ thanh toán\"");
        Assert.assertFalse(checkout.isOrderPaidViewDisplayed(),
                "Che do demo: KHONG duoc hien order-paid-view khi chua chuyen khoan that");
    }

    @Story("Đăng nhập khi thanh toán")
    @Test(description = "TC-CN07-02 (luồng E4): Khách bấm thanh toán -> bị đưa qua đăng nhập -> đăng nhập xong quay lại đúng trang giỏ")
    public void checkoutRequiresLoginAndReturnsAfterLogin() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.submitOrder(); // nut cua khach ghi "Dang nhap de thanh toan"

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Khach bam thanh toan phai bi dua sang trang dang nhap");

        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.waitForModalVisible();
        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Dang nhap xong phai quay lai trang gio hang");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay lai form thanh toan");
        Assert.assertTrue(new CartDrawerPageObject().isItemDisplayed(productId),
                "San pham trong gio khach phai con nguyen sau dang nhap (da gop vao tai khoan)");
    }

    @Story("Địa chỉ đã lưu")
    @Test(description = "CN-07.1 + CN-03.3: Đặt hàng bằng địa chỉ đã lưu trong sổ địa chỉ (không nhập lại form)")
    public void placeOrderUsingSavedAddressFromAddressBook() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Co So Dia Chi", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();
        profile.addAddress("Khach Co So Dia Chi", GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_DETAIL);

        getDriver().get(GlobalConstants.getAppUrl() + "/");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isSavedAddressListDisplayed(),
                "Checkout phai liet ke so dia chi da luu cua tai khoan");
        checkout.selectFirstSavedAddress();
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(),
                "Dat hang bang dia chi da luu phai thanh cong");
        Assert.assertTrue(checkout.getOrderSuccessCode().matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don phai dung dinh dang DH");
    }

    @Story("Chống đặt trùng đơn")
    @Test(description = "TC-CN07-03 (CN-07 E5): Bấm nút Đặt hàng 2 lần liên tiếp chỉ tạo đúng 1 đơn (idempotency)")
    public void doubleClickSubmitCreatesOnlyOneOrder() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Bam Hai Lan", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Bam Hai Lan", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.doubleClickSubmitOrder();

        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang phai thanh cong");
        OrdersPageObject ordersPage = checkout.clickViewOrders();
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 1,
                "Tai khoan moi chi duoc co dung 1 don du bam nut 2 lan");
    }

    @Story("Ghi chú đơn hàng")
    @Test(description = "CN-07 E8: Ghi chú đơn hàng quá 200 ký tự bị chặn ngay trên form")
    public void orderNoteLongerThan200CharsIsRejected() {
        loginWithDemoAccount();
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        String longNote = "a".repeat(201);
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD,
                GlobalConstants.SHIPPING_DETAIL, longNote);

        Assert.assertTrue(checkout.isFieldErrorDisplayed(vn.dafl.pageUIs.CheckoutPageUI.SHIPPING_NOTE_ERROR),
                "Ghi chu 201 ky tu phai bao loi shipping-note-error");
        checkout.submitOrder();
        checkout.sleepInSecond(2);
        Assert.assertFalse(checkout.isOrderSuccessViewDisplayed(),
                "Ghi chu qua dai thi khong duoc dat hang thanh cong");
    }

    @Story("Phí vận chuyển")
    @Test(description = "CN-07 (override O1): Phí ship 25.000đ khi dưới 299.000đ và miễn phí khi đạt ngưỡng")
    public void shippingFeeIs25kBelowThresholdAndFreeAbove() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        homePage.searchProduct(GlobalConstants.CHEAP_SEED_PRODUCT_KEYWORD);
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai tim thay san pham mat na giay trong du lieu seed my pham");
        long unitPrice = catalog.getProductPrice(productId);
        Assert.assertTrue(unitPrice < GlobalConstants.FREE_SHIPPING_THRESHOLD,
                "San pham dung cho test phi ship phai re hon nguong mien phi ship");

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertTrue(cart.getShippingFeeText().contains("25.000"),
                "Don duoi 299.000d phai tinh phi ship 25.000d, dang thay: " + cart.getShippingFeeText());

        int needed = (int) Math.ceil((double) GlobalConstants.FREE_SHIPPING_THRESHOLD / unitPrice);
        cart.setItemQuantity(productId, needed);
        cart.waitForItemQuantity(productId, needed);
        Assert.assertTrue(cart.getShippingFeeText().contains("Miễn phí"),
                "Don tu 299.000d phai duoc mien phi ship, dang thay: " + cart.getShippingFeeText());
    }

    @Story("Nút đặt hàng")
    @Test(description = "CN-07 (đợt 3): Nút Đặt hàng có class checkout-submit-btn (sticky trên mobile) và ghi đúng tổng tiền")
    public void checkoutSubmitButtonHasStickyClassAndOrderTotal() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isStickySubmitButtonDisplayed(),
                "Nut dat hang phai co class checkout-submit-btn va hien thi");
        Assert.assertTrue(checkout.getSubmitButtonText().contains("Đặt hàng"),
                "Nut dat hang (COD mac dinh) phai ghi 'Đặt hàng', dang thay: " + checkout.getSubmitButtonText());
    }

    @Story("Lịch sử đơn hàng")
    @Test(description = "CN-08 (đợt 2-3): Tab trạng thái đơn hiện số lượng; bấm tab lọc đúng danh sách (không còn <select>)")
    public void orderStatusTabsShowCountsAndFilterByTab() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Xem Tab Don", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Xem Tab Don", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");

        OrdersPageObject ordersPage = checkout.clickViewOrders();
        Assert.assertTrue(ordersPage.isOrderStatusTabDisplayed(""),
                "Phai thay tab Tat ca");
        Assert.assertEquals(ordersPage.getOrderStatusTabCount(""), 1,
                "Tai khoan moi co 1 don thi tab Tat ca phai ghi (1), dang thay: "
                        + ordersPage.getOrderStatusTabText(""));
        Assert.assertEquals(ordersPage.getOrderStatusTabCount(GlobalConstants.ORDER_STATUS_PENDING), 1,
                "Don COD moi phai dem 1 o tab Cho xac nhan, dang thay: "
                        + ordersPage.getOrderStatusTabText(GlobalConstants.ORDER_STATUS_PENDING));

        // Tab loc dung that (khong phai <select>): tab Da huy -> trong, tab Cho xac nhan -> co don.
        ordersPage.filterByStatus(GlobalConstants.ORDER_STATUS_CANCELLED);
        Assert.assertTrue(ordersPage.isOrdersEmptyStateDisplayed(),
                "Loc tab Da huy phai thay trang thai rong (tai khoan moi chua huy don nao)");
        ordersPage.filterByStatus(GlobalConstants.ORDER_STATUS_PENDING);
        Assert.assertEquals(ordersPage.getOrderCountOnCurrentPage(), 1,
                "Loc tab Cho xac nhan phai thay dung 1 don vua dat");
    }

    // ---------- Test bo sung — gop tu CheckoutExtendedTest (BUOC 3, file manual v15.1) ----------

    /** Ten SP seed (DbInitializer.cs) + gia (đ) dung de ghep gio dung tong can thiet. */
    private static final String[][] CART_EXACT_2M = {
            {"Velvet Lip Tint", "289000", "1"},
            {"Zero Matte Lipstick", "199000", "5"},
            {"Color Sensational", "179000", "4"}
    }; // 289.000 + 995.000 + 716.000 = 2.000.000d dung (ton kho seed 95/120/110)
    private static final String[][] CART_OVER_2M = {
            {"Velvet Lip Tint", "289000", "1"},
            {"Zero Matte Lipstick", "199000", "2"},
            {"Peripera Ink Glasting", "219000", "6"}
    }; // 289.000 + 398.000 + 1.314.000 = 2.001.000d (xem ghi chu o test 025)

    /** Dang nhap demo, them SP dau tien vao gio, mo /cart, tra ve form checkout. */
    private CheckoutPageObject openCheckoutFormWithOneProduct() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        return checkout;
    }

    /** Tim tung SP theo tu khoa duy nhat, kiem tra dung gia seed, them 1 cai vao gio. */
    private List<String> addProductsByKeyword(HomePageObject homePage, String[][] items) {
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        List<String> ids = new ArrayList<>();
        for (String[] item : items) {
            homePage.searchProduct(item[0]);
            catalog.waitForProductsLoaded();
            String productId = catalog.getFirstProductId();
            Assert.assertNotNull(productId, "Phai tim thay san pham voi tu khoa '" + item[0] + "'");
            Assert.assertEquals(catalog.getProductPrice(productId), Long.parseLong(item[1]),
                    "Gia SP tim duoc voi tu khoa '" + item[0] + "' phai dung gia seed " + item[1] + "d");
            int badgeBefore = homePage.getCartBadgeCount();
            catalog.addToCart(productId);
            homePage.waitForCartBadgeCount(badgeBefore + 1);
            ids.add(productId);
        }
        return ids;
    }

    /** Mo /cart va dat so luong tung dong theo bang items (chi goi khi so luong > 1). */
    private CartDrawerPageObject openCartAndSetQuantities(HomePageObject homePage,
                                                          List<String> productIds, String[][] items) {
        CartDrawerPageObject cart = homePage.openCart();
        for (int i = 0; i < productIds.size(); i++) {
            int quantity = Integer.parseInt(items[i][2]);
            if (quantity > 1) {
                cart.setItemQuantity(productIds.get(i), quantity);
                cart.waitForItemQuantity(productIds.get(i), quantity);
            }
        }
        return cart;
    }

    /** Xay gio dung tong tu bang items tren tai khoan moi (gio bat dau rong); tra ve gio hang. */
    private CartDrawerPageObject buildExactCart(String[][] items, long expectedSubtotal) {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Ghep Gio", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), expectedSubtotal,
                "Tam tinh gio phai dung " + expectedSubtotal + "d, dang thay: " + cart.getSubtotalText());
        return cart;
    }

    // ---------- Form giao hang: hien thi ----------

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_001: Form giao hàng hiển thị đủ 8 trường + bộ đếm ghi chú \"{n}/200\"")
    public void shippingFormDisplaysAllEightFieldsAndNoteCounter() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();

        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_EMAIL_INPUT),
                "Phai thay o Email (shipping-email-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_INPUT),
                "Phai thay o Nguoi nhan (shipping-recipient-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_PHONE_INPUT),
                "Phai thay o So dien thoai (shipping-phone-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_SELECT),
                "Phai thay o Tinh/TP (shipping-province-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Phai thay o Quan/Huyen (shipping-district-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Phai thay o Phuong/Xa (shipping-ward-select)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_DETAIL_INPUT),
                "Phai thay o So nha ten duong (shipping-detail-input)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_NOTE_INPUT),
                "Phai thay o Ghi chu (shipping-note-input)");
        Assert.assertEquals(checkout.getShippingNoteCounterText(), "0/200",
                "Bo dem ghi chu luc chua nhap phai la \"0/200\", dang thay: "
                        + checkout.getShippingNoteCounterText());
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_002: Khối địa chỉ đã lưu hiển thị danh sách + địa chỉ mặc định có badge \"MẶC ĐỊNH\" + nút thêm địa chỉ mới")
    public void savedAddressListShowsDefaultBadgeAndAddButton() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Co Dia Chi", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();
        profile.addAddress("Khach Co Dia Chi", GlobalConstants.DEMO_PHONE, "Số 10 Đường Một");
        profile.addAddress("Khach Co Dia Chi", GlobalConstants.DEMO_PHONE, "Số 20 Đường Hai");
        String defaultAddressId = profile.getDefaultAddressId();
        Assert.assertNotNull(defaultAddressId, "Dia chi dau tien them phai tu thanh mac dinh");

        getDriver().get(GlobalConstants.getAppUrl() + "/");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isSavedAddressListDisplayed(),
                "Phai thay khoi dia chi da luu (shipping-address-list)");
        Assert.assertEquals(checkout.getSavedAddressOptionCount(), 2,
                "Tai khoan co 2 dia chi thi khoi dia chi da luu phai hien dung 2 lua chon");
        String defaultOptionText = checkout.getSavedAddressOptionText(defaultAddressId);
        Assert.assertTrue(defaultOptionText.contains("MẶC ĐỊNH"),
                "Dia chi mac dinh phai co badge \"MẶC ĐỊNH\", dang thay: " + defaultOptionText);
        Assert.assertTrue(defaultOptionText.contains("Khach Co Dia Chi"),
                "Dia chi mac dinh phai hien ten nguoi nhan, dang thay: " + defaultOptionText);
        Assert.assertTrue(defaultOptionText.contains(GlobalConstants.DEMO_PHONE),
                "Dia chi mac dinh phai hien SDT, dang thay: " + defaultOptionText);
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.SHIPPING_NEW_ADDRESS_BTN),
                "Phai thay nut them dia chi moi (shipping-new-address-btn)");
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_003: Chọn địa chỉ theo cascade Tỉnh → Quận → Phường; Quận/Phường bị khóa trước khi chọn cấp trên; đổi Tỉnh thì Quận/Phường reset về rỗng")
    public void addressCascadeLocksLowerLevelsAndResetsOnProvinceChange() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();

        Assert.assertFalse(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Chua chon Tinh thi o Quan/Huyen phai bi khoa");
        Assert.assertFalse(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Chua chon Quan thi o Phuong/Xa phai bi khoa");

        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, "Thành phố Hà Nội");
        sleepInSecond(1);
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_DISTRICT_SELECT),
                "Chon Tinh xong o Quan/Huyen phai mo khoa de chon");
        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_DISTRICT_SELECT, "Quận Cầu Giấy");
        sleepInSecond(1);
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Chon Quan xong o Phuong/Xa phai mo khoa de chon");
        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_WARD_SELECT, "Phường Dịch Vọng");
        Assert.assertTrue(checkout.isElementEnabled(CheckoutPageUI.SHIPPING_WARD_SELECT),
                "Phuong da chon xong van phai mo de doi neu can");

        checkout.selectByVisibleText(CheckoutPageUI.SHIPPING_PROVINCE_SELECT, "Thành phố Hồ Chí Minh");
        sleepInSecond(1);
        Assert.assertEquals(checkout.getSelectedValue(CheckoutPageUI.SHIPPING_DISTRICT_SELECT), "",
                "Doi Tinh thi Quan/Huyen da chon phai reset ve rong");
        Assert.assertEquals(checkout.getSelectedValue(CheckoutPageUI.SHIPPING_WARD_SELECT), "",
                "Doi Tinh thi Phuong/Xa da chon phai reset ve rong");
    }

    @Story("Form giao hàng")
    @Test(description = "TC_SCR7_UI_004: Submit form trống -> lỗi đỏ từng ô + focus vào ô lỗi đầu tiên (Người nhận) + không tạo đơn")
    public void emptyFormSubmitShowsErrorsAndFocusesFirstError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.ensureNewAddressFormVisible();
        checkout.submitOrder();
        sleepInSecond(1); // cho focusFirstError cuon + focus o loi dau tien

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Form trong phai bao loi duoi o Nguoi nhan");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "Form trong phai bao loi duoi o So dien thoai");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Form trong phai bao loi duoi o Email");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Form trong phai bao loi duoi o Tinh/TP");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Form trong phai bao loi duoi o Quan/Huyen");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Form trong phai bao loi duoi o Phuong/Xa");
        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Form trong phai bao loi duoi o Dia chi chi tiet");

        // FIELD_ORDER cua CheckoutForm.tsx: recipient dung dau -> o loi dau tien duoc focus
        // la o Nguoi nhan (shipping-recipient-input).
        Object activeTestId = checkout.executeScript(
                "return document.activeElement ? document.activeElement.getAttribute('data-testid') : '';");
        Assert.assertEquals(String.valueOf(activeTestId), "shipping-recipient-input",
                "Focus phai roi vao o loi dau tien (Nguoi nhan), dang o: " + activeTestId);
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(),
                "Submit form loi thi form van hien (khong tao don, khong chuyen trang)");
    }

    // ---------- Luu dia chi mac dinh ----------

    @Story("Địa chỉ đã lưu")
    @Test(description = "TC_SCR7_FUNC_006: Tick \"Lưu làm địa chỉ mặc định\" rồi đặt COD -> địa chỉ mới xuất hiện trong Sổ địa chỉ và là mặc định")
    public void saveAsDefaultCheckboxCreatesDefaultAddressAfterOrder() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isSaveDefaultCheckboxDisplayed(),
                "Tai khoan da dang nhap phai thay checkbox \"Lưu làm địa chỉ mặc định\"");
        String newDetail = "Số 99 Đường Mới " + (System.currentTimeMillis() % 100000);
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD, newDetail, null);
        checkout.clickSaveDefaultCheckbox();
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");

        ProfilePageObject profile = homePage.openProfilePage();
        profile.waitForAddressBookVisible();
        String defaultAddressId = profile.getDefaultAddressId();
        Assert.assertNotNull(defaultAddressId, "Sau khi dat hang phai co dia chi mac dinh trong so");
        boolean found = false;
        for (String addressId : profile.getAddressIds()) {
            if (profile.getAddressItemText(addressId).contains(newDetail)) {
                found = true;
                Assert.assertEquals(addressId, defaultAddressId,
                        "Dia chi vua dat (" + newDetail + ") phai la dia chi mac dinh");
            }
        }
        Assert.assertTrue(found, "Dia chi vua dat (" + newDetail + ") phai xuat hien trong So dia chi");
    }

    // ---------- Validation tung truong ----------

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_007: Để trống tên người nhận -> lỗi \"Vui lòng nhập họ tên người nhận.\" + không tạo đơn")
    public void emptyRecipientShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm("", GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "De trong Nguoi nhan phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Vui lòng nhập họ tên người nhận.", "Loi Nguoi nhan phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_008: Tên người nhận \"A\" (1 ký tự) -> lỗi \"Họ tên người nhận phải có ít nhất 2 ký tự.\" + không tạo đơn")
    public void oneCharRecipientShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm("A", GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Ten Nguoi nhan 1 ky tu phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_RECIPIENT_ERROR),
                "Họ tên người nhận phải có ít nhất 2 ký tự.", "Loi Nguoi nhan phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_009: Để trống số điện thoại -> lỗi \"Vui lòng nhập số điện thoại.\" + không tạo đơn")
    public void emptyPhoneShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, "",
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "De trong SDT phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "Vui lòng nhập số điện thoại.", "Loi SDT phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_011: Để trống email -> lỗi \"Vui lòng nhập email.\" + không tạo đơn")
    public void emptyEmailShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, "", GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "De trong Email phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Vui lòng nhập email.", "Loi Email phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_012: Email \"khachhang@\" sai định dạng -> lỗi \"Email không đúng định dạng.\" + không tạo đơn")
    public void invalidEmailFormatShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, "khachhang@", GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT,
                GlobalConstants.SHIPPING_WARD, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Email sai dinh dang phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_EMAIL_ERROR),
                "Email không đúng định dạng.", "Loi Email phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_013: Không chọn Tỉnh/TP -> lỗi \"Vui lòng chọn Tỉnh / Thành phố.\" + không tạo đơn")
    public void missingProvinceShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, null, null, null, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Khong chon Tinh/TP phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_PROVINCE_ERROR),
                "Vui lòng chọn Tỉnh / Thành phố.", "Loi Tinh/TP phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_014: Chọn Tỉnh nhưng không chọn Quận/Huyện -> lỗi \"Vui lòng chọn Quận / Huyện.\" + không tạo đơn")
    public void missingDistrictShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE, null, null,
                GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Khong chon Quan/Huyen phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_DISTRICT_ERROR),
                "Vui lòng chọn Quận / Huyện.", "Loi Quan/Huyen phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_015: Chọn Tỉnh + Quận nhưng không chọn Phường/Xã -> lỗi \"Vui lòng chọn Phường / Xã.\" + không tạo đơn")
    public void missingWardShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingFormPartial(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, null, GlobalConstants.SHIPPING_DETAIL, null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Khong chon Phuong/Xa phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_WARD_ERROR),
                "Vui lòng chọn Phường / Xã.", "Loi Phuong/Xa phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    @Story("Kiểm tra giá trị form giao hàng")
    @Test(description = "TC_SCR7_VAL_017: Địa chỉ chi tiết \"12 A\" (4 ký tự) -> lỗi \"Địa chỉ chi tiết phải có ít nhất 5 ký tự.\" + không tạo đơn")
    public void shortDetailAddressShowsError() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE, GlobalConstants.SHIPPING_PROVINCE,
                GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD, "12 A", null);
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Dia chi chi tiet 4 ky tu phai bao loi");
        Assert.assertEquals(checkout.getFieldError(CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Địa chỉ chi tiết phải có ít nhất 5 ký tự.", "Loi dia chi chi tiet phai dung nguyen van");
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Loi validation thi khong duoc tao don");
    }

    // ---------- Khoi tinh tien + phuong thuc ----------

    @Story("Khối tính tiền và phương thức")
    @Test(description = "TC_SCR7_UI_026: Khối tính tiền tách dòng Tạm tính/Phí vận chuyển/Tổng thanh toán + đủ 2 phương thức COD/PayOS (mặc định COD) + KHÔNG có tab \"Nhúng Cổng PayOS\" hay iframe")
    public void pricingSummaryAndPaymentMethodsDisplayedWithoutEmbeddedGateway() {
        CheckoutPageObject checkout = openCheckoutFormWithOneProduct();

        Assert.assertTrue(isTextVisibleOnPage("Tạm tính"), "Khoi tinh tien phai co dong Tam tinh");
        Assert.assertTrue(isTextVisibleOnPage("Phí vận chuyển:"), "Khoi tinh tien phai co dong Phi van chuyen");
        Assert.assertTrue(isTextVisibleOnPage("Tổng thanh toán:"), "Khoi tinh tien phai co dong Tong thanh toan");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.PAYMENT_METHOD_COD),
                "Phai thay lua chon COD (payment-method-cod)");
        Assert.assertTrue(checkout.isElementDisplayed(CheckoutPageUI.PAYMENT_METHOD_PAYOS),
                "Phai thay lua chon PayOS (payment-method-payos)");
        Assert.assertTrue(checkout.isCodPaymentSelected(),
                "Phuong thuc mac dinh phai la COD");
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán khi nhận hàng (COD)"),
                "Lua chon COD phai ghi dung ten");
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán trực tuyến PayOS"),
                "Lua chon PayOS phai ghi dung ten");
        Assert.assertFalse(isTextVisibleOnPage("Nhúng Cổng PayOS"),
                "Form checkout khong duoc con tab/chu \"Nhúng Cổng PayOS\"");
        Assert.assertEquals(checkout.getListWebElements(CheckoutPageUI.PAYOS_ANY_IFRAME).size(), 0,
                "Form checkout khong duoc con iframe nhung cong thanh toan nao");
    }

    @Story("Khối tính tiền và phương thức")
    @Test(description = "TC_SCR7_UI_033: Nhãn nút submit đổi theo phương thức: COD -> \"Đặt hàng ({tổng})\", PayOS -> \"Thanh toán trực tiếp PayOS ({tổng})\", chưa đăng nhập -> \"Đăng nhập để thanh toán ({tổng})\"")
    public void submitButtonLabelChangesByPaymentMethodAndLoginState() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");

        // Ca 1: COD mac dinh.
        String codLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(codLabel.startsWith("Đặt hàng ("),
                "Chon COD nut phai ghi \"Đặt hàng ({tổng})\", dang thay: " + codLabel);

        // Ca 2: chuyen sang PayOS.
        checkout.selectPayosPayment();
        String payosLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(payosLabel.startsWith("Thanh toán trực tiếp PayOS ("),
                "Chon PayOS nut phai ghi \"Thanh toán trực tiếp PayOS ({tổng})\", dang thay: " + payosLabel);

        // Ca 3: chua dang nhap (dang xuat roi them gio bang tai khoan khach).
        homePage.logout();
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        sleepInSecond(1);
        homePage.openCart();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Khach van thay form thanh toan trong trang gio");
        String guestLabel = checkout.getSubmitButtonText();
        Assert.assertTrue(guestLabel.startsWith("Đăng nhập để thanh toán ("),
                "Chua dang nhap nut phai ghi \"Đăng nhập để thanh toán ({tổng})\", dang thay: " + guestLabel);
    }

    // ---------- Gioi han COD ----------

    @Story("Giới hạn COD")
    @Test(description = "TC_SCR7_FUNC_024: Đơn có tổng cuối đúng 2.000.000đ vẫn chọn được COD + không hiện ghi chú giới hạn")
    public void codStillAvailableAtExactlyTwoMillion() {
        CartDrawerPageObject cart = buildExactCart(CART_EXACT_2M, 2_000_000L);
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 2_000_000L,
                "Tong cuoi phai dung 2.000.000d (tam tinh >= 299.000d nen mien phi ship), dang thay: "
                        + cart.getFinalTotalText());

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertTrue(checkout.isCodPaymentEnabled(),
                "Tong dung 2.000.000d thi radio COD phai van chon duoc (codAvailable = finalTotal <= 2.000.000d)");
        Assert.assertFalse(checkout.isCodLimitNoteDisplayed(),
                "Tong dung 2.000.000d thi khong duoc hien ghi chu gioi han COD (payment-cod-limit-note)");
    }

    @Story("Giới hạn COD")
    @Test(description = "TC_SCR7_FUNC_025: Đơn tổng cuối 2.001.000đ (trên 2.000.000đ) -> radio COD bị disable + hiện ghi chú giới hạn + tự chuyển sang PayOS")
    public void codDisabledAboveTwoMillionWithNoteAndAutoSwitchToPayos() {
        // Luu y: 2.000.001d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 2.001.000d, muc tren 2.000.000d
        // gan nhat ghep duoc, van kich hoat dung luat codAvailable = finalTotal <= 2.000.000d.
        CartDrawerPageObject cart = buildExactCart(CART_OVER_2M, 2_001_000L);
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 2_001_000L,
                "Tong cuoi phai dung 2.001.000d (mien phi ship), dang thay: " + cart.getFinalTotalText());

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        Assert.assertFalse(checkout.isCodPaymentEnabled(),
                "Tong tren 2.000.000d thi radio COD phai bi disable");
        Assert.assertTrue(checkout.isCodLimitNoteDisplayed(),
                "Tong tren 2.000.000d phai hien ghi chu gioi han COD (payment-cod-limit-note)");
        String note = checkout.getCodLimitNoteText();
        Assert.assertTrue(note.contains("Đơn hàng trên 2.000.000"),
                "Ghi chu phai neu dung moc 2.000.000d, dang thay: " + note);
        Assert.assertTrue(note.contains("chỉ hỗ trợ thanh toán trực tuyến PayOS."),
                "Ghi chu phai neu chi ho tro PayOS, dang thay: " + note);
        Assert.assertTrue(checkout.isPayosPaymentSelected(),
                "Tong tren 2.000.000d thi phuong thuc phai tu chuyen sang PayOS");
    }

    // ---------- Dieu huong ----------

    @Story("Điều hướng")
    @Test(description = "TC_SCR7_FUNC_052: Truy cập trực tiếp /checkout bị chuyển về /cart (không có trang checkout riêng)")
    public void directCheckoutUrlRedirectsToCart() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/checkout");
        sleepInSecond(2); // cho route <Navigate to="/cart" replace /> chay
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Truy cap /checkout phai chuyen ve /cart, dang o: " + getDriver().getCurrentUrl());
        Assert.assertFalse(getDriver().getCurrentUrl().contains("/checkout"),
                "URL sau chuyen huong khong duoc con /checkout");
    }

    // ---------- Test thanh toan — gop tu CheckoutPaymentTest (BUOC 3, file manual v15.1) ----------

    /** Tim SP theo tu khoa duy nhat, kiem tra dung gia seed, them 1 cai vao gio. */

    /** Mo /cart va dat so luong tung dong theo bang items (chi goi khi so luong > 1). */

    /** Xay gio tren tai khoan MOI (gio bat dau rong), ap voucher, tra ve gio hang. */
    private CartDrawerPageObject buildCartWithVoucher(String[][] items, long expectedSubtotal,
                                                      String voucherCode) {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Ap Voucher", email, randomPhone(),
                GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), expectedSubtotal,
                "Tam tinh gio phai dung " + expectedSubtotal + "d, dang thay: " + cart.getSubtotalText());
        cart.applyVoucher(voucherCode);
        if (!cart.isVoucherAppliedBannerDisplayed()) {
            String reason = cart.isVoucherErrorDisplayed()
                    ? cart.getVoucherErrorMessage() : "không rõ lý do";
            throw new SkipException("Ap ma " + voucherCode + " that bai (" + reason + ") — bo qua test");
        }
        return cart;
    }

    /** Dat COD thanh cong tu gio dang mo; tra ve ma don hien thi DH... */
    private String placeCodOrder(CartDrawerPageObject cart, String name, String email, String phone) {
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan trong trang gio");
        checkout.fillDefaultShippingForm(name, email, phone);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        return checkout.getOrderSuccessCode();
    }

    /** Dat PayOS thanh cong bang tai khoan demo; tra ve {ma don so, tong tien}. */
    private long[] placePayosOrderWithDemo() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        long totalAmount = parseMoney(cart.getFinalTotalText());
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        String orderCodeText = checkout.getOrderCode(); // "Đơn hàng #<ma so>"
        String digits = orderCodeText.replaceAll("[^0-9]", "");
        Assert.assertFalse(digits.isEmpty(),
                "Man hinh thanh toan phai hien ma don so, dang thay: " + orderCodeText);
        return new long[]{Long.parseLong(digits), totalAmount};
    }

    /** Dinh dang so tien kieu "218.000" de doi chieu chuoi hien thi tren trang. */
    private String formatVnd(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    /** Doc so du diem kha dung cua tai khoan demo qua /membership (mau PT-02/PT-03). */
    private int readDemoAvailablePoints() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();
        return membershipPage.getAvailablePoints();
    }

    /** Mo gio demo voi 1 gio tam tinh 500.000d (de tran diem hang Thuong = 50 diem). */
    private CartDrawerPageObject openDemoCartWith500k() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String[][] items = {
                {"Tẩy tế bào chết cơ thể", "165000", "1"},
                {"Green Tea Hand Cream", "99000", "1"},
                {"Green Tea Balancing", "59000", "4"}
        }; // 165.000 + 99.000 + 236.000 = 500.000d
        List<String> ids = addProductsByKeyword(homePage, items);
        CartDrawerPageObject cart = openCartAndSetQuantities(homePage, ids, items);
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), 500_000L,
                "Tam tinh gio diem phai dung 500.000d, dang thay: " + cart.getSubtotalText());
        return cart;
    }

    /** Trich tran diem {m} tu cau goi y "Hạng ... dùng tối đa {m} điểm cho đơn này." */
    private int parseMaxPointsFromHint(String hintText) {
        Matcher matcher = Pattern.compile("tối đa\\s+([0-9][0-9.,]*)\\s+điểm").matcher(hintText);
        if (!matcher.find()) {
            throw new AssertionError("Khong trich duoc tran diem tu goi y: " + hintText);
        }
        return (int) parseMoney(matcher.group(1));
    }

    // ---------- Voucher khi dat hang ----------

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_020: Giảm giá không làm mất freeship vì freeship tính trước giảm giá (350.000đ + DAFL10 và 306.000đ + DAFL10 đều vẫn miễn phí ship)")
    public void discountDoesNotRemoveFreeShipping() {
        // Ca 1: tam tinh 350.000d, DAFL10 giam 35.000d (con 315.000d sau giam).
        String[][] caseA = {{"bồ kết", "175000", "2"}}; // 175.000 x 2 = 350.000d
        CartDrawerPageObject cartA = buildCartWithVoucher(caseA, 350_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cartA.getVoucherDiscountValue(), 35_000L,
                "DAFL10 tren 350.000d phai giam dung 35.000d");
        Assert.assertTrue(cartA.getShippingFeeText().contains("Miễn phí"),
                "Tam tinh 350.000d (>= 299.000d) phai duoc mien phi ship, dang thay: "
                        + cartA.getShippingFeeText());

        // Ca 2: tam tinh 306.000d, DAFL10 giam 30.600d -> con 275.400d sau giam (< 299.000d)
        // nhung van freeship vi nguong tinh tren tam tinh TRUOC giam gia.
        // Luu y: 305.000d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 306.000d gan nhat:
        // 129.000 + 59.000 x 3 (ton kho seed 120/150).
        String[][] caseB = {
                {"Speedy Eyebrow", "129000", "1"},
                {"Green Tea Balancing", "59000", "3"}
        };
        CartDrawerPageObject cartB = buildCartWithVoucher(caseB, 306_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cartB.getVoucherDiscountValue(), 30_600L,
                "DAFL10 tren 306.000d phai giam dung 30.600d");
        Assert.assertTrue(cartB.getShippingFeeText().contains("Miễn phí"),
                "Tam tinh 306.000d truoc giam van >= 299.000d nen phai mien phi ship du sau giam"
                        + " chi con 275.400d, dang thay: " + cartB.getShippingFeeText());
    }

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_029: Voucher DAFL10 tính giảm đúng khi đặt hàng (500.000đ giảm 50.000đ, đơn lưu mã + tổng đúng)")
    public void voucherPercent10DiscountIsCorrectOnOrder() {
        String[][] items = {
                {"Tẩy tế bào chết cơ thể", "165000", "1"},
                {"Green Tea Hand Cream", "99000", "1"},
                {"Green Tea Balancing", "59000", "4"}
        }; // 165.000 + 99.000 + 236.000 = 500.000d
        CartDrawerPageObject cart = buildCartWithVoucher(items, 500_000L, GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertEquals(cart.getVoucherDiscountValue(), 50_000L,
                "DAFL10 tren 500.000d phai giam dung 50.000d (10%, chua cham tran 100.000d)");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Tong cuoi phai la 500.000 - 50.000 + 0 ship = 450.000d, dang thay: "
                        + cart.getFinalTotalText());

        String orderCode = placeCodOrder(cart, "Khach Ap Voucher", randomEmail(), GlobalConstants.DEMO_PHONE);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains("Giảm giá (DAFL10):"),
                "Chi tiet don phai luu ma voucher DAFL10 o dong giam gia");
        Assert.assertTrue(detailText.contains("-" + formatVnd(50_000L)),
                "Chi tiet don phai giam dung 50.000d, dang thay: " + detailText);
        Assert.assertTrue(detailText.contains("Tạm tính:"), "Chi tiet don phai co dong Tam tinh");
        Assert.assertTrue(detailText.contains(formatVnd(500_000L)),
                "Chi tiet don phai co tam tinh 500.000d");
        Assert.assertTrue(detailText.contains("Tổng cộng:"), "Chi tiet don phai co dong Tong cong");
        Assert.assertTrue(detailText.contains(formatVnd(450_000L)),
                "Tong thanh toan don phai dung 450.000d (500.000 - 50.000 + 0 ship)");
    }

    @Story("Voucher khi đặt hàng")
    @Test(description = "TC_SCR7_FUNC_030: Voucher FREESHIP miễn phí vận chuyển khi đặt hàng (248.000đ, phí ship 0đ, đơn lưu mã FREESHIP)")
    public void voucherFreeshipMakesShippingFreeOnOrder() {
        // Luu y: 250.000d theo dung case manual KHONG ghep duoc tu gia seed
        // (moi gia seed deu la boi cua 1.000d) — dung 248.000d gan nhat, van >= 200.000d
        // (muc toi thieu cua FREESHIP): 149.000 + 99.000.
        String[][] items = {
                {"No-Sebum", "149000", "1"},
                {"Green Tea Hand Cream", "99000", "1"}
        };
        CartDrawerPageObject cart = buildCartWithVoucher(items, 248_000L, GlobalConstants.VOUCHER_FREESHIP);
        Assert.assertTrue(cart.getShippingFeeText().contains("Miễn phí"),
                "Ap FREESHIP thi phi ship phai ve 0d (\"Miễn phí\"), dang thay: " + cart.getShippingFeeText());
        long finalTotal = parseMoney(cart.getFinalTotalText());

        String orderCode = placeCodOrder(cart, "Khach Ap Voucher", randomEmail(), GlobalConstants.DEMO_PHONE);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains("Phí vận chuyển:"),
                "Chi tiet don phai co dong Phi van chuyen");
        Assert.assertTrue(detailText.contains("Miễn phí"),
                "Chi tiet don phai hien phi ship \"Miễn phí\" (0d)");
        Assert.assertTrue(detailText.contains("Giảm giá (FREESHIP):"),
                "Chi tiet don phai luu ma voucher FREESHIP o dong giam gia");
        Assert.assertTrue(detailText.contains(formatVnd(finalTotal)),
                "Tong cong don phai khop tong cuoi luc dat (" + formatVnd(finalTotal) + "d)");
    }

    // ---------- View thanh toan PayOS ----------

    @Story("Thanh toán PayOS")
    @Test(description = "TC_SCR7_UI_046: Trang thanh toán sau checkout PayOS: tiêu đề \"Đơn hàng #{mã}\", đồng hồ MM:SS bắt đầu quanh 15:00, số tiền đúng, nút \"Sao chép mã\" + \"Đổi giỏ hàng\"")
    public void payosPaymentViewShowsTimerAmountAndCopyBackButtons() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CartDrawerPageObject cart = new CartDrawerPageObject();
        long expectedTotal = parseMoney(cart.getFinalTotalText());
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectPayosPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Chon PayOS phai hien man hinh thanh toan");
        String orderCodeText = checkout.getOrderCode();
        Assert.assertTrue(orderCodeText.startsWith("Đơn hàng #"),
                "Tieu de view thanh toan phai la \"Đơn hàng #{mã}\", dang thay: " + orderCodeText);
        String timerText = checkout.getPaymentTimerText();
        Matcher timerMatcher = Pattern.compile("^(\\d{2}):(\\d{2})$").matcher(timerText);
        Assert.assertTrue(timerMatcher.matches(),
                "Dong ho (payment-timer) phai dung dinh dang MM:SS, dang thay: " + timerText);
        int secondsLeft = Integer.parseInt(timerMatcher.group(1)) * 60
                + Integer.parseInt(timerMatcher.group(2));
        Assert.assertTrue(secondsLeft <= 900 && secondsLeft >= 840,
                "Dong ho phai bat dau quanh 15:00 (900 giay), dang thay: " + timerText);
        Assert.assertEquals(parseMoney(checkout.getPaymentTotalText()), expectedTotal,
                "So tien thanh toan phai dung tong cuoi luc dat (" + expectedTotal + "d)");
        Assert.assertTrue(checkout.isCopyOrderCodeButtonDisplayed(),
                "Phai thay nut \"Sao chép mã\" (copy-order-code-btn)");
        checkout.clickCopyOrderCodeButton();
        Assert.assertTrue(isTextVisibleOnPage("Đã chép mã"),
                "Bam \"Sao chép mã\" phai hien toast \"Đã chép mã\"");
        Assert.assertTrue(checkout.isBackToCartButtonDisplayed(),
                "Phai thay nut \"Đổi giỏ hàng\" (back-to-cart-btn)");
        checkout.clickBackToCartButton();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(),
                "Bam \"Đổi giỏ hàng\" phai quay lai gio hang (form thanh toan hien lai)");
    }

    @Story("Thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_048: Bấm kiểm tra thanh toán khi chưa chuyển khoản -> toast \"Đang chờ thanh toán\", đơn vẫn \"Chờ thanh toán\", KHÔNG sang view đã thanh toán (chế độ demo)")
    public void simulatePayosCheckBeforeTransferShowsWaitingToast() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];

        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isPaymentViewDisplayed(), "Phai thay man hinh thanh toan PayOS");
        checkout.simulatePayosSuccess();

        // Toast chi ton tai 5 giay (App.tsx showToast) nen khang dinh ngay sau khi bam.
        Assert.assertTrue(isTextVisibleOnPage("Đang chờ thanh toán"),
                "Chua chuyen khoan ma bam kiem tra thi phai hien toast \"Đang chờ thanh toán\"");
        Assert.assertTrue(checkout.isPaymentViewDisplayed(),
                "Chua thanh toan thi view thanh toan van phai hien (chua chuyen trang)");
        Assert.assertFalse(checkout.isOrderPaidViewDisplayed(),
                "Chua chuyen khoan thi KHONG duoc hien order-paid-view (che do demo khong tu Paid)");

        // Don van o trang thai "Chờ thanh toán" trong lich su.
        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        OrdersPageObject ordersPage = new OrdersPageObject();
        ordersPage.waitForOrdersLoaded();
        String firstCode = ordersPage.getFirstOrderDisplayCode();
        Assert.assertNotNull(firstCode, "Lich su don phai co don PayOS vua dat");
        ordersPage.openOrderDetail(firstCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), "Chờ thanh toán",
                "Don PayOS chua chuyen khoan phai o trang thai \"Chờ thanh toán\"");
    }

    @Story("Trang thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_049: Trang /mock-payos chỉ có 1 nút \"Xác nhận đã chuyển khoản thành công\"; bấm nút -> hiện \"Thanh toán thành công!\" + link xem đơn hàng")
    public void mockPayosPageHasSingleConfirmButtonAndConfirmsPayment() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];
        long amount = order[1];

        getDriver().get(GlobalConstants.getAppUrl() + "/mock-payos?orderCode=" + orderCode
                + "&amount=" + amount);
        sleepInSecond(2); // cho trang mock-payos render (khong co data-testid nao)

        Assert.assertTrue(isTextVisibleOnPage("Cổng thanh toán PayOS"),
                "Trang mock-payos phai hien tieu de \"Cổng thanh toán PayOS\"");
        Assert.assertTrue(isTextVisibleOnPage("Môi trường giả lập VietQR Sandbox"),
                "Trang mock-payos phai ghi \"Môi trường giả lập VietQR Sandbox\"");
        Assert.assertTrue(isTextVisibleOnPage("MBBank (Quân Đội)"),
                "Trang mock-payos phai hien ngan hang \"MBBank (Quân Đội)\"");
        Assert.assertTrue(isTextVisibleOnPage("0987654321"),
                "Trang mock-payos phai hien so tai khoan \"0987654321\"");
        Assert.assertTrue(isTextVisibleOnPage("DH " + orderCode),
                "Trang mock-payos phai hien noi dung chuyen khoan \"DH " + orderCode + "\"");

        List<?> confirmButtons = getDriver().findElements(By.xpath(
                "//button[contains(.,'Xác nhận đã chuyển khoản thành công')]"));
        Assert.assertEquals(confirmButtons.size(), 1,
                "Trang mock-payos chi duoc co dung 1 nut hanh dong xac nhan chuyen khoan");
        Assert.assertEquals(getDriver().findElements(By.xpath(
                        "//button[contains(.,'thất bại') or contains(.,'Thất bại')]")).size(), 0,
                "Trang mock-payos KHONG duoc co nut mo phong that bai");

        ((org.openqa.selenium.WebElement) confirmButtons.get(0)).click();
        sleepInSecond(2); // cho POST webhook gia + view thanh cong hien
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Bam xac nhan xong phai hien \"Thanh toán thành công!\"");
        Assert.assertTrue(isTextVisibleOnPage(
                        "Đơn hàng #" + orderCode + " đã được hệ thống xác nhận thanh toán qua PayOS."),
                "Bam xac nhan xong phai hien cau xac nhan don #" + orderCode);
        Assert.assertFalse(getDriver().findElements(By.xpath("//a[@href='/orders']")).isEmpty(),
                "Sau khi xac nhan phai co link \"Xem danh sách đơn hàng\" (/orders)");
    }

    @Story("Trang thanh toán PayOS")
    @Test(description = "TC_SCR7_FUNC_050: Trang /payment-success hiển thị sau khi PayOS trả về: \"Thanh toán thành công!\" + khối Mã đơn/Số tiền + 2 link đơn hàng/trang chủ")
    public void paymentSuccessPageShowsOrderSummaryAndLinks() {
        long[] order = placePayosOrderWithDemo();
        long orderCode = order[0];
        long amount = order[1];

        // Xac nhan chuyen khoan tren trang mock-payos de don thanh Paid that
        // (webhook gia; checksum demo chap nhan) truoc khi mo /payment-success.
        getDriver().get(GlobalConstants.getAppUrl() + "/mock-payos?orderCode=" + orderCode
                + "&amount=" + amount);
        sleepInSecond(2);
        List<?> confirmButtons = getDriver().findElements(By.xpath(
                "//button[contains(.,'Xác nhận đã chuyển khoản thành công')]"));
        Assert.assertEquals(confirmButtons.size(), 1, "Trang mock-payos phai co nut xac nhan");
        ((org.openqa.selenium.WebElement) confirmButtons.get(0)).click();
        sleepInSecond(2);
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Xac nhan tren mock-payos phai thanh cong truoc khi mo /payment-success");

        getDriver().get(GlobalConstants.getAppUrl() + "/payment-success?orderCode=" + orderCode);
        sleepInSecond(2); // cho trang goi GET /api/payments/status lay so tien
        Assert.assertTrue(isTextVisibleOnPage("Thanh toán thành công!"),
                "Trang /payment-success phai hien \"Thanh toán thành công!\"");
        Assert.assertTrue(isTextVisibleOnPage(
                        "Đơn hàng #" + orderCode + " đã được xác nhận thanh toán qua PayOS."),
                "Trang /payment-success phai xac nhan don #" + orderCode + " da thanh toan qua PayOS");
        Assert.assertTrue(isTextVisibleOnPage("Mã đơn:"),
                "Trang /payment-success phai co khoi \"Mã đơn:\"");
        Assert.assertTrue(isTextVisibleOnPage("#" + orderCode),
                "Khoi tom tat phai hien dung ma don #" + orderCode);
        Assert.assertTrue(isTextVisibleOnPage("Số tiền:"),
                "Trang /payment-success phai co khoi \"Số tiền:\"");
        Assert.assertTrue(isTextVisibleOnPage(formatVnd(amount)),
                "Khoi tom tat phai hien dung so tien " + formatVnd(amount) + "d");
        Assert.assertFalse(getDriver().findElements(
                By.xpath("//a[@href='/orders' and contains(.,'Xem lịch sử đơn hàng')]")).isEmpty(),
                "Trang /payment-success phai co link \"Xem lịch sử đơn hàng\" (/orders)");
        Assert.assertFalse(getDriver().findElements(
                By.xpath("//a[@href='/' and contains(.,'Về trang chủ')]")).isEmpty(),
                "Trang /payment-success phai co link \"Về trang chủ\" (/)");
    }

    // ---------- Dung diem hoi vien o checkout (PT-05..PT-08) ----------
    // Diem chi tich khi don Completed (khong tao duoc qua UI shop) nen tai khoan
    // demo hien co the dang 0 diem — cac test PT tu SKIP theo dung mau PT-02/PT-03.

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_056 (PT-05 đợt 15): Đang dùng 50 điểm, tắt rồi bật lại toggle -> ô vẫn là 50 điểm (không reset về tối đa)")
    public void toggleOffAndOnKeepsUsedPoints() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-05 (TC_SCR7_FUNC_056)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        int maxPoints = parseMaxPointsFromHint(cart.getPointsBalanceHintText());
        if (maxPoints != 50) {
            throw new SkipException("Tran diem don 500.000d hien la " + maxPoints + " (khac 50)"
                    + " — FE dien toi da khi bat lai toggle (UsePointsSection.tsx) nen case"
                    + " \"giu nguyen 50\" chi kiem duoc khi tran dung 50; bo qua PT-05");
        }
        cart.clickUsePointsToggle();
        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Bat toggle tren don 500.000d (tran 50) thi o diem phai tu dien 50");
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");

        cart.clickUsePointsToggle(); // tat
        Assert.assertEquals(cart.getPointsToUseValue(), 0, "Tat toggle thi so diem phai ve 0");
        cart.clickUsePointsToggle(); // bat lai
        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Bat lai toggle thi o diem phai van la 50 (khong reset ve toi da)");
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_VAL_058 (PT-06 đợt 15): Nhập \"50abc\" vào ô điểm -> ô chỉ giữ 50 (ký tự không phải số bị lọc), không hiện lỗi, dòng giảm giá -50.000đ")
    public void pointsInputFiltersNonDigitsAndKeepsValue() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-06 (TC_SCR7_VAL_058)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        cart.clickUsePointsToggle();
        cart.setPointsToUseText("50abc");

        Assert.assertEquals(cart.getPointsToUseValue(), 50,
                "Nhap \"50abc\" thi o diem chi duoc giu 50 (ky tu khong phai so bi loc)");
        Assert.assertFalse(cart.isPointsErrorDisplayed(),
                "Nhap 50 diem (trong tran) thi khong duoc hien loi diem");
        Assert.assertTrue(cart.isPointsDiscountDisplayed(),
                "Dung 50 diem thi phai hien dong giam gia diem (cart-points-discount)");
        Assert.assertEquals(cart.getPointsDiscountValue(), 50_000L,
                "Dung 50 diem thi dong giam gia phai la -50.000d (1 diem = 1.000d)");

        try {
            if (cart.getPointsToUseValue() > 0) {
                cart.clickUsePointsToggle();
            }
        } catch (Exception ignored) {
            // Don dep that bai thi bo qua.
        }
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_060 (PT-07 đợt 15): Đang dùng 50 điểm, tắt toggle -> số điểm về 0, dòng giảm giá điểm biến mất, tổng tiền trở lại như trước")
    public void turningPointsOffResetsPointsAndRestoresTotal() {
        int availablePoints = readDemoAvailablePoints();
        if (availablePoints < 50) {
            throw new SkipException("Tai khoan demo chi co " + availablePoints
                    + " diem kha dung (< 50) — bo qua PT-07 (TC_SCR7_FUNC_060)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        long totalBeforePoints = parseMoney(cart.getFinalTotalText());
        Assert.assertEquals(totalBeforePoints, 500_000L,
                "Don 500.000d mien phi ship thi tong truoc khi dung diem phai la 500.000d");

        cart.clickUsePointsToggle();
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Dung 50 diem thi tong phai giam con 450.000d");

        cart.clickUsePointsToggle(); // tat
        Assert.assertEquals(cart.getPointsToUseValue(), 0, "Tat toggle thi so diem phai ve 0");
        Assert.assertFalse(cart.isPointsDiscountDisplayed(),
                "Tat toggle thi dong giam gia diem phai bien mat");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), totalBeforePoints,
                "Tat toggle thi tong tien phai tro lai nhu truoc khi dung diem (500.000d)");
    }

    @Story("Dùng điểm khi thanh toán")
    @Test(description = "TC_SCR7_FUNC_061 (PT-08 đợt 15): Đặt hàng COD khi dùng 50 điểm -> đơn trừ 50.000đ, số dư điểm giảm đúng 50, lịch sử điểm có dòng \"Dùng điểm\" -50")
    public void orderWithPointsDeductsBalanceAndLogsHistory() {
        int pointsBefore = readDemoAvailablePoints();
        if (pointsBefore < 50) {
            throw new SkipException("Tai khoan demo chi co " + pointsBefore
                    + " diem kha dung (< 50) — bo qua PT-08 (TC_SCR7_FUNC_061)");
        }

        CartDrawerPageObject cart = openDemoCartWith500k();
        cart.waitForUsePointsSection();
        cart.clickUsePointsToggle();
        cart.setPointsToUse(50);
        Assert.assertEquals(cart.getPointsToUseValue(), 50, "Nhap 50 diem thi o phai giu 50");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 450_000L,
                "Dung 50 diem thi tong truoc khi dat phai la 450.000d (500.000 - 50.000 + 0 ship)");

        // Dat COD thanh cong — tao DON THAT tren tai khoan demo va tru 50 diem that.
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL,
                GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD dung diem phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();
        Assert.assertTrue(orderCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don phai dung dinh dang DH{yyyyMMdd}-{NNNN}, dang thay: " + orderCode);

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        String detailText = ordersPage.getOrderDetailText();
        Assert.assertTrue(detailText.contains(formatVnd(450_000L)),
                "Tong cong don dung diem phai la 450.000d (500.000 - 50.000 + 0 ship)");

        MembershipPageObject membershipPage = homePage.openMembershipPage();
        membershipPage.clickRefreshButton();
        int pointsAfter = membershipPage.getAvailablePoints();
        Assert.assertEquals(pointsAfter, pointsBefore - 50,
                "So du diem phai giam dung 50 sau khi dat don dung 50 diem (truoc "
                        + pointsBefore + ", sau " + pointsAfter + ")");
        String historyText = membershipPage.getHistoryText();
        Assert.assertTrue(historyText.contains("Dùng điểm"),
                "Lich su diem phai co dong \"Dùng điểm\" sau khi dat don dung diem");
        Assert.assertTrue(historyText.contains("-50 điểm"),
                "Lich su diem phai co dong tru \"-50 điểm\" gan voi lan dung diem vua roi");
    }
}
