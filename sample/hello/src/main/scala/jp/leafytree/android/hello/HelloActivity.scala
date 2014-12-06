package jp.leafytree.android.hello

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.common.collect.ImmutableSet
import org.apache.commons.math3.analysis.function.Abs
import scalaz.Scalaz._

class HelloActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_hello)
    val scalaTextView = findViewById(R.id.scala_text_view).asInstanceOf[TextView]
    scalaTextView.setText(new HelloJava().say())

    val values = for {
      str <- List("1", "2", "3", "string", "5")
      int <- str.parseInt.toOption
    } yield (new Abs()).value(int)
    Log.d("debug", "" + ImmutableSet.of(values))
  }
}
