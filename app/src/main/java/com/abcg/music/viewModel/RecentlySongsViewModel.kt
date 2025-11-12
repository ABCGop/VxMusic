package com.abcg.music.viewModel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.maxrave.domain.repository.SongRepository
import com.abcg.music.pagination.RecentPagingSource
import com.abcg.music.viewModel.base.BaseViewModel

class RecentlySongsViewModel(
    application: Application,
    private val songRepository: SongRepository,
) : BaseViewModel(application) {
    val recentlySongs =
        Pager(
            PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20,
            ),
        ) {
            RecentPagingSource(songRepository)
        }.flow.cachedIn(viewModelScope)
}
