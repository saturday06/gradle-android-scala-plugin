package jp.leafytree.android.hello;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

public class HelloActivityJavaTest extends ActivityInstrumentationTestCase2<HelloActivity> {
    public Solo solo;

    public HelloActivityJavaTest() {
        super(HelloActivity.class);
    }

    @Override
    public void setUp() {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void test1() {
        assertTrue(true);
    }

    public void test2() {
        assertTrue(solo.searchText("Hello. I'm Java !", true));
    }
}
