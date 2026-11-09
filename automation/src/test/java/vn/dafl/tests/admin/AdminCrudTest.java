package vn.dafl.tests.admin;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;

/**
 * Test CRUD that tren trang quan tri /admin o app rieng localhost:3001 (dot 9).
 * Khac voi cac test admin dot 3-8 (chi doc), lop nay them/sua/xoa THAT qua giao dien
 * nhung CHI tren du lieu do chinh test tao ra va tu don dep sau khi chay:
 * - San pham / danh muc: ten kem timestamp, xoa trong finally (neu van con).
 * - Voucher (dot 13): sua/xoa CHI voucher Draft do test tao (sua mo ta/gia tri,
 *   xoa qua hop xac nhan chung); voucher kich hoat roi tat (Active -> Disabled)
 *   de lai an toan vi da vo hieu, khong ap dung duoc khi dat hang.
 * - Don hang (dot 10): ACR-06/07 KHONG dat don moi qua web ban hang nua ma doi
 *   trang thai / huy 1 don 'Chờ xử lý' CO SAN trong DB (theo yeu cau bo web ban hang
 *   khoi test admin). Don do la du lieu that, khong phai do test tao, va trang thai cu
 *   khong khoi phuc lai duoc sau test; neu DB khong co don cho xu ly thi test tu
 *   bo qua (skip, khong tinh fail).
 * - Diem hoi vien (dot 13): doc so du diem cua demo TRUOC/SAU qua modal chi tiet
 *   khach (admin-customer-detail-points), khang dinh tang dung +5, hoan tac -5
 *   trong finally (tong ket bang 0).
 * Dot 12: form them/sua San pham / Danh muc / Khuyen mai mo trong modal
 * (admin-*-form-modal) nen test tao SP phai mo modal truoc roi moi doc duoc danh
 * muc trong select cua form; nut them/sua/xoa van dung testid/aria-label cu.
 * TUYET DOI KHONG xoa/sua du lieu seed co san, KHONG doi mat khau admin, KHONG
 * duyet/tu choi yeu cau tra hang cua nguoi khac.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
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

    /** Lay ma don 'Chờ xử lý' dau tien trong bang Don hang (loc server-side theo
     *  trang thai o admin; nhan hien thi cua Pending o admin la 'Chờ xử lý', khac
     *  nhan 'Chờ xác nhận' o web ban hang); null neu khong co don nao o trang thai nay. */
    private String getFirstPendingOrderCodeOrNull(AdminPageObject adminPage) {
        adminPage.clickAdminNav("orders");
        adminPage.selectOrderStatusFilter(GlobalConstants.ADMIN_ORDER_STATUS_PENDING_LABEL);
        adminPage.clickOrderFilterSearch();
        if (adminPage.getAdminTableRowCount() == 0) {
            return null;
        }
        return extractOrderDisplayCode(adminPage.getAdminTableRowTexts().get(0));
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
            // Form them/sua gio nam trong modal (dot 12): mo modal truoc roi moi doc
            // duoc danh muc trong select cua form, sau do dien + luu luon trong modal.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormAndSave(productName, categoryName, "99000", "10");
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
            // Form them/sua gio nam trong modal (dot 12): mo modal truoc roi moi doc
            // duoc danh muc trong select cua form, sau do dien + luu luon trong modal.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormAndSave(productName, categoryName, "99000", "10");
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
            // Form them/sua gio nam trong modal (dot 12): mo modal truoc roi moi doc
            // duoc danh muc trong select cua form, sau do dien + luu luon trong modal.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormAndSave(productName, categoryName, "99000", "10");
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

    @Test(description = "ACR-05 (dot 13): Tao voucher Draft -> sua mo ta/gia tri -> xoa qua hop xac nhan (khong con); voucher thu 2 tao -> kich hoat -> tat co ly do (luong cu)")
    public void adminPromoCreateActivateDisable() {
        String promoCode = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        // (1) Tao voucher Draft moi.
        adminPage.createPromo(promoCode, "10");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tao voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        String row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Draft"),
                "Voucher vua tao phai o trang thai Nháp (Draft), dang thay: " + row);
        Assert.assertTrue(adminPage.isPromoEditButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut sua (admin-promo-edit-btn)");
        Assert.assertTrue(adminPage.isPromoDeleteButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut xoa (admin-promo-delete-btn)");

        // (2) Sua voucher Draft: doi mo ta + gia tri giam 10% -> 15%.
        String editedDescription = "Voucher test ACR-05 da sua";
        adminPage.editPromo(promoCode, editedDescription, "15");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Sua voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains(editedDescription),
                "Sau khi sua, dong voucher phai hien mo ta moi, dang thay: " + row);
        Assert.assertTrue(row.contains("15%"),
                "Sau khi sua, dong voucher phai hien gia tri moi 15%, dang thay: " + row);
        Assert.assertTrue(row.contains("Draft"),
                "Sau khi sua voucher van o trang thai Nháp (Draft), dang thay: " + row);

        // (3) Xoa voucher Draft qua hop xac nhan chung -> khong con trong bang.
        adminPage.deletePromoByCode(promoCode);
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Xoa voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(promoCode);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim voucher vua xoa phai ra 0 dong");
        Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                "Tim voucher vua xoa phai hien dong 'Chưa có khuyến mãi.'");

        // (4) Luong cu: voucher thu 2 tao -> kich hoat -> tat co ly do (de lai o
        // trang thai Disabled la vo hieu, khong ap dung duoc khi dat hang).
        String secondCode = uniquePromoCode();
        adminPage.createPromo(secondCode, "10");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tao voucher thu 2 xong phai hien thong bao thanh cong");
        adminPage.searchPromos(secondCode);
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Draft"),
                "Voucher thu 2 vua tao phai o trang thai Nháp (Draft), dang thay: " + row);

        adminPage.activatePromo(secondCode);
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Kich hoat voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(secondCode);
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Active"),
                "Sau khi kich hoat voucher phai o trang thai Active, dang thay: " + row);
        Assert.assertFalse(adminPage.isPromoEditButtonDisplayedFor(secondCode),
                "Voucher Active khong duoc con nut sua (chi Draft moi sua duoc)");
        Assert.assertFalse(adminPage.isPromoDeleteButtonDisplayedFor(secondCode),
                "Voucher Active khong duoc con nut xoa (chi Draft moi xoa duoc)");

        adminPage.disablePromo(secondCode, "Tắt bởi test ACR-05 (voucher do test tạo)");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Tat voucher xong phai hien thong bao thanh cong");
        adminPage.searchPromos(secondCode);
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Disabled"),
                "Sau khi tat voucher phai o trang thai Disabled, dang thay: " + row);
    }

    // ---------- Don hang (dot 10: doi/huy 1 don 'Chờ xử lý' CO SAN trong DB) ----------

    @Test(description = "ACR-06: Doi trang thai 1 don 'Chờ xử lý' có sẵn sang 'Đã xác nhận' tren Admin (skip neu khong co don cho xu ly)")
    public void adminChangePendingOrderStatusToConfirmed() {
        // LUU Y (dot 10): test nay KHONG dat don
        // moi qua web ban hang nua ma doi trang thai 1 don 'Chờ xử lý' CO SAN trong DB.
        // Don do la du lieu that (khong phai do test tao) va trang thai cu khong khoi
        // phuc lai duoc sau test. Neu DB khong co don cho xu ly thi test tu bo qua.
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de doi trang thai");
        }

        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.changeOrderStatusTo("Đã xác nhận");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Doi trang thai don xong phai hien thong bao thanh cong");

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã xác nhận"),
                "Don " + orderCode + " sau khi doi trang thai phai hien 'Đã xác nhận' trong bang don");
    }

    @Test(description = "ACR-07: Huy 1 don 'Chờ xử lý' có sẵn tren Admin, bat buoc co ly do (skip neu khong co don cho xu ly)")
    public void adminCancelPendingOrder() {
        // LUU Y (dot 10, giong ACR-06): huy 1 don 'Chờ xử lý' CO SAN trong DB, khong
        // phai don do test tao; trang thai cu khong khoi phuc lai duoc sau test.
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String orderCode = getFirstPendingOrderCodeOrNull(adminPage);
        if (orderCode == null) {
            throw new SkipException("Bo qua: DB nay khong co don hang nao o trang thai 'Chờ xử lý' de huy");
        }

        adminPage.searchOrderByCodeAndOpen(orderCode);
        adminPage.cancelOrderWithReason("Hủy bởi test ACR-07 (đơn Chờ xử lý có sẵn)");
        Assert.assertTrue(adminPage.isNoticeDisplayed(), "Huy don xong phai hien thong bao thanh cong");

        adminPage.closeOrderDetail();
        Assert.assertTrue(adminPage.isOrderRowWithStatusDisplayed(orderCode, "Đã hủy"),
                "Don " + orderCode + " sau khi huy phai hien 'Đã hủy' trong bang don");
    }

    // ---------- Hoi vien: dieu chinh diem ----------

    @Test(description = "ACR-08 (dot 13): Doc so du diem demo TRUOC/SAU qua modal chi tiet khach -> cong 5 diem phai tang dung +5 (hoan tac -5 sau test)")
    public void adminAdjustPointsForDemoCustomer() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        int pointsBefore = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
        if (pointsBefore < 0) {
            throw new SkipException("Bo qua: khong tim thay khach " + GlobalConstants.DEMO_EMAIL
                    + " trong bang Khach hang de doc so du diem");
        }
        try {
            adminPage.adjustPointsForUser(GlobalConstants.DEMO_EMAIL, "5", "Cộng điểm test ACR-08");
            pointsAdjusted = true;
            Assert.assertTrue(adminPage.isNoticeDisplayed(), "Dieu chinh diem xong phai hien thong bao thanh cong");
            Assert.assertTrue(adminPage.isAdjustPointsFormCleared(),
                    "Dieu chinh diem xong o so diem + ly do phai duoc xoa trang");

            int pointsAfter = adminPage.getCustomerPointsByEmail(GlobalConstants.DEMO_EMAIL);
            Assert.assertEquals(pointsAfter, pointsBefore + 5,
                    "So du diem cua demo phai tang dung 5 sau khi cong (truoc: " + pointsBefore
                            + ", sau: " + pointsAfter + ")");
        } finally {
            reversePointsAdjustmentIfNeeded();
        }
    }
}
