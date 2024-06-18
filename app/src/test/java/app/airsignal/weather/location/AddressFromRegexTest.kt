package app.airsignal.weather.location

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertEquals

internal class AddressFromRegexTest {
    @Mock
    private lateinit var sb: StringBuilder
    @Mock
    private lateinit var sbRoad: StringBuilder
    @Mock
    private val testAddress = "대한민국 서울특별시 종로구 가나다동 1245"
    @Mock
    private val testRoadAddress = "대한민국 서울특별시 종로구 산책로 30번길 16"

    private lateinit var regexClass: AddressFromRegex
    private lateinit var regexRoadClass: AddressFromRegex

    @Before
    fun setUp() {
        sb = StringBuilder(testAddress)
        sbRoad = StringBuilder(testRoadAddress)
        regexClass = AddressFromRegex(testAddress)
        regexRoadClass = AddressFromRegex(testRoadAddress)
    }

    @Test
    fun testGetAddress() {
        val resultAddress = regexClass.getAddress()
        val resultRoadAddress = regexRoadClass.getAddress()
        val expectedAddress = "서울특별시 종로구 가나다동"
        val expectedRoadAddress = "서울특별시 종로구 산책로 30번길"

        assertEquals(expectedAddress, resultAddress)
        assertEquals(expectedRoadAddress, resultRoadAddress)
    }

    @Test
    fun testGetWarningAddress() {
        val resultWarningAddress = regexClass.getWarningAddress()
        val resultRoadWarningAddress = regexRoadClass.getWarningAddress()
        val expectedAddress = "서울특별시"
        val expectedRoadAddress = "서울특별시"

        assertEquals(expectedAddress, resultWarningAddress)
        assertEquals(expectedRoadAddress, resultRoadWarningAddress)
    }

    @Test
    fun testGetNotificationAddress() {
        val resulNotiAddress = regexClass.getNotificationAddress()
        val resultRoadNotiAddress = regexRoadClass.getNotificationAddress()
        val expectedAddress = "가나다동"
        val expectedRoadAddress = "산책로 30번길"

        assertEquals(expectedAddress, resulNotiAddress)
        assertEquals(expectedRoadAddress, resultRoadNotiAddress)
    }

    @Test
    fun testGetSecondAddress() {
        val resultSecondAddress = regexClass.getSecondAddress()
        val resultRoadSecondAddress = regexRoadClass.getSecondAddress()
        val expectedAddress = "서울특별시 종로구"
        val expectedRoadAddress = "서울특별시 종로구"

        assertEquals(expectedAddress, resultSecondAddress)
        assertEquals(expectedRoadAddress, resultRoadSecondAddress)
    }

    @Test
    fun testIsRoadAddress() {
        val isRoadAddress = regexRoadClass.isRoadAddress()
        val isNotRoadAddress = regexClass.isRoadAddress()
        assertEquals(true, isRoadAddress)
        assertEquals(false, isNotRoadAddress)
    }
}