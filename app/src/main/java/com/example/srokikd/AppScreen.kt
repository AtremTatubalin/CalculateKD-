package com.example.srokikd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class PickerOption(val title: String, val hint: String)

private fun examplesFor(label: String, values: List<String>): List<PickerOption> {
    val hints = when (label) {
        "Характер разработки" -> mapOf(
            "Изменение существующей КД" to "Изменить раму, добавить опоры, переработать крепления",
            "Разработка по готовому аналогу" to "Есть модель, чертежи или понятный прототип",
            "Новая разработка по ТЗ" to "Новый узел по ТЗ",
            "Восстановление по образцу / замерам" to "Реверс-инжиниринг детали или узла",
            "Разработка с поиском решения" to "Нет однозначной схемы, требуются варианты"
        )

        "Масштаб объекта" -> mapOf(
            "Отдельная деталь / простой элемент" to "Вал, втулка, плита, кронштейн, звёздочка",
            "Малый узел / кронштейн в сборе" to "Ролик в сборе, опора, муфта, небольшой сварной кронштейн",
            "Средняя сборочная единица / рама" to "Рама, площадка, приводной узел, корпус с опорами",
            "Механизм / крупная металлоконструкция" to "Тележка, сервисная площадка, конвейерный узел, крупная рама",
            "Машина / комплексный агрегат" to "Машина транспортировки, крупный агрегат, печное оборудование"
        )

        "Профиль изделия" -> mapOf(
            "Механические узлы и передачи" to "Валы, ролики, муфты, приводы, редукторы, опоры",
            "Сварные металлоконструкции" to "Рамы, площадки, ограждения, навесы, несущие узлы"
        )

        "Комплект КД" -> mapOf(
            "Эскизный" to "Общий вид/схема, базовая 3D-модель, ориентировочные габариты",
            "Рабочий минимальный" to "3D-модель, чертеж детали или сборочный чертеж, спецификация",
            "Рабочий полный" to "Модели, сборочные чертежи, деталировка, спецификации, требования",
            "Расширенный" to "Рабочий полный + расчёты, монтажные схемы, программа испытаний"
        )

        "Состояние исходных данных" -> mapOf(
            "Полные исходные данные" to "Есть ТЗ, размеры, нагрузки, модели сопрягаемых узлов",
            "Частично полные исходные данные" to "Часть размеров отсутствует, нужны уточнения",
            "Требуются замеры" to "Есть только существующий узел или фото",
            "Противоречивые / недостаточные данные" to "Нужно согласовывать решения и восстанавливать геометрию"
        )

        "Уровень согласований" -> mapOf(
            "Внутренний выпуск" to "Проверка внутри КБ",
            "Производственная проверка" to "Проверка технологом, сварщиком, изготовителем",
            "ОТК и/или КБ заказчика" to "Проверка ОТК и согласование с КБ заказчика",
            "Несколько циклов согласования" to "Заказчик, производство, промышленная безопасность, эксплуатация"
        )

        else -> emptyMap()
    }
    return values.map { PickerOption(it, hints[it].orEmpty()) }
}

@Composable
fun AppScreen(vm: MainViewModel) {
    val s by vm.ui.collectAsState()
    val history by vm.history.collectAsState()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf("Расчёт", "История", "Настройки").forEachIndexed { i, t ->
                        NavigationBarItem(selected = s.tab == i, onClick = { vm.setTab(i) }, label = { Text(t) }, icon = {})
                    }
                }
            }
        ) { p ->
            when (s.tab) {
                0 -> CalcTab(s, vm, Modifier.padding(p))
                1 -> HistoryTab(history, vm, Modifier.padding(p))
                else -> SettingsTab(s, vm, Modifier.padding(p))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Picker(label: String, value: String?, opts: List<String>, onSel: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember(label, opts) { examplesFor(label, opts) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(option.title)
                            if (option.hint.isNotBlank()) {
                                Text(option.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    onClick = {
                        onSel(option.title)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun CalcTab(s: UiState, vm: MainViewModel, m: Modifier) {
    val i = s.input
    var showResult by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }

    LaunchedEffect(s.result) {
        if (s.result != null) showResult = true
    }

    LazyColumn(m.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Оценка срока разработки КД", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { Text("Оценка трудоёмкости и срока разработки КД", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Picker("Профиль изделия", i.profile?.title, ProductProfile.entries.map { it.title }) { vm.updateInput(i.copy(profile = ProductProfile.entries.first { e -> e.title == it })) } }
        item { Picker("Характер разработки", i.character, s.config.character.keys.toList()) { vm.updateInput(i.copy(character = it)) } }
        item { Picker("Масштаб объекта", i.scale?.title, Scale.entries.map { it.title }) { vm.updateInput(i.copy(scale = Scale.entries.first { e -> e.title == it })) } }
        item { Picker("Комплект КД", i.pkg, s.config.pkg.keys.toList()) { vm.updateInput(i.copy(pkg = it)) } }
        item { Picker("Состояние исходных данных", i.inputs, s.config.inputs.keys.toList()) { vm.updateInput(i.copy(inputs = it)) } }
        item { Picker("Уровень согласований", i.approval, s.config.approval.keys.toList()) { vm.updateInput(i.copy(approval = it)) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = i.engineers.toString(), onValueChange = { vm.updateInput(i.copy(engineers = it.toIntOrNull() ?: 0)) }, label = { Text("Конструкторы") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = i.hoursPerDay.toString(), onValueChange = { vm.updateInput(i.copy(hoursPerDay = it.toDoubleOrNullSmart() ?: i.hoursPerDay)) }, label = { Text("Часов/день") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = i.reserve.toString(), onValueChange = { vm.updateInput(i.copy(reserve = it.toDoubleOrNullSmart() ?: i.reserve)) }, label = { Text("Коэф. резерва") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = i.uncertainty.toString(), onValueChange = { vm.updateInput(i.copy(uncertainty = it.toDoubleOrNullSmart() ?: i.uncertainty)) }, label = { Text("Неопределённость") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row {
                Button(onClick = { vm.calculate() }) { Text("Рассчитать") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.clear() }) { Text("Очистить") }
            }
        }
        item { s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
    }

    if (showResult && s.result != null) {
        AlertDialog(
            onDismissRequest = { showResult = false },
            title = { Text("Результаты расчёта") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                        Column(Modifier.padding(12.dp)) {
                            Text("Трудоёмкость: ${s.result!!.laborHours} чел.-ч")
                            Text("Срок: ${s.result!!.durationDays} рабочих дней")
                            Text("Диапазон: ${s.result!!.minDays}–${s.result!!.maxDays} рабочих дней")
                        }
                    }
                    OutlinedTextField(value = projectName, onValueChange = { projectName = it }, label = { Text("Наименование проекта") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = { vm.save(projectName); showResult = false }) { Text("Сохранить результат") }
            },
            dismissButton = {
                Button(onClick = { showResult = false }) { Text("Закрыть") }
            }
        )
    }
}

@Composable
fun HistoryTab(h: List<HistoryEntity>, vm: MainViewModel, m: Modifier) {
    LazyColumn(m.padding(12.dp)) {
        items(h) {
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(it.project.ifBlank { "Без названия" })
                        Text(it.profile)
                        Text("${it.labor} чел.-ч, ${it.days} дн.")
                    }
                    Text("Удалить", modifier = Modifier.clickable { vm.delete(it.id) })
                }
            }
        }
    }
}

@Composable
fun SettingsTab(s: UiState, vm: MainViewModel, m: Modifier) {
    var cfg by remember(s.config) { mutableStateOf(s.config) }

    LazyColumn(m.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Настройки норм", style = MaterialTheme.typography.titleLarge) }
        item { Text("Изменения применяются к новым расчётам после сохранения.") }

        item { Text("Коэффициенты характера разработки", fontWeight = FontWeight.SemiBold) }
        items(cfg.character.keys.toList()) { key ->
            OutlinedTextField(
                value = cfg.character[key].toString(),
                onValueChange = { v -> v.toDoubleOrNullSmart()?.let { num -> cfg = cfg.copy(character = cfg.character.toMutableMap().apply { put(key, num) }) } },
                label = { Text(key) }, modifier = Modifier.fillMaxWidth()
            )
        }

        item { Text("Коэффициенты комплекта КД", fontWeight = FontWeight.SemiBold) }
        items(cfg.pkg.keys.toList()) { key ->
            OutlinedTextField(value = cfg.pkg[key].toString(), onValueChange = { v -> v.toDoubleOrNullSmart()?.let { num -> cfg = cfg.copy(pkg = cfg.pkg.toMutableMap().apply { put(key, num) }) } }, label = { Text(key) }, modifier = Modifier.fillMaxWidth())
        }

        item { Text("Коэффициенты исходных данных", fontWeight = FontWeight.SemiBold) }
        items(cfg.inputs.keys.toList()) { key ->
            OutlinedTextField(value = cfg.inputs[key].toString(), onValueChange = { v -> v.toDoubleOrNullSmart()?.let { num -> cfg = cfg.copy(inputs = cfg.inputs.toMutableMap().apply { put(key, num) }) } }, label = { Text(key) }, modifier = Modifier.fillMaxWidth())
        }

        item { Text("Коэффициенты согласований", fontWeight = FontWeight.SemiBold) }
        items(cfg.approval.keys.toList()) { key ->
            OutlinedTextField(value = cfg.approval[key].toString(), onValueChange = { v -> v.toDoubleOrNullSmart()?.let { num -> cfg = cfg.copy(approval = cfg.approval.toMutableMap().apply { put(key, num) }) } }, label = { Text(key) }, modifier = Modifier.fillMaxWidth())
        }

        item { Text("Базовые часы Tbase", fontWeight = FontWeight.SemiBold) }
        items(ProductProfile.entries) { p ->
            Text(p.title, fontWeight = FontWeight.Medium)
            Scale.entries.forEach { sc ->
                OutlinedTextField(
                    value = cfg.baseHours[p]?.get(sc)?.toString().orEmpty(),
                    onValueChange = { v ->
                        v.toDoubleOrNullSmart()?.let { num ->
                            val inner = cfg.baseHours[p]?.toMutableMap() ?: mutableMapOf()
                            inner[sc] = num
                            cfg = cfg.copy(baseHours = cfg.baseHours.toMutableMap().apply { put(p, inner) })
                        }
                    },
                    label = { Text(sc.title) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { vm.saveConfig(cfg) }) { Text("Сохранить настройки") }
                Button(onClick = { vm.resetConfig(); cfg = Defaults.config() }) { Text("Восстановить значения по умолчанию") }
            }
        }
    }
}

private fun String.toDoubleOrNullSmart(): Double? = replace(',', '.').toDoubleOrNull()
