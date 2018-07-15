package co.vgw.robocode.mattjames.ragnar.radar
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.debugPrintln
import co.vgw.robocode.mattjames.ragnar.util.normaliseBearingRadians
import java.lang.Math.PI

class Radar(val ragnar: Ragnar) {
    var lock: Bot? = null
    var lastSeen: Long = -1
    var direction: Int = 1

    fun scan() {
        when {
            ragnar.state().time < 10 -> ragnar.setTurnRadarLeftRadians(2 * PI)
            ragnar.botStore.enemies.size < ragnar.others -> ragnar.setTurnRadarLeftRadians(2 * PI)
            ragnar.botStore.enemies.size==0 -> debugPrintln("store size 0!!! " + ragnar.others)
            ragnar.botStore.enemies.size==1 -> {
                lock = ragnar.botStore.stalest()
                ragnar.setTurnRadarRightRadians(
                        2.0 * normaliseBearingRadians(lock!!.angleRadians() - ragnar.state().radarHeadingRadians))

            }
            else -> {
                findLock()
                ragnar.setTurnRadarRightRadians(
                        direction * PI)
            }
        }
    }

    private fun findLock() {
        if(lastSeen != lock?.lastSeenTime()) {
            val toLock = ragnar.botStore.stalest()
            lock = toLock
            lastSeen = toLock.lastSeenTime()
            direction = if (normaliseBearingRadians(lock!!.angleRadians() - ragnar.state().radarHeadingRadians) < 0) -1 else 1
        }
    }
}