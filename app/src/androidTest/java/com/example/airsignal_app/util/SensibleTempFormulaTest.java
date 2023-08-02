package com.example.airsignal_app.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class SensibleTempFormulaTest {
    /**
     * @utbot.classUnderTest {@link SensibleTempFormula}
     * @utbot.methodUnderTest {@link SensibleTempFormula#getSensibleTemp(double, double, double)}
     */
    @Test
    public void testGetSensibleTempReturnsNanWithCornerCases() {
        SensibleTempFormula sensibleTempFormula = new SensibleTempFormula();

        double actual = sensibleTempFormula.getSensibleTemp(0.0, -1.0, Double.NEGATIVE_INFINITY);

        assertEquals(Double.NaN, actual, 1.0E-6);
    }
    ///endregion

    ///region FUZZER: SUCCESSFUL EXECUTIONS for method getSensibleTemp(double, double, double)

    /**
     * @utbot.classUnderTest {@link SensibleTempFormula}
     * @utbot.methodUnderTest {@link SensibleTempFormula#getSensibleTemp(double, double, double)}
     */
    @Test
    public void testGetSensibleTempReturnsNanWithCornerCases1() {
        SensibleTempFormula sensibleTempFormula = new SensibleTempFormula();

        double actual = sensibleTempFormula.getSensibleTemp(0.0, -1.0, Double.NEGATIVE_INFINITY);

        assertEquals(Double.NaN, actual, 1.0E-6);
    }
}
