package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var repo: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun createRepository() {
        stopKoin()

        repo = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            repo
        )

    }

    @Test
    fun ifExistISRemindersShown() = runBlockingTest {
        repo.saveReminder(
            ReminderDTO(
                "Title",
                "Description",
                "Location",
                2.89893,
                1.98893
            )
        )

        remindersListViewModel.loadReminders()
        Truth.assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty()).isFalse()
        Truth.assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
        Truth.assertThat(remindersListViewModel.showNoData.getOrAwaitValue()).isFalse()
    }

    @Test
    fun isLoadingShown() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        Truth.assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isTrue()
    }

    @Test
    fun isLoadingNotShown() = runBlockingTest {
        remindersListViewModel.loadReminders()
        Truth.assertThat(remindersListViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun remindersUnavailable_showsError() = runBlockingTest {
        repo.shouldReturnError(true)
        remindersListViewModel.loadReminders()

        Truth.assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue()).isNotEmpty()
    }
}