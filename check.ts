import { MENU_ITEMS } from './src/data/menu';

const filteredItems = MENU_ITEMS.flatMap(item => {
  if (item.category === 'doner') {
    const isBox = item.id.includes('box');
    return [
      item,
      { 
        ...item, 
        id: `${item.id}-menu`, 
        name: `${item.name} MENU`, 
      } as any
    ];
  }
  return [item];
});

const ids = filteredItems.map(item => item.id);
const dupes = ids.filter((item, index) => ids.indexOf(item) !== index);
console.log("DUPES in MenuSection filteredItems:", dupes);
