package co.vgw.robocode.mattjames.ragnar.util

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.debugPrintln
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.Point
import java.awt.Color
import java.awt.Color.*
import java.lang.Math.*
import java.util.*
import java.util.Random



fun normaliseBearingRadians(angleRadians: Double): Double {
    var angle = angleRadians
    while (angle > PI) angle -= 2*PI
    while (angle < -PI) angle += 2*PI
    return angle
}
fun sqrd(x: Double): Double {
    return pow(x, 2.0)
}

fun sqrd(x: Int): Int {
    return x*x
}

val TWO_PI = 2.0 * PI
val HALF_PI = PI / 2.0
val QUART_PI = PI / 4.0

val ROBOT_SIZE = 32.0

typealias Line = Pair<Point, Point>

fun shortestDistanceFromLine(p: Point, l: Line): Double {
    val A = -1.0 * gradient(l)
    val B = 1.0
    val C = -1.0 * yintercept(l)
    val x1 = p.x
    val y1 = p.y
    return abs(A * x1 + B * y1 + C) / sqrt(A * A + B * B)
}

fun center(ragnar: Ragnar): Point {
    return Point(ragnar.state().battleFieldWidth / 2.0, ragnar.state().battleFieldHeight / 2.0)
}

fun shortestDistanceFromWalls(p: Point, r: Ragnar): Double {
    return walls(r).map { shortestDistanceFromLine(p, it) }.sorted().first()
}

fun isHeadingIntoWall(ragnar: Ragnar): Boolean {
    return walls(ragnar).any { distanceToWallHit(ragnar, it) < ROBOT_SIZE *4 }
}

fun isHeadingBackwardsIntoWall(ragnar: Ragnar): Boolean {
    return walls(ragnar).any { distanceToWallHitBackwards(ragnar, it) < ROBOT_SIZE *4 }
}

fun distanceToWallHit(ragnar: Ragnar, wall: Line): Double {
    val p = intersect(ragnar, wall)
    if(abs(normaliseBearingRadians(ragnar.position().angleToPoint(p))) < HALF_PI){
        return p.distanceTo(ragnar.position())
    } else {
        return Double.POSITIVE_INFINITY
    }
}

fun distanceToWallHitBackwards(ragnar: Ragnar, wall: Line): Double {
    val p = intersect(ragnar, wall)
    if(abs(normaliseBearingRadians(ragnar.position().angleToPoint(p))) > HALF_PI){
        return p.distanceTo(ragnar.position())
    } else {
        return Double.POSITIVE_INFINITY
    }
}

fun intersect(ragnar: Ragnar, line: Line): Point {
    val x = (yintercept(ragnar) - yintercept(line)) / (gradient(line) - gradient(ragnar))
    val y = gradient(line) * x + yintercept(line)
    val p = Point(x, y)
    return p
}

fun yintercept(ragnar: Ragnar): Double{
    return ragnar.position().y - gradient(ragnar) * ragnar.position().x
}

fun yintercept(line: Line): Double {
    return line.first.y - gradient(line) * line.first.x
}

fun corners(r: Ragnar): List<Point> {
    return mutableListOf(
            Point(0.0, 0.0),
            Point(0.0, r.state().battleFieldHeight),
            Point(r.state().battleFieldWidth, r.state().battleFieldHeight),
            Point(r.state().battleFieldWidth, 0.0)
    )
}

fun gradient(ragnar: Ragnar): Double {
    return gradient(ragnar.position(), ragnar.state().headingRadians)
}

fun gradient(p: Point, angleRadians: Double): Double {
    return tan(HALF_PI - angleRadians)
}

fun gradient(l: Line): Double {
    return (l.second.y - l.first.y) / (l.second.x - l.first.x)
}

fun walls(r: Ragnar): List<Line> {
    val walls = ArrayList<Line>()
    val corners = corners(r)
    (0..3).mapTo(walls) { Pair(corners[it], corners[(it +1)%4]) }
    return walls
}

fun bulletDistance(bulletPower: Double, dt: Long): Double {
    return (20 - 3 * bulletPower) * dt
}

var lastDir = 1

fun moveTowards(pos: Point, ragnar: Ragnar): Int {

    val hitTeam = ragnar.allHitRobotEvents
            .filter { it.isMyFault }
            .filter { it.name.toLowerCase().contains("ragnar") }
            .filter { ragnar.time - it.time < 2 }
    if(hitTeam.isNotEmpty()){
        ragnar.setTurnRightRadians(0.0)
        lastDir *= -1
        ragnar.setAhead( -1.0 * lastDir * 100.0)
        return lastDir
    }

    val hitEnemy = ragnar.allHitRobotEvents
            .filter { it.isMyFault }
            .filter { it.energy > ragnar.state().energy }
            .filter { !it.name.toLowerCase().contains("ragnar") }
            .filter { ragnar.time - it.time < 2 }
    if(hitEnemy.isNotEmpty()){
        ragnar.setTurnRightRadians(0.0)
        lastDir *= -1
        ragnar.setAhead( -1.0 * lastDir * 100.0)
        return lastDir
    }

    val angle = normaliseBearingRadians(ragnar.position().angleToPoint(pos) - ragnar.state().headingRadians)

    if(Math.abs(angle) > HALF_PI){
        val nahead = if (abs(normaliseBearingRadians(angle+PI)) < PI/2.0) 100.0 else 0.0
        ragnar.setTurnRightRadians(normaliseBearingRadians(angle + PI))
        ragnar.setAhead(-1.0*nahead)
        lastDir = -1
        return -1
    } else {
        val nahead = if (abs(angle) < PI/2.0) 100.0 else 0.0
        ragnar.setTurnRightRadians(angle)
        ragnar.setAhead(nahead)
        lastDir = 1
        return 1
    }
}
val colours = listOf<Color>(GREEN, BLUE, YELLOW, ORANGE, RED)

fun restrict(min: Double, max: Double, value: Double): Double {
    return Math.max(Math.min(value, max), min)
}

fun colour(min: Double, max: Double, value: Double): Color {
    val d = max - min
    val i = restrict(0.0, 4.0, floor(value / d * colours.size)).toInt()
    return colours[i]
}

fun perpendicularity(p1: Point, p2: Point, p3: Point, p4: Point): Double {
    val angle = angleBetweenLines(p1,p2,p3,p4)
    return abs(angle) / HALF_PI
}

fun angleBetweenLines(p1: Point, p2: Point, p3: Point, p4: Point): Double {
    val dot = p1.vectorTo(p2).dot(p3.vectorTo(p4))
    val angle = acos(dot)
    return when {
        angle > HALF_PI -> PI - angle
        angle < -HALF_PI -> angle + PI
        else -> angle
    }
}

fun approx(v: Double, i: Int): Boolean {
    return abs(v-i.toDouble()) < 0.01
}

fun combination(n: Int): List<List<Int>> {
    return when(n){
        5 -> combination3of5()
        4 -> combination2of4()
        3 -> combination2of3()
        2 -> combination2of2()
        else -> listOf()
    }
}

fun smoothPoint(pt: Point, r: Ragnar): Point {
    val pad = 30.0
    return Point(max(pad, min(r.battleFieldWidth-pad, pt.x)), max(pad, min(r.battleFieldHeight-pad, pt.y)))
}

fun wallSmooth(p: Point, smooth: Double, ragnar: Ragnar): Point {
    val x: Double = if (p.x<smooth) {
        smooth
    } else if(p.x>ragnar.state().battleFieldWidth-smooth){
        ragnar.state().battleFieldWidth-smooth
    } else {
        p.x
    }
    val y: Double = if (p.y<smooth) {
        smooth
    } else if(p.y>ragnar.state().battleFieldHeight-smooth){
        ragnar.state().battleFieldHeight-smooth
    } else {
        p.y
    }
    return Point(x,y)
}


fun estimatedPositionCircular(position: Point, velocity: Double, headingRadians: Double, headingChangePerSecond: Double, dt: Long, ragnar: Ragnar): Point {
    return wallSmooth(position.move(velocity, headingRadians + headingChangePerSecond * dt, dt), 20.0, ragnar)
}

fun estimatedPositionDt(position: Point, velocity: Double, headingRadians: Double, acceleration: Double, dt: Long, ragnar: Ragnar): Point {
    return wallSmooth(position.move(velocity, headingRadians, dt, smoothAcceleration(acceleration)), 20.0, ragnar)
}

fun smoothAcceleration(a: Double): Double {
    return max(-1.0, min(1.0, a))
}

fun combination3of5(): List<List<Int>> {
    return listOf(
            listOf(0, 1, 2),
            listOf(0, 1, 3),
            listOf(0, 1, 4),
            listOf(0, 2, 3),
            listOf(0, 2, 4),
            listOf(0, 3, 4),
            listOf(1, 2, 3),
            listOf(1, 2, 4),
            listOf(1, 3, 4),
            listOf(2, 3, 4)
    )
}

fun combination2of4(): List<List<Int>> {
    return listOf(
            listOf(0, 1),
            listOf(0, 2),
            listOf(0, 3)
    )
}

fun combination2of3(): List<List<Int>> {
    return listOf(
            listOf(0, 1, 2)
//            listOf(0, 2),
//            listOf(1, 2)
    )
}

fun combination2of2(): List<List<Int>> {
    return listOf(
            listOf(0, 1)
    )
}

fun <T> permutations3(ls: List<T>): List<List<T>> {
    return listOf(
            listOf(ls[0], ls[1], ls[2]),
            listOf(ls[0], ls[2], ls[1]),
            listOf(ls[1], ls[0], ls[2]),
            listOf(ls[1], ls[2], ls[0]),
            listOf(ls[2], ls[0], ls[1]),
            listOf(ls[2], ls[1], ls[0])
    )
}

fun blockedPath(pt1: Point, pt2: Point, pts: List<Point>, debug: Boolean=false): Boolean {
    val robotSize = 36.0 * 2.0
    return pts.any {
        if(debug)
            debugPrintln("pt1: $pt1 pt2: $pt2 blocker: $it")
        var lineAngle = pt1.angleToPoint(pt2)
        val angleToBot = pt1.angleToPoint(it)
        var minAngle = pt1.angleToPoint(it.to(angleToBot - HALF_PI, robotSize))
        var maxAngle = pt1.angleToPoint(it.to(angleToBot + HALF_PI, robotSize))
        while(maxAngle < minAngle){
            maxAngle+=TWO_PI
        }
        while(maxAngle - minAngle > TWO_PI){
            minAngle+= TWO_PI
        }
        while(lineAngle<minAngle){
            lineAngle+= TWO_PI
        }

        while(lineAngle> maxAngle){
            lineAngle-= TWO_PI
        }
        if(debug){
            debugPrintln("block: ${toDegrees(lineAngle)} ${toDegrees(angleToBot)} ${toDegrees(minAngle)} ${toDegrees(maxAngle)}")
        }

        lineAngle in minAngle..maxAngle
    }
}
