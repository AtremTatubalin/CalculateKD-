package com.example.srokikd

import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.round

enum class ProductProfile(val title: String) { MECH("Механические узлы и передачи"), WELD("Сварные металлоконструкции") }
enum class Scale(val title: String) { SIMPLE("Отдельная деталь / простой элемент"), SMALL("Малый узел / кронштейн в сборе"), MEDIUM("Средняя сборочная единица / рама"), LARGE("Механизм / крупная металлоконструкция"), COMPLEX("Машина / комплексный агрегат") }

data class NormConfig(
    val baseHours: Map<ProductProfile, Map<Scale, Double>>,
    val character: Map<String, Double>,
    val pkg: Map<String, Double>,
    val inputs: Map<String, Double>,
    val approval: Map<String, Double>
)

data class EstimateInput(val profile: ProductProfile?, val scale: Scale?, val character: String?, val pkg: String?, val inputs: String?, val approval: String?, val engineers: Int, val hoursPerDay: Double, val reserve: Double, val uncertainty: Double)
data class EstimateResult(val laborHours: Double, val durationDays: Int, val minDays: Int, val maxDays: Int)

sealed class CalcOutcome { data class Success(val result: EstimateResult): CalcOutcome(); data class ValidationError(val message: String): CalcOutcome() }

object Defaults {
    fun config() = NormConfig(
        baseHours = mapOf(
            ProductProfile.MECH to mapOf(Scale.SIMPLE to 6.0, Scale.SMALL to 20.0, Scale.MEDIUM to 60.0, Scale.LARGE to 120.0, Scale.COMPLEX to 260.0),
            ProductProfile.WELD to mapOf(Scale.SIMPLE to 4.0, Scale.SMALL to 14.0, Scale.MEDIUM to 45.0, Scale.LARGE to 95.0, Scale.COMPLEX to 200.0),
        ),
        character = linkedMapOf("Изменение существующей КД" to 0.60, "Разработка по готовому аналогу" to 0.80, "Новая разработка по ТЗ" to 1.00, "Восстановление по образцу / замерам" to 1.30, "Разработка с поиском решения" to 1.50),
        pkg = linkedMapOf("Эскизный" to 0.45, "Рабочий минимальный" to 0.80, "Рабочий полный" to 1.00, "Расширенный" to 1.35),
        inputs = linkedMapOf("Полные исходные данные" to 1.00, "Частично полные исходные данные" to 1.15, "Требуются замеры" to 1.30, "Противоречивые / недостаточные данные" to 1.50),
        approval = linkedMapOf("Внутренний выпуск" to 1.00, "Производственная проверка" to 1.15, "ОТК и/или КБ заказчика" to 1.30, "Несколько циклов согласования" to 1.50)
    )
}

class CalculateEstimateUseCase {
    fun calculate(input: EstimateInput, config: NormConfig): CalcOutcome {
        if (input.profile == null || input.scale == null || input.character == null || input.pkg == null || input.inputs == null || input.approval == null) return CalcOutcome.ValidationError("Заполните обязательные поля")
        if (input.engineers <= 0) return CalcOutcome.ValidationError("Количество конструкторов должно быть больше 0")
        if (input.hoursPerDay <= 0 || input.reserve <= 0 || input.uncertainty < 0) return CalcOutcome.ValidationError("Проверьте коэффициенты")
        val base = config.baseHours[input.profile]?.get(input.scale) ?: return CalcOutcome.ValidationError("Нет базы")
        val t = base * (config.character[input.character] ?: 1.0) * (config.pkg[input.pkg] ?: 1.0) * (config.inputs[input.inputs] ?: 1.0) * (config.approval[input.approval] ?: 1.0)
        val tr = round(t * 10.0) / 10.0
        val d = ceil((t / (input.engineers * input.hoursPerDay)) * input.reserve).toInt()
        return CalcOutcome.Success(EstimateResult(tr, d, ceil(d * (1 - input.uncertainty)).toInt(), ceil(d * (1 + input.uncertainty)).toInt()))
    }
}
