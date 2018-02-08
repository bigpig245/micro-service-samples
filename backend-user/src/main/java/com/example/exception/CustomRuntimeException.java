package com.example.exception;

import com.example.dto.enumeration.BUMessage;
import lombok.Data;

@Data
public class CustomRuntimeException extends RuntimeException {

    private BUMessage buMessage;

    public CustomRuntimeException(BUMessage buMessage) {
        this(buMessage, null);
    }

    public CustomRuntimeException(BUMessage buMessage, Exception e) {
        this(buMessage.getMessage(), e);
        this.buMessage = buMessage;
    }

    public CustomRuntimeException(String message, Exception e) {
        super(message, e);
    }
}

