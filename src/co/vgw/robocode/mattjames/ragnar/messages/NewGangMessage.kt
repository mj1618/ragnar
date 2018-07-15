package co.vgw.robocode.mattjames.ragnar.messages

import co.vgw.robocode.mattjames.ragnar.entity.Point
import java.io.Serializable

class NewGangMessage(val enemyName: String, val teamNames: List<String>, val teamNumber: Int, val pts: List<Point>): Serializable {

}
