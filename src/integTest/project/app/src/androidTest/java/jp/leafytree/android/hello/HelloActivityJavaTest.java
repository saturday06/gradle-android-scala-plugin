package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import com.robotium.solo.Solo;
import scala.collection.concurrent.TrieMap;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity> {
    public Solo solo;

    @SuppressWarnings("deprecation")
    public HelloActivityJavaTest() {
        super("jp.leafytree.android.hello", HelloActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        solo.waitForText("Hello. I'm Java !");
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Hello. I'm Java !");
        solo.waitForText(map.apply("x"));
    }
}
