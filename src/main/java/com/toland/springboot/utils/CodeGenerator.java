package com.toland.springboot.utils;

//by Toland

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;

import java.util.*;

public class CodeGenerator
{
    public static void main(String[] args)
    {
        generate();
    }

    private static void generate()
    {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/testdb?serverTimezone=GMT%2b8", "toland", "990315")
                .globalConfig(builder ->
                              {
                                  builder.author("Toland") // 设置作者
                                          .enableSwagger() // 开启 swagger 模式
                                          .fileOverride() // 覆盖已生成文件
                                          .disableOpenDir() //不打开生成目录
                                          .outputDir(
                                                  "C:\\CodeSpace\\SpingBootBackEnd\\src\\main\\java\\"); // 指定输出目录
                              })
                .packageConfig(builder ->
                               {
                                   builder.parent("com.toland.springboot") // 设置父包名
                                           .moduleName(null) // 设置父包模块名
                                           .pathInfo(Collections.singletonMap(OutputFile.mapperXml,
                                                                              "C:\\CodeSpace\\SpingBootBackEnd\\src\\main\\resources\\mapper\\")); // 设置mapperXml生成路径
                               })
                .strategyConfig(builder ->
                                {
                                    builder.addInclude("sys_user") // 设置需要生成的表名
                                            .addTablePrefix("t_", "sys_") // 设置过滤表前缀
                                            .entityBuilder().enableLombok() //开启 lombok 模型
                                            .controllerBuilder().enableRestStyle()  //开启生成@RestController控制器
                                                                .enableHyphenStyle() //开启驼峰转连字符
                                            .mapperBuilder().enableMapperAnnotation(); //开启 @Mapper 注解

                                })
                .execute();
    }
}
