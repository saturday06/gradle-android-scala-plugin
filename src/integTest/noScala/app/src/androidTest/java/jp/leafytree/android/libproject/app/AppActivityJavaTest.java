package jp.leafytree.android.libproject.app;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;
import jp.leafytree.android.libproject.AppActivity;
import jp.leafytree.android.libproject.R;

public class AppActivityJavaTest extends ActivityInstrumentationTestCase2<AppActivity> {
    public AppActivityJavaTest() {
        super(AppActivity.class);
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        assertEquals("Lib1Java", ((TextView) getActivity().findViewById(R.id.message_text_view)).getText());
    }
}
