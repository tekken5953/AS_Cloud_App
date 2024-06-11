package app.airsignal.weather.utils.plain

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import app.airsignal.weather.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
internal class LunarShapeTest {

    @Mock
    private lateinit var mockContext: Context

    // Given
    private val lunarShape = LunarShape(1.5F)

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        `when`(mockContext.resources).thenReturn(mock(Resources::class.java))

        `when`(mockContext.getString(R.string.lunar_sak)).thenReturn("New Moon")
        `when`(mockContext.getString(R.string.lunar_cho)).thenReturn("Waxing Crescent")
        `when`(mockContext.getString(R.string.lunar_sang_d)).thenReturn("First Quarter")
        `when`(mockContext.getString(R.string.lunar_sang_m)).thenReturn("Waxing Gibbous")
        `when`(mockContext.getString(R.string.lunar_bo)).thenReturn("Full Moon")
        `when`(mockContext.getString(R.string.lunar_ha_m)).thenReturn("Waning Gibbous")
        `when`(mockContext.getString(R.string.lunar_ha_d)).thenReturn("Last Quarter")
        `when`(mockContext.getString(R.string.lunar_g)).thenReturn("Waning Crescent")
        `when`(mockContext.getString(R.string.error)).thenReturn("Error")
    }

    @Test
    fun testShapeText() {
        assertEquals("New Moon", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 5.0F
        assertEquals("Waxing Crescent", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 7.0F
        assertEquals("First Quarter", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 10.0F
        assertEquals("Waxing Gibbous", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 14.0F
        assertEquals("Full Moon", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 18.0F
        assertEquals("Waning Gibbous", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 21.0F
        assertEquals("Last Quarter", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 25.0F
        assertEquals("Waning Crescent", lunarShape.shapeText(mockContext))

        lunarShape.moonAge = 30.0F
        assertEquals("Error", lunarShape.shapeText(mockContext))
    }

    @Test
    fun shapeDrawable() {
        //When
        val expectedDrawable: Drawable? = mockContext.getDrawable(R.drawable.moon_sak)
//
        // When
        val drawable = lunarShape.shapeDrawable(mockContext)
//
        // Then
        assertEquals(expectedDrawable, drawable)

    }

    @Test
    fun progress() {
        assertEquals(0, lunarShape.progress())

        lunarShape.moonAge = 8.0F
        assertEquals(46, lunarShape.progress())

        lunarShape.moonAge = 15.0F
        assertEquals(96, lunarShape.progress())

        lunarShape.moonAge = 16.0F
        assertEquals(96, lunarShape.progress())

        lunarShape.moonAge = 25.0F
        assertEquals(32, lunarShape.progress())
    }
}