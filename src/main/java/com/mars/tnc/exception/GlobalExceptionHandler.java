/*
 *  TECH-STORE CONFIDENTIAL
 *  Copyright 2024 Tech-Store Corporation All Rights Reserved.
 *  The source code contained or described herein and all documents related to the
 *  source code ("Material") are owned by Tech-Store Malaysia or its suppliers or
 *  licensors. Title to the Material remains with Tech-Store Malaysia or its suppliers
 *  and licensors. The Material contains trade secrets and proprietary and
 *  confidential information of Tech-Store or its suppliers and licensors. The Material
 *  is protected by worldwide copyright and trade secret laws and treaty provisions.
 *  No part of the Material may be used, copied, reproduced, modified, published,
 *  uploaded, posted, transmitted, distributed, or disclosed in any way without
 *  Tech-Store's prior express written permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual
 *  property right is granted to or conferred upon you by disclosure or delivery of
 *  the Materials, either expressly, by implication, inducement, estoppel or
 *  otherwise. Any license under such intellectual property rights must be express
 *  and approved by Tech-Store in writing.
 */

package com.mars.tnc.exception;

import com.mars.tnc.dto.response.ApiDTO;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.add(error.getDefaultMessage())
        );
        return new ResponseEntity<>(
                new ApiDTO(false, errors.toString()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {InvalidArgumentException.class})
    public ResponseEntity<Object> handleInvalidArgumentException(InvalidArgumentException e) {
        return new ResponseEntity<>(
                new ApiDTO(false, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {UnauthorizedException.class})
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<>(
                new ApiDTO(false, e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        return new ResponseEntity<>(
                new ApiDTO(false, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {ConflictException.class})
    public ResponseEntity<Object> handleConflictException(ConflictException e) {
        return new ResponseEntity<>(
                new ApiDTO(false, e.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {FeignException.class})
    public ResponseEntity<Object> handleFeignException(FeignException e) {
        return new ResponseEntity<>(
                new ApiDTO(false, e.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception e) {
        return new ResponseEntity<>(
                new ApiDTO(false, "An unexpected exception occurred " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
