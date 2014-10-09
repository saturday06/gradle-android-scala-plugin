package jp.leafytree.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;
import android.view.ViewGroup;
import android.widget.TextView;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1JavaActivity> {
    public Lib1ScalaActivityJavaTest() {
        super(Lib1JavaActivity.class);
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        assertEquals("Lib1Java", ((TextView)((ViewGroup) getActivity().findViewById(android.R.id.content)).getChildAt(0)).getText());
    }
}
