package mynim.is.where.oh.com.whereismynim;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import static android.graphics.Color.BLACK;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Cap;
import static android.graphics.Paint.Cap.BUTT;
import static android.graphics.Paint.Cap.ROUND;
import static android.graphics.Paint.SUBPIXEL_TEXT_FLAG;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.support.v4.widget.DrawerLayout.DrawerListener;
import static java.lang.Math.sqrt;

public class DrawerArrowDrawable extends Drawable {

    private static class JoinedPath {

        private final PathMeasure measureFirst;
        private final PathMeasure measureSecond;
        private final float lengthFirst;
        private final float lengthSecond;

        private JoinedPath(Path pathFirst, Path pathSecond) {
            measureFirst = new PathMeasure(pathFirst, false);
            measureSecond = new PathMeasure(pathSecond, false);
            lengthFirst = measureFirst.getLength();
            lengthSecond = measureSecond.getLength();
        }

        private void getPointOnLine(float parameter, float[] coords) {
            if (parameter <= .5f) {
                parameter *= 2;
                measureFirst.getPosTan(lengthFirst * parameter, coords, null);
            } else {
                parameter -= .5f;
                parameter *= 2;
                measureSecond.getPosTan(lengthSecond * parameter, coords, null);
            }
        }
    }

    private class BridgingLine {

        private final JoinedPath pathA;
        private final JoinedPath pathB;

        private BridgingLine(JoinedPath pathA, JoinedPath pathB) {
            this.pathA = pathA;
            this.pathB = pathB;
        }

        private void draw(Canvas canvas) {
            pathA.getPointOnLine(parameter, coordsA);
            pathB.getPointOnLine(parameter, coordsB);
            if (rounded) insetPointsForRoundCaps();
            canvas.drawLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], linePaint);
        }

        private void insetPointsForRoundCaps() {
            vX = coordsB[0] - coordsA[0];
            vY = coordsB[1] - coordsA[1];

            magnitude = (float) sqrt((vX * vX + vY * vY));
            paramA = (magnitude - halfStrokeWidthPixel) / magnitude;
            paramB = halfStrokeWidthPixel / magnitude;

            coordsA[0] = coordsB[0] - (vX * paramA);
            coordsA[1] = coordsB[1] - (vY * paramA);
            coordsB[0] = coordsB[0] - (vX * paramB);
            coordsB[1] = coordsB[1] - (vY * paramB);
        }
    }

    private final static float PATH_GEN_DENSITY = 3;

    private final static float DIMEN_DP = 23.5f;

    private final static float STROKE_WIDTH_DP = 2;

    private BridgingLine topLine;
    private BridgingLine middleLine;
    private BridgingLine bottomLine;

    private final Rect bounds;
    private final float halfStrokeWidthPixel;
    private final Paint linePaint;
    private final boolean rounded;

    private boolean flip;
    private float parameter;

    // Helper fields during drawing calculations.
    private float vX, vY, magnitude, paramA, paramB;
    private final float coordsA[] = { 0f, 0f };
    private final float coordsB[] = { 0f, 0f };

    public DrawerArrowDrawable(Resources resources) {
        this(resources, false);
    }

    public DrawerArrowDrawable(Resources resources, boolean rounded) {
        this.rounded = rounded;
        float density = resources.getDisplayMetrics().density;
        float strokeWidthPixel = STROKE_WIDTH_DP * density;
        halfStrokeWidthPixel = strokeWidthPixel / 2;

        linePaint = new Paint(SUBPIXEL_TEXT_FLAG | ANTI_ALIAS_FLAG);
        linePaint.setStrokeCap(rounded ? ROUND : BUTT);
        linePaint.setColor(BLACK);
        linePaint.setStyle(STROKE);
        linePaint.setStrokeWidth(strokeWidthPixel);

        int dimen = (int) (DIMEN_DP * density);
        bounds = new Rect(0, 0, dimen, dimen);

        Path first, second;
        JoinedPath joinedA, joinedB;

        // Top
        first = new Path();
        first.moveTo(5.042f, 20f);
        first.rCubicTo(8.125f, -16.317f, 39.753f, -27.851f, 55.49f, -2.765f);
        second = new Path();
        second.moveTo(60.531f, 17.235f);
        second.rCubicTo(11.301f, 18.015f, -3.699f, 46.083f, -23.725f, 43.456f);
        scalePath(first, density);
        scalePath(second, density);
        joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 20f);
        first.rCubicTo(4.457f, 16.75f, 1.512f, 37.982f, -22.557f, 42.699f);
        second = new Path();
        second.moveTo(42.402f, 62.699f);
        second.cubicTo(18.333f, 67.418f, 8.807f, 45.646f, 8.807f, 32.823f);
        scalePath(first, density);
        scalePath(second, density);
        joinedB = new JoinedPath(first, second);
        topLine = new BridgingLine(joinedA, joinedB);

        // Middle
        first = new Path();
        first.moveTo(5.042f, 35f);
        first.cubicTo(5.042f, 20.333f, 18.625f, 6.791f, 35f, 6.791f);
        second = new Path();
        second.moveTo(35f, 6.791f);
        second.rCubicTo(16.083f, 0f, 26.853f, 16.702f, 26.853f, 28.209f);
        scalePath(first, density);
        scalePath(second, density);
        joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 35f);
        first.rCubicTo(0f, 10.926f, -8.709f, 26.416f, -29.958f, 26.416f);
        second = new Path();
        second.moveTo(35f, 61.416f);
        second.rCubicTo(-7.5f, 0f, -23.946f, -8.211f, -23.946f, -26.416f);
        scalePath(first, density);
        scalePath(second, density);
        joinedB = new JoinedPath(first, second);
        middleLine = new BridgingLine(joinedA, joinedB);

        // Bottom
        first = new Path();
        first.moveTo(5.042f, 50f);
        first.cubicTo(2.5f, 43.312f, 0.013f, 26.546f, 9.475f, 17.346f);
        second = new Path();
        second.moveTo(9.475f, 17.346f);
        second.rCubicTo(9.462f, -9.2f, 24.188f, -10.353f, 27.326f, -8.245f);
        scalePath(first, density);
        scalePath(second, density);
        joinedA = new JoinedPath(first, second);

        first = new Path();
        first.moveTo(64.959f, 50f);
        first.rCubicTo(-7.021f, 10.08f, -20.584f, 19.699f, -37.361f, 12.74f);
        second = new Path();
        second.moveTo(27.598f, 62.699f);
        second.rCubicTo(-15.723f, -6.521f, -18.8f, -23.543f, -18.8f, -25.642f);
        scalePath(first, density);
        scalePath(second, density);
        joinedB = new JoinedPath(first, second);
        bottomLine = new BridgingLine(joinedA, joinedB);
    }

    @Override public int getIntrinsicHeight() {
        return bounds.height();
    }

    @Override public int getIntrinsicWidth() {
        return bounds.width();
    }

    @Override public void draw(Canvas canvas) {
        if (flip) {
            canvas.save();
            canvas.scale(1f, -1f, getIntrinsicWidth() / 2, getIntrinsicHeight() / 2);
        }

        topLine.draw(canvas);
        middleLine.draw(canvas);
        bottomLine.draw(canvas);

        if (flip) canvas.restore();
    }

    @Override public void setAlpha(int alpha) {
        linePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override public void setColorFilter(ColorFilter cf) {
        linePaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override public int getOpacity() {
        return TRANSLUCENT;
    }

    public void setStrokeColor(int color) {
        linePaint.setColor(color);
        invalidateSelf();
    }

    public void setParameter(float parameter) {
        if (parameter > 1 || parameter < 0) {
            throw new IllegalArgumentException("Value must be between 1 and zero inclusive!");
        }
        this.parameter = parameter;
        invalidateSelf();
    }

    public void setFlip(boolean flip) {
        this.flip = flip;
        invalidateSelf();
    }

    private static void scalePath(Path path, float density) {
        if (density == PATH_GEN_DENSITY) return;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(density / PATH_GEN_DENSITY, density / PATH_GEN_DENSITY, 0, 0);
        path.transform(scaleMatrix);
    }
}