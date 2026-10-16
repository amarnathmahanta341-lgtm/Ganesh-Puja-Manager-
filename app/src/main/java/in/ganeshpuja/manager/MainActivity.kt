package in.ganeshpuja.manager
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

        val subtitle = TextView(this)
        subtitle.text = "ଚାନ୍ଦା • ଖର୍ଚ୍ଚ • ରିପୋର୍ଟ"
        subtitle.textSize = 18f
        subtitle.setTextColor(Color.BLACK)
        subtitle.gravity = Gravity.CENTER
        layout.addView(subtitle)

        setContentView(layout)
    }
}
