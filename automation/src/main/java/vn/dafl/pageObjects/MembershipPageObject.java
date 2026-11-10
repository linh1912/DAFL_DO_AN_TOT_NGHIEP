package vn.dafl.pageObjects;

import org.openqa.selenium.Keys;
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

    /** So du diem kha dung: lay cum so DAU TIEN trong dong so du diem
     *  (vd "1.250 điểm" -> 1250); 0 neu khong doc duoc so nao. */
    public int getAvailablePoints() {
        String text = getPointsBalanceText();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("\\d[\\d.]*").matcher(text);
        if (!matcher.find()) {
            return 0;
        }
        String digits = matcher.group().replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    // ---------- Ngay sinh + voucher sinh nhat (dot 15) ----------
    public boolean isBirthdateSectionDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDATE_SECTION, 10);
    }

    public boolean isBirthdateInputDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDATE_INPUT, 5);
    }

    /** Gia tri ngay sinh hien tai (yyyy-MM-dd); rong neu tai khoan chua dat. */
    public String getBirthdateValue() {
        return getElementValue(MembershipPageUI.BIRTHDATE_INPUT);
    }

    public boolean isBirthdateSaveButtonDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDATE_SAVE_BTN, 5);
    }

    /** Nhap ngay sinh (yyyy-MM-dd) vao o <input type="date">.
     *  Chrome hien lich theo dinh dang vi-VN (dd/mm/yyyy) nen go theo thu tu
     *  ddMMyyyy; neu go phim khong an thi dat truc tiep bang native setter
     *  + su kien input/change de React nhan gia tri moi. */
    public void setBirthdate(String isoDate) {
        String[] parts = isoDate.split("-"); // yyyy-MM-dd
        String typed = parts[2] + parts[1] + parts[0];
        waitForElementVisible(MembershipPageUI.BIRTHDATE_INPUT);
        getWebElement(MembershipPageUI.BIRTHDATE_INPUT).click();
        getWebElement(MembershipPageUI.BIRTHDATE_INPUT).sendKeys(Keys.chord(Keys.CONTROL, "a"));
        getWebElement(MembershipPageUI.BIRTHDATE_INPUT).sendKeys(Keys.DELETE);
        getWebElement(MembershipPageUI.BIRTHDATE_INPUT).sendKeys(typed);
        sleepInSecond(1);
        if (!isoDate.equals(getBirthdateValue())) {
            executeScript(
                    "var el = arguments[0];"
                            + "var setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;"
                            + "setter.call(el, arguments[1]);"
                            + "el.dispatchEvent(new Event('input', {bubbles: true}));"
                            + "el.dispatchEvent(new Event('change', {bubbles: true}));",
                    getWebElement(MembershipPageUI.BIRTHDATE_INPUT), isoDate);
            sleepInSecond(1);
        }
    }

    public void clickSaveBirthdate() {
        clickToElement(MembershipPageUI.BIRTHDATE_SAVE_BTN);
        sleepInSecond(2); // cho BE luu ngay sinh + FE hien thong bao
    }

    public boolean isBirthdateErrorDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDATE_ERROR_MSG, 5);
    }

    public String getBirthdateErrorMessage() {
        waitForElementVisible(MembershipPageUI.BIRTHDATE_ERROR_MSG);
        return getElementText(MembershipPageUI.BIRTHDATE_ERROR_MSG);
    }

    public boolean isBirthdateSuccessDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDATE_SUCCESS_MSG, 10);
    }

    public String getBirthdateSuccessMessage() {
        waitForElementVisible(MembershipPageUI.BIRTHDATE_SUCCESS_MSG);
        return getElementText(MembershipPageUI.BIRTHDATE_SUCCESS_MSG);
    }

    public boolean isClaimBirthdayVoucherButtonDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.CLAIM_BIRTHDAY_VOUCHER_BTN, 5);
    }

    /** Bam nut nhan voucher sinh nhat (co the tao voucher that neu du dieu kien). */
    public void clickClaimBirthdayVoucher() {
        clickToElement(MembershipPageUI.CLAIM_BIRTHDAY_VOUCHER_BTN);
        sleepInSecond(2); // cho BE tra ve ket qua + FE hien thong bao
    }

    public boolean isBirthdayVoucherErrorDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDAY_VOUCHER_ERROR_MSG, 5);
    }

    public String getBirthdayVoucherErrorMessage() {
        waitForElementVisible(MembershipPageUI.BIRTHDAY_VOUCHER_ERROR_MSG);
        return getElementText(MembershipPageUI.BIRTHDAY_VOUCHER_ERROR_MSG);
    }

    public boolean isBirthdayVoucherSuccessDisplayed() {
        return isElementDisplayedWithWait(MembershipPageUI.BIRTHDAY_VOUCHER_SUCCESS_MSG, 10);
    }

    public String getBirthdayVoucherSuccessMessage() {
        waitForElementVisible(MembershipPageUI.BIRTHDAY_VOUCHER_SUCCESS_MSG);
        return getElementText(MembershipPageUI.BIRTHDAY_VOUCHER_SUCCESS_MSG);
    }
}
