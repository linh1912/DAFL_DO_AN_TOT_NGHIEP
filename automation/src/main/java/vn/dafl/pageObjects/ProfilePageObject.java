package vn.dafl.pageObjects;

import org.openqa.selenium.WebElement;
import vn.dafl.commons.BasePage;
import vn.dafl.pageUIs.ProfilePageUI;

import java.util.List;

/** Page Object cho trang ho so /profile: cap nhat thong tin, doi mat khau va
 *  so dia chi giao hang (CN-03.3, toi da 5 dia chi, mac dinh tu dong/ke thua khi xoa). */
public class ProfilePageObject extends BasePage {

    public void waitForProfileLoaded() {
        waitForElementVisible(ProfilePageUI.PROFILE_FORM);
    }

    public boolean isProfilePageDisplayed() {
        return getCurrentPageUrl().contains("/profile");
    }

    // ---------- 3.1 Thong tin ca nhan ----------
    public String getProfileName() {
        return getElementValue(ProfilePageUI.PROFILE_NAME_INPUT);
    }

    public String getProfilePhone() {
        return getElementValue(ProfilePageUI.PROFILE_PHONE_INPUT);
    }

    public String getProfileEmail() {
        return getElementValue(ProfilePageUI.PROFILE_EMAIL_INPUT);
    }

    public void updateProfile(String fullName, String phone) {
        sendKeyToElement(ProfilePageUI.PROFILE_NAME_INPUT, fullName);
        sendKeyToElement(ProfilePageUI.PROFILE_PHONE_INPUT, phone);
        clickToElement(ProfilePageUI.PROFILE_SAVE_BTN);
    }

    public boolean isProfileSuccessDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.PROFILE_SUCCESS_MSG, 10);
    }

    public String getProfileSuccessMessage() {
        waitForElementVisible(ProfilePageUI.PROFILE_SUCCESS_MSG);
        return getElementText(ProfilePageUI.PROFILE_SUCCESS_MSG);
    }

    public boolean isProfilePhoneErrorDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.PROFILE_PHONE_ERROR, 5);
    }

    public String getProfilePhoneError() {
        waitForElementVisible(ProfilePageUI.PROFILE_PHONE_ERROR);
        return getElementText(ProfilePageUI.PROFILE_PHONE_ERROR);
    }

    public boolean isProfileNameErrorDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.PROFILE_NAME_ERROR, 5);
    }

    public String getProfileNameError() {
        waitForElementVisible(ProfilePageUI.PROFILE_NAME_ERROR);
        return getElementText(ProfilePageUI.PROFILE_NAME_ERROR);
    }

    // ---------- 3.2 Doi mat khau ----------
    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        sendKeyToElement(ProfilePageUI.CHANGE_PASSWORD_CURRENT_INPUT, currentPassword);
        sendKeyToElement(ProfilePageUI.CHANGE_PASSWORD_NEW_INPUT, newPassword);
        sendKeyToElement(ProfilePageUI.CHANGE_PASSWORD_CONFIRM_INPUT, confirmPassword);
        clickToElement(ProfilePageUI.CHANGE_PASSWORD_SUBMIT_BTN);
    }

    public boolean isChangePasswordSuccessDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.CHANGE_PASSWORD_SUCCESS, 10);
    }

    public boolean isChangePasswordCurrentErrorDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.CHANGE_PASSWORD_CURRENT_ERROR, 5);
    }

    public String getChangePasswordCurrentError() {
        waitForElementVisible(ProfilePageUI.CHANGE_PASSWORD_CURRENT_ERROR);
        return getElementText(ProfilePageUI.CHANGE_PASSWORD_CURRENT_ERROR);
    }

    public boolean isSetPasswordFormDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.SET_PASSWORD_FORM, 5);
    }

    public void setPassword(String password, String confirmPassword) {
        sendKeyToElement(ProfilePageUI.SET_PASSWORD_INPUT, password);
        sendKeyToElement(ProfilePageUI.SET_PASSWORD_CONFIRM_INPUT, confirmPassword);
        clickToElement(ProfilePageUI.SET_PASSWORD_SUBMIT_BTN);
    }

    public boolean isSetPasswordSuccessDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.SET_PASSWORD_SUCCESS, 10);
    }

    // ---------- 3.3 So dia chi ----------
    public void waitForAddressBookVisible() {
        waitForElementVisible(ProfilePageUI.ADDRESS_BOOK);
    }

    public int getAddressCount() {
        return getListWebElements(ProfilePageUI.ANY_ADDRESS_ITEM).size();
    }

    /** Lay id cua cac dia chi theo thu tu hien thi tu data-testid address-item-{id}. */
    public List<String> getAddressIds() {
        List<String> ids = new java.util.ArrayList<>();
        for (WebElement item : getListWebElements(ProfilePageUI.ANY_ADDRESS_ITEM)) {
            String testId = item.getAttribute("data-testid");
            if (testId != null) {
                ids.add(testId.replace("address-item-", ""));
            }
        }
        return ids;
    }

    public String getFirstAddressId() {
        List<String> ids = getAddressIds();
        return ids.isEmpty() ? null : ids.get(0);
    }

    /** id cua dia chi dang mang huy hieu mac dinh; null neu chua co dia chi nao. */
    public String getDefaultAddressId() {
        List<WebElement> badges = getListWebElements(ProfilePageUI.ANY_ADDRESS_DEFAULT_BADGE);
        if (badges.isEmpty()) {
            return null;
        }
        String testId = badges.get(0).getAttribute("data-testid");
        return testId == null ? null : testId.replace("address-default-badge-", "");
    }

    public boolean isAddressDefault(String addressId) {
        return isElementDisplayed(ProfilePageUI.DYNAMIC_ADDRESS_DEFAULT_BADGE, addressId);
    }

    public String getAddressItemText(String addressId) {
        return getElementText(ProfilePageUI.DYNAMIC_ADDRESS_ITEM, addressId);
    }

    public boolean isAddressLimitErrorDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.ADDRESS_LIMIT_ERROR, 5);
    }

    /** Mo form them dia chi; tra ve false neu bi chan boi gioi han 5 dia chi. */
    public boolean openAddAddressForm() {
        // Cuon nut ra giua man hinh truoc khi bam - nut nam sat header dinh (sticky) nen bi che.
        scrollToElement(ProfilePageUI.ADDRESS_ADD_BTN);
        clickToElement(ProfilePageUI.ADDRESS_ADD_BTN);
        if (isAddressLimitErrorDisplayed()) {
            return false;
        }
        waitForElementVisible(ProfilePageUI.ADDRESS_FORM);
        return true;
    }

    /** Dien form dia chi (them moi hoac sua) va bam Luu. */
    public void fillAndSaveAddressForm(String recipient, String phone,
                                       String province, String district, String ward,
                                       String detailAddress, boolean setDefault) {
        sendKeyToElement(ProfilePageUI.ADDRESS_RECIPIENT_INPUT, recipient);
        sendKeyToElement(ProfilePageUI.ADDRESS_PHONE_INPUT, phone);
        selectByVisibleText(ProfilePageUI.ADDRESS_PROVINCE_SELECT, province);
        sleepInSecond(1);
        selectByVisibleText(ProfilePageUI.ADDRESS_DISTRICT_SELECT, district);
        sleepInSecond(1);
        selectByVisibleText(ProfilePageUI.ADDRESS_WARD_SELECT, ward);
        sendKeyToElement(ProfilePageUI.ADDRESS_DETAIL_INPUT, detailAddress);
        if (setDefault && isElementDisplayed(ProfilePageUI.ADDRESS_SET_DEFAULT_CHECKBOX)) {
            clickToElement(ProfilePageUI.ADDRESS_SET_DEFAULT_CHECKBOX);
        }
        clickToElement(ProfilePageUI.ADDRESS_SAVE_BTN);
    }

    /** Them nhanh mot dia chi mac dinh (Ha Noi / Ba Dinh / Phuc Xa) va cho form dong lai. */
    public void addAddress(String recipient, String phone, String detailAddress) {
        openAddAddressForm();
        fillAndSaveAddressForm(recipient, phone,
                vn.dafl.commons.GlobalConstants.SHIPPING_PROVINCE,
                vn.dafl.commons.GlobalConstants.SHIPPING_DISTRICT,
                vn.dafl.commons.GlobalConstants.SHIPPING_WARD,
                detailAddress, false);
        waitForElementInvisible(ProfilePageUI.ADDRESS_FORM);
    }

    public void setAddressAsDefault(String addressId) {
        clickToElement(ProfilePageUI.DYNAMIC_ADDRESS_SET_DEFAULT_BTN, addressId);
        sleepInSecond(2);
    }

    public void openEditAddressForm(String addressId) {
        clickToElement(ProfilePageUI.DYNAMIC_ADDRESS_EDIT_BTN, addressId);
        waitForElementVisible(ProfilePageUI.ADDRESS_FORM);
    }

    /** Xoa dia chi va xac nhan trong hop xac nhan truc tuyen cua tung muc. */
    public void deleteAddress(String addressId) {
        clickToElement(ProfilePageUI.DYNAMIC_ADDRESS_DELETE_BTN, addressId);
        waitForElementVisible(ProfilePageUI.ADDRESS_DELETE_CONFIRM_BTN);
        clickToElement(ProfilePageUI.ADDRESS_DELETE_CONFIRM_BTN);
        sleepInSecond(2);
    }

    public boolean isAddressFormErrorDisplayed() {
        return isElementDisplayedWithWait(ProfilePageUI.ADDRESS_FORM_ERROR, 5);
    }

    public String getAddressFormError() {
        waitForElementVisible(ProfilePageUI.ADDRESS_FORM_ERROR);
        return getElementText(ProfilePageUI.ADDRESS_FORM_ERROR);
    }
}
