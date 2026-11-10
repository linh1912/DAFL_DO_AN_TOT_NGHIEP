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

    // Muc ngay sinh + voucher sinh nhat o trang hoi vien (dot 15):
    // ngay sinh chi duoc luu 1 lan (BE khoa sua 90 ngay sau khi dat);
    // voucher sinh nhat chi cho hang Vang tro len, 15% toi da 150.000d, han 30 ngay,
    // moi tai khoan chi nhan 1 lan/nam trong cua so 30 ngay quanh sinh nhat.
    public static final String BIRTHDATE_SECTION = "css=[data-testid='birthdate-section']";
    public static final String BIRTHDATE_INPUT = "css=[data-testid='birthdate-input']";
    public static final String BIRTHDATE_SAVE_BTN = "css=[data-testid='birthdate-save-btn']";
    public static final String BIRTHDATE_ERROR_MSG = "css=[data-testid='birthdate-error-msg']";
    public static final String BIRTHDATE_SUCCESS_MSG = "css=[data-testid='birthdate-success-msg']";
    public static final String CLAIM_BIRTHDAY_VOUCHER_BTN = "css=[data-testid='claim-birthday-voucher-btn']";
    public static final String BIRTHDAY_VOUCHER_ERROR_MSG = "css=[data-testid='birthday-voucher-error-msg']";
    public static final String BIRTHDAY_VOUCHER_SUCCESS_MSG = "css=[data-testid='birthday-voucher-success-msg']";

    // Wave15.1-auto: TC_SCR10_FUNC_004 — dong phu trang (subtitle) cua /membership.
    public static final String MEMBERSHIP_SUBTITLE = "css=[data-testid='membership-page'] .page-subtitle";
    // Wave15.1-auto: TC_SCR10_FUNC_002, TC_SCR10_UI_012 — 4 badge hang.
    public static final String DYNAMIC_TIER_BADGE = "css=[data-testid='membership-tier-badge-%s']";
    public static final String ANY_TIER_BADGE = "css=[data-testid^='membership-tier-badge-']";
    // Wave15.1-auto: TC_SCR10_UI_008 — thanh tien do len hang (chi hien khi chua o hang cao nhat).
    public static final String MEMBERSHIP_PROGRESS_BAR = "css=[data-testid='membership-tier'] [role='progressbar']";
    // Wave15.1-auto: TC_SCR10_FUNC_011, TC_SCR10_FUNC_020, TC_SCR10_FUNC_024 — tung voucher cua toi.
    public static final String ANY_VOUCHER_ITEM = "css=[data-testid^='membership-voucher-']";
}
