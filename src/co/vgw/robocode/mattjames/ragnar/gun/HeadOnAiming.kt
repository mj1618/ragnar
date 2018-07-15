package co.vgw.robocode.mattjames.ragnar.gun

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.Point
import co.vgw.robocode.mattjames.ragnar.types.IAimer
import co.vgw.robocode.mattjames.ragnar.util.blockedPath
import co.vgw.robocode.mattjames.ragnar.util.bulletDistance
import co.vgw.robocode.mattjames.ragnar.util.normaliseBearingRadians
import java.lang.Math.toDegrees
import java.lang.Math.toRadians

class HeadOnAiming(val ragnar: Ragnar, val nextPosition: Point) : IAimer {
    var enemyPosition = Point(0.0, 0.0)
    override fun target(e: Bot): Boolean {
        val bulletPower = bulletPower(distanceTo(e.position(), nextPosition))
        val turnAngle = normaliseBearingRadians(ragnar.position().angleToPoint(e.position()) - ragnar.state().gunHeadingRadians)
        ragnar.setTurnGunRightRadians(turnAngle)
        val d = ragnar.position().distanceTo(enemyPosition)
        if(blockedPath(ragnar.position(), enemyPosition, ragnar.botStore.team().filter { it.distanceTo(ragnar.position()) < d }.map { it.position() })){
            return false
        }
        if(toDegrees(turnAngle) < 20){
            ragnar.setFire(bulletPower)
        }
        return true
    }

    private fun distanceTo(enemyPosition: Point, nextPosition: Point): Double {
        return nextPosition.distanceTo(enemyPosition)
    }


    private fun bulletPower(distance: Double): Double {
        if(distance<100){
            return 3.0
        }
        return Math.pow((ragnar.state().battleFieldWidth - distance) / ragnar.state().battleFieldWidth, 1.0) * 3.0
    }

}