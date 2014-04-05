package jp.leafytree.android.libproject.app;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import jp.leafytree.android.libproject.lib1.Lib1ScalaActivity;

public class Lib1ScalaActivityJavaTest extends ActivityInstrumentationTestCase2<Lib1ScalaActivity> {
    public Solo solo;

    public Lib1ScalaActivityJavaTest() {
        super(Lib1ScalaActivity.class);
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
