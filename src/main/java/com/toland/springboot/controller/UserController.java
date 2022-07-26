package com.toland.springboot.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import com.toland.springboot.service.IUserService;
import com.toland.springboot.entity.User;
import org.springframework.web.multipart.MultipartFile;
import com.toland.springboot.controller.dto.UserDTO;


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

    //实现全部信息导出
    @GetMapping("/export")
    public void exportInfo(HttpServletResponse response) throws Exception
    {
        // 从数据库查询出所有的数据
        List<User> list = userService.list();

        // 通过工具类创建writer 写出到磁盘路径
        //ExcelWriter writer = ExcelUtil.getWriter(filesUploadPath + "/用户信息.xlsx");

        // 在内存操作，写出到浏览器
        ExcelWriter writer = ExcelUtil.getWriter(true);

        //自定义标题别名
        writer.addHeaderAlias("username", "用户名");
        writer.addHeaderAlias("password", "密码");
        writer.addHeaderAlias("nickname", "昵称");
        writer.addHeaderAlias("email", "邮箱");
        writer.addHeaderAlias("phone", "电话");
        writer.addHeaderAlias("address", "地址");
        writer.addHeaderAlias("createTime", "创建时间");
        writer.addHeaderAlias("avatarUrl", "头像");

        // 一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list, true);

        // 设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("AllUserInfo", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out, true);
        out.close();
        writer.close();
    }

//    //导入接口Way1:通过javabean的方式读取Excel内的对象，但是要求表头必须是英文，跟javabean的属性要对应起来
//    @PostMapping("/import")
//    public void importInfo(@RequestPart(value = "file") MultipartFile file) throws Exception
//    {
//        InputStream inputStream = file.getInputStream();
//        ExcelReader reader = ExcelUtil.getReader(inputStream);
//        List<User> list = reader.readAll(User.class);
//        System.out.println(list);
//    }

    //导入接口Way2:忽略表头的中文，直接读取表的内容
    @PostMapping("/import")
    public boolean importInfo(@RequestPart(value = "file") MultipartFile file) throws Exception
    {
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);

        List<List<Object>> list = reader.read(1);//忽略表头
        List<User> users = CollUtil.newArrayList();
        for (List<Object> row : list)
        {
            User user = new User();
            user.setUsername(row.get(0).toString());//对每一列哪一项属性做了具体定义，写死了
            user.setPassword(row.get(1).toString());
            user.setNickname(row.get(2).toString());
            user.setEmail(row.get(3).toString());
            user.setPhone(row.get(4).toString());
            user.setAddress(row.get(5).toString());
            user.setAvatarUrl(row.get(6).toString());
            users.add(user);
        }

        userService.saveBatch(users);
        return true;
    }

    //实现登录（@RequestBody把前端传来的json转换为后台的java对象）
    @PostMapping("/login")
    public boolean userLogin(@RequestBody UserDTO userDTO)
    {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();
        if(StrUtil.isBlank(username)||StrUtil.isBlank(password))       //利用isBlank方法判断是否为空
        {
            return false;       //为空则报错
        }

        return userService.userLogin(userDTO);      //不为空则进入验证环节
    }

}
