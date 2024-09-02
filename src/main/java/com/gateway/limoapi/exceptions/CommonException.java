package com.gateway.limoapi.exceptions;

public final class CommonException extends RuntimeException{
    public String message;

    public CommonException(){

    }
    public CommonException(String message){
        super(message);
        this.message=message;
    }
}
