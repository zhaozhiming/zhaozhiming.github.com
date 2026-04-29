# Hacker and Geeker's Way 博客项目

基于 Hexo 框架的技术博客，使用 hexo-fabric 主题，部署在 GitHub Pages 上。命令见 `package.json`，配置见 `_config.yml` 和 `themes/hexo-fabric/_config.yml`。

## 博客写作

项目中配置了 `write-post` 写作技能（`.claude/skills/write-post/`），包含完整的写作风格规范、文章结构模板和质量检查清单。撰写博客文章时会自动加载该技能，确保生成的文章与现有文章风格一致。

项目中还配置了 `review-post` 审查技能（`.claude/skills/review-post/`），用于全面审查博客文章质量，包括格式规范（Front Matter、题图、结构、代码块等 13 个维度）和内容深度（AI 套话检测、具体锚点、声音个性等 7 个维度）两个阶段。当用户要求检查、审查、review 文章、去除 AI 味、检查内容质量或提升文章深度时使用。
