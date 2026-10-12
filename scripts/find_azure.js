const fs = require('fs');

const buf = fs.readFileSync('cloud-infrastructure/azure_master_guide.md');
console.log('Byte length:', buf.length);
console.log('First 10 bytes:', buf.slice(0, 10));

let text = '';
if (buf[0] === 0xff && buf[1] === 0xfe) {
  console.log('Detected UTF-16LE');
  text = buf.toString('utf16le');
} else if (buf[0] === 0xfe && buf[1] === 0xff) {
  console.log('Detected UTF-16BE');
  text = buf.toString('utf16be');
} else {
  console.log('Detected UTF-8 / ASCII');
  text = buf.toString('utf8');
}

const lines = text.split('\n');
console.log('Total lines:', lines.length);

for (let i = 0; i < lines.length; i++) {
  if (lines[i].includes('Global Fiber') || lines[i].includes('Invalid end tag') || lines[i].includes('details') || lines[i].includes('Production Bic')) {
    console.log(`Line ${i + 1}: ${lines[i].slice(0, 100)}`);
  }
}
