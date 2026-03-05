package com.bookshare.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bookshare.app.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun loginScreen_isDisplayed() {
        // Wait for splash to transition
        Thread.sleep(2500)
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    @Test
    fun emptyEmail_showsError() {
        Thread.sleep(2500)
        onView(withId(R.id.btnLogin)).perform(click())
        // Snackbar should appear with validation error
        onView(withText("El correo no puede estar vacío")).check(matches(isDisplayed()))
    }

    @Test
    fun navigateToRegister_fromLogin() {
        Thread.sleep(2500)
        onView(withId(R.id.tvRegister)).perform(click())
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
    }

    @Test
    fun registerScreen_allFieldsPresent() {
        Thread.sleep(2500)
        onView(withId(R.id.tvRegister)).perform(click())
        onView(withId(R.id.etName)).check(matches(isDisplayed()))
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.etConfirmPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegister)).check(matches(isDisplayed()))
    }
}
