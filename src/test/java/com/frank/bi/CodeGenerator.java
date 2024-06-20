package com.frank.bi;

/**
 * @author Frank
 */
public class CodeGenerator {
    //public static void main(String[] args) {
    //    // 手动配置数据源
    //    // 【注意修改数据库名】
    //    String url = "jdbc:mysql://localhost:3306/frankbi?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8";
    //    String name = "root";
    //    String password = "xxxxx";
    //
    //    // 数据库表的设置
    //    // 设置需要自动代码生成的表名
    //    List<String> listTable = Arrays.asList(
    //            "chart",
    //            "user");
    //    // 过滤表的后缀
    //    List<String> listTableSuffix = Collections.singletonList("_b");
    //    // 过滤表的后缀
    //    List<String> listTablePrefix = Arrays.asList("system_", "c_");
    //
    //    // 基本信息
    //    // 作者
    //    String author = "Frank";
    //    // 父包名
    //    String parent = "com";
    //    // 模块包名
    //    String module = "frank.bi";
    //
    //    // 1. 配置数据源
    //    FastAutoGenerator.create(url, name, password)
    //
    //            // 2. 全局配置
    //            .globalConfig(builder -> {
    //                // 设置作者名
    //                builder.author(author)
    //                        // 设置输出路径：项目的 java 目录下【system.getProperty("user.dir") 意思是获取到项目所在的绝对路径】
    //                        .outputDir("F:\\Java\\Project\\FrankBI\\FrankBI-Backend" + "/src/main/java")
    //                        // 注释日期
    //                        //.commentDate("yyyy-MM-dd hh:mm:ss")
    //                        .commentDate("yyyy/MM/dd")
    //                        // 定义生成的实体类中日期的类型 TIME_PACK=LocalDateTime;ONLY_DATE=Date;
    //                        .dateType(DateType.ONLY_DATE)
    //                        // 覆盖之前的文件（deprecated）
    //                        //.fileOverride()
    //                        // 开启 swagger 模式
    //                        //.enableSwagger()
    //                        // 禁止打开输出目录，默认打开
    //                        .disableOpenDir();
    //            })
    //
    //            // 3. 包配置
    //            .packageConfig(builder -> {
    //                // 设置父包名
    //                builder.parent(parent)
    //                        // 设置模块包名
    //                        .moduleName(module)
    //                        // pojo 实体类包名
    //                        .entity("entity")
    //                        // Service 包名
    //                        .service("service")
    //                        // ***ServiceImpl 包名
    //                        .serviceImpl("service.impl")
    //                        // mapper 包名
    //                        .mapper("mapper")
    //                        // mapper/xml 包名
    //                        .xml("mapper.xml")
    //                        // controller 包名
    //                        .controller("controller");
    //                // 自定义包名(一般不在这里生成，而是后面编写的时候自己建包）
    //                //.other("config");
    //                // 配置 mapper.xml 路径信息：项目的 resources 目录下
    //                //.pathInfo(Collections.singletonMap(OutputFile.mapper.xml, system.getProperty("user.dir") + "/src/main/resources/mapper"));
    //            })
    //
    //            // 4. 策略配置
    //            .strategyConfig(builder -> {
    //                builder
    //                        // 开启大写命名
    //                        .enableCapitalMode()
    //                        // 创建实体类的时候跳过视图
    //                        .enableSkipView()
    //                        // 设置需要生成的数据表名
    //                        .addInclude(listTable)
    //                        // 设置过滤表的后缀
    //                        .addTableSuffix(listTableSuffix)
    //                        // 设置过滤表的前缀
    //                        .addTablePrefix(listTablePrefix)
    //
    //                        // 4.1. 实体类策略配置
    //                        .entityBuilder()
    //                        // 开启链式模型
    //                        .enableChainModel()
    //                        // 默认是开启实体类序列化，可以手动disable使它不序列化。由于项目中需要使用序列化就按照默认开启了
    //                        //.disableSerialVersionUID()
    //                        // 开启生成实体时生成字段注解
    //                        .enableTableFieldAnnotation()
    //                        // 开启 Lombok
    //                        .enableLombok()
    //                        //// 乐观锁字段名(数据库)
    //                        //.versionColumnName("version")
    //                        //// 乐观锁属性名(实体)
    //                        //.versionPropertyName("version")
    //                        //// 逻辑删除字段名(数据库)
    //                        //.logicDeleteColumnName("deleted")
    //                        //// 逻辑删除属性名(实体)
    //                        //.logicDeletePropertyName("deleteFlag")
    //                        // 数据库表映射到实体的命名策略：默认是下划线转驼峰命，这里可以不设置
    //                        .naming(NamingStrategy.underline_to_camel)
    //                        // 数据库表字段映射到实体的命名策略：下划线转驼峰命。（默认是和naming一致，所以也可以不设置）
    //                        .columnNaming(NamingStrategy.underline_to_camel)
    //                        // 添加表字段填充，"create_time"字段自动填充为插入时间，"update_time"字段自动填充为插入修改时间
    //                        .addTableFills(
    //                                new Column("create_time", FieldFill.INSERT),
    //                                new Column("update_time", FieldFill.INSERT_UPDATE)
    //                        )
    //                        // 设置主键自增
    //                        //.idType(IdType.AUTO)
    //
    //                        // 4.2. controller 策略配置
    //                        .controllerBuilder()
    //                        // 开启驼峰连转字符
    //                        .enableHyphenStyle()
    //                        // 格式化 Controller 类文件名称，%s进行匹配表名，如 UserController
    //                        .formatFileName("%sController")
    //                        // 开启生成 @RestController 控制器
    //                        .enableRestStyle()
    //
    //                        // 4.3. service 策略配置
    //                        .serviceBuilder()
    //                        // 格式化 service 接口文件名称，%s进行匹配表名，如 UserService
    //                        .formatServiceFileName("%sService")
    //                        // 格式化 service 实现类文件名称，%s进行匹配表名，如 UserServiceImpl
    //                        .formatServiceImplFileName("%sServiceImpl")
    //
    //                        // 4.4. Mapper策略配置
    //                        .mapperBuilder()
    //                        // 设置父类
    //                        .superClass(BaseMapper.class)
    //                        // 启用 BaseResultMap 生成
    //                        .enableBaseResultMap()
    //                        // 启用 BaseColumnList
    //                        .enableBaseColumnList()
    //                        // 格式化 mapper 文件名称
    //                        .formatMapperFileName("%sMapper")
    //                        // 开启 @Mapper 注解
    //                        .enableMapperAnnotation()
    //                        // 格式化Xml文件名称
    //                        .formatXmlFileName("%sMapperXml");
    //
    //            })
    //
    //            // 5、模板
    //            .templateEngine(new VelocityTemplateEngine())
    //            /*
    //                模板引擎配置，默认 Velocity 可选模板引擎 Beetl 或 Freemarker(以下两个引擎用哪个就保留哪个)
    //               .templateEngine(new BeetlTemplateEngine())
    //               .templateEngine(new FreemarkerTemplateEngine())
    //             */
    //            .templateEngine(new FreemarkerTemplateEngine())    // 本人选择了Freemarker
    //
    //            // 6. 执行
    //            .execute();
    //}
}
