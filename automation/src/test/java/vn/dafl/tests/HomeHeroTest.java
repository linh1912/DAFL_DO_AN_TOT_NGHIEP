package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.HomePageObject;

import java.util.Arrays;
import java.util.List;

/**
 * Banner hero trang chu + khoi noi dung kieu Cocolux (dot 2-4, web DAFL cuoi cung).
 * Banner: 3 slide tu chay 4s/slide, chu nam trong hop toi mo .hero-text-box tren anh,
 * nut prev/next + dots chuyen slide. Duoi banner: hang icon 6 danh muc my pham,
 * dai thuong hieu, muc Ban chay / Goi y cho ban.
 */
public class HomeHeroTest extends BaseTest {

    private static final List<String> EXPECTED_SLIDE_TITLES = Arrays.asList(
            "Mỹ phẩm chính hãng",
            "Freeship đơn từ 299K",
            "Tích điểm lên hạng"
    );

    @Test(description = "HERO-01: Banner hien thi du 3 slide, slide active co hop chu .hero-text-box voi tieu de/phu de")
    public void heroCarouselDisplaysThreeSlidesWithTextBox() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero tren trang chu");

        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(homePage.isElementDisplayed(
                    vn.dafl.pageUIs.HomePageUI.DYNAMIC_HERO_SLIDE, String.valueOf(i)),
                    "Phai co slide hero-slide-" + i);
            Assert.assertTrue(homePage.isElementDisplayed(
                    vn.dafl.pageUIs.HomePageUI.DYNAMIC_HERO_DOT, String.valueOf(i)),
                    "Phai co dot hero-dot-" + i);
        }

        Assert.assertTrue(homePage.isActiveHeroTextBoxDisplayed(),
                "Slide active phai co hop chu .hero-text-box hien thi (khong bi anh che)");
        String title = homePage.getActiveHeroTitle();
        Assert.assertTrue(EXPECTED_SLIDE_TITLES.contains(title),
                "Tieu de slide active phai la 1 trong 3 tieu de da biet, dang thay: " + title);
        Assert.assertFalse(homePage.getActiveHeroSubtitle().isEmpty(),
                "Slide active phai co phu de");
    }

    @Test(description = "HERO-02: Nut prev/next chuyen slide dung vong (0 -> 1 -> 0)")
    public void heroPrevNextButtonsNavigateSlides() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        homePage.clickHeroDot(0);
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 0, "Dot 0 phai dua ve slide 0");

        homePage.clickHeroNext();
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 1, "Nut next phai sang slide 1");

        homePage.clickHeroPrev();
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 0, "Nut prev phai quay lai slide 0");
    }

    @Test(description = "HERO-03: Bam dot chuyen dung slide tuong ung")
    public void heroDotsNavigateToSlide() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        homePage.clickHeroDot(2);
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 2, "Dot 2 phai dua toi slide 2");
        Assert.assertTrue(EXPECTED_SLIDE_TITLES.contains(homePage.getActiveHeroTitle()),
                "Tieu de slide 2 phai dung noi dung da biet");

        homePage.clickHeroDot(1);
        Assert.assertEquals(homePage.getActiveHeroSlideIndex(), 1, "Dot 1 phai dua toi slide 1");
    }

    @Test(description = "HERO-04: Banner tu chay - slide active tu doi sau khoang 4 giay")
    public void heroAutoAdvancesToNextSlide() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        int fromIndex = homePage.getActiveHeroSlideIndex();
        Assert.assertTrue(fromIndex >= 0, "Phai xac dinh duoc slide active ban dau");
        homePage.waitForHeroSlideAutoAdvance(fromIndex);
    }

    @Test(description = "HERO-05: Trang chu hien hang icon 6 danh muc my pham, dai thuong hieu, muc Ban chay / Goi y")
    public void homeShowsCosmeticsCategoryIconsBrandStripAndSections() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        Assert.assertEquals(homePage.getCategoryIconCount(), 6,
                "Phai co dung 6 icon danh muc my pham (Son moi, Cham soc da, Trang diem, Nuoc hoa, Co the, Toc)");
        Assert.assertTrue(homePage.isBrandStripDisplayed(), "Phai thay dai thuong hieu my pham");
        Assert.assertTrue(homePage.isBestSellerSectionDisplayed(), "Phai thay muc Ban chay");
        Assert.assertTrue(homePage.isSuggestedSectionDisplayed(), "Phai thay muc Goi y cho ban");
    }

    @Test(description = "FS-01 (dot 4): Flash Sale hien khi co SP giam gia - dem nguoc HH:MM:SS, 1-8 SP giam manh nhat")
    public void flashSaleSectionDisplaysDiscountedProductsAndCountdown() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isFlashSaleSectionDisplayed(),
                "Trang chu (xem tat ca SP) phai hien muc Flash Sale vi seed co nhieu SP OriginalPrice > Price");

        String countdown = homePage.getFlashSaleCountdownText();
        Assert.assertTrue(countdown.matches("^\\d{2}:\\d{2}:\\d{2}$"),
                "Dem nguoc Flash Sale phai dang HH:MM:SS, dang thay: " + countdown);

        int itemCount = homePage.getFlashSaleItemCount();
        Assert.assertTrue(itemCount >= 1 && itemCount <= 8,
                "Flash Sale phai co 1-8 SP giam gia manh nhat, dang thay: " + itemCount);
    }

    @Test(description = "FS-02 (dot 4): Dem nguoc Flash Sale chay that - so giay con lai giam theo thoi gian")
    public void flashSaleCountdownTicksDown() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isFlashSaleSectionDisplayed(), "Phai thay muc Flash Sale");

        int before = homePage.getFlashSaleCountdownSeconds();
        Assert.assertTrue(before > 0, "Dem nguoc phai > 0 giay, dang thay: " + before);
        homePage.sleepInSecond(3);
        int after = homePage.getFlashSaleCountdownSeconds();
        Assert.assertTrue(after >= 0, "Dem nguoc sau 3 giay phai doc duoc, dang thay: " + after);
        Assert.assertTrue(after < before,
                "Dem nguoc phai giam sau 3 giay (truoc: " + before + "s, sau: " + after + "s)");
    }
}
