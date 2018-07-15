package co.vgw.robocode.mattjames.ragnar.entity

import co.vgw.robocode.mattjames.ragnar.Ragnar
import robocode.ScannedRobotEvent
import java.io.Serializable
import java.lang.Math.cos
import java.lang.Math.sin

class ScannedRobotEventWrapper(
        val name: String,
        var time: Long,
        val headingRadians: Double,
        val velocity: Double,
        val energy: Double,
        val x: Double,
        val y: Double): Serializable {

    constructor(ragnar: Ragnar, e: ScannedRobotEvent):
            this(
                    e.name,
                    ragnar.state().time,
                    e.headingRadians,
                    e.velocity,
                    e.energy,
                    ragnar.state().x + sin(ragnar.state().headingRadians + e.bearingRadians)*e.distance,
                    ragnar.state().y + cos(ragnar.state().headingRadians + e.bearingRadians)*e.distance)

    constructor(ragnar: Ragnar):
            this(
                    ragnar.myName,
                    ragnar.state().time,
                    ragnar.state().headingRadians,
                    ragnar.state().velocity,
                    ragnar.state().energy,
                    ragnar.state().x,
                    ragnar.state().y
            )
}