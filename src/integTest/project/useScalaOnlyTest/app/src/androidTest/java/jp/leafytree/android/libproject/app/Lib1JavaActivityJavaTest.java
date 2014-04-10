package jp.leafytree.android.libproject.app;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import jp.leafytree.android.libproject.lib1.Lib1JavaActivity;

public class Lib1JavaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1JavaActivity> {
    public Solo solo;

    public Lib1JavaActivityJavaTest() {
        super(Lib1JavaActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        solo.waitForText("Lib1Java");
    }
}
