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

    // ---------- Toolbar bang chuan Spark: search rieng cua muc, loc, Export ----------
    // Dot 12: o search tren topbar DA BO HAN (ke ca loc theo muc dang mo) - chi con
    // o tim kiem rieng cua tung muc trong toolbar ngay tren bang. Locator cu giu lai
    // chi de khang dinh am (test ADM-11): topbar khong duoc con o tim kiem.
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

    // ---------- Toolbar chuan + phan trang + form modal (dot 12, giao dien admin moi) ----------
    // Toolbar moi ngay tren moi bang: ben TRAI nut "+ Them moi" (nen #051C12, GIU
    // testid cu admin-product-add-btn / admin-category-add-btn / admin-promo-add-btn);
    // ben PHAI o tim kiem cua muc + nut "Tim" + nut icon "Dat lai" + nut icon
    // "Lam moi" + Export (muc nao co). Muc Tong quan KHONG co toolbar/phan trang;
    // bang hang hoi vien chi 4 dong nen KHONG phan trang.
    // Nut "Dat lai" / "Lam moi" cua tung muc (muc = orders/products/categories/
    // customers/promos/members/returns; bang nhat ky tra hang dung 'returns-audit').
    public static final String DYNAMIC_ADMIN_TOOLBAR_RESET_BTN = "css=[data-testid='admin-%s-reset-btn']";
    public static final String DYNAMIC_ADMIN_TOOLBAR_REFRESH_BTN = "css=[data-testid='admin-%s-refresh-btn']";
    // Form them/sua San pham / Danh muc / Khuyen mai gio mo trong MODAL (SparkModal,
    // co nut X aria-label='Đóng' + nut 'Đóng' trong form); testid form + tung truong
    // + nut Luu giu nguyen. Form dieu chinh diem hoi vien VAN inline (khong modal).
    public static final String ADMIN_PRODUCT_FORM_MODAL = "css=[data-testid='admin-product-form-modal']";
    public static final String ADMIN_CATEGORY_FORM_MODAL = "css=[data-testid='admin-category-form-modal']";
    public static final String ADMIN_PROMO_FORM_MODAL = "css=[data-testid='admin-promo-form-modal']";
    // Phan trang client-side 10 dong/trang (SaPagination): khoi "Trang X / Y — Tong
    // N dong" + nut Truoc/Sau + nut so trang. API Don hang / Khach hang tra toi da
    // 20 dong nen 2 muc nay phan trang toi da 2 trang.
    public static final String DYNAMIC_ADMIN_PAGINATION = "css=[data-testid='admin-%s-pagination']";
    public static final String DYNAMIC_ADMIN_PAGINATION_PREV = "css=[data-testid='admin-%s-pagination-prev']";
    public static final String DYNAMIC_ADMIN_PAGINATION_NEXT = "css=[data-testid='admin-%s-pagination-next']";
    public static final String DYNAMIC_ADMIN_PAGINATION_PAGE = "css=[data-testid='admin-%s-pagination-page-%s']";

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
    // co testid admin-category-save-btn (FE them dot redesign) nen dinh vi theo testid.
    public static final String ADMIN_CATEGORY_SEARCH = "css=[aria-label='Tìm danh mục']";
    public static final String ADMIN_CATEGORY_FORM = "css=[data-testid='admin-category-form']";
    public static final String ADMIN_CATEGORY_NAME_INPUT = "css=[data-testid='admin-category-name-input']";
    public static final String ADMIN_CATEGORY_SAVE_BTN =
            "css=[data-testid='admin-category-save-btn']";
    public static final String DYNAMIC_CATEGORY_EDIT_BTN = "xpath=//button[@aria-label='Sửa danh mục %s']";
    public static final String DYNAMIC_CATEGORY_DELETE_BTN = "xpath=//button[@aria-label='Xóa danh mục %s']";
    public static final String ADMIN_EMPTY_CATEGORIES_MSG =
            "xpath=//p[contains(text(),'Chưa có danh mục.')]";

    // Khuyen mai: o tim (aria-label) + form tao; o 'Giá trị giảm' khong co testid nen
    // dinh vi theo nhan <span> cua no trong form. Dot 13: giao dien DA CO nut
    // sua/xoa voucher Draft (admin-promo-edit-btn / admin-promo-delete-btn) +
    // hop xac nhan chung khi xoa; chi voucher Draft moi sua/xoa duoc.
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

    // ---------- Dot 13 ----------
    // Sua/xoa voucher Draft (muc 4): 2 nut chi hien o dong voucher Draft, testid
    // 'admin-promo-edit-btn' / 'admin-promo-delete-btn' LAP LAI tren moi dong nen
    // test phai dinh vi theo dong qua aria-label chua ma voucher (khong dung
    // testid tran). Form sua dung lai admin-promo-form; o Mo ta khong co testid
    // nen dinh vi theo nhan <span> trong form (giong o Gia tri giam); nut luu o
    // che do sua ghi 'Lưu thay đổi' (submit) chu khong phai 'Tạo khuyến mãi'.
    public static final String DYNAMIC_PROMO_EDIT_BTN = "xpath=//button[@aria-label='Sửa khuyến mãi %s']";
    public static final String DYNAMIC_PROMO_DELETE_BTN = "xpath=//button[@aria-label='Xóa khuyến mãi %s']";
    public static final String ADMIN_PROMO_DESCRIPTION_INPUT =
            "xpath=//*[@data-testid='admin-promo-form']//label[span[contains(text(),'Mô tả')]]//input";
    public static final String ADMIN_PROMO_SAVE_BTN =
            "xpath=//*[@data-testid='admin-promo-form']//button[@type='submit']";

    // Hop xac nhan dung chung (muc 5): SparkModal testid 'admin-confirm-modal',
    // nut 'Hủy' (admin-confirm-no-btn) + nut xac nhan (admin-confirm-yes-btn,
    // nhan thay doi theo hanh dong: 'Xóa khuyến mãi', 'Xóa sản phẩm'...).
    public static final String ADMIN_CONFIRM_MODAL = "css=[data-testid='admin-confirm-modal']";
    public static final String ADMIN_CONFIRM_YES_BTN = "css=[data-testid='admin-confirm-yes-btn']";
    public static final String ADMIN_CONFIRM_NO_BTN = "css=[data-testid='admin-confirm-no-btn']";

    // So du diem khach trong admin (muc 3): cot 'Điểm' o bang Khach hang dung
    // testid 'admin-customer-points-<id>' (id khach, KHONG phai email); dong
    // 'Số dư điểm: X điểm' trong modal chi tiet khach (admin-customer-detail-points).
    // Nut dong X cua modal chi tiet khach: SparkModal nay khong co testid rieng
    // nen dong bang nut X trong dialog cha cua khoi chi tiet (khong dung
    // closeDetailModal('admin-customer-detail') — nut X nam ngoai khoi do).
    public static final String ADMIN_CUSTOMER_DETAIL_POINTS = "css=[data-testid='admin-customer-detail-points']";
    public static final String CUSTOMER_DETAIL_CLOSE_BTN =
            "xpath=//*[@data-testid='admin-customer-detail']/ancestor::div[@role='dialog']//button[@aria-label='Đóng']";

    // Ghi nhan da hoan tien o muc Tra hang (muc 7): nut chi hien khi yeu cau
    // Approved + refundStatus PendingRefund + co refundId; modal nhap ma giao
    // dich (3-100 ky tu) goi POST /api/admin/refunds/{id}/record.
    public static final String ADMIN_RETURN_RECORD_REFUND_BTN = "css=[data-testid='admin-return-record-refund-btn']";
    public static final String ADMIN_REFUND_RECORD_MODAL = "css=[data-testid='admin-refund-record-modal']";
    public static final String ADMIN_REFUND_TRANSACTION_INPUT = "css=[data-testid='admin-refund-transaction-input']";
    public static final String ADMIN_REFUND_RECORD_CONFIRM_BTN = "css=[data-testid='admin-refund-record-confirm-btn']";

    // Loc ngay + Export nhat ky he thong (muc 10): nam duoi bang Tra hang, cung
    // tab 'returns' (khong phai tab rieng). Nut Export o day co testid rieng
    // (khac nut Export chung cua bang — khong dung ADMIN_EXPORT_BTN).
    public static final String ADMIN_AUDIT_LOG_HEADING =
            "xpath=//h2[contains(text(),'Nhật ký hệ thống')]";
    public static final String ADMIN_LOG_DATE_FROM = "css=[data-testid='admin-log-date-from']";
    public static final String ADMIN_LOG_DATE_TO = "css=[data-testid='admin-log-date-to']";
    public static final String ADMIN_LOG_FILTER_BTN = "css=[data-testid='admin-log-filter-btn']";
    public static final String ADMIN_LOG_EXPORT_BTN = "css=[data-testid='admin-log-export-btn']";

    // ---------- Dot redesign form admin + upload anh (drawer/modal form moi) ----------
    // Hop dong testid FE phai dung (xem TEST-MAPPING.md muc "Dot redesign form
    // admin + upload anh"). Form san pham gio la DRAWER (van testid
    // admin-product-form-modal); dropdown danh muc la custom (nut trigger, KHONG
    // con <select>) nen option khong co testid rieng — dinh vi theo text hien thi
    // trong pham vi modal (xem AdminPageObject.selectProductCategory).
    // Toast moi: admin-toast (thay the .sa-notice o cac luong form moi); hang moi
    // tao duoc highlight bang class sa-row-highlight (khong phai testid).
    public static final String ADMIN_TOAST = "css=[data-testid='admin-toast']";

    // ----- Form san pham (drawer) -----
    public static final String ADMIN_PRODUCT_ORIGINAL_PRICE_INPUT = "css=[data-testid='admin-product-original-price-input']";
    public static final String ADMIN_PRODUCT_DESCRIPTION_INPUT = "css=[data-testid='admin-product-description-input']";
    public static final String ADMIN_PRODUCT_NAME_COUNT = "css=[data-testid='admin-product-name-count']";
    public static final String ADMIN_PRODUCT_DESCRIPTION_COUNT = "css=[data-testid='admin-product-description-count']";
    public static final String ADMIN_PRODUCT_DISCOUNT_BADGE = "css=[data-testid='admin-product-discount-badge']";
    public static final String ADMIN_PRODUCT_STATUS_TOGGLE = "css=[data-testid='admin-product-status-toggle']";
    public static final String ADMIN_PRODUCT_PREVIEW = "css=[data-testid='admin-product-preview']";
    public static final String ADMIN_PRODUCT_STOCK_MINUS_BTN = "css=[data-testid='admin-product-stock-minus-btn']";
    public static final String ADMIN_PRODUCT_STOCK_PLUS_BTN = "css=[data-testid='admin-product-stock-plus-btn']";
    public static final String ADMIN_PRODUCT_CANCEL_BTN = "css=[data-testid='admin-product-cancel-btn']";
    // Loi tung truong trong form san pham (field = name/price/stock/category/...).
    public static final String DYNAMIC_PRODUCT_FIELD_ERROR = "css=[data-testid='admin-product-%s-error']";
    // Hang moi tao duoc highlight (class, khong phai testid).
    public static final String ADMIN_HIGHLIGHTED_ROWS = "xpath=//tr[contains(@class,'sa-row-highlight')]";
    // O gia ban gio tu dinh dang dau cham nghin khi go (go 150000 hien "150.000"):
    // khi assert gia tri o phai chuan hoa bang cach bo dau "." (AdminPageObject).
    // ----- Dropdown danh muc custom (nut trigger + option theo text) -----
    // O tim trong dropdown (neu FE co); option uu tien role='option', du phong
    // <li> hoac <button> co dung text — vi hop dong khong quy dinh testid option.
    public static final String ADMIN_PRODUCT_CATEGORY_SEARCH =
            "xpath=//*[@data-testid='admin-product-form-modal']//input[contains(@placeholder,'Tìm')]";
    public static final String ADMIN_PRODUCT_CATEGORY_OPTIONS = "xpath=//*[@data-testid='admin-product-form-modal']//*[@role='option']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION =
            "xpath=//*[@data-testid='admin-product-form-modal']//*[@role='option' and normalize-space(text())='%s']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION_LI =
            "xpath=//*[@data-testid='admin-product-form-modal']//li[normalize-space(.)='%s']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION_BTN =
            "xpath=//*[@data-testid='admin-product-form-modal']//button[normalize-space(text())='%s']";

    // ----- Upload anh san pham (nhieu anh, anh chinh, tien trinh, loi) -----
    public static final String ADMIN_PRODUCT_IMAGE_DROPZONE = "css=[data-testid='admin-product-image-dropzone']";
    public static final String ADMIN_PRODUCT_IMAGE_INPUT = "css=[data-testid='admin-product-image-input']";
    public static final String ADMIN_PRODUCT_IMAGE_LINK_TOGGLE = "css=[data-testid='admin-product-image-link-toggle']";
    public static final String ADMIN_PRODUCT_IMAGE_URL_INPUT = "css=[data-testid='admin-product-image-url-input']";
    public static final String ADMIN_PRODUCT_IMAGE_ERROR = "css=[data-testid='admin-product-image-error']";
    public static final String DYNAMIC_PRODUCT_IMAGE_THUMB = "css=[data-testid='admin-product-image-thumb-%s']";
    public static final String DYNAMIC_PRODUCT_IMAGE_THUMB_IMG = "css=[data-testid='admin-product-image-thumb-%s'] img";
    public static final String DYNAMIC_PRODUCT_IMAGE_THUMB_MAIN_BADGE = "css=[data-testid='admin-product-image-thumb-%s'] .sa-thumb-main";
    public static final String DYNAMIC_PRODUCT_IMAGE_SET_MAIN_BTN = "css=[data-testid='admin-product-image-set-main-btn-%s']";
    public static final String DYNAMIC_PRODUCT_IMAGE_REMOVE_BTN = "css=[data-testid='admin-product-image-remove-btn-%s']";
    public static final String DYNAMIC_PRODUCT_IMAGE_PROGRESS = "css=[data-testid='admin-product-image-progress-%s']";
    // Anh trong modal chi tiet san pham (dung de khang dinh SP vua tao co anh that).
    public static final String ADMIN_PRODUCT_DETAIL_IMG = "css=[data-testid='admin-product-detail'] img";
    // Anh thumbnail trong dong bang san pham (dong chua ten SP).
    public static final String DYNAMIC_PRODUCT_ROW_IMG = "xpath=//tr[td[contains(.,'%s')]]//img";

    // ----- Form danh muc -----
    // Nut 'Lưu' cua form danh muc da co testid admin-category-save-btn (FE them dot
    // redesign) — dung ADMIN_CATEGORY_SAVE_BTN o tren.
    public static final String ADMIN_CATEGORY_SLUG_INPUT = "css=[data-testid='admin-category-slug-input']";
    public static final String ADMIN_CATEGORY_DESCRIPTION_INPUT = "css=[data-testid='admin-category-description-input']";
    public static final String ADMIN_CATEGORY_SORT_ORDER_INPUT = "css=[data-testid='admin-category-sort-order-input']";
    public static final String ADMIN_CATEGORY_STATUS_TOGGLE = "css=[data-testid='admin-category-status-toggle']";
    public static final String ADMIN_CATEGORY_CANCEL_BTN = "css=[data-testid='admin-category-cancel-btn']";
    // Anh danh muc: chi 1 anh (IconUrl).
    public static final String ADMIN_CATEGORY_IMAGE_DROPZONE = "css=[data-testid='admin-category-image-dropzone']";
    public static final String ADMIN_CATEGORY_IMAGE_INPUT = "css=[data-testid='admin-category-image-input']";
    public static final String ADMIN_CATEGORY_IMAGE_THUMB = "css=[data-testid='admin-category-image-thumb']";
    public static final String ADMIN_CATEGORY_IMAGE_REMOVE_BTN = "css=[data-testid='admin-category-image-remove-btn']";

    // ----- Form khuyen mai -----
    // Mo ta voucher KHONG co testid trong hop dong nen giu locator theo nhan
    // <span> (ADMIN_PROMO_DESCRIPTION_INPUT). Nut submit dung chung cho ca tao
    // moi ('Tạo khuyến mãi') va sua ('Lưu thay đổi'): ADMIN_PROMO_SAVE_BTN.
    public static final String ADMIN_PROMO_GENERATE_CODE_BTN = "css=[data-testid='admin-promo-generate-code-btn']";
    public static final String ADMIN_PROMO_DISCOUNT_TYPE_FIXED = "css=[data-testid='admin-promo-discount-type-fixed']";
    public static final String ADMIN_PROMO_DISCOUNT_TYPE_PERCENT = "css=[data-testid='admin-promo-discount-type-percent']";
    public static final String ADMIN_PROMO_VALUE_INPUT = "css=[data-testid='admin-promo-value-input']";
    public static final String ADMIN_PROMO_MIN_ORDER_INPUT = "css=[data-testid='admin-promo-min-order-input']";
    public static final String ADMIN_PROMO_MAX_DISCOUNT_INPUT = "css=[data-testid='admin-promo-max-discount-input']";
    public static final String ADMIN_PROMO_MAX_USES_INPUT = "css=[data-testid='admin-promo-max-uses-input']";
    public static final String ADMIN_PROMO_EXPIRY_INPUT = "css=[data-testid='admin-promo-expiry-input']";
    public static final String ADMIN_PROMO_STATUS_TOGGLE = "css=[data-testid='admin-promo-status-toggle']";
    // Khuyen mai KHONG co upload anh (backend khong co field anh) — khong viet test upload cho khuyen mai.

    // ---------- Muc Danh gia (dot 16) ----------
    // Admin khong tao danh gia (danh gia la cua khach) nen toolbar KHONG co nut
    // Them moi; chi co o tim + loc sao + Tim / Dat lai / Lam moi / Export.
    // Bang chi hien khi co dong (reviews.length > 0); rong thi hien dong
    // "Chưa có đánh giá nào." thay cho bang. Phan trang (SaPagination) cung chi
    // hien khi co dong; nut so trang co testid <testid>-page-<p>.
    public static final String ADMIN_NAV_REVIEWS = "css=[data-testid='admin-nav-reviews']";
    public static final String ADMIN_REVIEWS_SECTION = "css=[data-testid='admin-reviews-section']";
    public static final String ADMIN_REVIEW_SEARCH = "css=[data-testid='admin-review-search']";
    public static final String ADMIN_REVIEW_RATING_FILTER = "css=[data-testid='admin-review-rating-filter']";
    public static final String ADMIN_REVIEWS_SEARCH_BTN = "css=[data-testid='admin-reviews-search-btn']";
    public static final String ADMIN_REVIEWS_RESET_BTN = "css=[data-testid='admin-reviews-reset-btn']";
    public static final String ADMIN_REVIEWS_REFRESH_BTN = "css=[data-testid='admin-reviews-refresh-btn']";
    public static final String ADMIN_REVIEWS_EXPORT_BTN = "css=[data-testid='admin-reviews-export-btn']";
    // Bang danh gia (gioi han trong section de khong lan sang bang muc khac).
    public static final String ADMIN_REVIEW_TABLE_ROWS =
            "xpath=//*[@data-testid='admin-reviews-section']//tbody/tr";
    public static final String ADMIN_REVIEW_TABLE_HEADERS =
            "xpath=//*[@data-testid='admin-reviews-section']//thead/tr/th";
    public static final String ADMIN_REVIEW_FIRST_ROW =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]";
    // O Sao: <span aria-label="{n} trên 5 sao">★★★★☆ (4/5)</span>.
    public static final String ADMIN_REVIEW_FIRST_ROW_PRODUCT =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[1]";
    public static final String ADMIN_REVIEW_FIRST_ROW_RATING =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[3]//span";
    // Noi dung trong bang bi cat toi da 80 ky tu + "..." (shortComment); trong
    // modal chi tiet hien day du.
    public static final String ADMIN_REVIEW_FIRST_ROW_COMMENT =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[4]";
    public static final String ADMIN_REVIEW_ROW_RATING_SPANS =
            "xpath=//*[@data-testid='admin-reviews-section']//tbody/tr/td[3]//span";
    // Nut mat / nut xoa tren tung dong (aria-label chua ten khach hang).
    public static final String ADMIN_REVIEW_FIRST_ROW_DETAIL_BTN =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]//button[starts-with(@aria-label,'Xem đánh giá của')]";
    public static final String ADMIN_REVIEW_LAST_ROW_DELETE_BTN =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[last()]//button[starts-with(@aria-label,'Xóa đánh giá của')]";
    // Phan trang (chi hien khi co dong).
    public static final String ADMIN_REVIEWS_PAGINATION = "css=[data-testid='admin-reviews-pagination']";
    public static final String ADMIN_REVIEWS_PAGINATION_PREV = "css=[data-testid='admin-reviews-pagination-prev']";
    public static final String ADMIN_REVIEWS_PAGINATION_NEXT = "css=[data-testid='admin-reviews-pagination-next']";
    public static final String DYNAMIC_ADMIN_REVIEWS_PAGINATION_PAGE =
            "css=[data-testid='admin-reviews-pagination-page-%s']";
    // Modal chi tiet (SparkModal): KvRow label 'Nội dung' hien day du, khong cat.
    public static final String ADMIN_REVIEW_DETAIL_MODAL = "css=[data-testid='admin-review-detail-modal']";
    public static final String ADMIN_REVIEW_DETAIL_COMMENT =
            "xpath=//*[@data-testid='admin-review-detail-modal']//div[@class='sa-kv'][span[text()='Nội dung']]/div";
    public static final String ADMIN_REVIEW_DELETE_BTN = "css=[data-testid='admin-review-delete-btn']";
    // Trang thai rong (khong co bang).
    public static final String ADMIN_EMPTY_REVIEWS_MSG =
            "xpath=//p[contains(text(),'Chưa có đánh giá nào.')]";
}
