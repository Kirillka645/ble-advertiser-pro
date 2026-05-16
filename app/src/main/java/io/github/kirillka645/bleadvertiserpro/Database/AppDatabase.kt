package io.github.kirillka645.bleadvertiserpro.Database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.ContinuityActionModalAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.ContinuityIos17CrashAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.ContinuityNewAirtagPopUpAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.ContinuityNewDevicePopUpAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.ContinuityNotYourDevicePopUpAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.EasySetupBudsAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.EasySetupWatchAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.FastPairDevicesAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.FastPairDebugAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.FastPairNonProductionAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.FastPairPhoneSetupAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.LovespousePlayAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.LovespouseStopAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AdvertisementSetGenerators.SwiftPairAdvertisementSetGenerator
import io.github.kirillka645.bleadvertiserpro.AppContext.AppContext
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertiseDataDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertiseDataManufacturerSpecificDataDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertiseDataServiceDataDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertiseSettingsDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertisementSetCollectionDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertisementSetDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertisementSetListDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AdvertisingSetParametersDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AssociationCollectionListDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.AssociationListSetDao
import io.github.kirillka645.bleadvertiserpro.Database.Dao.PeriodicAdvertisingParametersDao
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseDataEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseDataManufacturerSpecificDataEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseDataServiceDataEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertiseSettingsEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetCollectionEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisementSetListEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AdvertisingSetParametersEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AssociatonCollectionListEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.AssociationListSetEntity
import io.github.kirillka645.bleadvertiserpro.Database.Entities.PeriodicAdvertisingParametersEntity
import io.github.kirillka645.bleadvertiserpro.Database.Migrations.Migration_1_2
import io.github.kirillka645.bleadvertiserpro.Helpers.DatabaseHelpers

@androidx.room.Database(
    entities = [AdvertiseDataEntity::class,
                AdvertiseDataManufacturerSpecificDataEntity::class,
                AdvertiseDataServiceDataEntity::class,
                AdvertisementSetCollectionEntity::class,
                AdvertisementSetEntity::class,
                AdvertisementSetListEntity::class,
                AdvertiseSettingsEntity::class,
                AdvertisingSetParametersEntity::class,
                AssociatonCollectionListEntity::class,
                AssociationListSetEntity::class,
                PeriodicAdvertisingParametersEntity::class],
    version = 2,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    var isSeeding = false
    abstract fun advertiseDataDao(): AdvertiseDataDao
    abstract fun advertiseDataManufacturerSpecificDataDao(): AdvertiseDataManufacturerSpecificDataDao
    abstract fun advertiseDataServiceDataDao(): AdvertiseDataServiceDataDao

    abstract fun advertisementSetCollectionDao(): AdvertisementSetCollectionDao

    abstract fun advertisementSetDao(): AdvertisementSetDao

    abstract fun advertisementSetListDao(): AdvertisementSetListDao

    abstract fun advertiseSettingsDao(): AdvertiseSettingsDao

    abstract fun advertisingSetParametersDao(): AdvertisingSetParametersDao

    abstract fun associationCollectionListDao(): AssociationCollectionListDao

    abstract fun associationListSetDao(): AssociationListSetDao

    abstract fun periodicAdvertisingParametersDao(): PeriodicAdvertisingParametersDao


    companion object {
        private const val _logTag = "AppDatabase"
        private var INSTANCE: AppDatabase? = null

        fun getInstance(): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(AppContext.getContext()).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "BluetoothLeSpamDatabase.db")
                .addCallback(seedDatabaseCallback(context))
                .addMigrations(Migration_1_2)
                //.fallbackToDestructiveMigration()
                .build()

        private fun seedDatabaseCallback(context: Context): Callback {
            return object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    Thread {
                        synchronized(this) {
                            seedingThread.run()
                        }
                    }.start()
                }
            }
        }

        val seedingThread = Runnable {
            Log.d(_logTag, "Starting Database Seeding")
            getInstance().isSeeding = true

            val advertisementSetGenerators = listOf(
                FastPairDevicesAdvertisementSetGenerator(),
                FastPairPhoneSetupAdvertisementSetGenerator(),
                FastPairNonProductionAdvertisementSetGenerator(),
                FastPairDebugAdvertisementSetGenerator(),

                //ContinuityDevicePopUpAdvertisementSetGenerator(),
                ContinuityNotYourDevicePopUpAdvertisementSetGenerator(),
                ContinuityNewDevicePopUpAdvertisementSetGenerator(),
                ContinuityNewAirtagPopUpAdvertisementSetGenerator(),
                ContinuityActionModalAdvertisementSetGenerator(),
                ContinuityIos17CrashAdvertisementSetGenerator(),

                SwiftPairAdvertisementSetGenerator(),

                EasySetupWatchAdvertisementSetGenerator(),
                EasySetupBudsAdvertisementSetGenerator(),

                LovespousePlayAdvertisementSetGenerator(),
                LovespouseStopAdvertisementSetGenerator()
            )

            advertisementSetGenerators.forEach{ generator ->
                val advertisementSets = generator.getAdvertisementSets(null)
                advertisementSets.forEach{ advertisementSet ->
                    DatabaseHelpers.saveAdvertisementSet(advertisementSet)
                }
            }

            getInstance().isSeeding = false
            Log.d(_logTag, "Database Seeding finished")
        }
    }
}