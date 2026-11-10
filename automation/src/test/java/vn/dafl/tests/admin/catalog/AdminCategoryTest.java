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
 * Test CRUD danh muc tren app Admin rieng localhost:3001 (dot 9, tach tu
 * AdminCrudTest cu khi chia thu muc admin dot 17 — moi lop chi lo 1 phan he).
 * Chi them/sua/xoa THAT tren du lieu do chinh test tao (ten kem timestamp) va
 * tu don dep sau khi chay; KHONG xoa/sua du lieu seed.
 * Dot 18 wave D: form them/sua DM la DRAWER RONG 680px
 * (admin-category-form-modal, class fs-drawer, --fs-width, overlay — bang
 * danh sach VAN O LAI trong DOM khi form mo); bang
 * them cot STT + cot Ma (DM-xxxx tu sinh khi de trong) + cot Trang thai dang
 * Switch; xoa DM dang Hoat dong bi chan boi ConfirmDialog 'Không thể xóa danh
 * mục' (CHI 2 nut [Hủy] + [Chuyển sang Ngừng hoạt động]) roi moi mo
 * 'Ẩn danh mục?'; trung ma thi CA toast CA message do duoi o Ma
 * ('Mã danh mục đã tồn tại.').
 * Dot 21: them/sua/xem DM la TRANG FULL PAGE (/categories/new,
 * /categories/:code, /categories/:code/edit — KHONG con Drawer/Modal);
 * tim/loc TU DONG (debounce 120ms, KHONG con nut Tim kiem); gạt Switch dung
 * modal xac nhan CHUNG 'Xác nhận thay đổi trạng thái?' + toast 'Đổi trạng
 * thái danh mục thành công!' (khong con popup C6/C7 'Ẩn/Hiển thị danh mục?').
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý danh mục")
public class AdminCategoryTest extends BaseTest {

    // Trang thai de don dep (chi du lieu do test tao); moi test method co browser moi
    // nen cac truong nay chi song trong pham vi 1 test.
    private String createdCategoryName;
    private boolean adminLoggedIn = false;

    // ---------- Helper dung chung ----------

    /** Ten danh muc duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueCategoryName() {
        return "TESTDM-" + System.currentTimeMillis();
    }

    /** Ma danh muc duy nhat theo tung lan chay (11 ky tu: DMDUP + 6 so cuoi
     *  cua timestamp — hop le 3-20 ky tu, chi chu in hoa + so). */
    private String uniqueCategoryCode() {
        return "DMDUP" + String.valueOf(System.currentTimeMillis()).substring(7);
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

    // ---------- Danh muc (ACR-04, goc dot 9, viet lai dot 17 theo form trang rieng) ----------

    @Story("Danh mục")
    @Test(description = "ACR-04 (đợt 18 wave D, form Drawer rộng 680px): Tạo danh mục mới (slug tự sinh + mô tả + thứ tự) → sửa tên (toggle trạng thái hiện) → xóa qua ConfirmDialog; mỗi bước đều kiểm tra qua tìm kiếm")
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

            // DM moi tao mac dinh Hoat dong: deleteCategoryByName tu xu ly popup
            // chan xoa (chuyen Ngung hoat dong) roi moi xoa qua ConfirmDialog.
            adminPage.deleteCategoryByName(editedName);
            createdCategoryName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.isToastDisplayed(), "Xoa danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(editedName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Tim danh muc vua xoa phai ra 0 dong");
            Assert.assertTrue(adminPage.isEmptyCategoriesMessageDisplayed(),
                    "Tim danh muc vua xoa phai hien dong 'Chưa có danh mục'");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    // ---------- Dot 17: ma trung / ma disable / cot Ma ----------

    @Story("Danh mục")
    @Test(description = "ADM17-02 (đợt 17): Tạo DM với mã trùng mã DM đã có → CẢ toast CẢ message đỏ dưới ô Mã đều là 'Mã danh mục đã tồn tại.', form vẫn mở, DM thứ 2 không được tạo")
    public void adminCreateCategoryWithDuplicateCodeShowsInlineError() {
        String code = uniqueCategoryCode();
        String firstName = uniqueCategoryName();
        String secondName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");

            adminPage.createCategoryFullWithCode(code, firstName, "", "Mo ta danh muc test ADM17-02", "5");
            createdCategoryName = firstName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc dau tien xong phai hien toast thanh cong (admin-toast)");

            // Tao DM thu 2 trung ma vua dung (ten khac, ma giong).
            adminPage.openCategoryForm();
            adminPage.setCategoryCode(code);
            adminPage.fillCategoryForm(secondName, "", "Mo ta danh muc test ADM17-02 lan 2", "6");
            adminPage.clickCategorySaveButton();
            String toast = adminPage.waitForToast();
            Assert.assertTrue(toast.contains("Mã danh mục đã tồn tại."),
                    "Trung ma DM thi toast phai ghi 'Mã danh mục đã tồn tại.', dang thay: " + toast);
            Assert.assertEquals(adminPage.getCategoryFieldErrorText("code"), "Mã danh mục đã tồn tại.",
                    "Trung ma DM thi message do duoi o Ma phai ghi 'Mã danh mục đã tồn tại.'");
            Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                    "Luu trung ma that bai thi form phai van mo (khong dong, khong tao DM)");

            // Dong form (dirty -> hop 'Thoát mà chưa lưu?' -> bam Thoat).
            adminPage.cancelCategoryForm();
            Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                    "Dong form xong khong duoc con thay form danh muc");

            adminPage.searchCategories(secondName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "DM thu 2 trung ma khong duoc tao nen tim ten no phai ra 0 dong");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM17-05 (đợt 17): Mở form sửa DM → ô Mã bị disable (hiện đúng mã DM-...), ô Tên sửa được; bấm Hủy + 'Thoát' thì không đổi gì (tên cũ vẫn còn, tên mới không có)")
    public void adminEditCategoryFormDisablesCodeInput() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta danh muc test ADM17-05", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchCategories(categoryName);
            String codeInRow = adminPage.getCategoryCodeInRow(categoryName);
            Assert.assertTrue(codeInRow.startsWith("DM-"),
                    "Ma DM trong bang phai bat dau 'DM-', dang thay: " + codeInRow);

            adminPage.openCategoryEditForm(categoryName);
            Assert.assertTrue(adminPage.isCategoryCodeInputDisabled(),
                    "Mo form sua DM thi o Ma phai bi disable (khong sua duoc ma)");
            Assert.assertEquals(adminPage.getCategoryCodeInputValue(), codeInRow,
                    "O Ma trong form sua phai hien dung ma cua DM, dang thay: " + adminPage.getCategoryCodeInputValue());

            // Doi ten trong form (khong luu) de chung minh truong khac sua duoc.
            String editedName = categoryName + "-SUA";
            adminPage.setCategoryName(editedName);
            Assert.assertEquals(adminPage.getCategoryNameInputValue(), editedName,
                    "Go ten moi vao o Ten phai hien dung ten moi trong form");

            // Huy: form dirty -> hop 'Thoát mà chưa lưu?' -> bam Thoat.
            adminPage.cancelCategoryForm();
            Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                    "Dong form xong khong duoc con thay form sua danh muc");

            adminPage.searchCategories(editedName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Huy sua thi ten moi khong duoc luu, tim ten moi phai ra 0 dong");
            adminPage.searchCategories(categoryName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Huy sua thi ten cu van phai con trong bang");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM17-16 (đợt 17): Tạo DM để trống mã → BE tự sinh mã DM-...; cột Mã trong bảng hiển thị đúng định dạng DM-")
    public void adminCreatedCategoryShowsAutoGeneratedDmCode() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta danh muc test ADM17-16", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchCategories(categoryName);
            String code = adminPage.getCategoryCodeInRow(categoryName);
            Assert.assertTrue(code.matches("DM-[A-Za-z0-9-]+"),
                    "DM tao moi de trong ma phai duoc sinh ma dang 'DM-...', dang thay: '" + code + "'");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    // ---------- Wave 19: gap SCR_05 (UI bang + validate BE + hanh vi bang/form) ----------

    @Story("Danh mục")
    @Test(description = "ADM19-89 (TC_SCRA05_UI_001): Bảng DM có 9 cột (STT/Mã DM/Tên/Slug/Mô tả/Số SP/Thứ tự/Trạng thái/Thao tác) + toolbar chuẩn: cột Mã bắt đầu 'DM-', cột Trạng thái là Switch, mỗi dòng có đúng 3 nút thao tác (Xem/Sửa/Xóa), ô tìm kiếm placeholder 'Tìm tên, slug danh mục...', dropdown Trạng thái có đủ 3 lựa chọn (Tất cả/Hoạt động/Ngừng hoạt động), nút + Thêm nằm cuối toolbar (phải nhất), icon Tìm/Đặt lại hiện, KHÔNG có nút Export")
    public void adminCategoriesTableHasNineColumnsAndStandardToolbar() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        java.util.List<String> headers = adminPage.getAdminTableHeaderTexts();
        Assert.assertEquals(headers, java.util.Arrays.asList(
                        "STT", "Mã DM", "Tên", "Slug", "Mô tả", "Số SP", "Thứ tự", "Trạng thái", "Thao tác"),
                "Bang DM dot 18 phai co dung 9 cot theo thu tu STT/Mã DM/Tên/Slug/Mô tả/Số SP/Thứ tự/Trạng thái/Thao tác, dang thay: " + headers);
        String firstCategoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (firstCategoryName == null || firstCategoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de kiem tra cot Ma/Switch/nut thao tac");
        }
        String firstCode = adminPage.getCategoryCodeInRow(firstCategoryName);
        Assert.assertTrue(firstCode.startsWith("DM-"),
                "Cot Ma DM o dong dau tien phai bat dau bang 'DM-', dang thay: '" + firstCode + "'");
        Assert.assertFalse(adminPage.getAllRowStatusSwitchStates().isEmpty(),
                "Cot Trang thai trong bang DM phai la Switch (moi dong co 1 Switch)");
        Assert.assertEquals(adminPage.getFirstRowActionButtonCount(), 3,
                "Moi dong DM phai co dung 3 nut thao tac (Xem chi tiet/Sua/Xoa)");
        Assert.assertEquals(adminPage.getElementAttribute(AdminPageUI.ADMIN_CATEGORY_SEARCH, "placeholder"),
                "Tìm tên, slug danh mục...",
                "O tim kiem DM phai co placeholder 'Tìm tên, slug danh mục...'");
        Assert.assertEquals(adminPage.getStatusFilterOptionTexts(),
                java.util.Arrays.asList("Tất cả", "Hoạt động", "Ngừng hoạt động"),
                "Dropdown Trang thai DM phai co du 3 lua chon Tất cả/Hoạt động/Ngừng hoạt động");
        Assert.assertTrue(adminPage.isToolbarAddButtonLast("categories"),
                "Nut '+ Thêm mới' phai nam cuoi toolbar (phai nhat) o muc Danh muc");
        Assert.assertTrue(adminPage.isToolbarSearchIconDisplayed("categories"),
                "Toolbar DM phai co icon Tim kiem (Lucide Search, khong dung emoji)");
        Assert.assertTrue(adminPage.isToolbarResetIconDisplayed("categories"),
                "Toolbar DM phai co icon Dat lai (Lucide RotateCcw, khong dung emoji)");
        Assert.assertTrue(adminPage.isToolbarResetButtonDisplayed("categories"),
                "Toolbar DM phai co nut Dat lai");
        Assert.assertFalse(adminPage.isToolbarSearchButtonDisplayed("categories"),
                "Toolbar DM dot 21 KHONG duoc co nut Tim kiem (tim/loc tu dong)");
        Assert.assertFalse(adminPage.isExportButtonDisplayed(),
                "Toolbar DM dot 18 KHONG duoc co nut Export");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-90 (TC_SCRA05_UI_002, dot 21): Bấm '+ Thêm mới' mở TRANG Thêm DM full page /categories/new — tiêu đề 'Thêm mới danh mục', phụ đề 'Thêm danh mục mới cho sàn.', bảng danh sách KHÔNG còn trong DOM (trang thay thế danh sách, không Drawer overlay), không có nút 'Quay lại' trong form; đóng bằng Hủy thì về danh sách")
    public void adminCategoryAddOpensDrawer680() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        adminPage.openCategoryForm();
        Assert.assertTrue(adminPage.isCategoryCreatePageDisplayed(),
                "Bam '+ Thêm mới' dot 21 phai mo TRANG them DM full page (admin-category-create-page)");
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Trang them DM phai hien form danh muc (admin-category-form)");
        String formText = adminPage.getCategoryFormModalText();
        Assert.assertTrue(formText.contains("Thêm mới danh mục"),
                "Trang them DM phai co tieu de 'Thêm mới danh mục', dang thay: " + formText);
        Assert.assertTrue(formText.contains("Thêm danh mục mới cho sàn."),
                "Trang them DM phai co phu de 'Thêm danh mục mới cho sàn.', dang thay: " + formText);
        Assert.assertFalse(adminPage.isAdminTablePresent(),
                "Mo trang them DM full page thi bang danh sach KHONG duoc con trong DOM (trang thay the danh sach)");
        Assert.assertFalse(adminPage.isElementDisplayed("css=[data-testid='admin-form-back-btn']"),
                "Trang them DM dot 21 KHONG duoc co nut 'Quay lại' (admin-form-back-btn da bo)");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
        Assert.assertTrue(adminPage.isCategoryListPageDisplayed(),
                "Huy trang them DM xong phai ve lai danh sach DM");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-91 (TC_SCRA05_VAL_016): Slug để trống được tự sinh khi lưu — nhập Tên DM mới, ô Slug tự điền slug theo tên; xóa trắng ô Slug rồi bấm Lưu thì vẫn tạo được DM, dòng DM mới trong bảng hiện đúng slug đã tự sinh theo tên; xóa DM sau test")
    public void adminCategoryEmptySlugAutoGeneratedOnSave() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.openCategoryForm();
            adminPage.setCategoryName(categoryName);
            adminPage.sleepInSecond(1); // cho FE tu sinh slug tu ten
            String autoSlug = adminPage.getCategorySlugValue();
            Assert.assertEquals(autoSlug, categoryName.toLowerCase(),
                    "Nhap Ten '" + categoryName + "' thi o Slug phai tu dien '" + categoryName.toLowerCase() + "'");

            adminPage.setCategorySlug("");
            Assert.assertEquals(adminPage.getCategorySlugValue(), "",
                    "Xoa trang o Slug thi o Slug phai trong");
            adminPage.clickCategorySaveButton();
            adminPage.waitForFeedback();
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchCategories(categoryName);
            String row = getRowContaining(adminPage, categoryName);
            Assert.assertTrue(row.contains(autoSlug),
                    "DM luu voi slug de trong phai duoc tu sinh slug '" + autoSlug + "' hien trong bang, dang thay: " + row);
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-92 (TC_SCRA05_VAL_017): Tạo DM trùng slug DM đã có thì báo trùng — tạo DM1 rồi đọc slug của nó qua form sửa; tạo DM2 với slug nhập tay bằng slug DM1 rồi bấm Lưu thì toast 'Đường dẫn đã tồn tại' + lỗi inline 'Slug danh mục đã tồn tại.' dưới ô Slug, form vẫn mở, DM2 không được tạo; xóa DM1 sau test")
    public void adminCategoryDuplicateSlugShowsInlineError() {
        String categoryName1 = uniqueCategoryName();
        String categoryName2 = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName1, "", "Mo ta DM1 test trung slug VAL-017", "5");
            createdCategoryName = categoryName1;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc 1 xong phai hien toast thanh cong (admin-toast)");

            adminPage.openCategoryEditForm(categoryName1);
            String slug1 = adminPage.getCategorySlugValue();
            Assert.assertFalse(slug1 == null || slug1.isEmpty(),
                    "DM1 phai co slug (tu sinh tu ten) de dung lam slug trung cho DM2");
            adminPage.cancelCategoryForm();

            adminPage.openCategoryForm();
            adminPage.setCategoryName(categoryName2);
            adminPage.setCategorySlug(slug1);
            adminPage.clickCategorySaveButton();
            adminPage.sleepInSecond(2); // cho POST /api/admin/categories tra ve
            java.util.List<String> toasts = adminPage.getAllToastTexts();
            Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Đường dẫn đã tồn tại")),
                    "Tao DM trung slug thi phai hien toast 'Đường dẫn đã tồn tại', dang thay: " + toasts);
            Assert.assertEquals(adminPage.getCategoryFieldErrorText("slug"), "Slug danh mục đã tồn tại.",
                    "Tao DM trung slug thi phai bao loi inline 'Slug danh mục đã tồn tại.' duoi o Slug");
            Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                    "Luu that bai do trung slug thi form phai van mo (khong dong, khong tao DM2)");

            adminPage.cancelCategoryForm();
            adminPage.searchCategories(categoryName2);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "DM2 trung slug bi BE tu choi nen tim ten no phai ra 0 dong");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-93 (TC_SCRA05_FUNC_024, dot 21): Mở form sửa DM từ nút Sửa ở bảng — tạo DM với giá trị đã biết (mã, tên, slug, mô tả, thứ tự 7, icon, trạng thái) rồi bấm nút Sửa ở dòng đó: form mở dạng TRANG full page /categories/:code/edit, tiêu đề 'Chỉnh sửa danh mục', phụ đề 'Chỉnh sửa thông tin danh mục.', mọi trường điền sẵn đúng dữ liệu đã lưu, header [Hủy][Lưu thay đổi]; đóng bằng Hủy không lưu; xóa DM sau test")
    public void adminCategoryEditFormPrefillsData() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.openCategoryForm();
            adminPage.setCategoryName(categoryName);
            adminPage.setCategoryDescription("Mo ta DM test FUNC-024");
            adminPage.setCategorySortOrder("7");
            adminPage.addCategoryImageByUrl("https://example.com/dm-024.jpg");
            Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                    "Them icon bang link xong phai thay thumbnail icon trong form");
            adminPage.clickCategorySaveButton();
            adminPage.waitForFeedback();
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchCategories(categoryName);
            String codeInRow = adminPage.getCategoryCodeInRow(categoryName);
            adminPage.openCategoryEditForm(categoryName);
            Assert.assertTrue(adminPage.isCategoryEditPageDisplayed(),
                    "Form sua DM dot 21 phai la TRANG full page (admin-category-edit-page)");
            String modalText = adminPage.getCategoryFormModalText();
            Assert.assertTrue(modalText.contains("Chỉnh sửa danh mục"),
                    "Form sua mo ra phai co tieu de 'Chỉnh sửa danh mục', dang thay: " + modalText);
            Assert.assertTrue(modalText.contains("Chỉnh sửa thông tin danh mục."),
                    "Form sua DM phai co phu de 'Chỉnh sửa thông tin danh mục.', dang thay: " + modalText);
            Assert.assertEquals(adminPage.getCategoryCodeInputValue(), codeInRow,
                    "O Ma trong form sua phai dien san dung ma cua DM");
            Assert.assertTrue(adminPage.isCategoryCodeInputDisabled(),
                    "O Ma trong form sua DM phai bi disable (khong sua duoc ma)");
            Assert.assertEquals(adminPage.getCategoryNameInputValue(), categoryName,
                    "O Ten trong form sua phai dien san dung ten DM");
            Assert.assertEquals(adminPage.getCategorySlugValue(), categoryName.toLowerCase(),
                    "O Slug trong form sua phai dien san dung slug da tu sinh theo ten");
            Assert.assertEquals(adminPage.getCategoryDescriptionValue(), "Mo ta DM test FUNC-024",
                    "O Mo ta trong form sua phai dien san dung mo ta da luu");
            Assert.assertEquals(adminPage.getCategorySortOrderValue(), "7",
                    "O Thu tu sap xep trong form sua phai dien san '7'");
            Assert.assertTrue(adminPage.getCategoryStatusToggleChecked(),
                    "DM moi tao dang Hoat dong nen Toggle trang thai trong form sua phai bat");
            Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                    "Form sua DM phai hien du thumbnail icon da luu");
            java.util.List<String> footerTexts = adminPage.getPageHeadButtonTexts("admin-category-edit-page");
            Assert.assertEquals(footerTexts.size(), 2,
                    "Header trang sua DM phai CHI co dung 2 nut, dang thay: " + footerTexts);
            Assert.assertTrue(footerTexts.contains("Hủy") && footerTexts.contains("Lưu thay đổi"),
                    "Header trang sua DM phai la [Hủy] + [Lưu thay đổi], dang thay: " + footerTexts);

            // Dong bang Hủy, khong luu (form sach nen dong thang, khong hoi).
            adminPage.cancelCategoryForm();
            Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                    "Dong form xong khong duoc con thay form sua danh muc");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-94 (TC_SCRA05_FUNC_033/034/035): Tìm DM theo tên và theo slug — tìm 'Son' thì mọi dòng đều chứa 'son' (tên/slug); tìm 'son-moi' thì mọi dòng đều chứa 'son-moi'; tìm từ không có kết quả ('ZZZKHONGCO999') thì bảng 0 dòng, hiện empty-state 'Không tìm thấy danh mục', phân trang ẩn")
    public void adminCategorySearchFiltersByNameAndSlug() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");

        adminPage.searchCategories("Son");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong co danh muc nao chua 'Son' tren DB nay de kiem tra loc");
        }
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.toLowerCase().contains("son"),
                    "Tim 'Son' thi moi dong phai chua 'son' (ten hoac slug), dang thay: " + row);
        }

        adminPage.searchCategories("son-moi");
        if (adminPage.getAdminTableRowCount() == 0) {
            throw new SkipException("Bo qua: khong co danh muc nao chua 'son-moi' tren DB nay de kiem tra loc theo slug");
        }
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.toLowerCase().contains("son-moi"),
                    "Tim 'son-moi' thi moi dong phai chua 'son-moi' (ten hoac slug), dang thay: " + row);
        }

        adminPage.searchCategories("ZZZKHONGCO999");
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "Tim tu khong co ket qua 'ZZZKHONGCO999' thi bang phai 0 dong");
        Assert.assertTrue(adminPage.isEmptyCategoriesMessageDisplayed(),
                "Tim khong co ket qua thi phai hien empty-state 'Không tìm thấy danh mục'");
        Assert.assertFalse(adminPage.isPaginationDisplayed("categories"),
                "Tim khong co ket qua thi phan trang phai an");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-95 (TC_SCRA05_FUNC_036/037): Lọc DM theo Trạng thái + nút Đặt lại — lọc 'Ngừng hoạt động' thì tất cả Switch đều tắt và DM đã ẩn hiện trong bảng; bấm 'Đặt lại' thì ô tìm kiếm trống, dropdown Trạng thái về '', về trang 1; lọc 'Hoạt động' rồi tìm tên DM đã ẩn thì không thấy dòng nào")
    public void adminCategoryStatusFilterAndReset() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        String setupCategoryName = null;
        try {
            adminPage.clickAdminNav("categories");
            String hiddenName = adminPage.findHiddenCategoryName();
            if (hiddenName == null || hiddenName.isEmpty()) {
                // Setup: tao 1 DM roi an no (DM test, khong co SP).
                setupCategoryName = uniqueCategoryName();
                adminPage.createCategoryFull(setupCategoryName, "", "Mo ta DM an test FUNC-036", "5");
                createdCategoryName = setupCategoryName;
                Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
                adminPage.searchCategories(setupCategoryName);
                adminPage.clickRowStatusSwitch(setupCategoryName);
                adminPage.clickConfirmYes();
                adminPage.waitForFeedback();
                hiddenName = setupCategoryName;
            }
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(hiddenName), Boolean.FALSE,
                    "DM dung de kiem tra loc phai dang Ngung hoat dong (Switch tat) truoc khi loc");

            // Loc 'Ngừng hoạt động' (value 'false'): tat ca Switch tat, DM an hien.
            adminPage.clickToolbarReset("categories");
            adminPage.selectStatusFilterByValue("false");
            adminPage.sleepInSecond(1);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(hiddenName),
                    "Loc 'Ngừng hoạt động' thi DM da an '" + hiddenName + "' phai hien trong bang");
            for (Boolean state : adminPage.getAllRowStatusSwitchStates()) {
                Assert.assertEquals(state, Boolean.FALSE,
                        "Loc 'Ngừng hoạt động' thi moi Switch trong bang deu phai tat");
            }

            // Nut 'Đặt lại': xoa tim kiem + dropdown ve '' + ve trang 1.
            adminPage.searchCategories(hiddenName);
            adminPage.clickToolbarReset("categories");
            adminPage.sleepInSecond(1);
            Assert.assertEquals(adminPage.getCategorySearchValue(), "",
                    "Bam 'Đặt lại' xong o tim kiem DM phai trong");
            Assert.assertEquals(adminPage.getStatusFilterValue(), "",
                    "Bam 'Đặt lại' xong dropdown Trang thai DM phai ve '' (Tất cả)");
            if (adminPage.isPaginationDisplayed("categories")) {
                Assert.assertEquals(adminPage.getPaginationCurrentPage("categories"), 1,
                        "Bam 'Đặt lại' xong phai ve trang 1");
            }

            // Loc 'Hoạt động' (value 'true') + tim ten DM da an: khong thay.
            adminPage.selectStatusFilterByValue("true");
            adminPage.searchCategories(hiddenName);
            Assert.assertFalse(adminPage.isRowWithTextDisplayed(hiddenName),
                    "Loc 'Hoạt động' roi tim ten DM da an thi khong duoc thay dong nao");
        } finally {
            if (setupCategoryName != null) {
                showCategoryIfHidden(setupCategoryName);
                deleteCategoryIfPresent(createdCategoryName);
                createdCategoryName = null;
            }
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-96 (TC_SCRA05_FUNC_038, dot 21): Tìm kiếm TỰ ĐỘNG đưa về trang 1 — tìm 'Son' (phải có > 10 kết quả) rồi sang trang 2, gõ lại từ khóa tìm kiếm (debounce 120ms, KHÔNG còn nút Tìm kiếm) thì về trang 1, ô tìm kiếm vẫn 'Son', các dòng vẫn lọc theo 'Son'; tự bỏ qua khi không đủ dữ liệu phân trang")
    public void adminCategorySearchButtonReturnsToPageOne() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        adminPage.searchCategories("Son");
        if (adminPage.getPaginationTotalRows("categories") <= 10) {
            throw new SkipException("Bo qua: tim 'Son' chi co <= 10 ket qua, khong du phan trang de kiem tra tim kiem ve trang 1");
        }
        adminPage.clickPaginationNext("categories");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("categories"), 2,
                "Sang trang 2 truoc khi go lai tu khoa thi phai dang o trang 2");

        // Dot 21: KHONG con nut Tim kiem de bam — go lai tu khoa thi loc tu dong
        // chay lai (debounce 120ms) va ve trang 1.
        adminPage.searchCategories("Son");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("categories"), 1,
                "Go lai tu khoa tim kiem khi dang o trang 2 thi phai ve trang 1");
        Assert.assertEquals(adminPage.getCategorySearchValue(), "Son",
                "Go lai tu khoa xong o tim kiem van phai la 'Son'");
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.toLowerCase().contains("son"),
                    "Ve trang 1 xong cac dong van phai loc theo 'Son', dang thay: " + row);
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-97 (TC_SCRA05_FUNC_039/040): Phân trang DM — trang 1: STT dòng đầu = 1, nút Trước bị disable; sang trang 2: STT dòng đầu = 11, nút Trước bật; quay lại trang 1: STT dòng đầu lại = 1; tự bỏ qua khi tổng số DM ≤ 10")
    public void adminCategoryPaginationNextPrevAndStt() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        if (adminPage.getPaginationTotalRows("categories") <= 10) {
            throw new SkipException("Bo qua: tong so danh muc <= 10, khong du phan trang");
        }
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Trang 1 thi STT dong dau tien phai la 1");
        Assert.assertFalse(adminPage.isPaginationPrevEnabled("categories"),
                "Trang 1 thi nut 'Trước' phai bi disable");
        Assert.assertTrue(adminPage.isPaginationNextEnabled("categories"),
                "Trang 1 (con trang sau) thi nut 'Sau' phai bat");

        adminPage.clickPaginationNext("categories");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("categories"), 2,
                "Bam 'Sau' thi phai sang trang 2");
        Assert.assertEquals(adminPage.getFirstRowStt(), 11,
                "Trang 2 (10 dong/trang) thi STT dong dau tien phai la 11");
        Assert.assertTrue(adminPage.isPaginationPrevEnabled("categories"),
                "Trang 2 thi nut 'Trước' phai bat");

        adminPage.clickPaginationPrev("categories");
        Assert.assertEquals(adminPage.getPaginationCurrentPage("categories"), 1,
                "Bam 'Trước' tu trang 2 thi phai ve trang 1");
        Assert.assertEquals(adminPage.getFirstRowStt(), 1,
                "Ve trang 1 thi STT dong dau tien phai lai la 1");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-98 (TC_SCRA05_FUNC_041/042/043, dot 21): Gạt Switch DM qua modal xác nhận CHUNG — tạo DM (mặc định Hoạt động): gạt Switch tắt thì popup 'Xác nhận thay đổi trạng thái?' (message '... trạng thái của danh mục <tên> ...'), bấm 'Hủy' thì Switch vẫn bật; gạt lại rồi xác nhận thì toast 'Đổi trạng thái danh mục thành công!', Switch tắt; gạt bật lại thì popup chung, xác nhận thì toast 'Đổi trạng thái danh mục thành công!', Switch bật; xóa DM sau test")
    public void adminCategoryStatusSwitchConfirmFlow() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta DM test Switch FUNC-041", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.TRUE,
                    "DM moi tao phai dang Hoat dong (Switch aria-checked=true)");

            // Gat tat -> hop xac nhan CHUNG -> bam Huy: Switch van bat.
            adminPage.clickRowStatusSwitch(categoryName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch tat DM dot 21 phai mo modal xac nhan CHUNG 'Xác nhận thay đổi trạng thái?', dang thay: " + adminPage.getConfirmModalText());
            Assert.assertTrue(adminPage.getConfirmModalText().contains("danh mục " + categoryName),
                    "Modal chung phai ghi ro loai + ten DM ('... trạng thái của danh mục " + categoryName + " ...'), dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmNo();
            adminPage.sleepInSecond(1);
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.TRUE,
                    "Bam Huy o hop xac nhan chung thi Switch DM van phai bat (khong goi API)");

            // Gat tat -> xac nhan: toast 'Đổi trạng thái danh mục thành công!', Switch tat.
            adminPage.clickRowStatusSwitch(categoryName);
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertTrue(adminPage.getToastText().contains("Đổi trạng thái danh mục thành công!"),
                    "Xac nhan doi trang thai DM thi phai hien toast 'Đổi trạng thái danh mục thành công!', dang thay: " + adminPage.getToastText());
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.FALSE,
                    "Xac nhan doi trang thai DM xong Switch DM phai tat (aria-checked=false)");

            // Gat bat -> hop xac nhan CHUNG -> xac nhan: toast, Switch bat.
            adminPage.clickRowStatusSwitch(categoryName);
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Gat Switch bat lai DM dot 21 phai mo modal xac nhan CHUNG, dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertTrue(adminPage.getToastText().contains("Đổi trạng thái danh mục thành công!"),
                    "Xac nhan doi trang thai DM thi phai hien toast 'Đổi trạng thái danh mục thành công!', dang thay: " + adminPage.getToastText());
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.TRUE,
                    "Xac nhan doi trang thai DM xong Switch DM phai bat lai (aria-checked=true)");
        } finally {
            showCategoryIfHidden(categoryName);
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-99 (TC_SCRA05_FUNC_044): Xóa DM đang Hoạt động bị chặn — tạo DM (mặc định Hoạt động) rồi bấm nút Xóa ở dòng đó thì popup 'Không thể xóa danh mục' (message 'Danh mục **<tên>** đang hoạt động. Vui lòng chuyển trạng thái sang Ngừng hoạt động trước.') với đúng 2 nút [Hủy] + [Chuyển sang Ngừng hoạt động]; bấm 'Hủy' thì popup đóng, DM còn trong bảng, Switch vẫn bật; xóa DM sau test")
    public void adminCategoryDeleteActiveBlockedPopupCancelKeepsRow() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta DM test chan xoa FUNC-044", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);

            adminPage.clickCategoryDeleteButton(categoryName);
            Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                    "Bam Xoa DM dang Hoat dong thi phai hien popup chan xoa 'Không thể xóa danh mục'");
            Assert.assertTrue(adminPage.getDeleteBlockedModalText().contains("Không thể xóa danh mục"),
                    "Popup chan xoa DM phai co tieu de 'Không thể xóa danh mục', dang thay: " + adminPage.getDeleteBlockedModalText());
            Assert.assertTrue(adminPage.getDeleteBlockedModalText().contains("đang hoạt động"),
                    "Popup chan xoa DM phai co message '... đang hoạt động. Vui lòng chuyển trạng thái sang Ngừng hoạt động trước.', dang thay: "
                            + adminPage.getDeleteBlockedModalText());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup chan xoa DM dot 18 phai CHI co dung 2 nut ([Hủy] + [Chuyển sang Ngừng hoạt động])");
            Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Hủy",
                    "Nut trai popup chan xoa DM phai la 'Hủy'");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Chuyển sang Ngừng hoạt động",
                    "Nut phai popup chan xoa DM phai la 'Chuyển sang Ngừng hoạt động'");

            adminPage.clickDeleteBlockedClose();
            adminPage.sleepInSecond(1);
            Assert.assertFalse(adminPage.isDeleteBlockedModalDisplayed(),
                    "Bam 'Hủy' o popup chan xoa xong popup phai dong");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Bam 'Hủy' o popup chan xoa thi DM van phai con trong bang");
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.TRUE,
                    "Bam 'Hủy' o popup chan xoa thi Switch DM van phai bat");
        } finally {
            showCategoryIfHidden(categoryName);
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-100 (TC_SCRA05_FUNC_045, dot 21): Chuyển DM sang Ngừng hoạt động từ popup chặn xóa — tạo DM (mặc định Hoạt động) rồi bấm Xóa: popup 'Không thể xóa danh mục' → bấm 'Chuyển sang Ngừng hoạt động' thì hiện modal xác nhận CHUNG 'Xác nhận thay đổi trạng thái?', xác nhận thì toast 'Đổi trạng thái danh mục thành công!', Switch tắt, DM vẫn còn trong bảng (chưa bị xóa); hiện lại DM rồi xóa sau test")
    public void adminCategoryDeleteBlockedConvertHidesCategory() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta DM test chuyen Ngung FUNC-045", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);

            adminPage.clickCategoryDeleteButton(categoryName);
            Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                    "Bam Xoa DM dang Hoat dong thi phai hien popup chan xoa 'Không thể xóa danh mục'");
            adminPage.clickDeleteBlockedConvert();
            Assert.assertTrue(adminPage.getConfirmModalText().contains("Xác nhận thay đổi trạng thái?"),
                    "Bam 'Chuyển sang Ngừng hoạt động' dot 21 thi phai mo modal xac nhan CHUNG, dang thay: " + adminPage.getConfirmModalText());
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertTrue(adminPage.getToastText().contains("Đổi trạng thái danh mục thành công!"),
                    "Xac nhan doi trang thai DM tu popup chan xoa thi phai hien toast 'Đổi trạng thái danh mục thành công!', dang thay: " + adminPage.getToastText());
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.FALSE,
                    "An DM tu popup chan xoa xong Switch DM phai tat (aria-checked=false)");
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Chuyen sang Ngung hoat dong thi DM van phai con trong bang (chua bi xoa)");
        } finally {
            showCategoryIfHidden(categoryName);
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-101 (TC_SCRA05_FUNC_046/047, dot 21): Xóa DM đã Ngừng hoạt động qua ConfirmDialog — tạo DM rồi ẩn nó (xác nhận modal chung 'Xác nhận thay đổi trạng thái?', Switch tắt); bấm Xóa thì popup 'Xóa danh mục?' (message 'Bạn sắp xóa danh mục **<tên>**. Hành động này không thể hoàn tác.') với đúng 2 nút; bấm 'Hủy' thì DM vẫn còn; bấm Xóa lần nữa rồi xác nhận 'Xóa danh mục' thì toast 'Đã xóa danh mục', DM biến mất khỏi bảng")
    public void adminCategoryDeleteStoppedCategoryConfirmFlow() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.createCategoryFull(categoryName, "", "Mo ta DM test xoa FUNC-046", "5");
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");
            adminPage.searchCategories(categoryName);
            adminPage.clickRowStatusSwitch(categoryName);
            adminPage.clickConfirmYes();
            adminPage.waitForFeedback();
            Assert.assertEquals(adminPage.getRowStatusSwitchChecked(categoryName), Boolean.FALSE,
                    "DM phai dang Ngung hoat dong (Switch tat) truoc khi xoa");

            adminPage.clickCategoryDeleteButton(categoryName);
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Xóa danh mục?",
                    "Bam Xoa DM da Ngung thi phai mo popup 'Xóa danh mục?'");
            Assert.assertTrue(adminPage.getConfirmDialogText().contains("không thể hoàn tác"),
                    "Popup xoa DM phai co message '... Hành động này không thể hoàn tác.', dang thay: " + adminPage.getConfirmDialogText());
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Popup xoa DM dot 18 phai CHI co dung 2 nut ([Hủy] + [Xóa danh mục])");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Xóa danh mục",
                    "Nut phai popup xoa DM phai la 'Xóa danh mục'");
            adminPage.clickConfirmNo();
            adminPage.sleepInSecond(1);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(categoryName),
                    "Bam 'Hủy' o popup xoa thi DM van phai con trong bang");

            adminPage.clickCategoryDeleteButton(categoryName);
            adminPage.clickConfirmDialogConfirm();
            adminPage.waitForFeedback();
            createdCategoryName = null; // da xoa, khong can don dep nua
            Assert.assertTrue(adminPage.getToastText().contains("Đã xóa danh mục"),
                    "Xac nhan xoa DM thi phai hien toast 'Đã xóa danh mục', dang thay: " + adminPage.getToastText());
            adminPage.searchCategories(categoryName);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Xoa DM xong tim ten no phai ra 0 dong");
        } finally {
            if (createdCategoryName != null) {
                showCategoryIfHidden(createdCategoryName);
                deleteCategoryIfPresent(createdCategoryName);
                createdCategoryName = null;
            }
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-102 (TC_SCRA05_FUNC_048/049): Xóa DM đã Ngừng mà còn SP bị chặn dạng 1 nút + link — tìm 1 DM đã Ngừng có SP (tự bỏ qua nếu DB không có), bấm Xóa thì popup 'Không thể xóa danh mục' (message 'Danh mục **<tên>** đang có <n> sản phẩm. Hãy chuyển hoặc xóa các sản phẩm trước.') CHỈ có 1 nút [Đã hiểu] + link 'Xem sản phẩm trong danh mục'; bấm link thì sang mục Sản phẩm, bộ lọc Danh mục đã chọn đúng DM đó, về trang 1, mọi dòng đều thuộc DM đó; quay lại mục Danh mục thì DM vẫn còn; bấm Xóa lần nữa rồi [Đã hiểu] thì popup đóng, DM vẫn còn")
    public void adminCategoryDeleteBlockedWithProductsSingleButtonAndLink() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        String hiddenName = adminPage.findHiddenCategoryWithProductsName();
        if (hiddenName == null || hiddenName.isEmpty()) {
            throw new SkipException("Bo qua: DB nay khong co danh muc nao da Ngung hoat dong ma con san pham de kiem tra popup chan xoa A3");
        }

        adminPage.searchCategories(hiddenName);
        adminPage.clickCategoryDeleteButton(hiddenName);
        Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                "Bam Xoa DM da Ngung ma con SP thi phai hien popup chan xoa 'Không thể xóa danh mục'");
        String modalText = adminPage.getDeleteBlockedModalText();
        Assert.assertTrue(modalText.contains("Không thể xóa danh mục"),
                "Popup chan xoa A3 phai co tieu de 'Không thể xóa danh mục', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("đang có") && modalText.contains("sản phẩm"),
                "Popup chan xoa A3 phai co message 'Danh mục **<tên>** đang có <n> sản phẩm. Hãy chuyển hoặc xóa các sản phẩm trước.', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isConfirmDialogSingleButton(),
                "Popup chan xoa A3 dot 18 CHI co dung 1 nut");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Đã hiểu",
                "Nut duy nhat cua popup chan xoa A3 phai la 'Đã hiểu'");
        Assert.assertTrue(adminPage.isConfirmDialogLinkDisplayed(),
                "Popup chan xoa A3 phai co link 'Xem sản phẩm trong danh mục'");
        Assert.assertEquals(adminPage.getConfirmDialogLinkText(), "Xem sản phẩm trong danh mục",
                "Link trong popup chan xoa A3 phai la 'Xem sản phẩm trong danh mục'");

        // Bam link: sang muc San pham, bo loc Danh muc da chon dung DM do.
        adminPage.clickConfirmDialogLink();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductCategoryFilterSelectedText(), hiddenName,
                "Bam link trong popup chan xoa A3 thi sang muc San pham voi bo loc Danh muc da chon dung '" + hiddenName + "'");
        if (adminPage.isPaginationDisplayed("products")) {
            Assert.assertEquals(adminPage.getPaginationCurrentPage("products"), 1,
                    "Bam link trong popup chan xoa A3 thi phai ve trang 1 o muc San pham");
        }
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Bam link trong popup chan xoa A3 thi bang San pham phai co dong (SP cua DM do)");
        for (String row : adminPage.getAdminTableRowTexts()) {
            Assert.assertTrue(row.contains(hiddenName),
                    "Bam link trong popup chan xoa A3 thi moi dong SP phai thuoc DM '" + hiddenName + "', dang thay: " + row);
        }

        // Quay lai muc Danh muc: DM van con; bam Xoa lan nua roi [Đã hiểu].
        adminPage.clickAdminNav("categories");
        adminPage.searchCategories(hiddenName);
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(hiddenName),
                "Quay lai muc Danh muc thi DM bi chan xoa van phai con trong bang");
        adminPage.clickCategoryDeleteButton(hiddenName);
        Assert.assertTrue(adminPage.isDeleteBlockedModalDisplayed(),
                "Bam Xoa lan 2 thi popup chan xoa A3 phai hien lai");
        adminPage.clickConfirmDialogConfirm();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isDeleteBlockedModalDisplayed(),
                "Bam [Đã hiểu] o popup chan xoa A3 thi popup phai dong");
        Assert.assertTrue(adminPage.isRowWithTextDisplayed(hiddenName),
                "Bam [Đã hiểu] o popup chan xoa A3 thi DM van phai con trong bang");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-103 (TC_SCRA05_FUNC_052): Xóa thumbnail icon khỏi form sửa DM — tạo DM có icon qua dán link, mở form sửa, bấm nút Xóa ảnh (admin-category-image-remove-btn) thì thumbnail biến mất; bấm Lưu thì icon bị xóa thật, mở lại form sửa không còn thumbnail; xóa DM sau test")
    public void adminCategoryIconRemoveThumbnailClearsIcon() {
        String categoryName = uniqueCategoryName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("categories");
            adminPage.openCategoryForm();
            adminPage.setCategoryName(categoryName);
            adminPage.addCategoryImageByUrl("https://example.com/dm-052.jpg");
            Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                    "Them icon bang link xong phai thay thumbnail icon trong form");
            adminPage.clickCategorySaveButton();
            adminPage.waitForFeedback();
            createdCategoryName = categoryName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Tao danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.openCategoryEditForm(categoryName);
            Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                    "Mo form sua DM co icon thi phai thay thumbnail icon");
            adminPage.removeCategoryImage();
            Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB),
                    "Bam nut Xoa anh trong form sua thi thumbnail icon phai bien mat ngay");
            adminPage.clickCategorySaveButton();
            adminPage.waitForFeedback();
            Assert.assertTrue(adminPage.isToastDisplayed(), "Luu sua danh muc xong phai hien toast thanh cong (admin-toast)");

            adminPage.openCategoryEditForm(categoryName);
            Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB),
                    "Luu xong mo lai form sua thi DM khong duoc con thumbnail icon nao");
            adminPage.cancelCategoryForm();
            Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                    "Dong form xong khong duoc con thay form sua danh muc");
        } finally {
            deleteCategoryIfPresent(createdCategoryName);
            createdCategoryName = null;
        }
    }
}
