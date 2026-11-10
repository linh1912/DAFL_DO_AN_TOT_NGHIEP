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

    // ---------- Lien ket chan trang (dot 13, muc 9) ----------
    /** 15 khoa link footer hien co trong SiteFooter.tsx (doi chieu du ca 15). */
    public static final String[] FOOTER_LINK_KEYS = {
            "category-skincare", "category-makeup", "category-hair", "category-body",
            "flash-sale", "guide", "shipping", "returns", "track-order", "faq",
            "about", "shops", "magazine", "member", "contact"
    };

    /** So link footer-link-* dang hien tren trang (ky vong 15). */
    public int getFooterLinkCount() {
        return getListWebElements(SiteFooterUI.ANY_FOOTER_LINK).size();
    }

    /** Gia tri href cua 1 link footer (URL tuyet doi do trinh duyet tra ve);
     *  null neu link khong ton tai. */
    public String getFooterLinkHref(String linkKey) {
        if (!isElementDisplayedWithWait(getDynamicLocator(SiteFooterUI.DYNAMIC_FOOTER_LINK, linkKey), 10)) {
            return null;
        }
        return getElementAttribute(getDynamicLocator(SiteFooterUI.DYNAMIC_FOOTER_LINK, linkKey), "href");
    }

    /** Cuon toi roi bam 1 link footer (chi dung cho link dan toi route can kiem tra). */
    public void clickFooterLink(String linkKey) {
        scrollToElement(SiteFooterUI.DYNAMIC_FOOTER_LINK, linkKey);
        clickToElement(SiteFooterUI.DYNAMIC_FOOTER_LINK, linkKey);
        sleepInSecond(2); // cho route moi tai xong
    }

    // ---------- Bo sung Wave15.1-auto (buoc 3, nhom footer) ----------
    // Wave15.1-auto: TC_SCR11_UI_022 — toan bo text khoi footer (brand, hotline, dong day).
    public String getFooterText() {
        scrollToElement(SiteFooterUI.SITE_FOOTER);
        waitForElementVisible(SiteFooterUI.SITE_FOOTER);
        return getElementText(SiteFooterUI.SITE_FOOTER);
    }

    // Wave15.1-auto: TC_SCR11_UI_022 — "Gian hang DAFL tren toan quoc" co phai la link khong
    // (ky vong KHONG: day la chu tinh, khong co the <a> nao chua dong chu nay trong footer).
    public boolean isNationwideShopsTextALink() {
        return getListWebElements(SiteFooterUI.FOOTER_NATIONWIDE_LINK).size() > 0;
    }
}
