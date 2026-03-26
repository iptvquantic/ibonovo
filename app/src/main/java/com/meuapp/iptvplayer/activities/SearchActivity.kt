package com.meuapp.iptvplayer.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.meuapp.iptvplayer.adapter.SearchResultAdapter
import com.meuapp.iptvplayer.apps.Constants
import com.meuapp.iptvplayer.databinding.ActivitySearchBinding
import com.meuapp.iptvplayer.helper.IptvRepository
import com.meuapp.iptvplayer.helper.PreferenceHelper
import com.meuapp.iptvplayer.helper.Result
import com.meuapp.iptvplayer.models.LiveChannelModel
import com.meuapp.iptvplayer.models.MovieModel
import com.meuapp.iptvplayer.models.SeriesModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchItem(
    val id: Int, val name: String, val icon: String,
    val type: String, val ext: String = "mp4", val streamUrl: String = ""
)

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var repo: IptvRepository
    private lateinit var adapter: SearchResultAdapter
    private var searchJob: Job? = null
    private var filterType = "all"

    private var allMovies = listOf<MovieModel>()
    private var allSeries = listOf<SeriesModel>()
    private var allChannels = listOf<LiveChannelModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        repo = IptvRepository(this)
        binding.ivBack.setOnClickListener { finish() }

        adapter = SearchResultAdapter { item -> openItem(item) }
        binding.rvResults.layoutManager = GridLayoutManager(this, 3)
        binding.rvResults.adapter = adapter

        // Chip filters
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            filterType = when {
                checkedIds.contains(com.meuapp.iptvplayer.R.id.chipLive) -> Constants.TYPE_LIVE
                checkedIds.contains(com.meuapp.iptvplayer.R.id.chipMovies) -> Constants.TYPE_VOD
                checkedIds.contains(com.meuapp.iptvplayer.R.id.chipSeries) -> Constants.TYPE_SERIES
                else -> "all"
            }
            triggerSearch()
        }
        (binding.root.findViewById<Chip>(com.meuapp.iptvplayer.R.id.chipAll)).isChecked = true

        // Search with debounce
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {
                binding.btnClear.visibility = if (s?.isNotEmpty() == true) View.VISIBLE else View.GONE
                searchJob?.cancel()
                searchJob = lifecycleScope.launch { delay(300); search(s.toString()) }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnClear.setOnClickListener { binding.etSearch.text?.clear() }

        preloadData()
    }

    private fun preloadData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvResultCount.text = "Carregando conteúdo..."
        lifecycleScope.launch {
            val movies = repo.getMovies()
            val series = repo.getSeries()
            val channels = repo.getLiveStreams()
            if (movies is Result.Success) allMovies = movies.data
            if (series is Result.Success) allSeries = series.data
            if (channels is Result.Success) allChannels = channels.data
            binding.progressBar.visibility = View.GONE
            val total = allMovies.size + allSeries.size + allChannels.size
            binding.tvResultCount.text = "📦 $total itens disponíveis — digite para pesquisar"
        }
    }

    private fun triggerSearch() {
        search(binding.etSearch.text.toString())
    }

    private fun search(query: String) {
        if (query.length < 2) {
            adapter.submitList(emptyList())
            binding.rvResults.visibility = View.GONE
            binding.llEmpty.visibility = View.VISIBLE
            binding.tvResultCount.text = "Digite pelo menos 2 caracteres"
            return
        }
        val q = query.trim()
        val results = mutableListOf<SearchItem>()

        if (filterType == "all" || filterType == Constants.TYPE_VOD) {
            allMovies.filter { it.name.contains(q, true) }
                .sortedByDescending { it.name.indexOf(q, ignoreCase = true) == 0 }
                .forEach { results.add(SearchItem(it.streamId, it.name, it.streamIcon, Constants.TYPE_VOD, it.containerExtension)) }
        }
        if (filterType == "all" || filterType == Constants.TYPE_SERIES) {
            allSeries.filter { it.name.contains(q, true) }
                .sortedByDescending { it.name.indexOf(q, ignoreCase = true) == 0 }
                .forEach { results.add(SearchItem(it.seriesId, it.name, it.cover, Constants.TYPE_SERIES)) }
        }
        if (filterType == "all" || filterType == Constants.TYPE_LIVE) {
            allChannels.filter { it.name.contains(q, true) }
                .sortedByDescending { it.name.indexOf(q, ignoreCase = true) == 0 }
                .forEach {
                    val url = PreferenceHelper.buildStreamUrl(this, it.streamId)
                    results.add(SearchItem(it.streamId, it.name, it.streamIcon, Constants.TYPE_LIVE, "ts", url))
                }
        }

        adapter.submitList(results)
        binding.rvResults.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE
        binding.llEmpty.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        binding.tvResultCount.text = when {
            results.isEmpty() -> "❌ Nenhum resultado para \"$q\""
            else -> "✅ ${results.size} resultados para \"$q\""
        }
    }

    private fun openItem(item: SearchItem) {
        when (item.type) {
            Constants.TYPE_VOD -> startActivity(Intent(this, MovieInfoActivity::class.java).apply {
                putExtra(Constants.EXTRA_VOD_ID, item.id)
                putExtra(Constants.EXTRA_STREAM_NAME, item.name)
                putExtra(Constants.EXTRA_STREAM_ICON, item.icon)
            })
            Constants.TYPE_SERIES -> startActivity(Intent(this, SeriesInfoActivity::class.java).apply {
                putExtra(Constants.EXTRA_SERIES_ID, item.id)
                putExtra(Constants.EXTRA_STREAM_NAME, item.name)
                putExtra(Constants.EXTRA_STREAM_ICON, item.icon)
            })
            Constants.TYPE_LIVE -> startActivity(Intent(this, LiveChannelActivity::class.java).apply {
                putExtra(Constants.EXTRA_STREAM_URL, item.streamUrl)
                putExtra(Constants.EXTRA_STREAM_NAME, item.name)
                putExtra(Constants.EXTRA_STREAM_ICON, item.icon)
                putExtra(Constants.EXTRA_STREAM_ID, item.id)
            })
        }
    }
}
