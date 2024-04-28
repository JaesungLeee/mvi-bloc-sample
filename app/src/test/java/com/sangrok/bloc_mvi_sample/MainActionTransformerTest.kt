package com.sangrok.bloc_mvi_sample

import com.sangrok.bloc_mvi_sample.bloc.ActionTransformer
import com.sangrok.bloc_mvi_sample.repository.MockRepository
import com.sangrok.bloc_mvi_sample.ui.main.MainAction
import com.sangrok.bloc_mvi_sample.ui.main.MainActionTransformer
import com.sangrok.bloc_mvi_sample.ui.main.Member
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations


/**
 * bloc-mvi-sample
 * @author jaesung
 * @created 4/28/24
 */

class MainActionTransformerTest {

    @Mock
    lateinit var mockRepository: MockRepository

    lateinit var mainActionTransformer: ActionTransformer<MainAction>

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        mainActionTransformer = MainActionTransformer(mockRepository)
    }

    @Test
    fun `GIVEN 좋아요 상태가 true WHEN 메인 토글 클릭 시 THEN 좋아요 상태가 false로 변경된다`() = runTest {
        // GIVEN
        val givenMember = Member(
            name = "JaesungLeee",
            liked = true
        )

        // WHEN
        val action = MainAction.ClickToggle(member = givenMember)
        val actualAction = mainActionTransformer.transformActions(action = action).first()

        // THEN
        val expectAction = MainAction.SetMemberState(
            member = givenMember.copy(
                name = "JaesungLeee",
                liked = givenMember.liked.not()
            )
        )

        // CHECK
        Assert.assertEquals(expectAction, actualAction)
    }
}