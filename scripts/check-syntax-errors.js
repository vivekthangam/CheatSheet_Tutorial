const fs = require('fs');
const path = require('path');
const { createMarkdownRenderer } = require('vitepress');
const { compile } = require('@vue/compiler-dom');

function getAllFiles(dirPath, arrayOfFiles = []) {
  const files = fs.readdirSync(dirPath);

  files.forEach(file => {
    const fullPath = path.join(dirPath, file);
    if (file === 'node_modules' || file === '.git' || (file === '.vitepress' && !file.endsWith('.md'))) {
      return;
    }
    if (fs.statSync(fullPath).isDirectory()) {
      arrayOfFiles = getAllFiles(fullPath, arrayOfFiles);
    } else if (file.endsWith('.md')) {
      arrayOfFiles.push(fullPath);
    }
  });

  return arrayOfFiles;
}

async function run() {
  const rootDir = process.cwd();
  console.log('Initializing VitePress markdown renderer (fast mode)...');
  const md = await createMarkdownRenderer(rootDir, { highlight: () => '' });
  const files = getAllFiles(rootDir);
  console.log(`Found ${files.length} markdown files to scan.`);

  const errors = [];
  let checked = 0;

  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    const relFile = path.relative(rootDir, file).replace(/\\/g, '/');
    const content = fs.readFileSync(file, 'utf-8');
    const env = { path: file, cleanUrls: true };

    try {
      const html = md.render(content, env);
      compile(html, {
        onError: (err) => {
          throw err;
        }
      });
    } catch (err) {
      errors.push({
        file: relFile,
        message: err.message,
        loc: err.loc ? {
          start: err.loc.start,
          end: err.loc.end
        } : null
      });
      console.error(`[ERROR ${errors.length}] ${relFile}: ${err.message}`);
    }
    checked++;
    if (checked % 50 === 0) {
      console.log(`Scanned ${checked}/${files.length} files... (Errors found so far: ${errors.length})`);
    }
  }

  console.log(`\nScan complete! Checked ${files.length} files. Total syntax errors: ${errors.length}`);
  fs.writeFileSync('syntax_errors_report.json', JSON.stringify(errors, null, 2), 'utf-8');
  console.log('Report saved to syntax_errors_report.json');
}

run().catch(err => {
  console.error('Fatal error during scan:', err);
  process.exit(1);
});
