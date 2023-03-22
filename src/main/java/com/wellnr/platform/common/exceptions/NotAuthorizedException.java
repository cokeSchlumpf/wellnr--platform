package com.wellnr.platform.common.exceptions;

public class NotAuthorizedException extends DomainException {

    private NotAuthorizedException(String message) {
        super(message);
    }

    public static NotAuthorizedException apply(String message) {
        return new NotAuthorizedException(message);
    }

    @Override
    public int getHttpStatus() {
        return 401;
    }
}
