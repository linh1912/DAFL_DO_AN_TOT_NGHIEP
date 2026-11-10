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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CN-04 Danh muc san pham: hien thi, tim kiem, loc danh muc/gia, sap xep va
 * phan trang 12 san pham/trang (ban chot v5).
  *
 * Gop them 15 test tu CatalogExtendedTest (BUOC 3, dot 15.1) ngay 2026-11-10 — 1 file duy nhat cho danh muc/tim kiem (27 test).
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

    // ---------- Test bo sung — gop tu CatalogExtendedTest (BUOC 3, file manual v15.1) ----------

    private static final Pattern BADGE_COUNT_PATTERN =
            Pattern.compile("Hiển thị\\s+(\\d+)\\s+sản phẩm");
    private static final Pattern RATING_PATTERN =
            Pattern.compile("(\\d+\\.\\d)\\s*\\((\\d+)\\)");

    /** So SP trong badge dem ("Hien thi {n} san pham"); -1 neu khong doc duoc. */
    private int parseBadgeCount(String badgeText) {
        Matcher matcher = BADGE_COUNT_PATTERN.matcher(badgeText);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }

    /** Mo trang chu va doi luoi chinh tai xong. */
    private ProductCatalogPageObject openCatalog() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        return catalog;
    }

    @Story("Hiển thị danh mục")
    @Test(description = "TC_SCR4_UI_001: Thẻ sản phẩm hiển thị đủ badge giảm giá, sao + lượt đánh giá, giá gốc gạch ngang, tồn kho")
    public void productCardShowsDiscountBadgeRatingOriginalPriceAndStock() {
        ProductCatalogPageObject catalog = openCatalog();

        int gridCount = catalog.getGridProductCount();
        Assert.assertTrue(gridCount > 0, "Luoi chinh phai co san pham");
        Assert.assertTrue(gridCount <= 12, "Moi trang chi hien thi toi da 12 san pham, dang thay " + gridCount);

        // Tim 1 the co giam gia de kiem tra day du (seed co SP giam gia; neu
        // khong co thi khong the kiem tra badge giam gia).
        String discountedId = null;
        for (String id : catalog.getGridProductIds()) {
            if (!catalog.getProductDiscountBadgeText(id).isEmpty()) {
                discountedId = id;
                break;
            }
        }
        if (discountedId == null) {
            throw new SkipException("Du lieu seed hien tai khong co san pham giam gia tren trang 1"
                    + " — khong kiem tra duoc badge giam gia / gia goc");
        }

        String badge = catalog.getProductDiscountBadgeText(discountedId);
        Assert.assertTrue(badge.matches("^-\\d+%$"),
                "Badge giam gia phai dang '-{n}%', dang thay: " + badge);

        long salePrice = catalog.getProductPrice(discountedId);
        String originalText = catalog.getProductOriginalPriceText(discountedId);
        Assert.assertFalse(originalText.isEmpty(), "The giam gia phai hien gia goc");
        long originalPrice = catalog.parseMoney(originalText);
        Assert.assertTrue(originalPrice > salePrice,
                "Gia goc (" + originalPrice + ") phai lon hon gia ban (" + salePrice + ")");
        int expectedPercent = (int) Math.round((originalPrice - salePrice) * 100.0 / originalPrice);
        Assert.assertEquals(badge, "-" + expectedPercent + "%",
                "Badge giam gia phai dung % tinh tu gia goc/gia ban");
        Assert.assertTrue(catalog.isOriginalPriceStruckThrough(discountedId),
                "Gia goc phai bi gach ngang (line-through)");

        String ratingText = catalog.getProductRatingText(discountedId);
        Matcher ratingMatcher = RATING_PATTERN.matcher(ratingText);
        Assert.assertTrue(ratingMatcher.find(),
                "The phai hien sao dang '{rating} ({reviewCount})', dang thay: " + ratingText);

        String stockText = catalog.getProductStockText(discountedId).trim();
        Assert.assertTrue(stockText.startsWith("Còn") || stockText.equals("Hết hàng"),
                "The phai hien 'Còn {n}' hoac 'Hết hàng', dang thay: " + stockText);
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_005: Nút \"Xem tất cả\" chỉ hiện khi đã chọn danh mục; bấm thì bỏ chọn và về đủ 12 SP")
    public void clearCategoryButtonOnlyShowsAfterSelectingCategory() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        Assert.assertFalse(catalog.isClearCategoryButtonDisplayed(),
                "Chua chon danh muc thi nut 'Xem tất cả' (clear-category-btn) phai vang mat");

        homePage.selectCategoryByIndex(0);
        Assert.assertTrue(catalog.isClearCategoryButtonDisplayed(),
                "Sau khi chon 1 danh muc, nut 'Xem tất cả' phai xuat hien");
        Assert.assertNotNull(catalog.getActiveCategoryPillName(),
                "Sau khi chon danh muc phai co pill dang duoc chon");

        catalog.clickClearCategoryButton();
        Assert.assertFalse(catalog.isClearCategoryButtonDisplayed(),
                "Bam 'Xem tất cả' xong nut phai bien mat (khong con danh muc duoc chon)");
        Assert.assertNull(catalog.getActiveCategoryPillName(),
                "Bam 'Xem tất cả' xong khong duoc con pill nao dang chon");
        Assert.assertEquals(catalog.getGridProductCount(), 12,
                "Bo chon danh muc xong luoi phai ve du 12 SP trang dau");
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_006: Gõ \"son\" vào ô tìm danh mục -> chỉ còn pill tên chứa \"son\"")
    public void searchCategoryByNameShowsOnlyMatchingPills() {
        ProductCatalogPageObject catalog = openCatalog();

        int initialCount = catalog.getVisibleCategoryPillNames().size();
        Assert.assertTrue(initialCount > 1, "Phai co nhieu hon 1 danh muc de kiem tra loc, dang thay " + initialCount);

        catalog.typeCategorySearch("son");
        List<String> visible = catalog.getVisibleCategoryPillNames();
        Assert.assertEquals(visible.size(), 1,
                "Tim 'son' chi duoc con dung 1 pill, dang thay: " + visible);
        Assert.assertTrue(visible.get(0).toLowerCase().contains("son"),
                "Pill con lai phai co ten chua 'son', dang thay: " + visible);
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_007: Gõ từ chỉ có trong mô tả danh mục (\"cushion\") -> danh mục đó vẫn hiện dù tên không chứa từ khóa")
    public void searchCategoryByDescriptionShowsMatchingCategory() {
        ProductCatalogPageObject catalog = openCatalog();

        catalog.typeCategorySearch("cushion");
        if (catalog.isCategoryEmptyStateDisplayed()) {
            throw new SkipException("Du lieu seed hien tai khong co danh muc nao mo ta chua 'cushion'"
                    + " — khong kiem tra duoc tim danh muc theo mo ta");
        }
        List<String> visible = catalog.getVisibleCategoryPillNames();
        Assert.assertEquals(visible.size(), 1,
                "Tim 'cushion' chi duoc con dung 1 pill, dang thay: " + visible);
        Assert.assertEquals(visible.get(0), "Trang điểm",
                "Danh muc co mo ta chua 'cushion' la 'Trang điểm', dang thay: " + visible);
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_008: Gõ \"xyzabc\" vào ô tìm danh mục -> trạng thái rỗng + nút \"Hiển thị tất cả\" khôi phục toàn bộ pill")
    public void searchCategoryNoMatchShowsEmptyStateAndShowAllRestores() {
        ProductCatalogPageObject catalog = openCatalog();

        int initialCount = catalog.getVisibleCategoryPillNames().size();
        catalog.typeCategorySearch("xyzabc");

        Assert.assertTrue(catalog.isCategoryEmptyStateDisplayed(),
                "Tim danh muc khong khop phai hien trang thai rong (category-empty-state)");
        String emptyText = catalog.getCategoryEmptyStateText();
        Assert.assertTrue(emptyText.contains("Không tìm thấy danh mục nào phù hợp với"),
                "Trang thai rong phai dung cau bao khong tim thay, dang thay: " + emptyText);
        Assert.assertTrue(emptyText.contains("xyzabc"),
                "Trang thai rong phai ghi dung tu khoa vua go 'xyzabc', dang thay: " + emptyText);

        catalog.clickCategoryEmptyShowAll();
        Assert.assertEquals(catalog.getCategorySearchValue(), "",
                "Bam 'Hiển thị tất cả' xong o tim danh muc phai tro ve trong");
        Assert.assertEquals(catalog.getVisibleCategoryPillNames().size(), initialCount,
                "Bam 'Hiển thị tất cả' xong toan bo pill phai hien lai");
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_009: Nút xóa trong ô tìm danh mục -> ô trống và toàn bộ pill hiện lại")
    public void clearButtonInCategorySearchRestoresAllPills() {
        ProductCatalogPageObject catalog = openCatalog();

        int initialCount = catalog.getVisibleCategoryPillNames().size();
        catalog.typeCategorySearch("son");
        Assert.assertTrue(catalog.isCategorySearchClearButtonDisplayed(),
                "Go tu khoa xong nut xoa tim kiem danh muc phai hien");
        Assert.assertTrue(catalog.getVisibleCategoryPillNames().size() < initialCount,
                "Go 'son' xong so pill phai giam so voi ban dau");

        catalog.clickCategorySearchClearButton();
        Assert.assertEquals(catalog.getCategorySearchValue(), "",
                "Bam nut xoa xong o tim danh muc phai tro ve trong");
        Assert.assertFalse(catalog.isCategorySearchClearButtonDisplayed(),
                "O tim danh muc trong roi thi nut xoa phai an");
        Assert.assertEquals(catalog.getVisibleCategoryPillNames().size(), initialCount,
                "Bam nut xoa xong toan bo pill phai hien lai");
    }

    @Story("Lọc theo danh mục")
    @Test(description = "TC_SCR4_FUNC_010: Nhóm \"Loại sản phẩm\" ở sidebar lọc theo danh mục; \"Tất cả sản phẩm\" thì về đủ SP mọi danh mục")
    public void productTypeGroupInSidebarFiltersByCategory() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        List<String> filterLinks = catalog.getFilterLinkNames();
        Assert.assertTrue(filterLinks.contains("Tất cả sản phẩm"),
                "Nhom 'Loại sản phẩm' phai co muc 'Tất cả sản phẩm', dang thay: " + filterLinks);
        int pillCount = catalog.getVisibleCategoryPillNames().size();
        Assert.assertEquals(filterLinks.size(), pillCount + 1,
                "Nhom 'Loại sản phẩm' phai co du ten cac danh muc + 'Tất cả sản phẩm'");
        String firstCategory = filterLinks.get(1);

        catalog.clickFilterLinkByName(firstCategory);
        Assert.assertEquals(catalog.getActiveCategoryPillName(), firstCategory,
                "Bam ten danh muc trong nhom 'Loại sản phẩm' phai chon dung danh muc do");
        int filtered = catalog.getGridProductCount();
        Assert.assertTrue(filtered >= 1 && filtered <= 12,
                "Loc theo danh muc phai co ket qua hop le, dang thay " + filtered);

        catalog.clickFilterLinkByName("Tất cả sản phẩm");
        Assert.assertNull(catalog.getActiveCategoryPillName(),
                "Bam 'Tất cả sản phẩm' xong khong duoc con danh muc nao duoc chon");
        Assert.assertEquals(catalog.getGridProductCount(), 12,
                "Bam 'Tất cả sản phẩm' xong luoi phai ve du 12 SP trang dau");
    }

    @Story("Lọc theo giá")
    @Test(description = "TC_SCR4_FUNC_011, TC_SCR4_FUNC_013, TC_SCR4_FUNC_014, TC_SCR4_FUNC_015: Các mức giá sẵn còn lại điền sẵn Từ/Đến, lọc ngay và tô active (\"Dưới 100K\", \"300K – 500K\", \"500K – 1 triệu\", \"Trên 1 triệu\")")
    public void remainingPricePresetsFillRangeFilterImmediatelyAndMarkActive() {
        ProductCatalogPageObject catalog = openCatalog();

        // {presetIndex, from, to, minPrice, maxPrice} — gia tri null = khong gioi han dau do.
        Object[][] presets = {
                {0, "", "100000", null, 100000L},
                {2, "300000", "500000", 300000L, 500000L},
                {3, "500000", "1000000", 500000L, 1000000L},
                {4, "1000000", "", 1000000L, null},
        };
        for (Object[] preset : presets) {
            int index = (Integer) preset[0];
            String expectedFrom = (String) preset[1];
            String expectedTo = (String) preset[2];
            Long minPrice = (Long) preset[3];
            Long maxPrice = (Long) preset[4];

            catalog.clickPricePreset(index);
            Assert.assertEquals(catalog.getPriceFromValue(), expectedFrom,
                    "Preset " + index + " phai dien o Tu = '" + expectedFrom + "'");
            Assert.assertEquals(catalog.getPriceToValue(), expectedTo,
                    "Preset " + index + " phai dien o Den = '" + expectedTo + "'");
            Assert.assertTrue(catalog.isPricePresetActive(index),
                    "Preset " + index + " sau khi bam phai duoc to active (class active + aria-pressed)");

            int count = catalog.getGridProductCount();
            if (count == 0) {
                Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(),
                        "Preset " + index + " khong co SP nao trong khoang thi phai hien trang thai rong");
            } else {
                for (Long price : catalog.getGridProductPrices()) {
                    if (minPrice != null) {
                        Assert.assertTrue(price >= minPrice,
                                "Preset " + index + ": SP gia " + price + " nho hon muc toi thieu " + minPrice);
                    }
                    if (maxPrice != null) {
                        Assert.assertTrue(price <= maxPrice,
                                "Preset " + index + ": SP gia " + price + " lon hon muc toi da " + maxPrice);
                    }
                }
            }
        }
    }

    @Story("Lọc theo giá")
    @Test(description = "TC_SCR4_FUNC_017: Gõ tay vào ô Từ/Đến thì bỏ chọn mức giá sẵn nhưng lọc vẫn áp dụng theo giá trị vừa gõ")
    public void typingPriceManuallyDeselectsActivePreset() {
        ProductCatalogPageObject catalog = openCatalog();

        catalog.clickPricePreset(1);
        Assert.assertTrue(catalog.isPricePresetActive(1),
                "Bam preset '100K – 300K' xong preset 1 phai dang active");

        catalog.setPriceRange("150000", null);
        for (int i = 0; i < 5; i++) {
            Assert.assertFalse(catalog.isPricePresetActive(i),
                    "Go tay vao o Tu xong khong duoc con preset nao active (preset " + i + " van active)");
        }
        Assert.assertEquals(catalog.getPriceFromValue(), "150000",
                "O Tu phai giu dung gia tri vua go tay 150000");

        List<Long> prices = catalog.getGridProductPrices();
        Assert.assertFalse(prices.isEmpty(), "Khoang gia 150k-300k phai co san pham (du lieu seed)");
        for (Long price : prices) {
            Assert.assertTrue(price >= 150_000 && price <= 300_000,
                    "Sau khi go tay, SP ngoai khoang gia moi van bi hien: " + price);
        }
    }

    @Story("Tìm kiếm")
    @Test(description = "TC_SCR4_FUNC_019: Tìm kiếm theo từ khóa không phân biệt hoa thường — \"son\" và \"SON\" trả về cùng kết quả")
    public void searchKeywordIsCaseInsensitive() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        HomePageObject homePage = new HomePageObject();

        homePage.searchProduct("son");
        List<String> lowerIds = catalog.getGridProductIds();
        if (lowerIds.isEmpty()) {
            throw new SkipException("Du lieu seed hien tai khong co SP nao ten/mo ta chua 'son'"
                    + " — khong so sanh duoc chu thuong vs CHU HOA");
        }
        boolean lowerHasNext = catalog.isNextPageButtonEnabled();

        homePage.clearSearch();
        homePage.searchProduct("SON");
        List<String> upperIds = catalog.getGridProductIds();

        Assert.assertEquals(upperIds, lowerIds,
                "Tim 'SON' phai tra ve dung danh sach nhu tim 'son'");
        Assert.assertEquals(catalog.isNextPageButtonEnabled(), lowerHasNext,
                "Tim 'SON' va 'son' phai co cung so trang ket qua");
    }

    @Story("Tìm kiếm")
    @Test(description = "TC_SCR4_FUNC_020: Tìm kiếm không có kết quả rồi bấm \"Xóa bộ lọc\" -> danh sách về đủ SP như ban đầu")
    public void clearFiltersAfterEmptySearchRestoresFullList() {
        openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        HomePageObject homePage = new HomePageObject();

        homePage.searchProduct("sanphamkhongtontai123");
        Assert.assertEquals(catalog.getGridProductCount(), 0, "Khong duoc hien san pham nao");
        Assert.assertTrue(catalog.isCatalogEmptyStateDisplayed(),
                "Phai hien trang thai 'Không tìm thấy sản phẩm phù hợp'");

        catalog.clearFilters();
        Assert.assertEquals(catalog.getGridProductCount(), 12,
                "Bam 'Xóa bộ lọc' xong danh sach phai ve du 12 SP trang dau");
        Assert.assertEquals(homePage.getSearchInputValue(), "",
                "Bam 'Xóa bộ lọc' xong o tim kiem phai tro ve trong");
    }

    @Story("Sắp xếp")
    @Test(description = "TC_SCR4_FUNC_023: Sắp xếp mặc định là \"Mới nhất\" (sort-select = newest) và khác thứ tự sắp xếp theo giá")
    public void defaultSortIsNewest() {
        ProductCatalogPageObject catalog = openCatalog();

        Assert.assertEquals(catalog.getSortValue(), "newest",
                "O sap xep mac dinh phai la 'Mới nhất' (newest)");
        Assert.assertEquals(catalog.getGridProductCount(), 12, "Trang dau phai du 12 san pham");
        List<String> newestIds = catalog.getGridProductIds();

        catalog.selectSort("price-asc");
        List<Long> ascending = catalog.getGridProductPrices();
        Assert.assertFalse(ascending.isEmpty(), "Sap xep gia tang dan phai co san pham");
        for (int i = 1; i < ascending.size(); i++) {
            Assert.assertTrue(ascending.get(i) >= ascending.get(i - 1),
                    "Gia tang dan bi sai tai vi tri " + i + ": " + ascending);
        }
        Assert.assertFalse(catalog.getGridProductIds().equals(newestIds),
                "Thu tu 'Mới nhất' phai khac thu tu 'Giá tăng dần' (sap xep mac dinh phai la 1 cach sap that)");

        catalog.selectSort("newest");
        Assert.assertEquals(catalog.getSortValue(), "newest",
                "Chon lai 'Mới nhất' thi sort-select phai ve newest");
        Assert.assertEquals(catalog.getGridProductCount(), 12, "Ve 'Mới nhất' xong trang dau phai du 12 san pham");
    }

    @Story("Phân trang")
    @Test(description = "TC_SCR4_FUNC_026: Giữ nguyên bộ lọc danh mục khi chuyển trang (tự bỏ qua nếu seed không có danh mục nào trên 12 SP)")
    public void categoryFilterPersistsAcrossPages() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        int pillCount = catalog.getVisibleCategoryPillNames().size();
        for (int i = 0; i < pillCount; i++) {
            homePage.selectCategoryByIndex(i);
            if (!catalog.isNextPageButtonEnabled()) {
                continue; // danh muc nay chi co 1 trang — thu danh muc khac
            }
            String categoryName = catalog.getActiveCategoryPillName();
            List<String> pageOneIds = catalog.getGridProductIds();

            catalog.goToNextCatalogPage();
            Assert.assertEquals(catalog.getActiveCategoryPillName(), categoryName,
                    "Sang trang 2 pill danh muc van phai duoc chon");
            Assert.assertTrue(catalog.getCatalogResultCountText().contains("Trang 2"),
                    "Sang trang 2 badge dem phai ghi 'Trang 2', dang thay: "
                            + catalog.getCatalogResultCountText());
            Assert.assertFalse(catalog.getGridProductIds().equals(pageOneIds),
                    "Trang 2 phai hien cac SP khac trang 1 (van trong cung danh muc)");
            return;
        }
        throw new SkipException("Seed chuan chi co toi da 12 SP/danh muc (nhieu nhat la cham-soc-da = 12)"
                + " nen khong co danh muc nao du 2 trang de kiem tra giu bo loc khi chuyen trang");
    }

    @Story("Hiển thị danh mục")
    @Test(description = "TC_SCR4_FUNC_027: Badge đếm \"Hiển thị {n} sản phẩm\" đổi theo lọc và thêm \" • Trang {N}\" khi ở trang > 1")
    public void resultCountBadgeUpdatesWithFilterAndPage() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        // Khi online BE khong tra tong nen badge chi hien "Hien thi {n} san pham"
        // (dang "Hien thi {n} / {tong}" chi co o che do fallback/offline).
        String initialBadge = catalog.getCatalogResultCountText();
        Assert.assertEquals(parseBadgeCount(initialBadge), 12,
                "Trang dau chua loc badge phai dem dung 12 SP, dang thay: " + initialBadge);

        homePage.selectCategoryByIndex(0);
        int filteredCount = catalog.getGridProductCount();
        Assert.assertTrue(filteredCount >= 1 && filteredCount <= 12,
                "Loc theo danh muc phai co ket qua hop le, dang thay " + filteredCount);
        Assert.assertEquals(parseBadgeCount(catalog.getCatalogResultCountText()), filteredCount,
                "Sau khi loc, badge phai dem dung so SP dang xem");

        // Tim 1 bo loc co hon 12 ket qua de co trang 2 (thu cac preset theo thu tu).
        homePage.selectAllCategories();
        boolean foundMultiPageFilter = false;
        for (int preset = 1; preset <= 3; preset++) {
            catalog.clickPricePreset(preset);
            if (catalog.isNextPageButtonEnabled()) {
                foundMultiPageFilter = true;
                break;
            }
        }
        if (!foundMultiPageFilter) {
            throw new SkipException("Khong tim duoc bo loc gia nao co hon 12 ket qua trong du lieu seed hien tai"
                    + " — khong kiem tra duoc nhanh badge 'Trang {N}'");
        }
        catalog.goToCatalogPage(2);
        String pageTwoBadge = catalog.getCatalogResultCountText();
        Assert.assertTrue(pageTwoBadge.contains("Trang 2"),
                "O trang 2 badge phai ghi them ' • Trang 2', dang thay: " + pageTwoBadge);
        Assert.assertEquals(parseBadgeCount(pageTwoBadge), catalog.getGridProductCount(),
                "O trang 2 badge phai dem dung so SP dang xem");
    }

    @Story("Hiển thị danh mục")
    @Test(description = "TC_SCR4_FUNC_029: Sản phẩm ngừng bán không hiển thị trong danh sách/tìm kiếm, mở trực tiếp thì ra 404 (tự bỏ qua: không xác định được SP ngừng bán qua UI)")
    public void discontinuedProductNotShownInList() {
        // Da kiem chung truoc khi viet test:
        // - BE loc SP ngung ban khoi GET /api/products (Where IsActive && !IsDeleted).
        // - Seed chuan khong co SP IsActive=false; SP legacy chi bi an (Stock=0,
        //   IsActive=false) khi da co OrderItem tham chieu — khong co cach nao
        //   kham pha id/ten cua SP do qua UI de tim mo truc tiep /product/{id}.
        // Muon chay duoc case nay phai chuan bi du lieu truoc qua admin (tat trang
        // thai 1 SP roi moi kiem tra) — nam ngoai pham vi test web hien tai.
        throw new SkipException("Khong xac dinh duoc SP ngung ban (IsActive=false) nao qua UI"
                + " — seed khong co, SP legacy bi an chi khi co don hang tham chieu");
    }
}
