package vn.dafl.tests;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
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
public class ProductDetailTest extends BaseTest {

    @Test(description = "CN-05.1: Trang chi tiet hien day du ten, gia, ton kho va so luong mac dinh la 1")
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

    @Test(description = "TC-CN05-01 (CN-05 E1): So luong khong the giam xuong duoi 1 (nut tru bi khoa o muc 1)")
    public void quantityCannotGoBelowOne() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(catalog.getFirstProductId());

        Assert.assertEquals(catalog.getDetailQuantity(), 1, "So luong khoi tao phai la 1");
        Assert.assertFalse(catalog.isDetailMinusEnabled(), "Nut tru phai bi khoa khi so luong dang la 1");
    }

    @Test(description = "TC-CN05-02 (CN-05 E2): Tang so luong bi kep o dung ton kho, nut cong bi khoa khi cham tran")
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

    @Test(description = "CN-05.2: Chon so luong 3 o trang chi tiet roi them vao gio (che do khach)")
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

    @Test(description = "CN-05.2: Mua ngay khi da dang nhap -> them gio va chuyen thang sang trang thanh toan")
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

    @Test(description = "TC-CN05-03: San pham het hang -> hien thong bao het hang, khoa ca hai nut mua (bo qua neu seed khong co)")
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

    @Test(description = "TC-CN05-04: Mo lien ket san pham khong ton tai -> trang 404 cua SUT")
    public void nonExistentProductShows404() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/product/00000000-0000-0000-0000-000000000000");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        Assert.assertTrue(catalog.isProductNotFoundDisplayed(),
                "San pham khong ton tai phai hien trang '404 - Khong tim thay san pham'");
    }
}
