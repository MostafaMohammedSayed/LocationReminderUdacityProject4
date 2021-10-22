package com.udacity.project4.locationreminders.savereminder


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.*
import com.udacity.project4.locationreminders.reminderslist.MainCoroutineRule
import com.udacity.project4.locationreminders.reminderslist.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {


    //TODO: provide testing to the SaveReminderView and its live data objects
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var repo: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun createRepository() {
        stopKoin()

        repo = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            repo
        )

    }
    @Test
    fun viewModel_checkShowLoadingBehavior() {
        mainCoroutineRule.pauseDispatcher()


        saveReminderViewModel.validateAndSaveReminder(reminder)
        Truth.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isTrue()

        mainCoroutineRule.resumeDispatcher()
        Truth.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue()).isFalse()
    }

    @Test
    fun viewModel_checkSuccess() {
        saveReminderViewModel.validateAndSaveReminder(reminder)
        Truth.assertThat(saveReminderViewModel.showToast.getOrAwaitValue()).isEqualTo("Reminder Saved !")
    }

    @Test
    fun viewModel_nullReminder()
        {
            saveReminderViewModel.validateAndSaveReminder(nullReminder)

            Truth.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(
                R.string.err_enter_title
            )
        }



    @Test
    fun viewModel_savingDlocationMissingreminder() {
        saveReminderViewModel.validateAndSaveReminder(locationMissingreminder)
        Truth.assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

}