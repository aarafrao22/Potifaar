package com.aarafrao.potifaar


import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aarafrao.potifaar.databinding.ActivityFirstBinding
import java.util.concurrent.TimeUnit

class FirstActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityFirstBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFirstBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(24, TimeUnit.HOURS) // Change this delay as needed
            .build()


        WorkManager.getInstance(this).enqueue(workRequest)

        val linearLayout: LinearLayout = findViewById(R.id.linearLayout)
        val linearLayout1: LinearLayout = findViewById(R.id.linearLayout2)

        linearLayout.setOnClickListener(this)
        linearLayout1.setOnClickListener(this)

        binding.ah.setOnClickListener(this)
        binding.jkp.setOnClickListener(this)
        binding.nml.setOnClickListener(this)

    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.linearLayout -> {
                val intent = Intent(this, PdfActivity::class.java)
                startActivity(intent)
            }


            R.id.linearLayout2 -> {
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences("myPref", MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()

                val intent = Intent(this, PdfActivity::class.java)
                startActivity(intent)

            }

            R.id.jkp -> {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/dev?id=8199057463740975197")
                    )
                )

            }

            R.id.ah -> {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/dev?id=8199057463740975197")
                    )
                )

            }

            R.id.nml -> {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/dev?id=8199057463740975197")
                    )
                )

            }
        }
    }
}