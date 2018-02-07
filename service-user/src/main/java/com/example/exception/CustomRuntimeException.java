package com.example.exception;

import com.example.dto.enumeration.SUMessage;
import lombok.Data;

@Data
public class CustomRuntimeException extends RuntimeException {
    private SUMessage suMessage;

    public CustomRuntimeException(SUMessage suMessage) {
        this(suMessage, null);
    }

    public CustomRuntimeException(SUMessage suMessage, Exception e) {
        this(suMessage.getMessage(), e);
        this.suMessage = suMessage;
    }

    public CustomRuntimeException(String message, Exception e) {
        super(message, e);
    }
}
