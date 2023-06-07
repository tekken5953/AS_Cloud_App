//package com.example.airsignal_app
//
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.example.airsignal_app.util.SensibleTempFormula
//import org.junit.Assert.*
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import kotlin.properties.Delegates
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//
///** 체감온도 테스트 **/
//@RunWith(AndroidJUnit4::class)
//class SensibleTempUnitTest {
//    private lateinit var sensibleTemp: SensibleTempFormula
//    private var temp by Delegates.notNull<Double>()
//    private var humid by Delegates.notNull<Double>()
//    private var wind by Delegates.notNull<Double>()
//
//    @Before
//    fun setUp() {
//        sensibleTemp = SensibleTempFormula()
//        temp = 18.0
//        humid = 29.0
//        wind = 2.0
//    }
//
//    @Test
//    fun get_sens_temp_in_summer() {
//        val result = sensibleTemp.getInSummer(temp, humid)
//        println("result is $result")
//        assertEquals(16.0, result, 1.0)
//    }
//
//    @Test
//    fun get_sens_temp_in_winter() {
//        val result = sensibleTemp.getInWinter(temp, wind)
//        println("result is $result")
//        assertEquals(19.0, result, 1.0)
//    }
//
//    @Test
//    fun get_current_month() {
//        val result = sensibleTemp.getCurrentSeason()
//        println("result is $result")
//        assertSame(4, result)
//    }
//}