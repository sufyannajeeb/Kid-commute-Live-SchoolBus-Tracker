package com.example.kidcommute;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The route as a vertical timeline: one dot per area (newest on top), a grey
 * line for the whole route and a blue line for the part the bus has already
 * travelled, with the bus riding down and waiting at every stop.
 */
public class RouteLineView extends View {

    private static final long MS_PER_SEGMENT = 1400;   // travel + pause per stop
    private static final float HOLD = 0.8f;            // extra pause at the end

    private final List<String> names = new ArrayList<>();
    private final List<String> times = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();   // names, ellipsized to the width
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint busPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SimpleDateFormat clock = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private final float density;
    private final float rowH, lineX;
    private ValueAnimator animator;
    private float t = 0f;                              // 0 .. n-1 (+HOLD)
    private int labelsWidth = -1;

    public RouteLineView(Context c) {
        this(c, null);
    }

    public RouteLineView(Context c, AttributeSet a) {
        super(c, a);
        density = c.getResources().getDisplayMetrics().density;
        rowH = 56 * density;
        lineX = 28 * density;

        linePaint.setColor(0xFFCFD8DC);
        linePaint.setStrokeWidth(4 * density);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        trailPaint.setColor(0xFF1E88E5);               // part already travelled
        trailPaint.setStrokeWidth(4 * density);
        trailPaint.setStrokeCap(Paint.Cap.ROUND);

        namePaint.setColor(0xFF212121);
        namePaint.setTextSize(16 * density);
        timePaint.setColor(0xFF757575);
        timePaint.setTextSize(12 * density);

        busPaint.setTextSize(24 * density);
        busPaint.setTextAlign(Paint.Align.CENTER);
    }

    /** names/times are newest first, same as your shownNames / shownTimes. */
    public void setStops(List<String> newNames, List<Long> newTimes) {
        names.clear();
        times.clear();
        names.addAll(newNames);
        for (Long ms : newTimes) {
            times.add(clock.format(new Date(ms)));
        }
        labelsWidth = -1;                             // relayout the ellipsized labels
        requestLayout();
        restartAnimation();
    }

    public int getStopCount() {
        return names.size();
    }

    private void restartAnimation() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        int n = names.size();
        if (n < 2) {
            t = 0;
            invalidate();
            return;
        }
        float end = (n - 1) + HOLD;
        animator = ValueAnimator.ofFloat(0f, end);
        animator.setDuration((long) (end * MS_PER_SEGMENT));
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            t = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    /** Bus position in row units: n-1 (oldest, bottom) -> 0 (newest, top). */
    private float busRow() {
        int n = names.size();
        if (n < 2) {
            return 0f;
        }
        float tt = Math.min(t, n - 1);
        int seg = (int) Math.floor(tt);
        if (seg >= n - 1) {
            return 0f;
        }
        float f = tt - seg;
        // drive during the first 60% of a segment, wait at the stop for the rest
        float move = Math.min(1f, f / 0.6f);
        move = move * move * (3 - 2 * move);           // smoothstep ease in/out
        return (n - 1) - (seg + move);
    }

    private float yOf(float row) {
        return rowH * row + rowH / 2f;
    }

    /** Cuts names that would run past the right edge, and caches them per width. */
    private void ensureLabels() {
        int w = getWidth();
        if (w == labelsWidth) {
            return;
        }
        labelsWidth = w;
        labels.clear();
        float avail = w - (lineX + 36 * density);
        for (String s : names) {
            labels.add(avail <= 0 ? s
                    : TextUtils.ellipsize(s, namePaint, avail, TextUtils.TruncateAt.END).toString());
        }
    }

    @Override
    protected void onMeasure(int w, int h) {
        int height = (int) (rowH * Math.max(1, names.size()));
        setMeasuredDimension(resolveSize(getSuggestedMinimumWidth(), w), height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        labelsWidth = -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int n = names.size();
        if (n == 0) {
            return;
        }
        ensureLabels();
        float busY = yOf(busRow());

        if (n > 1) {
            canvas.drawLine(lineX, yOf(0), lineX, yOf(n - 1), linePaint);
            canvas.drawLine(lineX, busY, lineX, yOf(n - 1), trailPaint);  // travelled part
        }

        for (int i = 0; i < n; i++) {
            float y = yOf(i);
            boolean reached = y >= busY - 1f;
            dotPaint.setColor(reached ? 0xFF1E88E5 : 0xFFB0BEC5);
            float r = (i == 0 ? 8 : 6) * density;
            canvas.drawCircle(lineX, y, r, dotPaint);

            float tx = lineX + 36 * density;
            namePaint.setFakeBoldText(i == 0);
            canvas.drawText(labels.get(i), tx, y - 2 * density, namePaint);
            canvas.drawText(times.get(i), tx, y + 16 * density, timePaint);
        }

        // the bus, sitting on the line
        canvas.drawText("\uD83D\uDE8D", lineX, busY + 8 * density, busPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        restartAnimation();
    }
}