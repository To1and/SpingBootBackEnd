package com.toland.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import com.toland.springboot.service.IUserService;
import com.toland.springboot.entity.User;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Toland
 * @since 2022-07-25
 */


@RestController
@RequestMapping("/user")
public class UserController
{

    @Resource
    private IUserService userService;

    //实现新增或者更新数据
    @PostMapping
    public boolean saveOrUpdateInfo(@RequestBody User user)
    {
        return userService.saveOrUpdate(user);
    }

    //实现查询返回有数据
    @GetMapping
    public List<User> listAllInfo()
    {
        return userService.list();
    }

    //实现根据ID删除单个条目
    @DeleteMapping("/del/{id}")
    public boolean removeInfoById(@PathVariable Integer id)
    {
        return userService.removeById(id);
    }

    //实现根据多个ID删除多个条目
    @DeleteMapping("/del/batch")
    public boolean removeInfoByIds(@PathVariable List<Integer> ids)
    {
        return userService.removeByIds(ids);
    }

    //实现根据ID查询唯一条目
    @GetMapping("/get/{id}")
    public User getOneInfoById(@PathVariable Integer id)
    {
        return userService.getById(id);
    }

    //实现基础分页查询
    @GetMapping("/page")
    public Page<User> findPage(@RequestParam Integer pageNumber,
                                    @RequestParam Integer pageSize,
                                    @RequestParam(required = false, defaultValue = "") String username,
                                    @RequestParam(required = false, defaultValue = "") String nickname,
                                    @RequestParam(required = false, defaultValue = "") String address)
    {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();

        //此处为自定义添加的限定搜索方法
        if (!"".equals(username))
        {
        queryWrapper.like("username", username);
        }
        if (!"".equals(nickname))
        {
        queryWrapper.like("nickname", nickname);
        }
        if (!"".equals(address))
        {
        queryWrapper.like("address", address);
        }
//      queryWrapper.or().like("address", address);

        queryWrapper.orderByDesc("id");
        return userService.page(new Page<>(pageNumber,pageSize),queryWrapper);
    }

}
