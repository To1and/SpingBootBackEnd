# I. 前期配置

+ ## Maven配置阿里云仓库（加快依赖下载速度）

	```xml
	<repositories>
	    <repository>
	        <id>nexus-aliyun</id>
	        <name>nexus-aliyun</name>
	        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
	        <releases>
	            <enabled>true</enabled>
	        </releases>
	        <snapshots>
	            <enabled>false</enabled>
	        </snapshots>
	    </repository>
	</repositories>
	
	<pluginRepositories>
	    <pluginRepository>
	        <id>public</id>
	        <name>aliyun nexus</name>
	        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
	        <releases>
	            <enabled>true</enabled>
	        </releases>
	        <snapshots>
	            <enabled>false</enabled>
	        </snapshots>
	    </pluginRepository>
	</pluginRepositories>
	```


+ ## 用YML替代properties配置数据源

    ```yaml
    server:
      port: 9090
    
    spring:
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: jdbc:mysql://localhost:3306/testdb?serverTimezone=GMT%2b8
        username: toland
        password: 990315
    ```

---

# II. 集成MyBatis实现数据查询

+ ## 使用方法
1. __利用Postman，向localhost:9090/user发送一个GET请求。返回值为该表内全部数据的json串形式。__

+ ## ___com.toland.springboot.entity	User.java___

  1. 定义与数据库中每个字段对应的私有变量

  2. ~~使用___Alt+ Insert___一键生成Get和Set方法，用以获取查询数据~~（会生成大量冗余代码）

  3. 在User类前加上注解___@Data___，便可简单取代2所有的代码（使用了__Lombok__插件)

     ```java
     @Data
     public class User
     {
         private Integer id;
         private String username;
         private String password;
         private String nickname;
         private String email;
         private String phone;
         private String address;
     }
     ```

+ ## ___com.toland.springboot.mapper	UserMapper.java___

   1. 创建一个找到全部字段的查询接口，其上加入___@Mapper___注解，<u>**实现了把UserMapper内的Bean注入到Spring Boot中**</u>

      ```java
      @Mapper
      public interface UserMapper
      {
          @Select("SELECT * FROM sys_user")
          List<User> findAll();
      }
      ```

+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 使用***@Autowired***注入UserMapper

  2. 写一个测试方法，使用***@GetMapping***（依赖于***@RestController***）对其进行路由
  
     ```java
     @RestController
     public class UserController
     {
         @Autowired
         private UserMapper userMapper;
     
         @GetMapping("/")
         public List<User> index()
         {
             return userMapper.findAll();
         }
     }
     
     ```

---

# III. 实现增删改查

+ ## 使用方法
  1. __利用Postman，向localhost:9090/user这个地址POST一个json串，若是库中不包含的id则新增条目，若已包含则更新。返回操作的条目数__
  2. __利用Postman，对localhost:9090/user/{id}这个地址DELETE。返回操作的条目数__

+ ## ___com.toland.springboot.mapper	UserMapper.java___

  1. 使用___@Insert___注解实现向数据库插入条目

     ```java
     @Insert("INSERT INTO sys_user(username,password,nickname,email,phone,address) VALUES (#{username}, #{password}, #{nickname},#{email}, #{phone}, #{address})")
     int insert(User user);
     ```

  2. ~~使用___@Update___注解实现对数据库条目的更新。~~其交互方式为使用Postman向localhost:9090/user POST一个json串，在id已知的情况下替换数据。但是这种情况会导致假如在只更新个别数据的时候，会使其他没有更新的数据被清除，逻辑不完善,使用动态SQL进行完善，最终会将@Update注解完全删除

     ```java
     @Update("UPDATE sys_user SET username=#{username}, password=#{password},nickname=#{nickname},email=#{email},phone=#{phone},address=#{address} WHERE id=#{id}")
     int update(User user);
     ```

  3. 使用@Delete注解实现通过id检索删除条目

     ```java
     @Delete("DELETE FROM sys_user WHERE id = #{id}")   //这个id与下一行的id一一对应
     Integer deleteById(@Param("id") Integer id);
     ```


+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 在类名前添加___@RequestMapping___，得到的整个url需为类前的___@RequestMapping___的参数与类内的***@GetMapping***的参数所拼接的总体。***@GetMapping***不附带任何内容时注解的方法即为直接访问___@RequestMapping___的参数的返回结果。

  2. 追加新增或修改方法。使用___@RequestBody___，可以将json数据映射为User对象

     ```java
     @PostMapping
     public Integer save(@RequestBody User user)     //实现新增或者更新数据
     {
         return userService.save(user);      //返回操作的条目数
     }
     ```

  3. 追加删除条目方法。___@PathVariable___表示url参数

     ```java
     @DeleteMapping("/{id}") //这个id与下一行的id一一对应
     public Integer delete(@PathVariable Integer id) //   //实现删除数据
     {
         return userMapper.deleteById(user.getId(id));   //返回删除的条目数
     }
     ```

+ ## ___com.toland.springboot.Service	UserService.java___

  1. 类前加注解***@Service***，用来吧Service类注入到Spring Boot容器中，类似___UserMapper.java__中的***@Mapper***。

  2. 使用一个if语句来判断所使用的方法为新增还是更新

     ```java
     public int save(User user)
     {
         if (user.getId() == null)    //如果user没有id说明为新增内容,需要添加
         {
             return userMapper.insert(user);
         }
         else    //否则需要更新内容
         {
             return userMapper.update(user);
         }
     }
     ```

+ ## ___resources.mapper	User.xml___

  1. 首先是网上找到配置头模板，注意namespace需要定位到自己的UserMapper接口

  	```xml
     <?xml version="1.0" encoding="UTF-8" ?>
     <!DOCTYPE mapper
             PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
             "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
     <mapper namespace="com.toland.springboot.mapper.UserMapper">
         
     </mapper>

  2. 在插件商店内下载安装__MyBatisX__，用以快捷在左侧切换到UserMapper接口

  3. 利用动态SQL做一个判断逻辑，这里的逻辑是不会将空数据写入服务器，即任何值都不能在写入后再置空。id内的内容对应UserMapper中方法的名字。

  	```xml
         <update id="update">
             UPDATE sys_user
             <set>
                 
                 <if test="username!=null">
                     username=#{username},
                 </if>
     
     <!--            <if test="password!=null">          一般来说密码不在SQL里修改，比较危险-->
     <!--                password=#{password},-->
     <!--            </if>-->
     
                 <if test="nickname!=null">
                     nickname=#{nickname},
                 </if>
     
                 <if test="email!=null">
                     email=#{email},
                 </if>
     
                 <if test="phone!=null">
                     phone=#{phone},
                 </if>
     
                 <if test="address!=null">
                     address=#{address},
                 </if>
                 
             </set>
                 
             <where>
                 id = #{id}
             </where>
     
         </update>
     ```

  4. 实现前其他改动

     + 在UserMapper中将id对应方法前的注解删除，即删除___@Update___行

     + 在application.yml中配置MyBatis类地址，使得框架可以寻找到该文件。configuration中的内容为了在终端输出日志，其中包含实际运行的SQL语句以便观察

        ```yaml
        mybatis:
            mapper-locations: classpath:mapper/*.xml
            configuration:
                log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
        ```

---

# IV. 实现分页查询

+ ## 使用方法

  1. 
  
+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 追加分页查询方法。使用___@RequestParam___，其接口路径为_/user/page?pageNumber=1&pageSize=10_。

     ```java
     @GetMapping("/page")
     public List<User> findPage(@RequestParam Integer pageNumber, @RequestParam Integer pageSize)    //实现分页查询，接收页面数与页面大小两个数据
     {   //limit的第一个参数 = (pageNumber - 1) * pageSize,其原理来源于MySQL语句
         //limit的第二个参数 = pageSize
         pageNumber = pageNumber - 1 * pageSize;
         userMapper.selectPage(pageNumber, pageSize)
     }
     ```

  2. 对以上方法进行扩充，改写方法，扩充一个返回值代表总条目数

     ```java
     @GetMapping("/page")   //接口路径：/user/page?pageNumber=1&pageSize=10
     public Map<String, Object> findPage(@RequestParam Integer pageNumber, @RequestParam Integer pageSize)    //实现分页查询，接收页面数与页面大小两个数据
     {
         pageNumber = (pageNumber - 1) * pageSize;//limit的第一个参数 = (pageNumber - 1) * pageSize,其原理来源于MySQL语句
     
         List<User> data = userMapper.selectPage(pageNumber, pageSize);//获得查询信息
     
         Integer total = userMapper.selectTotal();//获得总条目数量
     
         Map<String, Object> res = new HashMap<>();
         res.put("data", data);
         res.put("total", total);
     
         return res;
     ```

+ ## ___com.toland.springboot.mapper	UserMapper.java___

  1. 使用@Select实现分页查询

     ```java
     @Select("SELECT * FROM sys_user LIMIT #{pageNumber},#{pageSize}")
     List<User> selectPage(Integer pageNumber,Integer pageSize);
     ```

  2. 使用@Select实现获取总条目数

     ```java
     @Select("SELECT COUNT(*) FROM sys_user")
     Integer selectTotal();
     ```

+ ## ___com.toland.springboot.config	CorsConfig.java___

  1. 编写一个类用于实现跨域

     ```java
     @Configuration
     public class CorsConfig
     {
         //跨域请求最大有效时长，此处默认为为1天
         private static final long MAX_AGE = 24 * 60 * 60;
     
         @Bean
         public CorsFilter corsFilter()
         {
             UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
             CorsConfiguration corsConfiguration = new CorsConfiguration();
             corsConfiguration.addAllowedOrigin("http://localhost:8080");//设置访问源地址
             corsConfiguration.addAllowedHeader("*");//设置访问源请求头
             corsConfiguration.addAllowedMethod("*");//设置访问源请求方法
             corsConfiguration.setMaxAge(MAX_AGE);
             source.registerCorsConfiguration("/**", corsConfiguration);//对接口配置跨域设置
             return new CorsFilter(source);
         }
     }
     ```

---

# V. 集成Mybatis-Plus和SwaggerUI

+ ## 前置工作

  1. 在pom.xml添加Mybatis-Plus依赖

     ```xml
     <dependency>
         <groupId>com.baomidou</groupId>
         <artifactId>mybatis-plus-boot-starter</artifactId>
         <version>最新版本</version>
     </dependency>
     ```

  2. 由于SQL已经被MybatisPlus接管，在application.yml中需要更换配置，更改后为

     ```yaml
     mybatis:
         mapper-locations: classpath:mapper/*.xml
     #    configuration:
     #        log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
     
     mybatis-plus:
         configuration:
             log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
     ```

     

+ ## ___com.toland.springboot.config	MybatisPlusConfig___.java

  1. 添加一个配置类用以配置MybatisPlus，在这里使用了___@MapperScan___这个注解，便可以替代，即删除___UserMapper.java___中的@Mapper注解
  
     ```java
     @Configuration
     @MapperScan("com.toland.springboot.mapper")
     public class MybatisPlusConfig
     {
         @Bean
         public MybatisPlusInterceptor mybatisPlusInterceptor() {
             MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
             interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
             return interceptor;
         }
     
     }
     ```

+ ## ___com.toland.springboot.Service	UserService.java___

  1. 声明一个继承关系

     ```java
     @Service
     public class UserService extends ServiceImpl<UserMapper, User>
     {
         ...
     }
     ```

  2. 由于继承的ServiceImpl类中已有save方法，所以需要将其重写，注释掉当前该类内所有内容。

  3. 新增saveUser方法作为其替代，且该方法内使用了来自于ServiceImpl父类内的saveOrUpdate方法，作用等同于之前的insert和update方法的组合

     ```java
     public boolean saveUser(User user)
     {
         return saveOrUpdate(user);//使用MyBatisPlus提供的方法，返回值为布尔型，可判断成功失败
     }
     ```

     4.在后面利用ServiceImpl类内的方法更新了UserController内的所有方法后，可以将UserService类内所有方法删除

+ ## ___com.toland.springboot.mapper	UserMapper.java___

  1. 声明一个继承关系

     ```java
     public interface UserMapper extends BaseMapper<User>
     {
     	...
     }
     ```

  2. 4.在后面利用ServiceImpl类内的方法更新了UserController内的所有方法后，可以将UserMapper类内所有方法删除

+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 使用UserService继承的ServiceImpl类中的方法，更新saveOrUpdateUserInfo方法

     ```java
         //实现新增或者更新数据
         @PostMapping
         public boolean saveOrUpdateUserInfo(@RequestBody User user)
         {
             return userService.saveOrUpdate(user);
         }
     ```

  2. 使用UserService继承的ServiceImpl类中的方法，更新listAllUserInfo方法

     ```java
         //实现查询返回有数据
         @GetMapping
         public List<User> listAllUserInfo()
         {
             return userService.list();
         }
     ```

  3. 使用UserService继承的ServiceImpl类中的方法，更新deleteUserInfoById方法

     ```java
         //实现删除特定ID数据
         @DeleteMapping("/{id}")
         public boolean deleteUserInfoById(@PathVariable Integer id)
         {
             return userService.removeById(id);
         }
     ```

  4. 使用UserService继承的ServiceImpl类中的方法，更新findPage方法

     ```java
     //实现分页查询-MyBatisPlus实现
     @GetMapping("/page") 
     public IPage<User> findPage(@RequestParam Integer pageNumber,
                                 @RequestParam Integer pageSize,
                                 @RequestParam String username)
     {
         IPage<User> page = new Page<>(pageNumber, pageSize);
         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         queryWrapper.like("username", username);    //模糊搜索
         return userService.page(page,queryWrapper);
     }
     ```

  5. 对findPage方法进行拓展。使用__queryWrapper.like()__添加所需要搜索的词条种类，则能实现多词条的AND搜索，即得到的结果为同时满足所有条件的记录。AND方法为默认方法，该方法等于__queryWrapper.and().like()__

     ```java
     //实现分页查询-MyBatisPlus实现
     @GetMapping("/page")  
     public IPage<User> findPage(@RequestParam Integer pageNumber,
                                 @RequestParam Integer pageSize,
                                 @RequestParam String username,
                                 @RequestParam String nickname,
                                 @RequestParam String address)
     {
         IPage<User> page = new Page<>(pageNumber, pageSize);
         QueryWrapper<User> queryWrapper = new QueryWrapper<>();
         queryWrapper.like("username", username);    //模糊搜索
         queryWrapper.like("nickname", nickname);
         queryWrapper.like("address", address);      //增加搜索词条种类，则能实现多词条的AND搜索，即得到的结果为同时满足所有条件的记录
     
         return userService.page(page,queryWrapper);
     }
     ```

  6. 对findPage方法进一步进行拓展。使用__queryWrapper.or().like()__添加所需要搜索的词条种类，则能实现多词条的OR搜索，即得到的结果为任意满足or()方法或同时满足所有默认方法的记录

     ```java
     //    queryWrapper.or().like("address", address);
     ```

  7. 优化方法，在类参数表内加入默认值设置，并添加是否为空判断逻辑。以便当不对某项值进行限定时，即返回空值时也能正常依据其他传入参数搜索

     ```java
         //实现分页查询-MyBatisPlus实现
         @GetMapping("/page")
         public IPage<User> findPage(@RequestParam Integer pageNumber,
                                     @RequestParam Integer pageSize,
                                     @RequestParam(required = false, defaultValue = "") String username,
                                     @RequestParam(required = false, defaultValue = "") String nickname,
                                     @RequestParam(required = false, defaultValue = "") String address)
                                     //在类参数表内加入默认值设置，以便当不对某项值进行限定时，即返回空值时也能正常依据其他传入参数搜索
         {
             IPage<User> page = new Page<>(pageNumber, pageSize);
             QueryWrapper<User> queryWrapper = new QueryWrapper<>();
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
             
             return userService.page(page, queryWrapper);
         }
     ```

  8. 将UserMapper的注入注解删除，便可正常运行

     ```java
     //    @Autowired
     //    private UserMapper userMapper;
     ```

+ ## ___com.toland.springboot.entity	User.java___

  1. 在类前加注解___@TableName___用于指定对应数据库中查询的表名。

     ```java
     @Data
     @TableName(value = "sys_user")
     public class User
     {
        ...
     }
     ```
  
  2. 在id前加注解___@TableId___用于标识表的主键。此时这个被注解的变量的名字则可以任意变化，而仍能使其对应与数据库中的id键，假如此时将声明的变量id变为userid，加上这条注解使用效果没有变化。
  
     ```java
     @TableId(value = "id")
     private Integer id;
     ```
  
  3. 在任意键值前使用注解___@TableField___用以对非主键实现类似第2条中的前半部分功能。例如这个avatar的声明，在postman中的json串前的词条标识，可只用avatar来对应数据库中的avatar_url词条。下半部分代码为POST的json串。
  
     ```java
     @TableField(value = "avatar_url")//数据库中的字段名称
     private String avatar;//该字段在项目中的别名
     ```
  
        ```json
        {
            "userid":1,
            "avatar":"123123"
        }
        ```
  
  4. 假如即便不使用___@TableField___这个注解，MyBatisPlus也会将驼峰自动与下划线进行转换，用如下形式的代码传入如下的json串同样可映射avatar_url这个字段
  
     ```java
     //@TableField(value = "avatar_url")//数据库中的字段名称
     private String avatarUrl;//该字段在项目中的别名
     ```

        ```json
        {
            "userid":1,
            "avatarUrl":"123123"
        }
        ```

+ ## ___使用Swagger-UI___

  1. 在pom.xml依赖配置

     ```xml
             <!--swagger-->
             <dependency>
                 <groupId>io.springfox</groupId>
                 <artifactId>springfox-boot-starter</artifactId>
                 <version>3.0.0</version>
             </dependency>
     ```

  2. 在创建好___SwaggerConfig.java___之后访问http://localhost:9090/swagger-ui/index.html即可进入Swagger-UI主页调试接口（具体地址和端口需要因地制宜）

+ ## ___com.toland.springboot.config	SwaggerConfig.java___

  1. 新建___SwaggerConfig.java___作为Swagger的配置类

     ```java
     package com.toland.springboot.config;
     
     import org.springframework.context.annotation.Bean;
     import org.springframework.context.annotation.Configuration;
     import springfox.documentation.builders.ApiInfoBuilder;
     import springfox.documentation.builders.PathSelectors;
     import springfox.documentation.builders.RequestHandlerSelectors;
     import springfox.documentation.service.ApiInfo;
     import springfox.documentation.service.Contact;
     import springfox.documentation.spi.DocumentationType;
     import springfox.documentation.spring.web.plugins.Docket;
     import springfox.documentation.oas.annotations.EnableOpenApi;
     
     @EnableOpenApi
     @Configuration
     public class SwaggerConfig
     {
     
         /**
          * 创建API应用
          * apiInfo() 增加API相关信息
          * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
          * 本例采用指定扫描的包路径来定义指定要建立API的目录。
          */
     
         @Bean
         public Docket restApi()
         {
             return new Docket(DocumentationType.SWAGGER_2)
                     .groupName("Standard Interface")
                     .apiInfo(apiInfo("Spring Boot-Vue Study Project Interface test APIs", "1.0"))
                     .useDefaultResponseMessages(true)
                     .forCodeGeneration(false)
                     .select()
                     .apis(RequestHandlerSelectors.basePackage("com.toland.springboot.controller"))
                     .paths(PathSelectors.any())
                     .build();
         }
     
         /**
          * 创建该API的基本信息（这些基本信息会展现在文档页面中）
          * 访问地址：http://localhost:9090/swagger-ui/index.html
          */
         private ApiInfo apiInfo(String title, String version)
         {
             return new ApiInfoBuilder()
                     .title(title)
                     .description("")
                     .termsOfServiceUrl("https://github.com/To1and/SpringBoot-Vue")
                     .contact(new Contact("Toland", "https://github.com/To1and", "zyzytoland@gmail.com"))
                     .version(version)
                     .build();
         }
     }
     ```

---

# VI. 代码生成器

+ ## 设置依赖

  1. MyBatis Plus Generator

     ```xml
             <!--MyBatis Plus Generator-->
             <dependency>
                 <groupId>com.baomidou</groupId>
                 <artifactId>mybatis-plus-generator</artifactId>
                 <version>3.5.1</version>
             </dependency>
     ```

  2. 引入模板引擎Velocity

     ```xml
         <!-- https://mvnrepository.com/artifact/org.apache.velocity/velocity-engine-core -->
         <dependency>
             <groupId>org.apache.velocity</groupId>
             <artifactId>velocity-engine-core</artifactId>
             <version>2.3</version>
         </dependency>
     </dependencies>
     ```

+ ## ___com.toland.springboot.utils	CodeGenerator.java___

  1. 编写配置代码生成器，这部分内容需要按需配置，我参考了MyBatisPlus给出的文档有如下设置

     ```java
     package com.toland.springboot.utils;
     
     //by Toland
     
     import com.baomidou.mybatisplus.generator.FastAutoGenerator;
     import com.baomidou.mybatisplus.generator.config.OutputFile;
     
     import java.util.*;
     
     public class CodeGenerator
     {
         public static void main(String[] args)
         {
             gengerate();
         }
     
         private static void gengerate()
         {
             FastAutoGenerator.create("jdbc:mysql://localhost:3306/testdb?serverTimezone=GMT%2b8", "toland", "990315")
                     .globalConfig(builder ->
                                   {
                                       builder.author("Toland") // 设置作者
                                               .enableSwagger() // 开启 swagger 模式
                                               .fileOverride() // 覆盖已生成文件
                                               .disableOpenDir() //不打开生成目录
                                               .outputDir(
                                                       "C:\\CodeSpace\\SpringBoot-Vue Study Project\\src\\main\\java\\"); // 指定输出目录
                                   })
                     .packageConfig(builder ->
                                    {
                                        builder.parent("com.toland.springboot") // 设置父包名
                                                .moduleName(null) // 设置父包模块名
                                                .pathInfo(Collections.singletonMap(OutputFile.mapperXml,
                                                                                   "C:\\CodeSpace\\SpringBoot-Vue Study Project\\src\\main\\resources\\mapper\\")); // 设置mapperXml生成路径
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
     
     ```

  2. 运行该主函数，生成成功后删除原有项目里面没有被覆盖掉的的所有方法

  3. 在外部库中找到___com\baomidou\mybatis-plus-generator\3.5.1\mybatis-plus-generator-3.5.1.jar!\templates\controller.java.vm___，并将其复制到___src/main/resources/templates___中进行修改，移植生成代码前___UserController___中的方法，此为生成**UserController**类的模板。这部分代码生成非常繁琐和复杂，有大量需要精细化修改的地方，为了获得满意的效果花了我两个多小时小时，这个模板可以直接生成一个附带一些基础方法的Controller。

     ```java
     package ${package.Controller};
     
     import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
     import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
     import org.springframework.stereotype.Controller;
     import org.springframework.web.bind.annotation.*;
     import javax.annotation.Resource;
     import java.util.List;
     import $!{package.Service}.$!{table.serviceName};
     import ${package.Entity}.${entity};
     
     #if(${superControllerClassPackage})
     import ${superControllerClassPackage};
     #end
     
     /**
      * <p>
      * $!{table.comment} 前端控制器
      * </p>
      *
      * @author ${author}
      * @since ${date}
      */
     
     
     #if(${restControllerStyle})
     @RestController
     #else
     @Controller
     #end
     @RequestMapping("#if(${package.ModuleName})/${package.ModuleName}#end/#if(${controllerMappingHyphenStyle})${controllerMappingHyphen}#else${table.entityPath}#end")
     #if(${kotlin})
     class ${table.controllerName}#if(${superControllerClass}) : ${superControllerClass}()#end
     
     #else
         #if(${superControllerClass})
     public class ${table.controllerName} extends ${superControllerClass}
     {
         #else
     public class ${table.controllerName}
     {
         #end
     
         @Resource
         private ${table.serviceName} ${table.entityPath}Service;
     
         //实现新增或者更新数据
         @PostMapping
         public boolean saveOrUpdateInfo(@RequestBody ${entity} ${table.entityPath})
         {
             return ${table.entityPath}Service.saveOrUpdate(${table.entityPath});
         }
     
         //实现查询返回有数据
         @GetMapping
         public List<${entity}> listAllInfo()
         {
             return ${table.entityPath}Service.list();
         }
     
         //实现根据ID删除单个条目
         @DeleteMapping("/del/{id}")
         public boolean removeInfoById(@PathVariable Integer id)
         {
             return ${table.entityPath}Service.removeById(id);
         }
     
         //实现根据多个ID删除多个条目
         @DeleteMapping("/del/batch")
         public boolean removeInfoByIds(@PathVariable List<Integer> ids)
         {
             return ${table.entityPath}Service.removeByIds(ids);
         }
     
         //实现根据ID查询唯一条目
         @GetMapping("/get/{id}")
         public ${entity} getOneInfoById(@PathVariable Integer id)
         {
             return ${table.entityPath}Service.getById(id);
         }
     
         //实现基础分页查询
         @GetMapping("/page")
         public Page<${entity}> findPage(@RequestParam Integer pageNumber,
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
             return ${table.entityPath}Service.page(new Page<>(pageNumber,pageSize),queryWrapper);
         }
     
     }
     #end
     ```

+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 此时检查完全自动生成的代码，实现了之前手写的代码的功能。其他的各种接口和实体类也被覆盖，这里不再列出

     ```java
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
      * @since 2022-07-24
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
     
     ```

---

# VII.利用Hutool实现数据导入导出

+ ## 前期配置

  1. 配置依赖

     ```xml
     <!--hutool-->
         <dependency>
             <groupId>cn.hutool</groupId>
             <artifactId>hutool-all</artifactId>
             <version>5.8.3</version>
         </dependency>
         <dependency>
             <groupId>org.apache.poi</groupId>
             <artifactId>poi-ooxml</artifactId>
             <version>5.2.2</version>
         </dependency>
     ```

  2. 接口测试可简单的在Swagger-UI中，用importInfo方法上传文件来实现向数据库导入数据，或用exportInfo方法下载包含所有数据库信息的文件

+ ## ___com.toland.springboot.entity	User.java___

  1. 在类名前添加@ToString注解，这是一个Lombok提供的注解，用于代替大量toString方法的撰写。

+ ## ___com.toland.springboot.Controller	UserController.java___

  1. 对于导出，在文件内添加方法exportInfo。使用list方法获取所有数据后，通过输出流写入.xlsx，可以选择直接写入到特定磁盘路径，或是写出到浏览器供用户下载

     ```java
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
     ```

  2. 对于导入的第一种方法，使用了javabean的方式读取xlsx文件内的对象，但是要求表头必须要和javabean的属性对应起来，优点是方法比较灵活，可以在不加巨大改动的情况下应用于多种情况

     ```java
     @PostMapping("/import")
     public void importInfo(@RequestPart(value = "file") MultipartFile file) throws Exception
     {
         InputStream inputStream = file.getInputStream();
         ExcelReader reader = ExcelUtil.getReader(inputStream);
         List<User> list = reader.readAll(User.class);
         System.out.println(list);
     }
     ```

  3. 对于导入的第二种方法，使用特别定义的方法忽略了表头的内容而直接读取表的内容，优点不受表头内容的影响，缺点是对每一张表都需要重写过滤策略

     ```java
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
     ```

---

# VIII. 实现用户登录

+ ## ___com.toland.springboot.controller.dto     UserDTO.java___

  1. 在controller/dto中加入一个___UserDTO.java___用于存放接收前端请求的参数

     ```java
     import lombok.Data;
     
     /**
      * 接收前端登录请求的参数
      */
     @Data
     public class UserDTO
     {
         private String username;
         private String password;
     }
     ```

+ ## ___com.toland.springboot.service     IUserService.java___

  1. 在服务类___IUserService.java___定义一个userLogin方法

     ```java
     boolean userLogin(UserDTO userDTO);
     ```

+ ## ___com.toland.springboot.service.impl     UserServiceImpl.java___

  1. 在实现类UserServiceImpl.java中实现userLogin方法

     ```java
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
     ```

+ ## ___com.toland.springboot.controller     UserController.java___

  1. 在控制类中对传回数据做非空验证，并中转

     ```java
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
     ```

---

# IIX. 实现用户注册和异常处理

+ ## ___com.toland.springboot.common     Result.java___

  1. 编写包装类，实现返回结果的统一



---

# IX. 集成JSON Web Tokens

+ ## 前期准备

  1. 添加JSON Web Tokens依赖

     ```xml
     <!--java-jwt -->
     <dependency>
         <groupId>com.auth0</groupId>
         <artifactId>java-jwt</artifactId>
         <version>3.10.3</version>
     </dependency>
     ```

+ ## 
