import webapp2

from handlers import StoreHandler, SingleStoreHandler
from handlers import CategoryHandler, SingleCategoryHandler
from handlers import ItemHandler, SingleItemHandler

application = webapp2.WSGIApplication([

    ('/api/stores', StoreHandler),
    ('/api/stores/(\d+)', SingleStoreHandler),

    ('/api/categories', CategoryHandler),
    ('/api/categories/(\d+)', SingleCategoryHandler),

    ('/api/items', ItemHandler),
    ('/api/items/(\d+)', SingleItemHandler),

], debug=True)
