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

  def testSimpleAssertion() {
    Assert.assertTrue(true)
  }

  def testSimpleActivityAssertion() {
    onView(withText("Hello. I'm Java !")).check(matches(isDisplayed))
  }

  def testCallScalaLibraryClassOfNotUsedByMainApp() {
    val map = new TrieMap[String, String]
    map.put("x", "Hello. I'm Java !")
    solo.waitForText(map("x"))
  }
}
