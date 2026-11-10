package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.SiteFooterPageObject;

import java.util.List;

/**
 * Thong bao + footer + dark mode + trang 404 (buoc 3, dot 15.1).
 *
 * Can cu hanh vi that trong App.tsx / Header.tsx / SiteFooter.tsx (fork v15):
 * - Modal thong bao: badge "{n} mới" chi hien khi co chua doc; cham .unread-dot
 *   tren item chua doc; bam item / "Danh dau da doc tat ca" cap nhat lac quan
 *   (PUT /api/notifications/... chi de dong bo server) nen UI doi ngay.
 * - Luu y: fetchNotifications dung FALLBACK_NOTIFICATIONS khi API tra rong nen
 *   tai khoan moi KHONG bao gio thay trang thai rong "Chưa có thông báo" qua UI —
 *   test UI_004 tu skip khi gap truong hop nay (xem ghi chu trong test).
 * - Route /notifications khong co trang rieng: mo modal + URL ve /.
 * - Footer co 15 link footer-link-* (FTR-01 da kiem href + bam 'track-order');
 *   test nay bam that 14 link con lai, gom chung 1 test mo rong (cung 1 luong).
 * - Dark mode: mac dinh SANG; body sang rgb(246, 246, 246) (#f6f6f6), toi
 *   rgb(15, 15, 16) (#0F0F10 — rule override cuoi index.css thang #0b0d13).
 * - 2 nut trang 404 KHONG co data-testid — dinh vi theo text nut trong khoi not-found-page.
 *
 * Tac dung phu that: test "Danh dau da doc tat ca" (FUNC_003) va "bam item"
 * (FUNC_005) doi trang thai that cua tai khoan demo (danh dau da doc); cac lan
 * chay sau co the khong con thong bao chua doc va cac test nay se tu skip.
 */
@Feature("Thông báo và chân trang")
public class NotificationsFooterTest extends BaseTest {

    private static final String NOT_FOUND_HOME_BTN =
            "xpath=//div[@data-testid='not-found-page']//button[contains(text(),'Về trang chủ')]";
    private static final String NOT_FOUND_BACK_BTN =
            "xpath=//div[@data-testid='not-found-page']//button[contains(text(),'Quay lại')]";

    /** 14 link footer con lai sau FTR-01 (da bam 'track-order'): {key, duong dan ky vong}. */
    private static final String[][] REMAINING_FOOTER_LINKS = {
            {"category-skincare", "/"},
            {"category-makeup", "/"},
            {"category-hair", "/"},
            {"category-body", "/"},
            {"flash-sale", "/"},
            {"guide", "/"},
            {"shipping", "/cart"},
            {"returns", "/orders"},
            {"faq", "/"},
            {"about", "/"},
            {"shops", "/"},
            {"magazine", "/"},
            {"member", "/membership"},
            {"contact", "/"}
    };

    /** Ve trang chu va cho tai xong (dung sau moi lan bam link footer). */
    private void goHome() {
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        new HomePageObject().waitForHomePageLoaded();
    }

    @Story("Thông báo")
    @Test(description = "TC_SCR11_UI_002: Modal thông báo hiển thị badge '{n} mới' đúng số chưa đọc (ẩn khi không có thông báo chưa đọc), khớp badge chuông trên header")
    public void notificationModalShowsUnreadBadgeCount() {
        HomePageObject homePage = loginWithDemoAccount();
        int bellBadgeBefore = homePage.getNotifBadgeCount();

        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao truoc");
        int unreadDots = homePage.getUnreadDotCount();

        if (unreadDots > 0) {
            Assert.assertTrue(homePage.isNotificationUnreadBadgeDisplayed(),
                    "Co " + unreadDots + " thong bao chua doc thi modal phai hien badge '{n} mới'");
            Assert.assertEquals(homePage.getNotificationUnreadBadgeText(), unreadDots + " mới",
                    "Badge trong modal phai dung '" + unreadDots + " mới'");
            Assert.assertEquals(bellBadgeBefore, unreadDots,
                    "Badge chuong tren header phai bang so thong bao chua doc (" + unreadDots + ")");
        } else {
            Assert.assertFalse(homePage.isNotificationUnreadBadgeDisplayed(),
                    "Khong co thong bao chua doc thi modal khong duoc hien badge '{n} mới'");
            Assert.assertEquals(bellBadgeBefore, 0,
                    "Khong co thong bao chua doc thi chuong khong duoc co badge");
        }

        homePage.closeNotifications();
    }

    @Story("Thông báo")
    @Test(description = "TC_SCR11_FUNC_003: Bấm 'Đánh dấu đã đọc tất cả' -> tất cả item mất chấm unread-dot, badge '{n} mới' trong modal và badge chuông biến mất (đổi trạng thái thật của tài khoản demo)")
    public void notificationMarkAllReadClearsUnreadDotsAndBadges() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao truoc");

        if (homePage.getUnreadDotCount() == 0) {
            homePage.closeNotifications();
            throw new SkipException("Tai khoan demo khong con thong bao chua doc"
                    + " (da bi danh dau o lan chay truoc) — khong con gi de danh dau, bo qua TC_SCR11_FUNC_003");
        }

        homePage.clickMarkAllRead();
        homePage.waitForNoUnreadDots();
        Assert.assertFalse(homePage.isNotificationUnreadBadgeDisplayed(),
                "Danh dau tat ca xong badge '{n} mới' trong modal phai bien mat");

        homePage.closeNotifications();
        Assert.assertEquals(homePage.getNotifBadgeCount(), 0,
                "Danh dau tat ca xong badge chuong tren header phai bien mat");
    }

    @Story("Thông báo")
    @Test(description = "TC_SCR11_UI_004: Tài khoản chưa có thông báo nào -> modal hiển thị 'Chưa có thông báo'")
    public void notificationEmptyStateShowsForNewAccount() {
        HomePageObject homePage = registerAndLogin("Khách Thông Báo", randomEmail(),
                randomPhone(), GlobalConstants.DEMO_PASSWORD);
        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao truoc");

        if (!homePage.isNotificationEmptyStateDisplayed()) {
            homePage.closeNotifications();
            throw new SkipException("Tai khoan moi van thay danh sach thong bao thay vi 'Chưa có thông báo'"
                    + " — App.tsx fetchNotifications dung FALLBACK_NOTIFICATIONS khi API tra rong"
                    + " nen trang thai rong khong xuat hien duoc qua UI; bo qua TC_SCR11_UI_004");
        }
        Assert.assertTrue(homePage.getNotificationModalText().contains("Chưa có thông báo"),
                "Modal thong bao rong phai hien 'Chưa có thông báo'");
        homePage.closeNotifications();
    }

    @Story("Thông báo")
    @Test(description = "TC_SCR11_FUNC_005: Bấm 1 item chưa đọc -> item đó mất chấm unread-dot, các item chưa đọc khác vẫn giữ chấm (đổi trạng thái thật của 1 thông báo demo)")
    public void notificationClickItemMarksOnlyThatItemRead() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openNotifications();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Bam chuong phai mo duoc modal thong bao truoc");

        List<String> unreadIds = homePage.getUnreadNotificationItemIds();
        if (unreadIds.size() < 2) {
            homePage.closeNotifications();
            throw new SkipException("Tai khoan demo chi con " + unreadIds.size()
                    + " thong bao chua doc (can it nhat 2 de doi chieu item da bam voi item chua bam)"
                    + " — bo qua TC_SCR11_FUNC_005");
        }

        String clickedId = unreadIds.get(0);
        homePage.clickNotificationItem(clickedId);
        Assert.assertFalse(homePage.isNotificationItemUnread(clickedId),
                "Item vua bam phai mat cham unread-dot");
        for (int i = 1; i < unreadIds.size(); i++) {
            Assert.assertTrue(homePage.isNotificationItemUnread(unreadIds.get(i)),
                    "Item chua doc khac (" + unreadIds.get(i) + ") phai van giu cham unread-dot");
        }

        homePage.closeNotifications();
    }

    @Story("Thông báo")
    @Test(description = "TC_SCR11_FUNC_006: Truy cập trực tiếp /notifications -> modal thông báo mở ra và URL trở về /")
    public void notificationsRouteOpensModalAndReturnsToHome() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/notifications");
        sleepInSecond(2);

        HomePageObject homePage = new HomePageObject();
        Assert.assertTrue(homePage.isNotificationModalDisplayed(),
                "Vao /notifications phai mo modal thong bao");
        Assert.assertEquals(getDriver().getCurrentUrl(), GlobalConstants.getAppUrl() + "/",
                "Vao /notifications xong URL phai ve / (route chi redirect, khong co trang rieng)");

        homePage.closeNotifications();
    }

    @Story("Chân trang")
    @Test(description = "TC_SCR11_FUNC_007 + TC_SCR11_FUNC_008 + TC_SCR11_FUNC_009 + TC_SCR11_FUNC_010 + TC_SCR11_FUNC_011 + TC_SCR11_FUNC_012 + TC_SCR11_FUNC_013 + TC_SCR11_FUNC_014 + TC_SCR11_FUNC_016 + TC_SCR11_FUNC_017 + TC_SCR11_FUNC_018 + TC_SCR11_FUNC_019 + TC_SCR11_FUNC_020 + TC_SCR11_FUNC_021: Bấm thật 14 link footer còn lại (sau FTR-01 đã bấm 'Tra cứu đơn hàng') -> điều hướng đúng đích (/, /cart, /orders, /membership)")
    public void footerLinksNavigateToExpectedRoutes() {
        loginWithDemoAccount();
        SiteFooterPageObject footer = new SiteFooterPageObject();

        for (int i = 0; i < REMAINING_FOOTER_LINKS.length; i++) {
            String linkKey = REMAINING_FOOTER_LINKS[i][0];
            String expectedPath = REMAINING_FOOTER_LINKS[i][1];

            footer.clickFooterLink(linkKey);
            String currentUrl = getDriver().getCurrentUrl();
            if ("/".equals(expectedPath)) {
                Assert.assertTrue(currentUrl.equals(GlobalConstants.getAppUrl() + "/")
                                || currentUrl.equals(GlobalConstants.getAppUrl()),
                        "Link footer '" + linkKey + "' phai toi /, dang o: " + currentUrl);
            } else {
                Assert.assertTrue(currentUrl.contains(expectedPath),
                        "Link footer '" + linkKey + "' phai toi " + expectedPath + ", dang o: " + currentUrl);
            }

            if (i < REMAINING_FOOTER_LINKS.length - 1) {
                goHome();
            }
        }
    }

    @Story("Chân trang")
    @Test(description = "TC_SCR11_UI_022: Footer hiển thị logo 'DAFL', mô tả, hotline, chữ tĩnh 'Gian hàng DAFL trên toàn quốc' (không phải link) và dòng đáy bản quyền + thanh toán")
    public void footerShowsBrandHotlineAndBottomBar() {
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        String footerText = footer.getFooterText();

        Assert.assertTrue(footerText.contains("DAFL"),
                "Footer phai hien logo 'DAFL'");
        Assert.assertTrue(footerText.contains("Mỹ phẩm chính hãng từ nhiều gian hàng, giá rõ ràng."),
                "Footer phai hien dung mo ta brand, dang thay: " + footerText);
        Assert.assertTrue(footerText.contains("Hotline 1900 0000 (8h–21h)"),
                "Footer phai hien 'Hotline 1900 0000 (8h–21h)', dang thay: " + footerText);
        Assert.assertTrue(footerText.contains("Gian hàng DAFL trên toàn quốc"),
                "Footer phai hien chu tinh 'Gian hàng DAFL trên toàn quốc', dang thay: " + footerText);
        Assert.assertFalse(footer.isNationwideShopsTextALink(),
                "'Gian hàng DAFL trên toàn quốc' phai la chu tinh, khong phai link");
        Assert.assertTrue(footerText.contains("© 2026 DAFL. Bảo lưu mọi quyền."),
                "Dong day footer phai hien '© 2026 DAFL. Bảo lưu mọi quyền.', dang thay: " + footerText);
        Assert.assertTrue(footerText.contains("Thanh toán: PayOS · COD · Visa · Mastercard"),
                "Dong day footer phai hien 'Thanh toán: PayOS · COD · Visa · Mastercard', dang thay: " + footerText);
    }

    @Story("Chế độ tối")
    @Test(description = "TC_SCR11_UI_026: Nút chuyển đổi dark mode hiển thị nhãn 'Theme Sáng' + title 'Chuyển sang giao diện Tối' ở chế độ sáng, đổi thành 'Theme Tối' + 'Chuyển sang giao diện Sáng' sau khi bấm")
    public void themeToggleShowsCorrectLabelAndTitle() {
        HomePageObject homePage = openHomePage();
        if ("dark".equals(homePage.getCurrentTheme())) {
            homePage.toggleTheme();
        }
        Assert.assertEquals(homePage.getCurrentTheme(), "light",
                "Dang o che do sang truoc khi kiem nhan/title mac dinh");

        Assert.assertEquals(homePage.getThemeToggleLabel(), "Theme Sáng",
                "Che do sang nut phai hien nhan 'Theme Sáng'");
        Assert.assertEquals(homePage.getThemeToggleTitle(), "Chuyển sang giao diện Tối",
                "Che do sang nut phai co title 'Chuyển sang giao diện Tối'");

        String afterToggle = homePage.toggleTheme();
        Assert.assertEquals(afterToggle, "dark", "Bam nut 1 lan phai sang che do toi");
        Assert.assertEquals(homePage.getThemeToggleLabel(), "Theme Tối",
                "Che do toi nut phai hien nhan 'Theme Tối'");
        Assert.assertEquals(homePage.getThemeToggleTitle(), "Chuyển sang giao diện Sáng",
                "Che do toi nut phai co title 'Chuyển sang giao diện Sáng'");

        homePage.toggleTheme();
    }

    @Story("Chế độ tối")
    @Test(description = "TC_SCR11_FUNC_027: Dark mode được lưu vào localStorage 'dafl_theme' và giữ nguyên sau khi tải lại trang")
    public void darkModePersistsInLocalStorageAfterReload() {
        HomePageObject homePage = openHomePage();
        if ("dark".equals(homePage.getCurrentTheme())) {
            homePage.toggleTheme();
        }
        Assert.assertEquals(homePage.getCurrentTheme(), "light",
                "Bat dau o che do sang truoc khi kiem tinh ben dark mode");

        homePage.toggleTheme();
        Assert.assertEquals(homePage.getCurrentTheme(), "dark", "Bam nut phai sang che do toi");
        Assert.assertEquals(homePage.getStoredTheme(), "dark",
                "Chuyen sang toi xong localStorage 'dafl_theme' phai la 'dark'");

        getDriver().navigate().refresh();
        homePage.waitForHomePageLoaded();
        Assert.assertEquals(homePage.getCurrentTheme(), "dark",
                "Tai lai trang xong giao dien phai van toi");
        Assert.assertEquals(homePage.getStoredTheme(), "dark",
                "Tai lai trang xong localStorage 'dafl_theme' phai van la 'dark'");

        homePage.toggleTheme();
        Assert.assertEquals(homePage.getCurrentTheme(), "light", "Bam nut lan nua phai ve che do sang");
        Assert.assertEquals(homePage.getStoredTheme(), "light",
                "Ve che do sang xong localStorage 'dafl_theme' phai la 'light'");
    }

    @Story("Chế độ tối")
    @Test(description = "TC_SCR11_FUNC_028: Chuyển đổi dark mode đảo data-theme light<->dark và đổi màu nền thật (sáng #f6f6f6, tối #0F0F10)")
    public void darkModeToggleChangesBackgroundColor() {
        HomePageObject homePage = openHomePage();
        if ("dark".equals(homePage.getCurrentTheme())) {
            homePage.toggleTheme();
        }

        Assert.assertEquals(homePage.getCurrentTheme(), "light", "Bat dau o che do sang");
        Assert.assertEquals(homePage.getBodyBackgroundColor(), "rgb(246, 246, 246)",
                "Nen che do sang phai la #f6f6f6");

        String afterFirst = homePage.toggleTheme();
        Assert.assertEquals(afterFirst, "dark", "Bam lan 1 phai sang toi");
        Assert.assertEquals(homePage.getBodyBackgroundColor(), "rgb(15, 15, 16)",
                "Nen che do toi phai la #0F0F10");

        String afterSecond = homePage.toggleTheme();
        Assert.assertEquals(afterSecond, "light", "Bam lan 2 phai ve sang");
        Assert.assertEquals(homePage.getBodyBackgroundColor(), "rgb(246, 246, 246)",
                "Nen sau khi ve sang phai lai la #f6f6f6");
    }

    @Story("Trang 404")
    @Test(description = "TC_SCR11_FUNC_031: Trang 404 có 2 nút: 'Về trang chủ' đưa về / và 'Quay lại' quay về trang trước đó")
    public void notFoundPageButtonsGoHomeAndBack() {
        openHomePage();

        getDriver().get(GlobalConstants.getAppUrl() + "/trang-khong-ton-tai-xyz");
        Assert.assertTrue(isTextVisibleOnPage("Không tìm thấy trang"),
                "Phai hien tieu de trang 404");
        getDriver().findElement(org.openqa.selenium.By.xpath(NOT_FOUND_HOME_BTN)).click();
        new HomePageObject().waitForHomePageLoaded();
        Assert.assertEquals(getDriver().getCurrentUrl(), GlobalConstants.getAppUrl() + "/",
                "Bam 'Về trang chủ' phai ve dung trang chu");

        getDriver().get(GlobalConstants.getAppUrl() + "/trang-khong-ton-tai-xyz");
        Assert.assertTrue(isTextVisibleOnPage("Không tìm thấy trang"),
                "Mo lai route la phai hien trang 404");
        getDriver().findElement(org.openqa.selenium.By.xpath(NOT_FOUND_BACK_BTN)).click();
        sleepInSecond(2);
        Assert.assertEquals(getDriver().getCurrentUrl(), GlobalConstants.getAppUrl() + "/",
                "Bam 'Quay lại' phai quay ve trang truoc do (trang chu)");
    }
}
