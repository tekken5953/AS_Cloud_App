package app.airsignal.weather.utils.plain

import org.junit.Test
import org.mockito.Mock
import kotlin.test.assertEquals

internal class SensibleTempFormulaTest {

    @Mock
    private val testTemp: Double = 26.0
    @Mock
    private val testRh: Double = 55.0
    @Mock
    private val testV: Double = 1.5
    @Mock
    private val testMonth: Int = 6

    @Test
    fun getSensibleTemp() {
        // Given
        val sensibleTempClass = SensibleTempFormula().getSensibleTemp(testTemp,testRh,testV,testMonth)
        val expectedResult = 26.046853158182305

        assertEquals(expectedResult, sensibleTempClass)
    }
}