package jp.leafytree.android.libproject.app;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import jp.leafytree.android.libproject.AppActivity;

public class AppActivityJavaTest extends ActivityInstrumentationTestCase2<AppActivity> {
    public Solo solo;

    public AppActivityJavaTest() {
        super(AppActivity.class);
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
