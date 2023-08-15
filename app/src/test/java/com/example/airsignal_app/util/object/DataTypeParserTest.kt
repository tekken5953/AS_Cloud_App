package com.example.airsignal_app.util.`object`

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.*
import kotlin.properties.Delegates

/**
 * @author : Lee Jae Young
 * @since : 2023-07-10 오후 4:53
 */
internal class DataTypeParserTest {
    private var getParser by Delegates.notNull<Int>()

    @Before
    fun setUpLunar() {
        getParser = DataTypeParser.getLunarDate()
    }

    @Test
    fun getLunarDate() {
        val calendar = mock(Calendar::class.java)
        `when`(calendar.get(Calendar.DAY_OF_MONTH)).thenReturn(30)

        verify(calendar).get(Calendar.DAY_OF_MONTH)
    }
}