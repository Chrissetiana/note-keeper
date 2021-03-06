package com.chrissetiana.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import java.util.List;

public class ModuleStatusView extends View {
    public static final int EDIT_MODE_MODULE_COUNT = 7;
    public static final int INVALID_INDEX = -1;
    public static final int SHAPE_CIRCLE = 0;
    public static final float DEFAULT_OUTLINE_WIDTH_DP = 2f;
    private boolean[] moduleStatus;
    private float outlineWidth;
    private float shapeSize;
    private float spacing;
    private Rect[] moduleRectangles;
    private int outlineColor;
    private Paint paintOutline;
    private int fillColor;
    private Paint paintFill;
    private float radius;
    private int maxHorizontalModules;
    private int shape;
    private ModuleStatusAccessibilityHelper accessibilityHelper;

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    public boolean[] getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    private void init(AttributeSet attrs, int defStyle) {
        if (isInEditMode()) {
            setupEditModeValues();
        }

        setFocusable(true);
        accessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, accessibilityHelper);

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float displayDensity = dm.density;
        float defaultOutlineWidthPixels = displayDensity * DEFAULT_OUTLINE_WIDTH_DP;

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ModuleStatusView, defStyle, 0);

        outlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        shape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE);
        outlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth, defaultOutlineWidthPixels);

        a.recycle();

        shapeSize = 144f;
        spacing = 30f;
        radius = (shapeSize - outlineWidth) / 2;

        paintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintOutline.setStyle(Paint.Style.STROKE);
        paintOutline.setStrokeWidth(outlineWidth);
        paintOutline.setColor(outlineColor);

        fillColor = getContext().getResources().getColor(R.color.psorange);
        paintFill = new Paint(Paint.ANTI_ALIAS_FLAG);

        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setColor(fillColor);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        accessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return accessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return accessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    private void setupEditModeValues() {
        boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_COUNT];
        int middle = EDIT_MODE_MODULE_COUNT / 2;

        for (int i = 0; i < middle; i++) {
            exampleModuleValues[i] = true;
        }

        setModuleStatus(exampleModuleValues);
    }

    private void setupModuleRectangles(int width) {
        int availableWidth = width - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (shapeSize + spacing));
        int maxHorizontalModules = Math.min(horizontalModulesThatCanFit, moduleStatus.length);

        moduleRectangles = new Rect[moduleStatus.length];

        for (int i = 0; i < moduleRectangles.length; i++) {
            int column = i % maxHorizontalModules;
            int row = i / maxHorizontalModules;
            int x = getPaddingLeft() + (int) (column * (shapeSize + spacing));
            int y = getPaddingTop() + (int) (row * (shapeSize + spacing));

            moduleRectangles[i] = new Rect(x, y, x + (int) shapeSize, y + (int) shapeSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (shapeSize + spacing));
        maxHorizontalModules = Math.min(horizontalModulesThatCanFit, moduleStatus.length);

        int desiredWidth = (int) ((maxHorizontalModules * (shapeSize + spacing)) - spacing);
        desiredWidth += getPaddingLeft() + getPaddingRight();

        int rows = ((moduleStatus.length - 1) / maxHorizontalModules) + 1;

        int desiredHeight = (int) ((rows * (shapeSize + spacing) - spacing));
        desiredHeight += getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setupModuleRectangles(w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < moduleRectangles.length; i++) {
            if (shape == SHAPE_CIRCLE) {
                float x = moduleRectangles[i].centerX();
                float y = moduleRectangles[i].centerY();

                if (moduleStatus[i]) {
                    canvas.drawCircle(x, y, radius, paintFill);
                }

                canvas.drawCircle(x, y, radius, paintOutline);
            } else {
                drawSquare(canvas, i);
            }
        }
    }

    private void drawSquare(Canvas canvas, int i) {
        Rect moduleRectangle = moduleRectangles[i];

        if (moduleStatus[i]) {
            canvas.drawRect(moduleRectangle, paintFill);
            canvas.drawRect(moduleRectangle.left + (outlineWidth / 2),
                    moduleRectangle.top + (outlineWidth / 2),
                    moduleRectangle.right - (outlineWidth / 2),
                    moduleRectangle.bottom - (outlineWidth / 2),
                    paintFill);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int moduleIndex = findItemAtPoint(event.getX(), event.getY());
                onModuleSelected(moduleIndex);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void onModuleSelected(int moduleIndex) {
        if (moduleIndex == INVALID_INDEX) {
            return;
        }

        moduleStatus[moduleIndex] = !moduleStatus[moduleIndex];
        invalidate();

        accessibilityHelper.invalidateVirtualView(moduleIndex);
        accessibilityHelper.sendEventForVirtualView(moduleIndex, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private int findItemAtPoint(float x, float y) {
        int moduleIndex = INVALID_INDEX;

        for (int i = 0; i < moduleRectangles.length; i++) {
            if (moduleRectangles[i].contains((int) x, (int) y)) {
                moduleIndex = i;
                break;
            }
        }

        return moduleIndex;
    }

    private class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper {

        ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findItemAtPoint(x, y);
            return moduleIndex == INVALID_INDEX ? ExploreByTouchHelper.INVALID_ID : moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> list) {
            if (moduleRectangles == null) {
                return;
            }

            for (int i = 0; i < moduleRectangles.length; i++) {
                list.add(i);
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            node.setFocusable(true);
            node.setBoundsInParent(moduleRectangles[virtualViewId]);
            node.setContentDescription("Module " + virtualViewId);
            node.setCheckable(true);
            node.setCheckable(moduleStatus[virtualViewId]);
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle bundle) {
            switch (action) {
                case AccessibilityNodeInfoCompat.ACTION_CLICK:
                    onModuleSelected(virtualViewId);
                    return true;
            }

            return false;
        }
    }
}
