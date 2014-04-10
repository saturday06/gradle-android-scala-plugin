package jp.leafytree.android.libproject.lib1

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert

class Lib1JavaActivityTest extends ActivityInstrumentationTestCase2[Lib1JavaActivity](classOf[Lib1JavaActivity]) {
  var solo: Solo = _

  override def setUp() {
    solo = new Solo(getInstrumentation(), getActivity())
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    solo.waitForText("Lib1Java")
  }
}
