package com.toland.springboot.common.interceptor;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.toland.springboot.common.Constants;
import com.toland.springboot.entity.User;
import com.toland.springboot.exception.ServiceException;
import com.toland.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*拦截器，验证token*/

public class JwtInterceptor implements HandlerInterceptor
{
    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        request.getHeader("token");
        String token = request.getHeader("token");

        //若非映射到方法则直接通过
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        //进行认证
        if (StrUtil.isBlank(token))
        {
            throw new ServiceException(Constants.CODE_401, "No token provided!");
        }

        //获取token中的userid
        String userId;
        try
        {
            userId = JWT.decode(token).getAudience().get(0);
        }
        catch (JWTDecodeException j)
        {
            throw new ServiceException(Constants.CODE_402, "Invalid token!");
        }

        //根据token中的userid查询数据库，验证用户是否存在
        User user = userService.getById(userId);
        if (user == null)
        {
            throw new ServiceException(Constants.CODE_403, "User does not exist!");
        }

        //用户密码加签名验证
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
        try
        {
            jwtVerifier.verify(token);  //验证token
        }
        catch (JWTVerificationException e)
        {
            throw new ServiceException(Constants.CODE_402, "Invalid token!");  // Token验证失败（无效） - Invalid token!
        }

        //若之前所有验证都通过则予以放行
        return true;
    }
}
