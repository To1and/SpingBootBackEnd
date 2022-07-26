package com.toland.springboot.common;

//标识码接口类

public interface Constants
{
    String CODE_200="200";  // 成功 - Succeeded!
    String CODE_500="500";  // 系统错误 - System Error!
    String CODE_400="400"; // 参数错误 - Parameter Error!
    String CODE_401="401";  // Token为空 - No token provided!
    String CODE_402="402";  // Token无效 - Invalid token!
    String CODE_403="403";  // 用户不存在 - User does not exist!
    String CODE_600="600"; // 其他业务异常

}
