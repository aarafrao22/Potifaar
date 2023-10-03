package com.aarafrao.potifaar


import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.aarafrao.potifaar.databinding.ActivityPdfBinding
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

@Suppress("DEPRECATION")
class PdfActivity : AppCompatActivity(), View.OnClickListener, OnPageChangeListener {

    private var pageCountG: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var mAdView: AdView
    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "PdfActivity"
    private lateinit var editor: SharedPreferences.Editor
    private var nightMode: Boolean = false
    private lateinit var pageArray: List<String>
    private lateinit var nightBtn: ImageView
    private lateinit var page: ImageView
    private lateinit var btnBack: ImageView
    lateinit var binding: ActivityPdfBinding
    private lateinit var mainBg: ConstraintLayout
    private var isTrue: Boolean = false
    private lateinit var adapter: ArrayAdapter<String>
    lateinit var pdfView: PDFView

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val whiteColor = ContextCompat.getColor(this, R.color.white)

        // Create a ColorStateList using the color
        val colorStateList = ColorStateList.valueOf(whiteColor)
        binding.txtPageNumber.backgroundTintList = colorStateList

        nightBtn = findViewById(R.id.nightMode)
        page = findViewById(R.id.page)
        btnBack = findViewById(R.id.btnBack)
        pdfView = findViewById(R.id.pdfView)
        mainBg = findViewById(R.id.mainBg)

        btnBack.setOnClickListener(this)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                pdfView.setNightMode(true)
                nightMode = true
                mainBg.setBackgroundColor(Color.BLACK)
                pdfView.requestLayout()
                nightBtn.setImageResource(R.drawable.ic_moonfilled)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                pdfView.setNightMode(false)
                nightMode = false
                pdfView.requestLayout()
                mainBg.setBackgroundColor(Color.WHITE)
                nightBtn.setImageResource(R.drawable.ic_moon)
            }
        }


        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val pagesNo = sharedPref.getInt("page", 0)
        nightBtn.setOnClickListener(this)
        page.setOnClickListener(this)

        MobileAds.initialize(this) {}
//        binding.txtPageNumber.text = "# $pagesNo"


        loadAd()
        pdfView.fromAsset("potifar.pdf")
            .defaultPage(pagesNo)
            .onPageChange(this)
            .swipeHorizontal(true)
            .pageSnap(true)
            .autoSpacing(true)
            .enableAnnotationRendering(false)
            .pageFling(true)
            .pageFitPolicy(FitPolicy.BOTH)
            .nightMode(nightMode)
            .load()


        binding.txtPageNumber.setOnTouchListener { _, _ ->
            isTrue = true
            false
        }
        binding.txtPageNumber.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                if (isTrue) {
                    pdfView.jumpTo(p2)
                    binding.txtPageNumber.setSelection(p2)
                    Toast.makeText(applicationContext, "$p2", Toast.LENGTH_SHORT).show()
                    editor = sharedPref.edit()
                    editor.putInt("page", p2)
                    editor.apply()
                    isTrue = false
                }


            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


    }


    @SuppressLint("VisibleForTests")
    private fun loadAd() {
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {

            R.id.nightMode ->
                if (nightMode) {
                    loadAd()
                    // Turn off PDF night mode
                    pdfView.setNightMode(false)
                    nightMode = false
                    pdfView.requestLayout()
                    mainBg.setBackgroundColor(Color.WHITE)
                    nightBtn.setImageResource(R.drawable.ic_moon)
                    Toast.makeText(this, "Day Mode Activated", Toast.LENGTH_SHORT).show()
                } else {
                    loadAd()
                    pdfView.setNightMode(true)
                    nightMode = true
                    mainBg.setBackgroundColor(Color.BLACK)
                    pdfView.requestLayout()
                    nightBtn.setImageResource(R.drawable.ic_moonfilled)
                    Toast.makeText(this, "Night Mode Activated", Toast.LENGTH_SHORT).show()
                }

            R.id.btnBack -> {
                onBackPressed()
                loadAd()

            }

        }

    }

    private fun addItemsWithPageCount(pageCount: Int): List<String> {
        val items = mutableListOf<String>()
        var currentPage = 0

        while (currentPage <= pageCount) {
            val newItem = "$currentPage"
            items.add(newItem)
            currentPage++
        }

        return items
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        pageCountG = pageCount
        pageArray = addItemsWithPageCount(pageCountG)
        adapter = ArrayAdapter(this, R.layout.spinner_item, pageArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.txtPageNumber.adapter = adapter
        binding.txtPageNumber.setSelection(page)

        editor = sharedPref.edit()
        editor.putInt("page", page)
        editor.apply()
        loadAd()

        if (page % 5 == 0) {
            //loadInterstitialAdHere
            loadInterstitial()
        }
    }

    @SuppressLint("VisibleForTests")
    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-3744228358425966/3106917242",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.toString())
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd

                    mInterstitialAd?.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdClicked() {
                                Log.d(TAG, "Ad was clicked.")
                            }

                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad dismissed fullscreen content.")
                                mInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                Log.e(TAG, "Ad failed to show fullscreen content.")
                                mInterstitialAd = null
                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                Log.d(TAG, "Ad recorded an impression.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad showed fullscreen content.")
                            }
                        }
                }
            })

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

}