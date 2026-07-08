# NTMC 开发者指南 (AGENTS.md)

本文件为开发智能体 (Agent) 提供关于 Hbm's Nuclear Tech Mod - Space Branch (NTMC) 项目的架构、技术栈与开发规约介绍，以便于快速上手并遵守项目规范。

## 1. 技术栈介绍

* **目标游戏版本**: Minecraft 1.7.10
* **开发框架**: Minecraft Forge (1.7.10)
* **核心语言**: Java
* **编译与构建工具**: Gradle (配备 Jabel 编译器插件)
  * 注: 本地构建采用的高版本 JDK (如 JDK 25) 搭配 Jabel 进行编译，编译输出目标保持 Java 8 兼容性，构建指令为 `.\gradlew.bat compileJava`。
* **主要依赖与支持**:
  * **NotEnoughItems (NEI)**: 配方与物品检索支持，自定义配方处理器位于 `com.hbm.handler.nei`。
  * **Sedna 枪械框架**: 模组内置的模块化枪械系统，支持复杂的 3D 渲染、配件系统和自定义弹药。

## 2. 项目结构介绍

项目的核心源代码位于 `src/main/java`，资源文件位于 `src/main/resources`。

### 2.1 源代码结构 (`src/main/java`)

* **`com.hbm.main`**:
  * 模组的入口点与主注册类。
  * `MainRegistry.java`: 各种方块、物品、实体、成就的注册与重映射管理。
* **`com.hbm.blocks`**:
  * 包含所有机器、装饰方块、矿物、管道等方块类。
* **`com.hbm.items`**:
  * 包含普通物品、工具、电池、材料等。
  * **`com.hbm.items.weapon.sedna`**: Sedna 新版枪械系统的核心实现。
    * `ItemGunBaseNT.java`: 枪械基类。
    * `factory/`: 枪械工厂、渲染数据、子弹配置（如 `XFactoryEnergy.java` 等）。
* **`com.hbm.crafting`**:
  * 基础工作台配方的注册类。
  * `WeaponRecipes.java`: 武器工作台和常规配方。
* **`com.hbm.inventory.recipes`**:
  * 各种机器（化工厂、反应堆、离心机等）的自定义配方注册类。
  * `ChemicalPlantRecipes.java`: 化工厂配方。
* **`com.hbm.util`**:
  * 通用工具集、数据结构、处理器。
  * `AchievementHandler.java`: 模组自定义成就的触发和事件分发逻辑。
* **`com.hbm.creativetabs`**:
  * 创造模式物品栏的自定义 Tab 分类实现（如 `WeaponTab.java`）。

### 2.2 资源文件结构 (`src/main/resources`)

* **`assets/hbm/lang`**:
  * 语言本地化文件，如 `en_US.lang`（英文）和 `zh_CN.lang`（中文）。
* **`assets/hbm/sounds.json`**:
  * 游戏音效的声效映射定义文件。

## 3. 编码规约

* **注释规范**: 不要在函数或逻辑内部编写无意义的内联注释。
* **语言规范**: 对于所有的用户提问、工作计划、README.md、walkthrough.md 以及临时生成的 artifacts，必须使用中文编写（除非有明确要求）。
* **编译校验**: 每次修改完代码后，必须通过本地终端运行 `./gradlew.bat compileJava` 进行编译校验，确保没有语法和编译期报错。
