package com.toland.springboot.service.impl;

import com.toland.springboot.entity.User;
import com.toland.springboot.mapper.UserMapper;
import com.toland.springboot.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Toland
 * @since 2022-07-25
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
