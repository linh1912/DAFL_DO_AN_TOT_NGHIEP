package vn.dafl.pageUIs;

/** Locator cua trang Hoi vien rieng /membership (dot 13, muc 2).
 *  Tat ca data-testid trich truc tiep tu MembershipPage.tsx (src/Frontend).
 *  Chua dang nhap, route /membership chuyen ve /login (guard o App.tsx) nen
 *  trang nay khong tu render khoi 'membership-login-required' khi mo truc tiep. */
public class MembershipPageUI {

    public static final String MEMBERSHIP_PAGE = "css=[data-testid='membership-page']";
    public static final String MEMBERSHIP_TIER = "css=[data-testid='membership-tier']";
    public static final String MEMBERSHIP_POINTS_BALANCE = "css=[data-testid='membership-points-balance']";
    public static final String MEMBERSHIP_BENEFITS = "css=[data-testid='membership-benefits']";
    public static final String MEMBERSHIP_HISTORY = "css=[data-testid='membership-history']";
    public static final String MEMBERSHIP_VOUCHERS = "css=[data-testid='membership-vouchers']";
    public static final String MEMBERSHIP_REFRESH_BTN = "css=[data-testid='membership-refresh-btn']";
    public static final String MEMBERSHIP_LOGIN_REQUIRED = "css=[data-testid='membership-login-required']";
}
