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
    public static final String ACCOUNT_TOGGLE_BTN = "css=[data-testid='account-toggle-btn']";
    /** Ten nguoi dung hien thi tren nut tai khoan o header (khi chua dang nhap la "Dang nhap"). */
    public static final String ACCOUNT_TOGGLE_NAME = "css=.account-toggle-name";
    public static final String ACCOUNT_DROPDOWN_MENU = "css=[data-testid='account-dropdown-menu']";
    public static final String DROPDOWN_LOGIN_BTN = "css=[data-testid='dropdown-login-btn']";
    public static final String DROPDOWN_LOGOUT_BTN = "css=[data-testid='dropdown-logout-btn']";
    public static final String DROPDOWN_ORDERS_BTN = "css=[data-testid='dropdown-orders-btn']";
    public static final String DROPDOWN_PROFILE_BTN = "css=[data-testid='dropdown-profile-btn']";
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
}
