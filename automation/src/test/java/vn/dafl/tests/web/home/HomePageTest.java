package vn.dafl.tests.web.home;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pha B dot 15 buoc 3 — case loai (c) sheet SCR_09 Trang chu (phan hero +
 * cac muc noi dung trang chu). Bo sung cho HomeHeroTest.
 *
 * Luu y ve du lieu (da kiem chung tu App.tsx v15 + DbInitializer cua fork):
 * - Noi dung 3 slide hero (badge/tieu de/phu/CTA) lay dung tu HERO_SLIDES.
 * - Flash Sale / Ban chay / Goi y chi tinh tren 12 SP cua TRANG HIEN TAI
 *   (khong phai toan bo catalog): Ban chay = top 8 theo rating roi
 *   reviewCount, Goi y = cac SP con lai cua trang, Flash Sale = SP giam gia
 *   manh nhat (toi da 8) xep theo % giam.
 * - The trong 3 muc nay dung testid hau to -flash/-best/-suggest nen khi dem
 *   hay doc luoi chinh phai dung locator gioi han trong #products-section.
 */
@Feature("Trang chủ")
public class HomePageTest extends BaseTest {

    private static final Pattern RATING_PATTERN =
            Pattern.compile("(\\d+\\.\\d)\\s*\\((\\d+)\\)");

    private static final String[] EXPECTED_HERO_BADGES = {
            "FLASH SALE • GIẢM ĐẾN 50%",
            "FREESHIP ĐƠN TỪ 299K",
            "HỘI VIÊN DAFL",
    };
    private static final String[] EXPECTED_HERO_TITLES = {
            "Mỹ phẩm chính hãng",
            "Freeship đơn từ 299K",
            "Tích điểm lên hạng",
    };
    private static final String[] EXPECTED_HERO_SUBTITLES = {
            "Son, serum, kem chống nắng chính hãng từ nhiều shop.",
            "Giao nhanh 2–4 ngày, đổi trả trong 30 ngày.",
            "Thường • Bạc • Vàng • Kim cương.",
    };
    private static final String[] EXPECTED_HERO_CTAS = {
            "Mua ngay",
            "Xem deal freeship",
            "Khám phá hội viên",
    };

    /** Tach (rating, reviewCount) tu text sao dang "4.9 (78)". */
    private double[] parseRating(String ratingText) {
        Matcher matcher = RATING_PATTERN.matcher(ratingText);
        if (!matcher.find()) {
            throw new AssertionError("Khong doc duoc sao/luot danh gia tu: " + ratingText);
        }
        return new double[]{Double.parseDouble(matcher.group(1)), Integer.parseInt(matcher.group(2))};
    }

    /** Thu tu top 8 mong doi cua Ban chay: rating giam dan, roi reviewCount giam dan. */
    private List<String> expectedBestSellerOrder(List<String> gridIds, ProductCatalogPageObject catalog) {
        List<String> sorted = new ArrayList<>(gridIds);
        sorted.sort((a, b) -> {
            double[] ra = parseRating(catalog.getProductRatingText(a));
            double[] rb = parseRating(catalog.getProductRatingText(b));
            int byRating = Double.compare(rb[0], ra[0]);
            if (byRating != 0) {
                return byRating;
            }
            return Double.compare(rb[1], ra[1]);
        });
        return sorted.subList(0, Math.min(8, sorted.size()));
    }

    @Story("Banner hero")
    @Test(description = "TC_SCR9_UI_001, TC_SCR9_UI_002, TC_SCR9_UI_003: Nội dung đủ 3 slide hero — badge, tiêu đề, dòng phụ, nút CTA đúng từng slide")
    public void heroSlidesDisplayCorrectContentPerSlide() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero tren trang chu");

        // Luu y: interval tu chay 4s/slide KHONG reset khi bam dot nen slide co the
        // tu doi giua luc bam dot va luc doc noi dung — doc xong kiem tra lai
        // index, neu da doi slide thi bam lai dot va doc lai (toi da 3 lan/slide).
        for (int i = 0; i < 3; i++) {
            boolean verified = false;
            for (int attempt = 0; attempt < 3 && !verified; attempt++) {
                homePage.clickHeroDot(i);
                if (homePage.getActiveHeroSlideIndex() != i) {
                    continue;
                }
                String badge = homePage.getActiveHeroBadge();
                String title = homePage.getActiveHeroTitle();
                String subtitle = homePage.getActiveHeroSubtitle();
                String cta = homePage.getActiveHeroCtaText();
                if (homePage.getActiveHeroSlideIndex() != i) {
                    continue; // slide da tu doi giua chung — doc lai
                }
                Assert.assertEquals(badge, EXPECTED_HERO_BADGES[i],
                        "Badge slide " + i + " phai dung nguyen van");
                Assert.assertEquals(title, EXPECTED_HERO_TITLES[i],
                        "Tieu de slide " + i + " phai dung nguyen van");
                Assert.assertEquals(subtitle, EXPECTED_HERO_SUBTITLES[i],
                        "Dong phu slide " + i + " phai dung nguyen van");
                Assert.assertTrue(cta.startsWith(EXPECTED_HERO_CTAS[i]),
                        "CTA slide " + i + " phai la '" + EXPECTED_HERO_CTAS[i] + "', dang thay: " + cta);
                verified = true;
            }
            Assert.assertTrue(verified,
                    "Khong doc on dinh duoc noi dung slide " + i + " (slide tu doi lien tuc giua cac lan doc)");
        }
    }

    @Story("Banner hero")
    @Test(description = "TC_SCR9_UI_004: Hero tự chạy đúng nhịp khoảng 4 giây/slide và quay vòng từ slide cuối về slide đầu")
    public void heroAutoAdvancesAboutEveryFourSecondsAndWrapsAround() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        int startIndex = homePage.getActiveHeroSlideIndex();
        Assert.assertTrue(startIndex >= 0, "Phai xac dinh duoc slide active ban dau");

        long firstAdvanceMs = homePage.measureHeroAutoAdvanceMillis(startIndex);
        Assert.assertTrue(firstAdvanceMs > 0,
                "Banner phai tu chuyen slide trong 12 giay (van o slide " + startIndex + ")");
        Assert.assertTrue(firstAdvanceMs >= 2000 && firstAdvanceMs <= 6000,
                "Nhip tu chay phai khoang 4 giay/slide (cho phep sai so do poll + tai trang), dang thay: "
                        + firstAdvanceMs + "ms");

        // Cho toi slide cuoi (2) roi doi lan doi tiep theo phai quay ve slide 0.
        Assert.assertTrue(homePage.waitForHeroSlideIndex(2, 12),
                "Banner phai tu chay toi slide cuoi (2) trong 12 giay");
        long wrapMs = homePage.measureHeroAutoAdvanceMillis(2);
        Assert.assertTrue(wrapMs > 0, "Banner phai tu chuyen khoi slide cuoi trong 12 giay");
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 0,
                "Sau slide cuoi, banner phai quay ve slide dau (0)");
    }

    @Story("Banner hero")
    @Test(description = "TC_SCR9_FUNC_007: Bấm CTA của hero thì cuộn tới khu vực sản phẩm (#products-section)")
    public void heroCtaScrollsToProductsSection() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        // Ve slide 0 (interval tu chay khong reset khi bam dot nen thu lai neu
        // slide vua tu doi trong luc cho); CTA slide nao cung cuon toi luoi.
        boolean onSlideZero = false;
        for (int attempt = 0; attempt < 3 && !onSlideZero; attempt++) {
            homePage.clickHeroDot(0);
            onSlideZero = homePage.getActiveHeroSlideIndex() == 0;
        }
        Assert.assertTrue(onSlideZero, "Phai ve duoc slide 0 truoc khi bam CTA");
        homePage.clickActiveHeroCta();

        Assert.assertTrue(homePage.isProductsSectionInViewport(),
                "Bam CTA hero xong #products-section phai nam trong khung nhin");
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        Assert.assertTrue(catalog.getGridProductCount() > 0,
                "Cuon toi luoi xong phai thay san pham trong luoi");
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_UI_010: Trust strip hiển thị đủ 4 cam kết mua sắm")
    public void trustStripShowsAllFourCommitments() {
        HomePageObject homePage = openHomePage();
        String trustText = homePage.getTrustStripText();

        for (String commitment : Arrays.asList(
                "Chính hãng 100%", "Freeship đơn từ 299K", "Đổi trả 30 ngày", "PayOS & COD")) {
            Assert.assertTrue(trustText.contains(commitment),
                    "Trust strip phai co cam ket '" + commitment + "', dang thay: " + trustText);
        }
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_FUNC_012: Bấm icon danh mục \"Son môi\" thì lọc theo Son môi và cuộn tới lưới sản phẩm")
    public void categoryIconFiltersProductsAndScrollsToGrid() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        List<String> pillNames = catalog.getVisibleCategoryPillNames();
        homePage.clickCategoryIcon("son-moi");

        String activeCategory = catalog.getActiveCategoryPillName();
        Assert.assertNotNull(activeCategory,
                "Bam icon danh muc xong phai co 1 danh muc duoc chon");
        if (pillNames.contains("Son môi")) {
            Assert.assertEquals(activeCategory, "Son môi",
                    "Bam icon 'Son môi' phai loc dung danh muc Son môi");
        }
        int filtered = catalog.getGridProductCount();
        Assert.assertTrue(filtered >= 1 && filtered <= 12,
                "Loc theo icon danh muc phai co ket qua hop le, dang thay " + filtered);
        Assert.assertTrue(homePage.isProductsSectionInViewport(),
                "Bam icon danh muc xong #products-section phai nam trong khung nhin");
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_UI_013: Brand strip hiển thị đủ 6 thương hiệu dạng chữ, bấm vào không điều hướng")
    public void brandStripShowsSixBrandsAsPlainTextWithoutNavigation() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isBrandStripDisplayed(), "Phai thay dai thuong hieu");
        String brandText = homePage.getBrandStripText();

        Assert.assertTrue(brandText.contains("Thương hiệu nổi bật"),
                "Brand strip phai co nhan 'Thương hiệu nổi bật', dang thay: " + brandText);
        for (String brand : Arrays.asList("La Roche-Posay", "L'Oréal", "3CE", "Peripera", "Clio", "CeraVe")) {
            Assert.assertTrue(brandText.contains(brand),
                    "Brand strip phai co thuong hieu '" + brand + "', dang thay: " + brandText);
        }

        String urlBefore = homePage.getCurrentPageUrl();
        homePage.clickBrandByName("3CE");
        Assert.assertEquals(homePage.getCurrentPageUrl(), urlBefore,
                "Bam vao ten thuong hieu khong duoc dieu huong di dau");
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(),
                "Bam vao ten thuong hieu xong van phai o trang chu");
    }

    @Story("Flash Sale")
    @Test(description = "TC_SCR9_FUNC_014: Flash Sale hiển thị tối đa 8 sản phẩm giảm giá xếp theo % giảm dần")
    public void flashSaleShowsUpToEightProductsOrderedByDiscountDesc() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isFlashSaleSectionDisplayed(),
                "Trang chu (xem tat ca SP) phai hien muc Flash Sale vi seed co SP giam gia");
        Assert.assertTrue(homePage.getFlashSaleSectionText().contains("Flash Sale"),
                "Muc Flash Sale phai co tieu de 'Flash Sale'");

        List<Integer> percents = homePage.getFlashSaleDiscountPercents();
        Assert.assertTrue(percents.size() >= 1 && percents.size() <= 8,
                "Flash Sale phai co 1-8 SP giam gia manh nhat, dang thay: " + percents.size());
        for (int i = 1; i < percents.size(); i++) {
            Assert.assertTrue(percents.get(i) <= percents.get(i - 1),
                    "% giam Flash Sale phai giam dan, dang thay: " + percents);
        }
    }

    @Story("Flash Sale")
    @Test(description = "TC_SCR9_FUNC_016: Flash Sale ẩn khi đã chọn danh mục và hiện lại khi bỏ chọn")
    public void flashSaleHidesWhenCategorySelectedAndReturnsWhenCleared() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isFlashSaleSectionDisplayed(),
                "Chua chon danh muc thi muc Flash Sale phai hien (seed co SP giam gia)");

        homePage.selectCategoryByIndex(0);
        Assert.assertFalse(homePage.isFlashSaleSectionPresent(),
                "Sau khi chon 1 danh muc, muc Flash Sale phai bien mat");

        homePage.selectAllCategories();
        Assert.assertTrue(homePage.isFlashSaleSectionDisplayed(),
                "Bo chon danh muc xong muc Flash Sale phai hien lai");
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_FUNC_017: Mục Bán chạy hiển thị tối đa 8 sản phẩm theo rating rồi reviewCount, testid có hậu tố -best")
    public void bestSellerSectionShowsTopEightByRatingThenReviewCount() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        List<String> gridIds = catalog.getGridProductIds();
        Assert.assertTrue(gridIds.size() > 1, "Luoi chinh phai co nhieu hon 1 SP de xep hang Ban chay");
        Assert.assertTrue(homePage.isBestSellerSectionDisplayed(), "Phai thay muc Ban chay");

        List<String> bestIds = homePage.getBestSellerCardIds();
        Assert.assertFalse(bestIds.isEmpty(), "Muc Ban chay phai co san pham");
        Assert.assertTrue(bestIds.size() <= 8,
                "Muc Ban chay toi da 8 SP, dang thay: " + bestIds.size());
        Assert.assertEquals(bestIds, expectedBestSellerOrder(gridIds, catalog),
                "Thu tu Ban chay phai dung top 8 theo rating roi reviewCount cua trang hien tai");

        // Kiem tra thu tu truc tiep tren the -best (khong phu thuoc cach tinh o tren).
        List<double[]> ratings = new ArrayList<>();
        for (String id : bestIds) {
            ratings.add(parseRating(homePage.getSectionCardRatingText(id, "-best")));
        }
        for (int i = 1; i < ratings.size(); i++) {
            Assert.assertTrue(ratings.get(i)[0] <= ratings.get(i - 1)[0]
                            || (ratings.get(i)[0] == ratings.get(i - 1)[0]
                            && ratings.get(i)[1] <= ratings.get(i - 1)[1]),
                    "Ban chay phai xep rating giam dan roi reviewCount giam dan, dang thay: " + bestIds);
        }
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_FUNC_018: Mục Gợi ý cho bạn hiển thị các sản phẩm còn lại (không nằm trong top 8 Bán chạy), testid có hậu tố -suggest")
    public void suggestedSectionShowsRemainingProductsNotInBestSellers() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        List<String> gridIds = catalog.getGridProductIds();
        if (gridIds.size() <= 8) {
            throw new SkipException("Trang hien tai chi co " + gridIds.size()
                    + " SP (<= 8) nen khong co SP con lai cho muc Goi y");
        }
        Assert.assertTrue(homePage.isBestSellerSectionDisplayed(), "Phai thay muc Ban chay");
        Assert.assertTrue(homePage.isSuggestedSectionDisplayed(), "Phai thay muc Goi y cho ban");

        Set<String> bestIds = new HashSet<>(homePage.getBestSellerCardIds());
        List<String> suggestedIds = homePage.getSuggestedCardIds();
        Assert.assertFalse(suggestedIds.isEmpty(), "Muc Goi y phai co san pham");

        Set<String> suggestedSet = new HashSet<>(suggestedIds);
        Assert.assertEquals(suggestedSet.size(), suggestedIds.size(),
                "Muc Goi y khong duoc trung lap SP: " + suggestedIds);
        for (String id : suggestedIds) {
            Assert.assertFalse(bestIds.contains(id),
                    "SP trong muc Goi y khong duoc nam trong top 8 Ban chay: " + id);
            Assert.assertTrue(gridIds.contains(id),
                    "SP trong muc Goi y phai la SP cua trang hien tai: " + id);
        }
        Set<String> expected = new HashSet<>(gridIds);
        expected.removeAll(bestIds);
        Assert.assertEquals(suggestedSet, expected,
                "Muc Goi y phai gom dung cac SP con lai cua trang hien tai (ngoai top 8 Ban chay)");
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_UI_019: Gian hàng nổi bật hiển thị đủ 4 shop, DAFL Official có \"4.9 • 12k đã bán\"")
    public void featuredShopsSectionShowsFourShops() {
        HomePageObject homePage = openHomePage();
        Assert.assertEquals(homePage.getShopCardCount(), 4,
                "Muc gian hang noi bat phai co dung 4 shop");

        for (String shopName : Arrays.asList("DAFL Official", "Lumière Beauty", "PureSkin Lab", "Rose Botanica")) {
            boolean found = false;
            for (String slug : Arrays.asList("dafl-official", "lumiere-beauty", "pureskin-lab", "rose-botanica")) {
                if (homePage.getShopCardText(slug).contains(shopName)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue(found, "Muc gian hang noi bat phai co shop '" + shopName + "'");
        }

        String officialText = homePage.getShopCardText("dafl-official");
        Assert.assertTrue(officialText.contains("4.9"),
                "Shop DAFL Official phai hien rating 4.9, dang thay: " + officialText);
        Assert.assertTrue(officialText.contains("12k đã bán"),
                "Shop DAFL Official phai hien '12k đã bán', dang thay: " + officialText);
    }

    @Story("Nội dung trang chủ")
    @Test(description = "TC_SCR9_FUNC_020: Bấm shop card chỉ bỏ lọc danh mục và cuộn tới lưới, không mở trang shop riêng")
    public void shopCardClearsCategoryFilterAndScrollsToGrid() {
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();

        homePage.selectCategoryByIndex(0);
        Assert.assertNotNull(catalog.getActiveCategoryPillName(),
                "Truoc khi bam shop card phai dang chon 1 danh muc");
        String urlBefore = homePage.getCurrentPageUrl();

        homePage.clickShopCard("dafl-official");
        Assert.assertNull(catalog.getActiveCategoryPillName(),
                "Bam shop card xong loc danh muc phai duoc bo");
        Assert.assertEquals(catalog.getGridProductCount(), 12,
                "Bam shop card xong luoi phai hien lai tat ca SP (12 SP trang dau)");
        Assert.assertEquals(homePage.getCurrentPageUrl(), urlBefore,
                "Bam shop card khong duoc mo trang shop rieng (URL phai giu nguyen)");
        Assert.assertTrue(homePage.isProductsSectionInViewport(),
                "Bam shop card xong #products-section phai nam trong khung nhin");
    }
}
