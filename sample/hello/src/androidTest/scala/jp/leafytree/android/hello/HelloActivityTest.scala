package jp.leafytree.android.hello

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity](classOf[HelloActivity]) {
  def test1() {
    Assert.assertTrue(true)
  }

  def test2() {
    Assert.assertEquals("Hello. I'm Java !", getActivity.findViewById(R.id.scala_text_view).asInstanceOf[TextView].getText)
  }
}
