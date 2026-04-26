import fs from 'fs';
const text = fs.readFileSync('src/App.tsx', 'utf-8');

// Match all key={}
const keyMatches = Array.from(text.matchAll(/key=\{([^}]+)\}/g));
const keys = keyMatches.map(m => m[1]);
console.log('All key expressions:', keys.join('\n'));
