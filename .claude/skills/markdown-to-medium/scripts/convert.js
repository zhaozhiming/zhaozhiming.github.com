#!/usr/bin/env node

const fs = require("fs");
const path = require("path");

function printHelp() {
  console.log(`Usage:
  node .claude/skills/markdown-to-medium/scripts/convert.js <input.md> [--output output.html]
  cat input.md | node .claude/skills/markdown-to-medium/scripts/convert.js [--output output.html]

Options:
  -o, --output   Write the converted HTML to a file
  -h, --help     Show this help message`);
}

function parseArgs(argv) {
  const args = {
    inputPath: null,
    outputPath: null,
    help: false,
  };

  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index];

    if (arg === "-h" || arg === "--help") {
      args.help = true;
      continue;
    }

    if (arg === "-o" || arg === "--output") {
      index += 1;
      if (index >= argv.length) {
        throw new Error("Missing value for --output");
      }
      args.outputPath = argv[index];
      continue;
    }

    if (arg.startsWith("-")) {
      throw new Error(`Unknown argument: ${arg}`);
    }

    if (args.inputPath) {
      throw new Error(`Unexpected extra argument: ${arg}`);
    }

    args.inputPath = arg;
  }

  return args;
}

function loadMarked() {
  const repoRoot = path.resolve(__dirname, "..", "..", "..", "..");
  const markedPath = path.join(
    repoRoot,
    "markdown-to-medium-tool",
    "node_modules",
    "marked"
  );

  try {
    return require(markedPath);
  } catch (error) {
    throw new Error(
      `Unable to load marked from ${markedPath}. Ensure markdown-to-medium-tool dependencies are present.`
    );
  }
}

function createConverter() {
  const marked = loadMarked();
  const renderer = new marked.Renderer();

  marked.setOptions({
    renderer,
    gfm: true,
    tables: true,
    breaks: false,
    pedantic: false,
    sanitize: true,
    smartLists: true,
    smartypants: false,
  });

  return (markdown) => marked(markdown);
}

function readStdin() {
  return new Promise((resolve, reject) => {
    let data = "";

    process.stdin.setEncoding("utf8");
    process.stdin.on("data", (chunk) => {
      data += chunk;
    });
    process.stdin.on("end", () => resolve(data));
    process.stdin.on("error", reject);
  });
}

async function readInput(inputPath) {
  if (inputPath) {
    return fs.readFileSync(path.resolve(process.cwd(), inputPath), "utf8");
  }

  if (process.stdin.isTTY) {
    throw new Error("No input provided. Pass a Markdown file path or pipe Markdown via stdin.");
  }

  return readStdin();
}

function writeOutput(outputPath, html) {
  if (!outputPath) {
    process.stdout.write(html);
    if (!html.endsWith("\n")) {
      process.stdout.write("\n");
    }
    return;
  }

  const resolvedOutput = path.resolve(process.cwd(), outputPath);
  fs.mkdirSync(path.dirname(resolvedOutput), { recursive: true });
  fs.writeFileSync(resolvedOutput, html, "utf8");
  process.stderr.write(`Wrote Medium HTML to ${resolvedOutput}\n`);
}

async function main() {
  const args = parseArgs(process.argv.slice(2));

  if (args.help) {
    printHelp();
    return;
  }

  const markdown = await readInput(args.inputPath);
  const convert = createConverter();
  const html = convert(markdown);
  writeOutput(args.outputPath, html);
}

main().catch((error) => {
  process.stderr.write(`${error.message}\n`);
  process.exit(1);
});
