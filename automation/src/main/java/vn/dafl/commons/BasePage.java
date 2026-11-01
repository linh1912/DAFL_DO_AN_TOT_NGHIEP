package vn.dafl.commons;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import vn.dafl.driver.DriverManager;

import java.time.Duration;
import java.util.List;

/**
 * Lop co so cho moi Page Object: thao tac phan tu, cho doi (explicit wait),
 * phan tich locator dang "css=..." / "xpath=..." / "id=..." / "name=..." / "class=...".
 * Locator dong dung String.format voi tham so %s.
 */
public class BasePage {

    /**
     * Tam dung sau moi thao tac click/go phim, de chay cham lai cho de quan sat khi demo.
     * Doc tu tham so -Daction.delay.ms (don vi mili-giay). Mac dinh 0 = khong cham,
     * nen Jenkins/CI (khong truyen tham so nay) van chay toc do binh thuong.
     * Vi du chay demo: -Daction.delay.ms=800 (moi thao tac nghi 0,8 giay).
     */
    private static final long ACTION_DELAY_MS = loadActionDelayMs();

    private static long loadActionDelayMs() {
        try {
            return Math.max(0, Long.parseLong(System.getProperty("action.delay.ms", "0").trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void pauseAfterAction() {
        if (ACTION_DELAY_MS > 0) {
            try {
                Thread.sleep(ACTION_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    protected WebDriverWait getWait(long timeoutInSeconds) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutInSeconds));
    }

    public void openPageUrl(String url) {
        getDriver().get(url);
        pauseAfterAction();
    }

    public String getCurrentPageUrl() {
        return getDriver().getCurrentUrl();
    }

    public String getPageTitle() {
        return getDriver().getTitle();
    }

    public By getByLocator(String locator) {
        if (locator.startsWith("css=")) {
            return By.cssSelector(locator.substring(4));
        } else if (locator.startsWith("xpath=")) {
            return By.xpath(locator.substring(6));
        } else if (locator.startsWith("id=")) {
            return By.id(locator.substring(3));
        } else if (locator.startsWith("name=")) {
            return By.name(locator.substring(5));
        } else if (locator.startsWith("class=")) {
            return By.className(locator.substring(6));
        } else if (locator.startsWith("tag=")) {
            return By.tagName(locator.substring(4));
        }
        throw new IllegalArgumentException("Locator chua dinh nghia tien to (css=/xpath=/id=/name=/class=): " + locator);
    }

    public String getDynamicLocator(String locator, String... values) {
        return String.format(locator, (Object[]) values);
    }

    public WebElement getWebElement(String locator) {
        return getDriver().findElement(getByLocator(locator));
    }

    public WebElement getWebElement(String locator, String... values) {
        return getDriver().findElement(getByLocator(getDynamicLocator(locator, values)));
    }

    public List<WebElement> getListWebElements(String locator) {
        return getDriver().findElements(getByLocator(locator));
    }

    public List<WebElement> getListWebElements(String locator, String... values) {
        return getDriver().findElements(getByLocator(getDynamicLocator(locator, values)));
    }

    public void clickToElement(String locator) {
        waitForElementClickable(locator);
        getWebElement(locator).click();
        pauseAfterAction();
    }

    public void clickToElement(String locator, String... values) {
        String dynamic = getDynamicLocator(locator, values);
        waitForElementClickable(dynamic);
        getWebElement(dynamic).click();
        pauseAfterAction();
    }

    public void clickToElementByJS(String locator) {
        WebElement element = getWebElement(locator);
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
        pauseAfterAction();
    }

    public void sendKeyToElement(String locator, String textValue) {
        waitForElementVisible(locator);
        WebElement element = getWebElement(locator);
        element.click();
        // Xoa bang phim tat (su kien ban phim that) thay cho element.clear():
        // clear() cua WebDriver khong kich hoat onChange cua React khi o nhap DA co gia tri,
        // nen trang thai form van giu gia tri cu du nhin bang mat thay chu moi (loi gui lai mat khau cu).
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(Keys.DELETE);
        element.sendKeys(textValue);
        pauseAfterAction();
    }

    public void sendKeyToElement(String locator, String textValue, String... values) {
        sendKeyToElement(getDynamicLocator(locator, values), textValue);
    }

    public String getElementText(String locator) {
        waitForElementVisible(locator);
        return getWebElement(locator).getText().trim();
    }

    public String getElementText(String locator, String... values) {
        String dynamic = getDynamicLocator(locator, values);
        waitForElementVisible(dynamic);
        return getWebElement(dynamic).getText().trim();
    }

    public String getElementAttribute(String locator, String attributeName) {
        waitForElementVisible(locator);
        return getWebElement(locator).getAttribute(attributeName);
    }

    public boolean isElementDisplayed(String locator) {
        try {
            return getWebElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayed(String locator, String... values) {
        try {
            return getWebElement(getDynamicLocator(locator, values)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementDisplayedWithWait(String locator, long timeoutInSeconds) {
        try {
            getWait(timeoutInSeconds).until(ExpectedConditions.visibilityOfElementLocated(getByLocator(locator)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForElementVisible(String locator) {
        getWait(GlobalConstants.LONG_TIMEOUT)
                .until(ExpectedConditions.visibilityOfElementLocated(getByLocator(locator)));
    }

    public void waitForElementVisible(String locator, String... values) {
        waitForElementVisible(getDynamicLocator(locator, values));
    }

    public void waitForListElementsVisible(String locator) {
        getWait(GlobalConstants.LONG_TIMEOUT)
                .until(ExpectedConditions.visibilityOfAllElementsLocatedBy(getByLocator(locator)));
    }

    public void waitForElementClickable(String locator) {
        getWait(GlobalConstants.LONG_TIMEOUT)
                .until(ExpectedConditions.elementToBeClickable(getByLocator(locator)));
    }

    public void waitForElementClickable(String locator, String... values) {
        waitForElementClickable(getDynamicLocator(locator, values));
    }

    public void waitForElementInvisible(String locator) {
        getWait(GlobalConstants.LONG_TIMEOUT)
                .until(ExpectedConditions.invisibilityOfElementLocated(getByLocator(locator)));
    }

    public void scrollToElement(String locator) {
        WebElement element = getWebElement(locator);
        ((JavascriptExecutor) getDriver()).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", element);
    }

    public void scrollToElement(String locator, String... values) {
        scrollToElement(getDynamicLocator(locator, values));
    }

    /** Cat bo moi dinh dang tien te, chi giu chu so (vd "1.250.000d" -> 1250000). */
    public long parseMoney(String moneyText) {
        String digits = moneyText.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Long.parseLong(digits);
    }

    public void sleepInSecond(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- Tien ich bo sung cho ban CN (select, JS, doc gia tri input) ----------

    /** Doc thuoc tinh value cua phan tu (dung cho o nhap dang <input>, vi du o so luong gio hang). */
    public String getElementValue(String locator) {
        waitForElementVisible(locator);
        String value = getWebElement(locator).getAttribute("value");
        return value == null ? "" : value.trim();
    }

    public String getElementValue(String locator, String... values) {
        return getElementValue(getDynamicLocator(locator, values));
    }

    /** Phan tu co dang duoc chon/kich hoat hay khong (nut bi disabled thi tra ve false). */
    public boolean isElementEnabled(String locator) {
        try {
            return getWebElement(locator).isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isElementEnabled(String locator, String... values) {
        return isElementEnabled(getDynamicLocator(locator, values));
    }

    /**
     * Chon mot muc trong o chon dia gioi (tinh/quan/phuong) theo dung text hien thi.
     * FE render dang <select> khi co du lieu dia gioi tinh kem theo; neu phan tu la
     * <input> (che do du phong khi khong co du lieu tinh) thi go text truc tiep.
     */
    public void selectByVisibleText(String locator, String visibleText) {
        waitForElementVisible(locator);
        WebElement element = getWebElement(locator);
        if ("select".equalsIgnoreCase(element.getTagName())) {
            new Select(element).selectByVisibleText(visibleText);
        } else {
            element.click();
            element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            element.sendKeys(Keys.DELETE);
            element.sendKeys(visibleText);
        }
        pauseAfterAction();
    }

    public void selectByVisibleText(String locator, String visibleText, String... values) {
        selectByVisibleText(getDynamicLocator(locator, values), visibleText);
    }

    /** Chon muc trong <select> theo thuoc tinh value (vd: loc trang thai don, sap xep san pham). */
    public void selectByValue(String locator, String value) {
        waitForElementVisible(locator);
        new Select(getWebElement(locator)).selectByValue(value);
        pauseAfterAction();
    }

    /** Gia tri value dang duoc chon cua <select>. */
    public String getSelectedValue(String locator) {
        waitForElementVisible(locator);
        return new Select(getWebElement(locator)).getFirstSelectedOption().getAttribute("value");
    }

    /** Thuc thi JavaScript tren trinh duyet (doc/ghi localStorage phuc vu test JWT...). */
    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) getDriver()).executeScript(script, args);
    }

    /** Bam nut bang JavaScript - dung khi can "double click" that nhanh o nut dat hang. */
    public void clickElementByJS(String locator) {
        clickToElementByJS(locator);
    }
}
