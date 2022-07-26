package com.toland.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.toland.springboot.entity.User;
import com.toland.springboot.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class TokenUtils
{
    @Autowired
    private static IUserService staticUserService;

    @PostConstruct
    public void setUserService()
    {
        staticUserService = userService;
    }

    private IUserService userService;

    /*生成Token*/
    public static String generateToken(String userId, String signature)
    {
        return JWT.create().withAudience(userId).withExpiresAt(DateUtil.offsetHour(new Date(), 2))
                  .sign(Algorithm.HMAC256(signature));
    }

    /*获取当前登录的用户信息*/
    public static User getCurrentUser()
    {
        try
        {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader("token");
            if (StrUtil.isNotBlank(token))
            {
                String userId = JWT.decode(token).getAudience().get(0);
                return staticUserService.getById(Integer.valueOf(userId));
            }
        }
        catch (Exception e)
        {
            return null;
        }
        return null;
    }

}


