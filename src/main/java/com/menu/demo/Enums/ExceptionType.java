package com.menu.demo.Enums;

import lombok.Getter;

@Getter
public enum ExceptionType {
    BAD_CREDENTIALS(1, "Bad Credentials"),
    ACCESS_DENIED(2, "You don't have permissions to access this ressource !"),
    UNAUTHORIZED_ACCESS(3, "Full authentication is required to access this resource"),
    INVALID_INPUTS(4, "Invalid inputs"),
    UNKNOWN_ERROR(5, "Unknown error !"),
    TOKEN_EXPIRED(6, "Token expired"),
    REFRESH_TOKEN_EXPIRED(7, "Refresh token expired");



    private final int code;
    private final String message;

    ExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
