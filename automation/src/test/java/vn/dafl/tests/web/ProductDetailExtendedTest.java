package vn.dafl.tests.web;

import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.List;

/**
 * Pha B (dot 15 buoc 3) — case loai (c) sheet SCR_05 Chi tiet san pham.
 *
 * Luu y ve ky vong manual loi thoi (TC_SCR5_FUNC_015 / TC_SCR5_FUNC_016):
 * file manual v15.1 ghi "khong co form gui danh gia" nhung form gui danh gia
 * DA CO tu dot 15 (RV-01, Wave15FeaturesTest) va LUON hien tren trang chi tiet.
 * Cac test duoi day viet theo GIAO DIEN HIEN TAI: khang dinh form LUON hien
 * (ca khi SP chua co danh gia nao), khong khang dinh "khong co form".
 *
 * Cac test gui danh gia that (RV-06/07/08) dung binh luan kem timestamp de
 * phan biet khi chay lap; khong co chuc nang xoa danh gia qua UI nen du lieu
 * nay o lai trong DB (nhu RV-04).
 */
@Feature("Chi tiết sản phẩm")
public class ProductDetailExtendedTest extends BaseTest {

    /** Mo trang chi tiet cua san pham dau tien tren trang chu. */
    private ProductCatalogPageObject openFirstProductDetail() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());
        return catalog;
    }

    /** Tim san pham het hang (quet toi da 9 trang nhu ProductDetailTest); null neu seed khong co. */
    private String findOutOfStockProductId(ProductCatalogPageObject catalog) {
        for (int page = 1; page <= 9; page++) {
            for (String productId : catalog.getDisplayedProductIds()) {
                if ("Hết hàng".equals(catalog.getProductStockText(productId).trim())) {
                    return productId;
                }
            }
            if (catalog.isNextPageButtonEnabled()) {
                catalog.goToNextCatalogPage();
            } else {
                break;
            }
        }
        return null;
    }

    /** Mo thang trang chi tiet bang URL (dung khi can thu nhieu san pham lien tiep). */
    private ProductCatalogPageObject openProductDetailById(String productId) {
        getDriver().get(baseUrl + "/product/" + productId);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet san pham " + productId);
        return catalog;
    }

    /** Doc duong dan quay lai sau dang nhap (inventory §4/§5.1: sessionStorage,
     *  mapping ghi localStorage — doc sessionStorage truoc, localStorage sau). */
    private String readReturnRoute() {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        Object fromSession = js.executeScript("return window.sessionStorage.getItem('dafl_return_route');");
        if (fromSession != null) {
            return String.valueOf(fromSession);
        }
        Object fromLocal = js.executeScript("return window.localStorage.getItem('dafl_return_route');");
        return fromLocal == null ? null : String.valueOf(fromLocal);
    }

    /** Binh luan kem timestamp de khong trung khi chay lap (tranh rac du lieu trung lap). */
    private String timestampedComment(String base) {
        return base + " — tự động " + System.currentTimeMillis();
    }

    @Story("Thông tin sản phẩm")
    @Test(description = "TC_SCR5_UI_004: Trang chi tiết hiển thị đủ 4 dòng trust badge dưới gallery")
    public void trustBadgesDisplayedUnderGallery() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet san pham");
        Assert.assertTrue(isTextVisibleOnPage("Hàng chính hãng 100%"), "Phai thay trust badge 'Hàng chính hãng 100%'");
        Assert.assertTrue(isTextVisibleOnPage("Đổi trả trong 7 ngày"), "Phai thay trust badge 'Đổi trả trong 7 ngày'");
        Assert.assertTrue(isTextVisibleOnPage("Bảo hành 12 tháng"), "Phai thay trust badge 'Bảo hành 12 tháng'");
        Assert.assertTrue(isTextVisibleOnPage("Giao hàng toàn quốc"), "Phai thay trust badge 'Giao hàng toàn quốc'");
    }

    @Story("Biến thể")
    @Test(description = "TC_SCR5_FUNC_005: Khu vực biến thể chỉ có 1 nút duy nhất 'Mặc định'")
    public void variantSectionHasOnlyOneDefaultOption() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet san pham");
        Assert.assertEquals(catalog.getVariantOptionCount(), 1,
                "Chi co dung 1 nut bien the (BE chi co 1 bien the cho moi san pham)");
        List<String> labels = catalog.getVariantOptionLabels();
        Assert.assertEquals(labels.get(0), "Mặc định", "Nut bien the duy nhat phai co nhan 'Mặc định'");
    }

    @Story("Biến thể")
    @Test(description = "TC_SCR5_UI_006: Nút biến thể 'Mặc định' bị disable khi sản phẩm hết hàng")
    public void variantOptionDisabledWhenOutOfStock() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        String outOfStockId = findOutOfStockProductId(catalog);
        if (outOfStockId == null) {
            throw new SkipException("Du lieu seed hien tai khong co san pham het hang");
        }

        catalog.openProductDetail(outOfStockId);
        Assert.assertTrue(catalog.isOutOfStockNoticeDisplayed(), "Phai hien thong bao het hang");
        Assert.assertTrue(catalog.getVariantOptionCount() >= 1, "Phai thay nut bien the tren trang chi tiet");
        Assert.assertFalse(catalog.isFirstVariantOptionEnabled(),
                "Nut bien the 'Mặc định' phai bi disable khi san pham het hang");
    }

    @Story("Mua ngay")
    @Test(description = "TC_SCR5_FUNC_013: Khách vãng lai bấm Mua ngay -> thêm giỏ rồi chuyển sang /login, đã lưu dafl_return_route=/cart")
    public void buyNowAsGuestGoesToLoginWithReturnRoute() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.openProductDetail(productId);
        if (catalog.getDetailAvailableStock() <= 0) {
            throw new SkipException("San pham dau tien dang het hang, khong bam Mua ngay duoc");
        }

        catalog.buyNowFromDetail();
        sleepInSecond(1);

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Khach bam Mua ngay phai chuyen sang /login, dang thay: " + getDriver().getCurrentUrl());
        Assert.assertEquals(readReturnRoute(), "/cart",
                "Phai luu dafl_return_route=/cart de quay lai gio sau khi dang nhap");
        Object guestCart = ((JavascriptExecutor) getDriver())
                .executeScript("return window.localStorage.getItem('dafl_guest_cart');");
        Assert.assertTrue(guestCart != null && String.valueOf(guestCart).contains(productId),
                "San pham mua ngay phai da duoc them vao gio khach (dafl_guest_cart)");
        Assert.assertNotNull(homePage, "Mo trang chu thanh cong truoc khi bam Mua ngay");
    }

    @Story("Đánh giá")
    @Test(description = "RV-09 TC_SCR5_FUNC_015: Mục đánh giá hiển thị tóm tắt điểm TB, phân bố sao và từng đánh giá (form gửi LUÔN hiển thị từ đợt 15 — kỳ vọng manual cũ 'không có form gửi' đã lỗi thời)")
    public void reviewsSectionShowsSummaryDistributionAndItems() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        homePage.searchProduct("Son kem 3CE");

        String targetId = null;
        for (String productId : catalog.getDisplayedProductIds()) {
            if (catalog.getProductName(productId).contains("3CE Velvet")) {
                targetId = productId;
                break;
            }
        }
        Assert.assertNotNull(targetId, "Phai tim thay san pham Son kem 3CE Velvet trong ket qua tim kiem");
        catalog.openProductDetail(targetId);

        Assert.assertTrue(catalog.isReviewsSectionDisplayed(), "Phai thay muc 'Đánh giá của khách hàng'");
        int reviewCount = catalog.getReviewItemCount();
        if (reviewCount == 0) {
            throw new SkipException("DB hien tai khong co danh gia nao cho Son kem 3CE "
                    + "(seed chi tao danh gia mau khi bang Evaluations trong)");
        }
        Assert.assertTrue(catalog.isRatingSummaryDisplayed(), "Phai hien tom tat diem trung binh");
        String summary = catalog.getRatingSummaryText();
        Assert.assertTrue(summary.matches("(?s).*\\d\\.\\d.*"),
                "Tom tat phai ghi diem trung binh dang X.Y, dang thay: " + summary);
        Assert.assertTrue(summary.contains("đánh giá"),
                "Tom tat phai ghi tong so danh gia, dang thay: " + summary);
        Assert.assertTrue(summary.contains(String.valueOf(reviewCount) + " đánh giá")
                        || catalog.getRatingSummaryReviewCount() == reviewCount,
                "Tong so trong tom tat phai khop so danh gia dang hien (" + reviewCount + "), dang thay: " + summary);
        String allReviews = catalog.getAllReviewsText();
        Assert.assertFalse(allReviews.trim().isEmpty(), "Danh sach danh gia phai co noi dung");
        // Tung danh gia: ten + sao + ngay vi-VN + noi dung (ngay dang dd/MM/yyyy).
        Assert.assertTrue(allReviews.matches("(?s).*\\d{1,2}/\\d{1,2}/\\d{4}.*"),
                "It nhat 1 danh gia phai ghi ngay dang dinh dang vi-VN (dd/MM/yyyy)");
        // Ky vong manual cu "khong co form gui" DA LOI THOI: form danh gia luon hien tu dot 15 (RV-01).
        Assert.assertTrue(catalog.isReviewFormDisplayed(),
                "Form gui danh gia phai LUON hien trong muc danh gia tu dot 15 (RV-01)");
    }

    @Story("Đánh giá")
    @Test(description = "RV-10 TC_SCR5_FUNC_016: Sản phẩm chưa có đánh giá vẫn hiển thị form gửi đánh giá (kỳ vọng manual cũ 'không có form gửi' đã lỗi thời sau đợt 15)")
    public void reviewFormDisplayedOnProductWithoutReviews() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = catalog.getDisplayedProductIds();
        Assert.assertFalse(ids.isEmpty(), "Phai co san pham tren trang chu");

        String noReviewId = null;
        int checked = 0;
        for (String productId : ids) {
            if (checked >= 6) {
                break;
            }
            checked++;
            ProductCatalogPageObject detail = openProductDetailById(productId);
            if (detail.getReviewItemCount() == 0) {
                noReviewId = productId;
                catalog = detail;
                break;
            }
        }
        if (noReviewId == null) {
            throw new SkipException("6 san pham dau tien deu da co danh gia — khong tim duoc SP chua co danh gia de kiem tra trang thai rong");
        }

        Assert.assertTrue(catalog.isReviewsSectionDisplayed(), "Phai thay muc 'Đánh giá của khách hàng'");
        Assert.assertTrue(catalog.isReviewsEmptyStateDisplayed(),
                "SP chua co danh gia phai hien dong 'Chưa có đánh giá nào'");
        Assert.assertTrue(catalog.isReviewFormDisplayed(),
                "Form gui danh gia phai hien ngay ca khi SP chua co danh gia nao (RV-01, dot 15)");
    }

    @Story("Đánh giá")
    @Test(description = "RV-05 TC_SCR5_VAL_023: Chọn 4 sao, nhập bình luận 1001 ký tự rồi gửi -> lỗi 'Nội dung đánh giá tối đa 1000 ký tự.'")
    public void submitReviewWithTooLongCommentShowsError() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        String longComment = "a".repeat(1001);
        catalog.clickReviewStar(4);
        catalog.setReviewerName("Người dùng Wave15");
        catalog.setReviewComment(longComment);
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewErrorDisplayed(), "Phai hien loi khi binh luan vuot 1000 ky tu");
        Assert.assertEquals(catalog.getReviewErrorMessage(), "Nội dung đánh giá tối đa 1000 ký tự.",
                "Loi phai dung nguyen van cau gioi han 1000 ky tu");
    }

    @Story("Đánh giá")
    @Test(description = "RV-06 TC_SCR5_FUNC_024: Đã đăng nhập gửi đánh giá 5 sao thành công -> hiện 'Cảm ơn bạn đã gửi đánh giá cho sản phẩm!', form được xóa, tải lại thấy đánh giá mới")
    public void submitReviewSuccessfullyWhenLoggedIn() {
        loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        String comment = timestampedComment("Son đẹp, màu chuẩn.");
        catalog.clickReviewStar(5);
        catalog.setReviewerName(GlobalConstants.DEMO_FULL_NAME);
        catalog.setReviewComment(comment);
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewSuccessDisplayed(), "Phai hien thong bao thanh cong sau khi gui danh gia");
        Assert.assertEquals(catalog.getReviewSuccessMessage(), "Cảm ơn bạn đã gửi đánh giá cho sản phẩm!",
                "Thong bao thanh cong phai dung nguyen van");
        Assert.assertEquals(catalog.getReviewCommentValue(), "",
                "Form phai duoc xoa sau khi gui thanh cong (o binh luan trong)");

        getDriver().navigate().refresh();
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Tai lai xong phai thay trang chi tiet");
        sleepInSecond(2); // cho danh sach danh gia tai xong
        Assert.assertTrue(catalog.getAllReviewsText().contains(comment),
                "Danh gia moi phai xuat hien trong danh sach sau khi tai lai");
    }

    @Story("Đánh giá")
    @Test(description = "RV-07 TC_SCR5_FUNC_025: Khách vãng lai để trống ô tên, gửi 4 sao + bình luận -> thành công, đánh giá mới hiển thị tên 'Khách hàng DAFL'")
    public void guestSubmitReviewWithoutNameDefaultsToGuestName() {
        ProductCatalogPageObject catalog = openFirstProductDetail();

        String comment = timestampedComment("Dùng tốt.");
        catalog.clickReviewStar(4);
        catalog.setReviewerName(""); // de trong o ten — BE mac dinh "Khach hang DAFL"
        catalog.setReviewComment(comment);
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewSuccessDisplayed(), "Phai hien thong bao thanh cong sau khi gui danh gia");
        Assert.assertEquals(catalog.getReviewSuccessMessage(), "Cảm ơn bạn đã gửi đánh giá cho sản phẩm!",
                "Thong bao thanh cong phai dung nguyen van");

        getDriver().navigate().refresh();
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Tai lai xong phai thay trang chi tiet");
        sleepInSecond(2); // cho danh sach danh gia tai xong
        String allReviews = catalog.getAllReviewsText();
        Assert.assertTrue(allReviews.contains(comment),
                "Danh gia moi phai xuat hien trong danh sach sau khi tai lai");
        Assert.assertTrue(allReviews.contains("Khách hàng DAFL"),
                "Danh gia gui khi de trong o ten phai hien ten mac dinh 'Khách hàng DAFL'");
    }

    @Story("Đánh giá")
    @Test(description = "RV-08 TC_SCR5_FUNC_028: Ghi điểm TB + số lượng trước khi gửi, gửi 5 sao thành công -> đánh giá mới xuất hiện, điểm TB và số lượng được tính lại")
    public void averageRatingAndCountUpdateAfterSubmittingReview() {
        loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        int countBefore = catalog.getRatingSummaryReviewCount();
        double avgBefore = catalog.getRatingSummaryAverage();
        if (countBefore < 0) {
            // Chua co tom tat: chi chap nhan khi that su chua co danh gia nao.
            if (catalog.getReviewItemCount() > 0) {
                throw new SkipException("SP co danh gia nhung khong doc duoc tom tat diem TB/so luong");
            }
            countBefore = 0;
            avgBefore = -1;
        }

        String comment = timestampedComment("Son đẹp, màu chuẩn.");
        catalog.clickReviewStar(5);
        catalog.setReviewerName(GlobalConstants.DEMO_FULL_NAME);
        catalog.setReviewComment(comment);
        catalog.clickSubmitReview();

        Assert.assertTrue(catalog.isReviewSuccessDisplayed(), "Phai hien thong bao thanh cong sau khi gui danh gia");

        getDriver().navigate().refresh();
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Tai lai xong phai thay trang chi tiet");
        sleepInSecond(2); // cho danh sach danh gia + tom tat tai xong
        Assert.assertTrue(catalog.getAllReviewsText().contains(comment),
                "Danh gia moi phai xuat hien trong danh sach sau khi tai lai");

        int countAfter = catalog.getRatingSummaryReviewCount();
        Assert.assertEquals(countAfter, countBefore + 1,
                "So luong danh gia phai tang dung 1 sau khi gui (" + countBefore + " -> " + countAfter + ")");
        double avgAfter = catalog.getRatingSummaryAverage();
        double expectedAvg = countBefore == 0
                ? 5.0
                : (avgBefore * countBefore + 5.0) / (countBefore + 1);
        Assert.assertTrue(Math.abs(avgAfter - expectedAvg) < 0.06,
                "Diem TB phai duoc tinh lai: ky vong ~" + expectedAvg + " (tu " + avgBefore + " x " + countBefore
                        + " + 5 sao), dang thay: " + avgAfter);
    }
}
