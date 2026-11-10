package vn.dafl.pageUIs;

/** Locator cua trang ho so /profile (CN-03): form thong tin ca nhan, form doi mat khau,
 *  form dat mat khau (tai khoan social) va so dia chi giao hang AddressBook.
 *  Dia chi co data-testid dong theo id: address-item-{id}, address-default-badge-{id},
 *  address-set-default-btn-{id}, address-edit-btn-{id}, address-delete-btn-{id}. */
public class ProfilePageUI {

    public static final String DYNAMIC_TEXT_ON_PAGE = "xpath=//*[contains(text(),'%s')]";

    // 3.1 Ho so ca nhan (email chi doc)
    public static final String PROFILE_FORM = "css=[data-testid='profile-form']";
    public static final String PROFILE_NAME_INPUT = "css=[data-testid='profile-name-input']";
    public static final String PROFILE_PHONE_INPUT = "css=[data-testid='profile-phone-input']";
    public static final String PROFILE_EMAIL_INPUT = "css=[data-testid='profile-email-input']";
    public static final String PROFILE_SAVE_BTN = "css=[data-testid='profile-save-btn']";
    public static final String PROFILE_SUCCESS_MSG = "css=[data-testid='profile-success-msg']";
    public static final String PROFILE_NAME_ERROR = "css=[data-testid='profile-name-error']";
    public static final String PROFILE_PHONE_ERROR = "css=[data-testid='profile-phone-error']";

    // 3.2 Doi mat khau
    public static final String CHANGE_PASSWORD_FORM = "css=[data-testid='change-password-form']";
    public static final String CHANGE_PASSWORD_CURRENT_INPUT = "css=[data-testid='change-password-current-input']";
    public static final String CHANGE_PASSWORD_NEW_INPUT = "css=[data-testid='change-password-new-input']";
    public static final String CHANGE_PASSWORD_CONFIRM_INPUT = "css=[data-testid='change-password-confirm-input']";
    public static final String CHANGE_PASSWORD_CURRENT_ERROR = "css=[data-testid='change-password-current-error']";
    public static final String CHANGE_PASSWORD_NEW_ERROR = "css=[data-testid='change-password-new-error']";
    public static final String CHANGE_PASSWORD_CONFIRM_ERROR = "css=[data-testid='change-password-confirm-error']";
    public static final String CHANGE_PASSWORD_SUBMIT_BTN = "css=[data-testid='change-password-submit-btn']";
    public static final String CHANGE_PASSWORD_SUCCESS = "css=[data-testid='change-password-success']";

    // Dat mat khau cho tai khoan social (chi hien khi tai khoan chua co mat khau)
    public static final String SET_PASSWORD_FORM = "css=[data-testid='set-password-form']";
    public static final String SET_PASSWORD_INPUT = "css=[data-testid='set-password-input']";
    public static final String SET_PASSWORD_CONFIRM_INPUT = "css=[data-testid='set-password-confirm-input']";
    public static final String SET_PASSWORD_SUBMIT_BTN = "css=[data-testid='set-password-submit-btn']";
    public static final String SET_PASSWORD_SUCCESS = "css=[data-testid='set-password-success']";
    public static final String SET_PASSWORD_ERROR = "css=[data-testid='set-password-error']";

    // 3.3 So dia chi giao hang
    public static final String ADDRESS_BOOK = "css=[data-testid='address-book']";
    public static final String ADDRESS_BOOK_LIST = "css=[data-testid='address-book-list']";
    public static final String ADDRESS_ADD_BTN = "css=[data-testid='address-add-btn']";
    public static final String ADDRESS_LIMIT_ERROR = "css=[data-testid='address-limit-error']";
    public static final String ANY_ADDRESS_ITEM = "css=[data-testid^='address-item-']";
    public static final String DYNAMIC_ADDRESS_ITEM = "css=[data-testid='address-item-%s']";
    public static final String DYNAMIC_ADDRESS_DEFAULT_BADGE = "css=[data-testid='address-default-badge-%s']";
    public static final String ANY_ADDRESS_DEFAULT_BADGE = "css=[data-testid^='address-default-badge-']";
    public static final String DYNAMIC_ADDRESS_SET_DEFAULT_BTN = "css=[data-testid='address-set-default-btn-%s']";
    public static final String DYNAMIC_ADDRESS_EDIT_BTN = "css=[data-testid='address-edit-btn-%s']";
    public static final String DYNAMIC_ADDRESS_DELETE_BTN = "css=[data-testid='address-delete-btn-%s']";
    public static final String ADDRESS_DELETE_CONFIRM_BTN = "css=[data-testid='address-delete-confirm-btn']";
    public static final String ADDRESS_DELETE_CANCEL_BTN = "css=[data-testid='address-delete-cancel-btn']";

    // Form them/sua dia chi
    public static final String ADDRESS_FORM = "css=[data-testid='address-form']";
    public static final String ADDRESS_RECIPIENT_INPUT = "css=[data-testid='address-recipient-input']";
    public static final String ADDRESS_PHONE_INPUT = "css=[data-testid='address-phone-input']";
    public static final String ADDRESS_PROVINCE_SELECT = "css=[data-testid='address-province-select']";
    public static final String ADDRESS_DISTRICT_SELECT = "css=[data-testid='address-district-select']";
    public static final String ADDRESS_WARD_SELECT = "css=[data-testid='address-ward-select']";
    public static final String ADDRESS_DETAIL_INPUT = "css=[data-testid='address-detail-input']";
    public static final String ADDRESS_NOTE_INPUT = "css=[data-testid='address-note-input']";
    public static final String ADDRESS_SET_DEFAULT_CHECKBOX = "css=[data-testid='address-set-default-checkbox']";
    public static final String ADDRESS_SAVE_BTN = "css=[data-testid='address-save-btn']";
    public static final String ADDRESS_CANCEL_BTN = "css=[data-testid='address-cancel-btn']";
    public static final String ADDRESS_FORM_ERROR = "css=[data-testid='address-form-error']";
    public static final String ADDRESS_RECIPIENT_ERROR = "css=[data-testid='address-recipient-error']";
    public static final String ADDRESS_PHONE_ERROR = "css=[data-testid='address-phone-error']";
    public static final String ADDRESS_PROVINCE_ERROR = "css=[data-testid='address-province-error']";
    public static final String ADDRESS_DISTRICT_ERROR = "css=[data-testid='address-district-error']";
    public static final String ADDRESS_WARD_ERROR = "css=[data-testid='address-ward-error']";
    public static final String ADDRESS_DETAIL_ERROR = "css=[data-testid='address-detail-error']";

    // Wave15.1-auto (TC_SCR3_VAL_009): checklist doi mat khau, prefix change-password (inventory §11.2).
    public static final String CHANGE_PASSWORD_RULE_LENGTH = "css=[data-testid='change-password-rule-len']";
    public static final String CHANGE_PASSWORD_RULE_UPPERCASE = "css=[data-testid='change-password-rule-upper']";
    public static final String CHANGE_PASSWORD_RULE_DIGIT = "css=[data-testid='change-password-rule-digit']";
    public static final String CHANGE_PASSWORD_RULE_SPECIAL = "css=[data-testid='change-password-rule-special']";
}
