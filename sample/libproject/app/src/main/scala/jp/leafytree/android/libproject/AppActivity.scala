package jp.leafytree.android.libproject

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.common.collect.ImmutableSet
import scalaz.Scalaz._
import jp.leafytree.android.libproject.lib1.Lib1Java
import org.apache.commons.math3.analysis.function.Abs

class AppActivity extends Activity {
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_app)
    val messageTextView = findViewById(R.id.message_text_view).asInstanceOf[TextView]
    messageTextView.setText(new Lib1Java().getName())

    val values = for {
      str <- List("1", "2", "-3", "string", "5")
      int <- str.parseInt.toOption
    } yield (new Abs()).value(int)
    Log.d("debug", "" + ImmutableSet.of(values))
  }
}
