package co.vgw.robocode.mattjames.ragnar.entity

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.State
import co.vgw.robocode.mattjames.ragnar.util.TWO_PI
import co.vgw.robocode.mattjames.ragnar.util.approx
import co.vgw.robocode.mattjames.ragnar.util.smoothAcceleration
import java.util.*


open class Bot(val ragnar: Ragnar, e: ScannedRobotEventWrapper, val isEnemy: Boolean): IBot {
    val events = ArrayList<Pair<State, ScannedRobotEventWrapper>>()
    val moveLocations = ArrayList<MoveLocation>()
    var isRogue = false

    override fun isRogueTeamMember(): Boolean {
        return isRogue
    }

    init {
        events.add(Pair(ragnar.state(), e))
    }

    fun add(e: ScannedRobotEventWrapper){
        val last = latest()
        if(last.second.time<e.time)
            events.add(Pair(ragnar.state(), e))
    }

    fun distance(): Double {
        return ragnar.distance(this)
    }

    fun lastSeenTime(): Long {
        return latestEvent().time
    }

    fun latestEvent(): ScannedRobotEventWrapper {
        return events.last().second
    }

    fun secondLatestEvent(): ScannedRobotEventWrapper {
        return events[events.size-2].second
    }

    override fun name(): String {
        return latestEvent().name
    }

    fun angleRadians(): Double {
        return ragnar.position().angleToPoint(position())
    }

    override fun point(): Point {
        return Point(getX(), getY())
    }

    fun getY(): Double {
//        val angle = (latest().first.headingRadians + latest().second.bearingRadians) % TWO_PI
//        return (latest().first.y + Math.cos(angle) * latest().second.distance)
        return latest().second.y
    }

    private fun latest(): Pair<State, ScannedRobotEventWrapper> {
        return events[events.size-1]
    }

    fun getX(): Double {
//        val angle = (latest().first.headingRadians + latest().second.bearingRadians) % TWO_PI
//        return (latest().first.x + Math.sin(angle) * latest().second.distance)
        return latest().second.x
    }

    fun headingRadians(): Double {
        return latestEvent().headingRadians
    }

    fun velocity(): Double {
        return latestEvent().velocity
    }

    fun position(): Point {
        return Point(getX(), getY())
    }

//    fun estimatedPositionAt(t: Long): Point {
//        return if(moveLocations.size>0){
//            val ml=moveLocations[moveLocations.size-1]
////            ml.current.move(velocity(), ml.current.angleToPoint(ml.to), t + (ragnar.state().time - ml.time))
//            ml.current
//        } else {
//            position().move(velocity(), headingRadians(), t + (ragnar.state().time - lastSeenTime()))
//        }
//    }

    private fun lastNonZeroVelocity(): Double {
        return events.reversed()
                .firstOrNull { !approx(it.second.velocity, 0) }
                ?.let { it.second.velocity }
                ?: 0.0
    }

    fun addLocationEvent(moveLocation: MoveLocation) {
        moveLocations.add(moveLocation)
    }


    fun choiceDistanceTo(pt: Point): Double {
        return if(moveLocations.size > 0){
            return moveLocations[moveLocations.size-1].to.distanceTo(pt)
        } else {
            return Double.POSITIVE_INFINITY
        }
    }

    fun energy(): Double {
        return latestEvent().energy
    }

    fun estimatedPositionAt(time: Long): Point {
        return wallSmooth(point().move(velocity(), headingRadians(), time - lastSeenTime()), 20.0)
    }

    fun estimatedPositionDt(dt: Long): Point {
        val realDt = dt + (ragnar.state().time - lastSeenTime())
        return wallSmooth(point().move(velocity(), headingRadians(), realDt, smoothAcceleration(acceleration())), 20.0)
    }

    fun acceleration(): Double {
        return 0.0
    }

    fun estimatedPositionCircular(lastEstimatedPosition: Point, increment: Long): Point {
        return wallSmooth(lastEstimatedPosition.move(velocity(), headingRadians() + headingChangePerSecond() * increment, increment), 20.0)
    }

    private fun wallSmooth(p: Point, smooth: Double): Point {
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

    private fun headingChangePerSecond(): Double {
        return if (events.size <= 1){
            0.0
        } else {
            (latestEvent().headingRadians - secondLatestEvent().headingRadians) / (latestEvent().time - secondLatestEvent().time)
        }
    }

    fun isDroid(): Boolean {
        return latest().second.name.contains("roid")
    }

    fun didShoot(): Boolean {
        return shotEnergy() in (0.1 .. 3.0)
    }

    fun shotEnergy(): Double {
        return if(events.size>1)
            return secondLatestEvent().energy - latestEvent().energy
        else
            0.0
    }

}