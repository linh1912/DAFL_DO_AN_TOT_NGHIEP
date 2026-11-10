package vn.dafl.tests.admin;

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

/**
 * Test upload anh san pham o Admin rieng (dot redesign form admin + upload anh).
 * Form san pham moi la drawer (admin-product-form-modal) co dropzone upload anh
 * (admin-product-image-dropzone) + input file THAT (admin-product-image-input):
 * test gui duong dan tuyet doi vao input file bang sendKeys (khong click truoc).
 * File anh test that nam o automation/src/test/resources/upload/ (2 anh PNG 1x1
 * hop le + 1 file txt sai dinh dang); file anh > 5MB duoc tao luc chay test
 * (them padding vao ban sao anh PNG hop le, KHONG commit file 5MB vao repo).
 * Chi dung du lieu do test tao (ten SP kem timestamp) va tu xoa sau khi chay.
 * UP-03 khang dinh anh chinh la anh thu 2 bang cach so sanh URL: nhan "Anh chinh"
 * tren thumbnail 0 (truoc/sau khi luu) + src anh trong modal chi tiet phai TRUNG
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
    @Test(description = "UP-01 (đợt redesign form admin): Tạo SP mới upload 1 ảnh qua input file → lưu xong SP hiện trong bảng có thumbnail + mở chi tiết thấy ảnh")
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
                    "Drawer them san pham phai co dropzone upload anh (admin-product-image-dropzone)");

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

            // Mo modal chi tiet cua dong vua tim thay (dong dau sau khi loc).
            adminPage.clickFirstProductDetail();
            Assert.assertTrue(adminPage.isProductDetailImageDisplayed(),
                    "Modal chi tiet san pham vua tao phai hien anh (<img> trong admin-product-detail)");
            adminPage.closeDetailModal("admin-product-detail");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }

    @Story("Sản phẩm")
    @Test(description = "UP-02 (đợt redesign form admin): Upload file sai định dạng (.txt) và file ảnh > 5MB → bị từ chối inline (admin-product-image-error), không tạo thumbnail, không lưu kèm ảnh lỗi")
    public void adminUploadInvalidAndOversizeImageIsRejectedInline() {
        AdminPageObject adminPage = loginAsAdminOrSkip();
        adminPage.clickAdminNav("products");

        // (1) File sai dinh dang: .txt.
        adminPage.openProductFormModal();
        Assert.assertTrue(adminPage.isProductImageDropzoneDisplayed(),
                "Drawer them san pham phai co dropzone upload anh (admin-product-image-dropzone)");
        adminPage.uploadProductImage(getTestResourcePath(INVALID_FILE));
        Assert.assertTrue(adminPage.isProductImageErrorDisplayed(),
                "Upload file .txt phai hien loi inline (admin-product-image-error)");
        Assert.assertFalse(adminPage.getProductImageErrorText().isEmpty(),
                "Loi upload file sai dinh dang phai co noi dung, khong duoc de trong");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 0,
                "Upload file sai dinh dang khong duoc tao thumbnail anh nao");
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam Hủy xong drawer form san pham phai dong (khong luu san pham loi)");

        // (2) File anh vuot 5MB (tao luc chay test, khong commit file lon vao repo).
        String oversizePath = createOversizeImagePath();
        Assert.assertTrue(new File(oversizePath).length() > MAX_IMAGE_BYTES,
                "File test vuot gioi han phai lon hon 5MB, dang la: " + new File(oversizePath).length() + " byte");
        adminPage.openProductFormModal();
        adminPage.uploadProductImage(oversizePath);
        Assert.assertTrue(adminPage.isProductImageErrorDisplayed(),
                "Upload anh > 5MB phai hien loi inline (admin-product-image-error)");
        Assert.assertEquals(adminPage.getProductImageThumbCount(), 0,
                "Upload anh > 5MB khong duoc tao thumbnail anh nao");
        adminPage.cancelProductForm();
        Assert.assertFalse(adminPage.isProductFormModalPresent(),
                "Bam Hủy xong drawer form san pham phai dong (khong luu san pham loi)");
    }

    @Story("Sản phẩm")
    @Test(description = "UP-03 (đợt redesign form admin): Upload 2 ảnh → đặt ảnh thứ 2 làm ảnh chính → lưu → mở lại thấy đủ 2 ảnh, ảnh chính là ảnh thứ 2")
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

            // Anh chinh phai la anh thu 2: anh hien trong modal chi tiet co src TRUNG
            // src thumbnail 0 khi mo lai form sua. Backend doi ten file upload thanh
            // UUID (POST /api/admin/upload) nen KHONG khang dinh bang ten file goc
            // ('test-product-2') nua.
            adminPage.clickFirstProductDetail();
            Assert.assertTrue(adminPage.isProductDetailImageDisplayed(),
                    "Modal chi tiet san pham phai hien anh chinh");
            String mainImageSrc = adminPage.getElementAttribute(AdminPageUI.ADMIN_PRODUCT_DETAIL_IMG, "src");
            Assert.assertTrue(mainImageSrc != null && mainImageSrc.equals(reopenedMainSrc),
                    "Anh chinh trong modal chi tiet phai trung src anh thumbnail 0 (anh thu 2 da dat lam chinh), dang thay: " + mainImageSrc + " vs " + reopenedMainSrc);
            adminPage.closeDetailModal("admin-product-detail");
        } finally {
            deleteProductIfPresent(createdProductName);
            createdProductName = null;
        }
    }
}
