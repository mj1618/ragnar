package co.vgw.robocode.mattjames.ragnar

import co.vgw.robocode.mattjames.ragnar.entity.*
import co.vgw.robocode.mattjames.ragnar.gang.GangController
import co.vgw.robocode.mattjames.ragnar.messages.ScannedRobotMessage
import co.vgw.robocode.mattjames.ragnar.gun.GunController
import co.vgw.robocode.mattjames.ragnar.messages.GoneRogueMessage
import co.vgw.robocode.mattjames.ragnar.mover.RamMover
import co.vgw.robocode.mattjames.ragnar.radar.Radar
import robocode.*
import java.awt.Graphics2D
import java.awt.Color

var shouldDebug: Boolean = false

fun debugPrintln(s: Any){
    if(shouldDebug){
        println(s)
    }
}

open class Ragnar : TeamRobot() {
    val botStore = BotStore(this)
    val mover: Mover = RamMover(this)
    val gun = GunController(this)
    val radar = Radar(this)
    val states = ArrayList<State>()
    var generatedState = false
    var myIndex = 0
    var myName = ""
    var isRogue = false

    val gang: GangController = GangController(this)

    override fun run() {
        myName = name
        if(name.indexOf("(")>-1)
            myIndex = name.substring(name.indexOf("(")+1, name.indexOf(")")).toInt()
        else
            myIndex=1

        isAdjustGunForRobotTurn = true
        isAdjustRadarForGunTurn = true
        isAdjustRadarForRobotTurn = true
        addCustomEvent(GunTurnCompleteCondition(this))

        setColors(Color.GREEN, Color.WHITE, Color.RED)

        while (true) {

            debugPrintln("t: "+state().time)

            debugPrintln("choosing mark")
            gang.tryChooseMark()

            mover.step()

            if(!gang.shouldGang()){
                debugPrintln("min risk mode")
                minRiskMode()
            } else {
                debugPrintln("gang mode")
                gangMode()
            }

            if(state().time%1==0L){
                broadcastMessage(ScannedRobotMessage(ScannedRobotEventWrapper(this)))
            }

            debugPrintln("execute")
            generatedState = false
            execute()
        }
    }

    private fun gangMode() {
        gang.doIt()
    }

    private fun isMinRiskMode(): Boolean {
        return gang.myMark == null
    }

    fun minRiskMode() {
        val p = mover.move()
        gun.blast(position())
        radar.scan()
    }

    override fun onCustomEvent(event: CustomEvent?) {
        if(event is GunTurnCompleteCondition){
            gun.onGunTurnCompletion()
        }
    }

    val allHitRobotEvents: MutableList<HitRobotEvent> = mutableListOf()

    override fun onHitRobot(event: HitRobotEvent?) {
        event?.let {
            mover.onHitRobot(event)
            allHitRobotEvents.add(event)
        }
    }
    override fun onMessageReceived(event: MessageEvent?) {
        event?.let {
            if(event.message is MoveLocation){
                botStore.addTeamLocation(event.sender, event.message as MoveLocation)
            } else if(event.message is ScannedRobotMessage){
                val wrapper = (event.message as ScannedRobotMessage).event
                onScannedRobot(wrapper, true)
            } else if(event.message is GoneRogueMessage){
                botStore.goneRogue((event.message as GoneRogueMessage).name)
                gang.tryChooseMark()
            }
            gang.onMessage(event)
        }
    }

    override fun onScannedRobot(e: ScannedRobotEvent) {
        val wrapper = ScannedRobotEventWrapper(this, e)
        onScannedRobot(wrapper)
    }

    private fun onScannedRobot(e: ScannedRobotEventWrapper, fromMessage:Boolean = false) {
        if(isTeammate(e.name)){
            botStore.getOrCreateBot(e, false)
        } else {
            botStore.getOrCreateBot(e, true)?.let {
                if(it.didShoot()){
                    debugPrintln("did shoot!")
                    mover.onBulletShot()
                }
            }
        }

        if(!fromMessage){
            broadcastMessage(ScannedRobotMessage(e))
        }
    }

    override fun onPaint(g: Graphics2D){
        if(shouldDebug){
            mover.onPaint(g)
            gang.onPaint(g)
        }
    }

    override fun onRobotDeath(event: RobotDeathEvent?) {
        event?.name?.let {
            botStore.getByName(it, true)
                    ?.let { gang.onEnemyDeath(it) }

            botStore.getByName(it, false)
                    ?.let { gang.onTeamDeath(it) }

            botStore.remove(it)
        }
    }

    fun position(): Point {
        return Point(state().x, state().y)
    }

    fun state(): State {
        if(!generatedState){
            val state = State(
                    x,
                    y,
                    heading,
                    headingRadians,
                    radarHeading,
                    radarHeadingRadians,
                    gunHeading,
                    gunHeadingRadians,
                    time,
                    battleFieldWidth,
                    battleFieldHeight,
                    gunHeat,
                    energy,
                    velocity
            )
            states.add(state)
            generatedState = true
            return state
        } else {
            return states[states.size-1]
        }
    }

    fun distance(enemy: Bot): Double {
        return position().distanceTo(enemy.point())
    }

}

class State(val x: Double,
            val y: Double,
            val heading: Double,
            val headingRadians: Double,
            val radarHeading: Double,
            val radarHeadingRadians: Double,
            val gunHeading: Double,
            val gunHeadingRadians: Double,
            val time: Long,
            val battleFieldWidth: Double,
            val battleFieldHeight: Double,
            val gunHeat: Double,
            val energy: Double,
            val velocity: Double) {
    fun position(): Point {
        return Point(x,y)
    }


}