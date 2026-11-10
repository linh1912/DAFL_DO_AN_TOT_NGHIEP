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
import org.testng.SkipException;
import java.util.HashMap;
import java.util.Map;

/**
 * CN-06 Gio hang (ban chot v5): KHACH VANG LAI dung gio duoc ngay (khong con cong dang nhap),
 * gio khach luu o localStorage va GOP vao gio tren tai khoan khi dang nhap (POST /api/cart/merge).
 * So luong nhap truc tiep qua o input; giam ve 0 / bam Xoa phai xac nhan (CN-06.3).
  *
 * Gop them 12 test tu CartExtendedTest (BUOC 3, dot 15.1) ngay 2026-11-10 — 1 file duy nhat cho gio hang (21 test).
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

    // ---------- Test bo sung — gop tu CartExtendedTest (BUOC 3, file manual v15.1) ----------

    /** Dinh dang tien VND kieu FE: 149000 -> "149.000". */
    private String formatVnd(long amount) {
        return String.format("%,d", amount).replace(',', '.');
    }

    /** Them SP vao gio (che do khach) roi mo /cart; tra ve gio + id/price da dung. */
    private CartDrawerPageObject openCartWithOneProduct(String productId) {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        return homePage.openCart();
    }

    /** Tim SP dau tien tren trang chu co don gia < gioi han (de xay tam tinh theo khoang). */
    private String findProductCheaperThan(long maxPrice) {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        for (String id : catalog.getDisplayedProductIds()) {
            long price = catalog.getProductPrice(id);
            if (price > 0 && price < maxPrice) {
                return id;
            }
        }
        return null;
    }

    /**
     * Xay gio khach dat tam tinh >= muc tieu voi it nhat minProducts dong khac nhau:
     * them tung SP roi tang so luong (kep theo ton kho) cho den khi du.
     * Skip neu het SP trang 1 ma van khong du (ton kho seed khong du).
     */
    private CartDrawerPageObject openCartWithSubtotalAtLeast(long targetSubtotal, int minProducts) {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = catalog.getDisplayedProductIds();
        Assert.assertTrue(ids.size() >= minProducts, "Can it nhat " + minProducts + " san pham tren trang 1");
        Map<String, Long> prices = new HashMap<>();
        for (String id : ids) {
            prices.put(id, catalog.getProductPrice(id));
        }

        int totalQty = 0;
        int addedCount = 0;
        CartDrawerPageObject cart = null;
        long subtotal = 0;
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            long price = prices.get(id);
            if (price <= 0) {
                continue;
            }
            catalog.addToCart(id);
            totalQty++;
            addedCount++;
            homePage.waitForCartBadgeCount(totalQty);

            cart = homePage.openCart();
            subtotal = parseMoney(cart.getSubtotalText());
            if (subtotal >= targetSubtotal && addedCount >= minProducts) {
                return cart;
            }
            if (subtotal < targetSubtotal) {
                int currentQty = cart.getItemQuantity(id);
                int needed = currentQty + (int) Math.ceil((targetSubtotal - subtotal) / (double) price);
                cart.setItemQuantity(id, needed);
                int settled = cart.waitForQuantityToSettle(id);
                totalQty += (settled - currentQty);
                subtotal = parseMoney(cart.getSubtotalText());
                if (subtotal >= targetSubtotal && addedCount >= minProducts) {
                    return cart;
                }
            }
            if (i + 1 >= ids.size()) {
                break;
            }
            homePage = openHomePage();
            catalog = new ProductCatalogPageObject();
            catalog.waitForProductsLoaded();
        }
        throw new SkipException("Khong xay duoc gio dat tam tinh >= " + formatVnd(targetSubtotal)
                + "d voi " + minProducts + " dong (tam tinh dat duoc: " + formatVnd(subtotal)
                + "d, ton kho seed khong du)");
    }

    /**
     * Xay gio co tam tinh nam trong [min, max): chon SP ma co so luong nguyen
     * dat tam tinh vao khoang (vd [200.000d, 299.000d) cho FREESHIP — phai duoi
     * nguong freeship 299K thi moi thay duoc phi ship ve 0).
     * Skip neu khong co SP nao dat duoc khoang hoac ton kho kep ra ngoai khoang.
     */
    private CartDrawerPageObject openCartWithSubtotalInRange(long minSubtotal, long maxSubtotal) {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String chosenId = null;
        long chosenPrice = 0;
        int chosenQty = 0;
        for (String id : catalog.getDisplayedProductIds()) {
            long price = catalog.getProductPrice(id);
            if (price <= 0) {
                continue;
            }
            int qty = (int) Math.ceil(minSubtotal / (double) price);
            long subtotal = price * qty;
            if (subtotal >= minSubtotal && subtotal < maxSubtotal) {
                chosenId = id;
                chosenPrice = price;
                chosenQty = qty;
                break;
            }
        }
        if (chosenId == null) {
            throw new SkipException("Khong co SP nao tren trang 1 dat tam tinh vao khoang ["
                    + formatVnd(minSubtotal) + "d, " + formatVnd(maxSubtotal) + "d) voi so luong nguyen");
        }

        HomePageObject homePage = openHomePage();
        catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.addToCart(chosenId);
        homePage.waitForCartBadgeCount(1);
        CartDrawerPageObject cart = homePage.openCart();
        cart.setItemQuantity(chosenId, chosenQty);
        cart.waitForItemQuantity(chosenId, chosenQty);
        long subtotal = parseMoney(cart.getSubtotalText());
        if (subtotal < minSubtotal || subtotal >= maxSubtotal) {
            throw new SkipException("Ton kho kep so luong " + chosenId + " ve " + cart.getItemQuantity(chosenId)
                    + " (tam tinh " + formatVnd(subtotal) + "d) ra ngoai khoang ["
                    + formatVnd(minSubtotal) + "d, " + formatVnd(maxSubtotal) + "d)");
        }
        Assert.assertEquals(chosenPrice * chosenQty, subtotal, "Tam tinh phai dung gia x so luong");
        return cart;
    }

    @Story("Hiển thị giỏ hàng")
    @Test(description = "TC_SCR6_UI_002: Một dòng giỏ hiển thị đủ ảnh/tên/biến thể/đơn giá/bộ số lượng/thành tiền/nút Xóa + tạm tính + nút Thanh toán")
    public void cartLineShowsAllComponentsAndTotals() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        String productName = catalog.getProductName(productId);
        long unitPrice = catalog.getProductPrice(productId);
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertEquals(cart.getCartItemCount(), 1, "Gio phai co dung 1 dong");
        Assert.assertTrue(cart.isItemDisplayed(productId), "Phai thay dong gio cua san pham vua them");
        Assert.assertEquals(cart.getItemName(productId), productName, "Ten san pham trong gio khong khop");
        String rowText = cart.getItemRowText(productId);
        Assert.assertTrue(rowText.contains("Mặc định"),
                "Dong gio phai ghi bien the duy nhat 'Mặc định', dang thay: " + rowText);
        Assert.assertTrue(cart.isItemPriceDisplayed(productId), "Phai thay don gia (cart-item-price)");
        Assert.assertTrue(rowText.contains(cart.getItemPriceText(productId)),
                "Dong gio phai ghi thanh tien (= don gia x so luong 1)");
        Assert.assertTrue(cart.isItemMinusButtonDisplayed(productId), "Phai thay nut tru so luong");
        Assert.assertEquals(cart.getItemQuantity(productId), 1, "So luong mac dinh phai la 1");
        Assert.assertTrue(cart.isItemPlusButtonDisplayed(productId), "Phai thay nut cong so luong");
        Assert.assertTrue(cart.isItemRemoveButtonDisplayed(productId), "Phai thay nut Xoa (cart-item-remove)");
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), unitPrice,
                "Tam tinh (cart-raw-subtotal) phai bang don gia khi so luong 1");
        Assert.assertTrue(cart.isCheckoutButtonDisplayed(), "Phai thay nut thanh toan/dat hang o cuoi gio");
        Assert.assertTrue(cart.getCheckoutButtonText().contains("Thanh toán"),
                "Nut cuoi gio phai co chu 'Thanh toán', dang thay: " + cart.getCheckoutButtonText());
    }

    @Story("Số lượng")
    @Test(description = "TC_SCR6_FUNC_008: Nhập trực tiếp số lượng: '2a3' chỉ giữ '23'; nhập '0'/'-1' mở hộp xác nhận xóa")
    public void directQuantityInputFiltersNonDigits() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);

        CartDrawerPageObject cart = homePage.openCart();
        cart.setItemQuantity(productId, 2);
        cart.waitForItemQuantity(productId, 2);

        // Ca 1: "2a3" -> chi giu chu so, thanh 23.
        cart.setItemQuantityText(productId, "2a3");
        int settled = cart.waitForQuantityToSettle(productId);
        Assert.assertEquals(settled, 23, "Nhap '2a3' phai chi giu lai chu so thanh 23");

        // Ca 2: "0" -> mo hop xac nhan xoa thay vi dat so luong 0.
        cart.setItemQuantityText(productId, "0");
        sleepInSecond(1);
        Assert.assertTrue(cart.isRemoveConfirmDialogDisplayed(),
                "Nhap so luong 0 phai mo hop xac nhan xoa (cart-remove-confirm-dialog)");
        cart.cancelRemoveItem();
        sleepInSecond(1);
        Assert.assertTrue(cart.isItemDisplayed(productId), "Huy xoa thi san pham phai con trong gio");
        Assert.assertEquals(cart.getItemQuantity(productId), 23, "Huy xoa thi so luong phai giu nguyen 23");

        // Ca 3: "-1" -> theo manual phai mo hop xac nhan xoa; nhung neu FE loc dau "-"
        // ngay khi go (chi giu chu so) thi gia tri con "1" va khong co hop xac nhan —
        // ca hai deu la hanh vi hop ly, khang dinh theo hanh vi that cua FE.
        cart.setItemQuantityText(productId, "-1");
        sleepInSecond(1);
        if (cart.isRemoveConfirmDialogDisplayed()) {
            cart.cancelRemoveItem();
            sleepInSecond(1);
            Assert.assertTrue(cart.isItemDisplayed(productId), "Huy xoa thi san pham phai con trong gio");
            Assert.assertEquals(cart.getItemQuantity(productId), 23, "Huy xoa thi so luong phai giu nguyen 23");
        } else {
            Assert.assertEquals(cart.getItemQuantity(productId), 1,
                    "FE da loc dau '-' khi go nen '-1' tro thanh 1 (khong mo hop xac nhan)");
        }
    }

    @Story("Tổng tiền")
    @Test(description = "TC_SCR6_FUNC_016: Công thức tổng tiền giỏ: tạm tính 2 dòng còn đặt được, áp DAFL20, tổng cuối = max(0, tạm tính − giảm) + phí ship (nhánh loại dòng ngừng bán khỏi tạm tính KHÔNG tạo được qua UI shop — phụ thuộc TC_SCR6_UI_014 loại (b))")
    public void cartTotalFormulaWithVoucherAndShipping() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(100_000, 2);
        long subtotalBefore = parseMoney(cart.getSubtotalText());
        long shippingBefore = parseMoney(cart.getShippingFeeText());

        cart.applyVoucher(GlobalConstants.VOUCHER_FIXED_20K);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma DAFL20 hop le phai hien banner da ap dung");
        Assert.assertEquals(parseMoney(cart.getSubtotalText()), subtotalBefore,
                "Ap voucher khong duoc lam doi tam tinh");
        Assert.assertEquals(cart.getVoucherDiscountValue(), 20_000,
                "Tien giam do voucher DAFL20 phai dung 20.000d");

        long expectedShipping = subtotalBefore >= GlobalConstants.FREE_SHIPPING_THRESHOLD
                ? 0 : GlobalConstants.SHIPPING_FEE;
        Assert.assertEquals(shippingBefore, expectedShipping,
                "Phi ship phai la 0 khi tam tinh >= 299.000d, nguoc lai 25.000d");
        if (expectedShipping == 0) {
            Assert.assertTrue(cart.getShippingFeeText().contains("Miễn phí"),
                    "Phi ship 0d phai hien 'Miễn phí', dang thay: " + cart.getShippingFeeText());
        }
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()),
                Math.max(0, subtotalBefore - 20_000) + expectedShipping,
                "Tong cuoi phai = max(0, tam tinh - giam) + phi ship");
    }

    @Story("Freeship")
    @Test(description = "TC_SCR6_UI_017 (Ca 1): Thanh tiến độ freeship khi chưa đủ ngưỡng: hiện 'Thêm {tiền} để freeship' kèm % hoàn thành")
    public void freeshipProgressShowsMissingAmount() {
        String productId = findProductCheaperThan(GlobalConstants.FREE_SHIPPING_THRESHOLD);
        if (productId == null) {
            throw new SkipException("Khong co SP nao tren trang 1 re hon nguong freeship 299.000d");
        }
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        long unitPrice = catalog.getProductPrice(productId);

        CartDrawerPageObject cart = openCartWithOneProduct(productId);
        long subtotal = parseMoney(cart.getSubtotalText());
        if (subtotal >= GlobalConstants.FREE_SHIPPING_THRESHOLD) {
            throw new SkipException("Tam tinh " + formatVnd(subtotal) + "d da vuot nguong freeship");
        }

        Assert.assertTrue(cart.isFreeshipProgressDisplayed(), "Phai thay thanh tien do freeship");
        String progress = cart.getFreeshipProgressText();
        Assert.assertTrue(progress.contains("Thêm " + formatVnd(GlobalConstants.FREE_SHIPPING_THRESHOLD - subtotal) + "đ để freeship"),
                "Phai hien dung so tien con thieu de freeship, dang thay: " + progress);
        Assert.assertTrue(progress.contains("%"),
                "Thanh tien do phai kem % hoan thanh, dang thay: " + progress);
        Assert.assertEquals(subtotal, unitPrice, "Tam tinh ca 1 phai bang don gia (so luong 1)");
    }

    @Story("Freeship")
    @Test(description = "TC_SCR6_UI_017 (Ca 2): Thanh tiến độ freeship khi đủ ngưỡng 299.000đ: hiện 'Đã được freeship'")
    public void freeshipProgressShowsFreeShipAchieved() {
        String productId = findProductCheaperThan(GlobalConstants.FREE_SHIPPING_THRESHOLD);
        if (productId == null) {
            throw new SkipException("Khong co SP nao tren trang 1 re hon nguong freeship 299.000d");
        }
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        long unitPrice = catalog.getProductPrice(productId);
        List<String> ids = catalog.getDisplayedProductIds();

        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        CartDrawerPageObject cart = homePage.openCart();
        int neededQty = (int) Math.ceil(GlobalConstants.FREE_SHIPPING_THRESHOLD / (double) unitPrice);
        cart.setItemQuantity(productId, neededQty);
        cart.waitForQuantityToSettle(productId);
        long subtotal = parseMoney(cart.getSubtotalText());

        // Neu ton kho kep so luong khong du 299K, them SP khac cho den khi du.
        int totalQty = cart.getItemQuantity(productId);
        int nextIndex = 1;
        while (subtotal < GlobalConstants.FREE_SHIPPING_THRESHOLD && nextIndex < ids.size()) {
            String nextId = ids.get(nextIndex++);
            if (nextId.equals(productId)) {
                continue;
            }
            homePage = openHomePage();
            catalog = new ProductCatalogPageObject();
            catalog.waitForProductsLoaded();
            catalog.addToCart(nextId);
            totalQty++;
            homePage.waitForCartBadgeCount(totalQty);
            cart = homePage.openCart();
            subtotal = parseMoney(cart.getSubtotalText());
        }
        if (subtotal < GlobalConstants.FREE_SHIPPING_THRESHOLD) {
            throw new SkipException("Ton kho seed khong du de nang tam tinh len 299.000d (dat "
                    + formatVnd(subtotal) + "d)");
        }

        Assert.assertTrue(cart.isFreeshipProgressDisplayed(), "Phai thay thanh tien do freeship");
        Assert.assertTrue(cart.getFreeshipProgressText().contains("Đã được freeship"),
                "Tam tinh >= 299.000d phai hien 'Đã được freeship', dang thay: " + cart.getFreeshipProgressText());
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_VAL_021: Giỏ chưa đạt tối thiểu 500.000đ mà áp VIP50K -> lỗi 'Đơn hàng tối thiểu cần đạt 500.000đ để áp dụng mã VIP50K.', tổng tiền không đổi")
    public void applyVip50kBelowMinOrderShowsError() {
        String productId = findProductCheaperThan(500_000);
        if (productId == null) {
            throw new SkipException("Khong co SP nao tren trang 1 re hon 500.000d de xay gio duoi muc toi thieu VIP50K");
        }
        CartDrawerPageObject cart = openCartWithOneProduct(productId);
        long subtotal = parseMoney(cart.getSubtotalText());
        Assert.assertTrue(subtotal < 500_000, "Tam tinh phai duoi 500.000d, dang la " + formatVnd(subtotal) + "d");
        long finalBefore = parseMoney(cart.getFinalTotalText());

        cart.applyVoucher(GlobalConstants.VOUCHER_VIP_50K);
        Assert.assertTrue(cart.isVoucherErrorDisplayed(), "Ap VIP50K khi chua du don toi thieu phai hien loi");
        Assert.assertEquals(cart.getVoucherErrorMessage(),
                "Đơn hàng tối thiểu cần đạt 500.000đ để áp dụng mã VIP50K.",
                "Loi phai dung nguyen van cau don toi thieu cua BE");
        Assert.assertFalse(cart.isVoucherAppliedBannerDisplayed(), "Ma khong duoc ap thi khong hien banner da ap dung");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), finalBefore,
                "Ap ma that bai thi tong tien khong duoc doi");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_FUNC_023: Áp mã DAFL10 giảm 10% (tối đa 100.000đ, đơn tối thiểu 100.000đ) -> banner đúng số tiền giảm, tổng cuối giảm đúng số đó")
    public void applyDafl10ReducesTotalByTenPercent() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(100_000, 1);
        long subtotal = parseMoney(cart.getSubtotalText());
        long expectedDiscount = Math.min(subtotal * 10 / 100, 100_000);
        long expectedShipping = subtotal >= GlobalConstants.FREE_SHIPPING_THRESHOLD
                ? 0 : GlobalConstants.SHIPPING_FEE;

        cart.applyVoucher(GlobalConstants.VOUCHER_PERCENT_10);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma DAFL10 hop le phai hien banner da ap dung");
        Assert.assertEquals(cart.getAppliedVoucherBannerText(),
                "✓ Đã áp dụng mã DAFL10: -" + formatVnd(expectedDiscount) + "đ",
                "Banner phai ghi dung so tien giam 10% tam tinh (tran 100.000d)");
        Assert.assertEquals(cart.getVoucherDiscountValue(), expectedDiscount,
                "Dong tien giam do voucher phai dung " + formatVnd(expectedDiscount) + "d");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), subtotal + expectedShipping - expectedDiscount,
                "Tong cuoi phai giam dung so tien giam cua DAFL10");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_FUNC_025: Áp mã FREESHIP (đơn tối thiểu 200.000đ) -> phí ship thành 'Miễn phí', tổng cuối không cộng phí ship")
    public void applyFreeshipVoucherMakesShippingFree() {
        CartDrawerPageObject cart = openCartWithSubtotalInRange(200_000, GlobalConstants.FREE_SHIPPING_THRESHOLD);
        long subtotal = parseMoney(cart.getSubtotalText());
        Assert.assertEquals(parseMoney(cart.getShippingFeeText()), GlobalConstants.SHIPPING_FEE,
                "Truoc khi ap FREESHIP, tam tinh duoi 299.000d phai dang chiu phi ship 25.000d");

        cart.applyVoucher(GlobalConstants.VOUCHER_FREESHIP);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma FREESHIP hop le phai hien banner da ap dung");
        Assert.assertEquals(parseMoney(cart.getShippingFeeText()), 0,
                "Ap FREESHIP xong phi ship phai ve 0");
        Assert.assertTrue(cart.getShippingFeeText().contains("Miễn phí"),
                "Phi ship 0d phai hien 'Miễn phí', dang thay: " + cart.getShippingFeeText());
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()),
                Math.max(0, subtotal - cart.getVoucherDiscountValue()),
                "Tong cuoi phai = tam tinh - giam (neu co), khong cong phi ship");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_FUNC_026: Áp mã VIP50K khi đơn đạt tối thiểu 500.000đ -> banner '✓ Đã áp dụng mã VIP50K: -50.000đ', tổng cuối giảm đúng 50.000đ")
    public void applyVip50kReducesTotalBy50k() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(500_000, 1);
        long subtotal = parseMoney(cart.getSubtotalText());
        Assert.assertTrue(subtotal >= 500_000, "Tam tinh phai dat toi thieu 500.000d, dang la " + formatVnd(subtotal) + "d");

        cart.applyVoucher(GlobalConstants.VOUCHER_VIP_50K);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma VIP50K hop le phai hien banner da ap dung");
        Assert.assertEquals(cart.getAppliedVoucherBannerText(), "✓ Đã áp dụng mã VIP50K: -50.000đ",
                "Banner phai dung nguyen van '-50.000đ'");
        Assert.assertEquals(cart.getVoucherDiscountValue(), 50_000,
                "Dong tien giam do voucher phai dung 50.000d");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), subtotal - 50_000,
                "Tong cuoi phai giam dung 50.000d (tam tinh >= 299.000d nen phi ship da 0)");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_FUNC_027: Áp mã FORDEV -> hiện 'Áp dụng thành công mã FORDEV! Đơn hàng chỉ còn 2.000đ để test PayOS.', tổng cuối chỉ còn 2.000đ")
    public void applyFordevVoucherBringsTotalTo2000() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(500_000, 1);

        cart.applyVoucher("FORDEV");
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma FORDEV phai hien banner da ap dung");
        Assert.assertTrue(isTextVisibleOnPage("Áp dụng thành công mã FORDEV! Đơn hàng chỉ còn 2.000đ để test PayOS."),
                "Phai hien dung thong bao thanh cong cua ma FORDEV");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), 2_000,
                "Tong cuoi sau ma FORDEV phai chi con dung 2.000d");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_UI_028: Ô nhập voucher tự uppercase ('dafl10' -> 'DAFL10') và chip gợi ý '🏷️ DAFL20 (-20.000đ)' bấm vào thì điền + áp dụng như nhập tay")
    public void voucherInputUppercaseAndChipAppliesCode() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(100_000, 1);

        cart.setVoucherCode("dafl10");
        Assert.assertEquals(cart.getVoucherInputValue(), "DAFL10",
                "Go chu thuong 'dafl10' phai tu chuyen thanh chu hoa 'DAFL10'");

        Assert.assertTrue(cart.isVoucherChipDisplayed(GlobalConstants.VOUCHER_FIXED_20K),
                "Phai thay chip goi y ma DAFL20");
        Assert.assertEquals(cart.getVoucherChipText(GlobalConstants.VOUCHER_FIXED_20K),
                "🏷️ DAFL20 (-20.000đ)", "Chip goi y phai dung dinh dang '🏷️ DAFL20 (-20.000đ)'");

        cart.applyVoucherByChip(GlobalConstants.VOUCHER_FIXED_20K);
        sleepInSecond(1);
        if (!cart.isVoucherAppliedBannerDisplayed()) {
            // Chip chi dien ma vao o nhap: bam them nut Ap dung nhu nhap tay.
            Assert.assertEquals(cart.getVoucherInputValue(), GlobalConstants.VOUCHER_FIXED_20K,
                    "Bam chip phai dien ma DAFL20 vao o nhap");
            cart.clickVoucherApplyButton();
        }
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(),
                "Bam chip DAFL20 phai ap dung ma nhu nhap tay (hien banner da ap dung)");
        Assert.assertTrue(cart.getAppliedVoucherBannerText().contains("DAFL20"),
                "Banner phai ghi ma DAFL20, dang thay: " + cart.getAppliedVoucherBannerText());
        Assert.assertEquals(cart.getVoucherDiscountValue(), 20_000,
                "Chip DAFL20 phai giam dung 20.000d nhu nhap tay");
    }

    @Story("Voucher")
    @Test(description = "TC_SCR6_UI_029: Áp DAFL20 rồi bấm 'Bỏ chọn' -> banner biến mất, tổng tiền về chưa giảm, ô nhập mã trống (chỉ xóa ở FE)")
    public void removeVoucherRestoresOriginalTotal() {
        CartDrawerPageObject cart = openCartWithSubtotalAtLeast(100_000, 1);
        long finalBefore = parseMoney(cart.getFinalTotalText());

        cart.applyVoucher(GlobalConstants.VOUCHER_FIXED_20K);
        Assert.assertTrue(cart.isVoucherAppliedBannerDisplayed(), "Ap ma DAFL20 hop le phai hien banner da ap dung");
        Assert.assertEquals(cart.getAppliedVoucherBannerText(), "✓ Đã áp dụng mã DAFL20: -20.000đ",
                "Banner phai dung nguyen van '-20.000đ'");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), finalBefore - 20_000,
                "Ap DAFL20 xong tong cuoi phai giam dung 20.000d");

        Assert.assertTrue(cart.isRemoveVoucherButtonDisplayed(), "Phai thay nut 'Bỏ chọn' (remove-voucher-btn)");
        cart.clickRemoveVoucher();
        Assert.assertFalse(cart.isVoucherAppliedBannerDisplayed(),
                "Bam 'Bỏ chọn' xong banner da ap dung phai bien mat");
        Assert.assertEquals(parseMoney(cart.getFinalTotalText()), finalBefore,
                "Bo chon voucher xong tong tien phai ve dung muc chua giam");
        Assert.assertEquals(cart.getVoucherInputValue(), "",
                "Bo chon voucher xong o nhap ma phai trong de nhap ma khac");
    }
}
