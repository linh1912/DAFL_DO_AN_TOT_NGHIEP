package vn.dafl.tests.web.orders;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

/**
 * CN-08.3 Huy don: chi huy duoc khi don o "Cho xac nhan", phai chon ly do,
 * huy xong hoan lai ton kho va nut Huy bien mat (idempotent o muc UI).
 */
@Feature("Hủy đơn hàng")
public class OrderCancellationTest extends BaseTest {

    /** Dat mot don COD 2 san pham bang tai khoan moi; tra ve ma don hien thi. */
    private String placeCodOrderWithTwoItems(String email, String productId) {
        HomePageObject homePage = new HomePageObject();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.addToCart(productId);
        homePage.waitForCartBadgeCount(1);
        homePage.openCart();
        new CartDrawerPageObject().setItemQuantity(productId, 2);

        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.fillDefaultShippingForm("Khach Huy Don", email, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang phai thanh cong truoc khi huy");
        return checkout.getOrderSuccessCode();
    }

    @Story("Hủy đơn")
    @Test(description = "TC-CN08-03: Hủy đơn ở trạng thái Chờ xác nhận thành công và tồn kho được hoàn lại")
    public void cancelPendingOrderSuccessfullyAndRestocks() {
        String email = randomEmail();
        registerAndLogin("Khach Huy Don", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);

        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();
        catalog.openProductDetail(productId);
        int stockBefore = catalog.getDetailAvailableStock();
        Assert.assertTrue(stockBefore >= 2, "Ton kho phai >= 2 de dat don 2 san pham");
        catalog.backFromDetail();

        String orderCode = placeCodOrderWithTwoItems(email, productId);

        catalog.openProductDetail(productId);
        Assert.assertEquals(catalog.getDetailAvailableStock(), stockBefore - 2,
                "Dat don 2 san pham thi ton kho phai giam 2");
        getDriver().get(GlobalConstants.getAppUrl() + "/");

        HomePageObject homePage = new HomePageObject();
        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Don moi phai o trang thai Cho xac nhan");
        ordersPage.cancelOrderWithReason("Đổi ý, không muốn mua nữa");

        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(), "Phai hien thong bao huy thanh cong");
        Assert.assertTrue(ordersPage.getCancelSuccessMessage().contains(GlobalConstants.CANCEL_SUCCESS_MESSAGE),
                "Noi dung thong bao huy khong dung");
        ordersPage.openOrderDetail(orderCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_CANCELLED,
                "Don sau khi huy phai o trang thai Da huy");

        getDriver().get(GlobalConstants.getAppUrl() + "/");
        catalog.waitForProductsLoaded();
        catalog.openProductDetail(productId);
        Assert.assertEquals(catalog.getDetailAvailableStock(), stockBefore,
                "Huy don phai hoan lai ton kho nhu ban dau");
    }

    @Story("Hủy đơn")
    @Test(description = "CN-08.3: Sau khi hủy, mở lại chi tiết đơn không còn nút Hủy đơn (không hủy 2 lần)")
    public void cancelButtonDisappearsAfterCancellation() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Huy Mot Lan", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();

        String orderCode = placeCodOrderWithTwoItems(email, productId);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.cancelOrderWithReason("Đặt nhầm sản phẩm");
        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(), "Huy don phai thanh cong");

        ordersPage.openOrderDetail(orderCode);
        Assert.assertFalse(ordersPage.isCancelOrderButtonDisplayed(),
                "Don da huy khong duoc con nut Huy don");
    }

    @Story("Lọc đơn hàng")
    @Test(description = "CN-08.1: Lọc danh sách đơn theo trạng thái Đã hủy / Chờ xác nhận")
    public void filterOrdersByStatus() {
        String email = randomEmail();
        HomePageObject homePage = registerAndLogin("Khach Loc Don", email, randomPhone(), GlobalConstants.DEMO_PASSWORD);
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        String productId = catalog.getFirstProductId();

        String orderCode = placeCodOrderWithTwoItems(email, productId);

        OrdersPageObject ordersPage = homePage.openOrdersPage();
        ordersPage.openOrderDetail(orderCode);
        ordersPage.cancelOrderWithReason("Tìm được giá tốt hơn");
        Assert.assertTrue(ordersPage.isCancelSuccessDisplayed(), "Huy don phai thanh cong");

        ordersPage.filterByStatus(GlobalConstants.ORDER_STATUS_CANCELLED);
        Assert.assertTrue(ordersPage.isOrderDisplayed(orderCode),
                "Loc 'Da huy' phai thay don vua huy");

        ordersPage.filterByStatus(GlobalConstants.ORDER_STATUS_PENDING);
        Assert.assertFalse(ordersPage.isElementDisplayed("css=[data-testid='order-item-" + orderCode + "']"),
                "Loc 'Cho xac nhan' khong duoc thay don da huy");
    }
}
