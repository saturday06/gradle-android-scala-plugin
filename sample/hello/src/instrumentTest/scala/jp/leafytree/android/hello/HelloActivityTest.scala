package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import junit.framework.Assert

class HelloActivityTest(activityClass: Class[HelloActivity]) extends ActivityInstrumentationTestCase2[HelloActivity](activityClass) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertTrue(true)
  }
}
