
package in.ganeshpuja.manager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(24, 32, 24, 32)
            setBackgroundColor(Color.rgb(248, 246, 238))
        }

        val title = TextView(this).apply {
            text = "🕉️ ଗଣେଶ ପୂଜା ମ୍ୟାନେଜର"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(23, 107, 69))
        }
        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "ଆମ ପୂଜା • ଆମ ହିସାବ"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 24)
        }
        layout.addView(subtitle)

        val menus = listOf(
            "ଦାନ ଜମା",
            "ଖର୍ଚ୍ଚ ହିସାବ",
            "ସଦସ୍ୟ ତାଲିକା",
            "ଆୟ-ବ୍ୟୟ ରିପୋର୍ଟ",
            "ଆଡମିନ୍ ଲଗଇନ୍"
        )

        menus.forEach { menu ->
            val button = Button(this).apply {
                text = menu
                isAllCaps = false
                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "$menu — ପରବର୍ତ୍ତୀ ସେଟ୍‌ଅପ୍‌ରେ ଯୋଡ଼ାଯିବ",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            layout.addView(
                button,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 8
                }
            )
        }

        setContentView(layout)
    }
}
