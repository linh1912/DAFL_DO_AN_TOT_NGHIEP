package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.HomePageObject;

/**
 * Dang nhap mang xa hoi Google/Facebook o che do MOCK (chi bat o moi truong
 * Development/Testing cua SUT): bam nut social -> panel mock -> Xac nhan thi
 * he thong tu tao tai khoan va dang nhap; Huy thi bao loi va khong dang nhap.
 */
public class SocialLoginTest extends BaseTest {

    @Test(description = "TC-SOC-01: Dang nhap mock Google tao tai khoan va dang nhap thanh cong")
    public void mockGoogleLoginCreatesAccountAndSignsIn() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isSocialGoogleButtonDisplayed(),
                "Tab dang nhap phai co nut dang nhap Google");
        authModal.clickSocialGoogleButton();
        Assert.assertTrue(authModal.isSocialMockPanelDisplayed(),
                "Moi truong test phai hien panel dang nhap mock");
        authModal.confirmSocialMockLogin();

        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), "Google Test User",
                "Dang nhap Google mock phai vao dung tai khoan Google Test User");
        Assert.assertTrue(getAuthToken() != null && !getAuthToken().isEmpty(),
                "Dang nhap social cung phai cap JWT");
    }

    @Test(description = "TC-SOC-02: Dang nhap mock Facebook dang nhap thanh cong")
    public void mockFacebookLoginSignsIn() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        Assert.assertTrue(authModal.isSocialFacebookButtonDisplayed(),
                "Tab dang nhap phai co nut dang nhap Facebook");
        authModal.clickSocialFacebookButton();
        Assert.assertTrue(authModal.isSocialMockPanelDisplayed(),
                "Moi truong test phai hien panel dang nhap mock");
        authModal.confirmSocialMockLogin();

        homePage.waitForLoggedIn();
        Assert.assertEquals(homePage.getAccountDisplayName(), "Facebook Test User",
                "Dang nhap Facebook mock phai vao dung tai khoan Facebook Test User");
    }

    @Test(description = "TC-SOC-03: Huy dang nhap mock giua chung -> hien loi social va khong dang nhap")
    public void cancelMockLoginShowsError() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();

        authModal.clickSocialGoogleButton();
        Assert.assertTrue(authModal.isSocialMockPanelDisplayed(), "Phai hien panel mock");
        authModal.cancelSocialMockLogin();

        Assert.assertTrue(authModal.isSocialErrorDisplayed(),
                "Huy dang nhap social phai hien thong bao loi social-error");
        Assert.assertTrue(authModal.isModalStillOpen(), "Modal dang nhap van phai mo");
        Assert.assertEquals(homePage.getAccountDisplayName(), "Đăng nhập",
                "Huy dang nhap thi van o trang thai chua dang nhap");
    }

    @Test(description = "Khoi dang nhap social cung xuat hien o tab Dang ky")
    public void socialButtonsAlsoAvailableOnRegisterTab() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.switchToRegisterTab();

        Assert.assertTrue(authModal.isSocialGoogleButtonDisplayed(),
                "Tab dang ky phai co nut Google");
        Assert.assertTrue(authModal.isSocialFacebookButtonDisplayed(),
                "Tab dang ky phai co nut Facebook");
    }
}
