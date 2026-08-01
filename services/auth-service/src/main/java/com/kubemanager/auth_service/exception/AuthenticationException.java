package com.kubemanager.auth_service.exception;


import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(ErrorCode.AUTHENTICATION_FAILED, message);
    }

}