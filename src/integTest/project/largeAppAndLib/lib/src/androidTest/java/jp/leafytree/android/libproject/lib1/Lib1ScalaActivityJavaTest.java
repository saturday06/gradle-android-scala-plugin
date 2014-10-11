package jp.leafytree.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1ScalaActivity> {
    public Lib1ScalaActivityJavaTest() {
        super(Lib1ScalaActivity.class);
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        assertEquals("Lib1Java", ((TextView) getActivity().findViewById(R.id.scala_text_view)).getText());
    }
}
