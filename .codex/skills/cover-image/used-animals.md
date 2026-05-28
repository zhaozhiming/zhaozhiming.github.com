# 已用动物登记表（封面去重用）

本表是 `cover-image` 技能的**去重数据源**。`动物` 列记录每篇文章封面里用过的动物。

**使用规则：**

- 生成新封面前，读取本表，把 `动物` 列里所有非 `none` 的值汇总成「已用动物清单」，**新封面的动物必须避开清单里的所有动物**（含近亲，如 llama/alpaca、marmot/gopher 视为同类）。
- 生成新封面后，在表格**末尾追加一行**（日期、slug、动物、来源标 `本次生成`、备注里写 2 件物品和英文标题）。

> 下表为初始 seeding，由查看 2024–2026 历史封面识别得到。标「中」的是中等置信度，可能与实际略有出入。

| 日期 | 文章 slug | 动物 | 置信度/来源 | 备注（场景 / 物品 / 英文标题） |
|------|-----------|------|-------------|-------------------------------|
| 2024-01-13 | llamaindex-eleasticsearch-rga-practice | 羊驼 / llama | 高 | 蓝色羊驼站在书本与笔筒旁，背景峡谷日落 |
| 2024-01-18 | rerank-model-deploy-and-usage | 猴子 / monkey | 高 | 戴耳机的小猴趴书桌写字 |
| 2024-01-29 | use-trulens-to-evaluate-rag-application | none | 高 | 盾牌/放大镜/清单/书本，无动物 |
| 2024-02-13 | use-xinference-deploy-llm | 熊（泰迪）/ bear | 中 | 魔法森林小巫师，河边棕色泰迪熊 |
| 2024-03-11 | sentence-windows-rag | 猫 / cat | 高 | 白猫趴书桌旁一杯咖啡 |
| 2024-03-19 | auto-merging-rag | 狐狸 / fox | 高 | 橙色狐狸坐在摊开的书上施法 |
| 2024-03-25 | several-solution-of-leetcode-373 | 熊猫 / panda | 高 | 竹林溪流间的熊猫玩偶 |
| 2024-03-31 | englisth-learning-introduction-goldlist | 鹦鹉 / parrot | 高 | 金色鸟笼里彩色鹦鹉，水果环绕 |
| 2024-04-09 | recursive-retriever-rag | none | 高 | 夜晚迷宫小男孩面对发光门，无动物 |
| 2024-04-22 | llamaindex-and-evaluation-tools | 浣熊 / raccoon | 中 | 日落草地浣熊（旁有似狼小动物） |
| 2024-04-26 | free-use-gpt-api-write-novel | 乌龟 / turtle | 高 | 乌龟趴笔记本旁，场记板亮字 |
| 2024-04-29 | use-opencompass-evaluate-llama3 | 羊驼 / alpaca | 高 | 蓬松小羊驼坐办公桌，霓虹蓝字 |
| 2024-05-04 | use-llama3-to-build-develop-team-copilot | 美洲驼 / llama | 高 | 复古科幻，驮装备的美洲驼与宇航员 |
| 2024-05-13 | query-rewrite-rag | 座头鲸 / humpback whale | 高 | 海岛日落，鲸鱼跃出海面 |
| 2024-05-24 | embedded-table-rag | 鹰 / hawk | 中 | 猛禽张翅翱翔粉色云海 |
| 2024-06-01 | llamaindex-llama3-es-hybrid-search | 骡 / mule | 中 | 半骆驼半骡混合头像（hybrid 寓意） |
| 2024-06-08 | rag-module-pipeline | 蛇 / snake | 高 | 霓虹丛林蓝绿蛇盘绕在 RAG FLOW 上 |
| 2024-06-16 | llamaindex-buildin-evaluation | none | 高 | 夜间足球比赛场景，无动物 |
| 2024-06-30 | rag-selfrag | 蜂鸟 / hummingbird | 高 | 蜂鸟悬停结苹果的树旁 |
| 2024-07-04 | rag-knowledge-graphs | 松鼠 / squirrel | 高 | 魔法森林红松鼠捧橡果 |
| 2024-07-15 | modern-python-project-tools-config | none | 高 | 马车 vs 现代汽车线描，无动物 |
| 2024-08-06 | rag-query-router | 章鱼 / octopus | 高 | 霓虹章鱼立于海底 |
| 2024-09-15 | publish-company-npm-package-guide | none | 高 | 黑底白线图书馆人物，无动物 |
| 2024-09-21 | Cursor-helped-me-get-up-to-speed-with-Go-in-just-one-day | 水母 / jellyfish | 高 | 发光蓝粉水母群漂浮深海 |
| 2024-10-03 | use-aws-role-to-isolate-resources-and-privilege | 旱獭/土拨鼠 / marmot | 中 | 草原圆胖啮齿动物 |
| 2024-10-04 | use-github-action-to-deploy-aws-ecs | 鲸鱼 / whale | 高 | 蓝鲸背驮集装箱（Docker 风） |
| 2024-10-18 | maybe-you-donot-known-5-way-to-mock-golang-programm | 地鼠 / gopher | 中 | 大门牙小动物探出洞口（Go gopher） |
| 2024-11-04 | curve-pool-base-invest-and-data-computer | 羚羊 / antelope | 高 | 紫色羚羊跃过比特币金币与价格柱状图 |
| 2024-12-07 | solana-resources-limitation-research | 猎豹 / cheetah | 高 | 数字粒子构成的猎豹高速奔跑 |
| 2025-02-22 | solana-storage-limitation | 松鼠 / squirrel | 高 | 红松鼠抱橡果，SOLANA STORAGE 木牌 |
| 2025-05-14 | solana-bloom-filter | 章鱼 / octopus | 高 | 章鱼盘踞迷宫纹背景，BLOOM FILTER |
| 2025-05-23 | deep-dive-into-solana-transaction | 河狸 / beaver | 高 | 河狸叼原木在水边奔跑 |
| 2025-07-01 | jujutsu-usage-practice | 大象 / elephant | 中 | 雪山藏村，溪边一头被骑乘的大象 |
| 2026-03-15 | claude-code-claude-md | 章鱼 / octopus | 高 | 海底珊瑚礁橙色章鱼，CLAUDE.md 字样 |
| 2026-04-03 | claude-code-slash-command | 蛇 / snake | 中 | 幽绿丛林蛇游过石径（也可能是藤蔓） |
| 2026-04-27 | claude-code-hooks | 寄居蟹 / hermit crab | 高 | 海边沙滩贝壳间一只寄居蟹 |
| 2026-05-29 | claude-code-plugin | 喜鹊 / magpie | 本次生成 | 闪亮银币、树枝鸟巢；Claude Code Plugins |
