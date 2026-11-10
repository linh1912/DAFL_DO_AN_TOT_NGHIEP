package vn.dafl.tests.web.catalog;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.List;

/**
 * CN-04 Danh muc san pham: hien thi, tim kiem, loc danh muc/gia, sap xep va
 * phan trang 12 san pham/trang (ban chot v5).
 */
@Feature("Danh mục và tìm kiếm")
public class CatalogAndSearchTest extends BaseTest {

    @Story("Hiển thị danh mục")
    @Test(description = "CN-04: Trang chủ hiển thị lưới sản phẩm (tối đa 12 sản phẩm ở trang đầu)")
    public void homePageDisplaysProducts() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        int count = catalog.getProductCountOnPage();
        Assert.assertTrue(count > 0, "Trang chu phai co san pham");
        Assert.assertTrue(count <= 12, "Moi trang chi hien thi toi da 12 san pham, dang thay " + count);
        Assert.assertNotNull(catalog.getFirstProductId(), "Phai lay duoc id san pham dau tien");
    }

    @Story("Tìm kiếm")
    @Test(description = "CN-04.2: Tìm kiếm theo từ khóa trùng tên sản phẩm")
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

    @Story("Tìm kiếm")
    @Test(description = "CN-04.2: Tìm kiếm không có kết quả -> trạng thái rỗng kèm nút xóa bộ lọc")
    public void searchWithNoMatchingKeyword() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        new HomePageObject().searchProduct("tukhoakhongtontai987654");

        Assert.assertEquals(catalog.getProductCountOnPage(), 0, "Khong duoc hien san pham nao");
        Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(), "Phai hien trang thai 'Khong tim thay san pham phu hop'");
    }

    @Story("Tìm kiếm")
    @Test(description = "TC-CN04-04 (bảo mật): Từ khóa SQL injection ' OR 1=1 -- được coi là text thường, không lộ toàn bộ dữ liệu")
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

    @Story("Lọc theo danh mục")
    @Test(description = "CN-04.3: Lọc theo danh mục đầu tiên rồi bỏ lọc về tất cả")
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

    @Story("Phân trang")
    @Test(description = "TC-CN04-01 (CN-04.5): Phân trang 12 sản phẩm/trang - di chuyển giữa các trang")
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

    @Story("Sắp xếp")
    @Test(description = "TC-CN04-02 (CN-04.4): Sắp xếp giá tăng dần và giảm dần trên trang hiện tại")
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

    @Story("Lọc theo giá")
    @Test(description = "TC-CN04-03 (CN-04.3): Lọc theo khoảng giá; nhập giá bắt đầu > giá đến thì báo lỗi và không áp dụng")
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

    @Story("Lọc theo giá")
    @Test(description = "TC-CN04-04 (đợt 8): Nút 'Áp dụng' lọc khoảng giá áp dụng đúng khoảng đã nhập; khoảng sai + nút Áp dụng thì báo lỗi")
    public void priceApplyButtonAppliesRange() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        Assert.assertTrue(catalog.isPriceApplyButtonDisplayed(),
                "Bo loc gia phai co nut 'Áp dụng' (price-apply-btn)");

        catalog.setPriceRange("100000", "200000");
        catalog.clickPriceApply();
        List<Long> prices = catalog.getDisplayedProductPrices();
        Assert.assertFalse(prices.isEmpty(), "Khoang gia 100k-200k phai co san pham (du lieu seed)");
        for (Long price : prices) {
            Assert.assertTrue(price >= 100_000 && price <= 200_000,
                    "Sau khi bam 'Áp dụng', san pham ngoai khoang gia van bi hien: " + price);
        }

        catalog.setPriceRange("500000", "100000");
        catalog.clickPriceApply();
        Assert.assertTrue(catalog.isPriceFilterErrorDisplayed(),
                "Khoang sai + bam 'Áp dụng' phai hien loi price-filter-error");
    }

    @Story("Lọc theo giá")
    @Test(description = "TC-CN04-05 (đợt 11): Bấm khoảng giá có sẵn '100K - 300K' thì điền sẵn Từ/Đến và lọc đúng khoảng")
    public void pricePresetAppliesRange() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        catalog.clickPricePreset(1);
        Assert.assertEquals(catalog.getPriceFromValue(), "100000",
                "Bam preset '100K - 300K' phai dien o Tu = 100000");
        Assert.assertEquals(catalog.getPriceToValue(), "300000",
                "Bam preset '100K - 300K' phai dien o Den = 300000");
        List<Long> prices = catalog.getDisplayedProductPrices();
        Assert.assertFalse(prices.isEmpty(), "Khoang gia 100k-300k phai co san pham (du lieu seed)");
        for (Long price : prices) {
            Assert.assertTrue(price >= 100_000 && price <= 300_000,
                    "Sau khi bam preset, san pham ngoai khoang gia van bi hien: " + price);
        }
    }

    @Story("Hiển thị danh mục")
    @Test(description = "CN-04/05: Sản phẩm hết hàng hiện nhãn 'Hết hàng' và nút thêm giỏ bị khóa (bỏ qua nếu dữ liệu seed chưa có)")
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

    @Story("Hiển thị danh mục")
    @Test(description = "CN-05: Mở trang chi tiết sản phẩm từ danh sách rồi quay lại")
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
