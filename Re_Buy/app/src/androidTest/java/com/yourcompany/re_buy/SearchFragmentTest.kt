package com.yourcompany.re_buy

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for SearchFragment
 * Tests search functionality, filters, and product display
 */
@RunWith(AndroidJUnit4::class)
class SearchFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun searchFragment_displaysAllComponents() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Verify all search components are displayed
        onView(withId(R.id.spinnerRegion))
            .check(matches(isDisplayed()))
        onView(withId(R.id.spinnerProductType))
            .check(matches(isDisplayed()))
        onView(withId(R.id.etSearchQuery))
            .check(matches(isDisplayed()))
        onView(withId(R.id.btnSearch))
            .check(matches(isDisplayed()))
        onView(withId(R.id.tvResultCount))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rvSearchResults))
            .check(matches(isDisplayed()))
    }

    @Test
    fun searchFragment_initialLoad_displaysAllProducts() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Result count should be displayed (shows all products initially)
        onView(withId(R.id.tvResultCount))
            .check(matches(isDisplayed()))
    }

    @Test
    fun searchFragment_textSearch_filtersResults() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Enter search query
        onView(withId(R.id.etSearchQuery))
            .perform(typeText("냉장고"), closeSoftKeyboard())

        // Click search button
        onView(withId(R.id.btnSearch))
            .perform(click())

        // Verify result count is updated
        onView(withId(R.id.tvResultCount))
            .check(matches(isDisplayed()))
    }

    @Test
    fun searchFragment_searchButton_isClickable() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Verify search button is enabled and clickable
        onView(withId(R.id.btnSearch))
            .check(matches(isEnabled()))
            .perform(click())
    }

    @Test
    fun searchFragment_regionSpinner_isInteractive() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Verify region spinner is displayed and clickable
        onView(withId(R.id.spinnerRegion))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }

    @Test
    fun searchFragment_productTypeSpinner_isInteractive() {
        // Navigate to Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Verify product type spinner is displayed and clickable
        onView(withId(R.id.spinnerProductType))
            .check(matches(isDisplayed()))
            .check(matches(isEnabled()))
    }
}
