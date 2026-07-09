qwww
## 3. 编码规约

* **注释规范**: 不要在函数或逻辑内部编写任何的内联注释。不要在shader中编写任何注释。
* **导入规范**: 绝对不要使用任何包名全称，能import包必须使用import。不准偷懒直接写全称，必须用编辑工具正常在顶部插入import。
* **编译校验**: 每次修改完代码后，必须通过本地终端运行 `$env:JAVA_HOME="C:\Program Files\Java\jdk-25.0.3"; .\gradlew.bat compileJava` 进行编译校验，确保没有语法和编译期报错。
