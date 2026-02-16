package com.aws.practice_aws.shared.exception;

import org.springframework.http.HttpStatus;

public final class ResourceAlreadyExistsException extends BusinessException {

    public ResourceAlreadyExistsException(String message) {
        super(ErrorCodes.RESOURCE_ALREADY_EXISTS, message, HttpStatus.CONFLICT);
    }
}
