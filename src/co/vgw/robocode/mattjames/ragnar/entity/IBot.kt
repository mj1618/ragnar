package co.vgw.robocode.mattjames.ragnar.entity

interface IBot {

    fun distanceTo(pt: Point): Double {
        return point().distanceTo(pt)
    }

    fun name(): String
    fun point(): Point
    fun distanceTo(bot: Bot): Double {
        return point().distanceTo(bot.position())
    }

    fun isRogueTeamMember(): Boolean
}