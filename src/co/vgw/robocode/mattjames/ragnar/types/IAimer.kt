package co.vgw.robocode.mattjames.ragnar.types

import co.vgw.robocode.mattjames.ragnar.entity.Bot

interface IAimer {
    fun target(mark: Bot): Boolean
}