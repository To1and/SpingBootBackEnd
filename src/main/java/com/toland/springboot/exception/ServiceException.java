package com.toland.springboot.exception;

import lombok.Getter;

//自定义异常，继承RuntimeException
@Getter
public class ServiceException extends RuntimeException
{
    private String code;

    public ServiceException(String code,String message)
    {
        super(message);
        this.code = code;
    }
}

