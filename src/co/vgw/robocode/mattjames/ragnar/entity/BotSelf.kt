package co.vgw.robocode.mattjames.ragnar.entity

import co.vgw.robocode.mattjames.ragnar.Ragnar

class BotSelf(val ragnar: Ragnar): IBot {
    var isRogue = false

    override fun isRogueTeamMember(): Boolean {
        return isRogue
    }

    override fun name(): String {
        return ragnar.myName
    }

    override fun point(): Point {
        return Point(ragnar.state().x, ragnar.state().y)
    }

}