package com.example.homework3

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.preference.PreferenceManager
import com.example.homework3.adapter.NewsAdapter
import com.example.homework3.databinding.ActivityMainBinding
import com.example.homework3.viewmodel.NewListViewModel

private const val SYNC_PREFERENCE = "sync"


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var displayImage: Boolean = true

    companion object {
        const val LOG_TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    private val newListViewModel: NewListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newsAdapter = NewsAdapter(this)
        binding.rvNewsData.adapter = newsAdapter


        newsAdapter.onItemClickListener = { item ->
            Log.e(LOG_TAG, "ITEM CLICKED: $item")
            val intent = Intent(this, DetailViewActivity::class.java)
            intent.putExtra("RSSInfo", item)
            startActivity(intent)
        }



        binding.btnLoadNews.setOnClickListener{
            binding.btnLoadNews.isEnabled = false
            newListViewModel.loadData()
            binding.btnLoadNews.isEnabled = true
        }



        newListViewModel.rssList.observe(this) {newsList ->
            if(newsList.isNotEmpty()){
                newsAdapter.setRssList(newsList)
            }
        }

        newListViewModel.hasError.observe(this) { hasError->
            if (hasError) {
                Toast.makeText(this@MainActivity, "An error occured", Toast.LENGTH_LONG)
                    .show()
                val btnReload = "RELOAD"
                binding.btnLoadNews.text = btnReload
            }
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        displayImage = sharedPreferences.getBoolean("displayimage", true)
        newsAdapter.setDisplayImage(displayImage)

        sharedPreferences.registerOnSharedPreferenceChangeListener(this)


    }

    fun updateFeedUrl(newUrl: String){
        newListViewModel.updateFeedUrl(newUrl)
    }

    private fun updateAdapterDisplayImage(){
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        displayImage = sharedPreferences.getBoolean("displayimage", true)

        val newsAdapter = binding.rvNewsData.adapter as? NewsAdapter
        newsAdapter?.setDisplayImage(displayImage)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?){
        if(key == "feedurl") {
            val newUrl = sharedPreferences?.getString(key, "") ?: ""
            updateFeedUrl(newUrl)
        } else if(key == "displayimage"){
            val displayImage = sharedPreferences?.getBoolean(key, true) ?: true
            val newsAdapter = binding.rvNewsData.adapter as? NewsAdapter
            newsAdapter?.setDisplayImage(displayImage)
        }
    }

    override fun onResume(){
        super.onResume()
        updateAdapterDisplayImage()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause(){
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // sucht sich das main_menu.xml
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.setting_option -> {
                Intent(this, SettingsActivity::class.java)
                    .run{startActivity(this)}
                return true
            }
            R.id.reload_option -> {
                newListViewModel.loadData()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

