package vn.dafl.pageObjects;

import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.AuthModalPageUI;

/** Page Object cho Auth Modal: dang nhap, dang ky (CN-01/02) bang email + mat khau.
 *  Ban CN: dang ky xong dang nhap duoc ngay (khong OTP), loi hien duoi tung o nhap. */
public class AuthModalPageObject extends BasePage {

    public void waitForModalVisible() {
        waitForElementVisible(AuthModalPageUI.AUTH_MODAL);
    }

    public boolean isLoginFormDisplayed() {
        return isElementDisplayed(AuthModalPageUI.LOGIN_FORM);
    }

    public boolean isRegisterFormDisplayed() {
        return isElementDisplayed(AuthModalPageUI.REGISTER_FORM);
    }

    /** Tieu de modal thay doi theo tab: "Dang nhap tai khoan" / "Dang ky thanh vien moi". */
    public String getModalTitle() {
        return getElementText(AuthModalPageUI.AUTH_MODAL_TITLE);
    }

    public void loginAs(String email, String password) {
        switchToLoginTab();
        sendKeyToElement(AuthModalPageUI.LOGIN_EMAIL_INPUT, email);
        sendKeyToElement(AuthModalPageUI.LOGIN_PASSWORD_INPUT, password);
        clickToElement(AuthModalPageUI.LOGIN_SUBMIT_BTN);
    }

    public void submitLogin() {
        clickToElement(AuthModalPageUI.LOGIN_SUBMIT_BTN);
    }

    /** Chi go mat khau vao form dang nhap, chua bam nut Dang nhap. */
    public void enterLoginPassword(String password) {
        sendKeyToElement(AuthModalPageUI.LOGIN_PASSWORD_INPUT, password);
    }

    /** Bam nut con mat (hien/an mat khau) tren form dang nhap. */
    public void toggleLoginPasswordVisibility() {
        clickToElement(AuthModalPageUI.TOGGLE_LOGIN_PASSWORD_BTN);
    }

    /** Thuoc tinh type cua o mat khau: "password" khi dang an, "text" khi dang hien. */
    public String getLoginPasswordInputType() {
        return getElementAttribute(AuthModalPageUI.LOGIN_PASSWORD_INPUT, "type");
    }

    /** Loi sai thong tin dang nhap hien MOT cho duy nhat: duoi o mat khau (CN-02 E1). */
    public boolean isLoginPasswordErrorDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.LOGIN_PASSWORD_ERROR, 5);
    }

    public String getLoginPasswordError() {
        waitForElementVisible(AuthModalPageUI.LOGIN_PASSWORD_ERROR);
        return getElementText(AuthModalPageUI.LOGIN_PASSWORD_ERROR);
    }

    public boolean isLoginEmailErrorDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.LOGIN_EMAIL_ERROR, 5);
    }

    public String getLoginEmailError() {
        waitForElementVisible(AuthModalPageUI.LOGIN_EMAIL_ERROR);
        return getElementText(AuthModalPageUI.LOGIN_EMAIL_ERROR);
    }

    public void switchToLoginTab() {
        if (isElementDisplayed(AuthModalPageUI.AUTH_TAB_LOGIN)) {
            clickToElement(AuthModalPageUI.AUTH_TAB_LOGIN);
        }
        waitForElementVisible(AuthModalPageUI.LOGIN_FORM);
    }

    public void switchToRegisterTab() {
        clickToElement(AuthModalPageUI.AUTH_TAB_REGISTER);
        waitForElementVisible(AuthModalPageUI.REGISTER_FORM);
    }

    /** Dang ky theo dung form CN-01: ho ten, email, SDT, mat khau, xac nhan mat khau. */
    public void register(String fullName, String email, String phone,
                         String password, String confirmPassword) {
        switchToRegisterTab();
        sendKeyToElement(AuthModalPageUI.REGISTER_NAME_INPUT, fullName);
        sendKeyToElement(AuthModalPageUI.REGISTER_EMAIL_INPUT, email);
        sendKeyToElement(AuthModalPageUI.REGISTER_PHONE_INPUT, phone);
        sendKeyToElement(AuthModalPageUI.REGISTER_PASSWORD_INPUT, password);
        sendKeyToElement(AuthModalPageUI.REGISTER_CONFIRM_PASSWORD_INPUT, confirmPassword);
        clickToElement(AuthModalPageUI.REGISTER_SUBMIT_BTN);
    }

    /** Sau dang ky thanh cong, FE chuyen ve tab dang nhap va hien register-success-msg. */
    public boolean isRegisterSuccessDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.REGISTER_SUCCESS_MSG, 10);
    }

    public String getRegisterSuccessMessage() {
        waitForElementVisible(AuthModalPageUI.REGISTER_SUCCESS_MSG);
        return getElementText(AuthModalPageUI.REGISTER_SUCCESS_MSG);
    }

    /** Thong bao phien het han (CN-02 E3) chi hien o tab dang nhap. */
    public boolean isSessionExpiredNoticeDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.SESSION_EXPIRED_NOTICE, 10);
    }

    public String getFieldError(String errorLocator) {
        waitForElementVisible(errorLocator);
        return getElementText(errorLocator);
    }

    public boolean isFieldErrorDisplayed(String errorLocator) {
        return isElementDisplayedWithWait(errorLocator, 5);
    }

    public String getRegisterNameError() {
        return getFieldError(AuthModalPageUI.REGISTER_NAME_ERROR);
    }

    public String getRegisterEmailError() {
        return getFieldError(AuthModalPageUI.REGISTER_EMAIL_ERROR);
    }

    public String getRegisterPhoneError() {
        return getFieldError(AuthModalPageUI.REGISTER_PHONE_ERROR);
    }

    public String getRegisterPasswordError() {
        return getFieldError(AuthModalPageUI.REGISTER_PASSWORD_ERROR);
    }

    public String getRegisterConfirmPasswordError() {
        return getFieldError(AuthModalPageUI.REGISTER_CONFIRM_PASSWORD_ERROR);
    }

    /** Mot quy tac mat khau duoc coi la dat khi dong tuong ung co dau check "✓" (chuyen xanh). */
    public boolean isPasswordRuleSatisfied(String ruleLocator) {
        return isElementDisplayed(ruleLocator) && getElementText(ruleLocator).contains("✓");
    }

    public boolean isLengthRuleSatisfied() {
        return isPasswordRuleSatisfied(AuthModalPageUI.PASSWORD_RULE_LENGTH);
    }

    public boolean isUppercaseRuleSatisfied() {
        return isPasswordRuleSatisfied(AuthModalPageUI.PASSWORD_RULE_UPPERCASE);
    }

    public boolean isDigitRuleSatisfied() {
        return isPasswordRuleSatisfied(AuthModalPageUI.PASSWORD_RULE_DIGIT);
    }

    public boolean isSpecialRuleSatisfied() {
        return isPasswordRuleSatisfied(AuthModalPageUI.PASSWORD_RULE_SPECIAL);
    }

    /** Chi go mat khau dang ky (khong submit) de quan sat checklist quy tac. */
    public void enterRegisterPassword(String password) {
        switchToRegisterTab();
        sendKeyToElement(AuthModalPageUI.REGISTER_PASSWORD_INPUT, password);
    }

    public boolean isErrorMessageDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.AUTH_ERROR_MSG, 5);
    }

    public String getErrorMessage() {
        waitForElementVisible(AuthModalPageUI.AUTH_ERROR_MSG);
        return getElementText(AuthModalPageUI.AUTH_ERROR_MSG);
    }

    public boolean isModalStillOpen() {
        return isElementDisplayed(AuthModalPageUI.AUTH_MODAL);
    }

    public void closeModal() {
        clickToElement(AuthModalPageUI.AUTH_CLOSE_BTN);
        waitForElementInvisible(AuthModalPageUI.AUTH_MODAL);
    }

    // ---------- Quen mat khau (dot 13, muc 1) — CHI kiem tra UI ----------
    // Test Selenium khong gui mail that, khong nhap ma reset that: chi khang dinh
    // link hien o form dang nhap, bam vao mo duoc form xin ma, nut quay lai dua
    // ve lai form dang nhap. Gui mail that qua SMTP chi kiem bang tay.
    public boolean isForgotPasswordLinkDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.FORGOT_PASSWORD_LINK, 10);
    }

    public String getForgotPasswordLinkText() {
        waitForElementVisible(AuthModalPageUI.FORGOT_PASSWORD_LINK);
        return getElementText(AuthModalPageUI.FORGOT_PASSWORD_LINK);
    }

    public void clickForgotPasswordLink() {
        clickToElement(AuthModalPageUI.FORGOT_PASSWORD_LINK);
        waitForElementVisible(AuthModalPageUI.FORGOT_PASSWORD_FORM);
    }

    /** Form xin ma dat lai hien du: form + o email + nut gui ma. */
    public boolean isForgotPasswordFormDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.FORGOT_PASSWORD_FORM, 10)
                && isElementDisplayed(AuthModalPageUI.FORGOT_PASSWORD_EMAIL_INPUT)
                && isElementDisplayed(AuthModalPageUI.FORGOT_PASSWORD_SUBMIT_BTN);
    }

    /** Bam 'Quay lai dang nhap' trong luong quen mat khau -> ve form dang nhap. */
    public void clickForgotPasswordBackButton() {
        clickToElement(AuthModalPageUI.FORGOT_PASSWORD_BACK_BTN);
        waitForElementVisible(AuthModalPageUI.LOGIN_FORM);
    }

    // ---------- Trang dang nhap dang page (dot 13, muc 10) ----------
    /** Nut quay lai / nut dong 'auth-close-btn' co dang hien khong. O trang
     *  dang nhap admin rieng no phai VANG MAT (khong co nut quay lai ve shop). */
    public boolean isBackButtonDisplayed() {
        return getListWebElements(AuthModalPageUI.AUTH_CLOSE_BTN).size() > 0;
    }

    /** Khung trang dang nhap .auth-page-wrapper (can giua man hinh) co hien khong. */
    public boolean isAuthPageWrapperDisplayed() {
        return isElementDisplayedWithWait(AuthModalPageUI.AUTH_PAGE, 10);
    }

    /** Class cua khung trang dang nhap (ky vong chua 'auth-page-wrapper'). */
    public String getAuthPageWrapperClass() {
        waitForElementVisible(AuthModalPageUI.AUTH_PAGE);
        return getElementAttribute(AuthModalPageUI.AUTH_PAGE, "class");
    }
}
