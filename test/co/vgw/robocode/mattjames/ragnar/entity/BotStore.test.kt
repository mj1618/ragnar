package co.vgw.robocode.mattjames.ragnar.entity

import io.kotlintest.specs.StringSpec
import co.vgw.robocode.mattjames.ragnar.Ragnar
import robocode.ScannedRobotEvent
import kotlin.test.assertEquals


class BotStoreTest : StringSpec() {
    init {
        "should create enemy" {
            val r = Ragnar()
            val enemyStore = BotStore(r)
            enemyStore.getOrCreateBot(ScannedRobotEventWrapper(r, ScannedRobotEvent("asdf", 0.0,0.0,0.0,0.0,0.0)), true)
            assertEquals(enemyStore.enemies.size, 1)
        }

        "should return latest" {
            val r = Ragnar()
            val enemyStore = BotStore(r)
            val e1 = ScannedRobotEventWrapper(r, ScannedRobotEvent("e1", 0.0,0.0,0.0,0.0,0.0))
            e1.time = 100
            enemyStore.getOrCreateBot(e1, true)
            val e2 = ScannedRobotEventWrapper(r, ScannedRobotEvent("e2", 0.0,0.0,0.0,0.0,0.0))
            e2.time = 50
            enemyStore.getOrCreateBot(e2, true)
            assertEquals(enemyStore.stalest().name(), "e2")
        }
    }
}


