package co.vgw.robocode.mattjames.ragnar.mover

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.Mover
import co.vgw.robocode.mattjames.ragnar.entity.Point
import co.vgw.robocode.mattjames.ragnar.util.bulletDistance
import co.vgw.robocode.mattjames.ragnar.util.moveTowards
import co.vgw.robocode.mattjames.ragnar.util.normaliseBearingRadians
import robocode.HitRobotEvent
import java.awt.Graphics2D
import java.lang.Math.floor


class RamMover(val ragnar: Ragnar) : Mover {

    override fun move(): Point{
        if(ragnar.botStore.enemies.size>0) {
            ragnar.botStore.weakestEnemy()//orderedEnemies()[(ragnar.myIndex-1)%ragnar.botStore.enemies.size]
                    ?.let {
                        ragnar.setTurnRightRadians(
                                normaliseBearingRadians(targetAngle(it))
//                            + wiggle()
                        )

                        ragnar.setAhead(100.0)
                        return ragnar.position().move(8.0, ragnar.state().headingRadians, 1)
                    }
        }

        return ragnar.position()
    }

    fun targetAngle(e: Bot): Double {
        var enemyPosition = e.point()
        var dt = 0L
        while(ragnar.position().distanceTo(enemyPosition) > 8.0*dt) {
            enemyPosition = e.estimatedPositionDt(dt)
            dt ++
        }
        return normaliseBearingRadians(
                ragnar.position().angleToPoint(enemyPosition) - ragnar.state().headingRadians)

    }


    override fun onPaint(g: Graphics2D) {

    }

    override fun onHitRobot(event: HitRobotEvent?) {

    }
    override fun onBulletShot() {

    }

}