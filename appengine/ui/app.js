var app = angular.module('ShoppingListApp', ['ngRoute', 'ngResource']);

app.config(function ($routeProvider) {
    $routeProvider
        .when('/list',
            {
                controller: 'ListController',
                templateUrl: 'ui/partials/list.html'
            })
        .when('/stores',
            {
                controller: 'StoreController',
                templateUrl: 'ui/partials/stores.html'
            })
        .when('/items',
            {
                controller: 'ItemController',
                templateUrl: 'ui/partials/items.html'
            })
        .when('/items/new',
            {
                controller: 'NewItemController',
                templateUrl: 'ui/partials/newitem.html'
            })
        .when('/items/updateitem/:itemId',
            {
                controller: 'UpdateItemController',
                templateUrl: 'ui/partials/updateitem.html'
            })
        .when('/categories',
            {
                controller: 'CategoryController',
                templateUrl: 'ui/partials/categories.html'
            })
        .when('/categories/updatecategory/:categoryId',
            {
                controller: 'UpdateCategoryController',
                templateUrl: 'ui/partials/updatecategory.html'
            })
        .when('/stores/updatestore/:storeId',
            {
                controller: 'UpdateStoreController',
                templateUrl: 'ui/partials/updatestore.html'
            })
        .when('/help',
            {
                templateUrl: 'ui/partials/help.html'
            })
        .otherwise({ redirectTo: '/list' });
});

app.run(function($http) {
    $http.defaults.headers.common.Accept = "application/json";
    $http.defaults.headers.common["Content-Type"] = "application/json";
});

