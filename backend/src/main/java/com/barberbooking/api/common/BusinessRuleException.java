package com.barberbooking.api.common;

public class BusinessRuleException extends RuntimeException{
    public BusinessRuleException(String message){
        super(message);
    }
}
