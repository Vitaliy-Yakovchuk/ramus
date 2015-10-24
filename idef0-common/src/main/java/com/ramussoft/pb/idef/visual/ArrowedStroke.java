package com.ramussoft.pb.idef.visual;

import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

public class ArrowedStroke implements Stroke {

    private float arrowWidth;
    private float arrowHeight;
    private int type;

    public ArrowedStroke(float arrowWidth, float arrowHeight) {
        this.arrowWidth = arrowWidth;
        this.arrowHeight = arrowHeight;
    }

    protected Shape createArrow(float fx, float fy, float tx, float ty) {
        float dx = tx - fx;
        float dy = ty - fy;
        float D = (float) Math.sqrt(dx * dx + dy * dy);
        float z = (dx <= 0) ? fx - D : fx + D;
        double alpha = (dx > 0) ? Math.asin(dy / D) : -Math.asin(dy / D);
        float h = arrowHeight;

        int n = (int) (D / h);
        h = D / (float) (n + 1);

        float dec = (dx <= 0) ? h : -h;

        GeneralPath gp = new GeneralPath();
        for (int i = 0; i <= n; i++) {
            gp.moveTo(z + dec, fy - arrowWidth);
            gp.lineTo(z, fy);
            gp.lineTo(z + dec, fy + arrowWidth);
            z += dec;
        }
        gp.closePath();
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(alpha, fx, fy);
        return gp.createTransformedShape(affineTransform);
    }

    public Shape createStrokedShape(Shape shape) {
        // We are flattening the path iterator to only get line segments.
        PathIterator path = shape.getPathIterator(null, 1);
        float points[] = new float[6];
        GeneralPath strokepath = new GeneralPath();
        float ix = 0, iy = 0;
        float px = 0, py = 0;

        while (!path.isDone()) {
            int type = path.currentSegment(points);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    ix = px = points[0];
                    iy = py = points[1];
                    strokepath.moveTo(ix, iy);
                    break;
                case PathIterator.SEG_LINETO:
                    strokepath.append(createArrow(px, py, points[0], points[1]),
                            false);
                    px = points[0];
                    py = points[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    if (px != ix && py != ix)
                        strokepath.append(createArrow(px, py, ix, iy), false);
                    break;
                default:
                    strokepath.append(createArrow(px, py, points[0], points[1]),
                            false);
                    px = points[0];
                    py = points[1];
                    // never appear.
            }
            path.next();
        }
        return strokepath;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setArrowHeight(float arrowHeight) {
        this.arrowHeight = arrowHeight;
    }

    public void setArrowWidth(float arrowWidth) {
        this.arrowWidth = arrowWidth;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + type;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ArrowedStroke))
            return false;
        ArrowedStroke other = (ArrowedStroke) obj;
        if (type != other.type)
            return false;
        return true;
    }
}
