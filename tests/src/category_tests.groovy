package com.slist.apitests

import org.testng.Assert
import org.testng.annotations.*

import com.slist.apitests.core.TestBase
import com.slist.apitests.core.HttpBadRequestException
import com.slist.apitests.core.HttpNotFoundException
import com.slist.apitests.core.CategoriesAPI

class CategoryTests extends TestBase {
    static def TESTCATEGORY = "testcategory"

    @Test
    void create() {
        def category = categoriesAPI.create([name: TESTCATEGORY])
        try {
            Assert.assertEquals(category.name, TESTCATEGORY)
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void list() {
        def category1 = null
        def category2 = null

        try {
            category1 = categoriesAPI.create([name: "testcategory1"])
            category2 = categoriesAPI.create([name: "testcategory2"])
            def categories = categoriesAPI.list()
            Assert.assertEquals(categories.size(), 2)
            Assert.assertNotNull(categories.find({it.name == "testcategory1"}))
            Assert.assertNotNull(categories.find({it.name == "testcategory2"}))
        } finally {
            run_with_no_exceptions((category1 != null), {categoriesAPI.delete(category1.id)})
            run_with_no_exceptions((category2 != null), {categoriesAPI.delete(category2.id)})
        }
    }

    @Test
    void empty_list() {
        Assert.assertEquals(categoriesAPI.list().size(), 0)
    }

    @Test
    void get() {
        def category = categoriesAPI.create([name: TESTCATEGORY])
        try {
            def get_category = categoriesAPI.get(category.id)
            Assert.assertEquals(get_category.name, TESTCATEGORY)
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void update() {
        def category = categoriesAPI.create([name: TESTCATEGORY])
        try {
            def updated_category = categoriesAPI.update(category.id, [name: "updated_testcategory"])
            Assert.assertEquals(updated_category.name, "updated_testcategory")
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void update_with_same_name() {
        def category = categoriesAPI.create([name: TESTCATEGORY])
        try {
            def updated_category = categoriesAPI.update(category.id, [name: TESTCATEGORY])
            Assert.assertEquals(updated_category.name, TESTCATEGORY)
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void delete() {
        def category = categoriesAPI.create([name: TESTCATEGORY])
        try {
            categoriesAPI.delete(category.id)
            expectException(HttpNotFoundException.class, {
                categoriesAPI.get(category.id)
            })
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
        }
    }
}

class CategoryInvalidDataTests extends TestBase {
    static def NON_EXISTING_CATEGORY_ID = 121212121212

    @Test
    void create_with_no_name() {
        def category = null
        
        try {
            expectException(HttpBadRequestException.class, {
                category = categoriesAPI.create([:])
                })
        } finally {
            run_with_no_exceptions((category != null), {categoriesAPI.delete(category.id)})
        }
    }

    @Test
    void test_that_a_category_matching_an_existing_name_case_insensitively_can_not_be_created() {
        def dupCategory = null
        def category = categoriesAPI.create([name: "testcategory"])
        
        try {
            expectException(HttpBadRequestException.class, {
                dupCategory = categoriesAPI.create([name: "TestCategory"])
                })
        } finally {
            run_with_no_exceptions({categoriesAPI.delete(category.id)})
            run_with_no_exceptions((dupCategory != null), {categoriesAPI.delete(dupCategory.id)})
        }
    }

    @Test
    void get_of_non_existing_category() {
        expectException(HttpNotFoundException.class, {
            categoriesAPI.get(NON_EXISTING_CATEGORY_ID)
        })
    }

    @Test
    void update_of_non_existing_category() {
        expectException(HttpNotFoundException.class, {
            categoriesAPI.update(NON_EXISTING_CATEGORY_ID, [:])
        })
    }

    @Test
    void test_that_a_category_matching_an_existing_name_case_insensitively_can_not_be_updated() {
        def category1 = null
        def category2 = null
        
        try {
            category1 = categoriesAPI.create([name: "testcategory1"])
            category2 = categoriesAPI.create([name: "testcategory2"])
            
            expectException(HttpBadRequestException.class, {
                categoriesAPI.update(category2.id, [name: "Testcategory1"])
            })
        } finally {
            run_with_no_exceptions((category1 != null), {categoriesAPI.delete(category1.id)})
            run_with_no_exceptions((category2 != null), {categoriesAPI.delete(category2.id)})
        }
    }

    @Test
    void delete_of_non_existing_category() {
        expectException(HttpNotFoundException.class, {
            categoriesAPI.delete(NON_EXISTING_CATEGORY_ID)
        })
    }
}

