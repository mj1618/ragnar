package co.vgw.robocode.mattjames.ragnar.util

import io.kotlintest.specs.StringSpec
import co.vgw.robocode.mattjames.ragnar.entity.Point
import java.awt.Color
import java.lang.Math.abs
import java.lang.Math.toDegrees
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class RagUtilTest : StringSpec() {
    init {
        "should return green" {
            assertEquals(colour(0.0, 10.0, 1.0), Color.GREEN)
        }

        "should return RED" {
            assertEquals(colour(0.0, 10.0, 10.0), Color.RED)
        }


        "should return BLUE" {
            assertEquals(colour(0.0, 10.0, 3.0), Color.BLUE)
        }

        "should be parallel" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(1.0, 1.0)
            val p3 = Point(0.0, -1.0)
            val p4 = Point(1.0, 0.0)

            assertTrue(abs(toDegrees(angleBetweenLines(p1,p2,p3,p4))) < 0.1, "actual angle: "+toDegrees(angleBetweenLines(p1,p2,p3,p4)))
            assertTrue(perpendicularity(p1,p2,p3,p4) < 0.1, "actual perpendicularity: "+perpendicularity(p1,p2,p3,p4))
        }

        "should be perpendicular" {
            val p1 = Point(0.0, 0.0)
            val p2 = Point(1.0, 1.0)
            val p3 = Point(0.0, 0.0)
            val p4 = Point(1.0, -1.0)

            assertTrue(abs(toDegrees(angleBetweenLines(p1,p2,p3,p4))-90) < 0.1, "actual: "+toDegrees(angleBetweenLines(p1,p2,p3,p4)))
            assertTrue(perpendicularity(p1,p2,p3,p4)-1 < 0.1, "actual perpendicularity: "+perpendicularity(p1,p2,p3,p4))
        }


        "should block path" {
            val pt1 = Point(0.0, 0.0)
            val pt2 = Point(200.0, 200.0)
            val pt3 = Point(100.0, 100.0)
            assertTrue(blockedPath(pt1, pt2, listOf(pt3)))
        }

        "should block path" {
            val pt1 = Point(0.0, 0.0)
            val pt2 = Point(200.0, 200.0)
            val pt3 = Point(110.0, 110.0)
            assertTrue(blockedPath(pt1, pt2, listOf(pt3)))
        }

        "should block path" {
            val pt1 = Point(0.0, 0.0)
            val pt2 = Point(300.0, 300.0)
            val pt3 = Point(150.0, 150.0)
            assertTrue(blockedPath(pt1, pt2, listOf(pt3)))
        }

        "should not block path" {
            val pt1 = Point(0.0, 0.0)
            val pt2 = Point(-100.0, -100.0)
            val pt3 = Point(-100.0, -60.0)
            assertFalse(blockedPath(pt1, pt2, listOf(pt3)))
        }
    }
}
