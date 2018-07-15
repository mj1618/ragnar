package co.vgw.robocode.mattjames.ragnar.gun

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.IBot
import co.vgw.robocode.mattjames.ragnar.entity.Point
import co.vgw.robocode.mattjames.ragnar.types.IAimer
import java.awt.Graphics2D

class GunController(val ragnar: Ragnar) {

    fun blast(nextPosition: Point) {

        val mark: Bot? = ragnar.botStore.getByName(ragnar.gang.myMark, true)
        val closest = ragnar.botStore.closestEnemy()
        val weakest = ragnar.botStore.weakestEnemy()
        var fired = false

        if(ragnar.gang.shouldGang()){
            if(!fired && mark!=null){
                fired = target(mark)
            }

            if(!fired && closest!=null) {
                fired = target(closest)
            }

            if(!fired && weakest!=null){
                fired = target(weakest)
            }
        } else {

            if(ragnar.botStore.weakestEnemy()!=null){
                ragnar.botStore.weakestEnemy()?.let {
                    target(it)
                }
            } else if(ragnar.botStore.closestEnemy()!=null){
                ragnar.botStore.closestEnemy()?.let {
                    target(it)
                }
            }
        }
    }

    fun target(bot: Bot): Boolean {
        val targeter:IAimer = if(ragnar.position().distanceTo(bot.position())<100.0)
            HeadOnAiming(ragnar,ragnar.position())
        else
            CircularAiming(ragnar, ragnar.position())
        return targeter.target(bot)
    }

    fun onGunTurnCompletion() {

    }
}