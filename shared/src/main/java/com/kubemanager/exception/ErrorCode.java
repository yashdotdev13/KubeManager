package com.kubemanager.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Resource Not Found"),
    CONFLICT("409", "Conflict"),
    VALIDATION_ERROR("422", "Validation Failed"),
    INTERNAL_SERVER_ERROR("500", "Internal Server Error");

    private final String code;
    private final String message;

}