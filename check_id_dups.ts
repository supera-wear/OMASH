import { MENU_ITEMS } from './src/data/menu.ts';
const allIds = MENU_ITEMS.map(i => i.id);
const dupes = allIds.filter((item, index) => allIds.indexOf(item) !== index);
console.log('MENU_ITEMS id dupes:', dupes);

// create what FullMenu's flatMap does
const filteredItems = MENU_ITEMS.flatMap(item => {
  const items = [item];
  if (item.category === 'doner') {
    const isBox = item.id.includes('box');
    items.push({
      ...item,
      id: `${item.id}-menu`,
      name: `${item.name} MENU`, 
      price: isBox ? item.price + 60 : item.price + 110, 
      category: 'menus', 
    } as any);
  }
  return items;
});
const combinedIds = filteredItems.map(i => i.id);
const cbDupes = combinedIds.filter((item, index) => combinedIds.indexOf(item) !== index);
console.log('filteredItems id dupes:', cbDupes);

// check what happens to groupedItems
const groups: any = {};
for (const item of filteredItems) {
  if (!groups[item.category]) groups[item.category] = [];
  groups[item.category].push(item.id);
}
for (const [cat, ids] of Object.entries(groups)) {
  const idsArr = ids as string[];
  const cDupes = idsArr.filter((item, index) => idsArr.indexOf(item) !== index);
  if (cDupes.length > 0) {
    console.log(`Dupe ids in category ${cat}:`, cDupes);
  }
}
