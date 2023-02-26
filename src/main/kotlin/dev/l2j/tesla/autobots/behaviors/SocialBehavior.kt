package dev.l2j.tesla.autobots.behaviors

import dev.l2j.tesla.autobots.Autobot
import dev.l2j.tesla.autobots.AutobotData
import dev.l2j.tesla.autobots.behaviors.preferences.SocialPreferences
import dev.l2j.tesla.autobots.behaviors.preferences.TownAction
import dev.l2j.tesla.autobots.behaviors.sequences.ReturnToDeathLocationSequence
import dev.l2j.tesla.autobots.behaviors.sequences.TeleportToLocationSequence
import dev.l2j.tesla.autobots.behaviors.sequences.TradingSequence
import dev.l2j.tesla.autobots.extensions.getActiveSequence
import dev.l2j.tesla.autobots.extensions.getSocialBehavior
import dev.l2j.tesla.autobots.models.RespawnAction
import kotlinx.coroutines.delay
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.enums.ZoneId
import net.sf.l2j.gameserver.model.actor.Player

internal open class SocialBehavior(val bot: Player, var socialPreferences: SocialPreferences) {
    
    internal suspend fun onUpdate() {

        if(bot.getActiveSequence() != null)
            return
        
        if(bot is Autobot) {
            if(bot.hasActiveTradeRequest() && bot.activeTradeList == null) {
                delay(2000)
                bot.answerTradeRequest(true)
                TradingSequence(bot).also { it.execute() }
                return
            }
        }
        
        
        if(bot.getSocialBehavior().socialPreferences.townAction == TownAction.TeleToRandomLocation && Rnd.get(2500) == 0) {
            TeleportToLocationSequence(
                bot,
                AutobotData.teleportLocations.random().location
            ).execute()
        }
    }

    internal suspend fun onRespawn() {
        if(bot is Autobot) {
            when(bot.respawnAction){
                RespawnAction.None -> {}
                RespawnAction.ReturnToDeathLocation -> {
                    if(bot.isInsideZone(ZoneId.CHANGE_PVP_ZONE))
                        ReturnToDeathLocationSequence(bot,true,true).execute()
                    else if(bot.isInsideZone(ZoneId.TOWN))
                        ReturnToDeathLocationSequence(bot,false, false).execute()
                    else
                        ReturnToDeathLocationSequence(bot,false, true).execute()
                }
                RespawnAction.Logout -> {
                    delay(Rnd.get(10000, 60000).toLong())
                    bot.despawn()
                }
            }
        }
    }
}