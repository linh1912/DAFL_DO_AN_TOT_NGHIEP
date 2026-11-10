package vn.dafl.pageUIs;

/** Locator cua Auth Modal (dang nhap / dang ky / dang nhap mang xa hoi) - trich tu Frontend cua DAFL.
 *  Ban CN: dang ky khong con OTP/xac thuc email, khong con o dia chi, khong con nut tai khoan demo;
 *  loi hien duoi tung o (login-*-error, register-*-error). */
public class AuthModalPageUI {

    public static final String AUTH_MODAL = "css=[data-testid='auth-modal']";
    public static final String AUTH_MODAL_TITLE = "css=[data-testid='auth-modal-title']";
    public static final String AUTH_CLOSE_BTN = "css=[data-testid='auth-close-btn']";
    public static final String AUTH_ERROR_MSG = "css=[data-testid='auth-error-msg']";
    public static final String AUTH_TAB_LOGIN = "css=[data-testid='auth-tab-login']";
    public static final String AUTH_TAB_REGISTER = "css=[data-testid='auth-tab-register']";
    public static final String REGISTER_SUCCESS_MSG = "css=[data-testid='register-success-msg']";
    public static final String SESSION_EXPIRED_NOTICE = "css=[data-testid='session-expired-notice']";

    // Form dang nhap
    public static final String LOGIN_FORM = "css=[data-testid='login-form']";
    public static final String LOGIN_EMAIL_INPUT = "css=[data-testid='login-email-input']";
    public static final String LOGIN_PASSWORD_INPUT = "css=[data-testid='login-password-input']";
    public static final String LOGIN_SUBMIT_BTN = "css=[data-testid='login-submit-btn']";
    public static final String LOGIN_EMAIL_ERROR = "css=[data-testid='login-email-error']";
    public static final String LOGIN_PASSWORD_ERROR = "css=[data-testid='login-password-error']";
    public static final String TOGGLE_LOGIN_PASSWORD_BTN = "css=[data-testid='toggle-login-password-btn']";

    // Form dang ky (CN-01: ho ten, email, SDT, mat khau, xac nhan mat khau - khong con dia chi)
    public static final String REGISTER_FORM = "css=[data-testid='register-form']";
    public static final String REGISTER_NAME_INPUT = "css=[data-testid='register-name-input']";
    public static final String REGISTER_EMAIL_INPUT = "css=[data-testid='register-email-input']";
    public static final String REGISTER_PHONE_INPUT = "css=[data-testid='register-phone-input']";
    public static final String REGISTER_PASSWORD_INPUT = "css=[data-testid='register-password-input']";
    public static final String REGISTER_CONFIRM_PASSWORD_INPUT = "css=[data-testid='register-confirm-password-input']";
    public static final String REGISTER_SUBMIT_BTN = "css=[data-testid='register-submit-btn']";
    public static final String REGISTER_NAME_ERROR = "css=[data-testid='register-name-error']";
    public static final String REGISTER_EMAIL_ERROR = "css=[data-testid='register-email-error']";
    public static final String REGISTER_PHONE_ERROR = "css=[data-testid='register-phone-error']";
    public static final String REGISTER_PASSWORD_ERROR = "css=[data-testid='register-password-error']";
    public static final String REGISTER_CONFIRM_PASSWORD_ERROR = "css=[data-testid='register-confirm-password-error']";
    public static final String TOGGLE_REGISTER_PASSWORD_BTN = "css=[data-testid='toggle-register-password-btn']";
    public static final String TOGGLE_REGISTER_CONFIRM_PASSWORD_BTN = "css=[data-testid='toggle-register-confirm-password-btn']";

    // Checklist quy tac mat khau (xanh + dau check khi dat): password-rule-len/upper/digit/special
    public static final String PASSWORD_RULE_LENGTH = "css=[data-testid='password-rule-len']";
    public static final String PASSWORD_RULE_UPPERCASE = "css=[data-testid='password-rule-upper']";
    public static final String PASSWORD_RULE_DIGIT = "css=[data-testid='password-rule-digit']";
    public static final String PASSWORD_RULE_SPECIAL = "css=[data-testid='password-rule-special']";

    // Quen mat khau (dot 13, muc 1) — AuthModal dung chung shop + admin
    // (Shared/components/auth/AuthModal.tsx): link o form dang nhap, bam vao mo
    // form xin ma 6 so (forgot-password-form), nhap ma + mat khau moi thi doi
    // o form reset-password-form. Ten testid that trong code (khong viet tat):
    // forgot-password-email-input / forgot-password-submit-btn /
    // forgot-password-back-btn / forgot-password-success / forgot-password-notice.
    public static final String FORGOT_PASSWORD_LINK = "css=[data-testid='forgot-password-link']";
    public static final String FORGOT_PASSWORD_FORM = "css=[data-testid='forgot-password-form']";
    public static final String FORGOT_PASSWORD_EMAIL_INPUT = "css=[data-testid='forgot-password-email-input']";
    public static final String FORGOT_PASSWORD_SUBMIT_BTN = "css=[data-testid='forgot-password-submit-btn']";
    public static final String FORGOT_PASSWORD_BACK_BTN = "css=[data-testid='forgot-password-back-btn']";
    public static final String FORGOT_PASSWORD_SUCCESS = "css=[data-testid='forgot-password-success']";
    public static final String FORGOT_PASSWORD_NOTICE = "css=[data-testid='forgot-password-notice']";
    public static final String RESET_PASSWORD_FORM = "css=[data-testid='reset-password-form']";
    public static final String RESET_CODE_INPUT = "css=[data-testid='reset-code-input']";
    public static final String RESET_NEW_PASSWORD_INPUT = "css=[data-testid='reset-new-password-input']";
    public static final String RESET_CONFIRM_PASSWORD_INPUT = "css=[data-testid='reset-confirm-password-input']";
    public static final String RESET_PASSWORD_SUBMIT_BTN = "css=[data-testid='reset-password-submit-btn']";

    // Trang dang nhap dang page (isModal=false): khung can giua .auth-page-wrapper
    // (data-testid='auth-page'); nut quay lai 'auth-close-btn' CHI hien o shop
    // (khong phai adminOnly) — o /login admin no phai VANG MAT (dot 13, muc 10).
    public static final String AUTH_PAGE = "css=[data-testid='auth-page']";

    // Wave15.1-auto (TC_SCR2_FUNC_018/019, TC_SCR2_VAL_020/021/022, TC_SCR2_FUNC_023/024):
    // loi + checklist buoc 2 quen mat khau, theo inventory §10.3 (testid that trong AuthModal.tsx).
    public static final String FORGOT_PASSWORD_EMAIL_ERROR = "css=[data-testid='forgot-password-email-error']";
    public static final String FORGOT_PASSWORD_ERROR = "css=[data-testid='forgot-password-error']";
    public static final String RESET_CODE_ERROR = "css=[data-testid='reset-code-error']";
    public static final String RESET_NEW_PASSWORD_ERROR = "css=[data-testid='reset-new-password-error']";
    public static final String RESET_CONFIRM_PASSWORD_ERROR = "css=[data-testid='reset-confirm-password-error']";
    public static final String RESET_PASSWORD_ERROR = "css=[data-testid='reset-password-error']";
    public static final String RESET_PASSWORD_RULE_LENGTH = "css=[data-testid='reset-password-rule-len']";
    public static final String RESET_PASSWORD_RULE_UPPERCASE = "css=[data-testid='reset-password-rule-upper']";
    public static final String RESET_PASSWORD_RULE_DIGIT = "css=[data-testid='reset-password-rule-digit']";
    public static final String RESET_PASSWORD_RULE_SPECIAL = "css=[data-testid='reset-password-rule-special']";

}
