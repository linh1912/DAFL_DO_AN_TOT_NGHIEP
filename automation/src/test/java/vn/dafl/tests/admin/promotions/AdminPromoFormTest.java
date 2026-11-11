package vn.dafl.tests.admin.promotions;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test form tao/sua voucher (validation + hanh vi form) tren app Admin rieng localhost:3001
 * (wave 19, mapping SCR_06 — tach khoi AdminPromoTest de lop kia chi lo bang/loc/tac vu tren dong).
 * Test validation chi assert loi roi dong form, KHONG luu (khong goi POST/PUT); test mo form sua
 * chi dung voucher Draft do chinh test tao (ma kem timestamp) va tu xoa sau khi chay.
 * Quy tac 'chi sua/xoa voucher Draft' la [CẦN XÁC NHẬN] (Matthew chua xac nhan chinh thuc) —
 * cac test viet theo code hien tai, ghi note trong description.
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý khuyến mãi — form tạo/sửa voucher")
public class AdminPromoFormTest extends BaseTest {

    /** Dang nhap Super Admin tren app Admin rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Ma voucher duy nhat theo tung lan chay (chu + so, hop le 3-50 ky tu). */
    private String uniquePromoCode() {
        return "TEST" + System.currentTimeMillis();
    }

    /** Mo /admin va dung o muc Khuyen mai; tra ve AdminPageObject da san sang. */
    private AdminPageObject openPromosOrSkip() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.waitForAdminGateResolved();
        Assert.assertTrue(adminPage.isAdminMenuDisplayed(), "Super Admin phai thay menu quan tri");
        adminPage.clickAdminNav("promotions");
        return adminPage;
    }

    /** Tao 1 voucher Draft de dung cho test form sua; tra ve ma voucher. */
    private String createDraftPromoForEdit(AdminPageObject adminPage, String code) {
        adminPage.createPromoFull(code, "Voucher test form sua", "Percentage", "10",
                "0", "", "", "2027-12-31");
        Assert.assertTrue(adminPage.isToastDisplayed(), "Tao voucher Draft xong phai hien toast thanh cong (admin-toast)");
        return code;
    }

    /** Xoa voucher Draft do test tao (best-effort, khong lam fail test chinh). */
    private void deleteDraftPromoQuietly(AdminPageObject adminPage, String code) {
        try {
            adminPage.searchPromos(code);
            if (adminPage.isRowWithTextDisplayed(code)) {
                adminPage.deletePromoByCode(code);
            }
        } catch (Throwable ignored) {
            // Don dep best-effort.
        }
    }

    // ---------- Form tao voucher: du truong + hanh vi (SCR_06 UI_005/006, FUNC_015/016/017) ----------

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-175 (TC_SCRA06_UI_005 + TC_SCRA06_UI_006, đợt 21): TRANG tạo voucher full page (/promotions/new) có đủ trường 4 section: Thông tin cơ bản (Mã* KHÔNG còn nút 'Tạo mã ngẫu nhiên', Mô tả); Giá trị giảm (Loại giảm 3 RADIO CARD Cố định/Phần trăm/Miễn phí ship, Giá trị giảm* + hậu tố '%' + ghi chú khi loại %, Đơn tối thiểu, Giảm tối đa (chỉ khi %), Số lượt dùng tối đa); Thời gian & phạm vi (Ngày bắt đầu kiểu datetime-local, Ngày hết hạn kiểu date KHÔNG còn ghi chú 'Định dạng dd/mm/yyyy.', Lượt dùng/người mặc định 1, Hạng tối thiểu, Phạm vi áp dụng); Trạng thái (ghi chú 'Nháp — khuyến mãi mới được tạo ở trạng thái Nháp.', KHÔNG toggle 'Kích hoạt ngay sau khi tạo'); form KHÔNG có mục upload ảnh; Hủy (form sạch) về danh sách")
    public void adminPromoCreateFormHasAllFields() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();

        Assert.assertTrue(adminPage.isPromoCreatePageDisplayed(),
                "Bam '+ Thêm mới' phai mo trang tao khuyen mai full page (admin-promo-create-page)");
        String pageText = adminPage.getPromoCreatePageText();
        Assert.assertTrue(pageText.contains("Thêm mới khuyến mãi"),
                "Trang tao khuyen mai phai co tieu de 'Thêm mới khuyến mãi', dang thay: " + pageText);
        Assert.assertTrue(pageText.contains("Thông tin cơ bản") && pageText.contains("Giá trị giảm")
                        && pageText.contains("Thời gian & phạm vi") && pageText.contains("Trạng thái"),
                "Trang tao khuyen mai phai co du 4 section: Thông tin cơ bản / Giá trị giảm / Thời gian & phạm vi / Trạng thái, dang thay: " + pageText);
        Assert.assertTrue(adminPage.isPromoCodeInputDisplayed(),
                "Form tao voucher phai co o Ma (admin-promo-code-input)");
        Assert.assertFalse(adminPage.isPromoGenerateCodeButtonDisplayed(),
                "Trang tao khuyen mai dot 21 KHONG duoc co nut 'Tạo mã ngẫu nhiên'");
        Assert.assertTrue(adminPage.getPromoFormText().contains("Mô tả"),
                "Form tao voucher phai co truong Mo ta");
        Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisplayed("Fixed"),
                "Form tao voucher phai co radio card loai giam 'Cố định'");
        Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisplayed("Percentage"),
                "Form tao voucher phai co radio card loai giam 'Phần trăm'");
        Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisplayed("FreeShip"),
                "Form tao voucher phai co radio card loai giam 'Miễn phí ship'");
        Assert.assertTrue(adminPage.isPromoDiscountValueInputDisplayed(),
                "Form tao voucher phai co o Gia tri giam (admin-promo-value-input)");
        Assert.assertTrue(adminPage.isPromoMinOrderInputDisplayed(),
                "Form tao voucher phai co o Don toi thieu (admin-promo-min-order-input)");
        Assert.assertTrue(adminPage.isPromoMaxDiscountInputDisplayed(),
                "Loai giam mac dinh la Phan tram nen o Giam toi da (admin-promo-max-discount-input) phai hien");
        Assert.assertTrue(adminPage.isPromoMaxUsesInputDisplayed(),
                "Form tao voucher phai co o So luot dung toi da (admin-promo-max-uses-input)");
        Assert.assertEquals(adminPage.getPromoStartInputType(), "datetime-local",
                "O Ngay bat dau phai la input kieu datetime-local");
        Assert.assertEquals(adminPage.getPromoExpiryInputType(), "date",
                "O Ngay het han phai la input kieu date");
        Assert.assertFalse(adminPage.getPromoFormText().contains("Định dạng dd/mm/yyyy."),
                "Trang tao khuyen mai dot 21 KHONG duoc con ghi chu 'Định dạng dd/mm/yyyy.' duoi o Ngay het han");
        Assert.assertTrue(adminPage.isPromoMaxUsesPerUserInputDisplayed(),
                "Form tao voucher phai co o Luot dung / nguoi (admin-promo-max-uses-per-user-input)");
        Assert.assertTrue(adminPage.isPromoMinTierSelectDisplayed(),
                "Form tao voucher phai co o Hang toi thieu (admin-promo-min-tier)");
        Assert.assertTrue(adminPage.isPromoScopeSelectDisplayed(),
                "Form tao voucher phai co o Pham vi ap dung (admin-promo-scope)");
        Assert.assertFalse(adminPage.isPromoStatusToggleDisplayed(),
                "Trang tao khuyen mai dot 21 KHONG duoc co toggle 'Kích hoạt ngay sau khi tạo'");
        Assert.assertFalse(adminPage.getPromoFormText().contains("Nháp"),
                "Dot 24/26: trang tao khuyen mai KHONG con dong trang thai — form KHONG duoc ghi chu 'Nháp', dang thay: " + adminPage.getPromoFormText());
        Assert.assertFalse(adminPage.isPromoFormImageUploadPresent(),
                "Khuyen mai KHONG co upload anh — form tao voucher khong duoc co muc upload anh");

        adminPage.cancelPromoForm();
        Assert.assertTrue(adminPage.isPromoListPageDisplayed(),
                "Huy o trang tao (form sach) phai ve lai danh sach khuyen mai");
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-176 (TC_SCRA06_FUNC_015 + TC_SCRA06_FUNC_016 + TC_SCRA06_FUNC_017, đợt 21): Mã voucher tự viết hoa khi gõ ('sale10' → 'SALE10'); nút 'Tạo mã ngẫu nhiên' ĐÃ BỎ nên không còn sinh mã tự động — test gõ mã explicit; loại Phần trăm thì ô Giảm tối đa hiện, loại Cố định thì ô Giảm tối đa biến mất, quay lại Phần trăm thì ô hiện lại; đóng form không lưu")
    public void adminPromoCreateFormCodeAndDiscountTypeBehavior() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();

        adminPage.setPromoCode("sale10");
        Assert.assertEquals(adminPage.getPromoCodeInputValue(), "SALE10",
                "Go 'sale10' vao o Ma thi o phai hien 'SALE10' (tu viet hoa ngay khi go)");

        Assert.assertFalse(adminPage.isPromoGenerateCodeButtonDisplayed(),
                "Trang tao khuyen mai dot 21 KHONG duoc co nut 'Tạo mã ngẫu nhiên' — ma phai go tay");
        adminPage.setPromoCode("TESTFREESHIP1");
        Assert.assertEquals(adminPage.getPromoCodeInputValue(), "TESTFREESHIP1",
                "Go ma explicit vao o Ma thi o phai giu dung ma da go");

        Assert.assertTrue(adminPage.isPromoMaxDiscountInputDisplayed(),
                "Loai giam mac dinh Phan tram thi o Giam toi da phai hien");
        adminPage.clickPromoDiscountTypeFixed();
        Assert.assertFalse(adminPage.isPromoMaxDiscountInputDisplayed(),
                "Chon loai 'Cố định' thi o Giam toi da phai bien mat");
        adminPage.clickPromoDiscountTypePercent();
        Assert.assertTrue(adminPage.isPromoMaxDiscountInputDisplayed(),
                "Chon lai loai 'Phần trăm' thi o Giam toi da phai hien lai");

        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    // ---------- Form tao voucher: validation (SCR_06 VAL_007-013) ----------

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-177 (TC_SCRA06_VAL_007): Bỏ trống Mã khi tạo voucher: để trống ô Mã, nhập Giá trị giảm 10 rồi bấm Lưu thì hiện lỗi đỏ 'Vui lòng bổ sung thông tin bắt buộc.' dưới ô Mã (admin-promo-code-error); không gọi POST /api/admin/promotions, form vẫn mở")
    public void adminPromoCodeRequiredError() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();
        adminPage.setPromoDiscountValue("10");
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("code"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong Ma thi loi duoi o Ma phai la 'Vui lòng bổ sung thông tin bắt buộc.'");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do thieu Ma thi form phai van mo (khong dong, khong tao voucher)");
        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-178 (TC_SCRA06_VAL_008): Mã voucher sai định dạng: mã quá ngắn ('AB') và mã chứa ký tự lạ ('SALE@10') đều hiện lỗi đỏ 'Mã chỉ gồm chữ, số, gạch ngang/gạch dưới, từ 3 đến 50 ký tự.' dưới ô Mã; không trường hợp nào gọi POST")
    public void adminPromoCodeFormatValidation() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();
        adminPage.setPromoDiscountValue("10");
        String expectedError = "Mã chỉ gồm chữ, số, gạch ngang/gạch dưới, từ 3 đến 50 ký tự.";

        adminPage.setPromoCode("AB");
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("code"), expectedError,
                "Ma qua ngan 'AB' thi loi duoi o Ma phai la chuoi dinh dang tren");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do sai dinh dang Ma thi form phai van mo");

        adminPage.setPromoCode("SALE@10");
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("code"), expectedError,
                "Ma chua ky tu la 'SALE@10' thi loi duoi o Ma phai la chuoi dinh dang tren");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do sai dinh dang Ma thi form phai van mo");
        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-179 (TC_SCRA06_VAL_009): Bỏ trống Giá trị giảm khi tạo voucher: nhập Mã, để trống Giá trị giảm rồi bấm Lưu thì hiện lỗi đỏ dưới ô Giá trị giảm (admin-promo-value-error); không gọi POST, form vẫn mở")
    public void adminPromoValueRequiredError() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();
        adminPage.setPromoCode(uniquePromoCode());
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("value"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong Gia tri giam thi loi duoi o Gia tri giam phai la 'Vui lòng bổ sung thông tin bắt buộc.'");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do thieu Gia tri giam thi form phai van mo (khong dong, khong tao voucher)");
        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-180 (TC_SCRA06_VAL_010 + TC_SCRA06_VAL_011): Giảm theo % vượt 100 và Giá trị giảm bằng 0: loại Phần trăm + Giá trị giảm 101 → lỗi 'Giá trị giảm không hợp lệ (giảm % tối đa 100).' dưới ô Giá trị giảm; Giá trị giảm 0 → cùng lỗi đó; không trường hợp nào gọi POST")
    public void adminPromoDiscountValueValidation() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();
        adminPage.setPromoCode(uniquePromoCode());
        adminPage.clickPromoDiscountTypePercent();
        String expectedError = "Giá trị giảm không hợp lệ (giảm % tối đa 100).";

        adminPage.setPromoDiscountValue("101");
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("value"), expectedError,
                "Gia tri giam 101% thi loi duoi o Gia tri giam phai la chuoi tren");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do gia tri giam vuot 100 thi form phai van mo");

        adminPage.setPromoDiscountValue("0");
        adminPage.clickPromoSaveButton();
        Assert.assertEquals(adminPage.getPromoFieldErrorText("value"), expectedError,
                "Gia tri giam 0 thi loi duoi o Gia tri giam phai la chuoi tren");
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do gia tri giam bang 0 thi form phai van mo");
        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-181 (TC_SCRA06_VAL_012 + TC_SCRA06_VAL_013): Phạm vi Theo danh mục nhưng không chọn danh mục → lỗi 'Voucher theo danh mục phải chọn danh mục.' dạng lỗi chung đầu form (sa-error, role=alert); phạm vi Theo sản phẩm nhưng để trống ô ID sản phẩm → lỗi 'Voucher theo sản phẩm phải chọn ít nhất 1 sản phẩm (nhập ID, cách nhau bằng dấu phẩy).'; không trường hợp nào gọi POST")
    public void adminPromoScopeValidation() {
        AdminPageObject adminPage = openPromosOrSkip();
        adminPage.openPromoForm();
        adminPage.setPromoCode(uniquePromoCode());
        adminPage.setPromoDiscountValue("10");

        adminPage.selectPromoScope("Category");
        adminPage.clickPromoSaveButton();
        Assert.assertTrue(adminPage.getPromoFormText().contains("Voucher theo danh mục phải chọn danh mục."),
                "Pham vi 'Theo danh mục' ma khong chon danh muc thi form phai hien loi 'Voucher theo danh mục phải chọn danh mục.', dang thay: " + adminPage.getPromoFormText());
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do thieu danh muc thi form phai van mo");

        adminPage.selectPromoScope("Product");
        adminPage.clickPromoSaveButton();
        Assert.assertTrue(adminPage.getPromoFormText().contains("Voucher theo sản phẩm phải chọn ít nhất 1 sản phẩm (nhập ID, cách nhau bằng dấu phẩy)."),
                "Pham vi 'Theo sản phẩm' ma de trong o ID san pham thi form phai hien loi 'Voucher theo sản phẩm phải chọn ít nhất 1 sản phẩm (nhập ID, cách nhau bằng dấu phẩy).', dang thay: " + adminPage.getPromoFormText());
        Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                "Luu that bai do thieu ID san pham thi form phai van mo");
        adminPage.cancelPromoForm();
        Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                "Dong form xong khong duoc con thay form khuyen mai");
    }

    // ---------- Form sua voucher Draft (SCR_06 FUNC_022/035/036, VAL_040) ----------

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-182 (TC_SCRA06_FUNC_022, quy tắc 'chỉ sửa voucher Draft' là [CẦN XÁC NHẬN] — viết test theo code hiện tại): Mở form sửa voucher Draft: các nút chọn Loại giảm bị disable; các ô Ngày bắt đầu, Lượt dùng/người, Hạng tối thiểu, Phạm vi áp dụng đều bị disable; chỉ sửa được Mô tả, Giá trị giảm, Đơn tối thiểu, Giảm tối đa, Ngày hết hạn, Số lượt dùng tối đa; đóng form không lưu")
    public void adminPromoEditFormLocksNonEditableFields() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            createDraftPromoForEdit(adminPage, code);
            adminPage.openPromoEditForm(code);
            Assert.assertEquals(adminPage.getPromoFormMode(), "edit",
                    "Form sua voucher phai co data-form-mode='edit'");
            Assert.assertTrue(adminPage.isPromoCodeInputDisabled(),
                    "Khi sua voucher thi o Ma phai bi disable (khong doi duoc ma)");
            Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisabled("Fixed"),
                    "Khi sua voucher thi nut loai giam 'Cố định' phai bi disable");
            Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisabled("Percentage"),
                    "Khi sua voucher thi nut loai giam 'Phần trăm' phai bi disable");
            Assert.assertTrue(adminPage.isPromoDiscountTypeButtonDisabled("FreeShip"),
                    "Khi sua voucher thi nut loai giam 'Miễn phí ship' phai bi disable");
            Assert.assertTrue(adminPage.isPromoStartInputDisabled(),
                    "Khi sua voucher thi o Ngay bat dau phai bi disable");
            Assert.assertTrue(adminPage.isPromoMaxUsesPerUserInputDisabled(),
                    "Khi sua voucher thi o Luot dung / nguoi phai bi disable");
            Assert.assertTrue(adminPage.isPromoMinTierSelectDisabled(),
                    "Khi sua voucher thi o Hang toi thieu phai bi disable");
            Assert.assertTrue(adminPage.isPromoScopeSelectDisabled(),
                    "Khi sua voucher thi o Pham vi ap dung phai bi disable");
            Assert.assertTrue(adminPage.isPromoDescriptionInputEnabled(),
                    "Khi sua voucher thi o Mo ta phai sua duoc");
            Assert.assertTrue(adminPage.isPromoDiscountValueInputEnabled(),
                    "Khi sua voucher thi o Gia tri giam phai sua duoc");
            Assert.assertTrue(adminPage.isPromoMinOrderInputEnabled(),
                    "Khi sua voucher thi o Don toi thieu phai sua duoc");
            Assert.assertTrue(adminPage.isPromoMaxDiscountInputEnabled(),
                    "Khi sua voucher thi o Giam toi da phai sua duoc");
            Assert.assertTrue(adminPage.isPromoExpiryInputEnabled(),
                    "Khi sua voucher thi o Ngay het han phai sua duoc");
            Assert.assertTrue(adminPage.isPromoMaxUsesInputEnabled(),
                    "Khi sua voucher thi o So luot dung toi da phai sua duoc");
            adminPage.cancelPromoForm();
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Dong form xong khong duoc con thay form khuyen mai");
        } finally {
            deleteDraftPromoQuietly(adminPage, code);
        }
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-183 (TC_SCRA06_FUNC_035 + TC_SCRA06_FUNC_036, đợt 24/25 viết lại — dialog 'Thoát mà chưa lưu?' -> 'Hủy các thay đổi?', nút 'Thoát' -> 'Rời trang'): Đã sửa mà bấm Hủy thì hỏi 'Hủy các thay đổi?': mở TRANG sửa voucher Draft thì nút 'Lưu thay đổi' bị disable khi chưa sửa gì, bật lên sau khi sửa Mô tả, lại disable khi trả Mô tả về giá trị ban đầu; sửa Mô tả rồi bấm Hủy → hộp 'Hủy các thay đổi?' (2 nút [Ở lại][Rời trang]): 'Ở lại' thì trang sửa vẫn mở, dữ liệu đã sửa còn nguyên; 'Rời trang' thì về danh sách không lưu")
    public void adminPromoEditFormDirtyBehavior() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            createDraftPromoForEdit(adminPage, code);
            adminPage.openPromoEditForm(code);
            Assert.assertFalse(adminPage.isPromoSaveButtonEnabled(),
                    "Mo form sua ma chua sua gi thi nut 'Lưu thay đổi' phai bi disable");

            String originalDescription = adminPage.getPromoDescriptionInputValue();
            String editedDescription = originalDescription + " da sua";
            adminPage.setPromoDescription(editedDescription);
            Assert.assertTrue(adminPage.isPromoSaveButtonEnabled(),
                    "Sua o Mo ta xong thi nut 'Lưu thay đổi' phai bat len");
            adminPage.setPromoDescription(originalDescription);
            Assert.assertFalse(adminPage.isPromoSaveButtonEnabled(),
                    "Tra Mo ta ve gia tri ban dau thi nut 'Lưu thay đổi' phai lai disable");

            adminPage.setPromoDescription(editedDescription);
            Assert.assertTrue(adminPage.isPromoSaveButtonEnabled(),
                    "Sua o Mo ta lan nua thi nut 'Lưu thay đổi' phai bat len");
            adminPage.clickPromoCancelButton();
            Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                    "Bam 'Hủy' khi form dirty phai mo hop 'Hủy các thay đổi?'");
            Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Hủy các thay đổi?",
                    "Hop dong form dirty phai co tieu de 'Hủy các thay đổi?'");
            Assert.assertTrue(adminPage.getConfirmDialogText().contains("Các thay đổi bạn vừa nhập sẽ bị mất."),
                    "Hop dong form dirty phai co thong diep 'Các thay đổi bạn vừa nhập sẽ bị mất.'");
            Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                    "Hop 'Hủy các thay đổi?' phai CHI co dung 2 nut");
            Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Ở lại",
                    "Nut trai hop dong form dirty phai la 'Ở lại'");
            Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Rời trang",
                    "Nut phai hop dong form dirty phai la 'Rời trang'");

            adminPage.clickConfirmNo();
            Assert.assertFalse(adminPage.isConfirmDialogPresent(),
                    "Bam 'Ở lại' thi hop 'Hủy các thay đổi?' phai dong");
            Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                    "Bam 'Ở lại' thi trang sua phai van mo");
            Assert.assertEquals(adminPage.getPromoDescriptionInputValue(), editedDescription,
                    "Bam 'Ở lại' thi du lieu da sua trong form phai con nguyen");

            adminPage.clickPromoCancelButton();
            adminPage.clickConfirmYes();
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Bam 'Rời trang' thi trang sua phai dong ma khong luu (ve danh sach)");
        } finally {
            deleteDraftPromoQuietly(adminPage, code);
        }
    }

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM19-184 (TC_SCRA06_VAL_040, đợt 21): Sửa voucher Draft: Giá trị giảm = 0 thì chặn (validate sửa đầy đủ như tạo mới): bấm Sửa ở voucher Draft, đổi Giá trị giảm thành 0 rồi bấm Lưu thay đổi thì hiện lỗi 'Giá trị giảm không hợp lệ (giảm % tối đa 100).' dưới ô Giá trị giảm; không gọi PUT; form vẫn mở; đóng form không lưu")
    public void adminPromoEditDiscountValueZeroBlocked() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            createDraftPromoForEdit(adminPage, code);
            adminPage.openPromoEditForm(code);
            adminPage.setPromoDiscountValue("0");
            adminPage.clickPromoSaveButton();
            Assert.assertEquals(adminPage.getPromoFieldErrorText("value"),
                    "Giá trị giảm không hợp lệ (giảm % tối đa 100).",
                    "Sua Gia tri giam thanh 0 thi loi duoi o Gia tri giam phai la chuoi tren");
            Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                    "Luu that bai do Gia tri giam bang 0 thi form phai van mo (khong goi PUT)");
            adminPage.cancelPromoForm();
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Dong form xong khong duoc con thay form khuyen mai");
        } finally {
            deleteDraftPromoQuietly(adminPage, code);
        }
    }

    // ---------- Dot 24 phase 1: khoi "Gia tri giam" (radio card / input-group / xem truoc realtime) ----------

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM26-06 (đợt 24 phase 1, MỚI): Khối 'Giá trị giảm' ở trang tạo khuyến mãi — mặc định loại Phần trăm + xem trước mờ 'Nhập mức giảm để xem trước'; chọn Cố định + nhập 1000000 thì ô hiện '1.000.000' (dấu chấm nghìn, hậu tố ₫) + xem trước 'Giảm 1.000.000₫ cho đơn từ 50.000₫' khi đơn tối thiểu 50000; chọn Phần trăm + nhập 15 + giảm tối đa 50000 thì xem trước 'Giảm 15% (tối đa 50.000₫) cho mọi đơn hàng'; phím ArrowRight trên radio card chuyển loại (Phần trăm -> Miễn phí ship) + ô Giá trị biến mất; để trống giá trị rồi Lưu thì lỗi 'Vui lòng bổ sung thông tin bắt buộc.' dưới ô Giá trị, form vẫn mở, không tạo voucher; đóng bằng Hủy")
    public void adminPromoDiscountBlockTypesPreviewAndEmptySave() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            adminPage.openPromoForm();
            Assert.assertTrue(adminPage.isPromoCreatePageDisplayed(),
                    "Bam '+ Thêm mới' phai mo trang tao khuyen mai full page");
            // Mac dinh: loai Phan tram + xem truoc mo.
            Assert.assertTrue(adminPage.isPromoDiscountTypeSelected("Percentage"),
                    "Mo trang tao khuyen mai thi loai giam mac dinh phai la 'Phần trăm'");
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("Nhập mức giảm để xem trước"),
                    "Chua nhap muc giam thi dai xem truoc phai ghi 'Nhập mức giảm để xem trước', dang thay: " + adminPage.getPromoPreviewText());

            // (1) Co dinh 1000000 + don toi thieu 50000.
            adminPage.clickPromoDiscountTypeFixed();
            Assert.assertTrue(adminPage.isPromoDiscountTypeSelected("Fixed"),
                    "Bam radio card 'Cố định' thi loai giam phai la 'Cố định'");
            adminPage.setPromoDiscountValue("1000000");
            Assert.assertEquals(adminPage.getPromoDiscountValueInputValue(), "1.000.000",
                    "Nhap 1000000 vao o Gia tri giam (Co dinh) thi o phai hien '1.000.000' (dau cham nghin)");
            adminPage.sendKeyToElement(AdminPageUI.ADMIN_PROMO_MIN_ORDER_INPUT, "50000");
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("Giảm 1.000.000"),
                    "Nhap Co dinh 1000000 thi xem truoc phai ghi 'Giảm 1.000.000₫...', dang thay: " + adminPage.getPromoPreviewText());
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("đơn từ 50.000"),
                    "Nhap don toi thieu 50000 thi xem truoc phai ghi 'cho đơn từ 50.000₫', dang thay: " + adminPage.getPromoPreviewText());

            // (2) Phan tram 15 + giam toi da 50000.
            adminPage.clickPromoDiscountTypePercent();
            adminPage.setPromoDiscountValue("15");
            adminPage.sendKeyToElement(AdminPageUI.ADMIN_PROMO_MAX_DISCOUNT_INPUT, "50000");
            Assert.assertEquals(adminPage.getPromoDiscountValueInputValue(), "15",
                    "Nhap 15 vao o Gia tri giam (Phan tram) thi o phai hien '15'");
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("Giảm 15%"),
                    "Nhap Phan tram 15 thi xem truoc phai ghi 'Giảm 15%...', dang thay: " + adminPage.getPromoPreviewText());
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("tối đa 50.000"),
                    "Nhap giam toi da 50000 thi xem truoc phai ghi '(tối đa 50.000₫)', dang thay: " + adminPage.getPromoPreviewText());

            // (3) Dieu huong bang phim: ArrowRight tu Phan tram -> Mien phi ship.
            adminPage.pressArrowRightOnPromoDiscountType("Percentage");
            Assert.assertTrue(adminPage.isPromoDiscountTypeSelected("FreeShip"),
                    "Bam ArrowRight tren radio card 'Phần trăm' thi loai giam phai chuyen sang 'Miễn phí ship'");
            Assert.assertFalse(adminPage.isPromoDiscountValueInputDisplayed(),
                    "Chon loai 'Miễn phí ship' thi o Gia tri giam phai bien mat");
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("Miễn phí vận chuyển"),
                    "Chon loai 'Miễn phí ship' thi xem truoc phai ghi 'Miễn phí vận chuyển...', dang thay: " + adminPage.getPromoPreviewText());

            // (4) De trong gia tri (quay lai Phan tram) roi Luu -> loi, khong tao voucher.
            adminPage.clickPromoDiscountTypePercent();
            adminPage.setPromoDiscountValue("");
            adminPage.fillPromoForm(code, "Voucher test ADM26-06 de trong gia tri", "Percentage", "", "", "", "", "2027-12-31");
            adminPage.clickPromoSaveButton();
            adminPage.sleepInSecond(1);
            Assert.assertEquals(adminPage.getPromoFieldErrorText("value"), "Vui lòng bổ sung thông tin bắt buộc.",
                    "De trong Gia tri giam (Phan tram) roi Luu thi loi duoi o Gia tri giam phai la 'Vui lòng bổ sung thông tin bắt buộc.'");
            Assert.assertTrue(adminPage.isPromoFormModalDisplayed(),
                    "Luu that bai do thieu Gia tri giam thi form phai van mo (khong goi POST)");

            adminPage.cancelPromoForm();
            Assert.assertFalse(adminPage.isPromoFormModalPresent(),
                    "Dong form xong khong duoc con thay form khuyen mai");
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 0,
                    "Luu that bai thi khong duoc tao voucher '" + code + "'");
        } finally {
            deleteDraftPromoQuietly(adminPage, code);
        }
    }

    // ---------- Dot 21: FreeShip khong yeu cau nhap gia tri (chot diem [CAN XAC MINH] cu) ----------

    @Story("Khuyến mãi — form voucher")
    @Test(description = "ADM21-01 (đợt 21, FreeShip — chốt điểm [CẦN XÁC MINH] cũ: FreeShip KHÔNG yêu cầu nhập giá trị, 0 được phép; đợt 24 viết lại — ghi chú FreeShip ĐÃ BỎ, thay bằng dải xem trước): Chọn radio card 'Miễn phí ship' ở trang tạo mới → ô Giá trị giảm biến mất, dải xem trước hiện 'Miễn phí vận chuyển cho mọi đơn hàng'; nhập mã + mô tả + đơn tối thiểu rồi Lưu → tạo thành công ở trạng thái Nháp, bảng hiện giá trị 'Freeship'; dọn dẹp: xóa voucher Draft")
    public void adminPromoCreateFreeShipWithoutValue() {
        String code = uniquePromoCode();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("promotions");
        try {
            adminPage.openPromoForm();
            Assert.assertTrue(adminPage.isPromoCreatePageDisplayed(),
                    "Bam '+ Thêm mới' phai mo trang tao khuyen mai full page");
            adminPage.clickPromoDiscountTypeFreeShip();
            Assert.assertFalse(adminPage.isPromoDiscountValueInputDisplayed(),
                    "Chon loai 'Miễn phí ship' thi o Gia tri giam phai bien mat (khong can nhap gia tri)");
            Assert.assertTrue(adminPage.isPromoPreviewDisplayed(),
                    "Chon loai 'Miễn phí ship' thi dai xem truoc muc giam phai hien");
            Assert.assertTrue(adminPage.getPromoPreviewText().contains("Miễn phí vận chuyển"),
                    "Chon loai 'Miễn phí ship' thi dai xem truoc phai ghi 'Miễn phí vận chuyển...', dang thay: " + adminPage.getPromoPreviewText());
            adminPage.fillPromoForm(code, "Voucher test ADM21-01 FreeShip", "FreeShip",
                    "", "0", "", "", "2027-12-31");
            adminPage.savePromoForm();
            Assert.assertTrue(adminPage.isToastDisplayed(),
                    "Tao voucher FreeShip xong phai hien toast thanh cong (admin-toast)");
            Assert.assertTrue(adminPage.getToastDescText().contains("Thêm khuyến mãi " + code + " thành công."),
                    "Toast tao voucher FreeShip phai ghi 'Thêm khuyến mãi " + code + " thành công.', dang thay: " + adminPage.getToastDescText());
            adminPage.searchPromos(code);
            Assert.assertEquals(adminPage.getAdminTableRowCount(), 1,
                    "Tim theo ma voucher FreeShip '" + code + "' thi chi duoc con dung 1 dong");
            String row = adminPage.getAdminTableRowTexts().get(0);
            Assert.assertTrue(row.contains("Freeship"),
                    "Dong voucher FreeShip phai hien gia tri 'Freeship', dang thay: " + row);
            Assert.assertTrue(row.contains("Nháp"),
                    "Voucher FreeShip moi tao phai o trang thai Nháp, dang thay: " + row);
        } finally {
            deleteDraftPromoQuietly(adminPage, code);
        }
    }
}
