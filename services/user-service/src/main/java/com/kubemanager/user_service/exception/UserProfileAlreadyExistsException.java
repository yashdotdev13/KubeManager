package com.kubemanager.user_service.exception;


import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class UserProfileAlreadyExistsException extends BaseException {

    public UserProfileAlreadyExistsException() {
        super(
                ErrorCode.USER_PROFILE_ALREADY_EXISTS,
                "User profile already exists."
        );
    }
}