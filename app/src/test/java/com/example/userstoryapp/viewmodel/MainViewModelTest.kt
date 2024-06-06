package com.example.userstoryapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.userstoryapp.DataDummy
import com.example.userstoryapp.MainDispatcherRule
import com.example.userstoryapp.adapter.AdapterStory
import com.example.userstoryapp.data.database.StoryEntity
import com.example.userstoryapp.data.database.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = mainDispatcherRule.runTest {
        val dummyStories = DataDummy.generateDummyStories()
        val data: PagingData<StoryEntity> = StoryPagingSource.snapshot(dummyStories)

        Mockito.`when`(storyRepository.getStories()).thenReturn(flowOf(data))

        val mainViewModel = MainViewModel(storyRepository)
        val actualStories = mainViewModel.getStories()

        val differ = AsyncPagingDataDiffer(
            diffCallback = AdapterStory.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories.first())

        awaitDifferLoad(differ)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = mainDispatcherRule.runTest {
        val data: PagingData<StoryEntity> = PagingData.from(emptyList())

        Mockito.`when`(storyRepository.getStories()).thenReturn(flowOf(data))

        val mainViewModel = MainViewModel(storyRepository)
        val actualStories = mainViewModel.getStories()

        val differ = AsyncPagingDataDiffer(
            diffCallback = AdapterStory.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories.first())

        awaitDifferLoad(differ)

        assertEquals(0, differ.snapshot().size)
    }

    private suspend fun awaitDifferLoad(differ: AsyncPagingDataDiffer<StoryEntity>) {
        withTimeoutOrNull(5000) {
            while (differ.snapshot().isEmpty()) {
                delay(100)
            }
        }
    }
}

class StoryPagingSource : PagingSource<Int, StoryEntity>() {
    companion object {
        fun snapshot(items: List<StoryEntity>): PagingData<StoryEntity> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryEntity>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryEntity> {
        return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}
