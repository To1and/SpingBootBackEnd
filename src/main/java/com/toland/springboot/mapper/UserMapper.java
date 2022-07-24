package com.toland.springboot.mapper;

import com.toland.springboot.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Toland
 * @since 2022-07-25
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
