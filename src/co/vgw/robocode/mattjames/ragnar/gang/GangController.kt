package co.vgw.robocode.mattjames.ragnar.gang

import co.vgw.robocode.mattjames.ragnar.Ragnar
import co.vgw.robocode.mattjames.ragnar.entity.Bot
import co.vgw.robocode.mattjames.ragnar.entity.IBot
import co.vgw.robocode.mattjames.ragnar.entity.Point
import co.vgw.robocode.mattjames.ragnar.messages.NewGangMessage
import co.vgw.robocode.mattjames.ragnar.messages.UpdateGangMessage
import co.vgw.robocode.mattjames.ragnar.gun.CircularAiming
import co.vgw.robocode.mattjames.ragnar.util.*
import robocode.MessageEvent
import java.awt.Graphics2D
import java.lang.Double.max

class GangController(val ragnar: Ragnar) {
    var myMark: String? = null
    var myPt: Point? = null
    var teamNumber: Int = 0
    val energyCoefficient = 0.6

    var marksAreAlive = true
    var firstMark = true
    val gangs: MutableMap<Int, Gang> = mutableMapOf()

    fun doIt(skipChooseMark: Boolean = false) {
        clearDead()
        if(!skipChooseMark){
            tryChooseMark()
        }
        move()
        shoot()
        ragnar.radar.scan()
    }

    private fun shoot() {
        ragnar.botStore.closestEnemy()?.let {
            CircularAiming(ragnar, ragnar.position()).target(it)
        }
    }

    private fun move() {
        myPt?.let{currentPt ->
            val pts = getMovePts(currentPt)
            val choice = pts.minBy { it.distanceTo(currentPt) }
            choice?.let {
                moveTowards(choice, ragnar)
            }
        }
    }

    private fun getMoveRadius(): Double {
        myPt?.let {
            return ragnar.position().distanceTo(it)*0.8
        }
        return ragnar.position().distanceTo(ragnar.botStore.closestBot().position())
    }

    fun tryChooseMark() {
        if (shouldChooseNewMark()) {
            chooseMark()
        } else {
            gangs.values.forEach {
                updateGang(it)
            }
        }
    }

    fun chooseMark() {
        val ts = ragnar.botStore.teamWithMeExcludeRogue()
        val bestCombo = findBestCombo(ts)
        val first = firstTeam(bestCombo, ts)
        val second = secondTeam(bestCombo, ts)
        val firstEnemy = bestEnemy(first)

        val firstGang = Gang(first, firstEnemy, 0)
        gangs[0] = firstGang
        val msg = NewGangMessage(firstEnemy.name(), first.map { it.name() }, 0, findBestPts(firstGang))
        ragnar.broadcastMessage(msg)
        onNewGang(msg)


        if(second.isNotEmpty()){
            val secondEnemy = bestEnemy(second, firstEnemy.name())
            val secondGang = Gang(second, secondEnemy, 1)
            gangs[1] = secondGang
            val secondMsg = NewGangMessage(secondEnemy.name(), second.map { it.name() }, 1, findBestPts(secondGang))
            ragnar.broadcastMessage(secondMsg)
            onNewGang(secondMsg)
        }
    }

    private fun updateGang(gang: Gang) {
        findBestPts(gang)
            .let {
                val msg = UpdateGangMessage(gang.mark.name(), gang.team.map { it.name() }, gang.teamNumber, it)
                ragnar.broadcastMessage(msg)
                onUpdateGang(msg)
            }
    }

    private fun findBestPts(gang: Gang): List<Point> {
        val useWallPtOptimiser = true
        val r = gang.team.map { it.distanceTo(gang.mark) }.min()
        var min = Double.POSITIVE_INFINITY
        var bestPts: List<Pair<String, Point>> = listOf()
        if(r!=null){
            val pts = ptsAroundEnemy(gang.mark, r)
            val ptLs = if(useWallPtOptimiser && pts.size in 3..69){
                if(gang.team.size == 3){
                    listOf(
                            listOf(pts[0], pts[pts.size/2 - 1], pts[pts.size-1]),
                            listOf(pts[0], pts[pts.size-1], pts[pts.size/2 - 1]),

                            listOf(pts[pts.size/2 - 1], pts[0], pts[pts.size-1]),
                            listOf(pts[pts.size/2 - 1], pts[pts.size-1], pts[0]),

                            listOf(pts[pts.size-1], pts[0], pts[pts.size/2 - 1]),
                            listOf(pts[pts.size-1], pts[pts.size/2 - 1], pts[0])
                    )
                } else {
                    listOf(
                            listOf(pts[0], pts[pts.size-1]),
                            listOf(pts[pts.size-1], pts[0])
                    )
                }

            } else {
                val slice = pts.size / gang.team.size.toDouble()
                (0 until pts.size)
                        .map {
                            firstPt ->
                            (0 until gang.team.size)
                                    .map { pts[(firstPt + it * slice.toInt())%pts.size] }
                        }
                        .toMutableList()

            }

            var teams = listOf(gang.team)
            if(gang.team.size==3){
                teams = permutations3(gang.team)
            }
            for(team in teams){
                for(currentPts in ptLs){
                    val sum = currentPts
                            .mapIndexed { index, it -> sqrd(team[index].distanceTo(it)) }
                            .sum()
                    if(sum<min){
                        min = sum
                        bestPts = currentPts.mapIndexed { index, pt -> Pair(team[index].name(), pt) }
                    }
                }
            }
        }
        return gang.team.mapNotNull { bot -> bestPts.find { it.first == bot.name() }?.second }
                .map { smoothPoint(it, ragnar) }
    }

    private fun ptsAroundEnemy(mark: Bot, r: Double): List<Point> {
        val pts = (0 until 100)
                .map { mark.estimatedPositionDt(3).to(it * TWO_PI/100.0, max(20.0, r*0.8)) }
                .toMutableList()

        if(pts.all { !it.outsideField(ragnar) }){
            return pts
        }

        var firstInside = pts.indexOfFirst { it.outsideField(ragnar) }
        while(pts[firstInside].outsideField(ragnar)){
            firstInside = (firstInside + 1) % pts.size
        }

        val pts2 = mutableListOf<Point>()
        while(!pts[firstInside].outsideField(ragnar)){
            pts2.add(pts[firstInside])
            firstInside = (firstInside + 1) % pts.size
        }
        return pts2
    }

    private fun findBestCombo(ts: MutableList<IBot>): List<Int> {
        val combos =  combination(ts.size)
        var min = Double.POSITIVE_INFINITY
        var bestCombo = combos.first()
        for(combo in combos) {
            val first = firstTeam(combo, ts)
            val second = secondTeam(combo, ts)
            val firstEnemy = bestEnemy(first)
            var dsum = 0.0

            dsum += first.map { sqrd(it.distanceTo(firstEnemy) + energyCoefficient*firstEnemy.energy()) }
                    .sum()


            if(second.isNotEmpty()){
                val secondEnemy = bestEnemy(second, firstEnemy.name())
                dsum += second.map { sqrd(it.distanceTo(secondEnemy) + energyCoefficient*secondEnemy.energy()) }
                        .sum()
            }

            if(dsum<min){
                min = dsum
                bestCombo = combo.toMutableList()
            }
        }
        return bestCombo
    }

    fun onPaint(g: Graphics2D) {
    }

    private fun getMovePts(to: Point): List<Point> {
        val myPos = ragnar.position()
        val r = myPos.distanceTo(to)/2.5
        val center = myPos.to(myPos.angleToPoint(to), r)
        val closeBots = ragnar.botStore.allBots()
                .map { it.position() }
                .filter { myPos.distanceTo(it)<myPos.distanceTo(to) }
        return (0 until 100)
                .map { center.to(it * TWO_PI/100.0, r) }
                .filter { !it.outsideField(ragnar) }
                .filter { !blockedPath(ragnar.position(), it, closeBots) }
    }

    private fun shouldChooseNewMark(): Boolean {
        if(ragnar.state().time<10L){
            return isLeader()
                    && ragnar.botStore.teamWithMeExcludeRogue().size > 4
                    && ragnar.botStore.enemies.size > 4
        }
        return isLeader()
                && ragnar.botStore.teamWithMeExcludeRogue().size > 1
                && ragnar.botStore.enemies.size > 0
                && (myMark==null || ragnar.state().time % 50==0L || !marksAreAlive)
    }

    private fun secondTeam(bestCombo: List<Int>, ts: MutableList<IBot>): List<IBot> {
        return (0 until ts.size).filter { !bestCombo.contains(it) }.map { ts[it] }
    }

    private fun firstTeam(combo: List<Int>, ts: MutableList<IBot>): List<IBot> {
        return combo.map { ts[it] }
    }

    private fun bestEnemy(team: List<IBot>, ignoreEnemy: String = ""): Bot {
        val ls = ragnar.botStore.enemies()
                .filter { it.name()!=ignoreEnemy }
                .sortedBy { enemy ->
                    team.map { it.distanceTo(enemy) + energyCoefficient*enemy.energy() }.sum()
                }
        return if(ls.isNotEmpty()){
            ls.first()
        } else {
            ragnar.botStore.enemies().first()
        }
    }

    private fun isLeader(): Boolean {
        val ts = ragnar.botStore.team.values
                .filter { !it.isDroid() }
                .filter { !it.isRogueTeamMember() }
                .map { it.name() }
                .toMutableList()
        if(!ragnar.name.contains("roid") && !ragnar.isRogue)
            ts.add(ragnar.name)

        ts.sort()
        return if(ts.size>0){
            val leader = ts[0]
            leader==ragnar.name
        } else {
            false
        }

    }

    fun shouldGang(): Boolean {
        return !ragnar.isRogue && myMark?.let { ragnar.botStore.enemies.containsKey(it) } ?: false && ragnar.botStore.team.size>0
    }

    fun onMessage(event: MessageEvent?) {
        event?.let {
            if(event.message is NewGangMessage){
                onNewGang(event.message as NewGangMessage)
            } else if(event.message is UpdateGangMessage){
                onUpdateGang(event.message as UpdateGangMessage)
            }
        }
    }

    private fun onUpdateGang(msg: UpdateGangMessage) {
        if(msg.teamNumber==teamNumber){
            (0 until msg.teamNames.size)
                    .find { msg.teamNames[it] == ragnar.myName }
                    ?.let { myPt = msg.pts[it] }
        }
    }

    private fun onNewGang(markMsg: NewGangMessage) {
        if(markMsg.teamNames.contains(ragnar.myName)){
            myMark = markMsg.enemyName
            teamNumber = markMsg.teamNumber
            marksAreAlive=true
            doIt(true)
        }
    }

    private fun clearDead() {
        myMark?.let {
            if(!ragnar.botStore.enemies.containsKey(it)){
                myMark = null
                marksAreAlive=false
            }
        }
    }

    fun onEnemyDeath(enemy: Bot) {
        marksAreAlive=false
    }


    fun onTeamDeath(enemy: Bot) {
        marksAreAlive=false
    }

}

class Gang(val team: List<IBot>, val mark: Bot, val teamNumber: Int)