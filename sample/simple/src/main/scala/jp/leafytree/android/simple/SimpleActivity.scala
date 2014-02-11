package jp.leafytree.android.simple

import android.app.Activity
import android.widget.TextView
import android.os.Bundle

class SimpleActivity extends Activity {
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_simple)
  }
}
