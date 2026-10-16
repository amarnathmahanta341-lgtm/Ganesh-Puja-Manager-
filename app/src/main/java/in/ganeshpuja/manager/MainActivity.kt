
package `in`.ganeshpuja.manager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.WHITE)
        layout.setPadding(24, 24, 24, 24)

        val title = TextView(this)
        title.text = "ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର"
        title.textSize = 28f
        title.setTextColor(Color.rgb(190, 75, 20))
        title.gravity = Gravity.CENTER
        layout.addView(title)

        val message = TextView(this)
        message.text = "ଆପ୍ ସଫଳତାର ସହ ଚାଲୁଛି!"
        message.textSize = 20f
        message.setTextColor(Color.BLACK)
        message.gravity = Gravity.CENTER
        message.setPadding(0, 30, 0, 0)
        layout.addView(message)

        setContentView(layout)
    }
}
