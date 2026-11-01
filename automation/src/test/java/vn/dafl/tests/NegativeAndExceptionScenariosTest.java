package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

/**
 * Kich ban am tinh / ngoai le theo dac ta CN-01..CN-08 (ban chot v5):
 * auth sai dinh dang/thieu truong, mat khau khong dat policy nghiem ngat,
 * voucher sai/bi Backend tu choi luc dat hang, loi khoi giao hang shipping-*,
 * gio trong va rieng tu don hang giua cac tai khoan.
 */
public class NegativeAndExceptionScenariosTest extends BaseTest {

    // ---------------- NEG-AUTH: dang nhap sai ----------------

    @Test(description = "NEG-AUTH-01 (CN-02 E1): Dang nhap bang email chua dang ky -> 'Email hoac mat khau khong dung'")
    public void loginWithUnregisteredEmail() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs("chua-tung-dang-ky@dafl.vn", GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Phai bao loi duoi o mat khau");
        Assert.assertEquals(authModal.getLoginPasswordError(), GlobalConstants.LOGIN_ERROR_MESSAGE,
                "Thong bao loi dang nhap phai dung cau cua SUT");
        Assert.assertTrue(authModal.isModalStillOpen(), "Dang nhap that bai thi modal van mo");
    }

    @Test(description = "NEG-AUTH-02: Dang nhap de trong ca email va mat khau -> loi ngay duoi tung o, khong dang nhap")
    public void loginWithEmptyFields() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.submitLogin();

        Assert.assertTrue(authModal.isLoginEmailErrorDisplayed() || authModal.isLoginPasswordErrorDisplayed(),
                "De trong truong dang nhap phai co loi inline");
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal phai con mo");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(), "Khong duoc cap token");
    }

    @Test(description = "NEG-AUTH-03: Dang nhap bang email sai dinh dang -> loi dinh dang duoi o email")
    public void loginWithInvalidEmailFormat() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs("day-khong-phai-email", GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isLoginEmailErrorDisplayed(),
                "Email sai dinh dang phai bao loi duoi o email");
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal phai con mo");
    }

    // ---------------- NEG-REG: mat khau khong dat policy nghiem ngat ----------------

    private AuthModalPageObject openRegisterForm() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();
        return authModal;
    }

    @Test(description = "NEG-REG-01: Dang ky mat khau qua ngan (< 8 ky tu) bi tu choi")
    public void registerWithTooShortPassword() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), randomPhone(), "Pa1!", "Pa1!");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PASSWORD_ERROR),
                "Mat khau ngan phai bao loi duoi o mat khau");
        Assert.assertFalse(authModal.isRegisterSuccessDisplayed(), "Khong duoc dang ky thanh cong");
    }

    @Test(description = "NEG-REG-02: Dang ky mat khau thieu chu hoa bi tu choi")
    public void registerPasswordMissingUppercase() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), randomPhone(), "password123!", "password123!");
        Assert.assertFalse(authModal.isUppercaseRuleSatisfied(), "Quy tac chu hoa phai chua dat");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PASSWORD_ERROR),
                "Thieu chu hoa phai bao loi mat khau");
    }

    @Test(description = "NEG-REG-03: Dang ky mat khau thieu ky tu dac biet bi tu choi")
    public void registerPasswordMissingSpecialChar() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), randomPhone(), "Password123", "Password123");
        Assert.assertFalse(authModal.isSpecialRuleSatisfied(), "Quy tac ky tu dac biet phai chua dat");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PASSWORD_ERROR),
                "Thieu ky tu dac biet phai bao loi mat khau");
    }

    @Test(description = "NEG-REG-04: Dang ky mat khau thieu chu so bi tu choi")
    public void registerPasswordMissingDigit() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), randomPhone(), "Password!", "Password!");
        Assert.assertFalse(authModal.isDigitRuleSatisfied(), "Quy tac chu so phai chua dat");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PASSWORD_ERROR),
                "Thieu chu so phai bao loi mat khau");
    }

    @Test(description = "NEG-REG-05 (CN-01 E5): Mat khau xac nhan khong khop -> loi duoi o xac nhan")
    public void registerWithMismatchedConfirmPassword() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), randomPhone(),
                GlobalConstants.DEMO_PASSWORD, "Password123@");
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_CONFIRM_PASSWORD_ERROR),
                "Xac nhan khong khop phai bao loi duoi o xac nhan mat khau");
        Assert.assertTrue(authModal.getRegisterConfirmPasswordError().contains("không khớp"),
                "Noi dung loi xac nhan khong dung: " + authModal.getRegisterConfirmPasswordError());
    }

    @Test(description = "NEG-REG-06: Dang ky SDT sai dinh dang (9 so) bi tu choi")
    public void registerWithInvalidPhoneFormat() {
        AuthModalPageObject authModal = openRegisterForm();
        authModal.register("Selenium Tester", randomEmail(), "091234567",
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PHONE_ERROR),
                "SDT 9 so phai bao loi duoi o SDT");
        Assert.assertFalse(authModal.isRegisterSuccessDisplayed(), "Khong duoc dang ky thanh cong");
    }

    // ---------------- NEG-VCH: voucher ----------------

    @Test(description = "NEG-VCH-01: Ap ma giam gia khong ton tai -> thong bao loi dung cua SUT")
    public void applyInvalidVoucherCode() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        cart.applyVoucher("MA-KHONG-TON-TAI-2026");

        Assert.assertTrue(cart.isVoucherErrorDisplayed(), "Ap ma sai phai hien loi voucher");
        Assert.assertTrue(cart.getVoucherErrorMessage().contains(GlobalConstants.VOUCHER_ERROR_MESSAGE),
                "Thong bao loi voucher khong dung: " + cart.getVoucherErrorMessage());
        Assert.assertFalse(cart.isVoucherAppliedBannerDisplayed(), "Ma sai khong duoc danh dau da ap dung");
    }

    @Test(description = "NEG-VCH-02: Ap ma giam gia rong -> khong ap dung, khong bao thanh cong")
    public void applyEmptyVoucherCode() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        cart.applyVoucher("");

        Assert.assertFalse(cart.isVoucherAppliedBannerDisplayed(),
                "Ma rong khong duoc coi la ap dung thanh cong");
    }

    @Test(description = "NEG-VCH-03 (TC-CN07-04): Gio tut xuong duoi muc toi thieu sau khi ap ma -> Backend TU CHOI dat hang kem thong diep rieng")
    public void voucherRejectedAtCheckoutWhenCartDropsBelowMinimum() {
        loginWithDemoAccount();
        HomePageObject homePage = new HomePageObject();
        homePage.searchProduct("Khăn ướt");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai tim thay san pham khan uot");
        long unitPrice = catalog.getProductPrice(productId);
        Assert.assertTrue(unitPrice < 100_000, "Test nay can san pham don gia duoi 100.000d");

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        CartDrawerPageObject cart = homePage.openCart();
        int needed = (int) Math.ceil(100_000.0 / unitPrice);
        cart.setItemQuantity(productId, needed);
        cart.waitForItemQuantity(productId, needed);
        cart.applyVoucher(GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap DAFL10 o muc >= 100.000d phai thanh cong");

        // Ha so luong xuong 1 -> tam tinh < 100.000d, voucher khong con hop le
        cart.setItemQuantity(productId, 1);
        cart.waitForItemQuantity(productId, 1);

        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isCheckoutErrorDisplayed(),
                "Backend phai tu choi dat hang va hien loi tai checkout");
        Assert.assertTrue(checkout.getCheckoutErrorMessage().contains(GlobalConstants.VOUCHER_MIN_ORDER_MESSAGE),
                "Thong diep tu choi phai noi ve don toi thieu: " + checkout.getCheckoutErrorMessage());
        Assert.assertFalse(checkout.isOrderSuccessViewDisplayed(), "Khong duoc dat hang thanh cong");
    }

    // ---------------- NEG-SRCH ----------------

    @Test(description = "NEG-SRCH-01: Tim kiem san pham khong ton tai -> trang thai rong, khong loi he thong")
    public void searchForNonExistentProduct() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        new HomePageObject().searchProduct("xyz-khong-co-san-pham-nay-123");

        Assert.assertEquals(catalog.getProductCountOnPage(), 0, "Khong duoc hien san pham nao");
        Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(), "Phai hien trang thai rong cua danh muc");
    }

    // ---------------- NEG-CHK: loi khoi giao hang ----------------

    @Test(description = "NEG-CHK-01: Dat hang voi SDT giao hang sai dinh dang -> loi shipping-phone-error, khong tao don")
    public void checkoutWithInvalidPhone() {
        loginWithDemoAccount();
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, "123",
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD,
                GlobalConstants.SHIPPING_DETAIL, null);
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(vn.dafl.pageUIs.CheckoutPageUI.SHIPPING_PHONE_ERROR),
                "SDT giao hang sai phai bao loi duoi o SDT");
        Assert.assertFalse(checkout.isOrderSuccessViewDisplayed(), "Khong duoc tao don voi SDT sai");
    }

    @Test(description = "NEG-CHK-02: Dat hang de trong dia chi chi tiet -> loi shipping-detail-error, khong tao don")
    public void checkoutWithEmptyShippingDetail() {
        loginWithDemoAccount();
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);

        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE,
                GlobalConstants.SHIPPING_PROVINCE, GlobalConstants.SHIPPING_DISTRICT, GlobalConstants.SHIPPING_WARD,
                "", null);
        checkout.selectCodPayment();
        checkout.submitOrder();

        Assert.assertTrue(checkout.isFieldErrorDisplayed(vn.dafl.pageUIs.CheckoutPageUI.SHIPPING_DETAIL_ERROR),
                "Dia chi chi tiet trong phai bao loi duoi o dia chi");
        Assert.assertFalse(checkout.isOrderSuccessViewDisplayed(), "Khong duoc tao don khi thieu dia chi");
    }

    @Test(description = "NEG-CHK-03: Gio trong -> khong co nut thanh toan de dat hang")
    public void checkoutWithEmptyCart() {
        registerAndLogin("Khach Gio Trong", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        HomePageObject homePage = new HomePageObject();
        CartDrawerPageObject cart = homePage.openCart();

        Assert.assertTrue(cart.isCartEmpty(), "Tai khoan moi phai co gio trong");
        Assert.assertFalse(new CheckoutPageObject().isCheckoutSubmitDisplayed(),
                "Gio trong thi khong duoc hien nut thanh toan");
    }

    // ---------------- NEG-ORD: rieng tu don hang ----------------

    @Test(description = "NEG-ORD-01 (TC-CN08-02): Tai khoan khac khong thay don hang cua tai khoan demo trong danh sach cua minh")
    public void cannotSeeAnotherUsersOrder() {
        // Tai khoan demo dat mot don COD
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        int badgeBefore = homePage.getCartBadgeCount();
        catalog.addToCart(catalog.getFirstProductId());
        homePage.waitForCartBadgeCount(badgeBefore + 1);
        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Don cua demo phai dat thanh cong");
        String demoOrderCode = checkout.getOrderSuccessCode();

        homePage.logout();
        homePage.waitForLoggedOut();

        // Tai khoan moi dang nhap: danh sach don cua ho khong duoc chua don cua demo
        registerAndLogin("Khach La", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD);
        OrdersPageObject ordersPage = new HomePageObject().openOrdersPage();
        Assert.assertFalse(ordersPage.isElementDisplayed("css=[data-testid='order-item-" + demoOrderCode + "']"),
                "Khong duoc thay don hang cua nguoi khac trong danh sach cua minh");
    }
}
