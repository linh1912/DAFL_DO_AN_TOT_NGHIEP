package vn.dafl.tests.web.cart;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.List;

/**
 * CN-06 Gio hang (ban chot v5): KHACH VANG LAI dung gio duoc ngay (khong con cong dang nhap),
 * gio khach luu o localStorage va GOP vao gio tren tai khoan khi dang nhap (POST /api/cart/merge).
 * So luong nhap truc tiep qua o input; giam ve 0 / bam Xoa phai xac nhan (CN-06.3).
 */
@Feature("Giỏ hàng")
public class CartManagementTest extends BaseTest {

    @Story("Thêm vào giỏ")
    @Test(description = "TC-CN06-01: Khách chưa đăng nhập thêm sản phẩm vào giỏ được ngay, không bị bắt đăng nhập")
    public void addProductToCartAsGuest() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        String productName = catalog.getProductName(productId);

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        Assert.assertFalse(new AuthModalPageObject().isModalStillOpen(),
                "Khach them gio khong duoc bi hien form dang nhap (ban CN bo cong dang nhap)");

        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertTrue(cart.isItemDisplayed(productId), "San pham vua them khong co trong gio khach");
        Assert.assertEquals(cart.getItemName(productId), productName, "Ten san pham trong gio khong khop");
        Assert.assertEquals(cart.getItemQuantity(productId), 1, "So luong mac dinh phai la 1");
    }

    @Story("Thêm vào giỏ")
    @Test(description = "CN-06.1: Thêm cùng một sản phẩm hai lần -> số lượng cộng dồn thành 2")
    public void addSameProductTwiceAccumulates() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(2);

        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertEquals(cart.getItemQuantity(productId), 2, "Them 2 lan cung san pham phai ra so luong 2");
    }

    @Story("Số lượng")
    @Test(description = "CN-06.2: Tăng số lượng trong giỏ -> tạm tính tăng tương ứng")
    public void increaseQuantityUpdatesTotal() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        long unitPrice = parseMoney(cart.getElementText("css=[data-testid='cart-item-price-" + productId + "']"));
        cart.increaseQuantity(productId);

        Assert.assertEquals(cart.getItemQuantity(productId), 2, "So luong sau khi tang phai la 2");
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), unitPrice * 2,
                "Tam tinh phai bang 2 lan don gia");
    }

    @Story("Số lượng")
    @Test(description = "TC-CN06-02 (CN-06.3): Giảm số lượng từ 1 -> hỏi xác nhận, đồng ý thì xóa khỏi giỏ")
    public void decreaseQuantityFromOneAsksConfirmation() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        cart.decreaseQuantity(productId);

        Assert.assertTrue(cart.isRemoveConfirmDialogDisplayed(),
                "Giam so luong tu 1 phai mo hop xac nhan xoa");
        cart.confirmRemoveItem();
        cart.sleepInSecond(2);
        Assert.assertTrue(cart.isCartEmpty(), "Xac nhan xoa xong gio phai trong");
    }

    @Story("Xóa khỏi giỏ")
    @Test(description = "CN-06.3: Bấm Xóa -> hủy bỏ thì giữ nguyên, xác nhận thì mới xóa")
    public void removeItemRequiresConfirmation() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        cart.clickRemoveItem(productId);
        Assert.assertTrue(cart.isRemoveConfirmDialogDisplayed(), "Bam Xoa phai mo hop xac nhan");

        cart.cancelRemoveItem();
        cart.sleepInSecond(1);
        Assert.assertTrue(cart.isItemDisplayed(productId), "Huy bo xoa thi san pham phai con trong gio");

        cart.clickRemoveItem(productId);
        cart.confirmRemoveItem();
        cart.sleepInSecond(2);
        Assert.assertTrue(cart.isCartEmpty(), "Xac nhan xoa xong gio phai trong");
    }

    @Story("Số lượng")
    @Test(description = "CN-06 E2: Gõ số lượng vượt tồn kho -> hệ thống tự kẹp về đúng số lượng tồn kho")
    public void setQuantityBeyondStockClampsToStock() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();

        catalog.openProductDetail(productId);
        int stock = catalog.getDetailAvailableStock();
        Assert.assertTrue(stock > 0, "Khong doc duoc ton kho o trang chi tiet");
        catalog.backFromDetail();

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        CartDrawerPageObject cart = homePage.openCart();
        cart.setItemQuantity(productId, stock + 50);

        int settled = cart.waitForQuantityToSettle(productId);
        Assert.assertEquals(settled, stock, "So luong phai bi kep ve dung ton kho " + stock);
    }

    @Story("Voucher")
    @Test(description = "CN-07 (voucher): Áp mã DAFL20 hợp lệ -> tổng tiền giảm đúng 20.000đ")
    public void applyVoucherReducesTotal() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        long unitPrice = catalog.getProductPrice(productId);
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        // DAFL20 yeu cau don toi thieu 100.000d: nang so luong neu don gia nho
        if (unitPrice < 100_000) {
            int needed = (int) Math.ceil(100_000.0 / unitPrice);
            cart.setItemQuantity(productId, needed);
            cart.waitForItemQuantity(productId, needed);
        }
        long subtotal = parseMoney(cart.getSubtotalText());
        long shipping = subtotal >= GlobalConstants.FREE_SHIPPING_THRESHOLD ? 0 : GlobalConstants.SHIPPING_FEE;

        cart.applyVoucher(GlobalConstants.VOUCHER_FIXED_20K);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma hop le phai hien banner da ap dung");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), subtotal + shipping - 20_000,
                "Tong tien phai giam dung 20.000d sau voucher");
    }

    @Story("Đồng bộ giỏ hàng")
    @Test(description = "TC-CN06-03: Đăng nhập -> giỏ khách được GỘP vào giỏ tài khoản (cộng dồn số lượng)")
    public void guestCartMergesIntoAccountCartOnLogin() {
        String email = randomEmail();
        String phone = randomPhone();
        String password = GlobalConstants.DEMO_PASSWORD;

        // Buoc 1: tai khoan moi dang nhap, them san pham A vao gio server roi dang xuat
        HomePageObject homePage = registerAndLogin("Khach Gop Gio", email, phone, password);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = catalog.getDisplayedProductIds();
        Assert.assertTrue(ids.size() >= 2, "Can it nhat 2 san pham de test gop gio");
        String productA = ids.get(0);
        String productB = ids.get(1);
        catalog.addToCart(productA);
        homePage.waitForCartBadgeCount(1);
        homePage.logout();
        homePage.waitForLoggedOut();

        // Buoc 2: o che do khach, them A x1 va B x1 vao gio khach (localStorage)
        catalog.addToCart(productA);
        catalog.addToCart(productB);
        homePage.waitForCartBadgeCount(2);

        // Buoc 3: dang nhap lai -> gio khach gop vao gio tai khoan: A = 2, B = 1
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.loginAs(email, password);
        homePage.waitForLoggedIn();

        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertEquals(cart.getItemQuantity(productA), 2,
                "San pham A phai duoc cong don: 1 (gio server) + 1 (gio khach)");
        Assert.assertEquals(cart.getItemQuantity(productB), 1,
                "San pham B cua gio khach phai duoc gop sang gio tai khoan");
    }

    @Story("Đồng bộ giỏ hàng")
    @Test(description = "CN-06.4: Giỏ của tài khoản được lưu phía server - đăng xuất rồi đăng nhập lại vẫn còn nguyên")
    public void accountCartPersistsAfterLogoutAndRelogin() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Luu Gio", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        homePage.logout();
        homePage.waitForLoggedOut();

        loginAs(email, GlobalConstants.DEMO_PASSWORD);
        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertTrue(cart.isItemDisplayed(productId), "Gio server phai con san pham sau dang nhap lai");
        Assert.assertEquals(cart.getItemQuantity(productId), 1, "So luong trong gio server phai giu nguyen");
    }
}
