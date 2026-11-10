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

    /** Mo trang Hoi vien rieng /membership tu menu tai khoan (dot 13, muc 2). */
    public MembershipPageObject openMembershipPage() {
        openAccountDropdown();
        clickToElement(HomePageUI.DROPDOWN_MEMBERSHIP_BTN);
        MembershipPageObject membershipPage = new MembershipPageObject();
        membershipPage.waitForMembershipLoaded();
        return membershipPage;
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

    // ---------- Modal thong bao + dark mode (dot 8) ----------
    public void openNotifications() {
        clickToElement(HomePageUI.NOTIF_BELL_BTN);
        waitForElementVisible(HomePageUI.NOTIF_MODAL);
    }

    public boolean isNotificationModalDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.NOTIF_MODAL, 10);
    }

    public boolean isMarkAllReadButtonDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.NOTIF_MARK_ALL_READ_BTN, 5);
    }

    public int getNotificationItemCount() {
        return getListWebElements(HomePageUI.ANY_NOTIF_ITEM).size();
    }

    public boolean isNotificationEmptyStateDisplayed() {
        return isElementDisplayed(HomePageUI.NOTIF_EMPTY_TEXT);
    }

    public String getNotificationModalText() {
        waitForElementVisible(HomePageUI.NOTIF_MODAL);
        return getElementText(HomePageUI.NOTIF_MODAL);
    }

    public void closeNotifications() {
        clickToElement(HomePageUI.NOTIF_CLOSE_BTN);
        waitForElementInvisible(HomePageUI.NOTIF_MODAL);
    }

    /** Gia tri theme hien tai tren <html data-theme>: 'light' hoac 'dark'. */
    public String getCurrentTheme() {
        Object theme = executeScript("return document.documentElement.getAttribute('data-theme');");
        return theme == null ? "" : String.valueOf(theme);
    }

    /** Bam nut chuyen doi dark/light mode o header; tra ve theme sau khi doi. */
    public String toggleTheme() {
        String before = getCurrentTheme();
        clickToElement(HomePageUI.THEME_TOGGLE_BTN);
        sleepInSecond(1); // cho App ghi localStorage + doi data-theme
        String after = getCurrentTheme();
        if (after.equals(before)) {
            sleepInSecond(1);
            after = getCurrentTheme();
        }
        return after;
    }

    // ---------- Bo sung Wave15.1-auto (buoc 3, nhom thong bao / dark mode) ----------
    // Wave15.1-auto: TC_SCR11_UI_002 — badge "{n} mới" canh tieu de modal thong bao.
    public boolean isNotificationUnreadBadgeDisplayed() {
        return isElementDisplayed(HomePageUI.NOTIF_UNREAD_BADGE);
    }

    // Wave15.1-auto: TC_SCR11_UI_002 — text badge "{n} mới" trong modal (rong neu khong co).
    public String getNotificationUnreadBadgeText() {
        if (!isNotificationUnreadBadgeDisplayed()) {
            return "";
        }
        return getElementText(HomePageUI.NOTIF_UNREAD_BADGE).trim();
    }

    // Wave15.1-auto: TC_SCR11_UI_002, TC_SCR11_FUNC_003, TC_SCR11_FUNC_005 — so cham unread-dot trong modal.
    public int getUnreadDotCount() {
        return getListWebElements(HomePageUI.ANY_UNREAD_DOT).size();
    }

    // Wave15.1-auto: TC_SCR11_FUNC_005 — id cac thong bao dang hien trong modal (bo tien to 'notification-item-').
    public java.util.List<String> getNotificationItemIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (org.openqa.selenium.WebElement item : getListWebElements(HomePageUI.ANY_NOTIF_ITEM)) {
            String testId = item.getAttribute("data-testid");
            if (testId != null && testId.startsWith("notification-item-")) {
                ids.add(testId.replace("notification-item-", ""));
            }
        }
        return ids;
    }

    // Wave15.1-auto: TC_SCR11_FUNC_005 — id cac thong bao DANG chua doc (con cham unread-dot).
    public java.util.List<String> getUnreadNotificationItemIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (String id : getNotificationItemIds()) {
            if (isNotificationItemUnread(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    // Wave15.1-auto: TC_SCR11_FUNC_005 — item con cham unread-dot khong.
    public boolean isNotificationItemUnread(String itemId) {
        return isElementDisplayed(getDynamicLocator(HomePageUI.DYNAMIC_NOTIF_ITEM_UNREAD_DOT, itemId));
    }

    // Wave15.1-auto: TC_SCR11_FUNC_005 — bam 1 item thong bao (danh dau da doc item do).
    public void clickNotificationItem(String itemId) {
        clickToElement(getDynamicLocator(HomePageUI.DYNAMIC_NOTIF_ITEM, itemId));
        sleepInSecond(1); // cho FE cap nhat lac quan trang thai da doc
    }

    // Wave15.1-auto: TC_SCR11_FUNC_003 — bam nut "Danh dau da doc tat ca".
    public void clickMarkAllRead() {
        clickToElement(HomePageUI.NOTIF_MARK_ALL_READ_BTN);
        sleepInSecond(1); // cho FE cap nhat lac quan trang thai da doc
    }

    // Wave15.1-auto: TC_SCR11_FUNC_003 — cho toi khi khong con cham unread-dot (toi da 10 giay).
    public void waitForNoUnreadDots() {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            if (getUnreadDotCount() == 0) {
                return;
            }
            sleepInSecond(1);
        }
        throw new AssertionError("Van con " + getUnreadDotCount()
                + " cham unread-dot sau 10 giay ke tu khi bam 'Danh dau da doc tat ca'");
    }

    // Wave15.1-auto: TC_SCR11_UI_002, TC_SCR11_FUNC_003 — badge so chua doc tren chuong header.
    public boolean isNotifBadgeDisplayed() {
        return isElementDisplayed(HomePageUI.NOTIF_BADGE_COUNT);
    }

    // Wave15.1-auto: TC_SCR11_UI_002, TC_SCR11_FUNC_003 — so chua doc tren chuong (0 neu khong co badge).
    public int getNotifBadgeCount() {
        if (!isNotifBadgeDisplayed()) {
            return 0;
        }
        String text = getElementText(HomePageUI.NOTIF_BADGE_COUNT).trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Wave15.1-auto: TC_SCR11_UI_026 — nhan hien thi cua nut doi theme ("Theme Sang"/"Theme Toi").
    public String getThemeToggleLabel() {
        waitForElementVisible(HomePageUI.THEME_TOGGLE_BTN);
        return getElementText(HomePageUI.THEME_TOGGLE_BTN).trim();
    }

    // Wave15.1-auto: TC_SCR11_UI_026 — title cua nut doi theme ("Chuyen sang giao dien Toi/Sang").
    public String getThemeToggleTitle() {
        waitForElementVisible(HomePageUI.THEME_TOGGLE_BTN);
        return getElementAttribute(HomePageUI.THEME_TOGGLE_BTN, "title");
    }

    // Wave15.1-auto: TC_SCR11_FUNC_027 — gia tri localStorage 'dafl_theme' (null neu chua dat).
    public String getStoredTheme() {
        Object theme = executeScript("return window.localStorage.getItem('dafl_theme');");
        return theme == null ? null : String.valueOf(theme);
    }

    // Wave15.1-auto: TC_SCR11_FUNC_028 — mau nen tinh duoc cua body (vd "rgb(246, 246, 246)").
    public String getBodyBackgroundColor() {
        Object color = executeScript("return window.getComputedStyle(document.body).backgroundColor;");
        return color == null ? "" : String.valueOf(color);
    }

    // ---------- Wave15.1-auto (SCR_09: hero, trang chu, header) ----------

    /** Badge cua slide hero dang active (vd "FLASH SALE • GIẢM ĐẾN 50%"). */
    public String getActiveHeroBadge() {
        waitForElementVisible(HomePageUI.ACTIVE_HERO_BADGE);
        return getElementText(HomePageUI.ACTIVE_HERO_BADGE);
    }

    /** Text nut CTA cua slide hero dang active (kem dau "→" o cuoi). */
    public String getActiveHeroCtaText() {
        waitForElementVisible(HomePageUI.ACTIVE_HERO_CTA);
        return getElementText(HomePageUI.ACTIVE_HERO_CTA);
    }

    /** Bam nut CTA cua slide hero dang active (cuon toi #products-section). */
    public void clickActiveHeroCta() {
        clickToElement(HomePageUI.ACTIVE_HERO_CTA);
        sleepInSecond(2); // cho cuon mem (scrollIntoView behavior smooth) hoan tat
    }

    /** Cho toi khi slide active doi khoi chi so ban dau; tra ve so mili-giay da cho (-1 neu het thoi gian). */
    public long measureHeroAutoAdvanceMillis(int fromIndex) {
        long start = System.currentTimeMillis();
        long deadline = start + 12000;
        while (System.currentTimeMillis() < deadline) {
            int current = getActiveHeroSlideIndex();
            if (current != fromIndex && current != -1) {
                return System.currentTimeMillis() - start;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }
        return -1;
    }

    /** Cho toi khi slide active dat dung chi so mong doi (toi da timeout giay). */
    public boolean waitForHeroSlideIndex(int targetIndex, long timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000;
        while (System.currentTimeMillis() < deadline) {
            if (getActiveHeroSlideIndex() == targetIndex) {
                return true;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return getActiveHeroSlideIndex() == targetIndex;
    }

    /** Text dai cam ket ngay duoi hero (aria-label "Cam kết mua sắm"). */
    public String getTrustStripText() {
        waitForElementVisible(HomePageUI.TRUST_STRIP);
        return getElementText(HomePageUI.TRUST_STRIP);
    }

    /** Bam icon danh muc theo slug (vd "son-moi") — loc theo danh muc + cuon toi luoi. */
    public void clickCategoryIcon(String slug) {
        clickToElement(HomePageUI.DYNAMIC_CATEGORY_ICON, slug);
        sleepInSecond(2); // cho loc ap dung + cuon toi luoi
    }

    /** Text dai thuong hieu (brand-strip). */
    public String getBrandStripText() {
        waitForElementVisible(HomePageUI.BRAND_STRIP);
        return getElementText(HomePageUI.BRAND_STRIP);
    }

    /** Bam vao 1 ten thuong hieu trong brand strip (chi la text, khong phai link). */
    public void clickBrandByName(String brandName) {
        String locator = "xpath=//*[@data-testid='brand-strip']//*[normalize-space()=\"" + brandName + "\"]";
        clickToElement(locator);
        sleepInSecond(1);
    }

    /** % giam cua tung item Flash Sale theo thu tu hien thi (vd 50, 30, 25). */
    public java.util.List<Integer> getFlashSaleDiscountPercents() {
        java.util.List<Integer> percents = new java.util.ArrayList<>();
        for (WebElement item : getListWebElements(HomePageUI.ANY_FLASH_SALE_ITEM)) {
            String testId = item.getAttribute("data-testid");
            if (testId == null) {
                continue;
            }
            String productId = testId.replace("flash-sale-item-", "");
            String badge = getFlashSaleItemDiscountBadge(productId);
            String digits = badge.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                percents.add(Integer.parseInt(digits));
            }
        }
        return percents;
    }

    /** Text badge giam gia ("-25%") cua 1 item Flash Sale; rong neu khong co badge. */
    public String getFlashSaleItemDiscountBadge(String productId) {
        String locator = getDynamicLocator(HomePageUI.DYNAMIC_FLASH_SALE_ITEM_BADGE, productId);
        if (!isElementDisplayed(locator)) {
            return "";
        }
        return getElementText(locator);
    }

    /** Text toan muc Flash Sale (tieu de "⚡ Flash Sale" + dem nguoc). */
    public String getFlashSaleSectionText() {
        waitForElementVisible(HomePageUI.FLASH_SALE_SECTION);
        return getElementText(HomePageUI.FLASH_SALE_SECTION);
    }

    /** Flash Sale co dang hien thi khong (kiem tra ngay, khong cho doi). */
    public boolean isFlashSaleSectionPresent() {
        return isElementDisplayed(HomePageUI.FLASH_SALE_SECTION);
    }

    /** Id san pham trong muc Ban chay theo thu tu hien thi (testid co hau to -best). */
    public java.util.List<String> getBestSellerCardIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (WebElement card : getListWebElements(HomePageUI.ANY_BEST_SELLER_CARD)) {
            String testId = card.getAttribute("data-testid");
            if (testId != null && testId.endsWith("-best")) {
                ids.add(testId.replace("product-card-", "").replace("-best", ""));
            }
        }
        return ids;
    }

    /** Id san pham trong muc Goi y theo thu tu hien thi (testid co hau to -suggest). */
    public java.util.List<String> getSuggestedCardIds() {
        java.util.List<String> ids = new java.util.ArrayList<>();
        for (WebElement card : getListWebElements(HomePageUI.ANY_SUGGESTED_CARD)) {
            String testId = card.getAttribute("data-testid");
            if (testId != null && testId.endsWith("-suggest")) {
                ids.add(testId.replace("product-card-", "").replace("-suggest", ""));
            }
        }
        return ids;
    }

    /** Text sao cua 1 the trong muc Ban chay/Goi y (vd "4.9 (78)"); suffix "-best" hoac "-suggest". */
    public String getSectionCardRatingText(String productId, String suffix) {
        String locator = getDynamicLocator(HomePageUI.DYNAMIC_SECTION_CARD_RATING, productId + suffix);
        if (!isElementDisplayed(locator)) {
            return "";
        }
        return getElementText(locator);
    }

    /** So shop trong muc gian hang noi bat. */
    public int getShopCardCount() {
        return getListWebElements(HomePageUI.ANY_SHOP_CARD).size();
    }

    /** Text day du cua 1 shop card (ten + tag + rating + luot ban). */
    public String getShopCardText(String slug) {
        waitForElementVisible(getDynamicLocator(HomePageUI.DYNAMIC_SHOP_CARD, slug));
        return getElementText(getDynamicLocator(HomePageUI.DYNAMIC_SHOP_CARD, slug));
    }

    /** Bam vao 1 shop card (chi bo loc danh muc + cuon toi luoi, khong co trang shop). */
    public void clickShopCard(String slug) {
        clickToElement(HomePageUI.DYNAMIC_SHOP_CARD, slug);
        sleepInSecond(2); // cho bo loc + cuon toi luoi
    }

    /** Text topbar header (class dafl-topbar, khong co testid). */
    public String getTopbarText() {
        waitForElementVisible(HomePageUI.TOPBAR);
        return getElementText(HomePageUI.TOPBAR);
    }

    /** Bam logo DAFL o header (ve trang chu). */
    public void clickBrandLogo() {
        clickToElement(HomePageUI.BRAND_LOGO);
        sleepInSecond(1);
    }

    /** Placeholder cua o tim kiem header. */
    public String getSearchPlaceholder() {
        waitForElementVisible(HomePageUI.SEARCH_INPUT);
        return getElementAttribute(HomePageUI.SEARCH_INPUT, "placeholder");
    }

    /** Gia tri dang go trong o tim kiem header. */
    public String getSearchInputValue() {
        return getElementValue(HomePageUI.SEARCH_INPUT);
    }

    /** So nut (button) hien co trong vung tim kiem header — 0 khi chua go, 1 (nut xoa) khi co chu. */
    public int getSearchBarButtonCount() {
        return getListWebElements(HomePageUI.ANY_SEARCH_BAR_BUTTON).size();
    }

    /** Bam muc "Gian hàng" tren header (chi ve trang chu, khong co trang rieng). */
    public void clickHeaderUtilityGianHang() {
        clickToElement(HomePageUI.HEADER_UTILITY_GIAN_HANG);
        sleepInSecond(1);
    }

    /** Bam muc "Tạp chí" tren header (chi ve trang chu, khong co trang rieng). */
    public void clickHeaderUtilityTapChi() {
        clickToElement(HomePageUI.HEADER_UTILITY_TAP_CHI);
        sleepInSecond(1);
    }

    /** Nut "Quản trị" tren header co hien thi khong (chi khi isAdmin). */
    public boolean isAdminNavLinkDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.ADMIN_NAV_LINK, 15);
    }

    /** Bam nut "Quản trị" tren header (vao /admin). */
    public void clickAdminNavLink() {
        clickToElement(HomePageUI.ADMIN_NAV_LINK);
        sleepInSecond(1);
    }

    /** Trang 404 cua web (data-testid="not-found-page") co hien thi khong. */
    public boolean isNotFoundPageDisplayed() {
        return isElementDisplayedWithWait(HomePageUI.NOT_FOUND_PAGE, 10);
    }

    /** Text trang 404 cua web. */
    public String getNotFoundPageText() {
        waitForElementVisible(HomePageUI.NOT_FOUND_PAGE);
        return getElementText(HomePageUI.NOT_FOUND_PAGE);
    }

    /** Badge chuong thong bao co hien thi khong (chi hien khi unreadCount > 0). */
    public boolean isNotificationBadgeDisplayed() {
        return isElementDisplayed(HomePageUI.NOTIF_BADGE_COUNT);
    }

    /** Cho toi khi badge chuong xuat hien (so thong bao chua doc tai bat dong bo sau dang nhap). */
    public boolean waitForNotificationBadge(int timeoutSeconds) {
        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (isNotificationBadgeDisplayed()) {
                return true;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return isNotificationBadgeDisplayed();
    }

    /** So thong bao chua doc tren badge chuong; -1 neu badge khong hien. */
    public int getNotificationBadgeCount() {
        if (!isNotificationBadgeDisplayed()) {
            return -1;
        }
        String text = getElementText(HomePageUI.NOTIF_BADGE_COUNT).trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Text day du cua menu tai khoan (mo dropdown, doc text, dong lai). */
    public String getAccountDropdownText() {
        openAccountDropdown();
        String text = getElementText(HomePageUI.ACCOUNT_DROPDOWN_MENU);
        closeAccountDropdown();
        return text;
    }

    /** #products-section co nam trong khung nhin hien tai khong (sau khi cuon). */
    public boolean isProductsSectionInViewport() {
        Object result = executeScript(
                "var el = document.getElementById('products-section');"
                        + "if (!el) return false;"
                        + "var r = el.getBoundingClientRect();"
                        + "return r.top < window.innerHeight && r.bottom > 0;");
        return Boolean.TRUE.equals(result);
    }
}
