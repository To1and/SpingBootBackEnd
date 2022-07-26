package com.toland.springboot.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//接口统一返回包装类
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result
{
    private String code;   //标识码，用于返回请求是否成功、失败等
    private String message; //返回成功或错误信息
    private Object data;    //通用数据类型，存储信息

    //无参成功返回
    public static Result success()
    {
        return new Result(Constants.CODE_200, "Succeeded!", null);
    }

    //有参成功返回
    public static Result success(Object data)
    {
        return new Result(Constants.CODE_200, "Succeeded!", data);
    }

    //错误返回
    public static Result error(String code, String message)
    {
        return new Result(code, message, null);
    }

    //默认错误返回，即“系统错误”
    public static Result error()
    {
        return new Result(Constants.CODE_500, "System Error!", null);
    }
}
