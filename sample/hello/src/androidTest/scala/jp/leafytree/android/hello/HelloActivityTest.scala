package jp.leafytree.android.hello

import junit.framework.Assert
import android.test.ActivityInstrumentationTestCase2
import com.google.android.apps.common.testing.ui.espresso.Espresso.onView
import com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed
import com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity](classOf[HelloActivity]) {
  override def setUp() {
    getActivity()
  }

  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    onView(withText("Hello. I'm Java !")).check(matches(isDisplayed))
  }
}
