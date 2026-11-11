package com.ramussoft.local.ai;

import com.dsoft.pb.types.FRectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

/** Finds an orthogonal path around the interiors of the diagram's blocks. */
final class ArrowRouter {
    private ArrowRouter() { }

    static List<Point2D.Double> route(Point2D.Double start, Point2D.Double end,
            List<FRectangle> blocks) {
        return route(start, end, blocks, List.of());
    }

    static List<Point2D.Double> route(Point2D.Double start, Point2D.Double end,
            List<FRectangle> blocks, List<FRectangle> existingLines) {
        TreeSet<Double> xs = new TreeSet<>(), ys = new TreeSet<>();
        xs.add(start.x); xs.add(end.x); ys.add(start.y); ys.add(end.y);
        for (FRectangle block : blocks) {
            xs.add(block.getLeft() - 5); xs.add(block.getRight() + 5);
            ys.add(block.getTop() - 5); ys.add(block.getBottom() + 5);
        }
        // Alternative lanes let later links avoid being drawn on top of earlier ones.
        for (FRectangle line : existingLines) {
            xs.add(line.getLeft() - 8); xs.add(line.getRight() + 8);
            ys.add(line.getTop() - 8); ys.add(line.getBottom() + 8);
        }
        xs.removeIf(v -> v < 5 || v > 795);
        ys.removeIf(v -> v < 5 || v > 439);
        xs.add(start.x); xs.add(end.x); ys.add(start.y); ys.add(end.y);
        List<Double> x = new ArrayList<>(xs), y = new ArrayList<>(ys);
        int width = x.size(), height = y.size(), states = width * height * 3;
        double[] distance = new double[states]; Arrays.fill(distance, Double.POSITIVE_INFINITY);
        int[] previous = new int[states]; Arrays.fill(previous, -1);
        int startIndex = (y.indexOf(start.y) * width + x.indexOf(start.x)) * 3;
        int endNode = y.indexOf(end.y) * width + x.indexOf(end.x);
        PriorityQueue<Node> queue = new PriorityQueue<>();
        distance[startIndex] = 0; queue.add(new Node(startIndex, 0));
        int finish = -1;
        while (!queue.isEmpty()) {
            Node current = queue.remove();
            if (current.cost > distance[current.state]) continue;
            int node = current.state / 3, direction = current.state % 3;
            if (node == endNode) { finish = current.state; break; }
            int col = node % width, row = node / width;
            int[][] steps = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] step : steps) {
                int nextCol = col + step[0], nextRow = row + step[1];
                if (nextCol < 0 || nextCol >= width || nextRow < 0 || nextRow >= height) continue;
                double x1 = x.get(col), y1 = y.get(row), x2 = x.get(nextCol), y2 = y.get(nextRow);
                if (crossesBlock(x1, y1, x2, y2, blocks)) continue;
                int nextDirection = step[0] != 0 ? 1 : 2;
                int next = (nextRow * width + nextCol) * 3 + nextDirection;
                double cost = current.cost + Math.abs(x2 - x1) + Math.abs(y2 - y1)
                        + (direction != 0 && direction != nextDirection ? 18 : 0)
                        + congestion(x1, y1, x2, y2, existingLines);
                if (cost < distance[next]) {
                    distance[next] = cost; previous[next] = current.state; queue.add(new Node(next, cost));
                }
            }
        }
        if (finish < 0) throw new IllegalStateException("Не удалось проложить стрелку между блоками");
        List<Point2D.Double> result = new ArrayList<>();
        for (int state = finish; state >= 0; state = previous[state]) {
            int node = state / 3;
            result.add(new Point2D.Double(x.get(node % width), y.get(node / width)));
        }
        Collections.reverse(result);
        for (int i = result.size() - 2; i > 0; i--) {
            Point2D.Double a = result.get(i - 1), b = result.get(i), c = result.get(i + 1);
            if ((a.x == b.x && b.x == c.x) || (a.y == b.y && b.y == c.y)) result.remove(i);
        }
        return result;
    }

    private static double congestion(double x1, double y1, double x2, double y2, List<FRectangle> lines) {
        double penalty = 0;
        for (FRectangle line : lines) {
            boolean horizontal = line.getWidth() > line.getHeight();
            if (y1 == y2 && horizontal && Math.abs(y1 - line.getTop()) < 3)
                penalty += Math.max(0, Math.min(Math.max(x1, x2), line.getRight()) - Math.max(Math.min(x1, x2), line.getLeft())) * 25;
            else if (x1 == x2 && !horizontal && Math.abs(x1 - line.getLeft()) < 3)
                penalty += Math.max(0, Math.min(Math.max(y1, y2), line.getBottom()) - Math.max(Math.min(y1, y2), line.getTop())) * 25;
            else if (y1 == y2 && !horizontal && line.getLeft() > Math.min(x1, x2) && line.getLeft() < Math.max(x1, x2)
                    && y1 > line.getTop() && y1 < line.getBottom()) penalty += 60;
            else if (x1 == x2 && horizontal && line.getTop() > Math.min(y1, y2) && line.getTop() < Math.max(y1, y2)
                    && x1 > line.getLeft() && x1 < line.getRight()) penalty += 60;
        }
        return penalty;
    }

    static boolean crossesBlock(double x1, double y1, double x2, double y2, List<FRectangle> blocks) {
        for (FRectangle b : blocks) {
            if (x1 == x2 && x1 > b.getLeft() && x1 < b.getRight()
                    && Math.max(y1, y2) > b.getTop() && Math.min(y1, y2) < b.getBottom()) return true;
            if (y1 == y2 && y1 > b.getTop() && y1 < b.getBottom()
                    && Math.max(x1, x2) > b.getLeft() && Math.min(x1, x2) < b.getRight()) return true;
        }
        return false;
    }

    private static final class Node implements Comparable<Node> {
        final int state; final double cost;
        Node(int state, double cost) { this.state = state; this.cost = cost; }
        public int compareTo(Node other) { return Double.compare(cost, other.cost); }
    }
}
