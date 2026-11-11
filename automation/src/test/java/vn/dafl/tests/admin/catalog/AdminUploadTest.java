package vn.dafl.tests.admin.catalog;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AdminPageObject;
import vn.dafl.pageUIs.AdminPageUI;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Test upload anh san pham o Admin rieng (dot redesign form admin + upload anh).
 * Form san pham dot 18 la DRAWER (admin-product-form-modal, class fs-drawer,
 * overlay — bang danh sach VAN O LAI trong DOM) co dropzone upload anh
 * (admin-product-image-dropzone) + input file THAT (admin-product-image-input):
 * test gui duong dan tuyet doi vao input file bang sendKeys (khong click truoc).
 * File anh test that nam o automation/src/test/resources/upload/ (2 anh PNG 1x1
 * hop le + 1 file txt sai dinh dang); file anh > 5MB duoc tao luc chay test
 * (them padding vao ban sao anh PNG hop le, KHONG commit file 5MB vao repo).
 * Chi dung du lieu do test tao (ten SP kem timestamp) va tu xoa sau khi chay
 * (xoa SP dang Hoat dong tu xu ly popup chan xoa o AdminPageObject).
 * UP-03 khang dinh anh chinh la anh thu 2 bang cach so sanh URL: nhan "Anh chinh"
 * tren thumbnail 0 (truoc/sau khi luu) + src anh thu 0 trong form XEM phai TRUNG
 * src thumbnail 0 khi mo lai form sua. Backend doi ten file upload thanh UUID
 * (POST /api/admin/upload) nen KHONG khang dinh bang ten file goc duoc.
 * Test tu bo qua (skip, khong tinh fail) khi service admin chua chay hoac tai
 * khoan admin@dafl.vn chua duoc seed tren DB nay.
 * Toan bo lop nay chi chay tren app Admin rieng, khong mo web ban hang.
 */
@Feature("Tải ảnh lên")
public class AdminUploadTest extends BaseTest {

    private static final String IMAGE_1 = "upload/test-product-1.png";
    private static final String IMAGE_2 = "upload/test-product-2.png";
    private static final String INVALID_FILE = "upload/invalid-file.txt";
    /** Gioi han dung luong anh FE chap nhan (theo hop dong dot redesign). */
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    private String createdProductName;
    private boolean adminLoggedIn = false;

    /** Ten san pham duy nhat theo tung lan chay (kem timestamp). */
    private String uniqueProductName() {
        return "TESTSP-IMG-" + System.currentTimeMillis();
    }

    /** Dang nhap Super Admin tren app Admin rieng; bo qua test neu chua san sang. */
    private AdminPageObject loginAsAdminOrSkip() {
        AdminPageObject adminPage = new AdminPageObject();
        adminPage.loginAsAdminOnAdminAppOrSkip();
        adminLoggedIn = true;
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
            Path oversize = Paths.get(System.getProperty("java.io.tmpdir"), "dafl-oversize-test.png");
            byte[] padded = new byte[(int) (MAX_IMAGE_BYTES + 1)];
            System.arraycopy(pngBytes, 0, padded, 0, pngBytes.length);
            Files.write(oversize, padded);
            return oversize.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new AssertionError("Khong tao duoc file anh > 5MB de test: " + e.getMessage());
        }
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

    @Story("Sản phẩm")
    @Test(description = "UP-01 (đợt 18, form Drawer + form xem): Tạo SP mới upload 1 ảnh qua input file → lưu xong SP hiện trong bảng có thumbnail + mở form xem thấy ảnh")
    public void adminCreateProductWithOneUploadedImage() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductForm(productName, categoryName, "99000", "", "10",
                    "San pham test upload anh UP-01");
            Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                    "Trang form them san pham phai co dropzone upload anh (admin-product-image-dropzone)");

            adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
            Assert.assertTrue(adminPage.isProductImageThumbDisplayed(0),
                    "Upload anh xong phai thay thumbnail anh thu 0 (admin-product-image-thumb-0)");
            Assert.assertFalse(adminPage.isProductImageErrorDisplayed(),
                    "Upload anh PNG hop le khong duoc hien loi upload anh");

            adminPage.saveProductForm();
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Luu san pham co anh xong phai hien toast thanh cong (admin-toast)");

            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            Assert.assertTrue(adminPage.isProductRowImageDisplayed(productName),
                    "Dong san pham vua tao phai hien thumbnail anh trong bang");

            // Mo form xem cua dong vua tim thay (dong dau sau khi loc): SP chi co
            // 1 anh nen gallery hien 1 thumb (admin-product-image-thumb, khong hau to -0).
            adminPage.clickFirstProductDetail();
            Assert.assertTrue(adminPage.isProductDetailImageDisplayed(),
                    "Trang chi tiet san pham vua tao phai hien anh (<img> trong admin-product-image-thumb)");
            adminPage.closeProductDetailPage(); // dot 21: trang chi tiet full page, dong bang [Quay lại]
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "UP-02 (đợt 18, form Drawer; SỬA đợt 19 theo TC_SCRA04_FUNC_070/071): Upload file sai định dạng (.txt) và file ảnh > 5MB → bị từ chối: toast đúng loại + thumbnail ở trạng thái lỗi (nội dung lỗi + nút 'Thử lại'), 0 thumbnail ready nhưng vẫn có 1 thumbnail lỗi, không lưu kèm ảnh lỗi. LƯU Ý đợt 19: lỗi validate file KHÔNG hiện ở admin-product-image-error (addFiles không gọi setDropError cho lỗi validate — chỉ hiện trên thumbnail + toast), bản UP-02 cũ assert sai ở đó nên viết lại theo code thật (FormControls.tsx addFiles)")
    public void adminUploadInvalidAndOversizeImageIsRejectedInline() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");

        // (1) File sai dinh dang: .txt.
        adminPage.openProductFormModal();
        Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                "Trang form them san pham phai co dropzone upload anh (admin-product-image-dropzone)");
        adminPage.uploadProductImage(getTestResourcePath(INVALID_FILE));
        List<String> toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Định dạng ảnh không hỗ trợ")),
                "Upload file .txt thi phai hien toast 'Định dạng ảnh không hỗ trợ', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getProductImageThumbErrorText(0),
                "Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).",
                "Upload file .txt thi thumbnail loi phai hien 'Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).'");
        Assert.assertTrue(adminPage.isProductImageRetryButtonDisplayed(0),
                "Thumbnail anh loi phai co nut 'Thử lại'");
        Assert.assertEquals(adminPage.getProductImageReadyThumbCount(), 0,
                "Upload file .txt thi khong duoc co thumbnail anh nao o trang thai ready");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 1,
                "Upload file .txt van tao 1 thumbnail (trang thai loi) de hien loi inline");
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam Hủy xong trang form san pham phai dong (khong luu san pham loi)");

        // (2) File anh vuot 5MB (tao luc chay test, khong commit file lon vao repo).
        String oversizePath = createOversizeImagePath();
        Assert.assertTrue(new File(oversizePath).length() > MAX_IMAGE_BYTES,
                "File test vuot gioi han phai lon hon 5MB, dang la: " + new File(oversizePath).length() + " byte");
        adminPage.openProductFormModal();
        adminPage.uploadProductImage(oversizePath);
        toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Ảnh quá lớn")),
                "Upload anh > 5MB thi phai hien toast 'Ảnh quá lớn', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getProductImageThumbErrorText(0), "Ảnh vượt quá 5MB.",
                "Upload anh > 5MB thi thumbnail loi phai hien 'Ảnh vượt quá 5MB.'");
        Assert.assertTrue(adminPage.isProductImageRetryButtonDisplayed(0),
                "Thumbnail anh loi phai co nut 'Thử lại'");
        Assert.assertEquals(adminPage.getProductImageReadyThumbCount(), 0,
                "Upload anh > 5MB thi khong duoc co thumbnail anh nao o trang thai ready");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 1,
                "Upload anh > 5MB van tao 1 thumbnail (trang thai loi) de hien loi inline");
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam Hủy xong trang form san pham phai dong (khong luu san pham loi)");
    }

    @Story("Sản phẩm")
    @Test(description = "UP-03 (đợt 18, form Drawer + form xem): Upload 2 ảnh → đặt ảnh thứ 2 làm ảnh chính → lưu → mở lại thấy đủ 2 ảnh, ảnh chính là ảnh thứ 2")
    public void adminSecondUploadedImageCanBeSetAsMain() {
        String productName = uniqueProductName();
        AdminPageObject adminPage = loginAsAdminOrSkip();
        try {
            adminPage.clickAdminNav("products");
            adminPage.openProductFormModal();
            String categoryName = adminPage.getFirstRealCategoryName();
            Assert.assertNotNull(categoryName, "Phai co it nhat 1 danh muc that de chon khi tao san pham");

            adminPage.fillProductForm(productName, categoryName, "99000", "", "10",
                    "San pham test 2 anh UP-03");
            adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
            Assert.assertTrue(adminPage.isProductImageThumbDisplayed(0),
                    "Upload anh thu 1 xong phai thay thumbnail anh thu 0");
            adminPage.uploadProductImage(getTestResourcePath(IMAGE_2));
            Assert.assertTrue(adminPage.isProductImageThumbDisplayed(1),
                    "Upload anh thu 2 xong phai thay thumbnail anh thu 1");
            Assert.assertEquals(adminPage.getProductImageThumbCount(), 2,
                    "Form phai co dung 2 thumbnail sau khi upload 2 anh");

            adminPage.setMainProductImage(1);
            Assert.assertTrue(adminPage.isProductImageMainBadgeDisplayed(0),
                    "Dat anh thu 2 lam anh chinh xong, nhan 'Anh chinh' phai hien tren thumbnail 0");
            adminPage.saveProductForm();
            createdProductName = productName;
            Assert.assertTrue(adminPage.isToastDisplayed(), "Luu san pham 2 anh xong phai hien toast thanh cong (admin-toast)");

            // Mo lai form sua: ca 2 anh van con, thu tu anh giu nguyen (anh thu 2 van
            // la anh chinh o vi tri 0) — src thumbnail luc nay la URL server da luu.
            adminPage.searchProducts(productName);
            Assert.assertTrue(adminPage.isRowWithTextDisplayed(productName),
                    "Tim theo ten san pham vua tao phai thay 1 dong trong bang");
            adminPage.openProductEditForm(productName);
            Assert.assertEquals(adminPage.getProductImageThumbCount(), 2,
                    "Mo lai form sua san pham phai thay du 2 thumbnail anh da upload");
            Assert.assertTrue(adminPage.isProductImageMainBadgeDisplayed(0),
                    "Mo lai form sua: nhan 'Anh chinh' phai van o thumbnail 0 (thu tu anh da luu dung)");
            String reopenedMainSrc = adminPage.getProductImageThumbSrc(0);
            Assert.assertTrue(reopenedMainSrc != null && !reopenedMainSrc.isEmpty(),
                    "Thumbnail 0 khi mo lai form sua phai co src anh (URL server da luu)");
            adminPage.cancelProductForm();

            // Anh chinh phai la anh thu 2: anh thu 0 hien trong form XEM co src
            // TRUNG src thumbnail 0 khi mo lai form sua. SP co 2 anh nen gallery
            // dung testid co hau to so (admin-product-image-thumb-0), khong dung
            // testid don (admin-product-image-thumb chi dung khi chi co 1 anh).
            // Backend doi ten file upload thanh UUID (POST /api/admin/upload) nen
            // KHONG khang dinh bang ten file goc ('test-product-2') nua.
            adminPage.clickFirstProductDetail();
            Assert.assertTrue(adminPage.isElementDisplayedWithWait(
                            adminPage.getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_VIEW_IMAGE_IMG, "0"), 10),
                    "Trang chi tiet san pham phai hien anh thu 0 (admin-product-image-thumb-0)");
            String mainImageSrc = adminPage.getElementAttribute(
                    adminPage.getDynamicLocator(AdminPageUI.DYNAMIC_PRODUCT_VIEW_IMAGE_IMG, "0"), "src");
            Assert.assertTrue(mainImageSrc != null && mainImageSrc.equals(reopenedMainSrc),
                    "Anh chinh trong trang chi tiet phai trung src anh thumbnail 0 (anh thu 2 da dat lam chinh), dang thay: " + mainImageSrc + " vs " + reopenedMainSrc);
            adminPage.closeProductDetailPage(); // dot 21: trang chi tiet full page, dong bang [Quay lại]
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    // ---------- Wave 19: gap SCR_04 (hanh vi upload anh form SP) ----------

    @Story("Sản phẩm")
    @Test(description = "ADM19-112 (TC_SCRA04_FUNC_066, đợt 26 viết lại — nút 'Hoặc dán link ảnh' ĐÃ BỎ): Dropzone upload ảnh SP — mở form Thêm SP: dropzone hiển thị hint 'Kéo thả ảnh vào đây hoặc bấm để chọn từ máy tính' + 'PNG, JPG, WEBP – tối đa 5MB – tối đa 8 ảnh'; input file thật (admin-product-image-input) hiển thị + bật được (visually-hidden để Selenium sendKeys); KHÔNG còn nút 'Hoặc dán link ảnh'; đóng form không lưu")
    public void adminProductImageDropzoneShowsHintTextAndRealFileInput() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                "Form them SP phai co dropzone upload anh (admin-product-image-dropzone)");
        String dropzoneText = adminPage.getProductImageDropzoneText();
        Assert.assertTrue(dropzoneText.contains("Kéo thả ảnh vào đây hoặc bấm để chọn từ máy tính"),
                "Dropzone SP phai co hint 'Kéo thả ảnh vào đây hoặc bấm để chọn từ máy tính', dang thay: " + dropzoneText);
        Assert.assertTrue(dropzoneText.contains("PNG, JPG, WEBP – tối đa 5MB – tối đa 8 ảnh"),
                "Dropzone SP phai co hint 'PNG, JPG, WEBP – tối đa 5MB – tối đa 8 ảnh', dang thay: " + dropzoneText);
        Assert.assertTrue(adminPage.isElementDisplayed(AdminPageUI.ADMIN_PRODUCT_IMAGE_INPUT),
                "Form them SP phai co input file that (admin-product-image-input) de Selenium sendKeys");
        Assert.assertTrue(adminPage.isElementEnabled(AdminPageUI.ADMIN_PRODUCT_IMAGE_INPUT),
                "Input file anh SP phai bat duoc (khong disabled)");
        Assert.assertFalse(adminPage.getProductFormModalText().contains("Hoặc dán link ảnh"),
                "Dot 26 da bo dan link anh — form them SP KHONG duoc con nut 'Hoặc dán link ảnh'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-113 (TC_SCRA04_FUNC_074/080, đợt 26 viết lại — dán link ảnh ĐÃ BỎ): Thêm ảnh SP bằng file upload — chọn 1 file ảnh qua dropzone thì hiện thumbnail-0 + nhận 'Ảnh chính'; form KHÔNG còn nút 'Hoặc dán link ảnh'; đóng form không lưu")
    public void adminAddProductImageByUrlAddsThumbnail() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();
        Assert.assertFalse(adminPage.getProductFormModalText().contains("Hoặc dán link ảnh"),
                "Dot 26 da bo dan link anh — form them SP KHONG duoc con nut 'Hoặc dán link ảnh'");

        adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
        Assert.assertTrue(adminPage.isProductImageThumbDisplayed(0),
                "Upload 1 file anh xong phai hien thumbnail anh thu 0");
        Assert.assertTrue(adminPage.isProductImageMainBadgeDisplayed(0),
                "Anh dau tien upload phai la anh chinh (nhan 'Ảnh chính' tren thumbnail 0)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-114 (TC_SCRA04_FUNC_075, đợt 26 viết lại — dán link ảnh ĐÃ BỎ, ca ftp link không còn): Upload file sai định dạng (.txt) → từ chối inline — chọn file .txt thì thumbnail lỗi hiện 'Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).' + toast 'Định dạng ảnh không hỗ trợ', không có thumbnail ready nào; đóng form không lưu")
    public void adminAddProductImageByInvalidUrlShowsInlineError() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();

        adminPage.uploadProductImage(getTestResourcePath(INVALID_FILE));
        java.util.List<String> toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Định dạng ảnh không hỗ trợ")),
                "Upload file .txt lam anh SP thi phai hien toast 'Định dạng ảnh không hỗ trợ', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getProductImageThumbErrorText(0), "Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).",
                "Upload file .txt lam anh SP thi thumbnail loi phai hien 'Định dạng không được hỗ trợ (chỉ PNG, JPG, WEBP).'");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-115 (TC_SCRA04_FUNC_078, đợt 26 viết lại — dán link ảnh ĐÃ BỎ): Xóa 1 ảnh khỏi danh sách thumbnail — upload 2 ảnh bằng file, bấm nút Xóa ở thumbnail 0 thì còn 1 thumbnail, ảnh còn lại trở thành ảnh chính (nhận 'Ảnh chính' ở thumbnail 0); đóng form không lưu")
    public void adminRemoveProductImageByThumbnailButton() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();

        adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
        adminPage.uploadProductImage(getTestResourcePath(IMAGE_2));
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 2,
                "Upload 2 anh bang file xong form phai co dung 2 thumbnail");
        Assert.assertTrue(adminPage.isProductImageMainBadgeDisplayed(0),
                "Truoc khi xoa, nhan 'Ảnh chính' phai o thumbnail 0 (anh dau tien)");

        adminPage.removeProductImage(0);
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 1,
                "Xoa thumbnail 0 xong form phai con dung 1 thumbnail");
        Assert.assertTrue(adminPage.isProductImageMainBadgeDisplayed(0),
                "Xoa anh chinh cu xong, anh con lai phai tro thanh anh chinh (nhan 'Ảnh chính' o thumbnail 0)");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }

    @Story("Sản phẩm")
    @Test(description = "ADM19-116 (TC_SCRA04_FUNC_079, đợt 26 viết lại — dán link ảnh ĐÃ BỎ, chỉ còn upload file): Tối đa 8 ảnh cho 1 SP — upload 8 ảnh bằng file (xen kẽ 2 file test) thì đủ 8 thumbnail; upload file thứ 9 thì lỗi inline 'Tối đa 8 ảnh.' + toast 'Đã đạt số ảnh tối đa', vẫn 8 thumbnail; đóng form không lưu")
    public void adminProductImageMaxEightLimit() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");
        adminPage.openProductFormModal();

        // Dot 26: chi con upload file — xen ke 2 file test de du 8 anh.
        for (int i = 1; i <= 8; i++) {
            adminPage.uploadProductImage(getTestResourcePath(i % 2 == 1 ? IMAGE_1 : IMAGE_2));
        }
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 8,
                "Upload 8 anh bang file xong form phai co dung 8 thumbnail");

        adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
        Assert.assertEquals(adminPage.getProductImageErrorText(), "Tối đa 8 ảnh.",
                "Upload file anh thu 9 thi phai bao loi inline 'Tối đa 8 ảnh.'");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 8,
                "Upload file anh thu 9 bi chan thi van chi co 8 thumbnail");

        adminPage.uploadProductImage(getTestResourcePath(IMAGE_1));
        java.util.List<String> toasts = adminPage.getAllToastTexts();
        Assert.assertTrue(toasts.stream().anyMatch(t -> t.contains("Đã đạt số ảnh tối đa")),
                "Chon them file anh khi da du 8 thi phai hien toast 'Đã đạt số ảnh tối đa', dang thay: " + toasts);
        Assert.assertEquals(adminPage.getProductImageErrorText(), "Tối đa 8 ảnh.",
                "Chon them file anh khi da du 8 thi phai bao loi inline 'Tối đa 8 ảnh.'");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 8,
                "Chon them file anh khi da du 8 bi chan thi van chi co 8 thumbnail");

        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Dong form bang nut Hủy xong khong duoc con thay form san pham");
    }
}
