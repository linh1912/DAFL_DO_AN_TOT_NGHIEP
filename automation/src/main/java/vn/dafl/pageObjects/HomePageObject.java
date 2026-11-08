package vn.dafl.pageObjects;

import org.openqa.selenium.ElementClickInterceptedException;
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
        try {
            clickToElement(HomePageUI.CART_BTN);
        } catch (ElementClickInterceptedException e) {
            // Toast thong bao cua FE (vd vua them san pham) che nut gio hang o goc header:
            // click bang JS de bo qua lop phu.
            clickElementByJS(HomePageUI.CART_BTN);
        }
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

    // ---------- Banner hero trang chu (dot 2-4) ----------
    public boolean isHeroCarouselDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.HERO_CAROUSEL, 10);
    }

    /** Chi so slide dang active (0..2): slide co class "active". -1 neu khong xac dinh duoc. */
    public int getActiveHeroSlideIndex() {
        for (int i = 0; i < 3; i++) {
            String cssClass = getElementAttribute(
                    getDynamicLocator(HomePageUI.DYNAMIC_HERO_SLIDE, String.valueOf(i)), "class");
            if (cssClass != null && cssClass.contains("active")) {
                return i;
            }
        }
        return -1;
    }

    public String getActiveHeroTitle() {
        waitForElementVisible(HomePageUI.ACTIVE_HERO_TITLE);
        return getElementText(HomePageUI.ACTIVE_HERO_TITLE);
    }

    public String getActiveHeroSubtitle() {
        waitForElementVisible(HomePageUI.ACTIVE_HERO_SUBTITLE);
        return getElementText(HomePageUI.ACTIVE_HERO_SUBTITLE);
    }

    /** Hop chu toi mo (.hero-text-box) cua slide dang active co hien thi khong. */
    public boolean isActiveHeroTextBoxDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.ACTIVE_HERO_TEXT_BOX, 5);
    }

    public void clickHeroNext() {
        clickToElement(HomePageUI.HERO_NEXT_BTN);
        sleepInSecond(1); // cho hieu ung chuyen slide (0.5s) hoan tat
    }

    public void clickHeroPrev() {
        clickToElement(HomePageUI.HERO_PREV_BTN);
        sleepInSecond(1);
    }

    public void clickHeroDot(int index) {
        clickToElement(HomePageUI.DYNAMIC_HERO_DOT, String.valueOf(index));
        sleepInSecond(1);
    }

    /** Cho toi khi slide active doi khoi chi so ban dau (banner tu chay 4s/slide); toi da 12 giay. */
    public void waitForHeroSlideAutoAdvance(int fromIndex) {
        long deadline = System.currentTimeMillis() + 12000;
        while (System.currentTimeMillis() < deadline) {
            if (getActiveHeroSlideIndex() != fromIndex && getActiveHeroSlideIndex() != -1) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Banner khong tu chuyen slide sau 12 giay (van o slide " + fromIndex + ")");
    }

    // ---------- Khoi noi dung trang chu kieu Cocolux ----------
    public int getCategoryIconCount() {
        return getListWebElements(HomePageUI.ANY_CATEGORY_ICON).size();
    }

    public boolean isBrandStripDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.BRAND_STRIP, 5);
    }

    public boolean isBestSellerSectionDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.BEST_SELLER_SECTION, 5);
    }

    public boolean isSuggestedSectionDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.SUGGESTED_SECTION, 5);
    }

    // ---------- Flash Sale (dot 4) ----------
    public boolean isFlashSaleSectionDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.FLASH_SALE_SECTION, 10);
    }

    public String getFlashSaleCountdownText() {
        waitForElementVisible(HomePageUI.FLASH_SALE_COUNTDOWN);
        return getElementText(HomePageUI.FLASH_SALE_COUNTDOWN).trim();
    }

    /** So SP trong muc Flash Sale (toi da 8, xep theo % giam). */
    public int getFlashSaleItemCount() {
        return getListWebElements(HomePageUI.ANY_FLASH_SALE_ITEM).size();
    }

    /** Doi dem nguoc HH:MM:SS sang tong so giay con lai; -1 neu khong dung dinh dang. */
    public int getFlashSaleCountdownSeconds() {
        String text = getFlashSaleCountdownText();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})$")
                .matcher(text);
        if (!matcher.matches()) {
            return -1;
        }
        return Integer.parseInt(matcher.group(1)) * 3600
                + Integer.parseInt(matcher.group(2)) * 60
                + Integer.parseInt(matcher.group(3));
    }
}
