package vn.dafl.tests.web.membership;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.MembershipPageObject;
import vn.dafl.pageObjects.SiteFooterPageObject;

/**
 * Trang Hoi vien rieng /membership + lien ket chan trang (dot 13, muc 2 + muc 9).
 * - MEM-01: da dang nhap mo /membership tu menu tai khoan -> thay hang hien tai,
 *   so du diem, quyen loi, lich su diem, voucher cua toi.
 * - MEM-02: chua dang nhap mo thang /membership -> bi chuyen ve /login.
 * - FTR-01: chan trang co du 15 link footer-link-* deu tro toi route co that;
 *   bam 'Tra cứu đơn hàng' -> ve /orders (can dang nhap truoc vi /orders la
 *   trang rieng tu).
 */
@Feature("Hội viên")
public class MembershipTest extends BaseTest {

    @Story("Trang hội viên")
    @Test(description = "MEM-01 (đợt 13): Đăng nhập mở /membership từ menu tài khoản -> hiện hạng, số dư điểm, quyền lợi, lịch sử điểm, voucher")
    public void loggedInMembershipPageShowsTierPointsHistoryAndVouchers() {
        HomePageObject homePage = loginWithDemoAccount();
        MembershipPageObject membershipPage = homePage.openMembershipPage();

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/membership"),
                "Mo tu menu tai khoan phai toi dung /membership");
        Assert.assertTrue(membershipPage.isMembershipPageDisplayed(), "Phai thay trang hoi vien (membership-page)");
        Assert.assertTrue(membershipPage.isTierDisplayed(), "Phai thay khoi hang hoi vien (membership-tier)");
        Assert.assertFalse(membershipPage.getTierText().isEmpty(), "Hang hoi vien phai co noi dung");
        Assert.assertTrue(membershipPage.isPointsBalanceDisplayed(), "Phai thay so du diem (membership-points-balance)");
        Assert.assertFalse(membershipPage.getPointsBalanceText().isEmpty(), "So du diem phai co noi dung");
        Assert.assertTrue(membershipPage.isBenefitsDisplayed(), "Phai thay khoi quyen loi theo hang (membership-benefits)");
        Assert.assertTrue(membershipPage.isHistoryDisplayed(), "Phai thay khoi lich su diem (membership-history)");
        Assert.assertTrue(membershipPage.isVouchersDisplayed(), "Phai thay khoi voucher cua toi (membership-vouchers)");
        Assert.assertTrue(membershipPage.isRefreshButtonDisplayed(), "Phai thay nut lam moi (membership-refresh-btn)");

        membershipPage.clickRefreshButton();
        Assert.assertTrue(membershipPage.isMembershipPageDisplayed(),
                "Bam lam moi xong trang hoi vien van hien day du");
    }

    @Story("Trang hội viên")
    @Test(description = "MEM-02 (đợt 13): Chưa đăng nhập mở thẳng /membership bị chuyển về /login")
    public void membershipPageRedirectsToLoginWhenLoggedOut() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/membership");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "/membership khi chua dang nhap phai ve /login, dang o: " + getDriver().getCurrentUrl());
    }

    @Story("Chân trang")
    @Test(description = "FTR-01 (đợt 13): Chân trang có đủ 15 link footer-link-* đều có href thật; bấm 'Tra cứu đơn hàng' -> /orders")
    public void footerLinksHaveRealHrefsAndTrackOrderGoesToOrders() {
        HomePageObject homePage = loginWithDemoAccount();
        SiteFooterPageObject footer = new SiteFooterPageObject();

        Assert.assertEquals(footer.getFooterLinkCount(), 15,
                "Chan trang phai co du 15 link footer-link-*");
        for (String linkKey : SiteFooterPageObject.FOOTER_LINK_KEYS) {
            String href = footer.getFooterLinkHref(linkKey);
            Assert.assertNotNull(href, "Link footer '" + linkKey + "' phai ton tai");
            Assert.assertFalse(href.trim().isEmpty(), "Link footer '" + linkKey + "' phai co href");
            Assert.assertFalse(href.endsWith("#") || href.contains("#"),
                    "Link footer '" + linkKey + "' phai tro toi route that, khong phai '#', dang thay: " + href);
            Assert.assertTrue(href.startsWith(GlobalConstants.getAppUrl()),
                    "Link footer '" + linkKey + "' phai tro toi cung SUT, dang thay: " + href);
        }
        String memberHref = footer.getFooterLinkHref("member");
        Assert.assertTrue(memberHref.endsWith("/membership"),
                "Link 'DAFL Member' phai tro toi /membership, dang thay: " + memberHref);

        footer.clickFooterLink("track-order");
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/orders"),
                "Bam 'Tra cứu đơn hàng' o chan trang phai toi /orders, dang o: " + getDriver().getCurrentUrl());
    }
}
