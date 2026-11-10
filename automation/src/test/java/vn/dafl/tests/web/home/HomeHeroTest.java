package vn.dafl.tests.web.home;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
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
@Feature("Trang chủ")
public class HomeHeroTest extends BaseTest {

    private static final List<String> EXPECTED_SLIDE_TITLES = Arrays.asList(
            "Mỹ phẩm chính hãng",
            "Freeship đơn từ 299K",
            "Tích điểm lên hạng"
    );

    @Story("Banner hero")
    @Test(description = "HERO-01: Banner hiển thị đủ 3 slide, slide active có hộp chữ .hero-text-box với tiêu đề/phụ đề")
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

    @Story("Banner hero")
    @Test(description = "HERO-02: Nút prev/next chuyển slide đúng vòng (0 -> 1 -> 0)")
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

    @Story("Banner hero")
    @Test(description = "HERO-03: Bấm dot chuyển đúng slide tương ứng")
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

    @Story("Banner hero")
    @Test(description = "HERO-04: Banner tự chạy - slide active tự đổi sau khoảng 4 giây")
    public void heroAutoAdvancesToNextSlide() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        int fromIndex = homePage.getActiveHeroSlideIndex();
        Assert.assertTrue(fromIndex >= 0, "Phai xac dinh duoc slide active ban dau");
        homePage.waitForHeroSlideAutoAdvance(fromIndex);
    }

    @Story("Nội dung trang chủ")
    @Test(description = "HERO-05: Trang chủ hiện hàng icon 6 danh mục mỹ phẩm, dải thương hiệu, mục Bán chạy / Gợi ý")
    public void homeShowsCosmeticsCategoryIconsBrandStripAndSections() {
        HomePageObject homePage = openHomePage();
        Assert.assertTrue(homePage.isHeroCarouselDisplayed(), "Phai thay banner hero");

        Assert.assertEquals(homePage.getCategoryIconCount(), 6,
                "Phai co dung 6 icon danh muc my pham (Son moi, Cham soc da, Trang diem, Nuoc hoa, Co the, Toc)");
        Assert.assertTrue(homePage.isBrandStripDisplayed(), "Phai thay dai thuong hieu my pham");
        Assert.assertTrue(homePage.isBestSellerSectionDisplayed(), "Phai thay muc Ban chay");
        Assert.assertTrue(homePage.isSuggestedSectionDisplayed(), "Phai thay muc Goi y cho ban");
    }

    @Story("Flash Sale")
    @Test(description = "FS-01 (đợt 4): Flash Sale hiện khi có SP giảm giá - đếm ngược HH:MM:SS, 1-8 SP giảm mạnh nhất")
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

    @Story("Flash Sale")
    @Test(description = "FS-02 (đợt 4): Đếm ngược Flash Sale chạy thật - số giây còn lại giảm theo thời gian")
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

    @Story("Thông báo")
    @Test(description = "HERO-08 (đợt 8): Bấm chuông mở được modal thông báo; có danh sách thông báo hoặc trạng thái rỗng 'Chưa có thông báo'")
    public void notificationModalOpensWithListOrEmptyState() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao (notifications-modal)");

        // Tu thich ung theo du lieu: tai khoan demo co thong bao seed (chao mung, flash sale)
        // thi hien danh sach; tai khoan chua co thong bao thi hien trang thai rong.
        boolean hasItems = homePage.getNotificationItemCount() > 0;
        boolean hasEmptyState = homePage.isNotificationEmptyStateDisplayed();
        Assert.assertTrue(hasItems || hasEmptyState,
                "Modal thong bao phai co it nhat 1 thong bao hoac dong 'Chưa có thông báo'");
        if (hasItems) {
            String modalText = homePage.getNotificationModalText();
            Assert.assertTrue(modalText.contains("Thông báo"),
                    "Modal thong bao phai co tieu de 'Thông báo'");
        }

        homePage.closeNotifications();
        Assert.assertFalse(homePage.isNotificationModalDisplayed(),
                "Dong modal xong khong duoc con thay modal thong bao");
    }

    @Story("Thông báo")
    @Test(description = "HERO-09 (đợt 8): Modal thông báo có nút 'Đánh dấu đã đọc tất cả' ở cùng hàng tiêu đề")
    public void notificationModalHasMarkAllReadButton() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao truoc");
        Assert.assertTrue(homePage.isMarkAllReadButtonDisplayed(),
                "Modal thong bao phai co nut 'Đánh dấu đã đọc tất cả'");
        homePage.closeNotifications();
    }

    @Story("Chế độ tối")
    @Test(description = "HERO-10 (đợt 8): Nút chuyển đổi dark mode đổi được data-theme light <-> dark và đổi lại được")
    public void darkModeToggleSwitchesTheme() {
        HomePageObject homePage = openHomePage();
        String initial = homePage.getCurrentTheme();
        Assert.assertTrue(initial.equals("light") || initial.equals("dark"),
                "Theme ban dau phai la 'light' hoac 'dark', dang thay: '" + initial + "'");

        String afterFirstToggle = homePage.toggleTheme();
        Assert.assertFalse(afterFirstToggle.equals(initial),
                "Bam nut doi theme lan 1 phai doi theme (truoc: '" + initial + "', sau: '" + afterFirstToggle + "')");
        Assert.assertTrue(afterFirstToggle.equals("light") || afterFirstToggle.equals("dark"),
                "Theme sau khi doi phai la 'light' hoac 'dark', dang thay: '" + afterFirstToggle + "'");

        String afterSecondToggle = homePage.toggleTheme();
        Assert.assertEquals(afterSecondToggle, initial,
                "Bam nut doi theme lan 2 phai tra ve theme ban dau '" + initial + "', dang thay: '" + afterSecondToggle + "'");
    }
}
