package us.cedarfarm.states

import us.cedarfarm.db.models.CrawlerState
import us.cedarfarm.db.models.StateContext

class StateMachine(private val states: Map<CrawlerState, StateDef>) {

    suspend fun next(context: StateContext): StateContext {
        val state = context.record.state
        val def = states[state] ?: error("Undefined state: $state")
        val handler = def.handler ?: error("No handler for state $state")

        return try {
            val next = handler(context)
            val nextState = next.record.state
            if (nextState != state && nextState !in def.transitions) {
                error("Invalid transition $state → $nextState")
            }
            next
        } catch (e: Exception) {
            // Set the record to failed
            context.dal.update(context.record.id.value, state=CrawlerState.FAILED)
            context.copy(record = context.record)
        }
    }

    suspend fun run(context: StateContext): StateContext {
        var ctx = context
        while (ctx.record.state != CrawlerState.COMPLETE && ctx.record.state != CrawlerState.FAILED) {
            ctx = next(ctx)
        }
        return ctx
    }
}