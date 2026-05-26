package com.example.srokikd

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppScreen(vm: MainViewModel) {
    val s by vm.ui.collectAsState()
    val history by vm.history.collectAsState()

    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    listOf("Расчёт", "История", "Настройки").forEachIndexed { i, t ->
                        NavigationBarItem(
                            selected = s.tab == i,
                            onClick = { vm.setTab(i) },
                            label = { Text(t) },
                            icon = {}
                        )
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
fun Picker(
    label: String,
    value: String?,
    opts: List<String>,
    onSel: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opts.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSel(option)
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
    var proj by remember { mutableStateOf("") }

    LazyColumn(m.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("Оценка срока разработки КД", style = MaterialTheme.typography.titleLarge) }
        item { Picker("Профиль изделия", i.profile?.title, ProductProfile.entries.map { it.title }) { vm.updateInput(i.copy(profile = ProductProfile.entries.first { e -> e.title == it })) } }
        item { Picker("Масштаб объекта", i.scale?.title, Scale.entries.map { it.title }) { vm.updateInput(i.copy(scale = Scale.entries.first { e -> e.title == it })) } }
        item { Picker("Характер разработки", i.character, s.config.character.keys.toList()) { vm.updateInput(i.copy(character = it)) } }
        item { Picker("Комплект КД", i.pkg, s.config.pkg.keys.toList()) { vm.updateInput(i.copy(pkg = it)) } }
        item { Picker("Состояние исходных данных", i.inputs, s.config.inputs.keys.toList()) { vm.updateInput(i.copy(inputs = it)) } }
        item { Picker("Уровень согласований", i.approval, s.config.approval.keys.toList()) { vm.updateInput(i.copy(approval = it)) } }
        item {
            OutlinedTextField(
                value = i.engineers.toString(),
                onValueChange = { vm.updateInput(i.copy(engineers = it.toIntOrNull() ?: 0)) },
                label = { Text("Количество конструкторов") }
            )
        }
        item {
            Row {
                Button(onClick = { vm.calculate() }) { Text("Рассчитать") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.clear() }) { Text("Очистить") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.save(proj) }) { Text("Сохранить расчёт") }
            }
        }
        item { OutlinedTextField(value = proj, onValueChange = { proj = it }, label = { Text("Наименование проекта (необязательно)") }) }
        item {
            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            s.result?.let {
                Card {
                    Column(Modifier.padding(8.dp)) {
                        Text("Итоговая трудоёмкость: ${it.laborHours} чел.-ч")
                        Text("Плановый срок: ${it.durationDays} раб. дн.")
                        Text("Диапазон: ${it.minDays}-${it.maxDays} раб. дн.")
                        Text("Оценка предварительная; уточняется после проверки исходных данных и первого согласования компоновки")
                    }
                }
            }
        }
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
    Column(m.padding(12.dp)) {
        Text("Настройки норм")
        Button(onClick = { vm.resetConfig() }) { Text("Восстановить значения по умолчанию") }
        Button(onClick = { vm.saveConfig(s.config) }) { Text("Сохранить настройки") }
    }
}
