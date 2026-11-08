package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

/**
 * Test CRUD that tren trang quan tri /admin o app rieng localhost:3001 (dot 9).
 * Khac voi cac test admin dot 3-8 (chi doc), lop nay them/sua/xoa THAT qua giao dien
 * nhung CHI tren du lieu do chinh test tao ra va tu don dep sau khi chay:
 * - San pham / danh muc: ten kem timestamp, xoa trong finally (neu van con).
 * - Voucher: giao dien KHONG co nut sua/xoa nen chi tao -> kich hoat -> tat co ly do;
 *   voucher o trang thai Da tat la vo hieu nen de lai an toan, khong don dep duoc.
 * - Don hang: chi doi trang thai / huy don do chinh test tao bang cach dat COD
 *   truoc do voi tai khoan demo; KHONG dung don cua nguoi khac.
 * - Diem hoi vien: cong 5 diem cho tai khoan demo roi hoan tac -5 trong finally
 *   (tong ket bang 0); khong co cho nao tren giao dien hien so du diem de doc lai
 *   nen chi khang dinh thong bao thanh cong + form duoc xoa trang.
 * TUYET DOI KHONG xoa/sua du lieu seed co san, KHONG doi mat khau admin, KHONG
 * duyet/tu choi yeu cau tra hang cua nguoi khac.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 */
public class AdminCrudTest extends BaseTest {

    // Trang thai de don dep (chi du lieu do test tao); moi test method co browser moi
    // nen cac truong nay chi song trong pham vi 1 test.
    private String createdProductName;
    private String createdCategoryName;
    private boolean pointsAdjusted = false;
    private boolean adminLoggedIn = false;

    // ---------- Helper dung chung ----------

    /** Ten san pham / danh muc duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueProductName() {
        return "TESTSP-" + System.currentTimeMillis();
    }

    private String uniqueCategoryName() {
        return "TESTDM-" + System.currentTimeMillis();
    }

    /** Ma voucher duy nhat theo tung lan chay (chu + so, hop le 3-50 ky tu). */
    private String uniquePromoCode() {
        return "TEST" + System.currentTimeMillis();
    }

    /** Dang nhap Super Admin tren app rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        adminLoggedIn = true;
        return adminPage;
    }

    /** Dat 1 don COD bang tai khoan demo tren shop :3000, tra ve ma don hien thi
     *  (vd DH261108-0001). Don nay do chinh test tao nen admin duoc phep doi/huy. */
    private String placeCodOrderAsDemo() {
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
        return orderCode;
    }

    /** Lay text cua dong dau tien trong bang dang mo co chua chuoi cho truoc ("" neu khong co). */
    private String getRowContaining(AdminPageObject adminPage, String text) {
        return adminPage.getAdminTableRowTexts().stream()
                .filter(row -> row.contains(text))
                .findFirst().orElse("");
    }

    // ---------- Don dep best-effort (khong fail test khi don dep loi) ----------

    /** Xoa san pham test neu van con (chi goi voi ten do test tao). */
    private void deleteProductIfPresent(String productName) {
        if (productName == null || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("products");
            adminPage.searchProducts(productName);
            if (adminPage.isRowWithTextDisplayed(productName)) {
                adminPage.deleteProductByName(productName);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Xoa danh muc test neu van con (chi goi voi ten do test tao). */
    private void deleteCategoryIfPresent(String categoryName) {
        if (categoryName == null || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("categories");
            adminPage.searchCategories(categoryName);
            if (adminPage.isRowWithTextDisplayed(categoryName)) {
                adminPage.deleteCategoryByName(categoryName);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Hoan tac +5 diem da cong cho demo (tru lai 5 diem, tong ket bang 0). */
    private void reversePointsAdjustmentIfNeeded() {
        if (!pointsAdjusted || !adminLoggedIn) {
            return;
        }
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "-5", "Hoàn tác điểm test ACR-08");
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        } finally {
            pointsAdjusted = false;
        }
    }

    // ---------- San pham ----------

    @Test(description = "ACR-01: Tao san pham moi o Admin -> hien trong bang + tim theo ten thay ngay")
    public void adminCreateProductShowsInTableAndSearchable() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.createProduct(productName, categoryName, "99000", "10");
            createdProductName = productName;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tao san pham xong phai hien thong bao thanh cong");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, productName);
            Assert.assertTrue(row.contains("99.000đ"),
                    "Dong san pham vua tao phai hien dung gia 99.000đ, dang thay: " + row);
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Test(description = "ACR-02: Sua san pham vua tao (doi ten + gia) -> bang cap nhat, tim ten cu khong thay")
    public void adminEditProductChangesNameAndPrice() {
        String productName = uniqueProductName();
        String editedName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.createProduct(productName, categoryName, "99000", "10");
            createdProductName = productName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham goc phai ton tai truoc khi sua");

            adminPage.editProduct(productName, editedName, "149000");
            createdProductName = editedName;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Sua san pham xong phai hien thong bao thanh cong");

            adminPage.searchProducts(editedName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(editedName),
                    "Tim theo ten moi sau khi sua phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, editedName);
            Assert.assertTrue(row.contains("149.000đ"),
                    "Sau khi sua, dong san pham phai hien gia moi 149.000đ, dang thay: " + row);

            adminPage.searchProducts(productName);
            Assert.assertFalse(adminPage.isRowWithTextDisplayed(productName),
                    "Tim ten cu sau khi sua phai khong thay dong nao");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Test(description = "ACR-03: Xoa san pham vua tao -> bien mat khoi bang, tim lai khong thay")
    public void adminDeleteProductRemovesFromTable() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.createProduct(productName, categoryName, "99000", "10");
            createdProductName = productName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham phai ton tai truoc khi xoa");

            adminPage.deleteProductByName(productName);
            createdProductName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Xoa san pham xong phai hien thong bao thanh cong");

            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim san pham vua xoa phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                    "Tim san pham vua xoa phai hien dong 'Không có sản phẩm nào.'");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Danh muc ----------

    @Test(description = "ACR-04: Tao danh muc moi -> sua ten -> xoa; moi buoc deu kiem tra qua tim kiem")
    public void adminCategoryCreateEditDelete() {
        String categoryName = uniqueCategoryName();
        String editedName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");

            adminPage.createCategory(categoryName);
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tao danh muc xong phai hien thong bao thanh cong");
            adminPage.searchCategories(categoryName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Tim danh muc vua tao phai thay 1 dong trong bang");

            adminPage.editCategory(categoryName, editedName);
            createdCategoryName = editedName;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Sua danh muc xong phai hien thong bao thanh cong");
            adminPage.searchCategories(editedName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(editedName),
                    "Tim ten danh muc moi sau khi sua phai thay 1 dong trong bang");
            adminPage.searchCategories(categoryName);
            Assert.assertFalse(adminPage.isRowWithTextDisplayed(categoryName),
                    "Tim ten danh muc cu sau khi sua phai khong thay dong nao");

            adminPage.deleteCategoryByName(editedName);
            createdCategoryName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Xoa danh muc xong phai hien thong bao thanh cong");
            adminPage.searchCategories(editedName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim danh muc vua xoa phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyCategoriesMessageDisplayed(),
                    "Tim danh muc vua xoa phai hien dong 'Chưa có danh mục.'");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    // ---------- Khuyen mai / voucher ----------

    @Test(description = "ACR-05: Tao voucher moi (Nhap) -> kich hoat (Dang chay) -> tat co ly do (Da tat); giao dien KHONG co sua/xoa voucher")
    public void adminPromoCreateActivateDisable() {
        String promoCode = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        adminPage.createPromo(promoCode, "10");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tao voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        String row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Draft"),
                "Voucher vua tao phai o trang thai Nháp (Draft), dang thay: " + row);

        adminPage.activatePromo(promoCode);
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Kich hoat voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Active"),
                "Sau khi kich hoat voucher phai o trang thai Active, dang thay: " + row);

        adminPage.disablePromo(promoCode, "Tắt bởi test ACR-05 (voucher do test tạo)");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tat voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Disabled"),
                "Sau khi tat voucher phai o trang thai Disabled, dang thay: " + row);

        // Khong don dep: giao dien admin khong co nut xoa voucher; voucher o trang
        // thai Disabled la vo hieu (khong ap dung duoc khi dat hang) nen de lai an toan.
    }

    // ---------- Don hang (chi don do chinh test tao) ----------

    @Test(description = "ACR-06: Doi trang thai don do chinh test tao (COD Pending -> Confirmed) tren Admin")
    public void adminChangeOwnTestOrderStatusToConfirmed() {
        String orderCode = placeCodOrderAsDemo();

        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.changeOrderStatusTo("Đã xác nhận");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Doi trang thai don xong phai hien thong bao thanh cong");

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã xác nhận"),
                "Don " + orderCode + " sau khi doi trang thai phai hien 'Đã xác nhận' trong bang don");
    }

    @Test(description = "ACR-07: Huy don do chinh test tao (COD Pending) tren Admin, bat buoc co ly do")
    public void adminCancelOwnTestOrder() {
        String orderCode = placeCodOrderAsDemo();

        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.cancelOrderWithReason("Hủy bởi test ACR-07 (đơn do test tạo)");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Huy don xong phai hien thong bao thanh cong");

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã hủy"),
                "Don " + orderCode + " sau khi huy phai hien 'Đã hủy' trong bang don");
    }

    // ---------- Hoi vien: dieu chinh diem ----------

    @Test(description = "ACR-08: Dieu chinh diem cho khach demo (+5) tren Admin -> bao thanh cong, form duoc xoa trang (hoan tac -5 sau test)")
    public void adminAdjustPointsForDemoCustomer() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "5", "Cộng điểm test ACR-08");
            pointsAdjusted = true;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Dieu chinh diem xong phai hien thong bao thanh cong");
            Assert.assertTrue(adminPage.isAdjustPointsFormCleared(),
                    "Dieu chinh diem xong o so diem + ly do phai duoc xoa trang");
        } finally {
            reversePointsAdjustmentIfNeeded();
        }
    }
}
