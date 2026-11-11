package com.ramussoft.local.ai;

import com.dsoft.pb.types.FRectangle;
import java.awt.geom.Point2D;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ArrowRouterTest {
    @Test public void givesParallelArrowsSeparateLanes() {
        var route = ArrowRouter.route(new Point2D.Double(20, 70), new Point2D.Double(200, 70), List.of(),
                List.of(new FRectangle(20, 70, 180, 0.1)));
        assertTrue("Congested horizontal path must detour", route.stream().anyMatch(p -> p.y != 70));
    }
    @Test public void avoidsBlockBetweenAlignedEndpoints() {
        List<FRectangle> blocks = List.of(new FRectangle(80, 30, 60, 80));
        List<Point2D.Double> route = ArrowRouter.route(new Point2D.Double(20, 70),
                new Point2D.Double(200, 70), blocks);
        assertTrue("The path must bend around the block", route.size() > 2);
        for (int i = 1; i < route.size(); i++) {
            Point2D.Double a = route.get(i - 1), b = route.get(i);
            assertTrue(a.x == b.x || a.y == b.y);
            assertFalse(ArrowRouter.crossesBlock(a.x, a.y, b.x, b.y, blocks));
        }
    }

    @Test public void feedbackCanReturnAroundMultipleBlocks() {
        List<FRectangle> blocks = List.of(new FRectangle(60, 50, 80, 60),
                new FRectangle(180, 50, 80, 60), new FRectangle(300, 50, 80, 60));
        Point2D.Double start = new Point2D.Double(388, 80), end = new Point2D.Double(100, 42);
        List<Point2D.Double> route = ArrowRouter.route(start, end, blocks);
        assertEquals(start, route.get(0));
        assertEquals(end, route.get(route.size() - 1));
        for (int i = 1; i < route.size(); i++) {
            Point2D.Double a = route.get(i - 1), b = route.get(i);
            assertFalse(ArrowRouter.crossesBlock(a.x, a.y, b.x, b.y, blocks));
        }
    }
}
