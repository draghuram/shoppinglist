import collections
import logging
import json
import traceback

import webapp2
from google.appengine.ext import ndb

from models import Store, Category, Item, RootEntity

class SlistException(Exception):
    pass

class StoreHandler(webapp2.RequestHandler):
    def post(self):
        try:
            input_store = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        name = input_store.get('name', None)
        if not name:
            self.response.set_status(400)
            return

        store = Store.query(Store.name_lower == name.lower()).get()
        if store is not None:
            self.response.set_status(400)
            return

        store = Store(parent=RootEntity.get_root_entity().key, name=input_store['name'])
        store.put()

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(store.to_resource()))

    def get(self):
        stores = ndb.gql("SELECT * from Store where ancestor is :1", RootEntity.get_root_entity().key)
        stores_json = {"stores": [store.to_resource() for store in stores]}

        self.response.headers['Content-Type'] = 'application/json'

        self.response.headers['Cache-Control'] = 'private, no-cache, no-store'

        self.response.write(json.dumps(stores_json))

class SingleStoreHandler(webapp2.RequestHandler):
    def get(self, store_id):
        store = Store.get_by_id(int(store_id), parent=RootEntity.get_root_entity().key)
        if not store:
            self.response.set_status(404)
            return

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(store.to_resource()))

    def put(self, store_id):
        store_id = int(store_id)

        store = Store.get_by_id(store_id, parent=RootEntity.get_root_entity().key)
        if not store:
            self.response.set_status(404)
            return

        try:
            input_store = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        new_name = input_store.get('name', None)
        if new_name:
            dup_store = Store.query(Store.name_lower == new_name.lower()).get()
            if (dup_store is not None) and (dup_store.key.id() != store_id):
                self.response.set_status(400)
                return

            store.name = new_name
            store.put()

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(store.to_resource()))

    def delete(self, store_id):
        store = Store.get_by_id(int(store_id), parent=RootEntity.get_root_entity().key)
        if not store:
            self.response.set_status(404)
            return

        store.key.delete()

class CategoryHandler(webapp2.RequestHandler):
    def post(self):
        try:
            input_category = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        name = input_category.get('name', None)
        if not name:
            self.response.set_status(400)
            return
        
        category = Category.query(Category.name_lower == name.lower()).get()
        if category is not None:
            self.response.set_status(400)
            return

        category = Category(parent=RootEntity.get_root_entity().key, name=input_category['name'])
        category.put()

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(category.to_resource()))

    def get(self):
        categories = ndb.gql("SELECT * from Category where ancestor is :1", RootEntity.get_root_entity().key)
        categories_json = {"categories": [category.to_resource() for category in categories]}

        self.response.headers['Cache-Control'] = 'private, no-cache, no-store'
        self.response.headers['Content-Type'] = 'application/json'

        self.response.write(json.dumps(categories_json))

class SingleCategoryHandler(webapp2.RequestHandler):
    def get(self, category_id):
        category = Category.get_by_id(int(category_id), parent=RootEntity.get_root_entity().key)
        if not category:
            self.response.set_status(404)
            return

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(category.to_resource()))

    def put(self, category_id):
        category_id = int(category_id)

        category = Category.get_by_id(category_id, parent=RootEntity.get_root_entity().key)
        if not category:
            self.response.set_status(404)
            return
        
        try:
            input_category = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        new_name = input_category.get('name', None)
        if new_name:
            dup_category = Category.query(Category.name_lower == new_name.lower()).get()
            if (dup_category is not None) and (dup_category.key.id() != category_id):
                self.response.set_status(400)
                return

            category.name = new_name
            category.put()

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(category.to_resource()))

    def delete(self, category_id):
        category = Category.get_by_id(int(category_id), parent=RootEntity.get_root_entity().key)
        if not category:
            self.response.set_status(404)
            return

        category.key.delete()

def get_boolean(val):
    if type(val) is bool:
        return val

    return val.lower() in ["true", "1", "t"]

def update_item(item_id, item, input_item, http_response):
    logging.info("input_item = %r" % input_item)

    new_category_id = input_item.get('category')
    if input_item.has_key('category'):
        new_category_id = input_item.get('category')
        if new_category_id:
            new_category_key = ndb.Key(RootEntity, "root_entity", Category, new_category_id)
            if not new_category_key:
                http_response.set_status(400)
                raise SlistException("Bad Request")

            item.category = ndb.Key(RootEntity, "root_entity", Category, new_category_id)
        else:
            item.category = None

    new_name = input_item.get('name', None)
    if new_name:
        dup_item = Item.query(Item.name_lower == new_name.lower()).get()
        if (dup_item is not None) and (dup_item.key.id() != item_id):
            http_response.set_status(400)
            raise SlistException("Bad Request")

        item.name = new_name

    new_comments = input_item.get('comments', None)
    if new_comments is not None:
        item.comments = new_comments

    new_count = input_item.get('count', None)
    if new_count is not None:
        item.count = new_count

    new_size = input_item.get('size', None)
    if new_size is not None:
        item.size = new_size

    new_in_slist = input_item.get('in_slist')
    if new_in_slist is not None:
        item.in_slist = get_boolean(new_in_slist)

    if input_item.has_key('stores'):
        new_store_ids = input_item.get('stores')
        if new_store_ids:
            item.stores = [ndb.Key(RootEntity, "root_entity", Store, store_id) for store_id in new_store_ids]
        else:
            item.stores = []

    item.put()

    return item

class ItemHandler(webapp2.RequestHandler):
    def post(self):
        try:
            input_item = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        name = input_item.get('name', None)
        if not name:
            self.response.set_status(400)
            return
        
        item = Item.query(Item.name_lower == name.lower()).get()
        if item is not None:
            self.response.set_status(400)
            return

        category_key = None
        category_id = input_item.get('category', None)
        logging.info("category_id: %r" % category_id)
        if category_id:
            category_key = ndb.Key(RootEntity, "root_entity", Category, category_id)
            if not category_key.get():
                self.response.set_status(400)
                return

        store_keys = []
        store_ids = input_item.get('stores', [])
        if store_ids:
            store_keys = [ndb.Key(RootEntity, "root_entity", Store, store_id) for store_id in store_ids]
            if None in [x.get() for x in store_keys]:
                self.response.set_status(400)
                return

            
        item = Item(parent=RootEntity.get_root_entity().key, name=name,
                    count = input_item.get('count', 0),
                    size = input_item.get('size', ''),
                    comments = input_item.get('comments', ''),
                    in_slist = get_boolean(input_item.get('in_slist', False)),
                    stores = store_keys,
                    category = category_key
                )

        item.put()

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(item.to_resource()))

    def list_items_by_store_and_category(self, item_resources):
        items_by_store_and_category = {}

        for item_res in item_resources:
            category_res = item_res.get('category', None)
            if category_res:
                category = category_res['name']
            else:
                category = "Uncategorized"

            stores = [s['name'] for s in item_res['stores']]
            if not stores:
                stores = ["Misc"]

            for store in stores:
                if not items_by_store_and_category.has_key(store):
                    items_by_store_and_category[store] = collections.defaultdict(list)
                    
                items_by_store_and_category[store][category].append(item_res)

        return items_by_store_and_category

    def list_items_by_category(self, item_resources):
        items_by_category = collections.defaultdict(list)

        for item_res in item_resources:
            category_res = item_res.get('category', None)
            if category_res:
                category = category_res['name']
            else:
                category = "Uncategorized"
            
            items_by_category[category].append(item_res)

        return items_by_category

    def filter(self, items, inShoppingList):
        filtered_items = []

        for item in items:
            if inShoppingList == item.in_slist:
                filtered_items.append(item)

        return filtered_items

    def get_stores(self, item_resources):
        stores = {}
        for item in item_resources:
            if not item['stores']:
                continue
                
            for store in item['stores']:
                store_id = store['id']
                if store_id not in stores:
                    stores[store_id] = store

        return stores.values()

    def get(self):
        items_q = ndb.gql("SELECT * from Item where ancestor is :1", RootEntity.get_root_entity().key)
        items = [item for item in items_q]

        inShoppingList = self.request.params.get('inShoppingList', None)
        if  inShoppingList is not None:
            items = self.filter(items, get_boolean(inShoppingList))

        item_resources = [item.to_resource() for item in items]
        if self.request.params.get('view', None) == "byCategory":
            items_json = {"items": self.list_items_by_category(item_resources)}
        elif self.request.params.get('view', None) == "byStoreAndCategory":
            items_json = {"items": self.list_items_by_store_and_category(item_resources)}
        else:
            items_json = {"items": item_resources}

        stores = self.get_stores(item_resources)
        logging.info("Stores = %r", stores)
        items_json["stores"] = stores

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(items_json))

    def put(self):
        try:
            input_data = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        input_items = input_data['items']

        updated_items = []
        for input_item in input_items:
            item_id = int(input_item['id'])
            item = Item.get_by_id(item_id, parent=RootEntity.get_root_entity().key)
            if not item:
                logging.info("Could not find item for %d" % item_id)
                continue

            try:
                item = update_item(item_id, item, input_item, self.response)
            except:
                traceback.print_exc()
                logging.info("There has been some problem in updating item with %d" % item_id)
                continue

            updated_items.append(item)

        item_resources = [item.to_resource() for item in updated_items]
        output_data = {"items": item_resources}

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(output_data))

class SingleItemHandler(webapp2.RequestHandler):
    def get(self, item_id):
        item = Item.get_by_id(int(item_id), parent=RootEntity.get_root_entity().key)
        if not item:
            self.response.set_status(404)
            return

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(item.to_resource()))

    def put(self, item_id):
        item_id = int(item_id)

        item = Item.get_by_id(item_id, parent=RootEntity.get_root_entity().key)
        if not item:
            self.response.set_status(404)
            return

        try:
            input_item = json.loads(self.request.body)
        except ValueError:
            self.response.set_status(400)
            return

        try:
            item = update_item(item_id, item, input_item, self.response)
        except SlistException:
            return

        self.response.headers['Content-Type'] = 'application/json'
        self.response.write(json.dumps(item.to_resource()))

    def delete(self, item_id):
        item = Item.get_by_id(int(item_id), parent=RootEntity.get_root_entity().key)
        if not item:
            self.response.set_status(404)
            return

        item.key.delete()

