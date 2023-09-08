package com.johansvartdal.ReactSpringTemplate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "You do not have access to this project")
public class NotAccessToProjectException extends RuntimeException{
}
