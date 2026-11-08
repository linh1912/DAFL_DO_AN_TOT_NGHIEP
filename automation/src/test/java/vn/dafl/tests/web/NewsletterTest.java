package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.SiteFooterPageObject;

/**
 * Dang ky ban tin o footer web ban hang (dot 3, web DAFL cuoi cung).
 * Dang ky that qua POST /api/newsletter/subscribe, luu vao DB.
 * 3 test NL-01..03 truoc day nam trong lop admin AdminAndNewsletterTest, chuyen
 * sang day o dot 10 vi chung test footer cua web ban hang (:3000), khong phai
 * trang quan tri (:3001). Giu nguyen ID va logic test.
 */
public class NewsletterTest extends BaseTest {

    @Test(description = "NL-01: Dang ky ban tin bang email moi -> hien khoi cam on (luu that vao DB)")
    public void newsletterSubscribeSuccess() {
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter(randomEmail());

        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(), "Dang ky email moi phai hien khoi cam on");
        Assert.assertTrue(footer.getNewsletterSuccessText()
                        .contains(GlobalConstants.NEWSLETTER_SUCCESS_MESSAGE),
                "Thong diep cam on khong dung: " + footer.getNewsletterSuccessText());
    }

    @Test(description = "NL-02: Dang ky ban tin bang email sai dinh dang -> bao loi ngay, khong goi API")
    public void newsletterInvalidEmailShowsError() {
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter("email-khong-hop-le");

        Assert.assertTrue(footer.isNewsletterErrorDisplayed(), "Email sai dinh dang phai hien loi");
        Assert.assertTrue(footer.getNewsletterErrorText()
                        .contains(GlobalConstants.NEWSLETTER_INVALID_EMAIL_MESSAGE),
                "Thong diep loi email khong dung: " + footer.getNewsletterErrorText());
        Assert.assertFalse(footer.isNewsletterSuccessDisplayed(),
                "Email sai dinh dang khong duoc hien khoi cam on");
    }

    @Test(description = "NL-03: Dang ky lai email da dang ky -> van hien khoi cam on, khong bao loi, khong tao trung lap")
    public void newsletterDuplicateEmailShowsAlreadyRegistered() {
        String email = randomEmail();
        openHomePage();
        SiteFooterPageObject footer = new SiteFooterPageObject();
        footer.subscribeNewsletter(email);
        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(), "Lan dang ky dau phai thanh cong");

        // Tai lai trang chu roi dang ky lai cung email (form chi hien lai sau khi tai lai trang).
        getDriver().get(GlobalConstants.getAppUrl() + "/");
        new HomePageObject().waitForHomePageLoaded();
        footer.subscribeNewsletter(email);

        Assert.assertTrue(footer.isNewsletterSuccessDisplayed(),
                "Dang ky lai email da co phai van hien khoi cam on (BE tra 200, khong tao trung lap)");
        Assert.assertFalse(footer.isNewsletterErrorDisplayed(),
                "Dang ky lai email da co khong duoc bao loi");
    }
}
