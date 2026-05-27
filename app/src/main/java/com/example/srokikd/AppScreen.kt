package com.example.srokikd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.srokikd.ui.theme.AppBackground
import com.example.srokikd.ui.theme.AppBackgroundSecondary
import com.example.srokikd.ui.theme.AppButtonGradientEnd
import com.example.srokikd.ui.theme.AppButtonGradientStart
import com.example.srokikd.ui.theme.AppDivider
import com.example.srokikd.ui.theme.AppOutline
import com.example.srokikd.ui.theme.AppPrimary
import com.example.srokikd.ui.theme.AppPrimaryLight
import com.example.srokikd.ui.theme.AppPrimaryMuted
import com.example.srokikd.ui.theme.AppResultBorder
import com.example.srokikd.ui.theme.AppSurface
import com.example.srokikd.ui.theme.AppSurfaceResult
import com.example.srokikd.ui.theme.AppTextPrimary
import com.example.srokikd.ui.theme.AppTextSecondary
import com.example.srokikd.ui.theme.AppTextTertiary
import com.example.srokikd.ui.theme.SrokiKDTheme

private data class PickerOption(val title: String, val hint: String)

@Composable
fun AppScreen(vm: MainViewModel) {
    val s by vm.ui.collectAsState()
    val history by vm.history.collectAsState()

    SrokiKDTheme {
        Scaffold(
            containerColor = AppBackground,
            bottomBar = {
                NavigationBar(containerColor = AppBackgroundSecondary) {
                    listOf("Расчёт", "История", "Настройки").forEachIndexed { i, title ->
                        NavigationBarItem(
                            selected = s.tab == i,
                            onClick = { vm.setTab(i) },
                            icon = {},
                            label = { Text(title) }
                        )
                    }
                }
            }
        ) { paddings ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackground)
                    .padding(paddings)
            ) {
                when (s.tab) {
                    0 -> CalculatorScreen(s, vm)
                    1 -> HistoryScreen(history, vm)
                    else -> SettingsScreen(s, vm)
                }
            }
        }
    }
}

@Composable
private fun CalculatorScreen(s: UiState, vm: MainViewModel) {
    val i = s.input
    val uncertaintyPercent = (i.uncertainty * 100).toInt()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Spacer(Modifier.height(6.dp)) }
        item { AppTopBar(onHistoryClick = { vm.setTab(1) }, onSettingsClick = { vm.setTab(2) }) }

        item {
            ParameterSelectionCard(
                label = "Профиль изделия",
                value = i.profile?.title,
                options = ProductProfile.entries.map { it.title },
                icon = Icons.Outlined.Inventory2,
                hints = mapOf(
                    "Механические узлы и передачи" to "Валы, ролики, муфты, приводы, редукторы, опоры",
                    "Сварные металлоконструкции" to "Рамы, площадки, ограждения, навесы, несущие узлы"
                ),
                onSelect = { selected ->
                    vm.updateInput(i.copy(profile = ProductProfile.entries.first { it.title == selected }))
                }
            )
        }
        item {
            ParameterSelectionCard(
                label = "Характер разработки",
                value = i.character,
                options = s.config.character.keys.toList(),
                icon = Icons.Outlined.Edit,
                hints = mapOf(
                    "Изменение существующей КД" to "Изменить раму, добавить опоры, переработать крепления",
                    "Разработка по готовому аналогу" to "Есть модель, чертежи или понятный прототип",
                    "Новая разработка по ТЗ" to "Новый узел по ТЗ",
                    "Восстановление по образцу / замерам" to "Реверс-инжиниринг детали или узла",
                    "Разработка с поиском решения" to "Нет однозначной схемы, требуются варианты"
                ),
                onSelect = { vm.updateInput(i.copy(character = it)) }
            )
        }
        item {
            ParameterSelectionCard(
                label = "Масштаб объекта",
                value = i.scale?.title,
                options = Scale.entries.map { it.title },
                icon = Icons.Outlined.MyLocation,
                hints = mapOf(
                    "Отдельная деталь / простой элемент" to "Вал, втулка, плита, кронштейн, звёздочка",
                    "Малый узел / кронштейн в сборе" to "Ролик в сборе, опора, муфта, небольшой сварной кронштейн",
                    "Средняя сборочная единица / рама" to "Рама, площадка, приводной узел, корпус с опорами",
                    "Механизм / крупная металлоконструкция" to "Тележка, сервисная площадка, конвейерный узел, крупная рама",
                    "Машина / комплексный агрегат" to "Машина транспортировки, крупный агрегат, печное оборудование"
                ),
                onSelect = { selected ->
                    vm.updateInput(i.copy(scale = Scale.entries.first { it.title == selected }))
                }
            )
        }
        item {
            ParameterSelectionCard(
                label = "Комплект КД",
                value = i.pkg,
                options = s.config.pkg.keys.toList(),
                icon = Icons.Outlined.Layers,
                hints = mapOf(
                    "Эскизный" to "Общий вид / схема, базовая 3D-модель, ориентировочные габариты",
                    "Рабочий минимальный" to "3D-модель, чертеж детали/сборки, спецификация",
                    "Рабочий полный" to "Модели, чертежи, деталировка, спецификации, требования",
                    "Расширенный" to "Рабочий полный + расчёты, монтажные схемы и испытания"
                ),
                onSelect = { vm.updateInput(i.copy(pkg = it)) }
            )
        }
        item {
            ParameterSelectionCard(
                label = "Исходные данные",
                value = i.inputs,
                options = s.config.inputs.keys.toList(),
                icon = Icons.Outlined.Description,
                hints = mapOf(
                    "Полные исходные данные" to "Есть ТЗ, размеры, нагрузки, модели сопрягаемых узлов",
                    "Частично полные исходные данные" to "Часть размеров отсутствует, нужны уточнения",
                    "Требуются замеры" to "Есть только существующий узел или фото",
                    "Противоречивые / недостаточные данные" to "Нужно согласовывать решения и восстанавливать геометрию"
                ),
                onSelect = { vm.updateInput(i.copy(inputs = it)) }
            )
        }
        item {
            ParameterSelectionCard(
                label = "Согласование",
                value = i.approval,
                options = s.config.approval.keys.toList(),
                icon = Icons.Outlined.Groups,
                hints = mapOf(
                    "Внутренний выпуск" to "Проверка внутри КБ",
                    "Производственная проверка" to "Проверка технологом, сварщиком, изготовителем",
                    "ОТК и/или КБ заказчика" to "Проверка ОТК и согласование с КБ заказчика",
                    "Несколько циклов согласования" to "Заказчик, производство, промышленная безопасность, эксплуатация"
                ),
                onSelect = { vm.updateInput(i.copy(approval = it)) }
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumericParameterCard(
                    label = "Конструкторы",
                    value = i.engineers.toString(),
                    unit = "чел.",
                    icon = Icons.Outlined.Person,
                    onValue = { vm.updateInput(i.copy(engineers = it.toIntOrNull() ?: 0)) },
                    modifier = Modifier.weight(1f)
                )
                NumericParameterCard(
                    label = "Часов/день",
                    value = i.hoursPerDay.toString(),
                    unit = "ч/день",
                    icon = Icons.Outlined.Schedule,
                    onValue = { vm.updateInput(i.copy(hoursPerDay = it.toDoubleOrNullSmart() ?: i.hoursPerDay)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            CalculateButton(onClick = { vm.calculate() })
        }

        item {
            s.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            ResultCard(result = s.result, uncertaintyPercent = uncertaintyPercent)
        }
        item { Spacer(Modifier.height(18.dp)) }
    }
}

@Composable
fun AppTopBar(onHistoryClick: () -> Unit, onSettingsClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.Settings, contentDescription = "Настройки", tint = AppTextPrimary)
            }
            Text(
                "Сроки КД",
                color = AppTextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onHistoryClick) {
                Icon(Icons.Outlined.AccessTime, contentDescription = "История", tint = AppPrimary)
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Меню", tint = AppPrimary)
            }
        }
        Text(
            text = "Оценка трудоёмкости и срока разработки КД",
            color = AppTextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterSelectionCard(
    label: String,
    value: String?,
    options: List<String>,
    icon: ImageVector,
    hints: Map<String, String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val items = options.map { PickerOption(it, hints[it].orEmpty()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .border(1.dp, AppOutline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp)
                    .menuAnchor()
                    .clickable { expanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = AppPrimary, modifier = Modifier.size(25.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, color = AppTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text(value.orEmpty(), color = AppTextPrimary, style = MaterialTheme.typography.titleMedium)
                }
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = AppTextSecondary)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                items.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(option.title, color = AppTextPrimary)
                                if (option.hint.isNotBlank()) {
                                    Text(option.hint, color = AppTextTertiary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        },
                        onClick = {
                            onSelect(option.title)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NumericParameterCard(
    label: String,
    value: String,
    unit: String,
    icon: ImageVector,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .height(114.dp)
                .fillMaxWidth()
                .border(1.dp, AppOutline, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AppPrimary)
                Spacer(Modifier.width(8.dp))
                Text(label, color = AppTextSecondary)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValue,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium,
                label = { Text(unit) }
            )
        }
    }
}

@Composable
fun CalculateButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(AppButtonGradientStart, AppButtonGradientEnd))),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Outlined.Calculate, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Рассчитать", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ResultCard(result: EstimateResult?, uncertaintyPercent: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppResultBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceResult)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Результаты расчёта", color = AppPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Box(modifier = Modifier.border(1.dp, AppPrimaryMuted, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("±$uncertaintyPercent% точность", color = AppPrimaryLight)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                ResultMetric("Трудоёмкость", result?.laborHours?.toString() ?: "—", "чел.-ч", Modifier.weight(1f))
                VerticalSeparator()
                ResultMetric("Срок, дней", result?.durationDays?.toString() ?: "—", "рабочих дней", Modifier.weight(1f))
                VerticalSeparator()
                ResultMetric("Диапазон срока", result?.let { "${it.minDays} – ${it.maxDays}" } ?: "—", "рабочих дней", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AppDivider)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.Info, null, tint = AppPrimary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Расчёт выполнен по базовым коэффициентам методики оценки трудоёмкости КД. Итоговые значения требуют уточнения по составу работ.",
                    color = AppTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultMetric(title: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = AppTextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(value, color = AppPrimaryLight, fontSize = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(unit, color = AppTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun VerticalSeparator() {
    Box(modifier = Modifier.padding(horizontal = 6.dp).width(1.dp).height(120.dp).background(AppDivider))
}

@Composable
private fun HistoryScreen(items: List<HistoryEntity>, vm: MainViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("История расчётов", color = AppTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        if (items.isEmpty()) {
            item { Text("Сохранённых расчётов пока нет", color = AppTextSecondary) }
        } else {
            items(items) { entry ->
                HistoryItemCard(entry = entry, onDelete = { vm.delete(entry.id) })
            }
        }
    }
}

@Composable
fun HistoryItemCard(entry: HistoryEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(entry.project.ifBlank { "Без названия" }, color = AppTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(entry.profile, color = AppTextSecondary)
            Spacer(Modifier.height(6.dp))
            Text("${entry.labor} чел.-ч", color = AppPrimaryLight, fontWeight = FontWeight.Medium)
            Text("${entry.days} рабочих дней", color = AppPrimaryLight, fontWeight = FontWeight.Medium)
            Text(
                text = "Удалить",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp).clickable { onDelete() }
            )
        }
    }
}

@Composable
private fun SettingsScreen(s: UiState, vm: MainViewModel) {
    var cfg by remember(s.config) { mutableStateOf(s.config) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Настройки коэффициентов", color = AppTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        item { Text("Изменения влияют на новые расчёты.", color = AppTextSecondary) }

        item {
            SettingsSection("Характер разработки", cfg.character, onUpdate = { cfg = cfg.copy(character = it) })
        }
        item {
            SettingsSection("Комплект КД", cfg.pkg, onUpdate = { cfg = cfg.copy(pkg = it) })
        }
        item {
            SettingsSection("Исходные данные", cfg.inputs, onUpdate = { cfg = cfg.copy(inputs = it) })
        }
        item {
            SettingsSection("Согласование", cfg.approval, onUpdate = { cfg = cfg.copy(approval = it) })
        }

        item {
            Button(onClick = { vm.saveConfig(cfg) }, modifier = Modifier.fillMaxWidth()) {
                Text("Сохранить настройки")
            }
        }
        item {
            Button(
                onClick = {
                    vm.resetConfig()
                    cfg = Defaults.config()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppSurface)
            ) {
                Text("Восстановить значения по умолчанию", color = AppTextPrimary)
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    values: Map<String, Double>,
    onUpdate: (Map<String, Double>) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = AppSurface)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = AppTextPrimary, fontWeight = FontWeight.SemiBold)
            values.forEach { (key, value) ->
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = { raw ->
                        raw.toDoubleOrNullSmart()?.let { parsed ->
                            onUpdate(values.toMutableMap().apply { put(key, parsed) })
                        }
                    },
                    label = { Text(key, color = AppTextSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun String.toDoubleOrNullSmart(): Double? = replace(',', '.').toDoubleOrNull()
