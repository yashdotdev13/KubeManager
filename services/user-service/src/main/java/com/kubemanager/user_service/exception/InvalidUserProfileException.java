package com.kubemanager.user_service.exception;


import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class InvalidUserProfileException extends BaseException {

    public InvalidUserProfileException(String message) {
        super(
                ErrorCode.INVALID_USER_PROFILE,
                message
        );
    }
}