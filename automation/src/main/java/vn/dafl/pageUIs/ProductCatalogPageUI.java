package vn.dafl.pageUIs;

/** Locator cua luoi san pham o trang chu + trang chi tiet san pham.
 *  Moi san pham co data-testid dong theo id san pham (trich tu ProductCard.tsx):
 *  product-card-{id}, product-name-{id}, product-price-{id}, product-stock-{id}, add-to-cart-btn-{id}.
 *  Danh sach phan trang 12 san pham/trang (CN-04), chi tiet san pham o route /product/:id (CN-05).
 *  ANY_PRODUCT_CARD chi dem the trong luoi chinh (.products-grid): 3 muc Flash Sale / Ban chay /
 *  Goi y dung cung prefix product-card- nhung hau to -flash/-best/-suggest (App.tsx renderSectionCard),
 *  dem ca 3 muc se vuot 12 va lam sai getProductCountOnPage()/getFirstProductId() (sua 2026-11-10). */
public class ProductCatalogPageUI {

    public static final String ANY_PRODUCT_CARD = "css=.products-grid [data-testid^='product-card-']";
    public static final String DYNAMIC_PRODUCT_CARD = "css=[data-testid='product-card-%s']";
    public static final String DYNAMIC_PRODUCT_NAME = "css=[data-testid='product-name-%s']";
    public static final String DYNAMIC_PRODUCT_PRICE = "css=[data-testid='product-price-%s']";
    public static final String DYNAMIC_PRODUCT_STOCK = "css=[data-testid='product-stock-%s']";
    public static final String DYNAMIC_ADD_TO_CART_BTN = "css=[data-testid='add-to-cart-btn-%s']";

    // Loc / sap xep / phan trang (CN-04)
    public static final String CATEGORY_FILTER_SECTION = "css=[data-testid='category-filter-section']";
    public static final String CATEGORY_SEARCH_INPUT = "css=[data-testid='category-search-input']";
    public static final String CATEGORY_SEARCH_CLEAR_BTN = "css=[data-testid='category-search-clear-btn']";
    public static final String CATEGORY_EMPTY_STATE = "css=[data-testid='category-empty-state']";
    public static final String CLEAR_CATEGORY_BTN = "css=[data-testid='clear-category-btn']";
    public static final String CATALOG_EMPTY_STATE = "css=[data-testid='catalog-empty-state']";
    public static final String CLEAR_FILTERS_BTN = "css=[data-testid='clear-filters-btn']";
    public static final String CATALOG_RESULT_COUNT = "css=[data-testid='catalog-result-count']";
    public static final String SORT_SELECT = "css=[data-testid='sort-select']";
    public static final String PRICE_FROM_INPUT = "css=[data-testid='price-from-input']";
    public static final String PRICE_TO_INPUT = "css=[data-testid='price-to-input']";
    public static final String PRICE_FILTER_ERROR = "css=[data-testid='price-filter-error']";
    // Nut 'Áp dụng' cua bo loc khoang gia (dot 8): bam moi ap dung khoang gia da nhap.
    public static final String PRICE_APPLY_BTN = "css=[data-testid='price-apply-btn']";
    // Khoang gia co san (dot 11): bam 1 cham la dien san Tu/Den + ap dung loc ngay.
    public static final String DYNAMIC_PRICE_PRESET_BTN = "css=[data-testid='price-preset-%s']";
    public static final String CATALOG_PAGINATION = "css=[data-testid='catalog-pagination']";
    public static final String PAGINATION_PREV_BTN = "css=[data-testid='pagination-prev-btn']";
    public static final String PAGINATION_NEXT_BTN = "css=[data-testid='pagination-next-btn']";
    public static final String DYNAMIC_PAGINATION_PAGE_BTN = "css=[data-testid='pagination-page-%s']";
    public static final String ANY_PAGINATION_PAGE_BTN = "css=[data-testid^='pagination-page-']";

    // Trang chi tiet san pham (CN-05)
    public static final String PRODUCT_DETAIL_PAGE = "css=[data-testid='product-detail-page']";
    public static final String PRODUCT_DETAIL_BACK_BTN = "css=[data-testid='product-detail-back-btn']";
    public static final String PRODUCT_NOT_FOUND_PAGE = "css=[data-testid='product-not-found-page']";
    public static final String DETAIL_QTY_MINUS = "css=[data-testid='detail-qty-minus']";
    public static final String DETAIL_QTY_PLUS = "css=[data-testid='detail-qty-plus']";
    public static final String DETAIL_QTY_VALUE = "css=[data-testid='detail-qty-value']";
    public static final String DETAIL_ADD_TO_CART_BTN = "css=[data-testid='detail-add-to-cart-btn']";
    public static final String BUY_NOW_BTN = "css=[data-testid='buy-now-btn']";
    public static final String DETAIL_OUT_OF_STOCK_NOTICE = "css=[data-testid='detail-out-of-stock-notice']";
    public static final String DYNAMIC_VARIANT_OPTION = "css=[data-testid='variant-option-%s']";
    // Wave15.1-auto: TC_SCR5_FUNC_005 / TC_SCR5_UI_006 — dem tat ca nut bien the
    // (moi SP chi co 1 nut "Mac dinh" nhung id bien the la dong, khong biet truoc).
    public static final String ANY_VARIANT_OPTION = "css=[data-testid^='variant-option-']";
    /** Dong chu "(Con X san pham co san)" canh o so luong o trang chi tiet. */
    public static final String DETAIL_STOCK_TEXT = "xpath=//span[contains(.,'sản phẩm có sẵn')]";

    // Danh gia sao o trang chi tiet (dot 4, App.tsx: muc "Danh gia cua khach hang").
    // Section LUON hien: co danh gia thi hien tom tat diem trung binh + danh sach
    // product-review-item; chua co danh gia thi hien dong "Chua co danh gia nao".
    // Du lieu tu ProductDetailDto.Evaluations (BE); seed tao 4 danh gia mau cho 4 son
    // dau tien (Son kem 3CE, Son thoi Rom&nd, Son bong Peripera, Son kem Clio) - chi
    // khi bang Evaluations trong luc seed.
    public static final String PRODUCT_REVIEWS_SECTION = "css=[data-testid='product-reviews-section']";
    public static final String PRODUCT_RATING_SUMMARY = "css=[data-testid='product-rating-summary']";
    public static final String ANY_PRODUCT_REVIEW_ITEM = "css=[data-testid='product-review-item']";
    public static final String PRODUCT_REVIEWS_EMPTY_MSG =
            "xpath=//section[@data-testid='product-reviews-section']//p[contains(text(),'Chưa có đánh giá nào')]";

    // Form gui danh gia moi o trang chi tiet (dot 15): hien trong muc danh gia,
    // co 5 nut chon sao + o ten + o binh luan + nut gui; loi/thanh cong hien inline.
    public static final String REVIEW_FORM = "css=[data-testid='review-form']";
    public static final String DYNAMIC_REVIEW_STAR_BTN = "css=[data-testid='review-star-%s']";
    public static final String REVIEW_NAME_INPUT = "css=[data-testid='review-name-input']";
    public static final String REVIEW_COMMENT_INPUT = "css=[data-testid='review-comment-input']";
    public static final String REVIEW_SUBMIT_BTN = "css=[data-testid='review-submit-btn']";
    public static final String REVIEW_ERROR_MSG = "css=[data-testid='review-error-msg']";
    public static final String REVIEW_SUCCESS_MSG = "css=[data-testid='review-success-msg']";

    // Wave15.1-auto (SCR_04): the SP trong LUOI CHINH (#products-section) — luoi
    // nay chi chua 12 SP/trang; cac the -flash/-best/-suggest o muc ben duoi
    // cung khop tien to product-card- nen KHONG dung ANY_PRODUCT_CARD de dem luoi.
    public static final String ANY_GRID_PRODUCT_CARD =
            "css=#products-section .products-grid [data-testid^='product-card-']";
    // Wave15.1-auto (TC_SCR4_UI_001): thanh phan the SP (khong co testid rieng).
    public static final String DYNAMIC_PRODUCT_DISCOUNT_BADGE =
            "css=[data-testid='product-card-%s'] .product-discount-badge";
    public static final String DYNAMIC_PRODUCT_RATING =
            "css=[data-testid='product-card-%s'] .product-rating";
    public static final String DYNAMIC_PRODUCT_ORIGINAL_PRICE =
            "css=[data-testid='product-card-%s'] .product-original-price";
    // Wave15.1-auto (TC_SCR4_FUNC_008): nut "Hien thi tat ca" trong trang thai
    // rong tim danh muc (khong co testid rieng).
    public static final String CATEGORY_EMPTY_SHOW_ALL_BTN =
            "xpath=//*[@data-testid='category-empty-state']//button[contains(text(),'Hiển thị tất cả')]";
    // Wave15.1-auto (TC_SCR4_FUNC_010): nhom "Loai san pham" o sidebar
    // (class filter-link, KHONG co data-testid rieng).
    public static final String DYNAMIC_FILTER_LINK_BTN =
            "xpath=//button[contains(@class,'filter-link')][normalize-space()='%s']";
    public static final String ANY_FILTER_LINK_BTN = "css=button.filter-link";
}
