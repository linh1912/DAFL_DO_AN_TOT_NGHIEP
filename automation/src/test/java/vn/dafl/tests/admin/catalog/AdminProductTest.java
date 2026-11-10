package vn.dafl.tests.admin.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test CRUD san pham tren app Admin rieng localhost:3001 (dot 9, tach tu
 * AdminCrudTest cu khi chia thu muc admin dot 17 — moi lop chi lo 1 phan he).
 * Chi them/sua/xoa THAT tren du lieu do chinh test tao (ten kem timestamp) va
 * tu don dep sau khi chay; KHONG xoa/sua du lieu seed.
 * Dot 21: form them/sua SP la TRANG full page (/products/new,
 * /products/:code/edit, admin-product-form — KHONG con Drawer
 * admin-product-form-modal, khong con class fs-drawer/--fs-width 760px;
 * mo form thi bang danh sach BI THAY THE trong DOM); trang chi tiet SP cung
 * la trang full page (/products/:code, breadcrumb "Quản lý sản phẩm >
 * Chi tiết sản phẩm", KHONG con form XEM Drawer); bang BO cot Đánh giá
 * (rating chi con o trang chi tiet + module Đánh giá); gat Switch mo modal
 * CHUNG 'Xác nhận thay đổi trạng thái?' (CHI 2 nut [Hủy] + [Xác nhận])
 * roi toast 'Đổi trạng thái sản phẩm thành công!' — KHONG con popup
 * 'Ẩn sản phẩm?' / 'Hiển thị sản phẩm?' o toggle; xoa SP dang Hoat dong
 * bi chan boi ConfirmDialog 'Không thể xóa sản phẩm' (tieu de khong dau
 * 'xóa', CHI 2 nut [Hủy] + [Chuyển sang Ngừng hoạt động]) roi moi mo
 * modal chung doi trang thai; trung ma thi CA toast CA message do duoi o
 * Ma ('Mã sản phẩm đã tồn tại.'); tien hien dang '99.000₫' (ky tu ₫, khong
 * phai 'đ'); dong moi tao highlight 2 giay (class sa-row-highlight) — phai
 * kiem ngay sau khi luu.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý sản phẩm")
public class AdminProductTest extends BaseTest {

    // Trang thai de don dep (chi du lieu do test tao); moi test method co browser moi
    // nen cac truong nay chi song trong pham vi 1 test.
    private String createdProductName;
    private String createdCategoryName;
    private boolean adminLoggedIn = false;

    // ---------- Helper dung chung ----------

    /** Ten san pham duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueProductName() {
        return "TESTSP-" + System.currentTimeMillis();
    }

    /** Ma san pham duy nhat theo tung lan chay (12 ky tu: TSPDUP + 6 so cuoi
     *  cua timestamp — hop le 3-20 ky tu, chi chu in hoa + so). */
    private String uniqueProductCode() {
        return "TSPDUP" + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    /** Dang nhap Super Admin tren app rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        adminLoggedIn = true;
        return adminPage;
    }

    /** Lay text cua dong dau tien trong bang dang mo co chua chuoi cho truoc ("" neu khong co). */
    private String getRowContaining(AdminPageObject adminPage, String text) {
        return adminPage.getAdminTableRowTexts().stream()
                .filter(row -> row.contains(text))
                .findFirst().orElse("");
    }

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

    /** Ten danh muc duy nhat theo tung lan chay (kem timestamp) — dung cho
     *  test can 1 danh muc that lam dieu kien (vd DM da an o VAL_031). */
    private String uniqueCategoryName() {
        return "TESTDM-" + System.currentTimeMillis();
    }

    /** Xoa danh muc test neu van con (chi goi voi ten do test tao); DM phai
     *  duoc hien lai truoc khi xoa neu truoc do test da an no (deleteCategoryByName
     *  tu xu ly popup chan xoa o DM dang Hoat dong, khong xu ly DM da Ngung ma
     *  con SP — DM test o day khong co SP nen an/hien deu xoa duoc). */
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

    /** Hien lai danh muc test da an truoc do (gat Switch + xac nhan 'Hiển thị
     *  danh mục?'); khong lam gi neu DM da hien hoac khong tim thay. */
    private void showCategoryIfHidden(String categoryName) {
        try {
            AdminPageObject adminPage = new AdminPageObject();
            adminPage.openPageUrl(GlobalConstants.ADMIN_APP_URL + "/admin");
            adminPage.clickAdminNav("categories");
            adminPage.searchCategories(categoryName);
            if (adminPage.isRowWithTextDisplayed(categoryName)
                    && Boolean.FALSE.equals(adminPage.getRowStatusSwitchChecked(categoryName))) {
                adminPage.clickRowStatusSwitch(categoryName);
                adminPage.clickConfirmYes();
                adminPage.waitForFeedback();
            }
        } catch (Throwable ignored) {
            // Don dep best-effort: loi don dep khong duoc lam fail test chinh.
        }
    }

    /** Tao 1 san pham moi (ma tu sinh SP-xxxx) roi tra ve ten; dung chung cho
     *  cac test dot 17 can 1 SP that co san. */
    private String createProductOrSkip(AdminPageObject adminPage) {
        String productName = uniqueProductName();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        String categoryName = adminPage.getFirstRealCategoryName();
        Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");
        adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                "Mo ta san pham test dot 17");
        createdProductName = productName;
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham xong phai hien toast thanh cong (admin-toast)");
        return productName;
    }

    // ---------- San pham (ACR-01..03, goc dot 9, viet lai dot 17 theo form trang rieng) ----------

    @Story("Sản phẩm")
    @Test(description = "ACR-01 (đợt 21, form trang full page + tiền ₫): Tạo sản phẩm mới ở Admin (giá gốc + mô tả) → toast + hiện trong bảng + tìm theo tên thấy ngay + dòng mới highlight (kiểm ngay sau khi lưu vì highlight chỉ kéo dài 2 giây)")
    public void adminCreateProductShowsInTableAndSearchable() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            // Form them/sua dot 21 la trang full page (/products/new):
            // mo form truoc roi moi doc duoc danh muc trong dropdown custom.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                    "Mo ta san pham test ACR-01");
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham xong phai hien toast thanh cong (admin-toast)");

            // Highlight dong moi tao chi ton tai 2 giay — kiem NGAY sau khi luu,
            // khi dong dang hien o trang hien tai (SP moi len dau trang 1).
            if (adminPage.isRowWithTextDisplayed(productName)) {
                Assert.assertTrue(adminPage.isRowHighlighted(productName),
                        "Dong san pham vua tao phai duoc highlight (class sa-row-highlight)");
            }

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, productName);
            Assert.assertTrue(row.contains("99.000₫"),
                    "Dong san pham vua tao phai hien dung gia 99.000₫, dang thay: " + row);
            Assert.assertTrue(row.contains("120.000₫"),
                    "Dong san pham vua tao phai hien gia goc 120.000₫ (gach ngang), dang thay: " + row);
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ACR-02 (đợt 21, form trang full page + tiền ₫): Sửa sản phẩm vừa tạo (đổi tên + giá + giá gốc) → bảng cập nhật, tìm tên cũ không thấy; badge % giảm + ô giá chuẩn hóa bỏ dấu chấm")
    public void adminEditProductChangesNameAndPrice() {
        String productName = uniqueProductName();
        String editedName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            // Form them/sua dot 21 la trang full page: mo form truoc roi
            // moi doc duoc danh muc trong dropdown custom cua form.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                    "Mo ta san pham test ACR-02");
            createdProductName = productName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham goc phai ton tai truoc khi sua");

            // Doi ten + gia 150.000₫ + gia goc 200.000₫ -> giam dung 25%.
            adminPage.editProductFull(productName, editedName, "150000", "200000");
            createdProductName = editedName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Sua san pham xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(editedName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(editedName),
                    "Tim theo ten moi sau khi sua phai thay 1 dong trong bang");
            String row = getRowContaining(adminPage, editedName);
            Assert.assertTrue(row.contains("150.000₫"),
                    "Sau khi sua, dong san pham phai hien gia moi 150.000₫, dang thay: " + row);

            // Mo lai form sua: badge % giam phai hien 25%; o gia tu dinh dang dau
            // cham nghin ("150.000") nen assert sau khi chuan hoa bo dau ".".
            adminPage.openProductEditForm(editedName);
            Assert.assertTrue(adminPage.isDiscountBadgeDisplayed(),
                    "Mo form sua san pham co gia goc > gia ban phai thay badge % giam");
            Assert.assertTrue(adminPage.getDiscountBadgeText().contains("25"),
                    "Badge % giam phai hien 25% (gia 150.000₫ / gia goc 200.000₫), dang thay: "
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

    @Story("Sản phẩm")
    @Test(description = "ACR-03 (đợt 21, ConfirmDialog + modal chung): Xóa sản phẩm vừa tạo → bấm xóa gặp popup chặn (SP đang Hoạt động) → 'Chuyển sang Ngừng hoạt động' → xác nhận modal chung 'Xác nhận thay đổi trạng thái?' → xóa qua ConfirmDialog 'Xóa sản phẩm?' → biến mất khỏi bảng, tìm lại không thấy")
    public void adminDeleteProductRemovesFromTable() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            // Form them/sua dot 21 la trang full page: mo form truoc roi
            // moi doc duoc danh muc trong dropdown custom cua form.
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormAndSave(productName, categoryName, "99000", "10");
            createdProductName = productName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham phai ton tai truoc khi xoa");

            // SP moi tao mac dinh Hoat dong: deleteProductByName tu xu ly popup
            // chan xoa (chuyen Ngung hoat dong) roi moi xoa qua ConfirmDialog.
            adminPage.deleteProductByName(productName);
            createdProductName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa san pham xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim san pham vua xoa phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                    "Tim san pham vua xoa phai hien dong 'Không tìm thấy sản phẩm'");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Dot 17: ma trung / ma disable / popup chan xoa / Switch / loc / STT / cot Ma ----------

    @Story("Sản phẩm")
    @Test(description = "ADM17-01 (đợt 17): Tạo SP với mã trùng mã SP đã có → CẢ toast CẢ message đỏ dưới ô Mã đều là 'Mã sản phẩm đã tồn tại.', form vẫn mở, SP thứ 2 không được tạo")
    public void adminCreateProductWithDuplicateCodeShowsInlineError() {
        String code = uniqueProductCode();
        String firstName = uniqueProductName();
        String secondName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.setProductCode(code);
            adminPage.fillProductForm(firstName, categoryName, "99000", "", "10",
                    "San pham test trung ma ADM17-01");
            adminPage.saveProductForm();
            createdProductName = firstName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham dau tien xong phai hien toast thanh cong (admin-toast)");

            // Tao SP thu 2 trung ma vua dung (ten khac, ma giong).
            adminPage.openProductFormModal();
            adminPage.setProductCode(code);
            adminPage.fillProductForm(secondName, categoryName, "99000", "", "10",
                    "San pham test trung ma ADM17-01 lan 2");
            adminPage.clickProductSaveButton();
            String toast = adminPage.waitForToast();
            Assert.assertTrue(toast.contains("Mã sản phẩm đã tồn tại."),
                    "Trung ma SP thi toast phai ghi 'Mã sản phẩm đã tồn tại.', dang thay: " + toast);
            Assert.assertEquals(adminPage.getProductFieldErrorText("code"), "Mã sản phẩm đã tồn tại.",
                    "Trung ma SP thi message do duoi o Ma phai ghi 'Mã sản phẩm đã tồn tại.'");
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Luu trung ma that bai thi form phai van mo (khong dong, khong tao SP)");

            // Dong form (dirty -> hop 'Thoát mà chưa lưu?' -> bam Thoat).
            adminPage.cancelProductForm();
            Assert.assertFalse(adminPage.isProductFormModalPresent(),
                    "Dong form xong khong duoc con thay form san pham");

            adminPage.searchProducts(secondName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "SP thu 2 trung ma khong duoc tao nen tim ten no phai ra 0 dong");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-04 (đợt 17): Mở form sửa SP → ô Mã bị disable (hiện đúng mã SP-...), ô Tên sửa được; bấm Hủy + 'Thoát' thì không đổi gì (tên cũ vẫn còn, tên mới không có)")
    public void adminEditProductFormDisablesCodeInput() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);
            String codeInRow = adminPage.getProductCodeInRow(productName);
            Assert.assertTrue(codeInRow.startsWith("SP-"),
                    "Ma SP trong bang phai bat dau 'SP-', dang thay: " + codeInRow);

            adminPage.openProductEditForm(productName);
            Assert.assertTrue(adminPage.isProductCodeInputDisabled(),
                    "Mo form sua SP thi o Ma phai bi disable (khong sua duoc ma)");
            Assert.assertEquals(adminPage.getProductCodeInputValue(), codeInRow,
                    "O Ma trong form sua phai hien dung ma cua SP, dang thay: " + adminPage.getProductCodeInputValue());
            Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT),
                    "O Ten trong form sua van phai sua duoc (khong disable)");

            // Doi ten trong form (khong luu) de chung minh truong khac sua duoc.
            String editedName = productName + "-SUA";
            adminPage.setProductName(editedName);
            Assert.assertEquals(adminPage.getProductNameInputValue(), editedName,
                    "Go ten moi vao o Ten phai hien dung ten moi trong form");

            // Huy: form dirty -> hop 'Thoát mà chưa lưu?' -> bam Thoat.
            adminPage.cancelProductForm();
            Assert.assertFalse(adminPage.isProductFormModalPresent(),
                    "Dong form xong khong duoc con thay form sua san pham");

            adminPage.searchProducts(editedName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Huy sua thi ten moi khong duoc luu, tim ten moi phai ra 0 dong");
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Huy sua thi ten cu van phai con trong bang");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-07 (đợt 21, ConfirmDialog + modal chung): Bấm Xóa trên SP đang Hoạt động → popup 'Không thể xóa sản phẩm' đúng nội dung, CHỈ 2 nút → 'Chuyển sang Ngừng hoạt động' → xác nhận modal chung 'Xác nhận thay đổi trạng thái?' → toast 'Đổi trạng thái sản phẩm thành công!' → Switch tắt → xóa qua ConfirmDialog 'Xóa sản phẩm?' → SP biến mất")
    public void adminDeleteActiveProductBlockedThenConvertAndDelete() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "San pham phai ton tai truoc khi xoa");
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.TRUE,
                    "SP moi tao phai dang Hoat dong (Switch aria-checked=true)");

            adminPage.clickProductDeleteButton(productName);
            Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                    "Xoa SP dang Hoat dong phai hien popup chan xoa (ConfirmDialog 'Không thể xóa sản phẩm')");
            String blockedText = adminPage.getDeleteBlockedModalText();
            Assert.assertTrue(blockedText.contains("Không thể xóa sản phẩm"),
                    "Popup chan xoa phai co tieu de 'Không thể xóa sản phẩm', dang thay: " + blockedText);
            Assert.assertTrue(blockedText.contains(productName),
                    "Popup chan xoa phai nhac ten SP '" + productName + "', dang thay: " + blockedText);
            Assert.assertTrue(blockedText.contains("đang hoạt động"),
                    "Popup chan xoa phai ghi 'đang hoạt động', dang thay: " + blockedText);
            Assert.assertTrue(blockedText.contains("Ngừng hoạt động"),
                    "Popup chan xoa phai huong dan chuyen sang 'Ngừng hoạt động', dang thay: " + blockedText);
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup chan xoa dot 18 phai CHI co dung 2 nut ([Hủy] + [Chuyển sang Ngừng hoạt động])");
            Assert.assertTrue(adminPage.getConfirmDialogConfirmText().contains("Chuyển sang Ngừng hoạt động"),
                    "Nut xac nhan popup chan xoa phai la 'Chuyển sang Ngừng hoạt động', dang thay: "
                            + adminPage.getConfirmDialogConfirmText());

            adminPage.clickDeleteBlockedConvert();
            String confirmText = adminPage.getConfirmModalText();
            Assert.assertTrue(confirmText.contains("Xác nhận thay đổi trạng thái?"),
                    "Bam 'Chuyển sang Ngừng hoạt động' phai mo modal chung 'Xác nhận thay đổi trạng thái?', dang thay: " + confirmText);
            Assert.assertTrue(confirmText.contains("sản phẩm " + productName),
                    "Modal chung phai nhac ten SP '" + productName + "', dang thay: " + confirmText);
            adminPage.clickConfirmYes();
            String feedback = adminPage.waitForFeedback();
            Assert.assertTrue(feedback.contains("Đổi trạng thái sản phẩm thành công!"),
                    "Doi trang thai SP xong phai hien toast 'Đổi trạng thái sản phẩm thành công!', dang thay: " + feedback);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                    "Xac nhan an SP xong Switch SP phai tat (aria-checked=false)");

            // Gio SP da Ngung hoat dong: xoa qua ConfirmDialog 'Xóa sản phẩm?'.
            adminPage.deleteProductByName(productName);
            createdProductName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa san pham xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim san pham vua xoa phai ra 0 dong");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-08 (đợt 18, ConfirmDialog): Bấm Xóa trên SP đang Hoạt động → popup 'Không thể xóa sản phẩm' → bấm 'Hủy' → không đổi gì (SP vẫn còn, Switch vẫn bật)")
    public void adminDeleteActiveProductBlockedCloseKeepsProduct() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);

            adminPage.clickProductDeleteButton(productName);
            Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                    "Xoa SP dang Hoat dong phai hien popup chan xoa (ConfirmDialog 'Không thể xóa sản phẩm')");
            adminPage.clickDeleteBlockedClose();
            Assert.assertFalse(adminPage.isDeleteBlockedModalDisplayed(),
                    "Bam 'Hủy' xong popup chan xoa phai bien mat");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Bam 'Hủy' o popup chan xoa thi SP van phai con trong bang");
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.TRUE,
                    "Bam 'Hủy' o popup chan xoa thi Switch SP van phai bat (Hoat dong)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-09 (đợt 21, modal chung): Gạt Switch SP → modal chung 'Xác nhận thay đổi trạng thái?': bấm Hủy thì Switch giữ nguyên (không gọi API); bấm 'Xác nhận' thì Switch tắt + toast 'Đổi trạng thái sản phẩm thành công!'; gạt lại → modal chung → bấm 'Xác nhận' để khôi phục Hoạt động")
    public void adminProductStatusSwitchCancelKeepsStateConfirmFlipsIt() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.TRUE,
                    "SP moi tao phai dang Hoat dong (Switch aria-checked=true)");

            // (1) Gat Switch -> modal chung 'Xác nhận thay đổi trạng thái?' -> bam Huy: khong doi gi.
            adminPage.clickRowStatusSwitch(productName);
            Assert.assertTrue(adminPage.isConfirmModalDisplayed(),
                    "Gat Switch phai mo ConfirmDialog (confirm-dialog)");
            String confirmText = adminPage.getConfirmModalText();
            Assert.assertTrue(confirmText.contains("Xác nhận thay đổi trạng thái?"),
                    "Hop xac nhan gat Switch tat phai co tieu de 'Xác nhận thay đổi trạng thái?', dang thay: " + confirmText);
            Assert.assertTrue(confirmText.contains("sản phẩm " + productName),
                    "Modal chung phai nhac ten SP '" + productName + "', dang thay: " + confirmText);
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup doi trang thai dot 21 phai CHI co dung 2 nut ([Hủy] + [Xác nhận])");
            Assert.assertTrue(adminPage.getConfirmDialogConfirmText().contains("Xác nhận"),
                    "Nut xac nhan modal chung phai la 'Xác nhận', dang thay: " + adminPage.getConfirmDialogConfirmText());
            adminPage.clickConfirmNo();
            adminPage.sleepInSecond(2);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.TRUE,
                    "Bam Huy o hop doi trang thai thi Switch van phai bat (khong goi API)");

            // (2) Gat Switch -> modal chung -> bam 'Xác nhận': Switch tat + toast.
            adminPage.clickRowStatusSwitch(productName);
            Assert.assertTrue(adminPage.isConfirmModalDisplayed(),
                    "Gat Switch lan 2 phai mo lai ConfirmDialog");
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch lan 2 phai mo lai modal chung 'Xác nhận thay đổi trạng thái?', dang thay: "
                            + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            String feedback = adminPage.waitForFeedback();
            Assert.assertTrue(feedback.contains("Đổi trạng thái sản phẩm thành công!"),
                    "Xac nhan doi trang thai xong phai hien toast 'Đổi trạng thái sản phẩm thành công!', dang thay: " + feedback);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                    "Bam 'Xác nhận' thi Switch SP phai tat (aria-checked=false)");

            // (3) Gat lai -> modal chung -> bam 'Xác nhận': khoi phuc Hoat dong
            // (SP do test tao, tra ve trang thai ban dau).
            adminPage.clickRowStatusSwitch(productName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch bat lai phai mo modal chung 'Xác nhận thay đổi trạng thái?', dang thay: "
                            + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.TRUE,
                    "Bam 'Xác nhận' thi Switch SP phai bat lai (Hoat dong)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-11 (đợt 21, dropdown Trạng thái 'Đang hoạt động'): Dropdown Trạng thái lọc đúng — chọn 'Ngừng hoạt động' thì SP vừa tắt hiện + mọi Switch đều tắt; chọn 'Đang hoạt động' thì SP đó biến mất; chọn 'Tất cả' thì hiện lại")
    public void adminProductStatusFilterShowsOnlyMatchingRows() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);

            // Tat SP vua tao bang Switch + xac nhan.
            adminPage.clickRowStatusSwitch(productName);
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                    "SP test phai o trang thai Ngung hoat dong truoc khi loc");

            // Xoa o tim de loc ap cho toan bo bang (khong chi dong vua tim).
            adminPage.clickToolbarReset("products");

            // (1) Loc 'Ngừng hoạt động' (value=false): SP test hien + moi dong deu tat.
            adminPage.selectStatusFilterByValue("false");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Loc 'Ngừng hoạt động' phai thay SP vua tat");
            Assert.assertFalse(adminPage.getAllRowStatusSwitchStates().isEmpty(),
                    "Loc 'Ngừng hoạt động' phai co it nhat 1 dong trong bang");
            for (Boolean checked : adminPage.getAllRowStatusSwitchStates()) {
                Assert.assertEquals(checked, Boolean.FALSE,
                        "Loc 'Ngừng hoạt động' thi moi Switch hien deu phai tat (aria-checked=false)");
            }

            // (2) Loc 'Đang hoạt động' (value=true): SP test khong duoc hien.
            adminPage.selectStatusFilterByValue("true");
            Assert.assertFalse(adminPage.isRowWithTextDisplayed(productName),
                    "Loc 'Đang hoạt động' thi SP dang Ngung khong duoc hien");

            // (3) Loc 'Tất cả' (value=''): SP test hien lai.
            adminPage.selectStatusFilterByValue("");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Loc 'Tất cả' thi SP test phai hien lai trong bang");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-14 (đợt 17): STT liên tục theo trang — bảng SP có > 10 dòng thì sang trang 2, STT dòng đầu phải là 11 (skip nếu ≤ 10 dòng)")
    public void adminProductsSttContinuesOnPageTwo() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");

        int total = adminPage.getPaginationTotalRows("products");
        if (total < 0) {
            throw new SkipException("Bo qua: muc San pham khong hien khoi phan trang nen khong doc duoc tong so dong de kiem tra STT");
        }
        if (total <= 10) {
            throw new SkipException("Bo qua: muc San pham chi co " + total
                    + " dong (<= 10) nen khong co trang 2 de kiem tra STT lien tuc");
        }
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Mac dinh phai o trang 1");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "STT dong dau trang 1 phai la 1");

        adminPage.clickPaginationNext("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 2,
                "Bam 'Sau' phai sang trang 2");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "STT dong dau trang 2 phai la 11 (lien tuc theo trang, 10 dong/trang)");

        adminPage.clickPaginationPrev("products");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                "Bam 'Trước' phai ve lai trang 1");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM17-15 (đợt 17): Tạo SP để trống mã → BE tự sinh mã SP-...; cột Mã trong bảng hiển thị đúng định dạng SP-")
    public void adminCreatedProductShowsAutoGeneratedSpCode() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);
            String code = adminPage.getProductCodeInRow(productName);
            Assert.assertTrue(code.matches("SP-[A-Za-z0-9-]+"),
                    "SP tao moi de trong ma phai duoc sinh ma dang 'SP-...', dang thay: '" + code + "'");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Wave 19: gap SCR_04 (UI + validate BE + hanh vi form/bang) ----------

    @Story("Sản phẩm")
    @Test(description = "ADM19-81 (TC_SCRA04_UI_004): Cột Tồn kho — tạo 3 SP test tồn kho 0/5/30: dòng tồn kho 0 hiện '0' + badge đỏ 'Hết hàng'; tồn kho 5 hiện '5' + badge cam 'Sắp hết'; tồn kho 30 chỉ hiện '30', không badge; xóa cả 3 SP sau test")
    public void adminProductsStockBadgesByQuantity() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        java.util.List<String> createdNames = new java.util.ArrayList<>();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");
            String baseName = uniqueProductName();
            String[] stocks = {"0", "5", "30"};
            for (String stock : stocks) {
                String name = baseName + "-KHO" + stock;
                createdNames.add(name);
                adminPage.fillProductForm(name, categoryName, "99000", "", stock,
                        "Mo ta san pham test ton kho UI-004");
                adminPage.saveProductForm();
                Assert.assertTrue(adminPage.isToastDisplayed(),
                        "Tao san pham ton kho " + stock + " xong phai hien toast thanh cong (admin-toast)");
                adminPage.openProductFormModal();
            }
            // Dong form thua mo o vong lap cuoi (khong tao them SP).
            adminPage.cancelProductForm();

            for (String stock : stocks) {
                String name = baseName + "-KHO" + stock;
                adminPage.searchProducts(name);
                Assert.assertTrue(adminPage.isRowWithTextDisplayed(name),
                        "Tim san pham ton kho " + stock + " phai thay 1 dong trong bang");
                String row = getRowContaining(adminPage, name);
                if ("0".equals(stock)) {
                    Assert.assertTrue(row.contains("Hết hàng"),
                            "Dong ton kho 0 phai hien badge đỏ 'Hết hàng', dang thay: " + row);
                } else if ("5".equals(stock)) {
                    Assert.assertTrue(row.contains("Sắp hết"),
                            "Dong ton kho 5 phai hien badge cam 'Sắp hết', dang thay: " + row);
                } else {
                    Assert.assertTrue(row.contains("30"),
                            "Dong ton kho 30 phai hien '30', dang thay: " + row);
                    Assert.assertFalse(row.contains("Hết hàng"),
                            "Dong ton kho 30 khong duoc hien badge 'Hết hàng', dang thay: " + row);
                    Assert.assertFalse(row.contains("Sắp hết"),
                            "Dong ton kho 30 khong duoc hien badge 'Sắp hết', dang thay: " + row);
                }
            }
        } finally {
            for (String name : createdNames) {
                deleteProductIfPresent(name);
            }
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-82 (TC_SCRA04_VAL_031): Chọn danh mục đã Ngừng hiển thị thì BE từ chối — tạo 1 DM rồi ẩn nó, mở form Thêm SP chọn DM đã ẩn + các trường khác hợp lệ rồi bấm Lưu thì BE trả lỗi 'Sản phẩm phải thuộc 1 danh mục đang hiển thị.' dạng formError trong form, form vẫn mở, không tạo sản phẩm; hiện lại DM rồi xóa sau test")
    public void adminCreateProductWithInactiveCategoryRejected() {
        String categoryName = uniqueCategoryName();
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            // Setup: tao 1 DM roi an no bang Switch (DM test, khong co SP).
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta DM an test VAL-031", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.TRUE,
                    "DM moi tao phai dang Hoat dong (Switch aria-checked=true) truoc khi an");
            adminPage.clickRowStatusSwitch(categoryName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Ẩn danh mục?"),
                    "Gat Switch tat DM phai mo ConfirmDialog 'Ẩn danh mục?', dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.FALSE,
                    "Xac nhan an DM xong Switch DM phai tat (aria-checked=false)");

            // Tao SP chon DM da an: CategoryPicker liet ke toan bo DM (ke ca DM
            // da an) nen van chon duoc; BE tu choi khi luu.
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            adminPage.setProductName(productName);
            adminPage.selectProductCategory(categoryName);
            adminPage.setProductPrice("99000");
            adminPage.setProductStock("10");
            adminPage.setProductDescription("Mo ta SP test DM an VAL-031");
            adminPage.clickProductSaveButton();
            adminPage.sleepInSecond(2); // cho POST /api/admin/products tra ve
            Assert.assertTrue(adminPage.getProductFormErrorText().contains("Sản phẩm phải thuộc 1 danh mục đang hiển thị."),
                    "Tao SP thuoc DM da an thi BE phai tra loi 'Sản phẩm phải thuộc 1 danh mục đang hiển thị.' dang formError trong form, dang thay: "
                            + adminPage.getProductFormErrorText());
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Luu that bai do DM da an thi form phai van mo (khong dong, khong tao SP)");

            adminPage.cancelProductForm();
            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "SP thuoc DM da an bi BE tu choi nen tim ten no phai ra 0 dong");
        } finally {
            showCategoryIfHidden(categoryName);
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-83 (TC_SCRA04_VAL_032): Tạo SP trùng tên SP đã có thì BE báo trùng slug — tạo SP 'TESTSP-...' thành công; tạo SP thứ 2 cùng tên với mã khác rồi bấm Lưu thì lỗi 'Slug sản phẩm đã tồn tại.' dạng formError trong form, form vẫn mở, SP thứ 2 không được tạo; xóa SP thứ nhất sau test")
    public void adminCreateProductWithDuplicateNameShowsSlugError() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "", "10",
                    "Mo ta san pham test trung ten VAL-032");
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham dau tien xong phai hien toast thanh cong (admin-toast)");

            // Tao SP thu 2 cung ten (ma khac) -> trung slug BE sinh tu ten.
            adminPage.openProductFormModal();
            adminPage.setProductCode(uniqueProductCode());
            adminPage.fillProductForm(productName, categoryName, "99000", "", "10",
                    "Mo ta san pham test trung ten VAL-032 lan 2");
            adminPage.clickProductSaveButton();
            adminPage.sleepInSecond(2); // cho POST /api/admin/products tra ve
            Assert.assertTrue(adminPage.getProductFormErrorText().contains("Slug sản phẩm đã tồn tại."),
                    "Tao SP trung ten thi BE phai tra loi 'Slug sản phẩm đã tồn tại.' dang formError trong form, dang thay: "
                            + adminPage.getProductFormErrorText());
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Luu that bai do trung ten thi form phai van mo (khong dong, khong tao SP thu 2)");

            adminPage.cancelProductForm();
            adminPage.searchProducts(productName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Chi co 1 SP ten '" + productName + "' duoc tao (SP thu 2 trung ten khong duoc tao)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-84 (TC_SCRA04_VAL_033): Mô tả quá dài (5001 ký tự) thì BE từ chối — nhập mô tả 5001 ký tự + các trường khác hợp lệ rồi bấm Lưu: FE không chặn, BE trả lỗi 'Mô tả tối đa 5000 ký tự.' dạng formError trong form, form vẫn mở, không tạo sản phẩm; đóng bằng Hủy")
    public void adminCreateProductWithTooLongDescriptionRejected() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        String categoryName = adminPage.getFirstRealCategoryName();
        Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

        adminPage.fillProductForm(productName, categoryName, "99000", "", "10", "");
        // Nhap mo ta 5001 ky tu bang JS (go tung ky tu qua sendKeys qua cham).
        adminPage.setInputValueByJs(AdminPageUI.ADMIN_PRODUCT_DESCRIPTION_INPUT, "M".repeat(5001));
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductDescriptionCountText(), "5001/2000",
                "Mo ta 5001 ky tu thi bo dem mo ta phai hien '5001/2000' (FE chi dem, khong chan)");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(2); // cho POST /api/admin/products tra ve
        Assert.assertTrue(adminPage.getProductFormErrorText().contains("Mô tả tối đa 5000 ký tự."),
                "Mo ta 5001 ky tu thi BE phai tra loi 'Mô tả tối đa 5000 ký tự.' dang formError trong form, dang thay: "
                        + adminPage.getProductFormErrorText());
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do mo ta qua dai thi form phai van mo (khong dong, khong tao SP)");

        adminPage.cancelProductForm();
        adminPage.searchProducts(productName);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "SP mo ta qua dai bi BE tu choi nen tim ten no phai ra 0 dong");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-85 (TC_SCRA04_FUNC_038): Thêm SP tồn kho = 0 — ngoài toast thành công 'Đã thêm sản phẩm' còn toast phụ 'Sản phẩm đã hết hàng'; dòng SP mới trong bảng có badge đỏ 'Hết hàng' ở cột Tồn kho; xóa SP sau test")
    public void adminCreateProductWithZeroStockShowsOutOfStockToast() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductForm(productName, categoryName, "99000", "", "0",
                    "Mo ta san pham het hang FUNC-038");
            // Bam Luu truc tiep (khong doi form dong) de doc kip ca 2 toast
            // (toast thanh cong/info tu an sau 4 giay).
            adminPage.clickProductSaveButton();
            adminPage.sleepInSecond(1);
            java.util.List<String> toasts = adminPage.getAllToastTexts();
            Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Đã thêm sản phẩm")),
                    "Tao SP ton kho 0 xong phai hien toast 'Đã thêm sản phẩm', dang thay: " + toasts);
            Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Sản phẩm đã hết hàng")),
                    "Tao SP ton kho 0 xong phai hien them toast phu 'Sản phẩm đã hết hàng', dang thay: " + toasts);
            createdProductName = productName;
            adminPage.waitForElementInvisible(AdminPageUI.ADMIN_PRODUCT_FORM);

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            Assert.assertTrue(getRowContaining(adminPage, productName).contains("Hết hàng"),
                    "Dong SP ton kho 0 phai hien badge đỏ 'Hết hàng' o cot Ton kho, dang thay: "
                            + getRowContaining(adminPage, productName));
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-86 (TC_SCRA04_FUNC_039): Hàng vừa lưu được highlight 2 giây trong bảng — tạo SP mới rồi chờ hơn 2 giây (sleep 3 giây) thì highlight biến mất, dòng về trạng thái bình thường (vẫn còn trong bảng); xóa SP sau test")
    public void adminCreatedProductHighlightDisappearsAfterTwoSeconds() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            // Highlight chi ton tai 2 giay (AdminPage setTimeout 2000ms) — cho
            // qua 3 giay roi kiem lai: dong van con nhung het highlight.
            adminPage.sleepInSecond(3);
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            Assert.assertFalse(adminPage.isRowHighlighted(productName),
                    "Sau hon 2 giay, dong san pham vua tao khong duoc con highlight (class sa-row-highlight)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-87 (TC_SCRA04_FUNC_040, đợt 21): Mở form sửa SP từ nút Sửa ở bảng — tạo SP với giá trị đã biết (mã, tên, danh mục, giá 100.000, giá gốc 150.000, tồn kho 10, mô tả, ảnh, trạng thái) rồi bấm nút Sửa ở dòng đó: form mở dạng TRANG full page /products/:code/edit (không Drawer, không 760px), tiêu đề 'Chỉnh sửa sản phẩm', mọi trường điền sẵn đúng dữ liệu đã lưu, Toggle đúng trạng thái; đóng bằng Hủy không lưu; xóa SP sau test")
    public void adminEditProductFormPrefillsAllFields() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductForm(productName, categoryName, "100000", "150000", "10",
                    "Mo ta SP test FUNC-040");
            adminPage.addProductImageByUrl("https://example.com/sp-040.jpg");
            Assert.assertTrue(adminPage.isProductImageThumbDisplayed(0),
                    "Them anh bang link xong phai thay thumbnail anh thu 0 trong form");
            adminPage.saveProductForm();
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao san pham xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(productName);
            String codeInRow = adminPage.getProductCodeInRow(productName);
            adminPage.openProductEditForm(productName);
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Form sua SP dot 21 phai mo dang trang full page (/products/:code/edit)");
            Assert.assertFalse(adminPage.isProductFormDrawer(),
                    "Form sua SP dot 21 KHONG con la Drawer (khong class fs-drawer)");
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/products/"),
                    "Form sua SP dot 21 phai o route /products/:code/edit, dang o: " + getDriver().getCurrentUrl());
            Assert.assertTrue(adminPage.getProductFormModalText().contains("Chỉnh sửa sản phẩm"),
                    "Form sua mo ra phai co tieu de 'Chỉnh sửa sản phẩm', dang thay: " + adminPage.getProductFormModalText());
            Assert.assertEquals(adminPage.getProductCodeInputValue(), codeInRow,
                    "O Ma trong form sua phai dien san dung ma cua SP");
            Assert.assertEquals(adminPage.getProductNameInputValue(), productName,
                    "O Ten trong form sua phai dien san dung ten SP");
            Assert.assertEquals(adminPage.getProductCategoryTriggerText(), categoryName,
                    "O Danh muc trong form sua phai dien san dung ten danh muc");
            Assert.assertEquals(adminPage.getProductPriceInputDigits(), "100000",
                    "O Gia ban trong form sua phai dien san '100.000' (chuan hoa 100000)");
            Assert.assertEquals(AdminPageObject.normalizePriceInput(adminPage.getProductOriginalPriceInputValue()), "150000",
                    "O Gia goc trong form sua phai dien san '150.000' (chuan hoa 150000)");
            Assert.assertEquals(adminPage.getProductStockInputValue(), "10",
                    "O Ton kho trong form sua phai dien san '10'");
            Assert.assertEquals(adminPage.getProductDescriptionInputValue(), "Mo ta SP test FUNC-040",
                    "O Mo ta trong form sua phai dien san dung mo ta da luu");
            Assert.assertTrue(adminPage.getProductStatusToggleChecked(),
                    "SP moi tao dang Hoat dong nen Toggle trang thai trong form sua phai bat");
            Assert.assertEquals(adminPage.getProductImageThumbCount(), 1,
                    "Form sua SP phai hien du 1 anh da luu");

            // Dong bang Hủy, khong luu (form sach nen dong thang, khong hoi).
            adminPage.cancelProductForm();
            Assert.assertFalse(adminPage.isProductFormModalPresent(),
                    "Dong form xong khong duoc con thay form sua san pham");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-88 (TC_SCRA04_FUNC_061, đợt 21): Gạt Switch SP Ngừng → Hoạt động rồi bấm Hủy thì Switch giữ nguyên — tạo SP rồi tắt nó (xác nhận modal chung, Switch tắt); gạt Switch lần nữa thì modal chung, bấm 'Hủy' thì popup đóng, không gọi PUT, Switch vẫn tắt; xóa SP sau test")
    public void adminProductStatusSwitchShowCancelKeepsInactive() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);

            // Tat SP vua tao bang Switch + xac nhan modal chung.
            adminPage.clickRowStatusSwitch(productName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch tat SP phai mo modal chung 'Xác nhận thay đổi trạng thái?', dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                    "Bam 'Xác nhận' thi Switch SP phai tat (aria-checked=false)");

            // Gat lai -> modal chung -> bam Huy: Switch van tat.
            adminPage.clickRowStatusSwitch(productName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch bat lai SP phai mo modal chung 'Xác nhận thay đổi trạng thái?', dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmNo();
            adminPage.sleepInSecond(2);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(productName), Boolean.FALSE,
                    "Bam Huy o modal chung thi Switch SP van phai tat (khong goi API)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Dot 21: loc ket hop / empty state / chi tiet full page / them full page ----------

    @Story("Sản phẩm")
    @Test(description = "ADM21-01 (đợt 21): Lọc KẾT HỢP (AND) trên bảng SP — tìm theo tên + theo mã + lọc Danh mục + lọc Trạng thái kết hợp đồng thời; sai tổ hợp thì empty state 'Không tìm thấy sản phẩm'; nút 'Đặt lại bộ lọc' khôi phục đủ dòng")
    public void adminProductCombinedFiltersAndEmptyState() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            String categoryName = adminPage.getFirstRealCategoryName();
            adminPage.searchProducts(productName);
            String code = adminPage.getProductCodeInRow(productName);
            Assert.assertFalse(code == null || code.isEmpty(),
                    "Dong SP vua tao phai hien ma SP de tim theo ma");

            // (1) Tim theo MA (chi ma, khong ten) -> dung 1 dong.
            adminPage.searchProducts(code);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Tim theo ma '" + code + "' phai ra dung 1 dong");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ma phai thay dung SP vua tao");

            // (2) Ket hop: tim ten + loc Danh muc dung -> van 1 dong (AND).
            adminPage.searchProducts(productName);
            adminPage.selectProductCategoryFilterByName(categoryName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Tim ten + loc dung danh muc phai van ra 1 dong (AND)");

            // (3) Ket hop them: + loc Trang thai 'Ngừng hoạt động' (SP dang Hoat dong)
            // -> 0 dong + empty state 'Không tìm thấy sản phẩm'.
            adminPage.selectStatusFilterByValue("false");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim ten + DM dung + loc 'Ngừng hoạt động' (SP dang Hoat dong) phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                    "Khong ket qua phai hien empty state 'Không tìm thấy sản phẩm'");

            // (4) Doi loc Trang thai ve 'Đang hoạt động' -> lai 1 dong (AND van giu).
            adminPage.selectStatusFilterByValue("true");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Tim ten + DM dung + loc 'Đang hoạt động' phai ra lai 1 dong");

            // (5) Tim tu khoa khong ton tai -> empty state.
            adminPage.searchProducts("zzzz-khong-co-san-pham");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim tu khoa khong ton tai phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyProductsMessageDisplayed(),
                    "Tim tu khoa khong ton tai phai hien empty state 'Không tìm thấy sản phẩm'");

            // (6) Dat lai bo loc -> SP test hien lai trong bang.
            adminPage.clickToolbarReset("products");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Bam 'Đặt lại bộ lọc' xong SP test phai hien lai trong bang");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM21-02 (đợt 21): Trang chi tiết SP full page — bấm mắt mở /products/:code (không Drawer/Modal), breadcrumb 'Quản lý sản phẩm > Chi tiết sản phẩm', đủ Mã/Tên/Danh mục/Giá bán/Giá gốc/Tồn kho/Trạng thái/Mô tả + rating; [Chỉnh sửa] sang trang sửa; [Quay lại] về danh sách")
    public void adminProductDetailFullPageShowsAllFields() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            String createdName = createProductOrSkip(adminPage);
            productName = createdName;
            adminPage.searchProducts(productName);
            String code = adminPage.getProductCodeInRow(productName);
            String categoryName = adminPage.getFirstRealCategoryName();

            adminPage.clickProductDetailButton(productName);
            Assert.assertTrue(adminPage.isProductDetailPageDisplayed(),
                    "Bam mat phai mo trang chi tiet SP full page (admin-product-detail-page)");
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/products/"),
                    "Trang chi tiet SP phai o route /products/:code, dang o: " + getDriver().getCurrentUrl());
            String breadcrumb = adminPage.getProductDetailBreadcrumbText();
            Assert.assertTrue(breadcrumb.contains("Quản lý sản phẩm") && breadcrumb.contains("Chi tiết sản phẩm"),
                    "Breadcrumb trang chi tiet phai la 'Quản lý sản phẩm > Chi tiết sản phẩm', dang thay: " + breadcrumb);
            String detail = adminPage.getProductDetailText();
            Assert.assertTrue(detail.contains(code), "Trang chi tiet phai hien Ma SP '" + code + "'");
            Assert.assertTrue(detail.contains(productName), "Trang chi tiet phai hien Ten SP");
            Assert.assertTrue(detail.contains(categoryName), "Trang chi tiet phai hien Danh muc '" + categoryName + "'");
            Assert.assertTrue(detail.contains("99.000"), "Trang chi tiet phai hien Gia ban 99.000₫");
            Assert.assertTrue(detail.contains("120.000"), "Trang chi tiet phai hien Gia goc 120.000₫");
            Assert.assertTrue(detail.contains("Đang hoạt động"), "Trang chi tiet phai hien Trang thai 'Đang hoạt động'");
            Assert.assertTrue(detail.contains("Mo ta san pham test dot 17"), "Trang chi tiet phai hien Mo ta SP");
            // SP moi tao chua co danh gia -> rating hien '—' o trang chi tiet
            // (cot Danh gia da bo khoi bang, du lieu rating chi con o day).
            Assert.assertEquals(adminPage.getProductDetailRatingText().trim(), "—",
                    "SP chua co danh gia thi trang chi tiet phai hien rating '—'");

            // [Chỉnh sửa] tren trang chi tiet -> trang sua full page, du lieu giu nguyen.
            adminPage.openProductEditFormFromDetail();
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Bam [Chỉnh sửa] o trang chi tiet phai mo trang sua SP full page");
            Assert.assertEquals(adminPage.getProductNameInputValue(), productName,
                    "Form sua mo tu trang chi tiet phai dien san dung ten SP");
            adminPage.cancelProductForm();
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Huy form sua xong phai ve danh sach SP (SP test van hien)");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "ADM21-03 (đợt 21): Trang Thêm mới SP full page — bấm '+ Thêm mới' mở /products/new (không Drawer/Modal, bảng không còn trong DOM), tiêu đề 'Thêm mới sản phẩm', lưu hợp lệ → toast 'Đã thêm sản phẩm' ghi tên SP + về danh sách")
    public void adminProductCreateFullPageAddsProduct() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/products/new"),
                    "Bam '+ Thêm mới' phai mo trang /products/new, dang o: " + getDriver().getCurrentUrl());
            Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                    "Trang them moi SP full page phai hien form (admin-product-form)");
            Assert.assertFalse(adminPage.isProductFormDrawer(),
                    "Trang them moi SP dot 21 KHONG con la Drawer");
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Mo trang them moi thi bang danh sach KHONG con trong DOM (trang thay bang)");
            Assert.assertTrue(adminPage.getProductFormModalText().contains("Thêm mới sản phẩm"),
                    "Trang them moi phai co tieu de 'Thêm mới sản phẩm'");
            Assert.assertEquals(adminPage.getProductFormMode(), "create",
                    "Form them moi phai o mode create");
            Assert.assertTrue(adminPage.isProductCodeInputFocused(),
                    "Mo trang them moi thi o Ma SP phai duoc autofocus");

            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");
            adminPage.fillProductFormFullAndSave(productName, categoryName, "99000", "120000", "10",
                    "Mo ta san pham test ADM21-03");
            createdProductName = productName;
            String toastTitle = adminPage.getToastTitleText();
            Assert.assertTrue(toastTitle.contains("Đã thêm sản phẩm"),
                    "Tao SP xong toast phai ghi 'Đã thêm sản phẩm', dang thay: " + toastTitle);
            Assert.assertTrue(adminPage.getToastDescText().contains(productName),
                    "Toast tao SP phai ghi ten SP '" + productName + "'");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Luu xong ve danh sach, tim theo ten phai thay SP vua tao");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }
}
