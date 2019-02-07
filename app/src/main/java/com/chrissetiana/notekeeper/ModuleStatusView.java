package com.chrissetiana.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ModuleStatusView extends View {
    public static final int EDIT_MODE_MODULE_COUNT = 7;
    public static final int INVALID_INDEX = -1;
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

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ModuleStatusView, defStyle, 0);

        outlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        a.recycle();

        outlineWidth = 6f;
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
        int desiredWidth = 0;
        int desiredHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModulesThatCanFit = (int) (availableWidth / (shapeSize + spacing));
        maxHorizontalModules = Math.min(horizontalModulesThatCanFit, moduleStatus.length);

        desiredWidth = (int) ((maxHorizontalModules * (shapeSize + spacing)) - spacing);
        desiredWidth += getPaddingLeft() + getPaddingRight();

        int rows = ((moduleStatus.length - 1) / maxHorizontalModules) + 1;

        desiredHeight = (int) ((rows * (shapeSize + spacing) - spacing));
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
            float x = moduleRectangles[i].centerX();
            float y = moduleRectangles[i].centerY();

            if (moduleStatus[i]) {
                canvas.drawCircle(x, y, radius, paintFill);
            }

            canvas.drawCircle(x, y, radius, paintOutline);
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
    }

    private int findItemAtPoint(float x, float y) {
        int moduleIndex = INVALID_INDEX;

        for (int i = 0; i < moduleRectangles.length; i++) {
            if (moduleRectangles[i].contains((int) x, (int) y)) {
                moduleIndex = i;
                break;
            }
        }

        return 0;
    }
}
