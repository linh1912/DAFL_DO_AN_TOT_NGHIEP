package vn.dafl.pageUIs;

/** Locator cua dai dang ky ban tin o footer (dot 3, SiteFooter.tsx).
 *  Footer KHONG co data-testid cho form nay: bam id cua o email (#newsletter-email),
 *  class cua form (.newsletter-form) va class thong bao (.newsletter-success/.newsletter-error).
 *  Hanh vi that: email sai dinh dang -> loi ngay o FE; email moi -> POST
 *  /api/newsletter/subscribe -> hien khoi cam on; email da dang ky -> BE tra 200
 *  success -> FE van hien khoi cam on (khong tao trung lap trong DB). */
public class SiteFooterUI {

    public static final String NEWSLETTER_EMAIL_INPUT = "css=#newsletter-email";
    public static final String NEWSLETTER_SUBMIT_BTN = "xpath=//form[contains(@class,'newsletter-form')]//button[@type='submit']";
    public static final String NEWSLETTER_SUCCESS_MSG = "css=.newsletter-success";
    public static final String NEWSLETTER_ERROR_MSG = "css=.newsletter-error";
    public static final String NEWSLETTER_FORM = "css=.newsletter-form";

    // Lien ket o chan trang (dot 13, muc 9): 15 link footer-link-* trong
    // SiteFooter.tsx, deu la <a> toi route co that ("/", "/cart", "/orders",
    // "/membership"). %s = phan sau 'footer-link-' (vd 'track-order').
    public static final String DYNAMIC_FOOTER_LINK = "css=[data-testid='footer-link-%s']";
    public static final String ANY_FOOTER_LINK = "css=[data-testid^='footer-link-']";
}
