package com.sangrok.bloc_mvi_sample.ui.main

import com.sangrok.bloc_mvi_sample.bloc.ViewAction

/**
 * Action 네이밍을 할 때 크게 두가지 기준을 세울 수 있음
 * 1. 사용자의 행위
 * 2. 사용자의 행위로 인해 발생되는 동작
 *
 * 2번으로 짓게되면 onAction(MainAction.ClickTab)처럼 내부 로직을 모르게 할 수 있어 ㄱㅊ다.
 */
sealed class MainAction : ViewAction {
    object ClickButton : MainAction()

    data class ClickTab(val selectedTab: Tab) : MainAction()

}
