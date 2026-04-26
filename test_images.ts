import { MENU_ITEMS } from './src/data/menu';

const images = new Set();
MENU_ITEMS.forEach(i => {
    if (images.has(i.image)) {
        console.log("DUPLICATE IMAGE:", i.image, "in id:", i.id);
    }
    images.add(i.image);
});
