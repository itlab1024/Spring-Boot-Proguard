# 什么是代码混淆？
就是将代码的通过工具，使其可读性变差（越差越好😄）
# Proguard是什么？
官网地址：https://www.guardsquare.com/proguard
该工具主要是为了实现Java以及Android App的代码混淆工作。
从官网的说明可以地址，该公司主要做手机应用保护的。proguard的使用手册是https://www.guardsquare.com/manual/home。
guard官方其实并没有提供maven的插件依赖，官网默认的是gradle，不过官方推荐了两个maven插件
[https://github.com/wvengen/proguard-maven-plugin](https://github.com/wvengen/proguard-maven-plugin)
[https://github.com/dingxin/proguard-maven-plugin](https://github.com/dingxin/proguard-maven-plugin)

# 本文目标

本文并不是介绍proguard如何使用（proguard有standalone模式，可以直接使用proguard.sh命令行进行混淆代码），而是使用wvengen的proguard-maven-plugin插件实现将一个spring boot项目进行混淆。

# 准备项目

我这里使用的是Spring Boot 2.7.x，JDK使用的是8.
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202210151723950.png)
# 混淆前
首先打包看下未混淆前打包，查看反编译后的文件，这里需要使用反编译工具，可以去https://github.com/java-decompiler/jd-gui/releases下载。
打包：使用mvn clean package -DskipTests 或者直接使用IDEA的Build工具都可以。
```shell
#非必须选项，因为我电脑上安装多个版本的JDK。而环境变量中配置的不是jdk1.8，所有需要执行如下命令。
➜  Spring-Boot-Proguard git:(main) ✗ export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_333.jdk/Contents/Home
#我这里使用的是./mvnw，用mvn也可（只要你的电脑配置了环境变量，如果没配置，就可以使用spring boot项目下的mvnw）
➜  Spring-Boot-Proguard git:(main) ✗ ./mvnw clean package -DskipTestclears
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------< com.itlab1024:Spring-Boot-Proguard >-----------------
[INFO] Building Spring-Boot-Proguard 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ Spring-Boot-Proguard ---
^C%                                                                                                                ➜  Spring-Boot-Proguard git:(main) ✗ 
➜  Spring-Boot-Proguard git:(main) ✗ ./mvnw clean package -DskipTests     
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------------< com.itlab1024:Spring-Boot-Proguard >-----------------
[INFO] Building Spring-Boot-Proguard 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.2.0:clean (default-clean) @ Spring-Boot-Proguard ---
[INFO] Deleting /Users/itlab/workspace/github/Spring-Boot-Proguard/target
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:resources (default-resources) @ Spring-Boot-Proguard ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] Copying 1 resource
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.10.1:compile (default-compile) @ Spring-Boot-Proguard ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /Users/itlab/workspace/github/Spring-Boot-Proguard/target/classes
[INFO] 
[INFO] --- maven-resources-plugin:3.2.0:testResources (default-testResources) @ Spring-Boot-Proguard ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Using 'UTF-8' encoding to copy filtered properties files.
[INFO] skip non existing resourceDirectory /Users/itlab/workspace/github/Spring-Boot-Proguard/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.10.1:testCompile (default-testCompile) @ Spring-Boot-Proguard ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to /Users/itlab/workspace/github/Spring-Boot-Proguard/target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ Spring-Boot-Proguard ---
[INFO] Tests are skipped.
[INFO] 
[INFO] --- maven-jar-plugin:3.2.2:jar (default-jar) @ Spring-Boot-Proguard ---
[INFO] Building jar: /Users/itlab/workspace/github/Spring-Boot-Proguard/target/Spring-Boot-Proguard-0.0.1-SNAPSHOT.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.7.4:repackage (repackage) @ Spring-Boot-Proguard ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  4.584 s
[INFO] Finished at: 2022-10-15T17:31:08+08:00
[INFO] ------------------------------------------------------------------------
```
完成后，我们target下就能看到打好的jar文件。使用jd-gui打开查看。
![](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202210151734967.png)
可以看到，代码没有被混淆。
# 代码混淆
接下来我使用proguard实现代码混淆。主要是在pom中使用插件来实现。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.itlab1024</groupId>
    <artifactId>Spring-Boot-Proguard</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Spring-Boot-Proguard</name>
    <description>Spring-Boot-Proguard</description>
    <properties>
        <java.version>1.8</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.wvengen</groupId>
                <artifactId>proguard-maven-plugin</artifactId>
                <version>2.6.0</version>
                <executions>
                    <!-- 以下配置说明执行mvn的package命令时候，会执行proguard-->
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>proguard</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <!-- 就是输入Jar的名称，我们要知道，代码混淆其实是将一个原始的jar，生成一个混淆后的jar，那么就会有输入输出。 -->
                    <injar>${project.build.finalName}.jar</injar>
                    <!-- 输出jar名称，输入输出jar同名的时候就是覆盖，也是比较常用的配置。 -->
                    <outjar>${project.build.finalName}.jar</outjar>
                    <!-- 是否混淆 默认是true -->
                    <obfuscate>true</obfuscate>
                    <!-- 配置一个文件，通常叫做proguard.cfg,该文件主要是配置options选项，也就是说使用proguard.cfg那么options下的所有内容都可以移到proguard.cfg中 -->
                    <proguardInclude>${project.basedir}/proguard.cfg</proguardInclude>
                    <!-- 额外的jar包，通常是项目编译所需要的jar -->
                    <libs>
                        <lib>${java.home}/lib/rt.jar</lib>
                        <lib>${java.home}/lib/jce.jar</lib>
                        <lib>${java.home}/lib/jsse.jar</lib>
                    </libs>
                    <!-- 对输入jar进行过滤比如，如下配置就是对META-INFO文件不处理。 -->
                    <inLibsFilter>!META-INF/**,!META-INF/versions/9/**.class</inLibsFilter>
                    <!-- 这是输出路径配置，但是要注意这个路径必须要包括injar标签填写的jar -->
                    <outputDirectory>${project.basedir}/target</outputDirectory>
                    <!--这里特别重要，此处主要是配置混淆的一些细节选项，比如哪些类不需要混淆，哪些需要混淆-->
                    <options>
                        <!-- 可以在此处写option标签配置，不过我上面使用了proguardInclude，故而我更喜欢在proguard.cfg中配置 -->
                    </options>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                        <configuration>
                            <mainClass>com.itlab1024.proguard.SpringBootProguardApplication</mainClass>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

再新建个mvc的controller。
```java
package com.itlab1024.proguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {
    /**
     * 主页Mapping
     * @return
     */
    @GetMapping
    public String index() {
        return "这是主页";
    }
}
```
proguard.cfg配置

```text
#指定Java的版本
-target 1.8
#proguard会对代码进行优化压缩，他会删除从未使用的类或者类成员变量等
-dontshrink
#是否关闭字节码级别的优化，如果不开启则设置如下配置
-dontoptimize
#混淆时不生成大小写混合的类名，默认是可以大小写混合
-dontusemixedcaseclassnames
# 对于类成员的命名的混淆采取唯一策略
-useuniqueclassmembernames
#混淆时不生成大小写混合的类名，默认是可以大小写混合
-dontusemixedcaseclassnames
#混淆类名之后，对使用Class.forName('className')之类的地方进行相应替代
-adaptclassstrings

#对异常、注解信息予以保留
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
# 此选项将保存接口中的所有原始名称（不混淆）-->
-keepnames interface ** { *; }
# 此选项将保存所有软件包中的所有原始接口文件（不进行混淆）
#-keep interface * extends * { *; }
#保留参数名，因为控制器，或者Mybatis等接口的参数如果混淆会导致无法接受参数，xml文件找不到参数
-keepparameternames
# 保留枚举成员及方法
-keepclassmembers enum * { *; }
# 不混淆所有类,保存原始定义的注释-
-keepclassmembers class * {
                        @org.springframework.context.annotation.Bean *;
                        @org.springframework.beans.factory.annotation.Autowired *;
                        @org.springframework.beans.factory.annotation.Value *;
                        @org.springframework.stereotype.Service *;
                        @org.springframework.stereotype.Component *;
                        }

#忽略warn消息
-ignorewarnings
#忽略note消息
-dontnote
#打印配置信息
-printconfiguration
-keep public class com.itlab1024.proguard.SpringBootProguardApplication {
        public static void main(java.lang.String[]);
    }
```

混淆后反编译结果如下：

![image-20221016164250423](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202210161642779.png)

可以看到IndexController被混淆了，Spring Boot的启动类SpringBootProguardApplication未被混淆。

对混淆的jar进行启动

```shell
➜  target git:(main) ✗ java -jar Spring-Boot-Proguard-0.0.1-SNAPSHOT.jar 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.4)

2022-10-16 16:44:23.132  INFO 12416 --- [           main] c.i.p.SpringBootProguardApplication      : Starting SpringBootProguardApplication v0.0.1-SNAPSHOT using Java 17.0.3.1 on 192.168.0.100 with PID 12416 (/Users/itlab/workspace/github/Spring-Boot-Proguard/target/Spring-Boot-Proguard-0.0.1-SNAPSHOT.jar started by itlab in /Users/itlab/workspace/github/Spring-Boot-Proguard/target)
2022-10-16 16:44:23.138  INFO 12416 --- [           main] c.i.p.SpringBootProguardApplication      : No active profile set, falling back to 1 default profile: "default"
2022-10-16 16:44:24.406  INFO 12416 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
2022-10-16 16:44:24.420  INFO 12416 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2022-10-16 16:44:24.420  INFO 12416 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.65]
2022-10-16 16:44:24.528  INFO 12416 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2022-10-16 16:44:24.528  INFO 12416 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 1266 ms
2022-10-16 16:44:24.996  INFO 12416 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-10-16 16:44:25.007  INFO 12416 --- [           main] c.i.p.SpringBootProguardApplication      : Started SpringBootProguardApplication in 2.538 seconds (JVM running for 3.108)
```

访问主页：

![image-20221016164530268](https://itlab1024-1256529903.cos.ap-beijing.myqcloud.com/202210161645431.png)

# 备注

1. 上面知识简单的介绍了插件的使用，其实有很多选项，并未一一介绍，但是基本够用。
2. 如果配置文件中一个-keep都没有设置，打包会报错的，要注意下。
3. 对于Java项目，哪些class需要混淆，哪些不需要混淆，自己一定要清楚。比如AOP或者读取路径等的地方肯定是不能够混淆的。另外一般接口也不建议混淆。因为有些第三方工具接口的名称是有意义的，就比如Mybatis的接口以及参数名字，在xml（也可能没有xml）的sql中都是一一对应的，如果混淆就找不到了。
4. 另外混淆后，强烈建议多测试，笔者就是在实际过程中混淆的时候发现了很多问题。

# 附录

* proguard官网：https://www.guardsquare.com/proguard，插件也是基于官网要求开发，很多配置并未做改变，具体含义可从官网查询。
* 插件官网：https://github.com/wvengen/proguard-maven-plugin

# 博主信息

个人网站：https://itlab1024.com

微信公众号：ITLab1024

稀土掘金：https://juejin.cn/user/1473775002718264

知乎：https://www.zhihu.com/people/xpp1109

github：https://github.com/itlab1024

Java QQ群：455914984

Go QQ群：609889104

