package com.example.srokikd

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class UiState(
    val tab: Int = 0,
    val config: NormConfig = Defaults.config(),
    val input: EstimateInput = EstimateInput(null,null,null,null,null,null,1,6.0,1.15,0.20),
    val result: EstimateResult? = null,
    val error: String? = null
)

class MainViewModel(private val repos: Repos): ViewModel() {
    private val calc = CalculateEstimateUseCase()
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()
    val history = repos.history.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { viewModelScope.launch { _ui.value = _ui.value.copy(config = repos.loadConfig()) } }
    fun setTab(t:Int){_ui.value=_ui.value.copy(tab=t)}
    fun updateInput(i: EstimateInput){_ui.value=_ui.value.copy(input=i)}
    fun calculate(){ when(val r=calc.calculate(_ui.value.input,_ui.value.config)){ is CalcOutcome.Success->_ui.value=_ui.value.copy(result=r.result,error=null); is CalcOutcome.ValidationError->_ui.value=_ui.value.copy(error=r.message)}}
    fun clear(){_ui.value=_ui.value.copy(input=EstimateInput(null,null,null,null,null,null,1,6.0,1.15,0.20), result=null,error=null)}
    fun save(project:String){ val rs=_ui.value.result?:return; val i=_ui.value.input; viewModelScope.launch{repos.saveHistory(HistoryEntity(date=LocalDateTime.now().toString(), project=project, profile=i.profile?.title?:"", labor=rs.laborHours, days=rs.durationDays, payload="")) }}
    fun delete(id:Long){viewModelScope.launch{repos.deleteHistory(id)}}
    fun saveConfig(c:NormConfig){viewModelScope.launch{repos.saveConfig(c);_ui.value=_ui.value.copy(config=c)}}
    fun resetConfig(){saveConfig(Defaults.config())}
    companion object { fun factory(context: Context) = object: ViewModelProvider.Factory{ override fun <T: ViewModel> create(modelClass: Class<T>): T = MainViewModel(Repos(context)) as T } }
}
