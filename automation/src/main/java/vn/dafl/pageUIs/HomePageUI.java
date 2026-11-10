package vn.dafl.pageUIs;

/** Locator cua Header + thanh danh muc (xuat hien tren moi trang cua SUT).
 *  Tat ca data-testid duoc trich truc tiep tu ma nguon Frontend cua DAFL. */
public class HomePageUI {

    public static final String HEADER = "css=[data-testid='header']";
    public static final String BRAND_LOGO = "css=[data-testid='brand-logo']";
    public static final String SEARCH_INPUT = "css=[data-testid='search-input']";
    public static final String SEARCH_CLEAR_BTN = "css=[data-testid='search-clear-btn']";
    public static final String THEME_TOGGLE_BTN = "css=[data-testid='theme-toggle-btn']";
    public static final String NOTIF_BELL_BTN = "css=[data-testid='notif-bell-btn']";
    public static final String NOTIF_BADGE_COUNT = "css=[data-testid='notif-badge-count']";
    // Modal thong bao (dot 8): rong 480-520px, co nut 'Đánh dấu đã đọc tất cả',
    // danh sach thong bao hoac dong 'Chưa có thông báo' khi rong.
    public static final String NOTIF_MODAL = "css=[data-testid='notifications-modal']";
    public static final String NOTIF_MARK_ALL_READ_BTN = "css=[data-testid='mark-all-read-btn']";
    public static final String NOTIF_CLOSE_BTN = "css=[data-testid='close-notif-btn']";
    public static final String ANY_NOTIF_ITEM = "css=[data-testid^='notification-item-']";
    public static final String NOTIF_EMPTY_TEXT =
            "xpath=//*[@data-testid='notifications-modal']//p[contains(text(),'Chưa có thông báo')]";
    // Wave15.1-auto: TC_SCR11_UI_002 — badge "{n} mới" canh tieu de modal (chi hien khi co chua doc).
    public static final String NOTIF_UNREAD_BADGE =
            "xpath=//*[@data-testid='notifications-modal']//span[contains(text(),'mới')]";
    // Wave15.1-auto: TC_SCR11_UI_002, TC_SCR11_FUNC_003, TC_SCR11_FUNC_005 — cham chua doc.
    public static final String ANY_UNREAD_DOT = "css=[data-testid='notifications-modal'] .unread-dot";
    public static final String DYNAMIC_NOTIF_ITEM = "css=[data-testid='notification-item-%s']";
    public static final String DYNAMIC_NOTIF_ITEM_UNREAD_DOT = "css=[data-testid='notification-item-%s'] .unread-dot";
    public static final String ACCOUNT_TOGGLE_BTN = "css=[data-testid='account-toggle-btn']";
    /** Ten nguoi dung hien thi tren nut tai khoan o header (khi chua dang nhap la "Dang nhap"). */
    public static final String ACCOUNT_TOGGLE_NAME = "css=.account-toggle-name";
    public static final String ACCOUNT_DROPDOWN_MENU = "css=[data-testid='account-dropdown-menu']";
    public static final String DROPDOWN_LOGIN_BTN = "css=[data-testid='dropdown-login-btn']";
    public static final String DROPDOWN_LOGOUT_BTN = "css=[data-testid='dropdown-logout-btn']";
    public static final String DROPDOWN_ORDERS_BTN = "css=[data-testid='dropdown-orders-btn']";
    public static final String DROPDOWN_PROFILE_BTN = "css=[data-testid='dropdown-profile-btn']";
    public static final String DROPDOWN_MEMBERSHIP_BTN = "css=[data-testid='dropdown-membership-btn']";
    public static final String CART_BTN = "css=[data-testid='cart-btn']";
    public static final String CART_BADGE_COUNT = "css=[data-testid='cart-badge-count']";

    // Thanh danh muc nam ngay duoi header o trang chu
    public static final String CATEGORIES_BAR = "css=[data-testid='categories-bar']";
    public static final String CATEGORY_PILL_ALL = "css=[data-testid='category-pill-all']";
    public static final String DYNAMIC_CATEGORY_PILL = "css=[data-testid='category-pill-%s']";
    public static final String ANY_CATEGORY_PILL = "css=[data-testid^='category-pill-']";

    // Banner hero trang chu (dot 2-4): 3 slide tu chay 4s/slide, chu nam trong hop toi mo
    // .hero-text-box tren anh (doi 4), nut prev/next + dots chuyen slide.
    public static final String HERO_CAROUSEL = "css=[data-testid='hero-carousel']";
    public static final String DYNAMIC_HERO_SLIDE = "css=[data-testid='hero-slide-%s']";
    public static final String DYNAMIC_HERO_DOT = "css=[data-testid='hero-dot-%s']";
    public static final String HERO_PREV_BTN = "css=[data-testid='hero-prev-btn']";
    public static final String HERO_NEXT_BTN = "css=[data-testid='hero-next-btn']";
    public static final String ACTIVE_HERO_TEXT_BOX = "css=.hero-slide.active .hero-text-box";
    public static final String ACTIVE_HERO_TITLE = "css=.hero-slide.active .hero-title";
    public static final String ACTIVE_HERO_SUBTITLE = "css=.hero-slide.active .hero-subtitle";

    // Khoi noi dung trang chu kieu Cocolux (dot 2): hang icon danh muc my pham, dai
    // thuong hieu, muc Ban chay / Goi y cho ban.
    public static final String ANY_CATEGORY_ICON = "css=[data-testid^='category-icon-']";
    public static final String BRAND_STRIP = "css=[data-testid='brand-strip']";
    public static final String BEST_SELLER_SECTION = "css=[data-testid='best-seller-section']";
    public static final String SUGGESTED_SECTION = "css=[data-testid='suggested-section']";

    // Flash Sale (dot 4): chi hien khi dang xem tat ca san pham VA co SP giam gia
    // (OriginalPrice > Price); dem nguoc toi 23:59:59 hom nay, dinh dang HH:MM:SS.
    public static final String FLASH_SALE_SECTION = "css=[data-testid='flash-sale-section']";
    public static final String FLASH_SALE_COUNTDOWN = "css=[data-testid='flash-sale-countdown']";
    public static final String ANY_FLASH_SALE_ITEM = "css=[data-testid^='flash-sale-item-']";

    // Wave15.1-auto (SCR_09): badge/CTA cua slide hero dang active (khong co
    // data-testid rieng — dinh vi theo class trong .hero-slide.active).
    public static final String ACTIVE_HERO_BADGE = "css=.hero-slide.active .hero-top-badge span";
    public static final String ACTIVE_HERO_CTA = "css=.hero-slide.active .hero-pdd-cta";
    // Wave15.1-auto (TC_SCR9_UI_010): trust strip khong co data-testid.
    public static final String TRUST_STRIP = "xpath=//*[@aria-label='Cam kết mua sắm']";
    // Wave15.1-auto (TC_SCR9_FUNC_012): icon danh muc theo slug (category-icon-{slug}).
    public static final String DYNAMIC_CATEGORY_ICON = "css=[data-testid='category-icon-%s']";
    // Wave15.1-auto (TC_SCR9_FUNC_014): badge giam gia trong tung flash-sale-item.
    public static final String DYNAMIC_FLASH_SALE_ITEM_BADGE =
            "css=[data-testid='flash-sale-item-%s'] .product-discount-badge";
    // Wave15.1-auto (TC_SCR9_FUNC_017/018): the SP trong muc Ban chay / Goi y
    // (testid co hau to -best / -suggest).
    public static final String ANY_BEST_SELLER_CARD =
            "css=[data-testid='best-seller-section'] [data-testid^='product-card-']";
    public static final String ANY_SUGGESTED_CARD =
            "css=[data-testid='suggested-section'] [data-testid^='product-card-']";
    public static final String DYNAMIC_SECTION_CARD_RATING =
            "css=[data-testid='product-card-%s'] .product-rating";
    // Wave15.1-auto (TC_SCR9_UI_019/020): gian hang noi bat (shop-card-{slug}).
    public static final String DYNAMIC_SHOP_CARD = "css=[data-testid='shop-card-%s']";
    public static final String ANY_SHOP_CARD = "css=[data-testid^='shop-card-']";
    // Wave15.1-auto (TC_SCR9_UI_021): topbar khong co testid.
    public static final String TOPBAR = "css=.dafl-topbar";
    // Wave15.1-auto (TC_SCR9_FUNC_024): 2 muc menu header khong co testid.
    public static final String HEADER_UTILITY_GIAN_HANG =
            "xpath=//nav[@aria-label='Tiện ích DAFL']//button[span[text()='Gian hàng']]";
    public static final String HEADER_UTILITY_TAP_CHI =
            "xpath=//nav[@aria-label='Tiện ích DAFL']//button[span[text()='Tạp chí']]";
    // Wave15.1-auto (TC_SCR9_FUNC_025): nut Quan tri + trang 404 cua web.
    public static final String ADMIN_NAV_LINK = "css=[data-testid='admin-nav-link']";
    public static final String NOT_FOUND_PAGE = "css=[data-testid='not-found-page']";
    // Wave15.1-auto (TC_SCR9_UI_023): vung tim kiem header (kiem tra khong co nut submit).
    public static final String SEARCH_BAR = "css=[data-testid='search-bar']";
    public static final String ANY_SEARCH_BAR_BUTTON = "css=[data-testid='search-bar'] button";
    // Wave15.1-auto: khu vuc luoi san pham chinh (dung kiem tra cuon toi).
    public static final String PRODUCTS_SECTION = "css=#products-section";
}
