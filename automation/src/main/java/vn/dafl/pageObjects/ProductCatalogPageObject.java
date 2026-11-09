package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.ProductCatalogPageUI;

import java.util.ArrayList;
import java.util.List;

/** Page Object cho luoi san pham trang chu (CN-04) va trang chi tiet san pham (CN-05). */
public class ProductCatalogPageObject extends BasePage {

    public void waitForProductsLoaded() {
        waitForListElementsVisible(ProductCatalogPageUI.ANY_PRODUCT_CARD);
    }

    /** San pham dau tien dang hien thi; id lay tu data-testid cua the san pham. */
    public String getFirstProductId() {
        List<WebElement> cards = getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD);
        if (cards.isEmpty()) {
            return null;
        }
        String testId = cards.get(0).getAttribute("data-testid");
        return testId == null ? null : testId.replace("product-card-", "");
    }

    public List<String> getDisplayedProductIds() {
        List<String> ids = new ArrayList<>();
        for (WebElement card : getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD)) {
            String testId = card.getAttribute("data-testid");
            if (testId != null) {
                ids.add(testId.replace("product-card-", ""));
            }
        }
        return ids;
    }

    public int getProductCountOnPage() {
        return getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_CARD).size();
    }

    public String getProductName(String productId) {
        return getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_NAME, productId);
    }

    public long getProductPrice(String productId) {
        String text = getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_PRICE, productId);
        return parseMoney(text);
    }

    /** Chuoi ton kho tren the san pham: "Con X san pham" hoac "Het hang". */
    public String getProductStockText(String productId) {
        return getElementText(ProductCatalogPageUI.DYNAMIC_PRODUCT_STOCK, productId);
    }

    /** Gia cua tung the san pham theo thu tu hien thi (dung cho test sap xep). */
    public List<Long> getDisplayedProductPrices() {
        List<Long> prices = new ArrayList<>();
        for (String id : getDisplayedProductIds()) {
            prices.add(getProductPrice(id));
        }
        return prices;
    }

    public void addToCart(String productId) {
        scrollToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_CARD, productId);
        clickToElement(ProductCatalogPageUI.DYNAMIC_ADD_TO_CART_BTN, productId);
        pauseAfterAction();
    }

    public boolean isAddToCartButtonEnabled(String productId) {
        return isElementEnabled(ProductCatalogPageUI.DYNAMIC_ADD_TO_CART_BTN, productId);
    }

    public void openProductDetail(String productId) {
        scrollToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_CARD, productId);
        clickToElement(ProductCatalogPageUI.DYNAMIC_PRODUCT_NAME, productId);
        waitForElementVisible(ProductCatalogPageUI.PRODUCT_DETAIL_PAGE);
    }

    // ---------- Loc / sap xep / phan trang (CN-04) ----------
    public String getCatalogResultCountText() {
        return getElementText(ProductCatalogPageUI.CATALOG_RESULT_COUNT);
    }

    public boolean isCatalogEmptyStateDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.CATALOG_EMPTY_STATE, 5);
    }

    public void clearFilters() {
        clickToElement(ProductCatalogPageUI.CLEAR_FILTERS_BTN);
        sleepInSecond(2);
    }

    /** Sap xep theo value cua <select>: newest | price-asc | price-desc. */
    public void selectSort(String sortValue) {
        selectByValue(ProductCatalogPageUI.SORT_SELECT, sortValue);
        sleepInSecond(2);
    }

    public String getSortValue() {
        return getSelectedValue(ProductCatalogPageUI.SORT_SELECT);
    }

    /** Loc gia theo khoang (FE debounce 500ms roi tai lai danh sach). */
    public void setPriceRange(String from, String to) {
        if (from != null) {
            sendKeyToElement(ProductCatalogPageUI.PRICE_FROM_INPUT, from);
        }
        if (to != null) {
            sendKeyToElement(ProductCatalogPageUI.PRICE_TO_INPUT, to);
        }
        sleepInSecond(2);
    }

    public boolean isPriceFilterErrorDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRICE_FILTER_ERROR, 5);
    }

    /** Bam nut 'Áp dụng' de ap dung khoang gia da nhap o 2 o gia (dot 8). */
    public void clickPriceApply() {
        clickToElement(ProductCatalogPageUI.PRICE_APPLY_BTN);
        sleepInSecond(2); // cho loc ap dung + luoi tai lai
    }

    public boolean isPriceApplyButtonDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRICE_APPLY_BTN, 10);
    }

    /** Bam 1 khoang gia co san (dot 11): dien san Tu/Den + ap dung loc ngay. */
    public void clickPricePreset(int index) {
        clickToElement(String.format(ProductCatalogPageUI.DYNAMIC_PRICE_PRESET_BTN, index));
        sleepInSecond(2); // cho loc ap dung + luoi tai lai
    }

    public String getPriceFromValue() {
        return getElementAttribute(ProductCatalogPageUI.PRICE_FROM_INPUT, "value");
    }

    public String getPriceToValue() {
        return getElementAttribute(ProductCatalogPageUI.PRICE_TO_INPUT, "value");
    }

    public String getPriceFilterError() {
        waitForElementVisible(ProductCatalogPageUI.PRICE_FILTER_ERROR);
        return getElementText(ProductCatalogPageUI.PRICE_FILTER_ERROR);
    }

    public boolean isPaginationDisplayed() {
        return isElementDisplayed(ProductCatalogPageUI.CATALOG_PAGINATION);
    }

    public void goToCatalogPage(int pageNumber) {
        clickToElement(ProductCatalogPageUI.DYNAMIC_PAGINATION_PAGE_BTN, String.valueOf(pageNumber));
        sleepInSecond(2);
    }

    public void goToNextCatalogPage() {
        clickToElement(ProductCatalogPageUI.PAGINATION_NEXT_BTN);
        sleepInSecond(2);
    }

    public void goToPreviousCatalogPage() {
        clickToElement(ProductCatalogPageUI.PAGINATION_PREV_BTN);
        sleepInSecond(2);
    }

    public boolean isNextPageButtonEnabled() {
        return isElementEnabled(ProductCatalogPageUI.PAGINATION_NEXT_BTN);
    }

    // ---------- Trang chi tiet san pham (CN-05) ----------
    public boolean isProductDetailDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_DETAIL_PAGE, 10);
    }

    public boolean isProductNotFoundDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_NOT_FOUND_PAGE, 10);
    }

    /** So luong con lai ghi o dong "(Con X san pham co san)" tren trang chi tiet; -1 neu khong doc duoc. */
    public int getDetailAvailableStock() {
        if (!isElementDisplayed(ProductCatalogPageUI.DETAIL_STOCK_TEXT)) {
            return -1;
        }
        String text = getElementText(ProductCatalogPageUI.DETAIL_STOCK_TEXT);
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? -1 : Integer.parseInt(digits);
    }

    public int getDetailQuantity() {
        String text = getElementText(ProductCatalogPageUI.DETAIL_QTY_VALUE).trim();
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }

    public void increaseDetailQuantity() {
        clickToElement(ProductCatalogPageUI.DETAIL_QTY_PLUS);
    }

    public void decreaseDetailQuantity() {
        clickToElement(ProductCatalogPageUI.DETAIL_QTY_MINUS);
    }

    public boolean isDetailPlusEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_QTY_PLUS);
    }

    public boolean isDetailMinusEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_QTY_MINUS);
    }

    public void addToCartFromDetail() {
        clickToElement(ProductCatalogPageUI.DETAIL_ADD_TO_CART_BTN);
        pauseAfterAction();
    }

    public void buyNowFromDetail() {
        clickToElement(ProductCatalogPageUI.BUY_NOW_BTN);
    }

    public boolean isDetailAddToCartEnabled() {
        return isElementEnabled(ProductCatalogPageUI.DETAIL_ADD_TO_CART_BTN);
    }

    public boolean isBuyNowEnabled() {
        return isElementEnabled(ProductCatalogPageUI.BUY_NOW_BTN);
    }

    public boolean isOutOfStockNoticeDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.DETAIL_OUT_OF_STOCK_NOTICE, 5);
    }

    public void backFromDetail() {
        clickToElement(ProductCatalogPageUI.PRODUCT_DETAIL_BACK_BTN);
        waitForProductsLoaded();
    }

    // ---------- Danh gia sao o trang chi tiet (dot 4) ----------
    public boolean isReviewsSectionDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_REVIEWS_SECTION, 10);
    }

    public boolean isRatingSummaryDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_RATING_SUMMARY, 5);
    }

    /** Text tom tat diem trung binh (vd "5.0 ... 1 đánh giá"); rong neu khong co tom tat. */
    public String getRatingSummaryText() {
        if (!isRatingSummaryDisplayed()) {
            return "";
        }
        return getElementText(ProductCatalogPageUI.PRODUCT_RATING_SUMMARY);
    }

    /** So danh gia (product-review-item) dang hien tren trang chi tiet. */
    public int getReviewItemCount() {
        return getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_REVIEW_ITEM).size();
    }

    /** Text gop cua toan bo danh gia dang hien (ten nguoi danh gia + binh luan). */
    public String getAllReviewsText() {
        StringBuilder combined = new StringBuilder();
        for (WebElement item : getListWebElements(ProductCatalogPageUI.ANY_PRODUCT_REVIEW_ITEM)) {
            combined.append(item.getText()).append("\n");
        }
        return combined.toString();
    }

    public boolean isReviewsEmptyStateDisplayed() {
        return isElementDisplayedWithWait(ProductCatalogPageUI.PRODUCT_REVIEWS_EMPTY_MSG, 5);
    }
}
