package com.slist.apitests

import org.testng.Assert
import org.testng.annotations.*

import com.slist.apitests.core.TestBase
import com.slist.apitests.core.HttpBadRequestException
import com.slist.apitests.core.HttpNotFoundException
import com.slist.apitests.core.ItemsAPI

class ItemTests extends TestBase {
    static def TESTITEM = "testitem"

    @Test
    void create_with_only_name() {
        def item = itemsAPI.create([name: TESTITEM])
        try {
            Assert.assertEquals(item.name, TESTITEM)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void create() {
        def store = null
        def category = null
        def item = null

        try {
            store = storesAPI.create([name: "teststore"])
            category = categoriesAPI.create([name: "testcategory"])
            item = itemsAPI.create([name: TESTITEM, count: 1, size: "1 Gallon", 
                                    comments: "test comments", stores: [store.id],
                                    category: category.id])
            Assert.assertEquals(item.name, TESTITEM)
            Assert.assertEquals(item.comments, "test comments")
        } finally {
            run_with_no_exceptions((item != null), {itemsAPI.delete(item.id)})
            run_with_no_exceptions((store != null), {storesAPI.delete(store.id)})
            run_with_no_exceptions((category != null), {categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void list() {
        def (item1, item2) = [null, null]

        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2"])
            def items = itemsAPI.list()
            Assert.assertEquals(items.size(), 2)
            Assert.assertNotNull(items.find({it.name == "testitem1"}))
            Assert.assertNotNull(items.find({it.name == "testitem2"}))
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void list_items_in_shopping_list() {
        def (item1, item2) = [null, null]

        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2", in_slist: true])
            def items = itemsAPI.list("inShoppingList=true")
            Assert.assertEquals(items.size(), 1)
            Assert.assertNotNull(items[0].name, "testitem2")
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void list_items_not_in_shopping_list() {
        def (item1, item2) = [null, null]

        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2", in_slist: true])
            def items = itemsAPI.list("inShoppingList=false")
            Assert.assertEquals(items.size(), 1)
            Assert.assertNotNull(items[0].name, "testitem1")
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void list_by_category() {
        def (item1, item2, category) = [null, null, null]

        try {
            category = categoriesAPI.create([name: "testcategory"])
            item1 = itemsAPI.create([name: "testitem1", category: category.id])
            item2 = itemsAPI.create([name: "testitem2"])

            def items = itemsAPI.list("view=byCategory")

            Assert.assertEquals(items.size(), 2)
            Assert.assertEquals(items["Uncategorized"][0].name, "testitem2")
            Assert.assertEquals(items["testcategory"][0].name, "testitem1")
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
            run_with_no_exceptions((category != null), {categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void list_by_store_and_category() {
        def (item1, item2, store) = [null, null, null]

        try {
            store = storesAPI.create([name: "teststore"])
            item1 = itemsAPI.create([name: "testitem1", stores: [store.id]])
            item2 = itemsAPI.create([name: "testitem2"])

            def resp = itemsAPI.get(null, "view=byStoreAndCategory")
            def items = resp.items
            def stores = resp.stores

            Assert.assertEquals(items["Misc"].size(), 1)
            Assert.assertEquals(items["teststore"].size(), 1)
            Assert.assertEquals(stores.size(), 1)
            Assert.assertEquals(stores[0].id, store.id)
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
            run_with_no_exceptions((store != null), {storesAPI.delete(store.id)})
        }
    }

    @Test
    void empty_list() {
        Assert.assertEquals(itemsAPI.list().size(), 0)
    }

    @Test
    void get() {
        def item = itemsAPI.create([name: TESTITEM])
        try {
            def get_item = itemsAPI.get(item.id)
            Assert.assertEquals(get_item.name, TESTITEM)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update() {
        def item = itemsAPI.create([name: TESTITEM])
        try {
            def updated_item = itemsAPI.update(item.id, [name: "updated_testitem"])
            Assert.assertEquals(updated_item.name, "updated_testitem")
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_with_same_name() {
        def item = itemsAPI.create([name: TESTITEM])
        try {
            def updated_item = itemsAPI.update(item.id, [name: TESTITEM])
            Assert.assertEquals(updated_item.name, TESTITEM)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_count() {
        def item = itemsAPI.create([name: TESTITEM, count: 1])
        try {
            def updated_item = itemsAPI.update(item.id, [count: 2])
            Assert.assertEquals(updated_item.count, 2)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_unset_count() {
        def item = itemsAPI.create([name: TESTITEM, count: 1])
        try {
            def updated_item = itemsAPI.update(item.id, [count: 0])
            Assert.assertNull(updated_item.count)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_size() {
        def item = itemsAPI.create([name: TESTITEM, size: "1 Gallon"])
        try {
            def updated_item = itemsAPI.update(item.id, [size: "2 Gallons"])
            Assert.assertEquals(updated_item.size, "2 Gallons")
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_unset_size() {
        def item = itemsAPI.create([name: TESTITEM, size: "1 Gallon"])
        try {
            def updated_item = itemsAPI.update(item.id, [size: ""])
            Assert.assertEquals(updated_item.size, "")
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void add_to_shopping_list() {
        def item = itemsAPI.create([name: TESTITEM, in_slist: false])
        try {
            def updated_item = itemsAPI.update(item.id, [in_slist: true])
            Assert.assertEquals(updated_item.in_slist, true)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void remove_from_shopping_list() {
        def item = itemsAPI.create([name: TESTITEM, in_slist: true])
        try {
            def updated_item = itemsAPI.update(item.id, [in_slist: false])
            Assert.assertEquals(updated_item.in_slist, false)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_comments() {
        def item = itemsAPI.create([name: TESTITEM, comments: "test comments"])
        try {
            def updated_item = itemsAPI.update(item.id, [comments: "updated comments"])
            Assert.assertEquals(updated_item.comments, "updated comments")
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_unset_comments() {
        def item = itemsAPI.create([name: TESTITEM, comments: "test comments"])
        try {
            def updated_item = itemsAPI.update(item.id, [comments: ""])
            Assert.assertEquals(updated_item.comments, "")
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }

    @Test
    void update_stores() {
        def (item, store1, store2) = [null, null, null]

        try {
            store1 = storesAPI.create([name: "teststore1"])
            store2 = storesAPI.create([name: "teststore2"])
            item = itemsAPI.create([name: "testitem", stores: [store1.id]])

            def updated_item = itemsAPI.update(item.id, [stores: [store2.id]])
            Assert.assertEquals(updated_item.stores[0].id, store2.id)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
            run_with_no_exceptions((store1 != null), {storesAPI.delete(store1.id)})
            run_with_no_exceptions((store2 != null), {storesAPI.delete(store2.id)})
        }
    }

    @Test
    void update_unset_stores() {
        def (item, store) = [null, null]

        try {
            store = storesAPI.create([name: "teststore"])
            item = itemsAPI.create([name: "testitem", stores: [store.id]])

            def updated_item = itemsAPI.update(item.id, [stores: []])
            Assert.assertEquals(updated_item.stores.size(), 0)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
            run_with_no_exceptions((store != null), {storesAPI.delete(store.id)})
        }
    }

    @Test
    void update_category() {
        def (item, category1, category2) = [null, null, null]

        try {
            category1 = categoriesAPI.create([name: "testcategory1"])
            category2 = categoriesAPI.create([name: "testcategory2"])
            item = itemsAPI.create([name: "testitem", category: category1.id])

            def updated_item = itemsAPI.update(item.id, [category: category2.id])
            Assert.assertEquals(updated_item.category.id, category2.id)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
            run_with_no_exceptions((category1 != null), {categoriesAPI.delete(category1.id)})
            run_with_no_exceptions((category2 != null), {categoriesAPI.delete(category2.id)})
        }
    }

    @Test
    void update_unset_category() {
        def (item, category) = [null, null]

        try {
            category = categoriesAPI.create([name: "testcategory"])
            item = itemsAPI.create([name: "testitem", category: category.id])

            def updated_item = itemsAPI.update(item.id, [category: 0])
            Assert.assertNull(updated_item.category)
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
            run_with_no_exceptions((category != null), {categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void update_multiple_items() {
        def (item1, item2) = [null, null]

        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2"])

            def update_item1 = [id: item1.id, name: "item1"]
            def update_item2 = [id: item2.id, name: "item2"]
            def items = itemsAPI.update_multiple([items: [update_item1, update_item2]])

            Assert.assertEquals(items.size(), 2)
            Assert.assertNotNull(items.find({it.name == "item1"}))
            Assert.assertNotNull(items.find({it.name == "item2"}))
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void update_multiple_items_with_one_invalid_item() {
        def (item1, item2) = [null, null]

        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2"])

            def update_item1 = [id: item1.id, name: "item1"]
            def update_item2 = [id: item2.id, name: "item2"]
            def update_item3 = [id: 12345, name: "item3"]
            def items = itemsAPI.update_multiple([items: [update_item1, update_item2, update_item3]])

            Assert.assertEquals(items.size(), 2)
            Assert.assertNotNull(items.find({it.name == "item1"}))
            Assert.assertNotNull(items.find({it.name == "item2"}))
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void delete() {
        def item = itemsAPI.create([name: TESTITEM])
        try {
            itemsAPI.delete(item.id)
            expectException(HttpNotFoundException.class, {
                itemsAPI.get(item.id)
            })
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
        }
    }
}

class ItemInvalidDataTests extends TestBase {
    static def NON_EXISTING_ITEM_ID = 121212121212

    @Test
    void create_with_no_name() {
        def item = null
        
        try {
            expectException(HttpBadRequestException.class, {
                item = itemsAPI.create([:])
                })
        } finally {
            run_with_no_exceptions((item != null), {itemsAPI.delete(item.id)})
        }
    }

    @Test
    void create_with_invalid_category() {
        def item = null
        
        try {
            expectException(HttpBadRequestException.class, {
                item = itemsAPI.create([name: "testitem", category: 1234])
                })
        } finally {
            run_with_no_exceptions((item != null), {itemsAPI.delete(item.id)})
        }
    }

    @Test
    void create_with_invalid_store() {
        def item = null
        
        try {
            expectException(HttpBadRequestException.class, {
                item = itemsAPI.create([name: "testitem", stores: [1234]])
                })
        } finally {
            run_with_no_exceptions((item != null), {itemsAPI.delete(item.id)})
        }
    }

    @Test
    void test_that_a_item_matching_an_existing_name_case_insensitively_can_not_be_created() {
        def dupItem = null
        def item = itemsAPI.create([name: "testitem"])
        
        try {
            expectException(HttpBadRequestException.class, {
                dupItem = itemsAPI.create([name: "TestItem"])
                })
        } finally {
            run_with_no_exceptions({itemsAPI.delete(item.id)})
            run_with_no_exceptions((dupItem != null), {itemsAPI.delete(dupItem.id)})
        }
    }

    @Test
    void get_of_non_existing_item() {
        expectException(HttpNotFoundException.class, {
            itemsAPI.get(NON_EXISTING_ITEM_ID)
        })
    }

    @Test
    void update_of_non_existing_item() {
        expectException(HttpNotFoundException.class, {
            itemsAPI.update(NON_EXISTING_ITEM_ID, [:])
        })
    }

    @Test
    void test_that_a_item_matching_an_existing_name_case_insensitively_can_not_be_updated() {
        def item1 = null
        def item2 = null
        
        try {
            item1 = itemsAPI.create([name: "testitem1"])
            item2 = itemsAPI.create([name: "testitem2"])
            
            expectException(HttpBadRequestException.class, {
                itemsAPI.update(item2.id, [name: "Testitem1"])
            })
        } finally {
            run_with_no_exceptions((item1 != null), {itemsAPI.delete(item1.id)})
            run_with_no_exceptions((item2 != null), {itemsAPI.delete(item2.id)})
        }
    }

    @Test
    void delete_of_non_existing_item() {
        expectException(HttpNotFoundException.class, {
            itemsAPI.delete(NON_EXISTING_ITEM_ID)
        })
    }
}

