package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

val reminder = ReminderDataItem(
    title = "Title",
    description = "Description",
    location = "Location",
    latitude = 12.23646,
    longitude = 18.126699
)

val nullReminder = ReminderDataItem(
    title = null,
    description = null,
    location =  null,
    latitude =  null,
    longitude =  null
)

val locationMissingreminder = ReminderDataItem(
    title = "Title",
    description = "Description",
    location = null,
    latitude = 12.23646,
    longitude = 18.126699
)



