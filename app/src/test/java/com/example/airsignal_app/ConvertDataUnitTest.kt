package com.example.airsignal_app

import android.content.Context
import com.example.airsignal_app.util.ConvertDataType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertEquals


/**
 * @author : Lee Jae Young
 * @since : 2023-04-21 오전 10:54
 **/


/** 데이터 타입 변환 테스트 **/
@RunWith(MockitoJUnitRunner::class)
class ConvertDataUnitTest {
    lateinit var convertData: ConvertDataType

    @Mock
    lateinit var context: Context

    private val dayOfWeek: Int = 1

    @Before
    fun setUp() {
        convertData = ConvertDataType
        context = mock(Context::class.java)
    }

    @Test
    fun dayOfWeek_to_dayString() {
        val result = convertData.convertDayOfWeekToKorean(context, dayOfWeek)
        println("result is $result")
        assertEquals("월", result)
    }
}