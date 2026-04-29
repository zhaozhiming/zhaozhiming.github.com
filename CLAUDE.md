# Hacker and Geeker's Way 博客项目

基于 Hexo 框架的技术博客，使用 hexo-fabric 主题，部署在 GitHub Pages 上。命令见 `package.json`，配置见 `_config.yml` 和 `themes/hexo-fabric/_config.yml`。

## 博客写作

项目中配置了 `write-post` 写作技能（`.claude/skills/write-post/`），包含完整的写作风格规范、文章结构模板和质量检查清单。撰写博客文章时会自动加载该技能，确保生成的文章与现有文章风格一致。

项目中还配置了 `review-post` 审查技能（`.claude/skills/review-post/`），用于检查博客文章是否符合写作规范。当用户要求检查、审查或 review 文章时会自动加载。

项目中还配置了 `review-content-depth` 内容深度审查技能（`.claude/skills/review-content-depth/`），用于检查文章是否存在 AI 写作特征（套话、无锚段落、模板化结构等）。与 `review-post` 互补：`review-post` 检查格式，`review-content-depth` 检查内容深度。当用户要求去除 AI 味、提升文章深度、检查内容质量时使用。
