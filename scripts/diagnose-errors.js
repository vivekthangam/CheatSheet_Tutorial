const fs = require('fs');
const { createMarkdownRenderer } = require('vitepress');
const { compile } = require('@vue/compiler-dom');

async function diagnose() {
  const md = await createMarkdownRenderer(process.cwd(), { highlight: () => '' });
  const reports = JSON.parse(fs.readFileSync('syntax_errors_report.json', 'utf-8'));

  for (const item of reports) {
    console.log(`\n========================================`);
    console.log(`FILE: ${item.file}`);
    console.log(`MSG:  ${item.message}`);
    const content = fs.readFileSync(item.file, 'utf-8');
    const html = md.render(content, {});
    let errorFound = false;

    try {
      compile(html, {
        onError: (err) => {
          errorFound = true;
          console.log(`Error type: ${err.message}`);
          if (err.loc) {
            console.log(`HTML Line: ${err.loc.start.line}, Col: ${err.loc.start.column}`);
            const htmlLines = html.split('\n');
            const start = Math.max(0, err.loc.start.line - 3);
            const end = Math.min(htmlLines.length, err.loc.start.line + 2);
            for (let i = start; i < end; i++) {
              console.log(`${i + 1 === err.loc.start.line ? '->' : '  '} [HTML ${i + 1}] ${htmlLines[i]}`);
            }
          }
        }
      });
    } catch (e) {
      console.log(`Caught exception: ${e.message}`);
    }
  }
}

diagnose().catch(err => console.error(err));
