
app.service('itemsService', function () {

    // Copy only items for the selected store. If no store is selected,
    // copy all items. 
    this.computeItemsByStore = function(items_ro, clearedItems, selectedStore) {
        var items = {};
        var stores = [];

        for (category in items_ro) {
            if (!items_ro.hasOwnProperty(category)) {
                continue;
            }

            var itemList = []
            for (var i = 0; i < items_ro[category].length; i++) {
                var item = items_ro[category][i];

                if (clearedItems.indexOf(item.id) > -1) {
                    continue;
                }

                if (!selectedStore) {
                    itemList.push(item);
                    continue;
                }

                if (!item.stores) {
                    continue;
                }
                
                if (item.stores.map(function(x) {
                    return x.id;
                }).indexOf(selectedStore.id) > -1) {
                    itemList.push(item);
                    continue;
                }
            }

            if (itemList) {
                items[category] = itemList;
            }
        }

        return items;
    }

    this.removeEmptyCategories = function(items) {
        for (category in items) {
            if (!items.hasOwnProperty(category)) {
                continue;
            }
            
            if (items[category].length == 0) {
                delete items[category];
            }
        }
    }

});
