# Spring Profiles 教学：本地与生产环境配置切换
## 1. 学习目标
通过本教程，你可以掌握以下能力：
- 理解 Spring Profiles 的作用和加载规则
- 将单一 `application.yml` 拆分为 `dev` / `prod` 多环境配置
- 掌握本地开发与线上部署的环境切换方式
- 了解生产环境的安全配置规范（尤其是敏感信息管理）

## 2. 当前项目现状（起点）
当前项目只有一个配置文件：`src/main/resources/application.yml`，其中同时包含：
- `spring.datasource.*`（数据库连接）
- `mybatis.*`（MyBatis 配置）
- `jwt.*`（JWT 配置）

这种写法在学习初期简单，但在真实项目中会导致：
- 本地与线上配置混在一起，切换不方便
- 账号密码、密钥容易误提交到代码仓库
- 发布时容易因为手工修改配置出错

## 3. Spring Profiles 核心原理
Spring Boot 会先加载 `application.yml`（公共配置），再加载激活环境对应的文件：
- `application-dev.yml`
- `application-prod.yml`

同名配置项会被后加载的环境文件覆盖（即：环境配置优先级更高）。

## 4. 推荐文件结构
在 `src/main/resources/` 下拆分为：

```text
application.yml
application-dev.yml
application-prod.yml
```

## 5. 配置拆分实操（基于本项目）
### 5.1 公共配置：`application.yml`
放“各环境都通用”的内容，例如 MyBatis 通用设置、JWT 非敏感参数、默认端口等。

```yaml
server:
  port: ${SERVER_PORT:8083}

spring:
  profiles:
    default: dev

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: org.example.jwtjavaeight.domain.entity
  configuration:
    map-underscore-to-camel-case: true

jwt:
  access-token-expiration: 900000
  refresh-token-expiration: 604800000
  token-prefix: "Bearer "
  header: Authorization
```

说明：
- `spring.profiles.default: dev` 表示未显式指定环境时，默认用 `dev`
- `SERVER_PORT` 支持环境变量覆盖，不传时默认 `8083`

### 5.2 本地开发配置：`application-dev.yml`
放本地开发专用配置（本地数据库地址、开发密钥等）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/jwt_java_eight?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: dev-secret-change-me
```

### 5.3 生产配置：`application-prod.yml`
生产环境不写死账号密码和密钥，改为环境变量注入：

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

jwt:
  secret: ${JWT_SECRET}
```

说明：
- 生产敏感信息统一来自部署平台（环境变量、密钥管理服务、容器平台 Secret）
- 不建议在生产配置里写默认敏感值（例如 `${DB_PASSWORD:root}`）

## 6. 如何切换环境
### 6.1 启动参数（最常用）
```bash
java -jar target/my-admin-java-eight-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
java -jar target/my-admin-java-eight-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 6.2 Maven 启动
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### 6.3 环境变量方式
Linux/macOS:
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar target/my-admin-java-eight-0.0.1-SNAPSHOT.jar
```

PowerShell:
```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
java -jar target/my-admin-java-eight-0.0.1-SNAPSHOT.jar
```

## 7. 生产环境配置规范（重点）
### 7.1 不把敏感信息提交到仓库
不要在 Git 中保存真实的：
- 数据库账号/密码
- JWT 密钥
- 第三方 API Key

### 7.2 使用环境变量注入敏感参数
生产至少准备以下变量：
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`

### 7.3 保持配置可审计
- `application-prod.yml` 可以保留“变量占位符模板”
- 真正的值只放在部署环境（服务器、CI/CD、容器平台）

### 7.4 让错误尽早暴露
- 生产敏感项不设置默认值，缺失时启动失败（Fail Fast）
- 避免应用“带着错误默认值上线”

## 8. 验证是否切换成功
启动日志中应看到类似信息：
- `The following profiles are active: dev`
- `The following profiles are active: prod`

可进一步验证：
- `dev` 环境连接本地数据库
- `prod` 环境读取到部署平台的环境变量

## 9. 常见问题排查
### 问题 1：始终走同一套配置
排查：
- 是否正确传了 `--spring.profiles.active=...`
- 是否设置了 `SPRING_PROFILES_ACTIVE`
- 环境变量是否被旧会话覆盖

### 问题 2：`${DB_PASSWORD}` 未解析
排查：
- 变量名是否与部署环境完全一致（区分大小写）
- 变量是否在应用启动前注入

### 问题 3：以为 `application.yml` 会覆盖环境文件
结论：
- 实际是环境文件覆盖公共文件中的同名项

## 10. 给学生的练习任务
1. 按本文完成三份配置文件拆分
2. 使用 `dev` 和 `prod` 各启动一次，并截图日志中的 active profile
3. 在 `prod` 下删除一个必需环境变量，观察并解释启动失败原因
4. 用一句话总结：为什么生产环境必须使用环境变量管理敏感配置

## 11. 一页结论
- 最推荐方案：`application.yml + application-{profile}.yml`
- 本地：`dev`，可使用本地数据库与开发密钥
- 生产：`prod`，敏感信息全部走环境变量
- 切换方式首选：启动参数或环境变量
- 目标：安全、清晰、可维护、可上线
