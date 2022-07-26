package com.toland.springboot.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.toland.springboot.common.Constants;
import com.toland.springboot.controller.dto.UserDTO;
import com.toland.springboot.entity.User;
import com.toland.springboot.exception.ServiceException;
import com.toland.springboot.mapper.UserMapper;
import com.toland.springboot.service.IUserService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Toland
 * @since 2022-07-25
 */


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService
{
    private static final Log LOG = Log.get();

    //实现登录方法
    @Override
    public UserDTO userLogin(UserDTO userDTO)
    {

        User one = getUserInfo(userDTO);

        //业务处理错误
        if (one != null)
        {
            BeanUtil.copyProperties(one, userDTO, true);//将从数据库查询出的用户信息copy至参数对象中
            return userDTO;
        }
        else
        {
            throw new ServiceException(Constants.CODE_600, "Username or password Error!");  //当返回数据与数据库中数据不一致时报错
        }
    }

    //实现注册方法
    @Override
    public User userRegister(UserDTO userDTO)
    {
        User one = getUserInfo(userDTO);
        if (one == null)
        {
            one = new User();
            BeanUtil.copyProperties(userDTO, one, true);    //将用户输入的用户信息copy至参数对象中
            save(one);  //把存有用户数据的参数对象存入数据库
        }
        else
        {
            throw new ServiceException(Constants.CODE_600, "Username already exists!");  //当用户名与数据库中某数据相同时报错
        }
        return null;
    }

    //将登录和注册中相同的方法封装为一个新的独立的方法，使代码更简洁
    private User getUserInfo(UserDTO userDTO)
    {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());
        User one;

        try
        {
            one = getOne(queryWrapper);
        }
        catch (Exception e)
        {
            LOG.error(e);
            throw new ServiceException(Constants.CODE_500, "System Error!");    //当返回数据不唯一时报错
        }
        return one;
    }
}