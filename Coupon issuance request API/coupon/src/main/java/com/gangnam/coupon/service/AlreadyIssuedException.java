package com.gangnam.coupon.service;

public class AlreadyIssuedException extends RuntimeException {
    public AlreadyIssuedException(String message) {
        super(message);
    }
}