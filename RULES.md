# 项目开发规则

## Java 版本规则

**重要**：编译项目时必须使用与项目配置一致的 Java 版本：
- 查看 `pom.xml` 中的 `<java.version>` 确定版本
- 本项目使用 **Java 17**
- 使用 `export JAVA_HOME=/path/to/java17` 切换版本后再编译

## 编码规范

- 注释必须写在代码**上方**，禁止写在同一行后面
