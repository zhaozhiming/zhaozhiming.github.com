# Hacker and Geeker's Way 博客项目

基于 Hexo 框架的技术博客，使用 hexo-fabric 主题，部署在 GitHub Pages 上。命令见 `package.json`，配置见 `_config.yml` 和 `themes/hexo-fabric/_config.yml`。

## 博客写作

项目中配置了 `write-post` 写作技能（`.claude/skills/write-post/`），包含完整的写作风格规范、文章结构模板和质量检查清单。撰写博客文章时会自动加载该技能，确保生成的文章与现有文章风格一致。

项目中还配置了 `review-post` 审查技能（`.claude/skills/review-post/`），用于检查博客文章是否符合写作规范。当用户要求检查、审查或 review 文章时会自动加载。

## 封面生成

项目中配置了 `cover-image` 封面生成技能（`.codex/skills/cover-image/`），用于为文章生成「像素风 + 动物 + 标题」的封面图。当用户要求生成封面、做封面、给文章配题图、cover 时，**读取并严格遵循 `.codex/skills/cover-image/SKILL.md` 的完整流程**。

要点：据文章联想一个**未用过**的动物（去重数据见同目录 `used-animals.md`，新封面动物不得与历史重复）、联想 2 件关联物品、提炼英文标题，用固定 prompt 模板经 Codex 内置 `image_gen` 工具生成，再归位到 `source/images/post/YYYY/MM/<slug>.png` 并接入文章题图行。生成前需向用户确认动物 / 物品 / 标题 / prompt / 路径。
