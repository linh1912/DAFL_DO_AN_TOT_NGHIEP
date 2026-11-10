package vn.dafl.tests.admin.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

/**
 * Test form them/sua san pham o Admin rieng localhost:3001 (wave 19, lop MOI
 * cho gap SCR_04): validate + hanh vi form TRANG full page dot 21
 * (/products/new, /products/:code/edit — KHONG con Drawer 760px
 * admin-product-form-modal).
 * Toan bo test trong lop nay CHI assert loi roi dong form, KHONG bam Luu thanh
 * cong nen KHONG tao/sua/xoa du lieu that — tru test mo form sua tren SP co san
 * (chi doc + dong, khong luu).
 * Can cu code that dot 18 (AdminForms.tsx: validateAll + FormShell):
 *  - Form tao moi tu chon san danh muc dau tien (openProductCreate dat
 *    categoryId = categories[0]) nen KHONG the bo trong danh muc qua UI, va
 *    khoi Xem truoc hien ten danh muc do ngay khi mo form (khong hien
 *    'Chưa chọn danh mục'); loi bat buoc chi co o Ten / Gia ban / Ton kho.
 *  - Loi BE (trung slug, mo ta qua dai, DM da an) chi hien dang formError
 *    trong form, KHONG kem toast (handleSubmit chi setFormError) — xem
 *    AdminProductTest (VAL_031/032/033).
 *  - Nut 'Lưu thay đổi' o form sua bi disable khi chua sua gi (dirty=false).
 * Test tu bo qua (skip, khong tinh fail) khi service admin :3001 chua chay hoac
 * tai khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Quản lý sản phẩm")
public class AdminProductFormTest extends BaseTest {

    /** Dang nhap Super Admin tren app rieng :3001; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        return adminPage;
    }

    /** Mo form them san pham moi (da dang nhap + vao muc San pham). */
    private AdminPageObject openCreateForm() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        return adminPage;
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-51 (TC_SCRA04_UI_006): Form Thêm SP có đủ 5 nhóm (Thông tin cơ bản, Giá & tồn kho, Hình ảnh, Mô tả, Xem trước); ô Mã SP đang focus khi mở form; dropzone chứa input file thật (không display:none); có nút 'Hoặc dán link ảnh', Toggle trạng thái, nút +/- tồn kho, khối xem trước; đóng bằng Hủy, không tạo dữ liệu")
    public void adminProductFormShowsAllSectionsAndAutofocusesCode() {
        AdminPageObject adminPage = openCreateForm();
        String modalText = adminPage.getProductFormModalText();
        Assert.assertTrue(modalText.contains("Thông tin cơ bản"),
                "Form them SP phai co nhom 'Thông tin cơ bản', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Giá & tồn kho"),
                "Form them SP phai co nhom 'Giá & tồn kho', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Hình ảnh"),
                "Form them SP phai co nhom 'Hình ảnh', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Mô tả"),
                "Form them SP phai co nhom 'Mô tả', dang thay: " + modalText);
        Assert.assertTrue(modalText.contains("Xem trước"),
                "Form them SP phai co nhom 'Xem trước', dang thay: " + modalText);
        Assert.assertTrue(adminPage.isProductCodeInputFocused(),
                "Mo form them SP thi o Ma SP (o nhap dau tien) phai dang duoc focus (FormShell autofocus)");
        Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                "Form them SP phai co dropzone upload anh (admin-product-image-dropzone)");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_IMAGE_INPUT),
                "Dropzone phai co input file that (admin-product-image-input) trong DOM, khong display:none");
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_IMAGE_INPUT),
                "Input file anh phai bat duoc (khong disable)");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_IMAGE_LINK_TOGGLE),
                "Form them SP phai co nut 'Hoặc dán link ảnh'");
        Assert.assertTrue(adminPage.isProductStatusToggleDisplayed(),
                "Form them SP phai co Toggle trang thai (admin-product-status-toggle)");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_STOCK_PLUS_BTN),
                "Form them SP phai co nut '+' tang ton kho");
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_STOCK_MINUS_BTN),
                "Form them SP phai co nut '−' giam ton kho");
        Assert.assertTrue(adminPage.isProductPreviewDisplayed(),
                "Form them SP phai co khoi Xem truoc (admin-product-preview)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-52 (TC_SCRA04_UI_007): Ô tiền MoneyInput định dạng dấu chấm nghìn + hậu tố ₫ — nhập 200000 vào Giá bán và 250000 vào Giá gốc thì ô hiển thị '200.000' và '250.000', mỗi ô có hậu tố ₫; gõ chữ vào ô tiền thì bị loại bỏ; đóng bằng Hủy, không lưu")
    public void adminProductFormMoneyInputsFormatThousandsAndSuffix() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setProductPrice("200000");
        Assert.assertEquals(adminPage.getProductPriceInputValue(), "200.000",
                "Go 200000 vao o Gia ban phai hien '200.000' (dau cham nghin)");
        adminPage.setProductOriginalPrice("250000");
        Assert.assertEquals(adminPage.getProductOriginalPriceInputValue(), "250.000",
                "Go 250000 vao o Gia goc phai hien '250.000' (dau cham nghin)");
        Assert.assertEquals(adminPage.getProductMoneySuffixCount(), 2,
                "Form san pham phai co dung 2 hau to ₫ (o Gia ban + o Gia goc)");

        // Go chu vao o tien: ky tu khong phai so bi loai ngay khi go.
        adminPage.setProductPrice("abc");
        Assert.assertEquals(adminPage.getProductPriceInputValue(), "",
                "Go 'abc' vao o Gia ban phai bi loai het, o tro ve rong");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-53 (TC_SCRA04_UI_008): Badge % giảm tự tính từ Giá bán + Giá gốc — nhập Giá bán 200000 + Giá gốc 250000 thì badge 'Giảm 20%' hiện ngay khi gõ; bỏ trống Giá gốc hoặc Giá gốc ≤ Giá bán thì badge ẩn; đóng bằng Hủy, không lưu")
    public void adminProductFormDiscountBadgeAppearsLiveWhileTyping() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setProductPrice("200000");
        adminPage.setProductOriginalPrice("250000");
        Assert.assertTrue(adminPage.isDiscountBadgeDisplayed(),
                "Nhap Gia ban 200000 + Gia goc 250000 thi badge % giam phai hien ngay khi go");
        Assert.assertEquals(adminPage.getDiscountBadgeText(), "Giảm 20%",
                "Badge % giam phai hien 'Giảm 20%' (200000/250000), dang thay: " + adminPage.getDiscountBadgeText());

        adminPage.setProductOriginalPrice("");
        Assert.assertFalse(adminPage.isDiscountBadgeDisplayed(),
                "Bo trong Gia goc thi badge % giam phai an");

        adminPage.setProductOriginalPrice("150000");
        Assert.assertFalse(adminPage.isDiscountBadgeDisplayed(),
                "Gia goc (150000) nho hon Gia ban (200000) thi badge % giam phai an");

        adminPage.setProductOriginalPrice("200000");
        Assert.assertFalse(adminPage.isDiscountBadgeDisplayed(),
                "Gia goc bang Gia ban (200000) thi badge % giam phai an");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-54 (TC_SCRA04_UI_009): Khối Xem trước cập nhật trực tiếp theo dữ liệu nhập — nhập tên 'Son kem', giá 200000, tồn kho 5 thì khối Xem trước hiện đúng tên, tên danh mục đã chọn sẵn, giá '200.000đ', tồn kho 5; chưa nhập tên/giá thì hiện placeholder 'Tên sản phẩm' / '—'; đóng bằng Hủy, không lưu")
    public void adminProductFormPreviewUpdatesLive() {
        AdminPageObject adminPage = openCreateForm();
        // Form tao moi tu chon san danh muc dau tien (openProductCreate) nen
        // khoi Xem truoc hien ten danh muc do ngay khi mo form, khong hien
        // placeholder 'Chưa chọn danh mục' (placeholder nay khong toi duoc qua UI).
        String categoryName = adminPage.getFirstRealCategoryName();
        Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de mo form san pham");
        String preview = adminPage.getProductPreviewText();
        Assert.assertTrue(preview.contains("Tên sản phẩm"),
                "Chua nhap ten thi khoi Xem truoc phai hien placeholder 'Tên sản phẩm', dang thay: " + preview);
        Assert.assertTrue(preview.contains(categoryName),
                "Khoi Xem truoc phai hien ten danh muc da chon san '" + categoryName + "', dang thay: " + preview);
        Assert.assertTrue(preview.contains("—"),
                "Chua nhap gia thi khoi Xem truoc phai hien '—' o cho gia, dang thay: " + preview);

        adminPage.setProductName("Son kem");
        adminPage.setProductPrice("200000");
        adminPage.setProductStock("5");
        preview = adminPage.getProductPreviewText();
        Assert.assertTrue(preview.contains("Son kem"),
                "Nhap ten 'Son kem' thi khoi Xem truoc phai hien 'Son kem', dang thay: " + preview);
        Assert.assertTrue(preview.contains(categoryName),
                "Khoi Xem truoc phai hien ten danh muc '" + categoryName + "', dang thay: " + preview);
        Assert.assertTrue(preview.contains("200.000đ"),
                "Nhap gia 200000 thi khoi Xem truoc phai hien '200.000đ', dang thay: " + preview);
        Assert.assertTrue(preview.contains("Tồn kho: 5"),
                "Nhap ton kho 5 thi khoi Xem truoc phai hien 'Tồn kho: 5', dang thay: " + preview);

        // Xoa ten: khoi Xem truoc tro lai placeholder 'Tên sản phẩm'.
        adminPage.setProductName("");
        Assert.assertTrue(adminPage.getProductPreviewText().contains("Tên sản phẩm"),
                "Xoa trong o Ten thi khoi Xem truoc phai tro lai placeholder 'Tên sản phẩm'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-55 (TC_SCRA04_UI_010/011): Bộ đếm ký tự tên sản phẩm x/120 và mô tả x/2000 — trống thì '0/120' và '0/2000', nhập 'Son kem' thì '7/120', nhập mô tả 25 ký tự thì '25/2000'; đóng bằng Hủy, không lưu")
    public void adminProductFormNameAndDescriptionCounters() {
        AdminPageObject adminPage = openCreateForm();
        Assert.assertEquals(adminPage.getProductNameCountText(), "0/120",
                "Bo dem ten khi trong phai hien '0/120'");
        Assert.assertEquals(adminPage.getProductDescriptionCountText(), "0/2000",
                "Bo dem mo ta khi trong phai hien '0/2000'");

        String name = "Son kem";
        adminPage.setProductName(name);
        Assert.assertEquals(adminPage.getProductNameCountText(), name.length() + "/120",
                "Nhap ten '" + name + "' thi bo dem ten phai hien '" + name.length() + "/120'");

        String description = "Mô tả sản phẩm test 25 ký";
        adminPage.setProductDescription(description);
        Assert.assertEquals(adminPage.getProductDescriptionCountText(), description.length() + "/2000",
                "Nhap mo ta " + description.length() + " ky tu thi bo dem mo ta phai hien '"
                        + description.length() + "/2000'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-56 (TC_SCRA04_UI_012 + TC_SCRA04_VAL_029): Nút tăng/giảm tồn kho (+/-), giảm ở 0 vẫn giữ 0; gõ '12ab' vào ô Tồn kho thì ô chỉ giữ '12' (chữ bị loại, không nhập được âm/thập phân); đóng bằng Hủy, không lưu")
    public void adminProductFormStockStepperAndDigitsOnly() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.clickProductStockPlus();
        Assert.assertEquals(adminPage.getProductStockInputValue(), "1",
                "Bam '+' ton kho 1 lan thi o Ton kho phai hien '1'");
        adminPage.clickProductStockMinus();
        Assert.assertEquals(adminPage.getProductStockInputValue(), "0",
                "Bam '−' ton kho 1 lan thi o Ton kho phai ve '0'");
        adminPage.clickProductStockMinus();
        Assert.assertEquals(adminPage.getProductStockInputValue(), "0",
                "Bam '−' khi ton kho dang 0 thi o Ton kho van phai giu '0' (khong am)");

        adminPage.setProductStock("12ab");
        Assert.assertEquals(adminPage.getProductStockInputValue(), "12",
                "Go '12ab' vao o Ton kho thi o chi giu '12' (ky tu khong phai so bi loai ngay khi go)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-57 (TC_SCRA04_UI_013): Dropdown Danh mục tự làm có ô tìm kiếm — bấm ô Danh mục thì dropdown mở có ô tìm kiếm (placeholder 'Tìm danh mục...'); gõ 'Son' thì chỉ còn các danh mục chứa 'Son'; chọn 'Son môi' thì ô Danh mục hiển thị 'Son môi'; đóng bằng Hủy, không lưu")
    public void adminProductFormCategoryPickerSearchFiltersOptions() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.openProductCategoryPicker();
        Assert.assertTrue(adminPage.isProductCategorySearchDisplayed(),
                "Mo dropdown Danh muc thi phai thay o tim kiem trong dropdown");
        Assert.assertEquals(adminPage.getElementAttribute(AdminPageUI.ADMIN_PRODUCT_CATEGORY_SEARCH, "placeholder"),
                "Tìm danh mục...",
                "O tim kiem trong dropdown Danh muc phai co placeholder 'Tìm danh mục...'");

        adminPage.setProductCategorySearch("Son");
        java.util.List<String> options = adminPage.getProductCategoryOptionTexts();
        if (options.isEmpty()) {
            throw new SkipException("Bo qua: DB nay khong co danh muc nao chua 'Son' de kiem tra loc dropdown Danh muc");
        }
        for (String option : options) {
            Assert.assertTrue(option.toLowerCase().contains("son"),
                    "Go 'Son' thi moi option con lai deu phai chua 'Son', dang thay: " + option);
        }
        if (!options.contains("Son môi")) {
            throw new SkipException("Bo qua: DB nay khong co danh muc 'Son môi' de kiem tra chon option trong dropdown Danh muc");
        }

        adminPage.selectProductCategoryOption("Son môi");
        Assert.assertEquals(adminPage.getProductCategoryTriggerText(), "Son môi",
                "Chon 'Son môi' thi o Danh muc phai hien 'Son môi'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-58 (TC_SCRA04_VAL_014/015/023/028/030): Bỏ trống tất cả trường bắt buộc khi thêm SP — bấm Lưu khi để trống thì mỗi ô bắt buộc (Tên, Giá bán, Tồn kho) hiện message đỏ 'Vui lòng bổ sung thông tin bắt buộc.'; ô Mã SP để trống KHÔNG báo lỗi; lặp lại từng trường rỗng riêng lẻ (chỉ trống Tên / chỉ trống Giá / chỉ trống Tồn kho) đều báo đúng lỗi ô đó; đóng bằng Hủy, không lưu")
    public void adminProductFormRequiredErrors() {
        AdminPageObject adminPage = openCreateForm();
        // (1) Bo trong toan bo: Ten + Gia ban + Ton kho bao loi; Ma + Danh muc
        // khong bao loi (Ma de trong thi tu sinh; Danh muc da duoc chon san
        // danh muc dau tien khi mo form nen khong the bo trong qua UI).
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong o Ten thi phai bao loi bat buoc duoi o Ten");
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong o Gia ban thi phai bao loi bat buoc duoi o Gia ban");
        Assert.assertEquals(adminPage.getProductFieldErrorText("stock"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Bo trong o Ton kho thi phai bao loi bat buoc duoi o Ton kho");
        Assert.assertEquals(adminPage.getProductFieldErrorText("code"), "",
                "O Ma SP de trong KHONG duoc bao loi (de trong thi BE tu sinh ma)");
        Assert.assertEquals(adminPage.getProductFieldErrorText("category"), "",
                "O Danh muc da duoc chon san danh muc dau tien nen KHONG duoc bao loi bat buoc");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do thieu truong bat buoc thi form phai van mo (khong dong, khong tao SP)");

        // (2) Chi dien Ten: Gia + Ton kho van bao loi, Ten het loi.
        adminPage.setProductName("Son kem test");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "",
                "Da dien Ten thi o Ten khong duoc con loi");
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chua dien Gia ban thi o Gia ban van phai bao loi bat buoc");
        Assert.assertEquals(adminPage.getProductFieldErrorText("stock"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chua dien Ton kho thi o Ton kho van phai bao loi bat buoc");

        // (3) Dien them Gia: chi con Ton kho bao loi.
        adminPage.setProductPrice("99000");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "",
                "Da dien Gia ban thi o Gia ban khong duoc con loi");
        Assert.assertEquals(adminPage.getProductFieldErrorText("stock"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chua dien Ton kho thi o Ton kho van phai bao loi bat buoc");

        // (4) Dien Ton kho (gio form da hop le) roi xoa trong tung o rieng le:
        // chi trong Ten.
        adminPage.setProductStock("10");
        adminPage.setProductName("");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chi de trong o Ten thi chi o Ten bao loi bat buoc");
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "",
                "Chi de trong o Ten thi o Gia ban khong duoc bao loi");
        Assert.assertEquals(adminPage.getProductFieldErrorText("stock"), "",
                "Chi de trong o Ten thi o Ton kho khong duoc bao loi");

        // (5) Chi trong Gia ban.
        adminPage.setProductName("Son kem test");
        adminPage.setProductPrice("");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chi de trong o Gia ban thi chi o Gia ban bao loi bat buoc");
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "",
                "Chi de trong o Gia ban thi o Ten khong duoc bao loi");

        // (6) Chi trong Ton kho.
        adminPage.setProductPrice("99000");
        adminPage.setProductStock("");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("stock"), "Vui lòng bổ sung thông tin bắt buộc.",
                "Chi de trong o Ton kho thi chi o Ton kho bao loi bat buoc");
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "",
                "Chi de trong o Ton kho thi o Ten khong duoc bao loi");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do thieu truong bat buoc thi form phai van mo (khong dong, khong tao SP)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-59 (TC_SCRA04_VAL_016/017): Tên sản phẩm ngắn hơn 3 ký tự ('So') và dài hơn 120 ký tự (121 ký tự) đều báo lỗi 'Tên sản phẩm phải có từ 3 đến 120 ký tự.' dưới ô Tên, không tạo sản phẩm; đóng bằng Hủy, không lưu")
    public void adminProductFormNameLengthValidation() {
        AdminPageObject adminPage = openCreateForm();
        // Cac truong khac dien hop le de chi con loi o Ten.
        adminPage.setProductPrice("99000");
        adminPage.setProductStock("10");

        adminPage.setProductName("So");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "Tên sản phẩm phải có từ 3 đến 120 ký tự.",
                "Ten 'So' (2 ky tu) phai bao loi do dai duoi o Ten");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do ten qua ngan thi form phai van mo (khong dong, khong tao SP)");

        String longName = "S".repeat(121);
        adminPage.setProductName(longName);
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("name"), "Tên sản phẩm phải có từ 3 đến 120 ký tự.",
                "Ten 121 ky tu phai bao loi do dai duoi o Ten");
        Assert.assertEquals(adminPage.getProductNameCountText(), "121/120",
                "Ten 121 ky tu thi bo dem ten phai hien '121/120'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-60 (TC_SCRA04_VAL_018/019/020/021): Mã SP sai định dạng — mã 'ab' (quá ngắn), 'SP_001' (gạch dưới), mã 21 ký tự đều báo lỗi 'Mã sản phẩm phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.' dưới ô Mã; gõ 'sp-001' thì ô hiển thị 'SP-001' (tự viết hoa) và không báo lỗi định dạng; đóng bằng Hủy, không lưu")
    public void adminProductFormCodeFormatValidation() {
        AdminPageObject adminPage = openCreateForm();
        // Cac truong khac dien hop le de chi con loi o Ma.
        adminPage.setProductName("Son kem test ma");
        adminPage.setProductPrice("99000");
        adminPage.setProductStock("10");

        adminPage.setProductCode("ab");
        Assert.assertEquals(adminPage.getProductCodeInputValue(), "AB",
                "Go 'ab' vao o Ma SP thi o phai hien 'AB' (tu viet hoa ngay khi go)");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("code"),
                "Mã sản phẩm phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 'ab' (qua ngan) phai bao loi dinh dang duoi o Ma");

        adminPage.setProductCode("SP_001");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("code"),
                "Mã sản phẩm phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 'SP_001' (chua gach duoi) phai bao loi dinh dang duoi o Ma");

        adminPage.setProductCode("ABCDEFGHIJ12345678901");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("code"),
                "Mã sản phẩm phải có 3–20 ký tự, chỉ gồm chữ in hoa, chữ số và gạch ngang.",
                "Ma 21 ky tu phai bao loi dinh dang duoi o Ma");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do ma sai dinh dang thi form phai van mo (khong dong, khong tao SP)");

        // Ma go chu thuong tu viet hoa thanh chu in hoa va hop le dinh dang:
        // chi kiem tra bang validate khi blur (khong bam Luu de khoi tao SP that).
        adminPage.setProductCode("sp-001");
        Assert.assertEquals(adminPage.getProductCodeInputValue(), "SP-001",
                "Go 'sp-001' vao o Ma SP thi o phai hien 'SP-001' (tu viet hoa ngay khi go)");
        adminPage.clickToElement(AdminPageUI.ADMIN_PRODUCT_NAME_INPUT);
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("code"), "",
                "Ma 'SP-001' hop le dinh dang nen khong duoc bao loi duoi o Ma");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-61 (TC_SCRA04_VAL_023/024): Bỏ trống ô Giá bán thì báo lỗi 'Vui lòng bổ sung thông tin bắt buộc.'; giá bán bằng 0 thì báo lỗi 'Giá bán phải lớn hơn 0.' dưới ô Giá bán, không tạo sản phẩm; đóng bằng Hủy, không lưu")
    public void adminProductFormPriceValidation() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setProductName("Son kem test gia");
        adminPage.setProductStock("10");

        adminPage.setProductPrice("");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "Vui lòng bổ sung thông tin bắt buộc.",
                "De trong o Gia ban thi phai bao loi bat buoc duoi o Gia ban");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do thieu gia ban thi form phai van mo (khong dong, khong tao SP)");

        adminPage.setProductPrice("0");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("price"), "Giá bán phải lớn hơn 0.",
                "Gia ban 0 phai bao loi 'Giá bán phải lớn hơn 0.' duoi o Gia ban");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-62 (TC_SCRA04_VAL_025/026): Giá gốc bằng giá bán (200000/200000) và giá gốc nhỏ hơn giá bán (giá bán 250000, giá gốc 200000) đều báo lỗi 'Giá gốc phải lớn hơn giá bán.' dưới ô Giá gốc và không hiện badge % giảm, không tạo sản phẩm; đóng bằng Hủy, không lưu")
    public void adminProductFormOriginalPriceValidation() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setProductName("Son kem test gia goc");
        adminPage.setProductStock("10");

        adminPage.setProductPrice("200000");
        adminPage.setProductOriginalPrice("200000");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("original-price"), "Giá gốc phải lớn hơn giá bán.",
                "Gia goc bang Gia ban (200000/200000) phai bao loi duoi o Gia goc");
        Assert.assertFalse(adminPage.isDiscountBadgeDisplayed(),
                "Gia goc bang Gia ban thi khong duoc hien badge % giam");

        adminPage.setProductPrice("250000");
        adminPage.setProductOriginalPrice("200000");
        adminPage.clickProductSaveButton();
        adminPage.sleepInSecond(1);
        Assert.assertEquals(adminPage.getProductFieldErrorText("original-price"), "Giá gốc phải lớn hơn giá bán.",
                "Gia goc (200000) nho hon Gia ban (250000) phai bao loi duoi o Gia goc");
        Assert.assertFalse(adminPage.isDiscountBadgeDisplayed(),
                "Gia goc nho hon Gia ban thi khong duoc hien badge % giam");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Luu that bai do gia goc khong hop le thi form phai van mo (khong dong, khong tao SP)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-63 (TC_SCRA04_FUNC_042): Nút Lưu ở form sửa bị DISABLE khi chưa sửa gì — mở form sửa 1 SP có sẵn thì nút 'Lưu thay đổi' bị disable; đổi tên thì nút bật lên; trả tên về đúng giá trị ban đầu thì nút lại disable; đóng bằng Hủy, không lưu")
    public void adminEditProductSaveDisabledWhenNotDirty() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        String productName = adminPage.getFirstDetailButtonLabel(
                AdminPageUI.FIRST_PRODUCT_DETAIL_BTN, "Xem chi tiết sản phẩm ");
        if (productName == null || productName.isEmpty()) {
            throw new SkipException("Bo qua: muc San pham khong co dong nao de mo form sua");
        }

        adminPage.openProductEditForm(productName);
        Assert.assertFalse(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN),
                "Mo form sua SP ma chua sua gi thi nut 'Lưu thay đổi' phai bi disable");
        adminPage.setProductName(productName + "-SUA");
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN),
                "Doi ten SP trong form sua thi nut 'Lưu thay đổi' phai bat len");
        adminPage.setProductName(productName);
        Assert.assertFalse(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_SAVE_BTN),
                "Tra ten SP ve dung gia tri ban dau thi nut 'Lưu thay đổi' phai lai disable");

        // Form sach nen bam Hủy dong thang, khong hoi dirty-close.
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form sua san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-64 (TC_SCRA04_FUNC_045, đợt 21): Đã sửa mà bấm Hủy thì hỏi \"Thoát mà chưa lưu?\" — nhập tên 'Son mới' rồi bấm Hủy thì popup đúng 2 nút [Ở lại][Thoát]; bấm 'Ở lại' thì popup đóng, trang form vẫn mở, dữ liệu 'Son mới' còn nguyên; bấm Hủy rồi 'Thoát' thì về danh sách, không lưu, không tạo sản phẩm")
    public void adminProductFormDirtyCancelShowsDiscardConfirm() {
        AdminPageObject adminPage = openCreateForm();
        adminPage.setProductName("Son mới");
        adminPage.clickToElement(AdminPageUI.ADMIN_PRODUCT_CANCEL_BTN);
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam Hủy khi form da sua thi phai hien hop 'Thoát mà chưa lưu?'");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Thoát mà chưa lưu?",
                "Hop dong form dirty phai co tieu de 'Thoát mà chưa lưu?'");
        Assert.assertTrue(adminPage.getConfirmDialogText().contains("Các thay đổi bạn vừa nhập sẽ bị mất."),
                "Hop dong form dirty phai co noi dung 'Các thay đổi bạn vừa nhập sẽ bị mất.'");
        Assert.assertEquals(adminPage.getConfirmDialogFooterButtonCount(), 2,
                "Hop dong form dirty dot 18 phai CHI co dung 2 nut ([Ở lại] + [Thoát])");
        Assert.assertEquals(adminPage.getConfirmDialogCancelText(), "Ở lại",
                "Nut trai hop dong form dirty phai la 'Ở lại'");
        Assert.assertEquals(adminPage.getConfirmDialogConfirmText(), "Thoát",
                "Nut phai hop dong form dirty phai la 'Thoát'");

        // Nhanh 'Ở lại': popup dong, trang form van mo, du lieu con nguyen.
        adminPage.clickConfirmNo();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam 'Ở lại' xong hop xac nhan phai bien mat");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam 'Ở lại' xong trang form san pham van phai mo");
        Assert.assertEquals(adminPage.getProductNameInputValue(), "Son mới",
                "Bam 'Ở lại' xong du lieu 'Son mới' trong form phai con nguyen");

        // Nhanh 'Thoát': ve danh sach, khong luu, khong tao san pham.
        adminPage.clickToElement(AdminPageUI.ADMIN_PRODUCT_CANCEL_BTN);
        adminPage.clickConfirmDialogConfirm();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam Hủy roi 'Thoát' xong form san pham phai dong (ve danh sach)");
        adminPage.searchProducts("Son mới");
        Assert.assertFalse(adminPage.isRowWithTextDisplayed("Son mới"),
                "Thoat form chua luu thi khong duoc tao san pham 'Son mới'");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-65 (TC_SCRA04_FUNC_046/047, đợt 21): Bấm ESC khi chưa sửa thì đóng trang form ngay, không hỏi; bấm ESC khi đã sửa thì hỏi \"Thoát mà chưa lưu?\" thay vì đóng ngay — bấm 'Ở lại' thì trang form vẫn mở, dữ liệu còn nguyên; bấm 'Thoát' thì về danh sách, không lưu")
    public void adminProductFormEscClosesWhenClean() {
        AdminPageObject adminPage = openCreateForm();
        // Form sach: ESC dong ngay, khong hoi.
        adminPage.pressEscapeKey();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam ESC khi form chua sua thi trang form phai dong ngay, khong hoi");
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam ESC khi form chua sua thi khong duoc hien hop xac nhan");

        // Form dirty: ESC hien hop 'Thoát mà chưa lưu?' thay vi dong ngay.
        adminPage.openProductFormModal();
        adminPage.setProductName("Son mới");
        adminPage.pressEscapeKey();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam ESC khi form da sua thi phai hien hop 'Thoát mà chưa lưu?' thay vi dong ngay");
        Assert.assertEquals(adminPage.getConfirmDialogTitle(), "Thoát mà chưa lưu?",
                "Hop hien khi bam ESC o form dirty phai co tieu de 'Thoát mà chưa lưu?'");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam ESC khi form da sua thi trang form van phai mo (cho xac nhan)");

        adminPage.clickConfirmNo();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isElementDisplayed(AdminPageUI.CONFIRM_DIALOG),
                "Bam 'Ở lại' xong hop xac nhan phai bien mat");
        Assert.assertTrue(adminPage.isProductFormModalDisplayed(),
                "Bam 'Ở lại' xong trang form san pham van phai mo");
        Assert.assertEquals(adminPage.getProductNameInputValue(), "Son mới",
                "Bam 'Ở lại' xong du lieu 'Son mới' trong form phai con nguyen");

        adminPage.pressEscapeKey();
        Assert.assertTrue(adminPage.isConfirmDialogDisplayed(),
                "Bam ESC lan 2 khi form da sua thi phai hien lai hop 'Thoát mà chưa lưu?'");
        adminPage.clickConfirmDialogConfirm();
        adminPage.sleepInSecond(1);
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam ESC roi 'Thoát' xong form san pham phai dong (ve danh sach)");
    }
}
