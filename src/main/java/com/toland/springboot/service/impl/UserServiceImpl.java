package com.toland.springboot.service.impl;

import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.toland.springboot.controller.dto.UserDTO;
import com.toland.springboot.entity.User;
import com.toland.springboot.mapper.UserMapper;
import com.toland.springboot.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

    @Override
    public boolean userLogin(UserDTO userDTO)
    {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        queryWrapper.eq("password", userDTO.getPassword());

        try     //当返回数据不唯一时报错
        {
            User one = getOne(queryWrapper);
            return one != null;
        }
        catch (Exception e)
        {
            LOG.error(e);
            return false;
        }
    }

}
