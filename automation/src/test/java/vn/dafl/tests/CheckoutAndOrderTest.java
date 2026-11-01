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
import vn.dafl.pageObjects.ProfilePageObject;

/**
 * CN-07 Thanh toan & dat hang (ban chot v5 + override O1: giu PayOS va voucher cua ban app hien tai).
 * Dat hang bat buoc co khoi thong tin giao hang shipping-* day du; khach chua dang nhap
 * bam thanh toan se bi dua qua dang nhap roi quay lai dung trang gio (luong E4).
 */
public class CheckoutAndOrderTest extends BaseTest {

    @Test(description = "CN-07 TC-CN07-01: Dat hang COD thanh cong -> ma don dang DH{yyyyMMdd}-{NNNN}, don o 'Cho xac nhan'")
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

    @Test(description = "CN-07 (override O1): Thanh toan PayOS (mock) thanh cong va thay don trong lich su")
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
        Assert.assertTrue(checkout.isOrderPaidViewDisplayed(), "Gia lap PayOS thanh cong phai bao da thanh toan");

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        String firstCode = ordersPage.getFirstOrderDisplayCode();
        Assert.assertNotNull(firstCode, "Lich su don phai co don vua thanh toan");
        Assert.assertTrue(firstCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don moi nhat phai dung dinh dang DH, dang thay: " + firstCode);
    }

    @Test(description = "TC-CN07-02 (luong E4): Khach bam thanh toan -> bi dua qua dang nhap -> dang nhap xong quay lai dung trang gio")
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

    @Test(description = "CN-07.1 + CN-03.3: Dat hang bang dia chi da luu trong so dia chi (khong nhap lai form)")
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

    @Test(description = "TC-CN07-03 (CN-07 E5): Bam nut Dat hang 2 lan lien tiep chi tao dung 1 don (idempotency)")
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

    @Test(description = "CN-07 E8: Ghi chu don hang qua 200 ky tu bi chan ngay tren form")
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

    @Test(description = "CN-07 (override O1): Phi ship 25.000d khi duoi 299.000d va mien phi khi dat nguong")
    public void shippingFeeIs25kBelowThresholdAndFreeAbove() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        homePage.searchProduct("Khăn ướt");
        String productId = catalog.getFirstProductId();
        Assert.assertNotNull(productId, "Phai tim thay san pham khan uot trong du lieu seed");
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
}
