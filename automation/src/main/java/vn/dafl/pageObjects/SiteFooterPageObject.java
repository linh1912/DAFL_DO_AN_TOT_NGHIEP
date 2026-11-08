package vn.dafl.pageObjects;

import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.SiteFooterUI;

/** Page Object cho dai dang ky ban tin o chan trang (SiteFooter.tsx, dot 3).
 *  Dang ky that qua POST /api/newsletter/subscribe, luu vao DB - moi test dung
 *  email ngau nhien rieng de khong anh huong lan nhau. */
public class SiteFooterPageObject extends BasePage {

    /** Cuon xuong chan trang de thay form dang ky ban tin. */
    public void scrollToNewsletterForm() {
        scrollToElement(SiteFooterUI.NEWSLETTER_EMAIL_INPUT);
        waitForElementVisible(SiteFooterUI.NEWSLETTER_FORM);
    }

    public boolean isNewsletterFormDisplayed() {
        return isElementDisplayedWithWait(SiteFooterUI.NEWSLETTER_FORM, 10);
    }

    /** Nhap email va bam Dang ky. */
    public void subscribeNewsletter(String email) {
        scrollToNewsletterForm();
        sendKeyToElement(SiteFooterUI.NEWSLETTER_EMAIL_INPUT, email);
        clickToElement(SiteFooterUI.NEWSLETTER_SUBMIT_BTN);
        sleepInSecond(2); // cho goi API /api/newsletter/subscribe tra ve
    }

    public boolean isNewsletterSuccessDisplayed() {
        return isElementDisplayedWithWait(SiteFooterUI.NEWSLETTER_SUCCESS_MSG, 10);
    }

    public String getNewsletterSuccessText() {
        waitForElementVisible(SiteFooterUI.NEWSLETTER_SUCCESS_MSG);
        return getElementText(SiteFooterUI.NEWSLETTER_SUCCESS_MSG);
    }

    public boolean isNewsletterErrorDisplayed() {
        return isElementDisplayedWithWait(SiteFooterUI.NEWSLETTER_ERROR_MSG, 5);
    }

    public String getNewsletterErrorText() {
        waitForElementVisible(SiteFooterUI.NEWSLETTER_ERROR_MSG);
        return getElementText(SiteFooterUI.NEWSLETTER_ERROR_MSG);
    }
}
