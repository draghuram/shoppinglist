
# At the moment, it is possible to delete stores and categories even if
# they are being referenced in items. For this reason, make sure that 
# the store or category exists before trying to convert it into a resource. 

import logging
from google.appengine.ext import ndb

# The only purpose of this entity is to act as the parent of all other
# entities so that strong consistency can be guaranteed. As and when users
# are added to system, each user's entities should have one such root entity. 
class RootEntity(ndb.Model):
    pass

    @classmethod
    def get_root_entity(cls):
        return RootEntity.get_or_insert("root_entity")

class Store(ndb.Model):
    name = ndb.StringProperty(required=True)
    name_lower = ndb.ComputedProperty(lambda self: self.name.lower())

    def to_resource(self):
        data = {
            "name": self.name, "id": self.key.id()
        }
        
        return data

class Category(ndb.Model):
    name = ndb.StringProperty(required=True)
    name_lower = ndb.ComputedProperty(lambda self: self.name.lower())

    def to_resource(self):
        data = {
            "name": self.name, "id": self.key.id()
        }
        
        return data

class Item(ndb.Model):
    name = ndb.StringProperty(required=True)
    name_lower = ndb.ComputedProperty(lambda self: self.name.lower())

    count = ndb.IntegerProperty()
    size = ndb.StringProperty(default='')
    stores = ndb.KeyProperty(kind=Store, repeated=True)
    category = ndb.KeyProperty(kind=Category, repeated=False, default="Uncategorized")
    in_slist = ndb.BooleanProperty(default=False)
    comments = ndb.StringProperty(default='')

    def get_description(self):
        comps = []

        if self.count:
            comps.append(str(self.count))

        comps.append(self.size)
        comps.append(self.comments)

        return " - ".join([x for x in comps if x])
    
    def to_resource(self):
        store_resources = []
        for store in self.stores:
            store_entity = store.get()
            if store_entity:
                store_resources.append(store_entity.to_resource())
            
        data = {
            "name": self.name, "id": self.key.id(),
            "in_slist": self.in_slist,
            "stores": store_resources,
            "comments": self.comments,
            "description": self.get_description()
        }
        
        if self.count:
            data["count"] = self.count

        data["size"] = self.size
        
        if self.category:
            category_entity = self.category.get()
            if category_entity:
                data["category"] = category_entity.to_resource()

        return data


