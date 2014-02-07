package jp.leafytree.android.libproject.lib1

import android.os.Bundle
import android.app.Activity
import android.widget.TextView

class Lib1ScalaActivity extends Activity {
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_lib1_scala)
    val scalaTextView = findViewById(R.id.scala_text_view).asInstanceOf[TextView]
    scalaTextView.setText(new Lib1Java().getName)
  }
}
