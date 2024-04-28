@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sangrok.bloc_mvi_sample.bloc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Bloc<STATE : Any, ACTION : Any>(
    initialState: STATE,
    private val actionTransformer: ActionTransformer<ACTION> = DefaultActionTransFormer(),
    private val actionMapper: ActionMapper<STATE, ACTION>,
    // NetworkError나 전역적인 에러를 가지고 다이얼로그를 띄운다거나 그럴때 사용
    private val errorMapper: ErrorMapper<STATE, ACTION> = DefaultErrorMapper(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {
    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow = _stateFlow.asStateFlow()

    val currentState: STATE
        get() = stateFlow.value

    private val actionFlow = Channel<ACTION>()

    /**
     * flatMapConcat은 순차적인 상태 변환을 의도해서 사용한 건 맞지만
     * 첫번째 액션이 오래걸리면 다른게 실행되지 않는 문제가 발생함
     * 이러한 문제를 ActionTransformer로 해결
     */
    fun start() {
        actionFlow.receiveAsFlow()
            .flatMapMerge { action -> transformAction(action) }  // Action을 받아 비동기적으로 다른 Action으로 변환
            .flatMapConcat { action -> mapActionToState(action) }  // Action을 받아 동기적으로 State로 변환
            .filter { it.currentState != it.nextState }
            .onEach {
                doOnTransition(it)
            }
            .launchIn(scope)
    }

    private suspend fun transformAction(action: ACTION): Flow<ACTION> {
        return actionTransformer.transformActions(action)
            .catch {
                emitAll(errorMapper.mapErrorToAction(currentState, action, it))
            }
    }

    private suspend fun mapActionToState(action: ACTION): Flow<Transition<STATE, ACTION>> {
        return actionMapper.mapActionToState(action, currentState)
            .catch {
                emitAll(errorMapper.mapErrorToState(currentState, action, it))
            }.map {
                Transition(currentState, action, it)
            }
    }

    /**
     * 액션의 전달을 bloc에 위임하는 역할로 dispatch라는 이름을 붙임
     */
    fun dispatch(action: ACTION) {
        scope.launch {
            actionFlow.send(action)
        }
    }

    private suspend fun doOnTransition(transition: Transition<STATE, ACTION>) {
        _stateFlow.emit(transition.nextState)
    }

    fun end() {
        scope.cancel()
        actionFlow.close()
    }
}