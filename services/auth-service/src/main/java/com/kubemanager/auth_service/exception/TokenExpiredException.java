package com.kubemanager.auth_service.exception;

import com.kubemanager.exception.BaseException;
import com.kubemanager.exception.ErrorCode;

public class TokenExpiredException extends BaseException {

    public TokenExpiredException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }

}