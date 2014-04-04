package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity> {
    public HelloActivityJavaTest() {
        super(HelloActivity.class);
    }

    @Override
    public void setUp() {
        getActivity();
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        onView(withText("Hello. I'm Java !")).check(matches(isDisplayed()));
    }
}
