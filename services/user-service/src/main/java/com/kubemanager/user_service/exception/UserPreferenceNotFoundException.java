package com.kubemanager.user_service.exception;

import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class UserPreferenceNotFoundException extends BaseException {

    public UserPreferenceNotFoundException() {
        super(
                ErrorCode.USER_PREFERENCE_NOT_FOUND,
                "User preference not found."
        );
    }
}