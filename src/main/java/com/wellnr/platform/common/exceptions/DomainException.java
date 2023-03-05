package com.wellnr.platform.common.exceptions;

import java.text.MessageFormat;

/**
 * An {@link DomainException} is an exception which is usually caused due to faulty user requests.
 * The error message of these exceptions is displayed to users.
 */
public class DomainException extends RuntimeException {

    public int getHttpStatus() {
        return 400;
    }

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public static DomainException apply(String message, Object... args) {
        return new DomainException(MessageFormat.format(message, args));
    }

}
