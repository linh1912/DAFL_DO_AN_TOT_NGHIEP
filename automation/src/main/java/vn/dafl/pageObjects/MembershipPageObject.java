package vn.dafl.pageObjects;

import vn.dafl.commons.BasePage;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageUIs.MembershipPageUI;

/** Page Object cho trang Hoi vien rieng /membership (dot 13, muc 2).
 *  Trang chi hien day du khi da dang nhap (GET /api/membership/me + /points +
 *  /vouchers); chua dang nhap thi App chuyen thang ve /login. */
public class MembershipPageObject extends BasePage {

    /** Mo thang /membership tren web ban hang. */
    public void openMembershipPage() {
        openPageUrl(GlobalConstants.getAppUrl() + "/membership");
    }

    /** Cho toi khi trang hoi vien tai xong: khoi hang + so du diem da hien. */
    public void waitForMembershipLoaded() {
        waitForElementVisible(MembershipPageUI.MEMBERSHIP_PAGE);
        waitForElementVisible(MembershipPageUI.MEMBERSHIP_TIER);
        waitForElementVisible(MembershipPageUI.MEMBERSHIP_POINTS_BALANCE);
    }

    public boolean isMembershipPageDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_PAGE, 10);
    }

    public boolean isTierDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_TIER, 10);
    }

    public String getTierText() {
        waitForElementVisible(MembershipPageUI.MEMBERSHIP_TIER);
        return getElementText(MembershipPageUI.MEMBERSHIP_TIER);
    }

    public boolean isPointsBalanceDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_POINTS_BALANCE, 10);
    }

    public String getPointsBalanceText() {
        waitForElementVisible(MembershipPageUI.MEMBERSHIP_POINTS_BALANCE);
        return getElementText(MembershipPageUI.MEMBERSHIP_POINTS_BALANCE);
    }

    public boolean isBenefitsDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_BENEFITS, 10);
    }

    public boolean isHistoryDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_HISTORY, 10);
    }

    public boolean isVouchersDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_VOUCHERS, 10);
    }

    public boolean isRefreshButtonDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.MEMBERSHIP_REFRESH_BTN, 10);
    }

    public void clickRefreshButton() {
        clickToElement(MembershipPageUI.MEMBERSHIP_REFRESH_BTN);
        sleepInSecond(2); // cho GET /api/membership/me + /points + /vouchers tai lai
    }

    public boolean isLoginRequiredDisplayed() {
        return isElementDisplayed(MembershipPageUI.MEMBERSHIP_LOGIN_REQUIRED);
    }
}
