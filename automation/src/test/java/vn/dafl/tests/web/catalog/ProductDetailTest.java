package vn.dafl.tests.web.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

/**
 * CN-05 Chi tiet san pham: thong tin, chon so luong theo bien ton kho,
 * them gio / mua ngay, san pham het hang va lien ket khong ton tai.
 */
@Feature("Chi tiết sản phẩm")
public class ProductDetailTest extends BaseTest {

    @Story("Thông tin sản phẩm")
    @Test(description = "CN-05.1: Trang chi tiết hiện đầy đủ tên, giá, tồn kho và số lượng mặc định là 1")
    public void productDetailDisplaysFullInformation() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        String productName = catalog.getProductName(productId);

        catalog.openProductDetail(productId);

        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet");
        Assert.assertTrue(isTextVisibleOnPage(productName), "Phai hien dung ten san pham");
        Assert.assertEquals(catalog.getDetailQuantity(), 1, "So luong mac dinh phai la 1");
        Assert.assertTrue(catalog.getDetailAvailableStock() >= 0, "Phai doc duoc dong ton kho '(Con X san pham co san)'");
    }

    @Story("Chọn số lượng")
    @Test(description = "TC-CN05-01 (CN-05 E1): Số lượng không thể giảm xuống dưới 1 (nút trừ bị khóa ở mức 1)")
    public void quantityCannotGoBelowOne() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        Assert.assertEquals(catalog.getDetailQuantity(), 1, "So luong khoi tao phai la 1");
        Assert.assertFalse(catalog.isDetailMinusEnabled(), "Nut tru phai bi khoa khi so luong dang la 1");
    }

    @Story("Chọn số lượng")
    @Test(description = "TC-CN05-02 (CN-05 E2): Tăng số lượng bị kẹp ở đúng tồn kho, nút cộng bị khóa khi chạm trần")
    public void quantityClampsAtAvailableStock() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        int stock = catalog.getDetailAvailableStock();
        if (stock <= 0 || stock > 25) {
            throw new SkipException("Ton kho " + stock + " khong phu hop de bam nut cong nhieu lan");
        }
        for (int i = 0; i < stock + 3; i++) {
            if (catalog.isDetailPlusEnabled()) {
                catalog.increaseDetailQuantity();
            }
        }
        Assert.assertEquals(catalog.getDetailQuantity(), stock, "So luong phai dung o dung ton kho");
        Assert.assertFalse(catalog.isDetailPlusEnabled(), "Nut cong phai bi khoa khi da cham tran ton kho");
    }

    @Story("Thêm vào giỏ và mua ngay")
    @Test(description = "CN-05.2: Chọn số lượng 3 ở trang chi tiết rồi thêm vào giỏ (chế độ khách)")
    public void addToCartFromDetailWithChosenQuantity() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.openProductDetail(productId);
        if (catalog.getDetailAvailableStock() < 3) {
            throw new SkipException("Ton kho khong du 3 de test them so luong 3");
        }

        catalog.increaseDetailQuantity();
        catalog.increaseDetailQuantity();
        Assert.assertEquals(catalog.getDetailQuantity(), 3, "So luong da chon phai la 3");
        catalog.addToCartFromDetail();
        homePage.waitForCartBadgeCount(3);

        CartDrawerPageObject cart = homePage.openCart();
        Assert.assertEquals(cart.getItemQuantity(productId), 3, "Gio phai nhan dung so luong da chon o chi tiet");
    }

    @Story("Thêm vào giỏ và mua ngay")
    @Test(description = "CN-05.2: Mua ngay khi đã đăng nhập -> thêm giỏ và chuyển thẳng sang trang thanh toán")
    public void buyNowAddsItemAndGoesToCheckout() {
        HomePageObject homePage = loginWithDemoAccount();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.openProductDetail(productId);

        catalog.buyNowFromDetail();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Mua ngay phai chuyen sang trang gio/thanh toan");
        CheckoutPageObject checkout = new CheckoutPageObject();
        Assert.assertTrue(checkout.isCheckoutFormDisplayed(), "Phai thay form thanh toan sau Mua ngay");
        CartDrawerPageObject cart = new CartDrawerPageObject();
        Assert.assertTrue(cart.isItemDisplayed(productId), "San pham mua ngay phai nam trong gio");
    }

    @Story("Hết hàng")
    @Test(description = "TC-CN05-03: Sản phẩm hết hàng -> hiện thông báo hết hàng, khóa cả hai nút mua (bỏ qua nếu seed không có)")
    public void outOfStockProductDetailDisablesPurchase() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        String outOfStockId = null;
        for (int page = 1; page <= 9 && outOfStockId == null; page++) {
            for (String productId : catalog.getDisplayedProductIds()) {
                if ("Hết hàng".equals(catalog.getProductStockText(productId).trim())) {
                    outOfStockId = productId;
                    break;
                }
            }
            if (outOfStockId == null && catalog.isNextPageButtonEnabled()) {
                catalog.goToNextCatalogPage();
            }
        }
        if (outOfStockId == null) {
            throw new SkipException("Du lieu seed hien tai khong co san pham het hang");
        }

        catalog.openProductDetail(outOfStockId);
        Assert.assertTrue(catalog.isOutOfStockNoticeDisplayed(), "Phai hien thong bao het hang");
        Assert.assertFalse(catalog.isDetailAddToCartEnabled(), "Nut them gio phai bi khoa khi het hang");
        Assert.assertFalse(catalog.isBuyNowEnabled(), "Nut mua ngay phai bi khoa khi het hang");
    }

    @Story("Sản phẩm không tồn tại")
    @Test(description = "TC-CN05-04: Mở liên kết sản phẩm không tồn tại -> trang 404 của SUT")
    public void nonExistentProductShows404() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/product/00000000-0000-0000-0000-000000000000");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        Assert.assertTrue(catalog.isProductNotFoundDisplayed(),
                "San pham khong ton tai phai hien trang '404 - Khong tim thay san pham'");
    }

    @Story("Đánh giá")
    @Test(description = "CN-05 (đợt 4): Trang chi tiết luôn hiện mục Đánh giá - có đánh giá thì hiện tóm tắt + danh sách, chưa có thì hiện trạng thái rỗng")
    public void productDetailShowsReviewsSection() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai thay trang chi tiet");
        Assert.assertTrue(catalog.isReviewsSectionDisplayed(),
                "Trang chi tiet phai co muc Danh gia cua khach hang (product-reviews-section)");
        Assert.assertTrue(isTextVisibleOnPage("Đánh giá của khách hàng"),
                "Muc danh gia phai co tieu de 'Đánh giá của khách hàng'");

        if (catalog.getReviewItemCount() > 0) {
            Assert.assertTrue(catalog.isRatingSummaryDisplayed(),
                    "San pham co danh gia phai hien tom tat diem trung binh (product-rating-summary)");
            Assert.assertTrue(catalog.getRatingSummaryText().matches("(?s).*\\d\\.\\d.*"),
                    "Tom tat phai ghi diem trung binh dang X.Y, dang thay: " + catalog.getRatingSummaryText());
        } else {
            Assert.assertTrue(catalog.isReviewsEmptyStateDisplayed(),
                    "San pham chua co danh gia phai hien dong 'Chưa có đánh giá nào'");
        }
    }

    @Story("Đánh giá")
    @Test(description = "CN-05 (đợt 4): Sản phẩm seed có đánh giá mẫu (Son kem 3CE) -> tóm tắt 5.0 + 1 đánh giá của Trần Hoàng Nam")
    public void seededProductShowsSampleReviewWithAverageRating() {
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

        Assert.assertTrue(catalog.isReviewsSectionDisplayed(), "Phai thay muc danh gia tren trang chi tiet");
        if (catalog.getReviewItemCount() == 0) {
            // Seed chi tao danh gia mau khi bang Evaluations trong luc seed; DB cu da co
            // danh gia cu thi san pham my pham moi co the chua co danh gia nao.
            throw new SkipException("Bo qua: DB hien tai khong co danh gia mau cho Son kem 3CE "
                    + "(seed chi tao khi bang Evaluations trong)");
        }
        Assert.assertEquals(catalog.getReviewItemCount(), 1,
                "Son kem 3CE chi co dung 1 danh gia mau trong seed");
        Assert.assertTrue(catalog.isRatingSummaryDisplayed(), "Phai hien tom tat diem trung binh");
        Assert.assertTrue(catalog.getRatingSummaryText().contains("5.0"),
                "Diem trung binh cua 1 danh gia 5 sao phai la 5.0, dang thay: " + catalog.getRatingSummaryText());
        String reviewsText = catalog.getAllReviewsText();
        Assert.assertTrue(reviewsText.contains("Trần Hoàng Nam"),
                "Danh gia mau phai cua Tran Hoang Nam, dang thay: " + reviewsText);
        Assert.assertTrue(reviewsText.contains("Son 3CE lên màu chuẩn đẹp"),
                "Danh gia mau phai co binh luan goc cua seed");
    }
}
