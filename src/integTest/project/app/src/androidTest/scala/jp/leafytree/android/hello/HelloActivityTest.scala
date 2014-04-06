package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity]("jp.leafytree.android.hello", classOf[HelloActivity]) {
  var solo: Solo = _

  override def setUp() {
    solo = new Solo(getInstrumentation(), getActivity())
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    solo.waitForText("Hello. I'm Java !")
  }
}
