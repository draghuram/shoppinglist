
app.controller('NavbarController', function ($scope, $location) {
    $scope.getClass = function (path) {
        if ($location.path().substr(0, path.length) == path) {
            return true
        } else {
            return false;
        }
    }
});

app.controller('StoreController', function ($scope, $rootScope, $resource, $log, jsonFilter, $location) {
    var Store = $resource('/api/stores/:storeId');

    $scope.listStores = function() {
        stores_resp = Store.get(function() {
            $scope.stores = stores_resp.stores;
        })
    }
    
    init();

    function init() {
        $scope.rest_error_message = "";
        $scope.listStores();
    }
    
    $scope.insertStore = function () {
        var store = new Store($scope.newStore);

        store.$save({}, function() {
            $scope.newStore.name = '';
            $scope.rest_error_message = '';
            $scope.listStores();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
        
    };

    $scope.deleteStore = function (storeId) {
        new Store().$delete({storeId: storeId}, function() {
            $scope.listStores();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };

    $scope.updateStore = function (store) {
        // Saving to be used in item update. 
        $rootScope.updateStore = store;
        // $location.path("stores/updatestore/" + store.id);
    }
});

app.controller('CategoryController', function ($scope, $rootScope, $resource, $log, jsonFilter, $location) {
    var Category = $resource('/api/categories/:categoryId');

    $scope.listCategories = function() {
        categories_resp = Category.get(function() {
            $scope.categories = categories_resp.categories;
        })
    }
    
    init();

    function init() {
        $scope.rest_error_message = "";
        $scope.listCategories();
    }
    
    $scope.insertCategory = function () {
        var category = new Category($scope.newCategory);

        category.$save({}, function() {
            $scope.newCategory.name = '';
            $scope.rest_error_message = '';
            $scope.listCategories();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };

    $scope.deleteCategory = function (categoryId) {
        new Category().$delete({categoryId: categoryId}, function() {
            $scope.listCategories();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };

    $scope.updateCategory = function (category) {
        // Saving to be used in item update. 
        $rootScope.updateCategory = category;
    }
});

app.controller('ItemController', function ($scope, $route, $rootScope, $resource, $log, jsonFilter, itemsService) {
    var Item = $resource('/api/items/:itemId', null, {
        update: {
            method: 'PUT'
        }});

    $scope.listItems = function() {
        items_resp = Item.get({"view":"byCategory"}, function() {
            $scope.items_ro = items_resp.items;
            $scope.stores = items_resp.stores;
            $scope.items = itemsService.computeItemsByStore($scope.items_ro, [], $scope.store);
        })
    }

    function init() {
        $scope.rest_error_message = "";
        $scope.listItems();
        $scope.selectedItems = []
        $scope.store = undefined;
        $scope.stores = []
    }

    init();

    $scope.processStoreSelection = function(store) {
        $scope.items = itemsService.computeItemsByStore($scope.items_ro, [], $scope.store);
        itemsService.removeEmptyCategories($scope.items);
    }

    $scope.getStoreNamesForItem = function (itemStores) {
        var storeNames = [];

        if (itemStores) {
            for (var i = 0; i < itemStores.length; i++) {
                storeNames.push(itemStores[i].name);
            }
        }

        return storeNames.join(", ")
    };

    $scope.toggleItemSelection = function (itemId) {
        var idx = $scope.selectedItems.indexOf(itemId);
        
        if (idx > -1) {
            $scope.selectedItems.splice(idx, 1);
        } else {
            $scope.selectedItems.push(itemId);
        }
    }

    function removeFromSelectedItemList(itemId) {
        var idx = $scope.selectedItems.indexOf(itemId);
        
        if (idx > -1) {
            $scope.selectedItems.splice(idx, 1);
        }
    }

    $scope.addItemsToShoppingList = function () {
        var itemList = $scope.selectedItems.slice(0);

        var input_items = []
        for (var i = 0; i < itemList.length; i++) {
            var itemId = itemList[i];
            input_items.push({id: itemId, in_slist: true})
        }

        Item.update({}, {"items": input_items}, function(responseData, responseHeaders) {
            $route.reload();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };

    $scope.addItemToShoppingList = function (item) {
        Item.update({itemId: item.id}, {in_slist: true}, function(updatedItem, responseHeaders) {
            // Update the corresponding item in the list so that the view
            // is updated. 
            var category = "Uncategorized";
            if (item.category) {
                category = item.category.name;
            }
                
            var tempItems = $scope.items[category];
            for (var i = 0; i < tempItems.length; i++) {
                if (tempItems[i].id == item.id) {
                    tempItems[i] = updatedItem;
                    break;
                }
            }
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }
    
    $scope.removeItemFromShoppingList = function (item) {
        Item.update({itemId: item.id}, {in_slist: false}, function(updatedItem, responseHeaders) {
            // Update the corresponding item in the list so that the view
            // is updated. 
            var category = "Uncategorized";
            if (item.category) {
                category = item.category.name;
            }
                
            var tempItems = $scope.items[category];
            for (var i = 0; i < tempItems.length; i++) {
                if (tempItems[i].id == item.id) {
                    tempItems[i] = updatedItem;
                    break;
                }
            }
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }

    $scope.updateItem = function (item) {
        // Saving to be used in item update. 
        $rootScope.updateItem = item;
        $rootScope.returnPath = "items";
    }
});

app.controller('NewItemController', function ($scope, $resource, $log, jsonFilter, $location) {
    var Item = $resource('/api/items/:itemId');
    var Category = $resource('/api/categories/:categoryId');
    var Store = $resource('/api/stores/:storeId');

    $scope.listCategories = function() {
        categories_resp = Category.get(function() {
            $scope.categories = categories_resp.categories;
        })
    }

    $scope.listStores = function() {
        stores_resp = Store.get(function() {
            $scope.stores = stores_resp.stores;
        })
    }
    
    function init() {
        $scope.listStores();
        $scope.listCategories();
        $scope.rest_error_message = "";
    }

    init();

    $scope.insertItem = function () {
        if ($scope.newItem.category) {
            $scope.newItem.category = $scope.newItem.category.id;
        }

        var storeIds = [];
        var stores = $scope.newItem.stores;
        if (stores) {
            for (var i = 0; i < stores.length; i++) {
                storeIds.push(stores[i].id);
            }

            $scope.newItem.stores = storeIds;
        }
        
        var item = new Item($scope.newItem);

        item.$save({}, function() {
            $location.path("list");
            $scope.rest_error_message = '';
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };
});

app.controller('ListController', function ($scope, $route, $rootScope, $resource, $log, jsonFilter, itemsService) {
    var Item = $resource('/api/items/:itemId', null, {
        update: {
            method: 'PUT'
        }});

    $scope.listItems = function() {
        items_resp = Item.get({"view":"byCategory", "inShoppingList":true}, function() {
            $scope.items_ro = items_resp.items;
            $scope.stores = items_resp.stores;
            $scope.items = itemsService.computeItemsByStore($scope.items_ro, $scope.clearedItems, $scope.store);
        })
    }

    $scope.processStoreSelection = function(store) {
        $scope.items = itemsService.computeItemsByStore($scope.items_ro, $scope.clearedItems, $scope.store);
        itemsService.removeEmptyCategories($scope.items);
    }

    $scope.toggleItemSelection = function (itemId) {
        var idx = $scope.selectedItems.indexOf(itemId);
        
        if (idx > -1) {
            $scope.selectedItems.splice(idx, 1);
        } else {
            $scope.selectedItems.push(itemId);
        }
    }

    function removeFromSelectedItemList(itemId) {
        var idx = $scope.selectedItems.indexOf(itemId);
        
        if (idx > -1) {
            $scope.selectedItems.splice(idx, 1);
        }
    }

    function init() {
        $scope.rest_error_message = "";
        $scope.listItems();
        $scope.selectedItems = []
        $scope.clearedItems = []
        $scope.store = undefined;
        $scope.stores = []
    }

    init();

    $scope.removeItemsFromShoppingList = function () {
        var itemList = $scope.selectedItems.slice(0);

        var input_items = []
        for (var i = 0; i < itemList.length; i++) {
            var itemId = itemList[i];
            input_items.push({id: itemId, in_slist: false})
        }

        Item.update({}, {"items": input_items}, function(responseData, responseHeaders) {
            $route.reload();
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };

    $scope.removeItemFromShoppingList = function (item) {
        Item.update({itemId: item.id}, {in_slist: false}, function(updatedItem, responseHeaders) {
            removeFromSelectedItemList(item.id);
            $scope.clearedItems.push(item.id);

            // Update the corresponding item in the list so that the view
            // is updated. 
            var category = "Uncategorized";
            if (item.category) {
                category = item.category.name;
            }
            var tempItems = $scope.items[category];
            for (var i = 0; i < tempItems.length; i++) {
                if (tempItems[i].id === item.id) {
                    tempItems.splice(i, 1);
                    break;
                }
            }
            
            itemsService.removeEmptyCategories($scope.items);
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }

    $scope.updateItem = function (item) {
        // Saving to be used in item update. 
        $rootScope.updateItem = item;
        $rootScope.returnPath = "simplelist";
    }
})

app.controller('UpdateItemController', function ($scope, $rootScope, $resource, $log, jsonFilter, $location) {
    var Category = $resource('/api/categories/:categoryId');
    var Store = $resource('/api/stores/:storeId');
    var Item = $resource('/api/items/:itemId', null, {
        update: {
            method: 'PUT'
        }});

    $scope.listStores = function() {
        stores_resp = Store.get(function() {
            $scope.stores = stores_resp.stores;

            if ($scope.item.stores) {
                var currentStoreIds = $scope.item.stores.map(function(x) {
                    return x.id;
                })

                var defaultStores = []
		        for (var i = 0; i < $scope.stores.length; i++) {
                    if (currentStoreIds.indexOf($scope.stores[i].id) > -1) {
                        defaultStores.push($scope.stores[i]);
                    }
                }

                $scope.item.stores = defaultStores;
            }
        })
    }

    $scope.listCategories = function() {
        categories_resp = Category.get(function() {
            $scope.categories = categories_resp.categories;

            // For default value in select list to work, the value should be
            // one of the objects in the list.
            if ($scope.item.category) {
		        for (var i = 0; i < $scope.categories.length; i++) {
                    if ($scope.categories[i].id === $scope.item.category.id) {
                        $scope.item.category = $scope.categories[i];
                    }
                }
            }
        })
    }

    function init() {
        $scope.item = $rootScope.updateItem;
        $scope.listStores();
        $scope.listCategories();
        $scope.rest_error_message = "";
    }

    init();

    $scope.updateItem = function () {
        if ($scope.item.category) {
            $scope.item.category = $scope.item.category.id;
        }
        
        if ($scope.item.stores) {
            $scope.item.stores = $scope.item.stores.map(function(x) {
                return x.id;
            })
        }

        Item.update({itemId: $scope.item.id}, $scope.item, function() {
            $location.path($rootScope.returnPath);
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }

    $scope.cancelUpdate = function () {
        $location.path($rootScope.returnPath);
    }

    $scope.deleteItem = function (itemId) {
        new Item().$delete({itemId: itemId}, function() {
            $location.path($rootScope.returnPath);
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };
});

app.controller('UpdateCategoryController', function ($scope, $rootScope, $resource, $log, jsonFilter, $location) {
    var Category = $resource('/api/categories/:categoryId', null, {
        update: {
            method: 'PUT'
        }});

    function init() {
        $scope.category = $rootScope.updateCategory;
        $scope.rest_error_message = "";
    }

    init();

    $scope.updateCategory = function () {
        Category.update({categoryId: $scope.category.id}, $scope.category, function() {
            $location.path("categories");
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }

    $scope.cancelUpdate = function () {
        $location.path("categories");
    }

    $scope.deleteCategory = function (categoryId) {
        new Category().$delete({categoryId: categoryId}, function() {
            $location.path("categories");
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };
});

app.controller('UpdateStoreController', function ($scope, $rootScope, $resource, $log, jsonFilter, $location) {
    var Store = $resource('/api/stores/:storeId', null, {
        update: {
            method: 'PUT'
        }});

    function init() {
        $scope.store = $rootScope.updateStore;
        $scope.rest_error_message = "";
    }

    init();

    $scope.updateStore = function () {
        Store.update({storeId: $scope.store.id}, $scope.store, function() {
            $location.path("stores");
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    }

    $scope.cancelUpdate = function () {
        $location.path("stores");
    }

    $scope.deleteStore = function (storeId) {
        new Store().$delete({storeId: storeId}, function() {
            $location.path("stores");
        }, function(httpResponse) {
            $scope.rest_error_message = jsonFilter(httpResponse);
        });
    };
});

