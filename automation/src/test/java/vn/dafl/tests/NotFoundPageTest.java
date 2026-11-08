package vn.dafl.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;

/**
 * Trang 404 (dot 3): duong dan khong ton tai khong tu chuyen ve trang chu nua ma
 * hien trang "Khong tim thay trang" (data-testid=not-found-page); mo san pham khong
 * ton tai hien trang 404 rieng cua san pham (product-not-found-page).
 */
public class NotFoundPageTest extends BaseTest {

    private static final String NOT_FOUND_HOME_BTN =
            "xpath=//div[@data-testid='not-found-page']//button[contains(text(),'Về trang chủ')]";

    @Test(description = "NF-01: Mo duong dan khong ton tai -> trang 404, nut Ve trang chu dua ve trang chu")
    public void unknownRouteShowsNotFoundPage() {
        getDriver().get(GlobalConstants.getAppUrl() + "/trang-khong-ton-tai-xyz");

        Assert.assertTrue(isTextVisibleOnPage("Không tìm thấy trang"),
                "Phai hien tieu de trang 404");
        Assert.assertTrue(getDriver().findElements(
                org.openqa.selenium.By.cssSelector("[data-testid='not-found-page']")).size() > 0,
                "Phai co khoi not-found-page");

        getDriver().findElement(org.openqa.selenium.By.xpath(NOT_FOUND_HOME_BTN)).click();
        new vn.dafl.pageObjects.HomePageObject().waitForHomePageLoaded();
        Assert.assertEquals(getDriver().getCurrentUrl(), GlobalConstants.getAppUrl() + "/",
                "Bam Ve trang chu phai ve dung trang chu");
    }

    @Test(description = "NF-02: Mo san pham khong ton tai -> trang 404 san pham")
    public void unknownProductShowsProductNotFoundPage() {
        getDriver().get(GlobalConstants.getAppUrl() + "/product/khong-ton-tai");

        Assert.assertTrue(isTextVisibleOnPage("404 - Không tìm thấy sản phẩm"),
                "Phai hien trang 404 cua san pham");
        Assert.assertTrue(getDriver().findElements(
                org.openqa.selenium.By.cssSelector("[data-testid='product-not-found-page']")).size() > 0,
                "Phai co khoi product-not-found-page");
    }
}
