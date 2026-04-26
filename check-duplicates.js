const fs = require('fs');
const content = fs.readFileSync('src/data/menu.ts', 'utf8');
const idRegex = /id:\s*'([^']+)'/g;
let match;
const ids = [];
while ((match = idRegex.exec(content)) !== null) {
  ids.push(match[1]);
}
const duplicates = ids.filter((item, index) => ids.indexOf(item) !== index);
console.log(duplicates);
