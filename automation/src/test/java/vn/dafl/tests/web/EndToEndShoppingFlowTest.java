package vn.dafl.tests.web;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Feature;
import vn.dafl.commons.BaseTest;
import vn.dafl.commons.GlobalConstants;
import vn.dafl.pageObjects.AuthModalPageObject;
import vn.dafl.pageObjects.CartDrawerPageObject;
import vn.dafl.pageObjects.CheckoutPageObject;
import vn.dafl.pageObjects.HomePageObject;
import vn.dafl.pageObjects.OrdersPageObject;
import vn.dafl.pageObjects.ProductCatalogPageObject;

import java.util.List;

/**
 * Luong mua hang dau-cuoi theo ban chot v5 (CN-04 -> CN-06 -> CN-07 -> CN-08):
 * khach duyet danh muc va them gio khong can dang nhap; den luc thanh toan moi
 * dang nhap (gio khach tu gop vao tai khoan), dien thong tin giao hang, dat COD
 * va thay don o trang thai Cho xac nhan trong lich su don hang.
 */
@Feature("Luồng mua hàng đầu cuối")
public class EndToEndShoppingFlowTest extends BaseTest {

    @Test(description = "E2E: Khách thêm giỏ -> thanh toán bị đưa qua đăng nhập -> quay lại đặt COD thành công -> thấy đơn Chờ xác nhận")
    public void guestToCheckoutEndToEndFlow() {
        // 1. Khach duyet danh muc va them 2 san pham (khong can dang nhap)
        HomePageObject homePage = openHomePage();
        ProductCatalogPageObject catalog = new ProductCatalogPageObject();
        catalog.waitForProductsLoaded();
        List<String> ids = catalog.getDisplayedProductIds();
        Assert.assertTrue(ids.size() >= 2, "Danh muc phai co it nhat 2 san pham");
        String productA = ids.get(0);
        String productB = ids.get(1);
        catalog.addToCart(productA);
        catalog.addToCart(productB);
        homePage.waitForCartBadgeCount(2);

        // 2. Mo gio va bam thanh toan -> bi dua qua dang nhap
        homePage.openCart();
        CheckoutPageObject checkout = new CheckoutPageObject();
        checkout.submitOrder();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/login"),
                "Khach bam thanh toan phai bi dua qua dang nhap");

        // 3. Dang nhap tai khoan demo -> quay lai trang gio, gio khach da gop day du
        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.waitForModalVisible();
        authModal.loginAs(GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PASSWORD);
        homePage.waitForLoggedIn();
        Assert.assertTrue(getDriver().getCurrentUrl().contains("/cart"),
                "Dang nhap xong phai quay lai trang gio");
        CartDrawerPageObject cart = new CartDrawerPageObject();
        Assert.assertTrue(cart.isItemDisplayed(productA), "San pham A cua gio khach phai duoc gop sang");
        Assert.assertTrue(cart.isItemDisplayed(productB), "San pham B cua gio khach phai duoc gop sang");

        // 4. Dien thong tin giao hang va dat hang COD
        checkout.fillDefaultShippingForm(GlobalConstants.DEMO_FULL_NAME, GlobalConstants.DEMO_EMAIL, GlobalConstants.DEMO_PHONE);
        checkout.selectCodPayment();
        checkout.submitOrder();
        Assert.assertTrue(checkout.isOrderSuccessViewDisplayed(), "Dat hang COD phai thanh cong");
        String orderCode = checkout.getOrderSuccessCode();
        Assert.assertTrue(orderCode.matches(GlobalConstants.ORDER_DISPLAY_CODE_REGEX),
                "Ma don phai dung dinh dang DH{yyyyMMdd}-{NNNN}, dang thay: " + orderCode);

        // 5. Don xuat hien trong lich su voi trang thai Cho xac nhan
        OrdersPageObject ordersPage = checkout.clickViewOrders();
        Assert.assertTrue(ordersPage.isOrderDisplayed(orderCode), "Don vua dat phai co trong lich su");
        ordersPage.openOrderDetail(orderCode);
        Assert.assertEquals(ordersPage.getOrderDetailStatus(), GlobalConstants.ORDER_STATUS_PENDING,
                "Don COD moi phai o trang thai Cho xac nhan");
        Assert.assertTrue(ordersPage.isOrderShippingInfoDisplayed(),
                "Chi tiet don phai hien khoi thong tin giao hang (anh chup dia chi)");
    }
}
