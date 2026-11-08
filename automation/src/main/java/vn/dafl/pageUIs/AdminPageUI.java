package vn.dafl.pageUIs;

/** Locator cua trang quan tri /admin (dot 3, chi Super Admin).
 *  Bao ve: chua dang nhap -> chuyen /login; tai khoan thuong -> trang 403
 *  (admin-forbidden, kem huong dan cap quyen bang SQL); Super Admin -> menu 8 muc.
 *  Dot 8 tro ve truoc cac test chi dung thao tac DOC (GET). Tu dot 9, lop
 *  AdminCrudTest duoc phep them/sua/xoa QUA GIAO DIEN nhung CHI tren du lieu do
 *  chinh test tao (ten/ma kem timestamp) va tu don dep sau khi chay; khong xoa/sua
 *  du lieu seed, khong doi mat khau admin, khong duyet/tu choi tra hang cua nguoi khac. */
public class AdminPageUI {

    public static final String ADMIN_PAGE = "css=[data-testid='admin-page']";
    public static final String ADMIN_FORBIDDEN = "css=[data-testid='admin-forbidden']";
    public static final String DYNAMIC_ADMIN_NAV = "css=[data-testid='admin-nav-%s']";
    public static final String ADMIN_NAV_DASHBOARD = "css=[data-testid='admin-nav-dashboard']";
    public static final String ADMIN_NAV_ORDERS = "css=[data-testid='admin-nav-orders']";
    public static final String ADMIN_NAV_PRODUCTS = "css=[data-testid='admin-nav-products']";
    public static final String ADMIN_NAV_CATEGORIES = "css=[data-testid='admin-nav-categories']";
    public static final String ADMIN_NAV_PROMOTIONS = "css=[data-testid='admin-nav-promotions']";
    public static final String ADMIN_NAV_CUSTOMERS = "css=[data-testid='admin-nav-customers']";
    public static final String ADMIN_NAV_MEMBERSHIP = "css=[data-testid='admin-nav-membership']";
    public static final String ADMIN_NAV_RETURNS = "css=[data-testid='admin-nav-returns']";

    // Tieu de muc Tong quan (h2) + o tim kiem cua cac muc chi doc
    public static final String ADMIN_DASHBOARD_HEADING = "xpath=//h2[contains(text(),'Tổng quan')]";
    public static final String ADMIN_ORDER_SEARCH = "css=[data-testid='admin-order-search']";
    public static final String ADMIN_PRODUCT_SEARCH = "css=[data-testid='admin-product-search']";
    public static final String ADMIN_CUSTOMER_SEARCH = "css=[data-testid='admin-customer-search']";
    public static final String ADMIN_PROMO_ADD_BTN = "css=[data-testid='admin-promo-add-btn']";
    public static final String ADMIN_CATEGORY_ADD_BTN = "css=[data-testid='admin-category-add-btn']";

    // Bieu do doanh thu (dot 4, muc Tong quan): SVG thuan tu GET /api/admin/reports/revenue.
    // Chi hien khi co don trong khoang (totalOrders > 0); chua co doanh thu thi hien dong
    // "Chưa có doanh thu trong khoảng thời gian này." thay cho bieu do.
    public static final String ADMIN_REVENUE_RANGE_7 = "css=[data-testid='admin-revenue-range-7']";
    public static final String ADMIN_REVENUE_RANGE_30 = "css=[data-testid='admin-revenue-range-30']";
    public static final String ADMIN_REVENUE_CHART = "css=[data-testid='admin-revenue-chart']";
    public static final String ADMIN_REVENUE_CHART_SVG = "css=[data-testid='admin-revenue-chart'] svg";
    public static final String ADMIN_REVENUE_EMPTY_MSG =
            "xpath=//*[contains(text(),'Chưa có doanh thu trong khoảng thời gian này.')]";

    // ---------- Modal xem chi tiet (dot 8, giao dien Spark) ----------
    // Cac modal deu dung SparkModal: tieu de = ten/ma doi tuong, nut dong X
    // (aria-label='Đóng') + nut 'Đóng' o footer. Nut mat (👁) tren tung dong bang
    // mo modal tuong ung; khong co testid rieng tren nut mat nen dinh vi bang aria-label.
    public static final String ADMIN_PRODUCT_DETAIL = "css=[data-testid='admin-product-detail']";
    public static final String ADMIN_CATEGORY_DETAIL = "css=[data-testid='admin-category-detail']";
    public static final String ADMIN_PROMO_DETAIL = "css=[data-testid='admin-promo-detail']";
    public static final String ADMIN_TIER_DETAIL = "css=[data-testid='admin-tier-detail']";
    public static final String ADMIN_RETURN_DETAIL = "css=[data-testid='admin-return-detail']";
    public static final String ADMIN_ORDER_DETAIL = "css=[data-testid='admin-order-detail']";
    public static final String ADMIN_CUSTOMER_DETAIL = "css=[data-testid='admin-customer-detail']";
    // Nut mat dau tien cua tung bang (aria-label chua ten/ma dong do).
    public static final String FIRST_PRODUCT_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem chi tiết sản phẩm')])[1]";
    public static final String FIRST_CATEGORY_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem chi tiết danh mục')])[1]";
    public static final String FIRST_PROMO_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem chi tiết khuyến mãi')])[1]";
    public static final String FIRST_TIER_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem chi tiết hạng')])[1]";
    public static final String FIRST_ORDER_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem chi tiết đơn')])[1]";
    public static final String FIRST_CUSTOMER_DETAIL_BTN =
            "xpath=(//button[starts-with(@aria-label,'Xem khách hàng')])[1]";
    // Nut mo modal chi tiet tra hang (khong co nut mat rieng o bang tra hang).
    public static final String FIRST_RETURN_REVIEW_BTN =
            "xpath=(//button[contains(text(),'Duyệt / Từ chối')])[1]";
    // Nut dong X cua SparkModal, nam trong modal co testid cho truoc (%s).
    public static final String DYNAMIC_MODAL_CLOSE_BTN =
            "css=[data-testid='%s'] button[aria-label='Đóng']";
    // Nut 'Đóng' o footer modal, nam trong modal co testid cho truoc (%s).
    public static final String DYNAMIC_MODAL_FOOTER_CLOSE_BTN =
            "xpath=//*[@data-testid='%s']//button[text()='Đóng']";
    // Nut 'Sửa sản phẩm' o footer modal chi tiet san pham (mo form sua, khong luu).
    public static final String PRODUCT_DETAIL_EDIT_BTN =
            "xpath=//*[@data-testid='admin-product-detail']//button[contains(text(),'Sửa sản phẩm')]";
    // Form them/sua san pham (mo tu modal chi tiet) + nut 'Đóng' cua form.
    public static final String ADMIN_PRODUCT_FORM = "css=[data-testid='admin-product-form']";
    public static final String ADMIN_PRODUCT_FORM_CLOSE_BTN =
            "xpath=//*[@data-testid='admin-product-form']//button[text()='Đóng']";

    // ---------- Toolbar bang chuan Spark: search tren topbar, loc, Export ----------
    // O search tren topbar (chi ho tro o muc Don hang / San pham / Khach hang;
    // o cac muc khac no bi disabled) - loc theo noi dung cua muc dang mo.
    public static final String ADMIN_TOPBAR_SEARCH_INPUT = "css=.sa-topbar-search input";
    // Nut '⬇ Export' (xuat CSV client-side tu du lieu dang hien thi) o cac muc co bang.
    public static final String ADMIN_EXPORT_BTN = "xpath=//button[contains(text(),'Export')]";
    // Tat ca dong du lieu trong bang cua muc dang mo.
    public static final String ADMIN_TABLE_ROWS = "xpath=//div[contains(@class,'sa-table-wrap')]//tbody/tr";
    // Dong bao rong cua tung bang khi loc khong ra ket qua.
    public static final String ADMIN_EMPTY_PRODUCTS_MSG =
            "xpath=//p[contains(text(),'Không có sản phẩm nào.')]";
    public static final String ADMIN_EMPTY_CUSTOMERS_MSG =
            "xpath=//p[contains(text(),'Không có khách hàng nào.')]";
    public static final String ADMIN_EMPTY_ORDERS_MSG =
            "xpath=//p[contains(text(),'Không có đơn hàng nào.')]";
    public static final String ADMIN_EMPTY_RETURNS_MSG =
            "xpath=//p[contains(text(),'Không có yêu cầu trả hàng nào.')]";
    // Loc don hang theo trang thai (select) + nut 'Tìm' ap dung loc.
    public static final String ADMIN_ORDER_STATUS_FILTER = "css=[data-testid='admin-order-status-filter']";
    public static final String ADMIN_ORDER_FILTER_BTN = "xpath=//button[text()='Tìm']";

    // ---------- CRUD (dot 9) - them/sua/xoa tren giao dien Admin ----------
    // Thong bao thanh cong (.sa-notice, khong co testid) hien sau moi thao tac luu /
    // xoa / doi trang thai / dieu chinh diem (AdminPage.tsx:1060).
    public static final String ADMIN_NOTICE = "css=.sa-notice";

    // San pham: nut them + form them/sua (co testid) + nut sua/xoa tren tung dong
    // (aria-label chua ten SP, khong co testid rieng).
    public static final String ADMIN_PRODUCT_ADD_BTN = "css=[data-testid='admin-product-add-btn']";
    public static final String ADMIN_PRODUCT_NAME_INPUT = "css=[data-testid='admin-product-name-input']";
    public static final String ADMIN_PRODUCT_CATEGORY_SELECT = "css=[data-testid='admin-product-category-select']";
    public static final String ADMIN_PRODUCT_PRICE_INPUT = "css=[data-testid='admin-product-price-input']";
    public static final String ADMIN_PRODUCT_STOCK_INPUT = "css=[data-testid='admin-product-stock-input']";
    public static final String ADMIN_PRODUCT_SAVE_BTN = "css=[data-testid='admin-product-save-btn']";
    public static final String DYNAMIC_PRODUCT_EDIT_BTN = "xpath=//button[@aria-label='Sửa sản phẩm %s']";
    public static final String DYNAMIC_PRODUCT_DELETE_BTN = "xpath=//button[@aria-label='Xóa sản phẩm %s']";

    // Danh muc: o tim (aria-label, khong testid) + form them/sua; nut 'Lưu' cua form
    // khong co testid nen dinh vi theo text trong pham vi form.
    public static final String ADMIN_CATEGORY_SEARCH = "css=[aria-label='Tìm danh mục']";
    public static final String ADMIN_CATEGORY_FORM = "css=[data-testid='admin-category-form']";
    public static final String ADMIN_CATEGORY_NAME_INPUT = "css=[data-testid='admin-category-name-input']";
    public static final String ADMIN_CATEGORY_SAVE_BTN =
            "xpath=//*[@data-testid='admin-category-form']//button[text()='Lưu']";
    public static final String DYNAMIC_CATEGORY_EDIT_BTN = "xpath=//button[@aria-label='Sửa danh mục %s']";
    public static final String DYNAMIC_CATEGORY_DELETE_BTN = "xpath=//button[@aria-label='Xóa danh mục %s']";
    public static final String ADMIN_EMPTY_CATEGORIES_MSG =
            "xpath=//p[contains(text(),'Chưa có danh mục.')]";

    // Khuyen mai: o tim (aria-label) + form tao; o 'Giá trị giảm' khong co testid nen
    // dinh vi theo nhan <span> cua no trong form. Giao dien KHONG co nut sua/xoa
    // voucher - chi co kich hoat (Draft) va tat co ly do (Active/Draft).
    public static final String ADMIN_PROMO_SEARCH = "css=[aria-label='Tìm khuyến mãi']";
    public static final String ADMIN_PROMO_FORM = "css=[data-testid='admin-promo-form']";
    public static final String ADMIN_PROMO_CODE_INPUT = "css=[data-testid='admin-promo-code-input']";
    public static final String ADMIN_PROMO_DISCOUNT_VALUE_INPUT =
            "xpath=//*[@data-testid='admin-promo-form']//label[span[contains(text(),'Giá trị giảm')]]//input";
    public static final String ADMIN_PROMO_CREATE_BTN =
            "xpath=//*[@data-testid='admin-promo-form']//button[text()='Tạo khuyến mãi']";
    public static final String DYNAMIC_PROMO_ACTIVATE_BTN = "xpath=//button[@aria-label='Kích hoạt khuyến mãi %s']";
    public static final String DYNAMIC_PROMO_DISABLE_BTN = "xpath=//button[@aria-label='Tắt khuyến mãi %s']";
    public static final String ADMIN_PROMO_DISABLE_REASON = "css=[data-testid='admin-promo-disable-reason']";
    public static final String ADMIN_PROMO_DISABLE_CONFIRM_BTN = "xpath=//button[text()='Xác nhận tắt']";
    public static final String ADMIN_EMPTY_PROMOS_MSG =
            "xpath=//p[contains(text(),'Chưa có khuyến mãi.')]";

    // Don hang: nut mat mo chi tiet theo ma don hien thi + doi trang thai / huy don
    // trong modal chi tiet. Nut 'Đổi trạng thái' khong co testid nen dinh vi theo text
    // trong pham vi modal chi tiet don.
    public static final String DYNAMIC_ORDER_DETAIL_BTN = "xpath=//button[@aria-label='Xem chi tiết đơn %s']";
    public static final String ADMIN_ORDER_STATUS_SELECT = "css=[data-testid='admin-order-status-select']";
    public static final String ADMIN_ORDER_CHANGE_STATUS_BTN =
            "xpath=//*[@data-testid='admin-order-detail']//button[contains(text(),'Đổi trạng thái')]";
    public static final String ADMIN_ORDER_CANCEL_REASON = "css=[data-testid='admin-order-cancel-reason']";
    public static final String ADMIN_ORDER_CANCEL_BTN = "css=[data-testid='admin-order-cancel-btn']";

    // Hoi vien: form dieu chinh diem cho 1 khach (nhap email hoac ID, diem, ly do).
    public static final String ADMIN_ADJUST_POINTS_FORM = "css=[data-testid='admin-adjust-points-form']";
    public static final String ADMIN_ADJUST_USER_INPUT = "css=[data-testid='admin-adjust-user-input']";
    public static final String ADMIN_ADJUST_POINTS_INPUT = "css=[data-testid='admin-adjust-points-input']";
    public static final String ADMIN_ADJUST_REASON_INPUT = "css=[data-testid='admin-adjust-reason-input']";
    public static final String ADMIN_ADJUST_POINTS_BTN = "css=[data-testid='admin-adjust-points-btn']";
}
