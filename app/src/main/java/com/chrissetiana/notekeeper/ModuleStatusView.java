package com.chrissetiana.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ModuleStatusView extends View {
    public static final int EDIT_MODE_MODULE_COUNT = 7;
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

        a.recycle();

        outlineWidth = 6f;
        shapeSize = 144f;
        spacing = 30f;
        radius = (shapeSize - outlineWidth) / 2;

        setupModuleRectangles();

        outlineColor = Color.BLACK;
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

    private void setupModuleRectangles() {
        moduleRectangles = new Rect[moduleStatus.length];

        for (int i = 0; i < moduleRectangles.length; i++) {
            int x = getPaddingLeft() + (int) (i * (shapeSize + spacing));
            int y = getPaddingTop();

            moduleRectangles[i] = new Rect(x, y, x + (int) shapeSize, y + (int) shapeSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 0;
        int desiredHeight = 0;

        desiredWidth = (int) ((moduleStatus.length * (shapeSize + spacing)) - spacing);
        desiredWidth += getPaddingLeft() + getPaddingRight();

        desiredHeight = (int) shapeSize;
        desiredHeight += getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0);
        int height = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);
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
}
