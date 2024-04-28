package com.cs.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserControllerBadRequestException extends RuntimeException {

    public UserControllerBadRequestException(String cause) {
        super(cause);
    }
}
