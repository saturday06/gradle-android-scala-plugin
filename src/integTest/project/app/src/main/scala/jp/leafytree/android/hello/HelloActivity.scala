package jp.leafytree.android.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class HelloActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_hello)
    val scalaTextView = findViewById(R.id.scala_text_view).asInstanceOf[TextView]
    scalaTextView.setText(new HelloJava().say + "\n" + new HelloScala().say)
  }
}
