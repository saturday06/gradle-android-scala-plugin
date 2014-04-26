package jp.leafytree.android.libproject.app

import android.test.ActivityInstrumentationTestCase2
import junit.framework.Assert
import jp.leafytree.android.libproject.lib1.Lib1ScalaActivity
import com.google.android.apps.common.testing.ui.espresso.Espresso._
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers._
import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions._

class Lib1ScalaActivityTest extends ActivityInstrumentationTestCase2[Lib1ScalaActivity](classOf[Lib1ScalaActivity]) {
  override def setUp() {
    getActivity()
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    onView(withText("Lib1Java")).check(matches(isDisplayed))
  }
}
