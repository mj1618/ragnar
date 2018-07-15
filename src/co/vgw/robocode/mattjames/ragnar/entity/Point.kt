package co.vgw.robocode.mattjames.ragnar.entity

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.util.HALF_PI
import co.vgw.robocode.mattjames.ragnar.util.normaliseBearingRadians
import co.vgw.robocode.mattjames.ragnar.util.sqrd
import java.io.Serializable
import java.lang.Math.*

class Point(val x: Double, val y: Double, val angleRadians: Double = 0.0): Serializable {
    fun to(angleRadians: Double, distance: Double): Point {
        return Point(
                x - (cos(angleRadians + HALF_PI) * distance),
                y + (sin(angleRadians + HALF_PI) * distance),
                angleRadians)
    }

    fun distanceTo(p: Point): Double {
        return sqrt(sqrd(x - p.x) + sqrd(y - p.y))
    }
    fun distanceTo(b: Bot): Double {
        val p = b.position()
        return sqrt(sqrd(x - p.x) + sqrd(y - p.y))
    }

    fun angleToPoint(p2: Point): Double {
        return if (p2.y < y){
            normaliseBearingRadians(atan((p2.x - x) / (p2.y - y)) + PI)
        } else {
            normaliseBearingRadians(atan((p2.x - x) / (p2.y - y)))
        }
    }

    fun outsideField(r: Ragnar): Boolean {
        val buffer=30.0
        return x < buffer || x > r.state().battleFieldWidth - buffer ||
                y < buffer || y > r.state().battleFieldHeight - buffer
    }

    override fun toString(): String {
        return "Point { $x, $y }"
    }

    constructor() : this(0.0, 0.0)

    fun vectorTo(p2: Point): Vector {
        return Vector(p2.x - x, p2.y - y)
    }

    fun move(velocity: Double, direction: Double, t: Long, acc: Double = 0.0): Point {
        val v = velocity
        val d = v*t
        val dx = d*sin(direction)
        val dy = d*cos(direction)
        return Point(x + dx, y + dy)
    }
    fun isMovingClockwise(enemy: Bot): Boolean {
        val a1 = angleToPoint(enemy.position())
        val a2 = angleToPoint(enemy.estimatedPositionDt(1))
        return a1<a2
    }
}
class Vector(val x: Double, val y: Double) {

    fun dot(v2: Vector): Double {
        return (x * v2.x + y*v2.y) / (magnitude() * v2.magnitude())
    }

    fun magnitude(): Double {
        return sqrt(sqrd(x) + sqrd(y))
    }

}

