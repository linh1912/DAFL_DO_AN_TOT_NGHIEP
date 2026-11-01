package vn.dafl.tests;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.List;

/**
 * CN-04 Danh muc san pham: hien thi, tim kiem, loc danh muc/gia, sap xep va
 * phan trang 12 san pham/trang (ban chot v5).
 */
public class CatalogAndSearchTest extends BaseTest {

    @Test(description = "CN-04: Trang chu hien thi luoi san pham (toi da 12 san pham o trang dau)")
    public void homePageDisplaysProducts() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        int count = catalog.getProductCountOnPage();
        Assert.assertTrue(count > 0, "Trang chu phai co san pham");
        Assert.assertTrue(count <= 12, "Moi trang chi hien thi toi da 12 san pham, dang thay " + count);
        Assert.assertNotNull(catalog.getFirstProductId(), "Phai lay duoc id san pham dau tien");
    }

    @Test(description = "CN-04.2: Tim kiem theo tu khoa trung ten san pham")
    public void searchByProductKeyword() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        String productName = catalog.getProductName(productId);
        String keyword = productName.split(" ")[0] + " " + productName.split(" ")[1];

        HomePageObject homePage = new HomePageObject();
        homePage.searchProduct(keyword);

        Assert.assertTrue(catalog.getDisplayedProductIds().contains(productId),
                "Ket qua tim kiem phai chua san pham " + productName);
    }

    @Test(description = "CN-04.2: Tim kiem khong co ket qua -> trang thai rong kem nut xoa bo loc")
    public void searchWithNoMatchingKeyword() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        new HomePageObject().searchProduct("tukhoakhongtontai987654");

        Assert.assertEquals(catalog.getProductCountOnPage(), 0, "Khong duoc hien san pham nao");
        Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(), "Phai hien trang thai 'Khong tim thay san pham phu hop'");
    }

    @Test(description = "TC-CN04-04 (bao mat): Tu khoa SQL injection ' OR 1=1 -- duoc coi la text thuong, khong lo toan bo du lieu")
    public void searchWithSqlInjectionKeywordIsTreatedAsPlainText() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        new HomePageObject().searchProduct("' OR 1=1 --");

        Assert.assertEquals(catalog.getProductCountOnPage(), 0,
                "Tu khoa doc hai khong duoc tra ve toan bo san pham");
        Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(),
                "He thong phai xu ly tu khoa doc nhu tu khoa thuong (khong co ket qua)");
    }

    @Test(description = "CN-04.3: Loc theo danh muc dau tien roi bo loc ve tat ca")
    public void filterByFirstCategory() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        Assert.assertEquals(catalog.getProductCountOnPage(), 12, "Trang dau chua loc phai du 12 san pham");

        String categoryId = homePage.getFirstCategoryId();
        Assert.assertNotNull(categoryId, "Phai co it nhat mot danh muc");
        homePage.selectCategory(categoryId);
        int filtered = catalog.getProductCountOnPage();
        Assert.assertTrue(filtered >= 1 && filtered <= 12,
                "Loc theo danh muc phai co ket qua hop le, dang thay " + filtered);

        homePage.selectAllCategories();
        Assert.assertEquals(catalog.getProductCountOnPage(), 12, "Bo loc phai ve lai 12 san pham trang dau");
    }

    @Test(description = "TC-CN04-01 (CN-04.5): Phan trang 12 san pham/trang - di chuyen giua cac trang")
    public void paginationShowsTwelvePerPageAndNavigates() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        Assert.assertTrue(catalog.isPaginationDisplayed(), "Phai co thanh phan trang");
        Assert.assertEquals(catalog.getProductCountOnPage(), 12, "Trang 1 phai co dung 12 san pham");
        List<String> pageOneIds = catalog.getDisplayedProductIds();

        catalog.goToCatalogPage(2);
        List<String> pageTwoIds = catalog.getDisplayedProductIds();
        Assert.assertTrue(pageTwoIds.size() >= 1, "Trang 2 phai co san pham");
        Assert.assertFalse(pageTwoIds.equals(pageOneIds), "Trang 2 phai khac trang 1");

        catalog.goToPreviousCatalogPage();
        Assert.assertEquals(catalog.getDisplayedProductIds(), pageOneIds, "Quay lai phai dung san pham trang 1");
    }

    @Test(description = "TC-CN04-02 (CN-04.4): Sap xep gia tang dan va giam dan tren trang hien tai")
    public void sortByPriceAscendingAndDescending() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        catalog.selectSort("price-asc");
        List<Long> ascending = catalog.getDisplayedProductPrices();
        for (int i = 1; i < ascending.size(); i++) {
            Assert.assertTrue(ascending.get(i) >= ascending.get(i - 1),
                    "Gia tang dan bi sai tai vi tri " + i + ": " + ascending);
        }

        catalog.selectSort("price-desc");
        List<Long> descending = catalog.getDisplayedProductPrices();
        for (int i = 1; i < descending.size(); i++) {
            Assert.assertTrue(descending.get(i) <= descending.get(i - 1),
                    "Gia giam dan bi sai tai vi tri " + i + ": " + descending);
        }
    }

    @Test(description = "TC-CN04-03 (CN-04.3): Loc theo khoang gia; nhap gia bat dau > gia den thi bao loi va khong ap dung")
    public void priceRangeFilterAndInvalidRangeError() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        catalog.setPriceRange("100000", "200000");
        List<Long> prices = catalog.getDisplayedProductPrices();
        Assert.assertFalse(prices.isEmpty(), "Khoang gia 100k-200k phai co san pham (du lieu seed)");
        for (Long price : prices) {
            Assert.assertTrue(price >= 100_000 && price <= 200_000,
                    "San pham ngoai khoang gia van bi hien: " + price);
        }

        catalog.setPriceRange("500000", "100000");
        Assert.assertTrue(catalog.isPriceFilterErrorDisplayed(),
                "Gia bat dau lon hon gia den phai hien loi price-filter-error");
        Assert.assertTrue(catalog.getPriceFilterError().contains("không được lớn hơn"),
                "Noi dung loi khoang gia khong dung: " + catalog.getPriceFilterError());
    }

    @Test(description = "CN-04/05: San pham het hang hien nhan 'Het hang' va nut them gio bi khoa (bo qua neu du lieu seed chua co)")
    public void outOfStockProductShowsBadge() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        for (int page = 1; page <= 9; page++) {
            for (String productId : catalog.getDisplayedProductIds()) {
                if ("Hết hàng".equals(catalog.getProductStockText(productId).trim())) {
                    Assert.assertFalse(catalog.isAddToCartButtonEnabled(productId),
                            "San pham het hang phai bi khoa nut them gio");
                    return;
                }
            }
            if (!catalog.isNextPageButtonEnabled()) {
                break;
            }
            catalog.goToNextCatalogPage();
        }
        throw new SkipException("Du lieu seed hien tai khong co san pham het hang - bo qua kiem thu nay");
    }

    @Test(description = "CN-05: Mo trang chi tiet san pham tu danh sach roi quay lai")
    public void openProductDetailAndBack() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        String productName = catalog.getProductName(productId);

        catalog.openProductDetail(productId);
        Assert.assertTrue(catalog.isProductDetailDisplayed(), "Phai mo duoc trang chi tiet san pham");
        Assert.assertTrue(isTextVisibleOnPage(productName), "Trang chi tiet phai hien dung ten san pham");

        catalog.backFromDetail();
        Assert.assertTrue(catalog.getProductCountOnPage() > 0, "Quay lai danh sach phai thay san pham");
    }
}
