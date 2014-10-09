package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import scala.collection.concurrent.TrieMap;

public class HelloJavaActivityTest extends ActivityInstrumentationTestCase2<HelloJavaActivity> {
    String flavor;

    @SuppressWarnings("deprecation")
    public HelloJavaActivityTest() {
        super("jp.leafytree.android.hello", HelloJavaActivity.class);
    }

    @Override
    public void setUp() {
        flavor = getInstrumentation().getTargetContext().getPackageName().replaceFirst(".*\\.", "");
    }

    public void testSimpleAssertion() {
        assertTrue(true);
    }

    public void testSimpleActivityAssertion() {
        assertEquals(flavor + "Java" + flavor + "Scala", ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }

    public void testCallScalaLibraryClassOfNotUsedByMainApp() {
        TrieMap<String, String> map = new TrieMap<String, String>();
        map.put("x", flavor + "Java" + flavor + "Scala");
        assertEquals(map.apply("x"), ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }
}
