package jp.leafytree.android.libproject.lib1;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

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
        assertTrue(solo.searchText("Lib1Java", true));
    }
}
