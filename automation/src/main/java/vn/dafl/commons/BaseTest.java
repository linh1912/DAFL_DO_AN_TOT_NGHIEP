package vn.dafl.commons;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import vn.dafl.driver.DriverManager;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.HomePageObject;

import java.time.Duration;

/**
 * Lop co so cho moi test class: khoi tao/dong trinh duyet quanh tung test method,
 * cung cap cac thao tac dung chung (mo trang chu, dang nhap tai khoan demo...).
 * Che do headless doc tu -Dheadless=true|false (mac dinh false khi chay tay).
 */
public abstract class BaseTest {

    protected String baseUrl;

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected boolean isHeadless() {
        String value = System.getProperty("headless", System.getenv("HEADLESS"));
        return value != null && Boolean.parseBoolean(value);
    }

    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("chrome") String browserName) {
        baseUrl = GlobalConstants.getAppUrl();
        DriverManager.setDriver(initBrowser(browserName));
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }

    protected WebDriver initBrowser(String browserName) {
        if ("firefox".equalsIgnoreCase(browserName)) {
            FirefoxOptions options = new FirefoxOptions();
            if (isHeadless()) {
                options.addArguments("-headless");
            }
            return new FirefoxDriver(options);
        }
        ChromeOptions options = new ChromeOptions();
        if (isHeadless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-notifications");
        options.addArguments("--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    /** Mo trang chu cua SUT va tra ve HomePageObject. */
    protected HomePageObject openHomePage() {
        HomePageObject homePage = new HomePageObject();
        homePage.openPageUrl(baseUrl);
        homePage.waitForHomePageLoaded();
        return homePage;
    }

    /** Dang nhap bang tai khoan demo (seed san cua SUT) qua Auth Modal. */
    protected HomePageObject loginWithDemoAccount() {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();
        return homePage;
    }

    /** Sinh email ngau nhien khong trung lap cho cac test dang ky. */
    protected String randomEmail() {
        return "test" + System.currentTimeMillis() + "@dafl.vn";
    }

    /** Cat bo moi dinh dang tien te, chi giu chu so (vd "1.250.000d" -> 1250000). */
    protected long parseMoney(String moneyText) {
        String digits = moneyText.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }

    /** Sinh so dien thoai Viet Nam (10 so, bat dau 09) gan nhu khong trung lap cho test dang ky. */
    protected String randomPhone() {
        String tail = String.valueOf(System.currentTimeMillis());
        return "09" + tail.substring(tail.length() - 8);
    }

    /**
     * Dang ky mot tai khoan moi qua UI va de nguyen o tab Dang nhap sau khi thanh cong
     * (ban CN-01: dang ky xong hien "Dang ky thanh cong" va chuyen sang dang nhap, khong con OTP).
     * Tra ve AuthModalPageObject dang mo o tab dang nhap.
     */
    protected AuthModalPageObject registerNewAccount(String fullName, String email, String phone, String password) {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.register(fullName, email, phone, password, password);
        if (!authModal.isRegisterSuccessDisplayed()) {
            throw new AssertionError("Dang ky tai khoan " + email + " khong thanh cong");
        }
        return authModal;
    }

    /** Dang ky tai khoan moi roi dang nhap ngay bang chinh tai khoan do. */
    protected HomePageObject registerAndLogin(String fullName, String email, String phone, String password) {
        AuthModalPageObject authModal = registerNewAccount(fullName, email, phone, password);
        authModal.loginAs(email, password);
        HomePageObject homePage = new HomePageObject();
        homePage.waitForLoggedIn();
        return homePage;
    }

    /** Dang nhap bang mot tai khoan bat ky qua Auth Modal. */
    protected HomePageObject loginAs(String email, String password) {
        HomePageObject homePage = openHomePage();
        AuthModalPageObject authModal = homePage.openLoginModal();
        authModal.loginAs(email, password);
        homePage.waitForLoggedIn();
        return homePage;
    }

    /** Token JWT hien tai trong localStorage (key dafl_token theo CN-02); null neu chua dang nhap. */
    protected String getAuthToken() {
        Object token = ((JavascriptExecutor) getDriver())
                .executeScript("return window.localStorage.getItem('dafl_token');");
        return token == null ? null : String.valueOf(token);
    }

    /** Lam hong token JWT de mo phong phien het han/bi sua (CN-02 E3). */
    protected void corruptAuthToken() {
        ((JavascriptExecutor) getDriver())
                .executeScript("window.localStorage.setItem('dafl_token', 'token.da.bi.sua.khong.hop.le');");
    }

    /** Trich ma don hien thi dang DH{yyyyMMdd}-{NNNN} tu mot doan text bat ky; null neu khong co. */
    protected String extractOrderDisplayCode(String text) {
        if (text == null) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile(GlobalConstants.ORDER_DISPLAY_CODE_REGEX).matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    /** Nghi ngan trong test class (BasePage.sleepInSecond chi co tren page object). */
    protected void sleepInSecond(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** Kiem tra mot doan text co xuat hien o bat ky dau tren trang hien tai khong. */
    protected boolean isTextVisibleOnPage(String text) {
        return !getDriver().findElements(org.openqa.selenium.By.xpath(
                "//*[contains(text(),'" + text.replace("'", "") + "')]")).isEmpty();
    }
}
