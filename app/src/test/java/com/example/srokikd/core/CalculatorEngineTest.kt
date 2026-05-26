package com.example.srokikd.core

import com.example.srokikd.*
import org.junit.Assert.*
import org.junit.Test

class CalculatorEngineTest {
    private val useCase = CalculateEstimateUseCase()
    private val cfg = Defaults.config()

    @Test fun baseScenario(){
        val i = EstimateInput(ProductProfile.MECH, Scale.MEDIUM, "Новая разработка по ТЗ", "Рабочий полный", "Полные исходные данные", "Внутренний выпуск",1,6.0,1.15,0.2)
        val r = useCase.calculate(i,cfg) as CalcOutcome.Success
        assertEquals(60.0,r.result.laborHours,0.0); assertEquals(12,r.result.durationDays); assertEquals(10,r.result.minDays); assertEquals(15,r.result.maxDays)
    }
    @Test fun weldScenario(){
        val i = EstimateInput(ProductProfile.WELD, Scale.MEDIUM, "Новая разработка по ТЗ", "Рабочий полный", "Частично полные исходные данные", "ОТК и/или КБ заказчика",1,6.0,1.15,0.2)
        val r = useCase.calculate(i,cfg) as CalcOutcome.Success
        assertEquals(67.3,r.result.laborHours,0.0); assertEquals(13,r.result.durationDays); assertEquals(11,r.result.minDays); assertEquals(16,r.result.maxDays)
    }
    @Test fun zeroEngineersValidation(){
        val i = EstimateInput(ProductProfile.MECH, Scale.MEDIUM, "Новая разработка по ТЗ", "Рабочий полный", "Полные исходные данные", "Внутренний выпуск",0,6.0,1.15,0.2)
        assertTrue(useCase.calculate(i,cfg) is CalcOutcome.ValidationError)
    }
    @Test fun defaultsRestore(){ assertEquals(Defaults.config(), Defaults.config()) }
}
