import { MENU_ITEMS } from './src/data/menu';
const filteredItems = MENU_ITEMS.flatMap(item => {
    const items = [item];
    if (item.category === 'doner') {
      const isBox = item.id.includes('box');
      items.push({
        ...item,
        id: `${item.id}-menu`,
      } as any);
    }
    return items;
});

const ids = new Set();
filteredItems.forEach(i => {
    if (ids.has(i.id)) {
        console.log("DUPLICATE:", i.id);
    }
    ids.add(i.id);
});
console.log("Total length:", filteredItems.length);
