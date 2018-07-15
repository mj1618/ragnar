package co.vgw.robocode.mattjames.ragnar

import robocode.BattleResults
import robocode.control.RobocodeEngine
import robocode.control.events.BattleAdaptor
import robocode.control.events.BattleCompletedEvent
import robocode.control.events.BattleErrorEvent
import java.io.File
import robocode.control.BattlefieldSpecification
import robocode.control.BattleSpecification

val debug = true
val Rags="mj.Ragnar 1.5"
val Rags1Droid="mj.Ragnar1Droid"
val RagsDroid="mj.RagnarDroid"
val Cors="kawigi.mini.Coriantumr"
val Shiz="kawigi.micro.Shiz"
val TShiz = "kawigi.micro.ArmyOfShiz 1.1"
val TCombat = "mn.CombatTeam*"
val TRadnor = "radnor.RadnorMedSchool 1.0"
val TShadow = "abc.ShadowTeam 3.83"

fun T(x: String): String {
    if(x==Rags){
        return "co.vgw.robocode.mattjames.ragnar.RagnarTeam"
    }
    return x+"Team*"
}

val players = listOf(T(Rags), T(Cors)).joinToString(",")
//val players = listOf(T(RagsDroid), T(Cors)).joinToString(",")
//val players = listOf(T(Rags), TCombat).joinToString(",")
//val players = listOf(T(Rags), T(Shiz)).joinToString(",")
//val players = listOf(T(Rags1Droid), TShiz).joinToString(",")
//val players = listOf(T(Rags), TShadow).joinToString(",")
//val players = listOf(T(Rags), TRadnor).joinToString(",")

fun main(args: Array<String>) {
    val engine = RobocodeEngine(File("/Users/matthewjames/robocode"))
    val battleObserver = BattleObserver()
    engine.addBattleListener(battleObserver)
    val battlefield = BattlefieldSpecification(1000, 1000)
    val selectedBots = engine.getLocalRepository(players)
    val battleSpec = BattleSpecification((if (debug) 500 else 5), battlefield, selectedBots)
    if(debug){
        engine.setVisible(true)
        engine.runBattle(battleSpec, true)
        val results = battleObserver.getResults()
        for( r in results ){
            debugPrintln(r.teamLeaderName + ", Score: " + r.score + ", Survivor: " + r.survival)
        }
    } else {
        for(i in 0..100){
            engine.runBattle(battleSpec, true)
            val results = battleObserver.getResults()
            val totalScores = results.map { it.score }.sum()
            val winner = results.sortedWith(compareBy(BattleResults::getScore)).last()
            val loser = results.sortedWith(compareBy(BattleResults::getScore)).first()
            debugPrintln("winner: "+winner.teamLeaderName+" "+(100.0 * winner.score.toDouble()/totalScores).toInt()+"% survivor: "+(if (winner.lastSurvivorBonus>0) winner.teamLeaderName else loser.teamLeaderName))

        }
    }
}

internal class BattleObserver : BattleAdaptor() {

    internal var results: Array<BattleResults>? = null

    override fun onBattleCompleted(e: BattleCompletedEvent?) {
        results = e!!.indexedResults
    }

    override fun onBattleError(e: BattleErrorEvent?) {
        debugPrintln("Error running battle: " + e!!.error)
    }

    fun getResults(): Array<BattleResults> {
        return results!!
    }

}