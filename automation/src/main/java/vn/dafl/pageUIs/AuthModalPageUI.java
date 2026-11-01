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

    // Dang nhap mang xa hoi (mock o moi truong test)
    public static final String SOCIAL_GOOGLE_BTN = "css=[data-testid='social-google-btn']";
    public static final String SOCIAL_FACEBOOK_BTN = "css=[data-testid='social-facebook-btn']";
    public static final String SOCIAL_ERROR = "css=[data-testid='social-error']";
    public static final String SOCIAL_LOADING = "css=[data-testid='social-loading']";
    public static final String SOCIAL_MOCK_PANEL = "css=[data-testid='social-mock-panel']";
    public static final String SOCIAL_MOCK_CONFIRM_BTN = "css=[data-testid='social-mock-confirm-btn']";
    public static final String SOCIAL_MOCK_CANCEL_BTN = "css=[data-testid='social-mock-cancel-btn']";
}
