package com.gangnam.coupon.service;

public class SoldOutException extends RuntimeException {
    public SoldOutException(String message) {
        super(message);
    }
}