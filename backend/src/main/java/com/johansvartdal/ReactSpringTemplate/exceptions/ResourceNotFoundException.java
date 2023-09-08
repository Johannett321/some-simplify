package com.johansvartdal.ReactSpringTemplate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "An error has occurred")
public class ResourceNotFoundException extends RuntimeException {
}
