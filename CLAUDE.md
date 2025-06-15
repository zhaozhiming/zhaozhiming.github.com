# Hacker and Geeker's Way 博客项目

这是一个基于 Hexo 框架的技术博客项目，使用 hexo-fabric 主题，部署在 GitHub Pages 上。

## 常用命令

### 构建和运行

```bash
# 安装依赖
npm install

# 清理生成的文件
npm run clean

# 生成静态文件
npm run build

# 启动本地服务器（http://localhost:4000）
npm run server

# 部署到 GitHub Pages
npm run deploy
```

### 文章管理

```bash
# 创建新文章（会自动使用日期命名）
npm run new "文章标题"

# 或者使用 hexo 命令
hexo new "文章标题"
```

## 项目结构

### 核心目录

- `source/_posts/` - 博客文章存放目录，文件命名格式：`YYYY-MM-DD-title.md`
- `source/images/post/` - 文章图片资源目录，按年份组织子目录
- `themes/hexo-fabric/` - 自定义主题目录
- `public/` - 生成的静态文件目录（自动生成，已在 .gitignore 中）

### 配置文件

- `_config.yml` - Hexo 主配置文件
- `themes/hexo-fabric/_config.yml` - 主题配置文件

## 文章发布流程

1. **创建文章**

   ```bash
   npm run new "新一代的版本管理工具 Jujutsu 使用实践"
   ```

   生成文件：`source/_posts/2025-06-15-xin-yi-dai-de-ban-ben-guan-li-gong-ju-jujutsu-shi-yong-shi-jian.md`

2. **文章 Front Matter 模板**

   ```yaml
   ---
   layout: post
   title: 文章标题
   date: 2025-06-15 14:51:12
   description: 文章描述，用于 SEO
   keywords: 关键词1, 关键词2, 关键词3
   comments: true
   categories: code/ai/blockchain/等
   tags:
     - tag1
     - tag2
   ---
   ```

3. **插入图片**

   ```markdown
   {% img /images/post/2025/06/image-name.png 400 300 %}
   ```

4. **添加摘要分隔符**
   在合适的位置添加 `<!--more-->` 来控制首页显示的摘要长度

5. **本地预览**

   ```bash
   npm run server
   ```

6. **部署发布**
   ```bash
   npm run build
   npm run deploy
   ```

## 主题定制

### 侧边栏配置

在 `_config.yml` 中配置侧边栏模块：

```yaml
default_asides:
  - "_partial/asides/recent_post" # 最近文章
  - "_partial/asides/tags" # 标签云
  - "_partial/asides/archives" # 归档
```

### 社交链接

在 `themes/hexo-fabric/_config.yml` 中配置：

```yaml
github_user: zhaozhiming
twitter_user: kingzzm
linkedin_user: zhaozhiming
stackoverflow_user: 1954315/zhaozhiming
```

### 评论系统

使用 Disqus 评论系统，在主题配置中设置：

```yaml
disqus_shortname: zhaozhiming
```

## 部署配置

博客通过 Git 部署到 GitHub Pages：

```yaml
deploy:
  type: git
  repo: git@github.com:zhaozhiming/zhaozhiming.github.com.git
  branch: master
```

注意：当前在 `source` 分支进行开发，部署时会自动推送到 `master` 分支。

## 文档和规划要求

### 重大变更的强制性文档

在实施任何重大模块变更、功能添加或架构修改时，必须在“docs/plan/”目录中创建并维护文档。

#### 所需文档流程

1. **创建规划文档**：在开始任何重大修改之前，请在 `docs/plan/[feature-name]-plan.md` 中创建一份详细的规划文档。
2. **记录进度**：随着工作进展，更新规划文档的进度状态。
3. **维护两种跟踪系统**：同时使用内存中的待办事项列表和持久化文档。

#### 规划文档结构

```markdown
# [功能/变更名称] 规划

## 概述

变更及其目的的简要描述

## 当前问题分析

详细分析需要变更的内容及其原因

## 策略与方法

变更的实施方式

## 实施步骤

详细分解任务，并列出优先级和状态

## 时间表

每个阶段的预计完成日期

## 风险评估

潜在风险及缓解策略

## 成功标准

如何衡量成功完成

## 进度跟踪

实时状态更新 (✅ ✓ ⏳ ❌)

## 相关文件

所有待修改文件的列表
```

#### 何时创建计划文档

- 新功能实现
- 编写新文章
- 架构重构（例如移除 v1 依赖项）
- 数据库架构变更
- API 版本迁移
- 安全增强
- 性能优化
- 影响多个组件的重大错误修复

#### 文档维护

- 任务完成后实时更新进度标记
- 记录任何与原计划的偏差并说明原因
- 记录经验教训和实施说明
- 保持状态更新，方便团队查看

这可确保所有主要工作都得到妥善跟踪和记录，并可由团队中的任何人恢复。

## 其他功能

- **RSS 订阅**：`/atom.xml`
- **Google Analytics**：已配置追踪 ID `UA-100485541-1`
- **搜索功能**：已启用简单搜索
- **代码高亮**：支持多种编程语言语法高亮

## 注意事项

1. 文章文件名会自动生成为 `YYYY-MM-DD-title.md` 格式
2. 图片资源建议按年月组织在 `source/images/post/` 目录下
3. 部署前先运行 `npm run clean` 清理旧文件
4. 文章中的中文会被自动转换为拼音作为 URL 的一部分
