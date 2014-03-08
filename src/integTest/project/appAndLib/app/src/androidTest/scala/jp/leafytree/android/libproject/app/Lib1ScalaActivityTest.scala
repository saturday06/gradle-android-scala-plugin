package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import com.robotium.solo.Solo
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.Lib1ScalaActivity

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  var solo: Solo = _

  override def setUp() {
    solo = new Solo(getInstrumentation(), getActivity())
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertTrue(solo.searchText("Lib1Java", true))
  }
}
