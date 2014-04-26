package jp.leafytree.android.libproject.app;

import android.test.ActivityInstrumentationTestCase2;

import jp.leafytree.android.libproject.lib1.Lib1ScalaActivity;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1ScalaActivity> {
    public Lib1ScalaActivityJavaTest() {
        super(Lib1ScalaActivity.class);
    }

    @Override
    public void setUp() {
        getActivity();
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        onView(withText("Lib1Java")).check(matches(isDisplayed()));
    }
}
