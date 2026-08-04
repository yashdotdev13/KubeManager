package com.kubemanager.user_service.exception;




import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class UserProfileNotFoundException extends BaseException {

    public UserProfileNotFoundException() {
        super(
                ErrorCode.USER_PROFILE_NOT_FOUND,
                "User profile not found."
        );
    }
}