package com.rpc.weatherapp.core.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rpc.weatherapp.core.local.dao.WeatherDataDao

@Database(entities = [WeatherDataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDataDao(): WeatherDataDao
}