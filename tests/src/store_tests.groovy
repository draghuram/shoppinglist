package com.slist.apitests

import org.testng.Assert
import org.testng.annotations.*

import com.slist.apitests.core.TestBase
import com.slist.apitests.core.HttpBadRequestException
import com.slist.apitests.core.HttpNotFoundException
import com.slist.apitests.core.StoresAPI

class StoreTests extends TestBase {
    static def TESTSTORE = "teststore"

    @Test
    void create() {
        def store = storesAPI.create([name: TESTSTORE])
        try {
            Assert.assertEquals(store.name, TESTSTORE)
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
        }
    }

    @Test
    void list() {
        def store1 = null
        def store2 = null

        try {
            store1 = storesAPI.create([name: "teststore1"])
            store2 = storesAPI.create([name: "teststore2"])
            def stores = storesAPI.list()
            Assert.assertEquals(stores.size(), 2)
            Assert.assertNotNull(stores.find({it.name == "teststore1"}))
            Assert.assertNotNull(stores.find({it.name == "teststore2"}))
        } finally {
            run_with_no_exceptions((store1 != null), {storesAPI.delete(store1.id)})
            run_with_no_exceptions((store2 != null), {storesAPI.delete(store2.id)})
        }
    }

    @Test
    void empty_list() {
        Assert.assertEquals(storesAPI.list().size(), 0)
    }

    @Test
    void get() {
        def store = storesAPI.create([name: TESTSTORE])
        try {
            def get_store = storesAPI.get(store.id)
            Assert.assertEquals(get_store.name, TESTSTORE)
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
        }
    }

    @Test
    void update() {
        def store = storesAPI.create([name: TESTSTORE])
        try {
            def updated_store = storesAPI.update(store.id, [name: "updated_teststore"])
            Assert.assertEquals(updated_store.name, "updated_teststore")
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
        }
    }

    @Test
    void update_with_same_name() {
        def store = storesAPI.create([name: TESTSTORE])
        try {
            def updated_store = storesAPI.update(store.id, [name: TESTSTORE])
            Assert.assertEquals(updated_store.name, TESTSTORE)
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
        }
    }

    @Test
    void delete() {
        def store = storesAPI.create([name: TESTSTORE])
        try {
            storesAPI.delete(store.id)
            expectException(HttpNotFoundException.class, {
                storesAPI.get(store.id)
            })
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
        }
    }
}

class StoreInvalidDataTests extends TestBase {
    static def NON_EXISTING_STORE_ID = 121212121212

    @Test
    void create_with_no_name() {
        def store = null
        
        try {
            expectException(HttpBadRequestException.class, {
                store = storesAPI.create([:])
                })
        } finally {
            run_with_no_exceptions((store != null), {storesAPI.delete(store.id)})
        }
    }

    @Test
    void test_that_a_store_matching_an_existing_name_case_insensitively_can_not_be_created() {
        def dupStore = null
        def store = storesAPI.create([name: "teststore"])
        
        try {
            expectException(HttpBadRequestException.class, {
                dupStore = storesAPI.create([name: "TestStore"])
                })
        } finally {
            run_with_no_exceptions({storesAPI.delete(store.id)})
            run_with_no_exceptions((dupStore != null), {storesAPI.delete(dupStore.id)})
        }
    }

    @Test
    void get_of_non_existing_store() {
        expectException(HttpNotFoundException.class, {
            storesAPI.get(NON_EXISTING_STORE_ID)
        })
    }

    @Test
    void update_of_non_existing_store() {
        expectException(HttpNotFoundException.class, {
            storesAPI.update(NON_EXISTING_STORE_ID, [:])
        })
    }

    @Test
    void test_that_a_store_matching_an_existing_name_case_insensitively_can_not_be_updated() {
        def store1 = null
        def store2 = null
        
        try {
            store1 = storesAPI.create([name: "teststore1"])
            store2 = storesAPI.create([name: "teststore2"])
            
            expectException(HttpBadRequestException.class, {
                storesAPI.update(store2.id, [name: "Teststore1"])
            })
        } finally {
            run_with_no_exceptions((store1 != null), {storesAPI.delete(store1.id)})
            run_with_no_exceptions((store2 != null), {storesAPI.delete(store2.id)})
        }
    }

    @Test
    void delete_of_non_existing_store() {
        expectException(HttpNotFoundException.class, {
            storesAPI.delete(NON_EXISTING_STORE_ID)
        })
    }
}

