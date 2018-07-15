package co.vgw.robocode.mattjames.ragnar.entity

import co.vgw.robocode.mattjames.ragnar.Ragnar
import java.util.*
import kotlin.comparisons.compareBy

class BotStore(val ragnar: Ragnar) {
    val enemies = HashMap<String, Bot>()
    val team = HashMap<String, Bot>()

    fun getOrCreateBot(e: ScannedRobotEventWrapper, isEnemy: Boolean): Bot? {
        return when {
            e.name == ragnar.myName -> null
            containsKey(e.name, isEnemy) -> {
                val bot = getByName(e.name, isEnemy)
                bot?.add(e)
                bot
            }
            else -> {
                val bot = Bot(ragnar, e, isEnemy)
                add(bot)
                bot
            }
        }
    }

    fun getByName(name: String?, enemy: Boolean): Bot? {
        return if (enemy) {
            enemies[name]
        } else {
            team[name]
        }
    }

    private fun containsKey(name: String?, enemy: Boolean): Boolean {
        return if (enemy) {
            enemies.containsKey(name)
        } else {
            team.containsKey(name)
        }
    }

    fun stalest(): Bot {
        return allBots().sortedWith(compareBy(Bot::lastSeenTime, Bot::name))[0]
    }

    fun allBots(): List<Bot> {
        val ls = ArrayList<Bot>()
        ls.addAll(enemies.values)
        ls.addAll(team.values)
        return ls
    }

    fun remove(name: String) {
        enemies.remove(name)
        team.remove(name)
    }

    fun add(bot: Bot) {
        if(bot.isEnemy){
            enemies[bot.name()] = bot
        } else {
            team[bot.name()] = bot
        }
    }

    fun closestEnemy(): Bot? {
        return if(enemies().isNotEmpty()){
            closestEnemies().first()
        } else null
    }

    fun bestEnemy(): Bot? {
        return if(enemies().isNotEmpty()){
            closestEnemies().first()
        } else null
    }

    fun closestEnemies(): List<Bot> {
        return enemies().sortedWith(compareBy(Bot::distance, Bot::name))
    }

    fun enemies(): List<Bot> {
        return enemies.values.toList()
    }


    fun team(): List<Bot> {
        return team.values.toList()
    }

    fun addTeamLocation(sender: String?, moveLocation: MoveLocation) {
        sender?.let {
            getByName(it, false)
        }?.let { it.addLocationEvent(moveLocation) }
    }

    fun orderedEnemies(): List<Bot> {
        return enemies().sortedBy { it.name() }
    }

    fun weakestEnemy(): Bot? {
        if(enemies.size>0)
            return enemies().sortedBy { it.energy() }[0]
        else return null
    }

    fun teamWithMe(): MutableList<IBot> {
        val ts: MutableList<IBot> = team().toMutableList()
        ts.add(BotSelf(ragnar))
        return ts
    }

    fun teamWithMeExcludeRogue(): MutableList<IBot> {
        val ts: MutableList<IBot> = team().toMutableList()
        ts.add(BotSelf(ragnar))
        return ts.filter { !it.isRogueTeamMember() }.toMutableList()
    }

    fun closestBot(): Bot {
        return enemies().plus(team()).sortedWith(compareBy(Bot::distance, Bot::name)).first()
    }

    fun goneRogue(name: String) {
        val t = team[name]
        t?.let {
            it.isRogue = true
        }
    }

    fun enemyLocations(): List<Bot> {
        return enemies().map { Bot(ragnar, it.latestEvent(), true) }.toList()
    }

}
