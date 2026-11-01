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
}
