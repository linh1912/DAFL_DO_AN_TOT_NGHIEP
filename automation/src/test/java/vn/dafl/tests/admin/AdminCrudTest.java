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
 * Dot redesign form admin + upload anh: form san pham la DRAWER (van testid
 * admin-product-form-modal), dropdown danh muc la custom (chon theo text, khong
 * con <select>), o gia tu dinh dang dau cham nghin, thong bao sau luu la toast
 * admin-toast, hang moi highlight (sa-row-highlight); xoa SP/danh muc qua hop
 * xac nhan chung; form danh muc co slug tu sinh + toggle; form khuyen mai co nut
 * tao ma ngau nhien + segmented loai giam + ngay het han (input date).
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

    @Test(description = "ACR-01 (dot redesign form admin): Tao san pham moi o Admin (gia goc + mo ta) -> toast + hien trong bang + tim theo ten thay ngay + dong moi highlight")
    public void adminCreateProductShowsInTableAndSearchable() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            // Form them/sua gio la drawer (admin-product-form-modal, van testid cu):
            // mo drawer truoc roi moi doc duoc danh muc trong dropdown custom.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                    "Mo ta san pham test ACR-01");
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, productName);
            Assert.assertTrue(row.contains("99.000đ"),
                    "Dong san pham vua tao phai hien dung gia 99.000đ, dang thay: " + row);
            Assert.assertTrue(row.contains("120.000đ"),
                    "Dong san pham vua tao phai hien gia goc 120.000đ (gach ngang), dang thay: " + row);
            Assert.assertTrue(adminPage.isRowHighlighted(productName),
                    "Dong san pham vua tao phai duoc highlight (class sa-row-highlight)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Test(description = "ACR-02 (dot redesign form admin): Sua san pham vua tao (doi ten + gia + gia goc) -> bang cap nhat, tim ten cu khong thay; badge % giam + o gia chuan hoa bo dau cham")
    public void adminEditProductChangesNameAndPrice() {
        String productName = uniqueProductName();
        String editedName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            // Form them/sua gio la drawer (dot redesign): mo drawer truoc roi moi doc
            // duoc danh muc trong dropdown custom cua form.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                    "Mo ta san pham test ACR-02");
            createdProductName = productName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham goc phai ton tai truoc khi sua");

            // Doi ten + gia 150.000đ + gia goc 200.000đ -> giam dung 25%.
            adminPage.editProductFull(productName, editedName, "150000", "200000");
            createdProductName = editedName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Sua san pham xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(editedName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(editedName),
                    "Tim theo ten moi sau khi sua phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, editedName);
            Assert.assertTrue(row.contains("150.000đ"),
                    "Sau khi sua, dong san pham phai hien gia moi 150.000đ, dang thay: " + row);

            // Mo lai form sua: badge % giam phai hien 25%; o gia tu dinh dang dau
            // cham nghin ("150.000") nen assert sau khi chuan hoa bo dau ".".
            adminPage.openProductEditForm(editedName);
            Assert.assertTrue(adminPage.isDiscountBadgeDisplayed(),
                    "Mo form sua san pham co gia goc > gia ban phai thay badge % giam");
            Assert.assertTrue(adminPage.getDiscountBadgeText().contains("25"),
                    "Badge % giam phai hien 25% (gia 150.000đ / gia goc 200.000đ), dang thay: "
                            + adminPage.getDiscountBadgeText());
            Assert.assertEquals(adminPage.getProductPriceInputDigits(), "150000",
                    "O gia ban hien '150.000' phai chuan hoa ve 150000 khi bo dau cham");
            adminPage.cancelProductForm();

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
            Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa san pham xong phai hien toast thanh cong (admin-toast)");

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

    @Test(description = "ACR-04 (dot redesign form admin): Tao danh muc moi (slug tu sinh + mo ta + thu tu) -> sua ten (toggle trang thai hien) -> xoa qua hop xac nhan; moi buoc deu kiem tra qua tim kiem")
    public void adminCategoryCreateEditDelete() {
        String categoryName = uniqueCategoryName();
        String editedName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");

            adminPage.createCategoryFull(categoryName, "", "Mo ta danh muc test ACR-04", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Tim danh muc vua tao phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, categoryName);
            Assert.assertTrue(row.contains("Mo ta danh muc test ACR-04"),
                    "Dong danh muc vua tao phai hien dung mo ta, dang thay: " + row);
            // Slug tu sinh tu ten (chu thuong, khong dau, noi bang '-'): ten
            // "TESTDM-<timestamp>" -> slug "testdm-<timestamp>".
            String expectedSlug = categoryName.toLowerCase();
            Assert.assertTrue(row.contains(expectedSlug),
                    "Dong danh muc vua tao phai hien slug tu sinh '" + expectedSlug + "', dang thay: " + row);

            // Mo form sua: toggle trang thai phai hien (khong doi gia tri toggle).
            adminPage.openCategoryEditForm(categoryName);
            Assert.assertTrue(adminPage.isCategoryStatusToggleDisplayed(),
                    "Form sua danh muc phai co toggle trang thai (admin-category-status-toggle)");
            adminPage.cancelCategoryForm();

            adminPage.editCategory(categoryName, editedName);
            createdCategoryName = editedName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Sua danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(editedName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(editedName),
                    "Tim ten danh muc moi sau khi sua phai thay 1 dong trong bang");
            adminPage.searchCategories(categoryName);
            Assert.assertFalse(adminPage.isRowWithTextDisplayed(categoryName),
                    "Tim ten danh muc cu sau khi sua phai khong thay dong nao");

            adminPage.deleteCategoryByName(editedName);
            createdCategoryName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa danh muc xong phai hien toast thanh cong (admin-toast)");
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

    @Test(description = "ACR-05 (dot redesign form admin): Tao voucher Draft qua form moi (loai %, don toi thieu, han) -> sua mo ta/gia tri/don toi thieu/tran giam/han/luot dung -> xoa qua hop xac nhan; voucher thu 2 dung nut tao ma ngau nhien + loai giam tien co dinh -> kich hoat -> tat co ly do")
    public void adminPromoCreateActivateDisable() {
        String promoCode = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");

        // (1) Tao voucher Draft moi qua form moi (giam 10%, don toi thieu 0, han 31/12/2027).
        adminPage.createPromoFull(promoCode, "Voucher test ACR-05", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(promoCode);
        String row = getRowContaining(adminPage, promoCode);
        Assert.assertTrue(row.contains("Draft"),
                "Voucher vua tao phai o trang thai Nháp (Draft), dang thay: " + row);
        Assert.assertTrue(row.contains("10%"),
                "Voucher vua tao phai hien gia tri giam 10%, dang thay: " + row);
        Assert.assertTrue(adminPage.isPromoEditButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut sua (admin-promo-edit-btn)");
        Assert.assertTrue(adminPage.isPromoDeleteButtonDisplayedFor(promoCode),
                "Voucher Draft phai co nut xoa (admin-promo-delete-btn)");

        // (2) Sua voucher Draft: doi mo ta + gia tri giam 10% -> 15% + don toi thieu
        // 100.000đ + tran giam 50.000đ + han 30/06/2028 + tong luot 100.
        String editedDescription = "Voucher test ACR-05 da sua";
        adminPage.editPromoFull(promoCode, editedDescription, "15", "100000", "50000", "2028-06-30", "100");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Sua voucher xong phai hien toast thanh cong (admin-toast)");
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
        Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa voucher xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(promoCode);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim voucher vua xoa phai ra 0 dong");
        Assert.assertTrue(adminPage.isEmptyPromosMessageDisplayed(),
                "Tim voucher vua xoa phai hien dong 'Chưa có khuyến mãi.'");

        // (4) Voucher thu 2: dung nut tao ma ngau nhien (admin-promo-generate-code-btn)
        // + loai giam tien co dinh 50.000đ, don toi thieu 100.000đ -> kich hoat -> tat co ly do.
        String secondCode = adminPage.createPromoFull(null, "Voucher test ACR-05 thu 2",
                "Fixed", "50000", "100000", "", "", "");
        Assert.assertFalse(secondCode == null || secondCode.isEmpty(),
                "Nut tao ma ngau nhien phai sinh ra ma voucher khong rong");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher thu 2 xong phai hien toast thanh cong (admin-toast)");
        adminPage.searchPromos(secondCode);
        row = getRowContaining(adminPage, secondCode);
        Assert.assertTrue(row.contains("Draft"),
                "Voucher thu 2 vua tao phai o trang thai Nháp (Draft), dang thay: " + row);
        Assert.assertTrue(row.contains("50.000đ"),
                "Voucher giam tien co dinh phai hien gia tri 50.000đ, dang thay: " + row);

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
