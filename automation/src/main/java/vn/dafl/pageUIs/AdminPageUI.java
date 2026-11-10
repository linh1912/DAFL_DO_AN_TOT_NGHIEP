package vn.dafl.pageUIs;

/** Locator cua trang quan tri /admin (dot 3, chi Super Admin).
 *  Bao ve: chua dang nhap -> chuyen /login; tai khoan thuong -> trang 403
 *  (admin-forbidden, kem huong dan cap quyen bang SQL); Super Admin -> menu 10 muc
 *  (dot 17 them muc Danh gia giua Khach hang va Hoi vien; dot 17.1 tach Nhat ky
 *  he thong khoi Tra hang).
 *  Dot 8 tro ve truoc cac test chi dung thao tac DOC (GET). Tu dot 9, lop
 *  AdminCrudTest duoc phep them/sua/xoa QUA GIAO DIEN nhung CHI tren du lieu do
 *  chinh test tao (ten/ma kem timestamp) va tu don dep sau khi chay; khong xoa/sua
 *  du lieu seed, khong doi mat khau admin, khong duyet/tu choi tra hang cua nguoi khac.
 *  Dot 17: form them/sua la TRANG RIENG 2 cot (khong con Drawer/Modal), bang them
 *  cot STT + cot Ma + cot Trang thai dang Switch (role='switch', doc qua
 *  aria-checked), co dropdown loc Trang thai o toolbar; xoa dong dang Hoat dong
 *  bi chan boi popup 'Không thể xoá'.
 *  Dot 18 (redesign): form them/sua/xem TRO LAI Drawer qua FormShell + mode
 *  create|edit|view (data-form-mode) — wave D (17:42) CHI CON Drawer rong
 *  (SP 760px, DM/KM/KH/Don 680px, CSS var --fs-width), KHONG con Modal giua
 *  man hinh; BỎ HẲN Export khoi moi toolbar; popup dung chung ConfirmDialog
 *  (testid confirm-dialog, mac dinh CHI DUNG 2 nut footer — ngoai le 1 nut:
 *  A3 chan xoa DM [Đã hiểu] + link, G4 phien het han [Đăng nhập lại];
 *  ly do khoa co bo dem confirm-dialog-reason-counter, toi da 300 ky tu;
 *  XOA khong phan biet hoa thuong); toolbar chuan PageToolbar
 *  ({prefix}-search-input/-search-btn/-reset-btn/-add-btn, prefix tran
 *  orders/products/categories/promotions/customers/reviews/returns/logs, KHÔNG
 *  refresh, Thêm mới sát phải nhất, icon Lucide Search/RotateCcw);
 *  menu PHANG (wave D: tieu de nhom admin-nav-group-{sales|products|
 *  customers|system} la <div> KHONG bam duoc, ten dang tieu de 'Bán hàng /
 *  Quản lý sản phẩm / Quản lý khách hàng / Hệ thống', nhom cach 20px, muc
 *  cach 4px, KHONG dropdown, KHONG localStorage, KHONG badge tong nhom) +
 *  badge đếm cam nhạt (admin-nav-{orders|returns|reviews}-badge, toi da '99+',
 *  an khi 0); phan trang dung chung ('Hiển thị {from}–{to} / {total} kết quả'
 *  + 'Trang X / Y', KHÔNG nut so trang); EmptyState (testid 'empty-state',
 *  tieu de khong dau cham); tien hien '₫' (vd 99.000₫), khong con 'đ'. Toast
 *  dung admin-toast (toi da 3, tu an 4s/6s/8s); .sa-notice da bo han.
 *  Dashboard moi la component rieng (h1 'Tổng quan', KPI + khoi
 *  'Cần xử lý ngay', khong con bieu do cu admin-revenue-chart / nut 7-30 ngay).
 *  2 testid trung dot 18 da doi ten o wave D: nut [Chỉnh sửa] o footer form
 *  xem KM la admin-promo-view-edit-btn (nut Sua o dong voucher van la
 *  admin-promo-edit-btn); nut [Đóng] o footer chi tiet don (don terminal)
 *  la admin-order-detail-footer-close-btn (nut X cua FormShell van la
 *  admin-order-detail-close-btn). */
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
    public static final String ADMIN_NAV_LOGS = "css=[data-testid='admin-nav-logs']";

    // Tieu de muc Tong quan (h1, dashboard moi dot 18) + o tim kiem cua cac muc
    // (PageToolbar dot 18: {prefix}-search-input, prefix tran, khong 'admin-').
    public static final String ADMIN_DASHBOARD_HEADING = "xpath=//h1[contains(text(),'Tổng quan')]";
    public static final String ADMIN_DASHBOARD_ACTION_NEEDED = "css=[data-testid='admin-dashboard-action-needed']";
    public static final String ADMIN_DASHBOARD_PENDING_ORDERS_VIEW =
            "css=[data-testid='admin-dashboard-action-pending-orders-view']";
    public static final String ADMIN_ORDER_SEARCH = "css=[data-testid='orders-search-input']";
    // Dot 21 — nut icon 'Tìm' (orders-search-btn) DA BO khoi toolbar Don hang
    // (loc ap dung ngay khi go/doi dropdown); khong con element that.
    // Nut "×" xoa nhanh trong o tim (chi hien khi o tim co chu).
    public static final String ADMIN_ORDER_SEARCH_CLEAR_BTN = "css=[data-testid='orders-search-clear-btn']";
    // Dot 21 — so ket qua "N / M don" (loc duoc / da tai) + nut "Xóa bộ lọc"
    // trong EmptyState khi tim/loc khong co ket qua.
    public static final String ADMIN_ORDER_RESULT_COUNT = "css=[data-testid='admin-orders-result-count']";
    public static final String ADMIN_ORDER_CLEAR_FILTERS_BTN = "css=[data-testid='admin-orders-clear-filters-btn']";
    public static final String ADMIN_PRODUCT_SEARCH = "css=[data-testid='products-search-input']";
    // Dot 21 — toolbar SP loc ngay (debounce 120ms) nen KHONG con nut icon
    // 'Tìm' (products-search-btn da bo) — tim chi go tu khoa + cho loc ap dung.
    public static final String ADMIN_PRODUCT_SEARCH_CLEAR_BTN = "css=[data-testid='products-search-clear-btn']";
    public static final String ADMIN_PRODUCT_RESET_BTN = "css=[data-testid='products-reset-btn']";
    public static final String ADMIN_CUSTOMER_SEARCH = "css=[data-testid='customers-search-input']";
    public static final String ADMIN_CUSTOMER_SEARCH_BTN = "css=[data-testid='customers-search-btn']";
    public static final String ADMIN_PROMO_ADD_BTN = "css=[data-testid='promotions-add-btn']";
    public static final String ADMIN_CATEGORY_ADD_BTN = "css=[data-testid='categories-add-btn']";

    // Dashboard moi dot 18 (Dashboard.tsx, component rieng): KPI + khoi 'Cần xử
    // lý ngay' + nut 'Xem' tung muc (admin-dashboard-action-*-view). Bieu do
    // doanh thu cu (admin-revenue-chart) + 2 nut chon khoang 7/30 ngay DA BO —
    // khong con locator cho chung (xem AdminDashboard.test.tsx ben Vitest).

    // ---------- Trang chi tiet san pham full page (dot 21, khong con Drawer) ----------
    // Nut mat (👁) tren tung dong bang mo TRANG CHI TIET /products/:code
    // (breadcrumb "Quản lý sản phẩm > Chi tiết sản phẩm", 4 card: Thong tin
    // chung / Gia & ton kho / Hinh anh / Mo ta, header [Quay lại] + [Chỉnh sửa]).
    // Cot "Đánh giá" DA BO khoi bang danh sach SP (dot 21) — du lieu rating
    // chi con o trang chi tiet nay (admin-product-detail-rating) + module
    // Đánh giá rieng; khong con locator cot Danh gia o bang.
    public static final String ADMIN_PRODUCT_DETAIL = "css=[data-testid='admin-product-detail-page']";
    public static final String ADMIN_PRODUCT_DETAIL_BREADCRUMB = "css=[data-testid='admin-product-breadcrumb']";
    public static final String ADMIN_PRODUCT_DETAIL_RATING = "css=[data-testid='admin-product-detail-rating']";
    // Hang 'Đánh giá' tren trang chi tiet (luon co; khi SP chua co danh gia
    // thi chi hien '—' o hang nay, span admin-product-detail-rating khong render).
    public static final String ADMIN_PRODUCT_DETAIL_RATING_ROW = "css=[data-testid='admin-product-detail-rating-row']";
    public static final String ADMIN_PRODUCT_DETAIL_BACK_BTN = "css=[data-testid='admin-product-detail-back-btn']";
    public static final String ADMIN_PRODUCT_DETAIL_EDIT_BTN = "css=[data-testid='admin-product-detail-edit-btn']";
    // Link 'Quản lý sản phẩm' o breadcrumb trang chi tiet (bam de ve danh sach).
    public static final String ADMIN_PRODUCT_BREADCRUMB_LINK =
            "xpath=//*[@data-testid='admin-product-breadcrumb']//button[text()='Quản lý sản phẩm']";
    // Tat ca nut mat tren bang SP (aria-label 'Xem chi tiết sản phẩm {ten}').
    public static final String ADMIN_PRODUCT_DETAIL_BTNS =
            "xpath=//button[starts-with(@aria-label,'Xem chi tiết sản phẩm ')]";
    // Trang them/sua SP full page (/products/new, /products/:code/edit).
    public static final String ADMIN_PRODUCT_CREATE_PAGE = "css=[data-testid='admin-product-create-page']";
    public static final String ADMIN_PRODUCT_EDIT_PAGE = "css=[data-testid='admin-product-edit-page']";
    public static final String ADMIN_PRODUCT_NOT_FOUND = "css=[data-testid='admin-product-not-found']";
    public static final String ADMIN_CATEGORY_DETAIL = "css=[data-testid='admin-category-form-modal']";
    // Dot 21: chi tiet KM KHONG con Drawer — trang full page /promotions/:code
    // (admin-promo-detail-page, 4 card: general/conditions/time/stats).
    public static final String ADMIN_PROMO_DETAIL = "css=[data-testid='admin-promo-detail-page']";
    public static final String ADMIN_PROMO_DETAIL_BREADCRUMB = "css=[data-testid='admin-promo-breadcrumb']";
    public static final String ADMIN_PROMO_DETAIL_BACK_BTN = "css=[data-testid='admin-promo-detail-back-btn']";
    public static final String ADMIN_PROMO_DETAIL_EDIT_BTN = "css=[data-testid='admin-promo-detail-edit-btn']";
    // Trang them/sua KM full page (/promotions/new, /promotions/:code/edit).
    public static final String ADMIN_PROMO_CREATE_PAGE = "css=[data-testid='admin-promo-create-page']";
    public static final String ADMIN_PROMO_EDIT_PAGE = "css=[data-testid='admin-promo-edit-page']";
    // Nut quay lai danh sach trong breadcrumb cua trang full page DM/KH/Tra/Đánh gia
    // (EntityBreadcrumb: nut root co text la ten muc, vd 'Trả hàng').
    public static final String ADMIN_ENTITY_BREADCRUMB_BACK =
            "xpath=//*[@data-testid='admin-entity-breadcrumb']//button";
    public static final String ADMIN_CATEGORY_CREATE_PAGE = "css=[data-testid='admin-category-create-page']";
    public static final String ADMIN_CATEGORY_EDIT_PAGE = "css=[data-testid='admin-category-edit-page']";
    public static final String ADMIN_CATEGORY_DETAIL_PAGE = "css=[data-testid='admin-category-detail-page']";
    // Trang xem/sua KH full page dot 21 (/customers/:code,
    // /customers/:code/edit) — KHONG co trang them moi.
    public static final String ADMIN_CUSTOMER_DETAIL_PAGE = "css=[data-testid='admin-customer-detail-page']";
    public static final String ADMIN_CUSTOMER_EDIT_PAGE = "css=[data-testid='admin-customer-edit-page']";
    // Trang chi tiet tra hang full page dot 21 (/returns/:code) + chi tiet
    // danh gia full page dot 21 (/reviews/:id).
    public static final String ADMIN_RETURN_DETAIL_PAGE = "css=[data-testid='admin-return-detail-page']";
    public static final String ADMIN_REVIEW_DETAIL_PAGE = "css=[data-testid='admin-review-detail-page']";
    public static final String ADMIN_PROMO_NOT_FOUND = "css=[data-testid='admin-promo-not-found']";
    public static final String ADMIN_TIER_DETAIL = "css=[data-testid='admin-tier-detail']";
    // (ADMIN_RETURN_DETAIL da bo dot 21 — tra hang la trang full page, dung
    // ADMIN_RETURN_DETAIL_PAGE. Giu hang so de cac file ngoai pham vi dot 21
    // van bien dich duoc; test nao dung no can cap nhat sang trang moi.)
    public static final String ADMIN_RETURN_DETAIL = "css=[data-testid='admin-return-detail']";
    public static final String ADMIN_ORDER_DETAIL = "css=[data-testid='admin-order-detail']";
    // (ADMIN_CUSTOMER_DETAIL da bo dot 21 — KH la trang full page, dung
    // ADMIN_CUSTOMER_DETAIL_PAGE / ADMIN_CUSTOMER_EDIT_PAGE. Giu hang so de
    // cac file ngoai pham vi dot 21 van bien dich duoc.)
    public static final String ADMIN_CUSTOMER_DETAIL = "css=[data-testid='admin-customer-form-page']";
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
    // Nut dong X cua FormShell/SparkModal, nam trong modal co testid cho truoc (%s).
    public static final String DYNAMIC_MODAL_CLOSE_BTN =
            "css=[data-testid='%s'] button[aria-label='Đóng']";
    // Nut 'Đóng' o footer modal/form xem, nam trong modal co testid cho truoc (%s).
    public static final String DYNAMIC_MODAL_FOOTER_CLOSE_BTN =
            "xpath=//*[@data-testid='%s']//button[text()='Đóng']";
    // Nut 'Chỉnh sửa' o header trang chi tiet SP (mo trang sua, giu du lieu,
    // khong luu). Wave D: nut [Chỉnh sửa] o footer form xem KM co testid
    // RIENG admin-promo-view-edit-btn (khong con trung nut Sua o dong
    // voucher Draft trong bang — nut do van la admin-promo-edit-btn).
    public static final String PRODUCT_VIEW_EDIT_BTN =
            "css=[data-testid='admin-product-detail-edit-btn']";
    // Nut mat cua 1 dong SP cu the (aria-label 'Xem chi tiết sản phẩm {ten}').
    public static final String DYNAMIC_PRODUCT_VIEW_BTN =
            "xpath=//button[@aria-label='Xem chi tiết sản phẩm %s']";
    public static final String CATEGORY_VIEW_EDIT_BTN =
            "css=[data-testid='admin-category-edit-btn']";
    public static final String CUSTOMER_VIEW_EDIT_BTN =
            "css=[data-testid='admin-customer-edit-btn']";
    // Dot 21: nut [Chỉnh sửa] o header trang chi tiet KM full page (chi hien
    // khi voucher Draft) — giu ten hang de PO/test khoi doi.
    public static final String ADMIN_PROMO_VIEW_EDIT_BTN =
            "css=[data-testid='admin-promo-detail-edit-btn']";
    // Form them/sua san pham (mo tu modal chi tiet) + nut 'Đóng' cua form.
    public static final String ADMIN_PRODUCT_FORM = "css=[data-testid='admin-product-form']";
    public static final String ADMIN_PRODUCT_FORM_CLOSE_BTN =
            "xpath=//*[@data-testid='admin-product-form']//button[text()='Đóng']";

    // ---------- Toolbar chuan PageToolbar (dot 18): search rieng cua muc, loc, KHONG Export ----------
    // Dot 12: o search tren topbar DA BO HAN (ke ca loc theo muc dang mo) - chi con
    // o tim kiem rieng cua tung muc trong toolbar ngay tren bang. Locator cu giu lai
    // chi de khang dinh am (test ADM-11): topbar khong duoc con o tim kiem.
    public static final String ADMIN_TOPBAR_SEARCH_INPUT = "css=.sa-topbar-search input";
    // Dot 18: Export DA BO HAN khoi MOI toolbar admin (quyet dinh cua Matthew
    // 2026-11-10) — khong con nut Export nao (ADMIN_EXPORT_BTN cu da xoa).
    // Tat ca dong du lieu trong bang cua muc dang mo.
    public static final String ADMIN_TABLE_ROWS = "xpath=//div[contains(@class,'sa-table-wrap')]//tbody/tr";
    // Dong bao rong cua tung bang (EmptyState dot 18, tieu de KHONG dau cham):
    // khi dang tim/loc thi 'Không tìm thấy ...', khi chua co du lieu thi
    // 'Chưa có ... nào'. Cac test duoi deu tim/loc truoc nen dung dang
    // 'Không tìm thấy ...' (rieng muc Danh gia dung 'Chưa có đánh giá nào'
    // cho trang thai rong khong loc).
    public static final String ADMIN_EMPTY_PRODUCTS_MSG =
            "xpath=//p[contains(text(),'Không tìm thấy sản phẩm')]";
    public static final String ADMIN_EMPTY_CUSTOMERS_MSG =
            "xpath=//p[contains(text(),'Không tìm thấy khách hàng')]";
    public static final String ADMIN_EMPTY_ORDERS_MSG =
            "xpath=//p[contains(text(),'Không tìm thấy đơn hàng')]";
    public static final String ADMIN_EMPTY_RETURNS_MSG =
            "xpath=//p[contains(text(),'Chưa có yêu cầu trả hàng nào')]";
    // Loc don hang theo trang thai (select) — dot 21: loc client-side tren
    // 100 don da tai (dropdown 6 nhom, doi la loc ngay, khong con nut Tim).
    public static final String ADMIN_ORDER_STATUS_FILTER = "css=[data-testid='admin-order-status-filter']";

    // ---------- Toolbar chuan + phan trang + form Drawer (dot 18 wave D) ----------
    // Toolbar moi (PageToolbar) ngay tren moi bang: ben TRAI o tim kiem cua muc
    // + filter; ben PHAI nut icon "Tìm" + nut icon "Đặt lại" + nut "Thêm mới"
    // primary SAT PHAI NHAT (chi muc nao co them moi). KHONG co nut "Làm mới",
    // KHONG co Export. Muc Tong quan KHONG co toolbar/phan trang; bang hang hoi
    // vien chi 4 dong nen KHONG phan trang. Icon nut Tim / Dat lai la Lucide
    // Search / RotateCcw (svg 18px), KHONG con emoji 🔍/↺.
    // Dot 21 NGOAI LE chi muc Don hang: KHONG nut Tim; nut Dat lai la nut chu
    // "Đặt lại bộ lọc" (disable khi chua loc); o tim co nut "×" xoa nhanh.
    // Nut "Đặt lại" cua tung muc (prefix tran: orders/products/categories/
    // promotions/customers/reviews/returns/logs — KHONG con tien to 'admin-').
    public static final String DYNAMIC_ADMIN_TOOLBAR_RESET_BTN = "css=[data-testid='%s-reset-btn']";
    public static final String DYNAMIC_ADMIN_TOOLBAR_SEARCH_BTN = "css=[data-testid='%s-search-btn']";
    public static final String DYNAMIC_ADMIN_TOOLBAR_ADD_BTN = "css=[data-testid='%s-add-btn']";
    // Icon Lucide trong nut Tim / Dat lai cua toolbar (prefix tran).
    public static final String DYNAMIC_ADMIN_TOOLBAR_SEARCH_ICON =
            "css=[data-testid='%s-search-btn'] svg.lucide-search";
    public static final String DYNAMIC_ADMIN_TOOLBAR_RESET_ICON =
            "css=[data-testid='%s-reset-btn'] svg.lucide-rotate-ccw";
    // Khoi toolbar dau tien tren trang (dung de khang dinh am: khong emoji).
    public static final String ADMIN_PAGE_TOOLBAR = "css=.sa-page-toolbar";
    // Nut "Thêm mới" o toolbar (prefix tran). Luu y: admin-promo-add-btn cu
    // nay la promotions-add-btn, admin-category-add-btn la categories-add-btn.
    public static final String ADMIN_PRODUCT_ADD_BTN = "css=[data-testid='products-add-btn']";
    // Form them/sua San pham DOT 21 la TRANG full page (admin-product-form
    // trong /products/new hoac /products/:code/edit, KHONG con Drawer
    // admin-product-form-modal); Danh muc / Khuyen mai / Khach hang DOT 18
    // wave D CHI CON DRAWER rong qua FormShell (DM/KM/KH 680px, CSS var
    // --fs-width; KHONG con Modal giua man hinh), co
    // data-form-mode='create|edit|view'; khi form Drawer mo, bang danh sach
    // VAN O LAI trong DOM (overlay, khong bi thay the nhu form trang rieng).
    // (ADMIN_PRODUCT_FORM_MODAL da bo — dung ADMIN_PRODUCT_FORM de dinh vi
    // form SP o trang; xem AdminPageObject.isProductFormModalDisplayed.)
    public static final String ADMIN_CATEGORY_FORM_MODAL = "css=[data-testid='admin-category-form-modal']";
    // (ADMIN_PROMO_FORM_MODAL da bo dot 21 — KM la trang full page, dung
    // ADMIN_PROMO_CREATE_PAGE / ADMIN_PROMO_EDIT_PAGE / ADMIN_PROMO_FORM.)
    // Phan trang dung chung (shared Pagination, dot 18): khoi 'Hiển thị {from}–
    // {to} / {total} kết quả' + 'Trang X / Y' o {testid}-range + nut Truoc/Sau
    // ({testid}-prev/-next); KHONG con nut so trang. API Don hang / Khach hang
    // tra toi da 20 dong nen 2 muc nay phan trang toi da 2 trang.
    public static final String DYNAMIC_ADMIN_PAGINATION = "css=[data-testid='admin-%s-pagination']";
    public static final String DYNAMIC_ADMIN_PAGINATION_PREV = "css=[data-testid='admin-%s-pagination-prev']";
    public static final String DYNAMIC_ADMIN_PAGINATION_NEXT = "css=[data-testid='admin-%s-pagination-next']";
    public static final String DYNAMIC_ADMIN_PAGINATION_RANGE = "css=[data-testid='admin-%s-pagination-range']";

    // ---------- CRUD (dot 9) - them/sua/xoa tren giao dien Admin ----------
    // Dot 18: thong bao thanh cong dung Toast (admin-toast + admin-toast-title/
    // admin-toast-desc, toi da 3, tu an 4s/6s/8s); .sa-notice DA BO HAN.
    // (ADMIN_NOTICE giu lai chi de waitForFeedback() tuong thich nguoc.)
    public static final String ADMIN_NOTICE = "css=.sa-notice";

    // San pham: nut them + form them/sua (co testid) + nut sua/xoa tren tung dong
    // (aria-label chua ten SP, khong co testid rieng).
    public static final String ADMIN_PRODUCT_NAME_INPUT = "css=[data-testid='admin-product-name-input']";
    public static final String ADMIN_PRODUCT_CATEGORY_SELECT = "css=[data-testid='admin-product-category-select']";
    public static final String ADMIN_PRODUCT_PRICE_INPUT = "css=[data-testid='admin-product-price-input']";
    public static final String ADMIN_PRODUCT_STOCK_INPUT = "css=[data-testid='admin-product-stock-input']";
    public static final String ADMIN_PRODUCT_SAVE_BTN = "css=[data-testid='admin-product-save-btn']";
    public static final String DYNAMIC_PRODUCT_EDIT_BTN = "xpath=//button[@aria-label='Sửa sản phẩm %s']";
    public static final String DYNAMIC_PRODUCT_DELETE_BTN = "xpath=//button[@aria-label='Xóa sản phẩm %s']";

    // Danh muc: o tim o toolbar (categories-search-input) + form them/sua; nut
    // 'Lưu' cua form co testid admin-category-save-btn (FE them dot redesign)
    // nen dinh vi theo testid.
    public static final String ADMIN_CATEGORY_SEARCH = "css=[data-testid='categories-search-input']";
    public static final String ADMIN_CATEGORY_SEARCH_BTN = "css=[data-testid='categories-search-btn']";
    public static final String ADMIN_CATEGORY_FORM = "css=[data-testid='admin-category-form']";
    public static final String ADMIN_CATEGORY_NAME_INPUT = "css=[data-testid='admin-category-name-input']";
    public static final String ADMIN_CATEGORY_SAVE_BTN =
            "css=[data-testid='admin-category-save-btn']";
    public static final String DYNAMIC_CATEGORY_EDIT_BTN = "xpath=//button[@aria-label='Sửa danh mục %s']";
    public static final String DYNAMIC_CATEGORY_DELETE_BTN = "xpath=//button[@aria-label='Xóa danh mục %s']";
    public static final String ADMIN_EMPTY_CATEGORIES_MSG =
            "xpath=//p[contains(text(),'Không tìm thấy danh mục')]";

    // Khuyen mai: o tim o toolbar (promotions-search-input, loc server-side:
    // go xong phai bam nut Tim) + form tao; o 'Giá trị giảm' khong co testid
    // nen dinh vi theo nhan <span> cua no trong form. Dot 13: giao dien DA CO
    // nut sua/xoa voucher Draft (admin-promo-edit-btn / admin-promo-delete-btn)
    // + hop xac nhan chung khi xoa; chi voucher Draft moi sua/xoa duoc.
    public static final String ADMIN_PROMO_SEARCH = "css=[data-testid='promotions-search-input']";
    public static final String ADMIN_PROMO_SEARCH_BTN = "css=[data-testid='promotions-search-btn']";
    public static final String ADMIN_PROMO_FORM = "css=[data-testid='admin-promo-form']";
    public static final String ADMIN_PROMO_CODE_INPUT = "css=[data-testid='admin-promo-code-input']";
    public static final String ADMIN_PROMO_DISCOUNT_VALUE_INPUT =
            "xpath=//*[@data-testid='admin-promo-form']//label[span[contains(text(),'Giá trị giảm')]]//input";
    // Dot 21: KHONG con nut 'Tạo khuyến mãi' rieng (nut submit cua form la
    // 'Lưu' khi tao / 'Lưu thay đổi' khi sua: ADMIN_PROMO_SAVE_BTN); KHONG con
    // nut Play 'Kích hoạt khuyến mãi' / 'Tắt khuyến mãi' rieng — bat/tat qua
    // Switch trang thai tren dong (DYNAMIC_ROW_STATUS_SWITCH).
    // Dot 21 (22:37): tat/bat voucher bang Switch deu qua modal xac nhan CHUNG
    // statusChange ('Xác nhận thay đổi trạng thái?', [Hủy][Xác nhận]) + toast
    // statusChanged ('Đổi trạng thái khuyến mãi thành công!'); popup C3 tat co
    // ly do CHI con o luong chan xoa A5 (nut phai [Tắt khuyến mãi]).
    public static final String ADMIN_EMPTY_PROMOS_MSG =
            "xpath=//p[contains(text(),'Không tìm thấy khuyến mãi')]";

    // Don hang (dot 21): chi tiet don la TRANG RIENG /orders/:code (bo Drawer
    // FormShell). Header co 2 nut [Hủy đơn] + [Đổi trạng thái] luon hien (disable
    // + tooltip khi khong hop le); bam nut mo PANEL ngay duoi header (khong mo
    // popup D2/D3/D1 nua — panel chinh la buoc xac nhan): panel doi trang thai
    // [Đóng]+[Xác nhận đổi], panel huy don nen do nhat [Giữ đơn]+[Xác nhận hủy
    // đơn] + o ly do BAT BUOC (loi 'Vui lòng nhập lý do hủy đơn.'). Nut X
    // FormShell (admin-order-detail-close-btn) va nut [Đóng] o footer
    // (admin-order-detail-footer-close-btn) da xoa cung Drawer; ve danh sach
    // bang breadcrumb [admin-order-detail-back] (ve /admin, tab orders).
    public static final String DYNAMIC_ORDER_DETAIL_BTN = "xpath=//button[@aria-label='Xem chi tiết đơn %s']";
    public static final String ADMIN_ORDER_DETAIL_BACK_BTN = "css=[data-testid='admin-order-detail-back']";
    public static final String ADMIN_ORDER_DETAIL_TITLE = "css=[data-testid='admin-order-detail-title']";
    public static final String ADMIN_ORDER_DETAIL_STATUS = "css=[data-testid='admin-order-detail-status']";
    public static final String ADMIN_ORDER_DETAIL_HEADER_BUTTONS =
            "xpath=//*[@data-testid='admin-order-detail']//div[contains(@class,'od-header-actions')]//button";
    public static final String ADMIN_ORDER_STATUS_SELECT = "css=[data-testid='admin-order-status-select']";
    public static final String ADMIN_ORDER_CHANGE_STATUS_BTN =
            "css=[data-testid='admin-order-change-status-btn']";
    public static final String ADMIN_ORDER_CANCEL_BTN = "css=[data-testid='admin-order-cancel-btn']";
    public static final String ADMIN_ORDER_STATUS_PANEL = "css=[data-testid='admin-order-status-panel']";
    public static final String ADMIN_ORDER_STATUS_CONFIRM_BTN = "css=[data-testid='admin-order-status-confirm-btn']";
    public static final String ADMIN_ORDER_STATUS_CLOSE_BTN = "css=[data-testid='admin-order-status-close-btn']";
    public static final String ADMIN_ORDER_STATUS_ERROR = "css=[data-testid='admin-order-status-error']";
    public static final String ADMIN_ORDER_CANCEL_PANEL = "css=[data-testid='admin-order-cancel-panel']";
    public static final String ADMIN_ORDER_CANCEL_REASON_INPUT = "css=[data-testid='admin-order-cancel-reason-input']";
    public static final String ADMIN_ORDER_CANCEL_CONFIRM_BTN = "css=[data-testid='admin-order-cancel-confirm-btn']";
    public static final String ADMIN_ORDER_CANCEL_KEEP_BTN = "css=[data-testid='admin-order-cancel-keep-btn']";
    public static final String ADMIN_ORDER_CANCEL_ERROR = "css=[data-testid='admin-order-cancel-error']";

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

    // ---------- Hop xac nhan dung chung (dot 18): ConfirmDialog + confirmAction ----------
    // MOI popup xac nhan deu dung ConfirmDialog: container 'confirm-dialog',
    // tieu de / thong diep / o ly do (requireReason) / o go xac nhan
    // (requireTypeToConfirm) / dong 'không thể hoàn tác' (irreversible) / loi
    // API; footer CHỈ ĐÚNG 2 nút: [Hủy] (confirm-dialog-cancel-btn) + nút xác
    // nhận (confirm-dialog-confirm-btn, nhãn thay đổi theo hành động).
    // Hop cu admin-confirm-modal + admin-confirm-yes/no-btn DA BO.
    public static final String CONFIRM_DIALOG = "css=[data-testid='confirm-dialog']";
    public static final String CONFIRM_DIALOG_TITLE = "css=[data-testid='confirm-dialog-title']";
    public static final String CONFIRM_DIALOG_MESSAGE = "css=[data-testid='confirm-dialog-message']";
    public static final String CONFIRM_DIALOG_REASON_INPUT = "css=[data-testid='confirm-dialog-reason-input']";
    public static final String CONFIRM_DIALOG_REASON_COUNTER = "css=[data-testid='confirm-dialog-reason-counter']";
    public static final String CONFIRM_DIALOG_LINK = "css=[data-testid='confirm-dialog-link']";
    public static final String CONFIRM_DIALOG_REASON_ERROR = "css=[data-testid='confirm-dialog-reason-error']";
    public static final String CONFIRM_DIALOG_TYPE_INPUT = "css=[data-testid='confirm-dialog-type-input']";
    public static final String CONFIRM_DIALOG_CONFIRM_BTN = "css=[data-testid='confirm-dialog-confirm-btn']";
    public static final String CONFIRM_DIALOG_CANCEL_BTN = "css=[data-testid='confirm-dialog-cancel-btn']";
    public static final String CONFIRM_DIALOG_ERROR = "css=[data-testid='confirm-dialog-error']";
    // 2 nut o footer popup (khung .sa-confirm-actions) — dung de khang dinh
    // popup CHI co dung 2 nut.
    public static final String CONFIRM_DIALOG_FOOTER_BUTTONS =
            "xpath=//*[@data-testid='confirm-dialog']//div[contains(@class,'sa-confirm-actions')]//button";
    // 2 nut o footer form XEM / modal chi tiet (khung .fs-foot) — dung de
    // khang dinh form xem CHI co [Đóng] + [Chỉnh sửa], modal tra hang CHI co
    // [Từ chối] + [Duyệt], modal don co dung 2 nut theo trang thai.
    public static final String DYNAMIC_FORM_FOOTER_BUTTONS =
            "xpath=//*[@data-testid='%s']//div[contains(@class,'fs-foot')]//button";
    // Cac nut o header sticky cua TRANG full page dot 21 (khung
    // .sa-promo-page-head-actions) — dung de khang dinh trang xem CHI co
    // [Đóng] + [Chỉnh sửa], trang tra hang CHI co [Từ chối] + [Duyệt].
    public static final String DYNAMIC_PAGE_HEAD_BUTTONS =
            "xpath=//*[@data-testid='%s']//div[contains(@class,'sa-promo-page-head-actions')]//button";
    // Panel xac nhan inline cua trang chi tiet tra hang full page dot 21
    // (KHONG ConfirmDialog): Duyệt -> panel + nut [Duyệt yêu cầu]; Từ chối ->
    // panel + o ly do bat buoc + nut [Từ chối yêu cầu].
    public static final String ADMIN_RETURN_APPROVE_PANEL = "css=[data-testid='admin-return-approve-panel']";
    public static final String ADMIN_RETURN_REJECT_PANEL = "css=[data-testid='admin-return-reject-panel']";
    public static final String ADMIN_RETURN_APPROVE_CONFIRM_BTN = "css=[data-testid='admin-return-approve-confirm-btn']";
    public static final String ADMIN_RETURN_REJECT_CONFIRM_BTN = "css=[data-testid='admin-return-reject-confirm-btn']";
    public static final String ADMIN_RETURN_REJECT_REASON_INPUT = "css=[data-testid='admin-return-reject-reason-input']";
    public static final String ADMIN_RETURN_REJECT_ERROR = "css=[data-testid='admin-return-reject-error']";

    // So du diem khach trong admin (muc 3): cot 'Điểm' o bang Khach hang dung
    // testid 'admin-customer-points-<id>' (id khach, KHONG phai email). Dot 18:
    // form XEM khach KHONG con dong 'Số dư điểm' (admin-customer-detail-points
    // da xoa) — doc diem TRUC TIEP tu cot trong bang qua
    // DYNAMIC_CUSTOMER_POINTS_CELL (email = email KH trong dong do).
    public static final String DYNAMIC_CUSTOMER_POINTS_CELL =
            "xpath=//tr[td[contains(.,'%s')]]//td[starts-with(@data-testid,'admin-customer-points-')]";

    // Ghi nhan da hoan tien o muc Tra hang (muc 7): nut chi hien khi yeu cau
    // Approved + refundStatus PendingRefund + co refundId; modal nhap ma giao
    // dich (3-100 ky tu) goi POST /api/admin/refunds/{id}/record.
    public static final String ADMIN_RETURN_RECORD_REFUND_BTN = "css=[data-testid='admin-return-record-refund-btn']";
    public static final String ADMIN_REFUND_RECORD_MODAL = "css=[data-testid='admin-refund-record-modal']";
    public static final String ADMIN_REFUND_TRANSACTION_INPUT = "css=[data-testid='admin-refund-transaction-input']";
    public static final String ADMIN_REFUND_RECORD_CONFIRM_BTN = "css=[data-testid='admin-refund-record-confirm-btn']";

    // Loc ngay nhat ky he thong (muc 10): tab 'logs' rieng
    // (dot 17.1 tach khoi tab Tra hang). Dot 18: nut 'Lọc' rieng
    // (admin-log-filter-btn) DA BO — khoang ngay Tu/Den ap dung khi bam nut
    // icon Tim o toolbar Nhat ky (logs-search-btn). Export nhat ky DA BO HAN.
    // Tieu de muc la <h1> o pagehead cua tab (khong con <h2> rieng trong muc).
    public static final String ADMIN_AUDIT_LOG_HEADING =
            "xpath=//h1[contains(text(),'Nhật ký hệ thống')]";
    public static final String ADMIN_LOG_DATE_FROM = "css=[data-testid='admin-log-date-from']";
    public static final String ADMIN_LOG_DATE_TO = "css=[data-testid='admin-log-date-to']";
    public static final String ADMIN_LOG_SEARCH_BTN = "css=[data-testid='logs-search-btn']";
    public static final String ADMIN_LOG_RESET_BTN = "css=[data-testid='logs-reset-btn']";

    // ---------- Dot redesign form admin + upload anh (drawer/modal form moi) ----------
    // Hop dong testid FE phai dung (xem TEST-MAPPING.md muc "Dot redesign form
    // admin + upload anh"). Form san pham gio la DRAWER (van testid
    // admin-product-form-modal); dropdown danh muc la custom (nut trigger, KHONG
    // con <select>) nen option khong co testid rieng — dinh vi theo text hien thi
    // trong pham vi modal (xem AdminPageObject.selectProductCategory).
    // Toast moi: admin-toast (thay the .sa-notice o cac luong form moi, dot 18
    // dung cho MOI thao tac); hang moi tao duoc highlight 2 giay bang class
    // sa-row-highlight (khong phai testid).
    public static final String ADMIN_TOAST = "css=[data-testid='admin-toast']";
    public static final String ADMIN_TOAST_TITLE = "css=[data-testid='admin-toast-title']";
    public static final String ADMIN_TOAST_DESC = "css=[data-testid='admin-toast-desc']";
    public static final String ADMIN_TOAST_CLOSE = "css=[data-testid='admin-toast-close']";
    // Tieu de form (FormShell {testid}-title; form SP trang full page dung
    // admin-product-form-title tren <h2> cua header sticky).
    public static final String ADMIN_PRODUCT_FORM_TITLE = "css=[data-testid='admin-product-form-title']";
    public static final String ADMIN_CATEGORY_FORM_TITLE = "css=[data-testid='admin-category-form-modal-title']";
    // (ADMIN_PROMO_FORM_TITLE da bo dot 21 — tieu de trang tao/sua KM la <h2>
    // khong testid trong admin-promo-create-page / admin-promo-edit-page.)
    public static final String ADMIN_CUSTOMER_FORM_TITLE = "css=[data-testid='admin-customer-form-page-title']";
    public static final String ADMIN_PRODUCT_STATUS_BADGE = "css=[data-testid='admin-product-status-badge']";
    public static final String ADMIN_CATEGORY_STATUS_BADGE = "css=[data-testid='admin-category-status-badge']";
    public static final String ADMIN_PROMO_STATUS_BADGE = "css=[data-testid='admin-promo-status-badge']";

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
            "xpath=//*[@data-testid='admin-product-form']//input[contains(@placeholder,'Tìm')]";
    public static final String ADMIN_PRODUCT_CATEGORY_OPTIONS = "xpath=//*[@data-testid='admin-product-form']//*[@role='option']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION =
            "xpath=//*[@data-testid='admin-product-form']//*[@role='option' and normalize-space(text())='%s']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION_LI =
            "xpath=//*[@data-testid='admin-product-form']//li[normalize-space(.)='%s']";
    public static final String DYNAMIC_PRODUCT_CATEGORY_OPTION_BTN =
            "xpath=//*[@data-testid='admin-product-form']//button[normalize-space(text())='%s']";

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
    // Anh trong form XEM san pham (dung de khang dinh SP vua tao co anh that).
    // Dot 18: anh hien qua ViewImageGallery trong form xem — 1 anh thi
    // testid admin-product-image-thumb, nhieu anh thi -thumb-{i} (anh chinh
    // luon o vi tri 0, co nhan 'Ảnh chính').
    public static final String ADMIN_PRODUCT_DETAIL_IMG = "css=[data-testid='admin-product-image-thumb'] img";
    public static final String DYNAMIC_PRODUCT_VIEW_IMAGE_IMG =
            "css=[data-testid='admin-product-image-thumb-%s'] img";
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
    // Them moi; chi co o tim + loc sao + Tim / Dat lai (PageToolbar prefix
    // 'reviews', KHONG co nut Lam moi, KHONG co Export — dot 18 bo han Export).
    // Bang chi hien khi co dong (reviews.length > 0); rong thi hien EmptyState
    // 'Chưa có đánh giá nào' (khong dau cham) thay cho bang. Phan trang dung
    // chung (admin-reviews-pagination, 'Hiển thị {from}–{to} / {total} kết quả'
    // + 'Trang X / Y', KHONG nut so trang).
    public static final String ADMIN_NAV_REVIEWS = "css=[data-testid='admin-nav-reviews']";
    public static final String ADMIN_REVIEWS_SECTION = "css=[data-testid='admin-reviews-section']";
    public static final String ADMIN_REVIEW_SEARCH = "css=[data-testid='reviews-search-input']";
    public static final String ADMIN_REVIEW_RATING_FILTER = "css=[data-testid='admin-review-rating-filter']";
    public static final String ADMIN_REVIEWS_SEARCH_BTN = "css=[data-testid='reviews-search-btn']";
    public static final String ADMIN_REVIEWS_RESET_BTN = "css=[data-testid='reviews-reset-btn']";
    // Bang danh gia (gioi han trong section de khong lan sang bang muc khac).
    // Dot 17: bang them cot STT dau tien nen chi so td doi +1 so voi dot 16
    // (td[1]=STT, td[2]=San pham, td[3]=Khach hang, td[4]=Sao, td[5]=Noi dung,
    // td[6]=Ngay, td[7]=Thao tac).
    public static final String ADMIN_REVIEW_TABLE_ROWS =
            "xpath=//*[@data-testid='admin-reviews-section']//tbody/tr";
    public static final String ADMIN_REVIEW_TABLE_HEADERS =
            "xpath=//*[@data-testid='admin-reviews-section']//thead/tr/th";
    public static final String ADMIN_REVIEW_FIRST_ROW =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]";
    // O Sao: <span aria-label="{n} trên 5 sao"><span class="sa-stars">5 icon
    // Star Lucide (day fill #f59e0b, rong fill none)</span> (n/5)</span>
    // (dot 18 wave D: khong con chuoi '★★★★☆ (4/5)' — doc rating qua
    // aria-label hoac chuoi '(n/5)', dem sao day qua svg[fill='#f59e0b']).
    public static final String ADMIN_REVIEW_FIRST_ROW_PRODUCT =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[2]";
    public static final String ADMIN_REVIEW_FIRST_ROW_RATING =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[4]//span";
    // Icon sao Lucide o 1 dong (rowIndex 1-based): tat ca / chi sao day.
    public static final String DYNAMIC_REVIEW_ROW_STAR_ICONS =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[4]//span[contains(@class,'sa-stars')]//svg";
    public static final String DYNAMIC_REVIEW_ROW_FILLED_STAR_ICONS =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[4]//span[contains(@class,'sa-stars')]//svg[@fill='#f59e0b']";
    public static final String DYNAMIC_REVIEW_ROW_RATING_LABEL =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[4]//span[@aria-label]";
    // Icon sao Lucide trong TRANG chi tiet danh gia full page dot 21.
    public static final String ADMIN_REVIEW_DETAIL_STAR_ICONS =
            "xpath=//*[@data-testid='admin-review-detail-page']//span[contains(@class,'sa-stars')]//svg";
    public static final String ADMIN_REVIEW_DETAIL_FILLED_STAR_ICONS =
            "xpath=//*[@data-testid='admin-review-detail-page']//span[contains(@class,'sa-stars')]//svg[@fill='#f59e0b']";
    // Noi dung trong bang bi cat toi da 80 ky tu + "..." (shortComment); trong
    // trang chi tiet hien day du.
    public static final String ADMIN_REVIEW_FIRST_ROW_COMMENT =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]/td[5]";
    public static final String ADMIN_REVIEW_ROW_RATING_SPANS =
            "xpath=//*[@data-testid='admin-reviews-section']//tbody/tr/td[4]//span";
    // Nut mat / nut xoa tren tung dong (aria-label chua ten khach hang).
    public static final String ADMIN_REVIEW_FIRST_ROW_DETAIL_BTN =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[1]//button[starts-with(@aria-label,'Xem đánh giá của')]";
    public static final String ADMIN_REVIEW_LAST_ROW_DELETE_BTN =
            "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[last()]//button[starts-with(@aria-label,'Xóa đánh giá của')]";
    // Phan trang (chi hien khi co dong): text 'Hiển thị {from}–{to} / {total}
    // kết quả' + range 'Trang X / Y' (admin-reviews-pagination-range); KHONG
    // nut so trang.
    public static final String ADMIN_REVIEWS_PAGINATION = "css=[data-testid='admin-reviews-pagination']";
    public static final String ADMIN_REVIEWS_PAGINATION_PREV = "css=[data-testid='admin-reviews-pagination-prev']";
    public static final String ADMIN_REVIEWS_PAGINATION_NEXT = "css=[data-testid='admin-reviews-pagination-next']";
    public static final String ADMIN_REVIEWS_PAGINATION_RANGE = "css=[data-testid='admin-reviews-pagination-range']";
    // Trang chi tiet danh gia full page dot 21 (FormShell dot 18 da bo):
    // KvRow label 'Nội dung' hien day du, khong cat; nut [Xóa đánh giá]
    // (admin-review-delete-btn) o header + nut [Quay lại] ve danh sach.
    // (ADMIN_REVIEW_DETAIL_MODAL da bo — dung ADMIN_REVIEW_DETAIL_PAGE.)
    public static final String ADMIN_REVIEW_DETAIL_MODAL = "css=[data-testid='admin-review-detail-modal']";
    public static final String ADMIN_REVIEW_DETAIL_COMMENT =
            "xpath=//*[@data-testid='admin-review-detail-page']//div[@class='sa-kv'][span[text()='Nội dung']]/div";
    public static final String ADMIN_REVIEW_DETAIL_BACK_BTN = "css=[data-testid='admin-review-detail-back-btn']";
    public static final String ADMIN_REVIEW_DELETE_BTN = "css=[data-testid='admin-review-delete-btn']";
    // Trang thai rong (khong co bang): EmptyState 'Chưa có đánh giá nào' (khong dau cham).
    public static final String ADMIN_EMPTY_REVIEWS_MSG =
            "xpath=//p[contains(text(),'Chưa có đánh giá nào')]";

    // ---------- Dot 17: cot Ma/STT + Switch + menu tai khoan (GIU) + Dot 18 wave D: Drawer rong + nav phang + ConfirmDialog ----------
    // Dot 18 wave D: form them/sua/xem CHI CON Drawer rong qua FormShell
    // (SP 760px, DM/KM/KH 680px, class fs-drawer, CSS var --fs-width);
    // container GIU testid cu (admin-product-form-modal /
    // admin-category-form-modal / admin-promo-form-modal /
    // admin-customer-form-page). Khi form mo, bang danh sach VAN O LAI trong
    // DOM (overlay). Nut 'Quay lai' (admin-form-back-btn) DA BO; form dirty
    // thi hien ConfirmDialog 'Thoát mà chưa lưu?' [Ở lại] + [Thoát]. Footer
    // form xem CHI [Đóng] + [Chỉnh sửa]; footer create [Hủy] + [Lưu]; footer
    // edit [Hủy] + [Lưu thay đổi] (nut Luu disable toi khi dirty).
    public static final String ADMIN_CUSTOMER_FORM_PAGE = "css=[data-testid='admin-customer-form-page']";
    public static final String ADMIN_CUSTOMER_FORM = "css=[data-testid='admin-customer-form']";
    public static final String ADMIN_CUSTOMER_CANCEL_BTN = "css=[data-testid='admin-customer-cancel-btn']";
    public static final String ADMIN_CUSTOMER_SAVE_BTN = "css=[data-testid='admin-customer-save-btn']";
    public static final String ADMIN_PROMO_CANCEL_BTN = "css=[data-testid='admin-promo-cancel-btn']";
    // Nut 'Đóng' o footer form XEM khach hang (admin-customer-cancel-btn, text
    // 'Đóng' o che do xem) — dung DYNAMIC_MODAL_FOOTER_CLOSE_BTN voi testid
    // 'admin-customer-form-page' de bam.
    // O Ma trong form (chi nhap khi tao; khi sua bi disable — SP/DM/VC co
    // disabled={isEdit}, KH luon disabled). Loi trung ma hien duoi o Ma:
    // admin-product-code-error / admin-category-code-error / admin-promo-code-error.
    public static final String ADMIN_PRODUCT_CODE_INPUT = "css=[data-testid='admin-product-code-input']";
    public static final String ADMIN_CATEGORY_CODE_INPUT = "css=[data-testid='admin-category-code-input']";
    public static final String ADMIN_CUSTOMER_CODE_INPUT = "css=[data-testid='admin-customer-code-input']";
    public static final String ADMIN_CUSTOMER_EMAIL_INPUT = "css=[data-testid='admin-customer-email-input']";
    public static final String ADMIN_CUSTOMER_NAME_INPUT = "css=[data-testid='admin-customer-name-input']";
    public static final String ADMIN_CUSTOMER_PHONE_INPUT = "css=[data-testid='admin-customer-phone-input']";
    public static final String ADMIN_CUSTOMER_ADDRESS_INPUT = "css=[data-testid='admin-customer-address-input']";
    public static final String DYNAMIC_CATEGORY_FIELD_ERROR = "css=[data-testid='admin-category-%s-error']";
    public static final String DYNAMIC_PROMO_FIELD_ERROR = "css=[data-testid='admin-promo-%s-error']";
    // Nut sua thong tin khach tren tung dong (aria-label chua ten KH).
    public static final String DYNAMIC_CUSTOMER_EDIT_BTN = "xpath=//button[@aria-label='Sửa khách hàng %s']";

    // Cot Ma tren bang: testid LAP LAI tren moi dong (admin-product-code /
    // admin-category-code / admin-customer-code) nen doc theo dong qua xpath
    // DYNAMIC_ROW_CODE_CELL (rowText = ten SP/DM hoac email KH, codeTestid =
    // testid cua o Ma). Cot STT la td[1] cua dong (lien tuc theo trang).
    public static final String DYNAMIC_ROW_CODE_CELL =
            "xpath=//tr[td[contains(.,'%s')]]//td[@data-testid='%s']";
    public static final String ADMIN_FIRST_ROW_STT =
            "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[1]/td[1]";

    // Switch trang thai trong bang (StatusSwitch: <button role='switch'
    // aria-checked='true|false' data-testid='admin-<loai>-status-switch-{id}>',
    // CHI co num gat, KHONG co nhan chu — doc trang thai qua aria-checked).
    // Vi id chi co trong testid cua switch nen dinh vi switch theo dong qua
    // DYNAMIC_ROW_STATUS_SWITCH (rowText = ten SP/DM hoac ma voucher).
    public static final String DYNAMIC_ROW_STATUS_SWITCH =
            "xpath=//tr[td[contains(.,'%s')]]//button[@role='switch']";
    public static final String ADMIN_TABLE_ROW_SWITCHES =
            "xpath=//div[contains(@class,'sa-table-wrap')]//tbody/tr//button[@role='switch']";
    // Dropdown loc Trang thai o toolbar (testid 'admin-status-filter' LAP LAI
    // o cac muc nhung chi 1 muc hien tai 1 thoi diem): SP/DM value ''/'true'/
    // 'false'; voucher ''/'Draft'/'Active'/'Disabled'/'Expired' (ap dung ngay
    // khi doi select, KHONG con nut 'Lọc'); KH ''/'active'/'locked'.
    public static final String ADMIN_STATUS_FILTER = "css=[data-testid='admin-status-filter']";

    // Popup CHAN xoa khi dong dang Hoat dong (dot 18): ConfirmDialog tieu de
    // 'Không thể xóa sản phẩm' / 'Không thể xóa danh mục' (KHONG dau 'xoá'),
    // noi dung 'Không thể xóa ... đang hoạt động. Vui lòng chuyển trạng thái
    // sang Ngừng hoạt động trước.', CHI 2 nut [Hủy] + [Chuyển sang Ngừng hoạt
    // động]; bam chuyen thi mo tiep modal xac nhan doi trang thai (SP dot 21:
    // modal CHUNG 'Xác nhận thay đổi trạng thái?' [Hủy] + [Xác nhận]; DM van
    // co the la 'Ẩn danh mục?' [Hủy] + [Ẩn ...]) roi moi PUT isActive=false.
    // Khong con modal rieng admin-delete-blocked-modal — dung CONFIRM_DIALOG_* o tren.

    // ---------- Dot 18 wave D: menu PHANG + badge dem ----------
    // Nhom: sales (Don hang, Tra hang, Khuyen mai), products (San pham, Danh
    // muc, Danh gia), customers (Khach hang, Hoi vien), system (Nhat ky he
    // thong). Tieu de nhom la <div> (KHONG phai <button>, KHONG aria-expanded,
    // KHONG chevron, KHONG bam duoc), ten dang tieu de ('Bán hàng',
    // 'Quản lý sản phẩm', 'Quản lý khách hàng', 'Hệ thống'); nhom cach 20px,
    // muc cach 4px; KHONG con localStorage 'dafl_admin_nav_groups', KHONG con
    // badge tong o tieu de nhom (admin-nav-group-*-badge DA XOA). Badge dem
    // nen cam nhat 12px, toi da '99+', an khi 0: Don = cho xac nhan
    // (pendingOrders tu GET /api/admin/dashboard), Tra = cho duyet (dem
    // Pending trong list tra hang), Danh gia = moi (tao trong 7 ngay qua;
    // Evaluation KHONG co co da xu ly).
    public static final String DYNAMIC_ADMIN_NAV_GROUP = "css=[data-testid='admin-nav-group-%s']";
    public static final String DYNAMIC_ADMIN_NAV_BADGE = "css=[data-testid='admin-nav-%s-badge']";
    public static final String ADMIN_NAV_ORDERS_BADGE = "css=[data-testid='admin-nav-orders-badge']";
    public static final String ADMIN_NAV_RETURNS_BADGE = "css=[data-testid='admin-nav-returns-badge']";
    public static final String ADMIN_NAV_REVIEWS_BADGE = "css=[data-testid='admin-nav-reviews-badge']";

    // ---------- Dot 18 wave D: doi mat khau dang FormShell Drawer + toolbar Hoi vien ----------
    // Drawer doi mat khau (AdminProfilePage): FormShell testid
    // admin-change-password-modal, form admin-change-password-form, footer CHI
    // [Hủy] (admin-change-password-cancel-btn) + [Đổi mật khẩu]
    // (admin-change-password-save-btn); dong bang nut X
    // (admin-change-password-modal-close-btn, aria-label='Đóng').
    public static final String ADMIN_CHANGE_PASSWORD_MODAL = "css=[data-testid='admin-change-password-modal']";
    public static final String ADMIN_CHANGE_PASSWORD_MODAL_CLOSE_BTN =
            "css=[data-testid='admin-change-password-modal-close-btn']";
    public static final String ADMIN_CHANGE_PASSWORD_MODAL_TITLE =
            "css=[data-testid='admin-change-password-modal-title']";
    // Toolbar rieng cua muc Hoi vien (khong dung PageToolbar): nut Dat lai /
    // Lam moi co testid rieng.
    public static final String ADMIN_MEMBERS_RESET_BTN = "css=[data-testid='admin-members-reset-btn']";
    public static final String ADMIN_MEMBERS_REFRESH_BTN = "css=[data-testid='admin-members-refresh-btn']";

    // Menu tai khoan o topbar (chip avatar + ten) + trang Ho so.
    public static final String ADMIN_USER_MENU_BTN = "css=[data-testid='admin-user-menu-btn']";
    public static final String ADMIN_USER_MENU = "css=[data-testid='admin-user-menu']";
    public static final String ADMIN_USER_MENU_PROFILE = "css=[data-testid='admin-user-menu-profile']";
    public static final String ADMIN_USER_MENU_LOGOUT = "css=[data-testid='admin-user-menu-logout']";
    // Dot 21: menu topbar chi 2 muc + divider truoc "Dang xuat".
    public static final String ADMIN_USER_MENU_ITEMS =
            "xpath=//*[@data-testid='admin-user-menu']//*[@role='menuitem']";
    public static final String ADMIN_USER_MENU_SEPARATOR =
            "css=[data-testid='admin-user-menu'] .sa-topbar-user-menu-sep";
    public static final String ADMIN_PROFILE_INFO_GRID = "css=[data-testid='admin-profile-info-grid']";
    public static final String ADMIN_PROFILE_FORM_GRID = "css=[data-testid='admin-profile-form-grid']";
    public static final String ADMIN_TOPBAR_USER_NAME = "xpath=//span[contains(@class,'sa-topbar-user-name')]";
    public static final String ADMIN_PROFILE_PAGE = "css=[data-testid='admin-profile-page']";
    public static final String ADMIN_PROFILE_HEADER_NAME =
            "xpath=//*[@data-testid='admin-profile-page']//h2";
    public static final String ADMIN_PROFILE_EDIT_BTN = "css=[data-testid='admin-profile-edit-btn']";
    public static final String ADMIN_PROFILE_FORM = "css=[data-testid='admin-profile-form']";
    public static final String ADMIN_PROFILE_NAME_INPUT = "css=[data-testid='admin-profile-name-input']";
    public static final String ADMIN_PROFILE_EMAIL_INPUT = "css=[data-testid='admin-profile-email-input']";
    public static final String ADMIN_PROFILE_PHONE_INPUT = "css=[data-testid='admin-profile-phone-input']";
    public static final String ADMIN_PROFILE_ADDRESS_INPUT = "css=[data-testid='admin-profile-address-input']";
    public static final String ADMIN_PROFILE_SAVE_BTN = "css=[data-testid='admin-profile-save-btn']";
    public static final String ADMIN_PROFILE_CANCEL_BTN = "css=[data-testid='admin-profile-cancel-btn']";
    public static final String DYNAMIC_PROFILE_FIELD_ERROR = "css=[data-testid='admin-profile-%s-error']";
    public static final String ADMIN_CHANGE_PASSWORD_BTN = "css=[data-testid='admin-change-password-btn']";
    public static final String ADMIN_CHANGE_PASSWORD_FORM = "css=[data-testid='admin-change-password-form']";
    public static final String ADMIN_CHANGE_PASSWORD_CURRENT_INPUT =
            "css=[data-testid='admin-change-password-current-input']";
    public static final String ADMIN_CHANGE_PASSWORD_NEW_INPUT =
            "css=[data-testid='admin-change-password-new-input']";
    public static final String ADMIN_CHANGE_PASSWORD_CONFIRM_INPUT =
            "css=[data-testid='admin-change-password-confirm-input']";
    public static final String ADMIN_CHANGE_PASSWORD_SAVE_BTN =
            "css=[data-testid='admin-change-password-save-btn']";
    public static final String ADMIN_CHANGE_PASSWORD_CANCEL_BTN =
            "css=[data-testid='admin-change-password-cancel-btn']";
    public static final String DYNAMIC_CHANGE_PASSWORD_FIELD_ERROR =
            "css=[data-testid='admin-change-password-%s-error']";

    // ----- Dot 19: locator moi tu file test case manual admin v18 -----
    public static final String ADMIN_CATEGORY_FORM_ERROR = "xpath=//*[@data-testid='admin-category-form']//p[@role='alert']";
    public static final String ADMIN_CATEGORY_IMAGE_ERROR = "css=[data-testid='admin-category-image-error']";
    public static final String ADMIN_CATEGORY_IMAGE_LINK_TOGGLE = "css=[data-testid='admin-category-image-link-toggle']";
    public static final String ADMIN_CATEGORY_IMAGE_RETRY_BTN = "xpath=//*[@data-testid='admin-category-image-thumb']//button[contains(@class,'sa-thumb-retry-btn')]";
    public static final String ADMIN_CATEGORY_IMAGE_THUMB_ERROR_TEXT = "xpath=//*[@data-testid='admin-category-image-thumb']//*[contains(@class,'sa-thumb-err-text')]";
    public static final String ADMIN_CATEGORY_IMAGE_URL_ADD_BTN = "xpath=//*[@data-testid='admin-category-image-url-input']/following-sibling::button";
    public static final String ADMIN_CATEGORY_IMAGE_URL_INPUT = "css=[data-testid='admin-category-image-url-input']";
    public static final String ADMIN_CUSTOMER_TIER_FILTER = "xpath=//select[@aria-label='Lọc hạng']";
    public static final String ADMIN_CUSTOMER_TIER_FILTER_OPTIONS =
                "css=select[aria-label='Lọc hạng'] option";
    public static final String ADMIN_DASHBOARD_ACTIVE_PROMOS_CARD =
                "css=[data-testid='admin-dashboard-active-promos-card']";
    public static final String ADMIN_DASHBOARD_CUSTOM_APPLY =
                "css=[data-testid='admin-dashboard-custom-apply']";
    public static final String ADMIN_DASHBOARD_CUSTOM_ERROR =
                "css=[data-testid='admin-dashboard-custom-error']";
    public static final String ADMIN_DASHBOARD_CUSTOM_FROM =
                "css=[data-testid='admin-dashboard-custom-from']";
    public static final String ADMIN_DASHBOARD_CUSTOM_PICKER =
                "css=[data-testid='admin-dashboard-custom-picker']";
    public static final String ADMIN_DASHBOARD_CUSTOM_TO =
                "css=[data-testid='admin-dashboard-custom-to']";
    public static final String ADMIN_DASHBOARD_LOW_STOCK_CARD =
                "css=[data-testid='admin-dashboard-low-stock-card']";
    public static final String ADMIN_DASHBOARD_LOW_STOCK_ROWS =
                "xpath=//*[@data-testid='admin-dashboard-low-stock-card']//ul/li";
    public static final String ADMIN_DASHBOARD_PROMO_FIRST_CODE =
                "xpath=(//*[@data-testid='admin-dashboard-active-promos-card']//button[contains(@class,'db-promo-row')])[1]//span[contains(@class,'db-promo-code')]";
    public static final String ADMIN_DASHBOARD_PROMO_FIRST_ROW =
                "xpath=(//*[@data-testid='admin-dashboard-active-promos-card']//button[contains(@class,'db-promo-row')])[1]";
    public static final String ADMIN_DASHBOARD_PROMO_ROWS =
                "xpath=//*[@data-testid='admin-dashboard-active-promos-card']//button[contains(@class,'db-promo-row')]";
    public static final String ADMIN_DASHBOARD_RECENT_ORDERS_ALL =
                "css=[data-testid='admin-dashboard-recent-orders-all']";
    public static final String ADMIN_DASHBOARD_RECENT_ORDERS_CARD =
                "css=[data-testid='admin-dashboard-recent-orders-card']";
    public static final String ADMIN_DASHBOARD_RECENT_ORDER_FIRST_CODE =
                "xpath=(//*[@data-testid='admin-dashboard-recent-orders-card']//tbody/tr)[1]/td[1]";
    public static final String ADMIN_DASHBOARD_RECENT_ORDER_FIRST_ROW =
                "xpath=(//*[@data-testid='admin-dashboard-recent-orders-card']//tbody/tr)[1]";
    public static final String ADMIN_DASHBOARD_RECENT_ORDER_ROWS =
                "xpath=//*[@data-testid='admin-dashboard-recent-orders-card']//tbody/tr";
    public static final String ADMIN_DASHBOARD_REFRESH_BTN =
                "css=[data-testid='admin-dashboard-refresh-btn']";
    public static final String ADMIN_DASHBOARD_STATUS_DONUT =
                "css=[data-testid='admin-dashboard-status-donut']";
    public static final String ADMIN_DASHBOARD_TIER_MIX_CARD =
                "css=[data-testid='admin-dashboard-tier-mix-card']";
    public static final String ADMIN_DASHBOARD_TIER_MIX_EMPTY =
                "css=[data-testid='admin-dashboard-tier-mix-empty']";
    public static final String ADMIN_DASHBOARD_UPDATED_AT =
                "css=[data-testid='admin-dashboard-updated-at']";
    public static final String ADMIN_EMPTY_STATE = "css=[data-testid='empty-state']";
    public static final String ADMIN_FIRST_ROW_ACTION_BUTTONS = "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[1]/td[contains(@class,'sa-actions')]//button";
    public static final String ADMIN_FORBIDDEN_HOME_BTN =
                "xpath=//*[@data-testid='admin-forbidden']//button[contains(text(),'Về trang chủ')]";
    public static final String ADMIN_FORBIDDEN_SWITCH_ACCOUNT_BTN =
                "xpath=//*[@data-testid='admin-forbidden']//button[contains(text(),'Đăng nhập tài khoản khác')]";
    public static final String ADMIN_FORM_OVERLAY = "css=.fs-overlay";
    public static final String ADMIN_LOG_SEARCH_INPUT = "css=[data-testid='logs-search-input']";
    public static final String ADMIN_ORDER_CARRIER_INPUT = "css=[data-testid='admin-order-carrier-input']";
    public static final String ADMIN_ORDER_DETAIL_ERROR =
                "xpath=//*[@data-testid='admin-order-detail']//p[contains(@class,'sa-error')]";
    public static final String ADMIN_ORDER_NOTE_INPUT =
                "xpath=//*[@data-testid='admin-order-detail']//input[@placeholder='Ghi chú (không bắt buộc)']";
    public static final String ADMIN_ORDER_STATUS_FILTER_OPTIONS =
                "xpath=//*[@data-testid='admin-order-status-filter']/option";
    public static final String ADMIN_ORDER_TARGET_STATUS_OPTIONS =
                "xpath=//*[@data-testid='admin-order-status-select']/option";
    public static final String ADMIN_ORDER_TRACKING_INPUT = "css=[data-testid='admin-order-tracking-input']";
    public static final String ADMIN_PAGEHEAD = "css=.sa-pagehead";
    public static final String ADMIN_PRODUCT_CATEGORY_FILTER = "css=select[aria-label='Lọc danh mục']";
    public static final String ADMIN_PRODUCT_FORM_ERROR = "xpath=//*[@data-testid='admin-product-form']//p[@role='alert']";
    public static final String ADMIN_PRODUCT_IMAGE_URL_ADD_BTN = "xpath=//*[@data-testid='admin-product-image-url-input']/following-sibling::button";
    public static final String ADMIN_PRODUCT_MONEY_SUFFIX = "xpath=//*[@data-testid='admin-product-form']//span[contains(@class,'sa-money-suffix')]";
    public static final String ADMIN_PROFILE_BACK_BTN = "css=[data-testid='admin-profile-back-btn']";
    public static final String ADMIN_PROMO_DISCOUNT_TYPE_FREESHIP = "css=[data-testid='admin-promo-discount-type-freeship']";
    public static final String ADMIN_PROMO_IMAGE_UPLOAD_CHECK =
                "xpath=//*[@data-testid='admin-promo-form']//*[contains(@data-testid,'image')]";
    public static final String ADMIN_PROMO_MAX_USES_PER_USER_INPUT = "css=[data-testid='admin-promo-max-uses-per-user-input']";
    public static final String ADMIN_PROMO_MIN_TIER_SELECT = "css=#admin-promo-min-tier";
    public static final String ADMIN_PROMO_SCOPE_CATEGORY_SELECT = "css=#admin-promo-scope-category";
    public static final String ADMIN_PROMO_SCOPE_PRODUCTS_INPUT = "css=#admin-promo-scope-products";
    public static final String ADMIN_PROMO_SCOPE_SELECT = "css=#admin-promo-scope";
    // ----- Trang chi tiet / Lich su su dung KM full page (dot 21) -----
    // Trang Chi tiet: 4 card (general/conditions/time/stats) + nut [Chỉnh sửa]
    // (chi Draft) / [Quay lại] o header; ma + tong luot da dung co testid rieng.
    public static final String ADMIN_PROMO_DETAIL_GENERAL = "css=[data-testid='admin-promo-detail-general']";
    public static final String ADMIN_PROMO_DETAIL_CONDITIONS = "css=[data-testid='admin-promo-detail-conditions']";
    public static final String ADMIN_PROMO_DETAIL_TIME = "css=[data-testid='admin-promo-detail-time']";
    public static final String ADMIN_PROMO_DETAIL_STATS = "css=[data-testid='admin-promo-detail-stats']";
    public static final String ADMIN_PROMO_DETAIL_CODE = "css=[data-testid='admin-promo-detail-code']";
    public static final String ADMIN_PROMO_DETAIL_USED_COUNT = "css=[data-testid='admin-promo-detail-used-count']";
    // Trang Lich su su dung: khoi tong quan (ma/mo ta/loai/gia tri/tong luot) +
    // o tim theo ma don/email + loc ngay (client-side, chi tren trang hien tai)
    // + bang 6 cot: Ma don / Khach hang / Gia tri don hang / So tien giam /
    // Thoi gian su dung / Trang thai (2 cot Gia tri don hang + Trang thai hien
    // "—" vi DTO khong co field — KHONG duoc assert so lieu o 2 cot nay).
    public static final String ADMIN_PROMO_USAGES_PAGE = "css=[data-testid='admin-promo-usages-page']";
    public static final String ADMIN_PROMO_USAGES_OVERVIEW = "css=[data-testid='admin-promo-usages-overview']";
    public static final String ADMIN_PROMO_USAGES_SEARCH = "css=[data-testid='admin-promo-usages-search-input']";
    public static final String ADMIN_PROMO_USAGES_DATE_FROM = "css=[data-testid='admin-promo-usages-date-from']";
    public static final String ADMIN_PROMO_USAGES_DATE_TO = "css=[data-testid='admin-promo-usages-date-to']";
    public static final String ADMIN_PROMO_USAGES_TOTAL = "css=[data-testid='admin-promo-usages-total']";
    public static final String ADMIN_PROMO_USAGES_BACK_BTN = "css=[data-testid='admin-promo-usages-back-btn']";
    public static final String ADMIN_PROMO_USAGES_HEADERS =
            "xpath=//*[@data-testid='admin-promo-usages-page']//table//thead/tr/th";
    public static final String ADMIN_PROMO_USAGES_ROWS =
            "xpath=//*[@data-testid='admin-promo-usages-page']//table//tbody/tr";
    // Ghi chu FreeShip (thay o Gia tri giam) + dong trang thai o trang tao/sua.
    public static final String ADMIN_PROMO_FREESHIP_NOTE = "css=[data-testid='admin-promo-value-freeship-note']";
    public static final String ADMIN_PROMO_STATUS_TEXT = "css=[data-testid='admin-promo-status-text']";
    public static final String ADMIN_PROMO_START_INPUT = "css=[data-testid='admin-promo-start-input']";
    public static final String ADMIN_REFUND_RECORD_CANCEL_BTN =
                "xpath=//*[@data-testid='admin-refund-record-modal']//button[text()='Hủy']";
    public static final String ADMIN_REFUND_RECORD_ERROR =
                "xpath=//*[@data-testid='admin-refund-record-modal']//p[contains(@class,'sa-error')]";
    public static final String ADMIN_RETURNS_NOT_FOUND_MSG =
                "xpath=//p[contains(text(),'Không tìm thấy yêu cầu trả hàng')]";
    public static final String ADMIN_RETURNS_SEARCH = "css=[data-testid='returns-search-input']";
    public static final String ADMIN_RETURNS_STATUS_FILTER =
                "xpath=//select[@aria-label='Lọc trạng thái trả hàng']";
    public static final String ADMIN_RETURNS_STATUS_FILTER_OPTIONS =
                "xpath=//select[@aria-label='Lọc trạng thái trả hàng']/option";
    public static final String ADMIN_RETURN_APPROVE_BTN = "css=[data-testid='admin-return-approve-btn']";
    public static final String ADMIN_RETURN_REJECT_BTN = "css=[data-testid='admin-return-reject-btn']";
    public static final String ADMIN_STATUS_FILTER_OPTIONS =
                "xpath=//*[@data-testid='admin-status-filter']/option";
    public static final String ADMIN_TABLE_FIRST_ROW_CELLS =
                "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[1]/td";
    public static final String ADMIN_TABLE_HEADERS = "xpath=//div[contains(@class,'sa-table-wrap')]//thead/tr/th";
    public static final String DYNAMIC_DASHBOARD_ACTION_COUNT =
                "css=[data-testid='admin-dashboard-action-%s-count']";
    public static final String DYNAMIC_DASHBOARD_ACTION_VIEW =
                "css=[data-testid='admin-dashboard-action-%s-view']";
    public static final String DYNAMIC_DASHBOARD_DONUT_LEGEND =
                "css=[data-testid='admin-dashboard-donut-%s']";
    public static final String DYNAMIC_DASHBOARD_KPI = "css=[data-testid='admin-dashboard-kpi-%s']";
    public static final String DYNAMIC_DASHBOARD_KPI_COMPARE =
                "css=[data-testid='admin-dashboard-kpi-%s'] .db-compare";
    public static final String DYNAMIC_DASHBOARD_KPI_ERROR =
                "css=[data-testid='admin-dashboard-kpi-%s-error']";
    public static final String DYNAMIC_DASHBOARD_KPI_SUB =
                "css=[data-testid='admin-dashboard-kpi-%s'] .db-kpi-sub";
    public static final String DYNAMIC_DASHBOARD_KPI_VALUE =
                "css=[data-testid='admin-dashboard-kpi-%s'] .db-kpi-value";
    public static final String DYNAMIC_DASHBOARD_RANGE_BTN =
                "css=[data-testid='admin-dashboard-range-%s']";
    public static final String DYNAMIC_DASHBOARD_TIER_ROW =
                "css=[data-testid='admin-dashboard-tier-%s']";
    public static final String DYNAMIC_FORM_OVERLAY = "xpath=//*[@data-testid='%s']/ancestor::div[contains(@class,'fs-overlay')]";
    public static final String DYNAMIC_PRODUCT_IMAGE_RETRY_BTN = "xpath=//*[@data-testid='admin-product-image-thumb-%s']//button[contains(@class,'sa-thumb-retry-btn')]";
    public static final String DYNAMIC_PRODUCT_IMAGE_THUMB_ERROR_TEXT = "xpath=//*[@data-testid='admin-product-image-thumb-%s']//*[contains(@class,'sa-thumb-err-text')]";
    // (Dot 21: cot "Đánh giá" DA BO khoi bang SP — khong con locator cot
    // danh gia td[7] / icon sao o bang; rating chi doc o trang chi tiet
    // qua ADMIN_PRODUCT_DETAIL_RATING.)
    // Dot 21: nut "Lượt dùng" dang text DA BO — icon History (aria-label
    // 'Xem lượt sử dụng khuyến mãi {mã}') mo TRANG /promotions/:code/usages.
    public static final String DYNAMIC_PROMO_USAGE_BTN =
                "xpath=//button[@aria-label='Xem lượt sử dụng khuyến mãi %s']";
    // Nut mat (Eye) o dong voucher (aria-label 'Xem chi tiết khuyến mãi {mã}').
    public static final String DYNAMIC_PROMO_DETAIL_BTN =
                "xpath=//button[@aria-label='Xem chi tiết khuyến mãi %s']";
    public static final String DYNAMIC_TABLE_ROW_CELL = "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[%s]/td[%s]";
    public static final String DYNAMIC_TABLE_ROW_CELL_STRONG = "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[%s]/td[%s]//strong";
    public static final String DYNAMIC_TABLE_ROW_SWITCH = "xpath=(//div[contains(@class,'sa-table-wrap')]//tbody/tr)[%s]//button[@role='switch']";

    // ----- Dot 19 (W4): const bi worker de comment, khoi phuc -----
    public static final String ADMIN_ADJUST_ERROR =
    "css=[data-testid='admin-adjust-points-form'] p.sa-error";
    public static final String ADMIN_ANY_SEARCH_INPUT =
    "css=[data-testid$='-search-input']";
    public static final String ADMIN_CUSTOMER_FORM_ERROR =
    "css=[data-testid='admin-customer-form'] p.sa-error[role='alert']";
    public static final String ADMIN_DASHBOARD_NEW_REVIEWS_COUNT =
    "css=[data-testid='admin-dashboard-action-new-reviews-count']";
    public static final String ADMIN_EMPTY_REVIEWS_SEARCH_MSG =
    "xpath=//p[contains(text(),'Không tìm thấy đánh giá')]";
    public static final String ADMIN_GENERIC_TABLE_HEADERS =
    "xpath=//div[contains(@class,'sa-table-wrap')]//thead/tr/th";
    public static final String ADMIN_REVIEW_PRODUCT_CELLS =
    "xpath=//*[@data-testid='admin-reviews-section']//tbody/tr/td[2]";
    public static final String ADMIN_SAVE_TIERS_BTN =
    "css=[data-testid='admin-save-tiers-btn']";
    public static final String ADMIN_TIERS_ERROR =
    "xpath=//div[contains(@class,'sa-table-wrap')]/preceding-sibling::p[@class='sa-error']";
    public static final String ADMIN_TIERS_NOTE =
    "xpath=//p[contains(text(),'ngưỡng chi tiêu phải tăng dần')]";
    public static final String DYNAMIC_CUSTOMER_DELETE_BTN =
    "xpath=//button[@aria-label='Xóa khách hàng %s']";
    public static final String DYNAMIC_CUSTOMER_FIELD_ERROR =
    "css=[data-testid='admin-customer-%s-error']";
    public static final String DYNAMIC_CUSTOMER_ROW_AVATAR =
    "xpath=//tr[td[contains(.,'%s')]]//span[contains(@class,'sa-avatar')]";
    public static final String DYNAMIC_CUSTOMER_TIER_CELL =
    "xpath=//tr[td[contains(.,'%s')]]/td[5]";
    public static final String DYNAMIC_REVIEW_ROW_COMMENT =
    "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[5]";
    public static final String DYNAMIC_REVIEW_ROW_CUSTOMER_NAME =
    "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[3]";
    public static final String DYNAMIC_REVIEW_ROW_DELETE_BTN =
    "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]//button[starts-with(@aria-label,'Xóa đánh giá của')]";
    public static final String DYNAMIC_REVIEW_ROW_PRODUCT_BOLD =
    "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[2]/strong";
    public static final String DYNAMIC_REVIEW_ROW_PRODUCT_NAME =
    "xpath=(//*[@data-testid='admin-reviews-section']//tbody/tr)[%s]/td[2]";
    public static final String DYNAMIC_TIER_DETAIL_BTN =
    "xpath=//button[starts-with(@aria-label,'Xem chi tiết hạng %s')]";
    public static final String DYNAMIC_TIER_FREESHIP_INPUT =
    "css=input[aria-label='Freeship %s']";
    public static final String DYNAMIC_TIER_MAX_POINTS_INPUT =
    "css=input[aria-label='Trần điểm %s']";
    public static final String DYNAMIC_TIER_SPEND_PER_POINT_INPUT =
    "css=input[aria-label='Tích điểm %s']";
    public static final String DYNAMIC_TIER_THRESHOLD_INPUT =
    "css=input[aria-label='Ngưỡng %s']";
}
