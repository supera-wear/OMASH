import fs from 'fs';

const locContent = fs.readFileSync('src/App.tsx', 'utf-8');

const checkArray = (regexStr, name) => {
  const match = locContent.match(new RegExp(`const ${regexStr} = \\s*\\[([\\s\\S]*?)\\];`));
  if (match) {
    const ids = Array.from(match[1].matchAll(/id:\s*['"]([^'"]+)['"]/g)).map(m => m[1]);
    const dupes = ids.filter((item, index) => ids.indexOf(item) !== index);
    if (dupes.length > 0) console.log(`${name} Dupes:`, dupes);
    else console.log(`${name} OK`);
  } else {
    console.log(`${name} not found`);
  }
};

checkArray('FAQS', 'FAQS');
checkArray('DRINK_OPTIONS', 'Drinks');
checkArray('EXTRA_INGREDIENTS', 'Extras');
checkArray('LOCATIONS', 'Locations');

