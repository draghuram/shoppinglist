
.. Contents::

====================
 Shopping List APIs
====================

Introduction
============

This document describes the REST APIs for the shopping list
application. 

General
=======

- An API is described by showing sample request and response. In most
  cases, this is sufficient to describe the fields but when that is
  not the case, a more detailed explanation is provided. 

- Request data and response data sections, when present, may not list
  each and every field for a given request and response. Specifically
  optional fields that are self-explanatory may not be described
  separately.  

- When a resource is created, the response always contains an integer
  "id" field. 

Stores
======

Creating a Store
----------------

**Sample Request**::

    POST /api/stores

    {
        "name": "Target"
    }

**Sample Response**::

    200 OK
    
    {
        "id": 5275456790069248, 
        "name": "Target"
    }
    
**Status**

- 400 - Request does not contain name.

- 400 - There is an existing store with the name given in the
  request. 

**Notes**

- name is required and is case insensitive. 

Listing Stores
--------------

**Sample Request**::

    GET /api/stores

**Sample Response**::

    200 OK
    
    {
        "stores": [
            {
                "id": 4829055069192192, 
                "name": "Sports authority"
            }, 
            {
                "id": 4969792557547520, 
                "name": "Michaels"
            }, 
            ...
        ]
    }
        
Getting a single Store
----------------------

**Sample Request**::

    GET /api/stores/4829055069192192

**Sample Response**::

    200 OK
    
    {
        "id": 4829055069192192, 
        "name": "Sports authority"
    }
    
**Status**

- 404 - There is no store corresponding to given id. 

Updating a single Store
-----------------------

**Sample Request**::

    PUT /api/stores/4829055069192192

    {
        "name": "The Sports authority"
    }

**Sample Response**::

    200 OK
    
    {
        "id": 4829055069192192, 
        "name": "The Sports authority"
    }
    
**Status**

- 404 - There is no store corresponding to given id. 

- 400 - There is an existing store with the name given in the
  request. 

Deleting a single Store
-----------------------

**Sample Request**::

    DELETE /api/stores/4829055069192192

**Sample Response**::

    200 OK
    
**Status**

- 404 - There is no store corresponding to given id. 

Categories
==========

Creating a Category
-------------------

**Sample Request**::

    POST /api/categories

    {
        "name": "Fruits"
    }

**Sample Response**::

    200 OK
    
    {
        "id": 5275456790069248, 
        "name": "Fruits"
    }
    
**Status**

- 400 - Request does not contain name.

- 400 - There is an existing category with the name given in the
  request. 

**Notes**

- name is required and is case insensitive. 

Listing Categories
------------------

**Sample Request**::

    GET /api/categories

**Sample Response**::

    200 OK
    
    {
        "categories": [
            {
                "id": 4829055069192192, 
                "name": "Fruits"
            }, 
            {
                "id": 4969792557547520, 
                "name": "Vegetables"
            }, 
            ...
        ]
    }
        

Getting a single Category
-------------------------

**Sample Request**::

    GET /api/categories/4829055069192192

**Sample Response**::

    200 OK
    
    {
        "id": 4829055069192192, 
        "name": "Fruits"
    }
    
**Status**

- 404 - There is no category corresponding to given id. 

Updating a single Category
--------------------------

**Sample Request**::

    PUT /api/categories/4829055069192192

    {
        "name": "Ripe Fruits"
    }

**Sample Response**::

    200 OK
    
    {
        "id": 4829055069192192, 
        "name": "Ripe Fruits"
    }
    
**Status**

- 404 - There is no category corresponding to given id. 

- 400 - There is an existing category with the name given in the
  request. 

Deleting a single Category
--------------------------

**Sample Request**::

    DELETE /api/categories/4829055069192192

**Sample Response**::

    200 OK
    
**Status**

- 404 - There is no category corresponding to given id. 


Items
=====

Creating a Item
---------------

**Sample Request**::

    POST /api/items

    {
        "name": "Milk",
        "count": 1,
        "size": "1 Gallon",
        "in_slist": false,
        "comments": "test comment",
        "stores": [5983542278356992],
        "category": 5420592324935680
    }
    
**Sample Response**::

    200 OK
    
    {
        "category": {
            "id": 5420592324935680, 
            "name": "Diary"
        }, 
        "comments": "test comment", 
        "count": 1, 
        "description": "1 - 1 Gallon - test comment", 
        "id": 6546492231778304, 
        "in_slist": false, 
        "name": "Milk", 
        "size": "1 Gallon", 
        "stores": [
            {
                "id": 5983542278356992, 
                "name": "Costco"
            }
        ]
    }

**Sample Request Data**

All fields except "name" are optional. 

name
    Required. Name of the item. Should be unique (case insensitive). 

in_slist
    Optional. When set to "true", the item would be added to shopping
    list. 

stores
    Optional. List of stores that are usually the places where this
    item is purchased. 

category, count, size, comments
    Optional. 

**Sample Response Data**

description
    A short string that is combination of some fields such as "count",
    "size', and "combination". 
        
**Status**

- 400 - Request does not contain name.

- 400 - There is an existing item with the name given in the
  request. 

- 400 - Given category doesn't exist. 

- 400 - One or more given stores do not exist. 

Listing Items as a flat list
----------------------------

**Sample Request**::

    GET /api/items

**Sample Response**::

    200 OK
    
    {
        "items": [
            {
                "comments": "", 
                "description": "", 
                "id": 5301845069135872, 
                "in_slist": true, 
                "name": "testitem", 
                "stores": [
                        {
                            "id": 5310641162158080, 
                            "name": "Costco"
                        }
                 ]
            }
        ],

        "stores": [
                {
                    "id": 5310641162158080, 
                    "name": "Costco"
                }
         ]
    }
    

**Notes**

- Items can be filtered based on whether they are added to the
  shopping list or not. To list items in shopping list, use::
 
    GET /api/items?inShoppingList=true

  To list items that are not in the shopping list, use::

    GET /api/items?inShoppingList=false

- "stores" contains list of stores corresponding to the returned
  items. 

Listing Items by category
-------------------------

**Sample Request**::

    GET /api/items?view=byCategory

**Sample Response**::

    200 OK
    
    {
        "items": {
            "Fruits": [
                {
                    "comments": "", 
                    "description": "", 
                    "id": 5301845069135872, 
                    "in_slist": false, 
                    "name": "Apples", 
                    "stores": []
                }
            ]
        },

        "stores": [ ]
    }

**Notes**

- Items without a category are listed under the special category
  called "Uncategorized". 

- Items can be filtered based on whether they are added to the
  shopping list or not. For details, see above. 

- "stores" contains list of stores corresponding to the returned
  items. 

Listing Items by store and category
-----------------------------------

**Sample Request**::

    GET /api/items?view=byStoreAndCategory

**Sample Response**::

    200 OK
    
    {
        "items": {
            "Costco": {
                "Fruits": [
                    {
                        "comments": "", 
                        "description": "", 
                        "id": 5301845069135872, 
                        "in_slist": false, 
                        "name": "Apples", 
                        "stores": []
                    }
                ]
            }
        },

        "stores": [ ]
    }
    
**Notes**

- Items without a store are listed under the special store called
  "Misc".  

- Items without a category are listed under the special category
  called "Uncategorized". 

- Items can be filtered based on whether they are added to the
  shopping list or not. For details, see above. 

- "stores" contains list of stores corresponding to the returned
  items. 

Getting a single Item
---------------------

**Sample Request**::

    GET /api/items/4829055069192192

**Sample Response**::

    200 OK
    
    {
        "id": 4829055069192192, 
        "name": "Apples"
    }
    
**Status**

- 404 - There is no item corresponding to given id. 

Updating a single Item
----------------------

**Sample Request**::

    PUT /api/items/4829055069192192

    {
        "name": "Green Apples"
    }

**Sample Response**::

    200 OK

    {
        "id": 4829055069192192, 
        "name": "Green Apples"
    }
    
**Status**

- 404 - There is no item corresponding to given id. 

- 400 - There is an existing item with the name given in the
  request. 

- Check the item creation API for more possible response scenarios.  

**Notes**

- Only the fields which are present in the request are updated. To
  delete the current value of a field, the request should contain the
  field that needs to be updated with the "empty" value (0 or "" or
  [] depending on the type of the field). 

  - To reset the category, use 0 for category id in the request. 

- Check the item creation API for details about the fields. 

Updating multiple items
-----------------------

**Sample Request**::

    PUT /api/items
    
    {
        "items": [
            {
                "id": 6575629289914368,
                "name": "item1"
            },
            {
                "id": 4746041941295104,
                "name": "item2"
            }
        ]
    }
    
**Sample Response**::

    200 OK

    {
        "items": [
            {
                "in_slist": false,
                "name": "item1",
                "stores": [
                    
                ],
                "size": "",
                "comments": "",
                "id": 6575629289914368,
                "description": "0 -  - ",
                "count": 0
            },
            {
                "in_slist": false,
                "name": "item2",
                "stores": [
                    
                ],
                "size": "",
                "comments": "",
                "id": 4746041941295104,
                "description": "0 -  - ",
                "count": 0
            }
        ]
    }
        
**Status**

- If there is any problem in updating an item in the input list (such
  as non-existing ID), that item is simply skipped. 

**Notes**

- Check the above section on updating a single item for more details.  

Deleting a single Item
----------------------

**Sample Request**::

    DELETE /api/items/4829055069192192

**Sample Response**::

    200 OK
    
**Status**

- 404 - There is no item corresponding to given id. 



