package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import scala.collection.concurrent.TrieMap;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity> {
    @SuppressWarnings("deprecation")
    public HelloActivityJavaTest() {
        super("jp.leafytree.android.hello", HelloActivity.class);
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals("Hello. I'm Java !", ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Hello. I'm Java !");
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }
}
