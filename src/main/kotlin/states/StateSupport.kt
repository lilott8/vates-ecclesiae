package us.cedarfarm.states

import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.db.models.StateContext

typealias StateHandler = suspend (StateContext) -> StateContext


class StateDef(
    val state: CrawlerState,
    val transitions: MutableSet<CrawlerState> = mutableSetOf(),
    var handler: StateHandler? = null
)

class StateMachineBuilder {
    private val states = mutableMapOf<CrawlerState, StateDef>()

    fun state(state: CrawlerState, block: StateBuilder.() -> Unit) {
        val builder = StateBuilder(state)
        builder.block()
        states[state] = builder.build()
    }

    fun build(): StateMachine = StateMachine(states)
}

class StateBuilder(private val state: CrawlerState) {
    private val transitions = mutableSetOf<CrawlerState>()
    private var handler: StateHandler? = null

    fun transitionsTo(vararg nextStates: CrawlerState) {
        transitions += nextStates
    }

    fun handle(fn: StateHandler) {
        handler = fn
    }

    fun build(): StateDef = StateDef(state, transitions, handler)
}

fun stateMachine(block: StateMachineBuilder.() -> Unit): StateMachine {
    val builder = StateMachineBuilder()
    builder.block()
    return builder.build()
}