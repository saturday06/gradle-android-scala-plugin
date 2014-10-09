package jp.leafytree.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;


import scala.collection.concurrent.TrieMap;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1ScalaActivity> {
    public Lib1ScalaActivityJavaTest() {
        super("jp.leafytree.android.libproject.lib1", Lib1ScalaActivity.class);
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals("Lib1Java", ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", "Lib1Java");
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }
}
