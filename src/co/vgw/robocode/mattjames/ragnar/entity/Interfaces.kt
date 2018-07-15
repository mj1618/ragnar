package co.vgw.robocode.mattjames.ragnar.entity

import robocode.HitRobotEvent
import java.awt.Graphics2D

interface Mover {
    fun move(): Point
    fun onPaint(g: Graphics2D)
    fun onHitRobot(event: HitRobotEvent?)
    fun onBulletShot()
    fun step() {
    }

}