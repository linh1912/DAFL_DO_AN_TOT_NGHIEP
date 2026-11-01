package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.HomePageObject;

/**
 * CN-01 Dang ky + CN-02 Dang nhap/dang xuat (ban chot v5).
 * Dang ky xong dang nhap duoc ngay (khong con OTP/xac thuc email);
 * dang nhap dung JWT luu o localStorage (key dafl_token);
 * loi sai thong tin dang nhap: "Email hoac mat khau khong dung" hien duoi o mat khau.
 */
public class AuthTest extends BaseTest {

    @Test(description = "CN-02 TC-CN02-01: Dang nhap thanh cong bang tai khoan demo (du lieu seed cua SUT)")
    public void loginWithDemoCredentialsSuccessfully() {
        Assert.assertEquals(loginWithDemoAccount().getAccountDisplayName(), GlobalConstants.DEMO_FULL_NAME);
        Assert.assertTrue(getAuthToken() != null && !getAuthToken().isEmpty(), "Token JWT phai duoc luu o localStorage sau dang nhap");
    }

    @Test(description = "CN-02 E1: Dang nhap sai mat khau -> loi 'Email hoac mat khau khong dung' duoi o mat khau")
    public void loginWithWrongPasswordShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, "WrongPassword123!");

        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Thong bao loi phai xuat hien duoi o mat khau");
        Assert.assertEquals(authModal.getLoginPasswordError(), GlobalConstants.LOGIN_ERROR_MESSAGE);
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal phai con mo sau khi dang nhap that bai");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(), "Khong duoc luu token khi dang nhap that bai");
    }

    @Test(description = "CN-02: Dang xuat thanh cong, menu tai khoan tro ve trang thai chua dang nhap")
    public void logoutSuccessfully() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.logout();
        homePage.waitForLoggedOut();
        Assert.assertEquals(homePage.getAccountDisplayName(), "Đăng nhập");
        Assert.assertTrue(getAuthToken() == null || getAuthToken().isEmpty(), "Token phai bi xoa khoi localStorage sau dang xuat");
    }

    @Test(description = "TC-CN02-04 (CN-02.3): Dang xuat xong bam Back cua trinh duyet khong thay lai trang rieng tu")
    public void logoutThenBackDoesNotRevealPrivatePage() {
        HomePageObject homePage = loginWithDemoAccount();
        homePage.openOrdersPage();
        homePage.logout();
        homePage.waitForLoggedOut();

        getDriver().navigate().back();
        sleepInSecond(2);

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login")
                        || !isTextVisibleOnPage(GlobalConstants.DEMO_FULL_NAME), "Sau dang xuat + Back, trang rieng tu khong duoc hien (bi day ve dang nhap)");
    }

    @Test(description = "TC-CN02-03 (CN-02.3): Chua dang nhap truy cap thang /orders va /profile bi chuyen ve dang nhap")
    public void protectedPagesRedirectToLoginWhenLoggedOut() {
        openHomePage();
        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "/orders khi chua dang nhap phai ve /login");

        getDriver().get(GlobalConstants.getAppUrl() + "/profile");
        sleepInSecond(2);
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "/profile khi chua dang nhap phai ve /login");
    }

    @Test(description = "TC-CN02-05 (CN-02 E3): Token het han/bi sua -> bi day ve dang nhap kem thong bao phien het han")
    public void expiredTokenRedirectsToLoginWithNotice() {
        loginWithDemoAccount();
        corruptAuthToken();

        getDriver().get(GlobalConstants.getAppUrl() + "/orders");
        sleepInSecond(2);

        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"), "Phai bi chuyen ve trang dang nhap");
        AuthModalPageObject authModal = new AuthModalPageObject();
        Assert.assertTrue(authModal.isSessionExpiredNoticeDisplayed(), "Phai hien thong bao phien het han");
    }

    @Test(description = "CN-01 TC-CN01-01: Dang ky tai khoan moi thanh cong roi dang nhap ngay duoc (khong con OTP)")
    public void registerNewAccountThenLogin() {
        String name = "Selenium Tester";
        String email = randomEmail();
        String phone = randomPhone();
        String password = GlobalConstants.DEMO_PASSWORD;

        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.register(name, email, phone, password, password);

        Assert.assertTrue(authModal.isRegisterSuccessDisplayed(), "Phai hien thong bao dang ky thanh cong");
        Assert.assertTrue(authModal.getRegisterSuccessMessage().contains(GlobalConstants.REGISTER_SUCCESS_MESSAGE), "Noi dung thong bao dang ky thanh cong");
        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Sau dang ky phai tu dong chuyen ve tab dang nhap");

        authModal.loginAs(email, password);
        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), name);
    }

    @Test(description = "TC-CN01-02: Dang ky thieu/sai tung truong -> thong bao loi dung ngay duoi o nhap")
    public void registerFieldValidationErrors() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        // (a) Ten qua ngan (< 2 ky tu)
        authModal.register("A", randomEmail(), randomPhone(), GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_NAME_ERROR), "Ten ngan phai co loi duoi o ho ten");

        // (b) Email sai dinh dang
        authModal.register("Selenium Tester", "khong-phai-email", randomPhone(), GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Email sai dinh dang phai co loi duoi o email");

        // (c) SDT sai dinh dang (9 so)
        authModal.register("Selenium Tester", randomEmail(), "091234567", GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PHONE_ERROR), "SDT 9 so phai co loi duoi o SDT");

        Assert.assertTrue(authModal.isModalStillOpen(), "Dang ky khong hop le thi modal van mo, chua dang nhap");
    }

    @Test(description = "TC-CN01-03: Checklist 4 quy tac mat khau chuyen xanh dan theo tung dieu kien dat duoc")
    public void registerPasswordPolicyChecklist() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        authModal.enterRegisterPassword("Abc1");
        Assert.assertTrue(!authModal.isLengthRuleSatisfied(), "Chua du 8 ky tu thi quy tac do dai chua dat");
        Assert.assertTrue(authModal.isUppercaseRuleSatisfied(), "Co chu hoa thi quy tac chu hoa dat");
        Assert.assertTrue(authModal.isDigitRuleSatisfied(), "Co chu so thi quy tac chu so dat");
        Assert.assertTrue(!authModal.isSpecialRuleSatisfied(), "Chua co ky tu dac biet thi quy tac ky tu dac biet chua dat");

        authModal.enterRegisterPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertTrue(authModal.isLengthRuleSatisfied() && authModal.isUppercaseRuleSatisfied()
                        && authModal.isDigitRuleSatisfied() && authModal.isSpecialRuleSatisfied(), "Mat khau dat policy thi ca 4 quy tac deu xanh");
    }

    @Test(description = "CN-01 E2: Dang ky trung email da ton tai -> 'Email da duoc su dung'")
    public void registerDuplicateEmailShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Nguoi Dung Trung Email", GlobalConstants.DEMO_EMAIL, randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Loi trung email phai hien duoi o email");
        Assert.assertTrue(authModal.getRegisterEmailError().contains(GlobalConstants.DUPLICATE_EMAIL_MESSAGE), "Noi dung loi trung email");
    }

    @Test(description = "CN-01 E3: Dang ky trung SDT da ton tai -> 'So dien thoai da duoc su dung'")
    public void registerDuplicatePhoneShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Nguoi Dung Trung SDT", randomEmail(), GlobalConstants.DEMO_PHONE,
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_PHONE_ERROR), "Loi trung SDT phai hien duoi o SDT");
        Assert.assertTrue(authModal.getRegisterPhoneError().contains(GlobalConstants.DUPLICATE_PHONE_MESSAGE), "Noi dung loi trung SDT");
    }

    @Test(description = "Dang ky bang email sai dinh dang -> loi ngay duoi o email, khong tao tai khoan")
    public void registerWithInvalidEmailFormat() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.register("Selenium Tester", "email@khong-co-domain", randomPhone(),
                GlobalConstants.DEMO_PASSWORD, GlobalConstants.DEMO_PASSWORD);

        Assert.assertTrue(authModal.isFieldErrorDisplayed(vn.dafl.pageUIs.AuthModalPageUI.REGISTER_EMAIL_ERROR), "Loi dinh dang email phai hien");
        Assert.assertTrue(!authModal.isRegisterSuccessDisplayed(), "Thong bao dang ky thanh cong khong duoc xuat hien");
    }

    @Test(description = "Chuyen qua lai giua tab Dang nhap va Dang ky trong Auth Modal")
    public void switchBetweenLoginAndRegisterTabs() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Mo modal thi mac dinh o tab Dang nhap");
        authModal.switchToRegisterTab();
        Assert.assertTrue(authModal.isRegisterFormDisplayed(), "Sau khi chuyen tab phai thay form Dang ky");
        authModal.switchToLoginTab();
        Assert.assertTrue(authModal.isLoginFormDisplayed(), "Chuyen lai phai thay form Dang nhap");
    }

    @Test(description = "Dong Auth Modal roi mo lai van hien thi dung form dang nhap")
    public void closeModalAndReopen() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.closeModal();

        AuthModalPageObject reopenedModal = homePage.openLoginModal();
        Assert.assertTrue(reopenedModal.isLoginFormDisplayed(), "Mo lai modal phai thay form dang nhap");
    }

    @Test(description = "Nut con mat an/hien mat khau tren form dang nhap")
    public void toggleLoginPasswordVisibility() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.enterLoginPassword(GlobalConstants.DEMO_PASSWORD);
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "text");
        authModal.toggleLoginPasswordVisibility();
        Assert.assertEquals(authModal.getLoginPasswordInputType(), "password");
    }

    @Test(description = "Dang nhap sai mot lan roi nhap dung ngay trong cung phien modal thi thanh cong")
    public void loginFailsThenSucceedsInSameModalSession() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, "WrongPassword123!");
        Assert.assertTrue(authModal.isLoginPasswordErrorDisplayed(), "Lan dang nhap sai phai bao loi");

        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), GlobalConstants.DEMO_FULL_NAME);
    }
}
