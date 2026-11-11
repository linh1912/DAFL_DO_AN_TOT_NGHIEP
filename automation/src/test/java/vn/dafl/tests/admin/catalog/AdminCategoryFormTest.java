package vn.dafl.tests.admin.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test form them/sua danh muc o Admin rieng localhost:3001 (wave 19, lop MOI
 * cho gap SCR_05): validate + hanh vi form TRANG FULL PAGE dot 21
 * (/categories/new, /categories/:code/edit — KHONG con Drawer 680px).
 * Toan bo test trong lop nay CHI assert loi roi dong form, KHONG bam Luu thanh
 * cong nen KHONG tao/sua/xoa du lieu that — tru test mo form sua tren DM co san
 * (chi doc + dong, khong luu) va test upload icon (chi chon file, khong luu).
 * Can cu code that dot 18 (AdminForms.tsx: CategoryForm.validateAll):
 *  - Ten 2-150 ky tu; slug chi gom chu thuong/so/gach ngang, tu sinh tu ten
 *    cho toi khi sua tay; khi sua thi slug chi doc (readOnly) va khong doi.
 *  - Ma de trong thi tu sinh DM-xxxx; ma nhap tay phai 3-20 ky tu, chi chu in
 *    hoa/so/gach ngang (go thuong tu viet hoa).
 *  - Loi BE (mo ta > 1000 ky tu) chi hien dang formError trong form, KHONG kem
 *    toast (handleSubmit chi setFormError).
 *  - Nut 'Lưu thay đổi' o form sua bi disable khi chua sua gi (dirty=false).
 *  - Upload icon chi 1 anh: chon 2 file cung luc bao loi 'Chỉ chọn 1 ảnh cho
 *    danh mục.' — Selenium khong gui duoc 2 file cung luc vao input khong co
 *    thuoc tinh multiple nen case nay KHONG viet o day (xem note_W2.md).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý danh mục")
public class AdminCategoryFormTest extends BaseTest {

    private static final String IMAGE_1 = "upload/test-product-1.png";
    private static final String INVALID_FILE = "upload/invalid-file.txt";
    /** Gioi han dung luong anh FE chap nhan (theo hop dong dot redesign). */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /** Dang nhap Super Admin tren app rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo form them danh muc moi (da dang nhap + vao muc Danh muc). */
    private AdminPageObject openCreateForm() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        adminPage.openCategoryForm();
        return adminPage;
    }

    /** Duong dan tuyet doi cua file trong automation/src/test/resources/upload/. */
    private String getTestResourcePath(String resourceName) {
        URL url = getClass().getClassLoader().getResource(resourceName);
        Assert.assertNotNull(url, "Khong tim thay file test trong resources: " + resourceName);
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new AssertionError("Khong doc duoc duong dan file test " + resourceName + ": " + e.getMessage());
        }
    }

    /** Tao file anh vuot gioi han 5MB luc chay test: anh PNG hop le + padding 0
     *  toi 5MB + 1 byte (chi dung de kiem tra FE tu choi file qua lon). */
    private String createOversizeImagePath() {
        try {
            byte[] pngBytes = Files.readAllBytes(Paths.get(getTestResourcePath(IMAGE_1)));
            Path oversize = Paths.get(System.getProperty("java.io.tmpdir"), "dafl-oversize-cat-test.png");
            byte[] padded = new byte[(int) (MAX_IMAGE_BYTES + 1)];
            System.arraycopy(pngBytes, 0, padded, 0, pngBytes.length);
            Files.write(oversize, padded);
            return oversize.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new AssertionError("Khong tao duoc file anh > 5MB de test: " + e.getMessage());
        }
    }

    @Story("Danh mục")
    @Test(description = "ADM19-66 (TC_SCRA05_UI_003): Form Thêm DM có đủ 2 nhóm Thông tin cơ bản + Hình ảnh — Mã DM placeholder 'Để trống để tự sinh (DM-xxxx)', Tên bắt buộc, Slug, Mô tả, Thứ tự sắp xếp, Toggle trạng thái 'Hiển thị'/'Đang ẩn', ImageUploader 1 ảnh duy nhất + nút 'Hoặc dán link ảnh'; footer [Hủy][Lưu] khi tạo mới; đóng không lưu")
    public void adminCategoryFormHasAllFields() {
        AdminPageObject adminPage = openCreateForm();
        String modalText = adminPage.getCategoryFormModalText();
        Assert.assertTrue(modalText.contains("Thông tin cơ bản"),
                "Form them DM phai co nhom 'Thông tin cơ bản', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Hình ảnh"),
                "Form them DM phai co nhom 'Hình ảnh', dang thay: " + modalText);
        Assert.assertEquals(adminPage.getElementAttribute(AdminPageUI.ADMIN_CATEGORY_CODE_INPUT, "placeholder"),
                "Để trống để tự sinh (DM-xxxx)",
                "O Ma DM phai co placeholder 'Để trống để tự sinh (DM-xxxx)'");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT),
                "Form them DM phai co o Ten danh muc");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_SLUG_INPUT),
                "Form them DM phai co o Slug");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT),
                "Form them DM phai co o Mo ta");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_SORT_ORDER_INPUT),
                "Form them DM phai co o Thu tu sap xep");
        Assert.assertTrue(adminPage.isCategoryStatusToggleDisplayed(),
                "Form them DM phai co Toggle trang thai (admin-category-status-toggle)");
        Assert.assertTrue(modalText.contains("Hiển thị"),
                "Toggle trang thai DM mac dinh bat phai hien nhan 'Hiển thị', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isCategoryImageDropzoneDisplayed(),
                "Form them DM phai co dropzone upload icon (admin-category-image-dropzone, chi 1 anh)");
        Assert.assertFalse(adminPage.getCategoryFormModalText().contains("Hoặc dán link ảnh"),
                "Dot 26 da bo dan link anh — form them DM KHONG duoc con nut 'Hoặc dán link ảnh'");
        Assert.assertTrue(adminPage.isCategoryFormBackBtnDisplayed(),
                "Header sticky form them DM phai co nut '← Quay lại' (admin-category-form-back-btn)");
        java.util.List<String> footerTexts = adminPage.getPageHeadButtonTexts("admin-category-create-page");
        Assert.assertEquals(footerTexts.size(), 3,
                "Header trang tao DM dot 24/25 phai co dung 3 nut, dang thay: " + footerTexts);
        Assert.assertTrue(footerTexts.contains("Quay lại") && footerTexts.contains("Hủy") && footerTexts.contains("Lưu"),
                "Header trang tao DM phai la [Quay lại] + [Hủy] + [Lưu], dang thay: " + footerTexts);

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-67 (TC_SCRA05_UI_004/005): Slug tự sinh từ tên (bỏ dấu tiếng Việt, nối gạch ngang) — nhập Tên 'Son Moi Moi' thì ô Slug tự điền 'son-moi-moi'; sửa tay Slug thành 'son-moi-rieng' rồi đổi Tên thành 'Son Moi Khac' thì Slug VẪN 'son-moi-rieng' (không tự sinh lại); đóng form không lưu")
    public void adminCategorySlugAutoGeneratesAndStopsAfterManualEdit() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName("Son Moi Moi");
        adminPage.sleepInSecond(1); // cho FE tu sinh slug tu ten
        Assert.assertEquals(adminPage.getCategorySlugValue(), "son-moi-moi",
                "Nhap Ten 'Son Moi Moi' thi o Slug phai tu dien 'son-moi-moi'");

        adminPage.setCategorySlug("son-moi-rieng");
        adminPage.setCategoryName("Son Moi Khac");
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategorySlugValue(), "son-moi-rieng",
                "Sua tay Slug roi doi Ten thi Slug phai GIU 'son-moi-rieng', khong tu sinh lai");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-68 (TC_SCRA05_UI_006, đợt 26 viết lại — dán link ảnh ĐÃ BỎ, chỉ còn upload file): Upload icon DM bằng file từ máy — chọn file ảnh qua dropzone thì hiện 1 thumbnail duy nhất (admin-category-image-thumb); form KHÔNG còn nút 'Hoặc dán link ảnh'; đóng form không lưu")
    public void adminCategoryIconPasteLinkShowsThumbnail() {
        AdminPageObject adminPage = openCreateForm();
        Assert.assertFalse(adminPage.getCategoryFormModalText().contains("Hoặc dán link ảnh"),
                "Dot 26 da bo dan link anh — form them DM KHONG duoc con nut 'Hoặc dán link ảnh'");

        adminPage.uploadCategoryImage(getTestResourcePath(IMAGE_1));
        Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                "Chon 1 file anh qua dropzone DM xong phai hien 1 thumbnail icon duy nhat");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-69 (TC_SCRA05_VAL_007): Bỏ trống ô Tên danh mục — để trống Tên rồi bấm Lưu thì hiện lỗi 'Vui lòng bổ sung thông tin bắt buộc.' dưới ô Tên, ô Tên viền đỏ; không gọi POST, form vẫn mở; đóng bằng Hủy, không lưu")
    public void adminCategoryNameRequiredError() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("name"), "Vui lòng bổ sung thông tin bắt buộc.",
                "De trong o Ten danh muc thi phai bao loi bat buoc duoi o Ten");
        String nameClass = adminPage.getElementAttribute(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT, "class");
        Assert.assertTrue(nameClass != null && nameClass.contains("sa-input-error"),
                "O Ten bao loi bat buoc thi phai co viền đỏ (class sa-input-error), dang thay: " + nameClass);
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Luu that bai do thieu ten thi form phai van mo (khong dong, khong tao DM)");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-70 (TC_SCRA05_VAL_008/009): Tên danh mục chỉ 1 ký tự ('S') và dài hơn 150 ký tự (151 ký tự) đều hiện lỗi 'Tên danh mục phải có từ 2 đến 150 ký tự.' dưới ô Tên; không gọi POST; đóng bằng Hủy, không lưu")
    public void adminCategoryNameLengthValidation() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName("S");
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("name"), "Tên danh mục phải có từ 2 đến 150 ký tự.",
                "Ten 'S' (1 ky tu) phai bao loi do dai duoi o Ten");
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Luu that bai do ten qua ngan thi form phai van mo (khong dong, khong tao DM)");

        adminPage.setCategoryName("A".repeat(151));
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("name"), "Tên danh mục phải có từ 2 đến 150 ký tự.",
                "Ten 151 ky tu phai bao loi do dai duoi o Ten");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-71 (TC_SCRA05_VAL_010/011/012/013): Mã DM sai định dạng — mã 'ab' (quá ngắn), 'DM_01' (gạch dưới), mã 21 ký tự đều hiện lỗi 'Mã danh mục phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.' dưới ô Mã; gõ 'dm-001' thì ô hiện 'DM-001' (tự viết hoa) và không báo lỗi định dạng; đóng bằng Hủy, không lưu")
    public void adminCategoryCodeFormatValidation() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName("Danh muc test ma");

        adminPage.setCategoryCode("ab");
        Assert.assertEquals(adminPage.getCategoryCodeInputValue(), "AB",
                "Go 'ab' vao o Ma DM thi o phai hien 'AB' (tu viet hoa ngay khi go)");
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("code"),
                "Mã danh mục phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 'ab' (qua ngan) phai bao loi dinh dang duoi o Ma");

        adminPage.setCategoryCode("DM_01");
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("code"),
                "Mã danh mục phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 'DM_01' (chua gach duoi) phai bao loi dinh dang duoi o Ma");

        adminPage.setCategoryCode("ABCDEFGHIJ12345678901");
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("code"),
                "Mã danh mục phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 21 ky tu phai bao loi dinh dang duoi o Ma");
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Luu that bai do ma sai dinh dang thi form phai van mo (khong dong, khong tao DM)");

        // Ma go chu thuong tu viet hoa thanh chu in hoa va hop le dinh dang:
        // chi kiem tra bang validate khi blur (khong bam Luu de khoi tao DM that).
        adminPage.setCategoryCode("dm-001");
        Assert.assertEquals(adminPage.getCategoryCodeInputValue(), "DM-001",
                "Go 'dm-001' vao o Ma DM thi o phai hien 'DM-001' (tu viet hoa ngay khi go)");
        adminPage.clickToElement(AdminPageUI.ADMIN_CATEGORY_NAME_INPUT);
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("code"), "",
                "Ma 'DM-001' hop le dinh dang nen khong duoc bao loi duoi o Ma");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-72 (TC_SCRA05_VAL_015): Slug chứa ký tự không cho phép — sửa tay Slug thành 'Son Moi!' rồi bấm Lưu thì hiện lỗi 'Slug chỉ gồm chữ thường, số và gạch ngang.' dưới ô Slug; không gọi POST; đóng bằng Hủy, không lưu")
    public void adminCategorySlugFormatValidation() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName("Danh muc test slug");
        adminPage.setCategorySlug("Son Moi!");
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategoryFieldErrorText("slug"), "Slug chỉ gồm chữ thường, số và gạch ngang.",
                "Slug 'Son Moi!' (chu hoa/khoang trang/dau cham than) phai bao loi dinh dang duoi o Slug");
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Luu that bai do slug sai dinh dang thi form phai van mo (khong dong, khong tao DM)");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-73 (TC_SCRA05_VAL_018): Mô tả DM quá dài (1001 ký tự) thì BE từ chối — FE không chặn, bấm Lưu thì BE trả lỗi 'Mô tả tối đa 1000 ký tự.' dạng formError trong form, form vẫn mở, không tạo DM; đóng bằng Hủy")
    public void adminCategoryDescriptionOver1000RejectedByBackend() {
        String categoryName = "TESTDM-" + System.currentTimeMillis();
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName(categoryName);
        // Nhap mo ta 1001 ky tu bang JS (go tung ky tu qua sendKeys qua cham).
        adminPage.setInputValueByJs(AdminPageUI.ADMIN_CATEGORY_DESCRIPTION_INPUT, "M".repeat(1001));
        adminPage.sleepInSecond(1);
        adminPage.clickCategorySaveButton();
        adminPage.sleepInSecond(2); // cho POST /api/admin/categories tra ve
        Assert.assertTrue(adminPage.getCategoryFormErrorText().contains("Mô tả tối đa 1000 ký tự."),
                "Mo ta 1001 ky tu thi BE phai tra loi 'Mô tả tối đa 1000 ký tự.' dang formError trong form, dang thay: "
                        + adminPage.getCategoryFormErrorText());
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Luu that bai do mo ta qua dai thi form phai van mo (khong dong, khong tao DM)");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
        adminPage.searchCategories(categoryName);
        Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                "DM mo ta qua dai bi BE tu choi nen tim ten no phai ra 0 dong");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-74 (TC_SCRA05_VAL_019): Ô Thứ tự sắp xếp chỉ nhận chữ số — gõ '5a' vào ô Thứ tự sắp xếp thì ô chỉ giữ '5'; có ghi chú dưới ô 'Số càng nhỏ càng hiện trước.'; đóng form không lưu")
    public void adminCategorySortOrderDigitsOnly() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategorySortOrder("5a");
        Assert.assertEquals(adminPage.getCategorySortOrderValue(), "5",
                "Go '5a' vao o Thu tu sap xep thi o chi giu '5' (ky tu khong phai so bi loai ngay khi go)");
        Assert.assertTrue(adminPage.getCategoryFormModalText().contains("Số càng nhỏ càng hiện trước."),
                "Duoi o Thu tu sap xep phai co ghi chu 'Số càng nhỏ càng hiện trước.'");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-75 (TC_SCRA05_FUNC_025): Đổi tên DM không sinh lại slug (slug giữ nguyên khi sửa) — mở form sửa 1 DM, đổi tên (không đụng ô Slug) thì ô Slug VẪN giá trị cũ; có ghi chú 'Slug không thay đổi khi sửa.'; đóng form không lưu")
    public void adminCategoryEditNameKeepsSlug() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        String categoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (categoryName == null || categoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo form sua");
        }

        adminPage.openCategoryEditForm(categoryName);
        String slugBefore = adminPage.getCategorySlugValue();
        adminPage.setCategoryName(categoryName + " Cao Cap");
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getCategorySlugValue(), slugBefore,
                "Doi ten DM khi sua thi o Slug phai GIU '" + slugBefore + "', khong tu sinh lai");
        Assert.assertTrue(adminPage.getCategoryFormModalText().contains("Slug không thay đổi khi sửa."),
                "Form sua DM phai co ghi chu 'Slug không thay đổi khi sửa.'");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form sua danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-76 (TC_SCRA05_FUNC_028/030, dot 21): Bấm Hủy khi chưa sửa thì đóng trang ngay, không hỏi — mở trang Thêm DM chưa nhập gì rồi bấm Hủy (lặp lại với phím ESC) thì trang đóng ngay, về bảng DM, không hiện hộp 'chưa lưu', không gọi POST/PUT, bảng giữ nguyên (trang full page KHÔNG có nút X như Drawer cũ)")
    public void adminCategoryCancelCleanFormClosesImmediately() {
        AdminPageObject adminPage = openCreateForm();
        // (1) Nut 'Hủy' o header.
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Bam Hủy khi form chua sua thi trang phai dong ngay");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam Hủy khi form chua sua thi khong duoc hien hop xac nhan");

        // (2) Phim ESC.
        adminPage.openCategoryForm();
        adminPage.pressEscapeKey();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Bam ESC khi form chua sua thi trang phai dong ngay, khong hoi");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam ESC khi form chua sua thi khong duoc hien hop xac nhan");
        Assert.assertTrue(adminPage.getAdminTableRowCount() > 0,
                "Dong form chua sua xong phai quay lai bang danh muc co du lieu");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-77 (TC_SCRA05_FUNC_029, đợt 24/25 viết lại — dialog 'Thoát mà chưa lưu?' -> 'Hủy các thay đổi?', nút 'Thoát' -> 'Rời trang'): Đã sửa mà bấm Hủy thì hỏi 'Hủy các thay đổi?' — nhập tên 'DM mới' rồi bấm Hủy thì hiện hộp 'Hủy các thay đổi?' / 'Các thay đổi bạn vừa nhập sẽ bị mất.' với 2 nút 'Ở lại'/'Rời trang'; bấm 'Ở lại' thì hộp đóng, trang form vẫn mở, dữ liệu 'DM mới' còn nguyên; bấm Hủy rồi 'Rời trang' thì trang đóng, không lưu, không gọi POST")
    public void adminCategoryDirtyFormCancelAsksConfirm() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setCategoryName("DM mới");
        adminPage.clickToElement(AdminPageUI.ADMIN_CATEGORY_CANCEL_BTN);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam Hủy khi form da sua thi phai hien hop 'Hủy các thay đổi?'");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Hủy các thay đổi?",
                "Hop dong form dirty phai co tieu de 'Hủy các thay đổi?'");
        Assert.assertTrue(adminPage.getConfirmDialogText().contains("Các thay đổi bạn vừa nhập sẽ bị mất."),
                "Hop dong form dirty phai co noi dung 'Các thay đổi bạn vừa nhập sẽ bị mất.'");
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Hop dong form dirty dot 18 phai CHI co dung 2 nut ([Ở lại] + [Rời trang])");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Ở lại",
                "Nut trai hop dong form dirty phai la 'Ở lại'");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Rời trang",
                "Nut phai hop dong form dirty phai la 'Rời trang'");

        // Nhanh 'Ở lại': hop dong, trang form van mo, du lieu con nguyen.
        adminPage.clickConfirmNo();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam 'Ở lại' xong hop xac nhan phai bien mat");
        Assert.assertTrue(adminPage.isCategoryFormModalDisplayed(),
                "Bam 'Ở lại' xong trang form danh muc van phai mo");
        Assert.assertEquals(adminPage.getCategoryNameInputValue(), "DM mới",
                "Bam 'Ở lại' xong du lieu 'DM mới' trong form phai con nguyen");

        // Nhanh 'Rời trang': trang dong, khong luu, khong tao danh muc.
        adminPage.clickToElement(AdminPageUI.ADMIN_CATEGORY_CANCEL_BTN);
        adminPage.clickConfirmDialogConfirm();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Bam Hủy roi 'Rời trang' xong trang form danh muc phai dong");
        adminPage.searchCategories("DM mới");
        Assert.assertFalse(adminPage.isRowWithTextDisplayed("DM mới"),
                "Thoat form chua luu thi khong duoc tao danh muc 'DM mới'");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-78 (TC_SCRA05_FUNC_050): Upload icon DM bằng file từ máy tính (tối đa 1 ảnh) — mở form Thêm DM, chọn 1 file ảnh qua dropzone (hint 'PNG, JPG, WEBP - tối đa 5MB', không có 'tối đa 8 ảnh' như form SP) thì preview ngay + hiện 1 thumbnail duy nhất; đóng form không lưu")
    public void adminCategoryIconFileUploadShowsThumbnail() {
        AdminPageObject adminPage = openCreateForm();
        String dropzoneText = adminPage.getCategoryImageDropzoneText();
        Assert.assertTrue(dropzoneText.contains("PNG, JPG, WEBP – tối đa 5MB"),
                "Dropzone icon DM phai co hint 'PNG, JPG, WEBP – tối đa 5MB', dang thay: " + dropzoneText);
        Assert.assertFalse(dropzoneText.contains("tối đa 8 ảnh"),
                "Dropzone icon DM chi nhan 1 anh nen KHONG duoc ghi 'tối đa 8 ảnh' nhu form SP, dang thay: " + dropzoneText);

        adminPage.uploadCategoryImage(getTestResourcePath(IMAGE_1));
        Assert.assertTrue(adminPage.isCategoryImageThumbDisplayed(),
                "Chon 1 file anh qua dropzone DM xong phai hien 1 thumbnail icon duy nhat");

        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-79 (TC_SCRA05_FUNC_053/054, đợt 26 viết lại — dán link ảnh ĐÃ BỎ nên không còn ca ftp link): Upload icon sai định dạng/quá 5MB → từ chối inline — chọn file .txt thì lỗi 'Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).'; chọn ảnh > 5MB thì lỗi 'Ảnh vượt quá 5MB.'; cả 2 ca không tạo thumbnail ready, kèm toast đúng loại; form KHÔNG còn nút dán link ảnh; đóng form không lưu")
    public void adminCategoryIconInvalidFileRejectedInline() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");

        // (1) File sai dinh dang: .txt.
        adminPage.openCategoryForm();
        adminPage.uploadCategoryImage(getTestResourcePath(INVALID_FILE));
        java.util.List<String> toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Định dạng ảnh không hỗ trợ")),
                "Upload file .txt lam icon DM thi phai hien toast 'Định dạng ảnh không hỗ trợ', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getCategoryImageThumbErrorText(),
                "Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).",
                "Upload file .txt lam icon DM thi thumbnail loi phai hien 'Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).'");
        Assert.assertTrue(adminPage.isCategoryImageRetryButtonDisplayed(),
                "Thumbnail icon loi phai co nut 'Thử lại'");
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Bam Hủy xong form danh muc phai dong (khong luu icon loi)");

        // (2) File anh vuot 5MB (tao luc chay test, khong commit file lon vao repo).
        String oversizePath = createOversizeImagePath();
        Assert.assertTrue(new File(oversizePath).length() > MAX_IMAGE_BYTES,
                "File test vuot gioi han phai lon hon 5MB, dang la: " + new File(oversizePath).length() + " byte");
        adminPage.openCategoryForm();
        adminPage.uploadCategoryImage(oversizePath);
        toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Ảnh quá lớn")),
                "Upload anh > 5MB lam icon DM thi phai hien toast 'Ảnh quá lớn', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getCategoryImageThumbErrorText(), "Ảnh vượt quá 5MB.",
                "Upload anh > 5MB lam icon DM thi thumbnail loi phai hien 'Ảnh vượt quá 5MB.'");
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Bam Hủy xong form danh muc phai dong (khong luu icon loi)");

        // (3) Dot 26: dan link anh DA BO — form khong con nut 'Hoặc dán link ảnh'.
        adminPage.openCategoryForm();
        Assert.assertFalse(adminPage.getCategoryFormModalText().contains("Hoặc dán link ảnh"),
                "Dot 26 da bo dan link anh — form them DM KHONG duoc con nut 'Hoặc dán link ảnh'");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.ADMIN_CATEGORY_IMAGE_THUMB),
                "Chua chon file thi khong duoc co thumbnail icon nao");
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }

    @Story("Danh mục")
    @Test(description = "ADM19-80 (TC_SCRA05_FUNC_055): Nút 'Lưu thay đổi' bị disable khi mở form sửa mà chưa sửa gì — mở form sửa 1 DM thì nút 'Lưu thay đổi' bị disable; sửa ô Tên thì nút bật; trả Tên về giá trị ban đầu thì nút lại disable; nút 'Lưu' ở form TẠO mới không bị disable kiểu này; đóng form không lưu")
    public void adminCategoryEditSaveDisabledUntilDirty() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("categories");
        String categoryName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_CATEGORY_DETAIL_BTN, "Xem chi tiết danh mục ");
        if (categoryName == null || categoryName.isEmpty()) {
            throw new SkipException("Bo qua: muc Danh muc khong co dong nao de mo form sua");
        }

        adminPage.openCategoryEditForm(categoryName);
        Assert.assertFalse(adminPage.isElementEnabled(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN),
                "Mo form sua DM ma chua sua gi thi nut 'Lưu thay đổi' phai bi disable");
        adminPage.setCategoryName(categoryName + "-SUA");
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN),
                "Doi ten DM trong form sua thi nut 'Lưu thay đổi' phai bat len");
        adminPage.setCategoryName(categoryName);
        Assert.assertFalse(adminPage.isElementEnabled(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN),
                "Tra ten DM ve dung gia tri ban dau thi nut 'Lưu thay đổi' phai lai disable");

        // Form sach nen bam Hủy dong thang, khong hoi dirty-close.
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form sua danh muc");

        // Form TAO moi: nut 'Lưu' khong bi disable khi chua sua gi.
        adminPage.openCategoryForm();
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_CATEGORY_SAVE_BTN),
                "Mo form tao DM moi thi nut 'Lưu' phai bat duoc (khong ap luat disable khi chua dirty nhu form sua)");
        adminPage.cancelCategoryForm();
        Assert.assertFalse(adminPage.isCategoryFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form danh muc");
    }
}
