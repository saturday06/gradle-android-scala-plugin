package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import scala.collection.concurrent.TrieMap;

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

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        onView(withText("Hello. I'm Java !")).check(matches(isDisplayed()));
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Hello. I'm Java !");
        solo.waitForText(map.apply("x"));
    }
}
