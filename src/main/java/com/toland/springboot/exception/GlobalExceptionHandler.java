package com.toland.springboot.exception;

import com.toland.springboot.common.Result;
;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

//全局异常捕获处理类

@ControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result handle(ServiceException serviceException)
    {
        return Result.error(serviceException.getCode(), serviceException.getMessage());
    }
}
