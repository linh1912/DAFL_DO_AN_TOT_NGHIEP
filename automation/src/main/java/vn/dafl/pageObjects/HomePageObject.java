package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.HomePageUI;

import java.util.List;

/** Page Object cho Header (dung chung tren moi trang) + thanh danh muc. */
public class HomePageObject extends BasePage {

    /** Cho trang chu tai xong: header, nut tai khoan va thanh danh muc da hien thi. */
    public void waitForHomePageLoaded() {
        waitForElementVisible(HomePageUI.HEADER);
        waitForElementVisible(HomePageUI.ACCOUNT_TOGGLE_BTN);
        waitForElementVisible(HomePageUI.CATEGORIES_BAR);
    }

    /** Chi bam nut tai khoan khi menu dang DONG - bam luc menu dang mo se dong menu lai. */
    private void openAccountDropdown() {
        if (!isElementDisplayed(HomePageUI.ACCOUNT_DROPDOWN_MENU)) {
            clickToElement(HomePageUI.ACCOUNT_TOGGLE_BTN);
            waitForElementVisible(HomePageUI.ACCOUNT_DROPDOWN_MENU);
        }
    }

    private void closeAccountDropdown() {
        if (isElementDisplayed(HomePageUI.ACCOUNT_DROPDOWN_MENU)) {
            clickToElement(HomePageUI.ACCOUNT_TOGGLE_BTN);
        }
    }

    public AuthModalPageObject openLoginModal() {
        openAccountDropdown();
        clickToElement(HomePageUI.DROPDOWN_LOGIN_BTN);
        AuthModalPageObject authModal = new AuthModalPageObject();
        authModal.waitForModalVisible();
        return authModal;
    }

    /** Dang nhap thanh cong khi menu tai khoan co nut Dang xuat; hoi lai trang thai toi da 30 giay. */
    public void waitForLoggedIn() {
        long deadline = System.currentTimeMillis() + 30000;
        while (System.currentTimeMillis() < deadline) {
            openAccountDropdown();
            if (isElementDisplayed(HomePageUI.DROPDOWN_LOGOUT_BTN)) {
                closeAccountDropdown();
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Dang nhap chua thanh cong: khong thay nut Dang xuat trong menu tai khoan sau 30 giay");
    }

    public boolean isLoggedIn() {
        openAccountDropdown();
        boolean result = isElementDisplayed(HomePageUI.DROPDOWN_LOGOUT_BTN);
        closeAccountDropdown();
        return result;
    }

    /** Ten dang hien thi tren nut tai khoan o header: "Dang nhap" khi chua dang nhap. */
    public String getAccountDisplayName() {
        waitForElementVisible(HomePageUI.ACCOUNT_TOGGLE_NAME);
        return getElementText(HomePageUI.ACCOUNT_TOGGLE_NAME).trim();
    }

    public void logout() {
        openAccountDropdown();
        clickToElement(HomePageUI.DROPDOWN_LOGOUT_BTN);
        waitForElementInvisible(HomePageUI.DROPDOWN_LOGOUT_BTN);
    }

    /** Cho toi khi menu tai khoan tro lai trang thai chua dang nhap. */
    public void waitForLoggedOut() {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            clickToElement(HomePageUI.ACCOUNT_TOGGLE_BTN);
            if (isElementDisplayed(HomePageUI.ACCOUNT_DROPDOWN_MENU)
                    && isElementDisplayed(HomePageUI.DROPDOWN_LOGIN_BTN)) {
                clickToElement(HomePageUI.ACCOUNT_TOGGLE_BTN);
                return;
            }
            if (isElementDisplayed(HomePageUI.ACCOUNT_DROPDOWN_MENU)) {
                clickToElement(HomePageUI.ACCOUNT_TOGGLE_BTN);
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Menu tai khoan khong tro ve trang thai chua dang nhap sau 15 giay");
    }

    public OrdersPageObject openOrdersPage() {
        openAccountDropdown();
        clickToElement(HomePageUI.DROPDOWN_ORDERS_BTN);
        OrdersPageObject ordersPage = new OrdersPageObject();
        ordersPage.waitForOrdersLoaded();
        return ordersPage;
    }

    public ProfilePageObject openProfilePage() {
        openAccountDropdown();
        clickToElement(HomePageUI.DROPDOWN_PROFILE_BTN);
        return new ProfilePageObject();
    }

    /** Bam nut gio hang - SUT chuyen sang trang /cart (route "cart"). */
    public CartDrawerPageObject openCart() {
        clickToElement(HomePageUI.CART_BTN);
        CartDrawerPageObject cartDrawer = new CartDrawerPageObject();
        cartDrawer.waitForDrawerVisible();
        return cartDrawer;
    }

    public int getCartBadgeCount() {
        if (!isElementDisplayed(HomePageUI.CART_BADGE_COUNT)) {
            return 0;
        }
        String text = getElementText(HomePageUI.CART_BADGE_COUNT).trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isCartBadgeDisplayed() {
        return isElementDisplayed(HomePageUI.CART_BADGE_COUNT);
    }

    /** Go tu khoa vao o tim kiem tren header (FE debounce ~300ms + tai lai danh sach). */
    public void searchProduct(String keyword) {
        waitForElementVisible(HomePageUI.SEARCH_INPUT);
        sendKeyToElement(HomePageUI.SEARCH_INPUT, keyword);
        sleepInSecond(2);
    }

    public void clearSearch() {
        if (isElementDisplayed(HomePageUI.SEARCH_CLEAR_BTN)) {
            clickToElement(HomePageUI.SEARCH_CLEAR_BTN);
            sleepInSecond(2);
        }
    }

    public String getFirstCategoryId() {
        List<WebElement> pills = getListWebElements(HomePageUI.ANY_CATEGORY_PILL);
        for (WebElement pill : pills) {
            String testId = pill.getAttribute("data-testid");
            if (testId != null && !testId.equals("category-pill-all") && testId.startsWith("category-pill-")) {
                return testId.replace("category-pill-", "");
            }
        }
        return null;
    }

    public void selectCategoryByIndex(int index) {
        List<WebElement> pills = getListWebElements(HomePageUI.ANY_CATEGORY_PILL);
        int count = 0;
        for (WebElement pill : pills) {
            String testId = pill.getAttribute("data-testid");
            if (testId != null && !testId.equals("category-pill-all")) {
                if (count == index) {
                    pill.click();
                    sleepInSecond(2);
                    return;
                }
                count++;
            }
        }
        throw new IllegalStateException("Khong tim thay nut danh muc o vi tri " + index);
    }

    public void selectCategory(String categoryId) {
        clickToElement(HomePageUI.DYNAMIC_CATEGORY_PILL, categoryId);
        sleepInSecond(2);
    }

    public void selectAllCategories() {
        clickToElement(HomePageUI.CATEGORY_PILL_ALL);
        sleepInSecond(2);
    }

    /** Cho toi khi huy hieu gio hang dat dung so luong mong doi (tong so luong san pham). */
    public void waitForCartBadgeCount(int expected) {
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            if (getCartBadgeCount() == expected) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Huy hieu gio hang khong dat " + expected + " sau 15 giay (hien tai: "
                + getCartBadgeCount() + ")");
    }
}
