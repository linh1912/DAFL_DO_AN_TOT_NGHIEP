package vn.dafl.tests.web;

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

/**
 * CN-07 Thanh toan & dat hang (ban chot v5 + override O1: giu PayOS va voucher cua ban app hien tai).
 * Dat hang bat buoc co khoi thong tin giao hang shipping-* day du; khach chua dang nhap
 * bam thanh toan se bi dua qua dang nhap roi quay lai dung trang gio (luong E4).
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
}
