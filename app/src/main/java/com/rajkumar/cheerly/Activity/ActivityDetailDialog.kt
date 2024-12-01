import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import coil.load
import com.google.android.material.button.MaterialButton
import com.rajkumar.cheerly.Activity.Models.NearbyActivity
import com.rajkumar.cheerly.R

class ActivityDetailDialog : DialogFragment() {
    private lateinit var activity: NearbyActivity

    companion object {
        fun newInstance(activity: NearbyActivity): ActivityDetailDialog {
            return ActivityDetailDialog().apply {
                arguments = Bundle().apply {
                    putParcelable("activity", activity)
                }
            }
        }

        // Static function to handle navigation
        fun openInMaps(context: android.content.Context, address: String) {
            try {
                // Try to open in Google Maps app
                val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                } else {
                    // Fallback to browser
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                    )
                    context.startActivity(browserIntent)
                }
            } catch (e: Exception) {
                // Final fallback
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
                )
                context.startActivity(browserIntent)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_activity_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity = arguments?.getParcelable("activity")!!

        // Initialize views
        view.apply {
            // Load activity image
            findViewById<ImageView>(R.id.activityImage).apply {
                if (activity.imageUrl.isNullOrEmpty()) {
                    // If no image URL, load the default outdoor activity image
                    setImageResource(R.drawable.activity_outdoor_image)
                } else {
                    // If image URL exists, load it with Coil
                    load(activity.imageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.placeholder_image)
                        error(R.drawable.activity_outdoor_image) // Also use outdoor image as error fallback
                    }
                }
            }

            // Set activity details
            findViewById<TextView>(R.id.activityTitle).text = activity.name
            findViewById<TextView>(R.id.activityCategory).text = activity.category
            findViewById<TextView>(R.id.activityAddress).text = activity.address
            findViewById<TextView>(R.id.activityDistance).text =
                String.format("%.1f km away", activity.distance)

            // Set weather info if available
            activity.weather?.let { weather ->
                findViewById<TextView>(R.id.weatherInfo).apply {
                    visibility = View.VISIBLE
                    text = String.format(
                        "%.1f°C, %s\n%s",
                        weather.temperature,
                        weather.description.capitalize(),
                        if (weather.isGoodForActivity) "Good weather for visiting"
                        else "Check weather before going"
                    )
                }
            }

            // Set contextual tips
            if (activity.contextualTips.isNotEmpty()) {
                findViewById<TextView>(R.id.tipsSection).apply {
                    visibility = View.VISIBLE
                    text = activity.contextualTips.joinToString("\n• ", "Tips:\n• ")
                }
            }

            // Rating if available
            activity.rating?.let { rating ->
                findViewById<TextView>(R.id.ratingText).apply {
                    visibility = View.VISIBLE
                    text = String.format("Rating: %.1f", rating)
                }
            }

            // Setup navigation button
            findViewById<MaterialButton>(R.id.btnNavigate).setOnClickListener {
                context?.let { ctx ->
                    openInMaps(ctx, activity.address)
                }
                dismiss()
            }

            // Setup close button
            findViewById<ImageView>(R.id.btnClose).setOnClickListener {
                dismiss()
            }
        }
    }
}