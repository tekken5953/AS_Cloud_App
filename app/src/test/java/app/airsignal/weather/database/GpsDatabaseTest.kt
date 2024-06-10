package app.airsignal.weather.database

import android.content.Context
import androidx.room.Room
import androidx.test.filters.SmallTest
import app.airsignal.weather.db.room.database.GpsDataBase
import app.airsignal.weather.db.room.model.GpsEntity
import app.airsignal.weather.db.room.scheme.GpsScheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SmallTest
@RunWith(MockitoJUnitRunner::class)
internal class GpsDatabaseTest {
    private lateinit var db: GpsDataBase
    private lateinit var gpsDao: GpsScheme

    private val testGpsEntity = GpsEntity(
        "test name",
        1.0,
        2.0,
        "english address",
        "korean address")

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun createDB() {
        MockitoAnnotations.openMocks(this)
        db = Room.inMemoryDatabaseBuilder(mockContext, GpsDataBase::class.java).build()

        gpsDao = db.gpsRepository()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndGetGps() {
        CoroutineScope(Dispatchers.IO).launch {
            gpsDao.insertGPSWithCoroutine(testGpsEntity)

            val retrievedUser = gpsDao.findByNameWithCoroutine(testGpsEntity.name)

            val expectedName = "test name"

            assertEquals(expectedName, retrievedUser.name)
        }
    }

    @Test
    fun testGetAndUpdateGps() {
        CoroutineScope(Dispatchers.IO).launch {
            val previousName = "test name"
            val replacedName = "replaced name"

            val getName = gpsDao.findByNameWithCoroutine(previousName)

            assertEquals(previousName, getName.name)

            testGpsEntity.name = replacedName

            gpsDao.updateCurrentGPS(testGpsEntity)



            assertEquals(replacedName, testGpsEntity.name)
        }
    }

    @Test
    fun testGetAndDeleteGps() {
        CoroutineScope(Dispatchers.IO).launch {
            val previousName = "test name"
            val deletedKrName = "korean address"

            val getDao = gpsDao.findByNameWithCoroutine(previousName)

            assertEquals(previousName, getDao.name)

            gpsDao.deleteFromAddrWithCoroutine(deletedKrName)

            val deletedDao = gpsDao.findByNameWithCoroutine(previousName)

            assertNotEquals(previousName, deletedDao.name)
        }
    }
}