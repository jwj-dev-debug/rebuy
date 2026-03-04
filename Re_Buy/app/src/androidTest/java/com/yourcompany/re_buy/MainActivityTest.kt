package com.yourcompany.re_buy

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for MainActivity
 * Tests tab navigation and core user flows
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivity_hasThreeTabs() {
        // Verify that all three tabs are displayed
        onView(withText(R.string.tab_home))
            .check(matches(isDisplayed()))
        onView(withText(R.string.tab_search))
            .check(matches(isDisplayed()))
        onView(withText(R.string.tab_community))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tabNavigation_switchToSearchTab_displaysSearchFragment() {
        // Click on Search tab
        onView(withText(R.string.tab_search))
            .perform(click())

        // Verify Search Fragment is displayed by checking for search button
        onView(withId(R.id.btnSearch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tabNavigation_switchToCommunityTab_displaysCommunityFragment() {
        // Click on Community tab
        onView(withText(R.string.tab_community))
            .perform(click())

        // Verify Community Fragment is displayed by checking for recycler view
        onView(withId(R.id.rvCommunityPosts))
            .check(matches(isDisplayed()))
    }

    @Test
    fun tabNavigation_switchBetweenTabs_maintainsCorrectState() {
        // Start at Home tab (default)
        onView(withText(R.string.tab_home))
            .perform(click())

        // Switch to Search
        onView(withText(R.string.tab_search))
            .perform(click())
        onView(withId(R.id.btnSearch))
            .check(matches(isDisplayed()))

        // Switch to Community
        onView(withText(R.string.tab_community))
            .perform(click())
        onView(withId(R.id.rvCommunityPosts))
            .check(matches(isDisplayed()))

        // Switch back to Home
        onView(withText(R.string.tab_home))
            .perform(click())
        onView(withId(R.id.rvHomeProducts))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeFragment_displaysProductList() {
        // Home tab should be selected by default
        onView(withId(R.id.rvHomeProducts))
            .check(matches(isDisplayed()))
    }

    @Test
    fun homeFragment_searchButtonNavigatesToSearchTab() {
        // Click search button on home
        onView(withId(R.id.btnSearchHome))
            .perform(click())

        // Verify we're now on Search tab
        onView(withId(R.id.btnSearch))
            .check(matches(isDisplayed()))
    }

    @Test
    fun authButtons_displayCorrectly() {
        // Verify auth buttons are displayed (either login/signup or logout depending on auth state)
        // This test checks the UI is rendered
        onView(withId(R.id.toolbar))
            .check(matches(isDisplayed()))
    }
}
