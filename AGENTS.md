# Repository Guidelines

## 项目结构与模块
- `source/_posts/`：Markdown 文章（必须含 front‑matter）。
- `themes/hexo-fabric/`：启用的 Hexo 主题与布局。
- `scaffolds/`：新文章模板；文件名模式见 `_config.yml` 的 `new_post_name`。
- `public/`：构建产物，请勿直接修改。
- `assets/`：`sync.py` 使用的 HTML/CSS 模板。
- `sync.py`：渲染并推送文章到微信公众号的辅助脚本（可选流程）。

## 构建、测试与本地开发
- `npm install`：安装 Hexo 及插件。
- `npm run server`：本地开发预览 `http://localhost:4000`。
- `npm run build`：生成站点到 `public/`。
- `npm run deploy`：使用 `hexo-deployer-git` 部署到 `master`。
- `npm run new "我的新文章"`：基于 scaffold 新建文章。

## 代码风格与命名
- 文章文件名：遵循 `:year-:month-:day-:title.md`。
- Front‑matter 示例：
  ```yaml
  ---
  title: 我的新文章
  date: 2025-08-01 12:00:00
  categories: [Tech]
  tags: [Hexo]
  subtitle: 可选摘要
  ---
  ```
- Markdown：优先使用围栏代码块；本地图片放 `source/images/`，引用示例 `![alt](/images/x.png)`。
- 缩进：YAML/JSON 使用 2 空格，不使用 Tab。

## 测试规范
- 确认 `npm run build` 无报错且本地页面渲染正常。
- 校对标题、链接与图片路径，尤其是 `source/images/` 下资源。
- 微信流程：本地运行 `sync.py`，检查 `output/result.html` 预览效果。

## 提交与合并请求
- 提交信息：遵循 Conventional Commits（如 `feat: add new post`、`fix: update links`）。
- PR 要求：简述改动、附本地预览截图、关联 issue（如有）；按内容/主题/脚本拆分变更，范围清晰。

## 安全与配置
- 禁止提交密钥；`sync.py` 从环境变量读取 `WECHAT_APP_ID`、`WECHAT_APP_SECRET`。
- 部署使用 Git SSH（详见 `_config.yml`），确保有推送权限。
