package com.wellnr.platform.common.validation;

import com.google.common.collect.Lists;
import com.wellnr.platform.common.exceptions.DomainException;

import java.util.List;

public final class ValidationException extends DomainException {

    private final List<String> messages;

    public ValidationException(String message) {
        super(message);
        this.messages = Lists.newArrayList();
    }

    public ValidationException(String message, List<String> messages) {
        super(message);
        this.messages = messages;
    }

    public static ValidationException apply(String message) {
        return new ValidationException(message);
    }

    public static ValidationException apply(String message, List<String> messages) {
        var sb = new StringBuilder()
            .append(message);

        for (var msg : messages) {
            sb
                .append("\n")
                .append("- ")
                .append(msg);
        }

        return new ValidationException(sb.toString(), messages);
    }

    public List<String> getMessages() {
        return messages;
    }

}
