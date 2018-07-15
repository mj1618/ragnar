package co.vgw.robocode.mattjames.ragnar.entity

import io.kotlintest.specs.StringSpec
import co.vgw.robocode.mattjames.ragnar.debugPrintln
import co.vgw.robocode.mattjames.ragnar.util.HALF_PI
import java.lang.Math.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class PointTest : StringSpec() {
    init {
        "should give 0 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(0.0, 1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) - 0) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should give 45 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(1.0, 1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) - 45) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should give -45 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(-1.0, 1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) + 45) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should give 180 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(0.0, -1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) - 180) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should give 135 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(1.0, -1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) - 135) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should give -135 angle" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(-1.0, -1.0)
            assertTrue(abs(toDegrees(p1.angleToPoint(p2)) + 135) < 0.1, "actual: "+toDegrees(p1.angleToPoint(p2)))
        }

        "should move right" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(HALF_PI, 1.0)
            assertTrue(p2.x.toInt() == 1, "actual: "+p2)
            assertTrue(p2.y.toInt() == 0, "actual: "+p2)
        }

        "should move left" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(1.5 * PI, 1.0)
            assertTrue(p2.x.toInt() == -1, "actual: "+p2)
            assertTrue(p2.y.toInt() == 0, "actual: "+p2)
        }

        "should move left" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(-HALF_PI, 1.0)
            assertTrue(p2.x.toInt() == -1, "actual: "+p2)
            assertTrue(p2.y.toInt() == 0, "actual: "+p2)
        }

        "should move down" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(PI, 1.0)
            assertTrue(p2.x.toInt() == 0, "actual: "+p2)
            assertTrue(p2.y.toInt() == -1, "actual: "+p2)
        }

        "should move up" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(0.0, 1.0)
            assertTrue(p2.x.toInt() == 0, "actual: "+p2)
            assertTrue(p2.y.toInt() == 1, "actual: "+p2)
        }


        "should give distance 1" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.to(0.0, 1.0)
            assertEquals(p1.distanceTo(p2), 1.0)
        }

        "should move 1 tick up" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, 0.0, 1)
            assertTrue(abs(p2.x) < 0.1)
            assertTrue(abs(p2.y-1.0) < 0.1)
        }

        "should move 1 tick right" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, HALF_PI, 1)
            assertTrue(abs(p2.x-1.0) < 0.1)
            assertTrue(abs(p2.y) < 0.1)
        }

        "should move 1 tick down" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, PI, 1)
            assertTrue(abs(p2.x) < 0.1, p2.toString())
            assertTrue(abs(p2.y+1.0) < 0.1, p2.toString())
        }

        "should move 1 tick left" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, 1.5 * PI, 1)
            assertTrue(abs(p2.x+1.0) < 0.1, p2.toString())
            assertTrue(abs(p2.y) < 0.1, p2.toString())
        }

        "should move 1 tick up and right" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, PI/4.0, 1)
            assertTrue(p2.x > 0.5, p2.toString())
            assertTrue(p2.y > 0.5, p2.toString())
            assertTrue(p2.x < 1.0, p2.toString())
            assertTrue(p2.y < 1.0, p2.toString())
            debugPrintln(p2)
        }

        "should move 1 tick down and left" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(1.0, 1.25*PI, 1)
            assertTrue(p2.x < -0.5, p2.toString())
            assertTrue(p2.y < -0.5, p2.toString())
            assertTrue(p2.x > -1.0, p2.toString())
            assertTrue(p2.y > -1.0, p2.toString())
        }

        "should move 2 ticks left" {
            val p1 = Point(0.0, 0.0)
            val p2 = p1.move(2.0, 1.5 * PI, 1)
            assertTrue(abs(p2.x+2.0) < 0.1, p2.toString())
            assertTrue(abs(p2.y) < 0.1, p2.toString())
        }
    }
}


