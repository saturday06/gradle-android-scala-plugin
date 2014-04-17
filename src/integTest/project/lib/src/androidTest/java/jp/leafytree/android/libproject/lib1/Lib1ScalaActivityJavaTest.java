package jp.leafytree.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import scala.collection.concurrent.TrieMap;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1ScalaActivity> {
    public Solo solo;

    public Lib1ScalaActivityJavaTest() {
        super("jp.leafytree.android.libproject.lib1", Lib1ScalaActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        solo.waitForText("Lib1Java");
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Lib1Java");
        solo.waitForText(map.apply("x"));
    }
}
