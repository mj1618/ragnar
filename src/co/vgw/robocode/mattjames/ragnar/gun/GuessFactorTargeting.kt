package co.vgw.robocode.mattjames.ragnar.gun

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.debugPrintln
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.Point
import co.vgw.robocode.mattjames.ragnar.types.IAimer
import co.vgw.robocode.mattjames.ragnar.util.*
import java.awt.Color
import java.awt.Graphics2D
import java.lang.Math.*

class GuessFactorTargeting(val ragnar: Ragnar) : IAimer {
    override fun target(mark: Bot): Boolean {
        if(blockedPath(ragnar.position(), mark.position(), ragnar.botStore.team().filter { it.distanceTo(ragnar.position()) < mark.distanceTo(ragnar.position()) }.map { it.position() })){
            return false
        }
        fireAt(mark)
        return true
    }

    val nAngles = 50
    val bins = (0 until 20).map { (0 until 20).map { (0 until nAngles).map { Bin(ragnar) } } }
    var waves = mutableListOf<Wave>()

    fun step() {
        waves = waves.filter { it.isActive() }.toMutableList()
        for(wave in waves){
            calcBuckets(wave)
        }
        for(bin in bins){
            for(b in bin)
                for(b2 in b)
                    b2.step()
        }

    }

    fun binFiringAngle(enemyAngle: Double, binI: Int, escapeAngle: Double, movingClockwise: Boolean): Double{
        val angle = binI * 2.0 * escapeAngle / nAngles
        return if (movingClockwise) {
            enemyAngle - escapeAngle + angle
        } else {
            enemyAngle + escapeAngle - angle
        }
    }

    fun binFromFiringAngle(initialEnemyAngle: Double, hitAngle:Double, escapeAngle: Double, movingClockwise: Boolean): Int{
        val min = initialEnemyAngle - escapeAngle
        val n = (hitAngle - min) / (2.0 * escapeAngle) * nAngles
        debugPrintln("binFromFiringAngle: "+ toDegrees(initialEnemyAngle)+ " "+ toDegrees(hitAngle)+" "+ toDegrees(escapeAngle)+" "+n.toInt())
        return if(movingClockwise){
            n.toInt()
        } else {
            nAngles - n.toInt()
        }
    }

    fun fireAt(enemy: Bot) {
        val dist = ragnar.position().distanceTo(enemy.position())
        val bulletPower = bulletPower(dist)
        val escapeAngle = getEscapeAngle(enemy, bulletPower)
        val binI = chooseBin(enemy, dist)
        val turnAngle: Double = if(binI < 0){
            CircularAiming(ragnar, ragnar.position()).targetAngle(enemy, bulletPower, ragnar.position())
        } else {
            val angle = binFiringAngle(ragnar.position().angleToPoint(enemy.position()), binI, escapeAngle, ragnar.position().isMovingClockwise(enemy))
            normaliseBearingRadians(angle - ragnar.gunHeadingRadians)
        }
//        val turnAngle = CircularAiming(ragnar, ragnar.position()).targetPoint(enemy, bulletPower, ragnar.position())

        ragnar.setTurnGunRightRadians(turnAngle)
        if(turnAngle < PI/8.0 && ragnar.state().gunHeat < 0.01){
            ragnar.setFire(bulletPower)
            debugPrintln("adding wave")
            waves.add(0, Wave(ragnar.position(), bulletPower, ragnar.state().time, nAngles, ragnar))
        }
    }

    fun onPaint(g: Graphics2D){
        val min = bins.map { it.map { it.map { it.value() }.min() as Double }.min() as Double }.min() ?: 0.0
        val max = bins.map { it.map { it.map { it.value() }.max() as Double }.max() as Double }.max() ?: 0.0
        if(ragnar.botStore.enemies.size>0){
            val enemy = ragnar.botStore.enemies().first()
            val dist = ragnar.position().distanceTo(enemy.position())
            val bulletPower = bulletPower(dist)
            val sign = if (ragnar.position().isMovingClockwise(enemy)) 1.0 else -1.0
            val escapeAngle =  getEscapeAngle(enemy, bulletPower)
            for(i in (0 until nAngles)){
                val angle = binFiringAngle(ragnar.position().angleToPoint(enemy.position()), i, escapeAngle, ragnar.position().isMovingClockwise(enemy))
                val pos = ragnar.position().to(angle, dist)
                g.color = colour(min, max, bins[accToBin(enemy)][distanceToBin(dist)][i].value().toDouble())
                g.drawOval(pos.x.toInt(), pos.y.toInt(), 10, 10)
            }
            val binI = chooseBin(enemy, dist)
            val angle = binFiringAngle(ragnar.position().angleToPoint(enemy.position()), binI, escapeAngle, ragnar.position().isMovingClockwise(enemy))
            val pos = ragnar.position().to(angle, dist)
            debugPrintln("binI: "+binI+" angle: "+ toDegrees(angle))
            g.color = Color.GREEN
            g.drawLine(ragnar.position().x.toInt(), ragnar.position().y.toInt(), pos.x.toInt(), pos.y.toInt())

        }

        for(wave in waves){
            debugPrintln("painting wave: "+wave.time)
            wave.onPaint(g)
            for(i in (0 until 100)){
                val enemy = ragnar.botStore.enemies().first()
                val dist = ragnar.position().distanceTo(enemy.position())
                val bulletPower = bulletPower(dist)
                val escapeAngle =  getEscapeAngle(enemy, bulletPower)
                val angle = binFiringAngle(ragnar.position().angleToPoint(enemy.position()), i, escapeAngle, ragnar.position().isMovingClockwise(enemy))
                val pos = ragnar.position().to(angle, dist)
                g.color = colour(min, max, bins[accToBin(enemy)][distanceToBin(dist)][i].value())
                g.drawOval(pos.x.toInt(), pos.y.toInt(), 10, 10)
            }
        }
    }

    private fun accToBin(enemy: Bot): Int {
        return (enemy.acceleration() + 8.0).toInt()
    }


    private fun chooseBin(enemy: Bot, distance: Double): Int {
        if(bins[accToBin(enemy)][distanceToBin(distance)].map { it.value() }.sum() < 1.0){
            return (nAngles * randBetween(2, 6) / 8)
        }
        return bins[accToBin(enemy)][distanceToBin(distance)].mapIndexed { i, b -> Pair(b, i)  }.sortedBy { it.first.value() }.last().second


//        val bins = bins
//                .mapIndexed { i, b -> Pair(b, i)  }
//                .sortedBy { it.first.value }
//                .takeLast(20)
//        val rand = randBetween(0.0, bins.map { it.first.value }.sum())
//        var sum = 0.0
//        var bin = 0
//        while (bin < nAngles && bins[bin].first.value + sum < rand) {
//            sum += bins[bin].first.value
//            bin++
//        }
//        return bins[bin].second
    }

    private fun randFromArray(ls: List<Pair<Bin, Int>>): Int {
        return ls[randBetween(0, ls.size-1)].second
    }

    private fun bulletPower(distance: Double): Double {
        if(distance<100){
            return 3.0
        }
        return min(3.0, max(1.0, Math.pow((ragnar.state().battleFieldWidth - distance) / (ragnar.state().battleFieldWidth-30.0), 1.0) * 3.0))
    }

    fun calcBuckets(wave: Wave) {
        val hitEnemies = ragnar.botStore.enemies()
                .filter { abs(wave.start.distanceTo(it.position()) - bulletDistance(wave.power, ragnar.state().time - wave.time)) < 10.0 }

        val res = hitEnemies
                .map {enemy ->
                    wave.initial(enemy)?.let {
                        val escapeAngle = getEscapeAngle(enemy, wave.power)
                        val bin = binFromFiringAngle(wave.start.angleToPoint(it.position()), wave.start.angleToPoint(enemy.position()), escapeAngle, wave.start.isMovingClockwise(it))
                        for (i in (1..10)) {
                            if(bin+i in 0..(nAngles - 1))
                                bins[accToBin(it)][distanceToBin(wave.initialDistance(enemy))][bin + i].add(100 / sqrd(i), ragnar.state().time)
                            if(bin-i in 0..(nAngles - 1))
                                bins[accToBin(it)][distanceToBin(wave.initialDistance(enemy))][bin - i].add(100 / sqrd(i), ragnar.state().time)
                        }

//                        val minAngle = min(angleToInitial, angleToInitial - escapeAngle)
//                        val maxAngle = max(angleToInitial, angleToInitial + escapeAngle)
//                        var angleToHit = wave.start.angleToPoint(enemy.position())
//                        while(angleToHit < minAngle) angleToHit += TWO_PI
//                        while(angleToHit > maxAngle) angleToHit -= TWO_PI
//                        if(angleToHit < minAngle){
//                            debugPrintln("escape angle wrong: "+wave.start + " "+wave.power+" "+wave.time+" "+enemy.position())
//                            -1
//                        } else {
//                            val i = ((angleToHit - minAngle) / (abs(maxAngle - minAngle) / wave.nBuckets)).toInt()
//                            if(ragnar.position().isMovingClockwise(enemy)){
//                                i
//                            } else {
//                                wave.nBuckets - i - 1
//                            }
//                        }
                    }
                }.filterNotNull()
        hitEnemies.forEach { enemy ->
            wave.initialEnemyLocations = wave.initialEnemyLocations.filter { it.name() != enemy.name() }
        }
    }


}
fun getEscapeAngle(enemy: Bot, bulletPower: Double): Double {
    return Math.asin(8.0 / (20.0 - 3.0 * bulletPower))
}


fun distanceToBin(dist: Double): Int {
    return restrict(0, 20, (dist/400).toInt())
}
class Wave(val start: Point, val power: Double, val time: Long, val nBuckets: Int, val ragnar: Ragnar) {

    var initialEnemyLocations = ragnar.botStore.enemyLocations()

    fun isActive(): Boolean {
        return corners(ragnar).any { pt -> start.distanceTo(pt) > (ragnar.state().time - time) * bulletSpeed(power) }
    }

    fun initial(enemy: Bot): Bot? {
        return initialEnemyLocations.find { it.name() == enemy.name() }
    }

    fun onPaint(g: Graphics2D) {
        g.color = Color.BLUE
        for(i in (0..100)){
            val pos = start.to(i / TWO_PI, (ragnar.state().time - time) * bulletSpeed(power))
            g.drawOval(pos.x.toInt(), pos.y.toInt(), 5, 5)
        }

        g.drawOval(start.x.toInt(), start.y.toInt(), 20, 20)
        g.drawOval(ragnar.botStore.enemies().first().getX().toInt(), ragnar.botStore.enemies().first().getY().toInt(), 20, 20)
        g.drawLine(ragnar.botStore.enemies().first().getX().toInt(), ragnar.botStore.enemies().first().getY().toInt(), start.x.toInt(), start.y.toInt())
    }

    fun initialDistance(enemy: Bot): Double {
        return initial(enemy)?.let {
            start.distanceTo(enemy)
        } ?: 1000.0

    }
}


class Bin(val ragnar: Ragnar) {
    var values = mutableListOf<Pair<Int, Long>>()

    override fun toString(): String {
        return value().toString()
    }

    fun step(){
        values = values.filter { ragnar.state().time - 100 < it.second }.toMutableList()
    }

    fun value(): Double {
        return values.map { it.first }.sum().toDouble()
    }

    fun add(i: Int, time: Long) {
        values.add(Pair(i, time))
    }
}
