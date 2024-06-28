# 火山引擎日志服务（TLS）Logback Appender

[![License](https://img.shields.io/badge/license-Apache2.0-blue.svg)](/LICENSE)

## TLS Logback Appender

Logback是由Log4j创始人设计的又一个开源日志组件。
通过使用Logback，您可以控制日志信息输送的目的地是控制台、文件、GUI组件、甚至是套接口服务器、NT的事件记录器、UNIX Syslog守护进程等；
您也可以控制每一条日志的输出格式；通过定义每一条日志信息的级别，您能够更加细致地控制日志的生成过程。
最令人感兴趣的就是，这些可以通过一个配置文件来灵活地进行配置，而不需要修改应用的代码。

通过火山引擎日志服务TLS Logback Appender，您可以将日志上报至火山引擎日志服务，写到日志服务中的日志的样式如下：

```
level: ERROR
location: com.volcengine.service.tls.logback.example.LogbackAppenderExample.main(LogbackAppenderExample.java:18)
message: this is an error log
throwable: java.lang.RuntimeException: xxx
thread: main
time: 2023-10-24T12:00+0000
log: 2023-10-24 12:00:00,000 ERROR [main] com.volcengine.service.tls.logback.example.LogbackAppenderExample: this is an error log
__source__: 192.168.1.100
__path__: sys.log
```

其中：
+ **level**：日志级别
+ **location**：日志打印语句的代码位置
+ **message**：日志内容
+ **throwable**：日志异常信息（只有记录了异常信息，这个字段才会出现）
+ **thread**：线程名称
+ **time**：日志打印时间（支持通过timeFormat或timeZone配置time字段呈现的格式和时区）
+ **log**：自定义日志格式（只有设置了encoder，这个字段才会出现）
+ **\_\_source\_\_**：日志来源，用户可在配置文件中指定。
+ **\_\_path\_\_**：日志数据源路径，用户可在配置文件中指定。


## 功能优势

+ **日志不落盘**：产生数据通过网络发给服务端
+ **零代码改造成本**：只需通过XML文件简单配置即可采集日志并上报到火山引擎日志服务
+ **高性能**：基于火山引擎日志服务Java SDK Producer，具有异步发送、高性能、失败重试等特性，适用于高并发场景


## 使用说明

### 1. maven 工程中引入依赖

```
<dependency>
    <groupId>com.volcengine</groupId>
    <artifactId>ve-tls-logback-appender</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 修改配置文件

在logback.xml文件中配置火山引擎日志服务Logback Appender，绑定com.volcengine.service.tls.logback.LogbackAppender类即可。

本项目提供了一段具体的配置示例如下所示（[logback-example.xml](/src/main/resources/logback-example.xml)），可供您参考。
该示例文件注册了两个Logback Appender，可分别将您的日志打印到控制台并上报到火山引擎日志服务。

在示例配置文件中，详细标注了日志服务Logback Appender必填和选填参数。其中，关于TLS Java Producer的自定义配置，您可参考[GitHub](https://github.com/volcengine/volc-sdk-java/blob/main/volc-sdk-java/src/main/java/com/volcengine/model/tls/producer/Producer.md)获取相应参数的信息。

```
<configuration>
    <!--为了防止进程退出时，内存中的数据丢失，请加上此选项-->
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook"/>

    <!-- 配置日志输出目的地为TLS日志服务的appender -->
    <appender name="TLSLogbackAppender" class="com.volcengine.service.tls.logback.LogbackAppender">
        <!-- 必选项：TLS日志服务基本配置 -->
        <endpoint></endpoint>
        <region></region>
        <accessKeyId></accessKeyId>
        <accessKeySecret></accessKeySecret>
        <!-- 必选项：TLS日志项目/主题配置 -->
        <projectID></projectID>
        <topicID></topicID>
        <!-- 可选项：日志数据源配置 -->
        <source></source>
        <filename></filename>

        <!-- 可选项：TLS Java Producer自定义配置 -->
        <totalSizeInBytes>104857600</totalSizeInBytes>
        <maxThreadCount>50</maxThreadCount>
        <maxBlockMs>60000</maxBlockMs>
        <maxBatchSizeBytes>524288</maxBatchSizeBytes>
        <maxBatchCount>4096</maxBatchCount>
        <lingerMs>2000</lingerMs>
        <retryCount>2</retryCount>
        <maxReservedAttempts>3</maxReservedAttempts>

        <!-- 可选项：通过配置encoder pattern自定义日志格式 -->
        <encoder>
            <pattern>%d %-5level [%thread] %logger{0}: %msg</pattern>
        </encoder>

        <!-- 可选项：设置时间格式 -->
        <timeFormat>yyyy-MM-dd'T'HH:mmZ</timeFormat>
        <!-- 可选项：设置时区 -->
        <timeZone>Asia/Shanghai</timeZone>

        <!-- 可选项：配置LevelFilter -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <!-- 可选项：配置mdcFields -->
        <mdcFields>THREAD_ID,MDC_KEY</mdcFields>
    </appender>

    <!-- 配置日志输出目的地为Stdout的appender -->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger - %msg %X{THREAD_ID} %n</pattern>
        </encoder>
    </appender>

    <!-- 可用来获取StatusManager中的状态 -->
    <statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener"/>
    <!-- 解决debug模式下循环发送的问题 -->
    <logger name="org.apache.http.impl.conn.Wire" level="WARN" />

    <root>
        <level value="DEBUG"/>
        <appender-ref ref="TLSLogbackAppender"/>
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

**注意**：
+ 请您加上`DelayingShutdownHook`标签，以防止进程退出时日志服务LogbackAppender缓存在内存中的少量数据丢失。
+ 日志服务LogbackAppender在运行过程中产生的异常会被捕获并放入Logback的`BasicStatusManager`类中，您可以通过配置`OnConsoleStatusListener`或其他方式查看出错信息。更多信息可参阅：https://logback.qos.ch/access.html

### 3. 使用原有Logback日志输出代码上报日志

本项目中的[LogbackAppenderExample](src/main/java/com/volcengine/service/tls/logback/example/LogbackAppenderExample.java)代码文件展示了最简单的通过Logback输出日志的示例。
当您实例化Logger对象后，通过trace/debug/info/warn/error方法即可将您的日志输出到logback.xml文件中配置的目的地。

当您根据步骤2修改了logback.xml配置文件后，您无需再修改系统中原有的输出日志相关的代码。

```java
package com.volcengine.service.tls.logback.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogbackAppenderExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackAppenderExample.class);

    public static void main(String[] args) {
        MDC.put("MDC_KEY","MDC_VALUE");
        MDC.put("THREAD_ID", String.valueOf(Thread.currentThread().getId()));

        LOGGER.trace("This is a trace log.");
        LOGGER.debug("This is a debug log.");
        LOGGER.info("This is a info log.");
        LOGGER.warn("This is a warn log.");
        LOGGER.error("This is an error log.");
    }
}
```