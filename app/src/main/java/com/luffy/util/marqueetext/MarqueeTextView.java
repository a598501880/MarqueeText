package com.luffy.util.marqueetext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SuppressLint("AppCompatCustomView")
public class MarqueeTextView extends TextView {
    /**
     * 获取的文字
     */
    private String text = "";
    /**
     * 需要绘制的文字
     */
    private String drawText = "";
    /**
     * 文本的颜色
     */
    private int textColor;
    /**
     * 文本的大小
     */
    private float textSize;
    /**
     * 移动的速度
     */
    private float textSpeed = 4f;
    /**
     * 控件宽
     */
    private int width;
    /**
     * 控件高
     */
    private int height;
    /**
     * 已经移动的像素
     */
    private float move = 0f;
    /**
     * 滚动模式
     */
    private int mode = 1;
    /**
     * 滚动2时触发效果的临界点
     */
    private float moveMeasured;
    /**
     * 滚动2时的间隔
     */
    private String step = "     ";
    /**
     * 炫彩特效
     */
    private int colorfulIndex;
    private ScheduledExecutorService ese;


    private Rect rect;
    private Paint paint;
    private Context context;

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MarqueeTextView, 0, 0);
        text = getText().toString();
        if (mode == 1) {
            drawText = text;
        } else {
            setDrawText();
        }

        textColor = getCurrentTextColor();
        textSize = getTextSize();

        height = typedArray.getInteger(R.styleable.MarqueeTextView_pixelHeight, -1);
        width = typedArray.getInteger(R.styleable.MarqueeTextView_pixelWidth, -1);

        paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);

        rect = new Rect();
        paint.getTextBounds(drawText, 0, drawText.length(), rect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (width == -1) {
            getDefaultWidth(widthMeasureSpec);
        }
        if (height == -1) {
            getDefaultHeight(heightMeasureSpec);
        }
        setMeasuredDimension(width, height);
    }

    private void getDefaultWidth(int widthMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            float textWidth = rect.width();
            width = (int) (getPaddingLeft() + textWidth + getPaddingRight());
        }
    }

    private void getDefaultHeight(int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            float textHeight = rect.height();
            height = (int) (getPaddingTop() + textHeight + getPaddingBottom());
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    public void setText(String marqueeText) {
        this.text = marqueeText;
        if (mode == 2) {
            setDrawText();
        } else {
            drawText = text;
        }
    }

    public void setMode(int marqueeTextMode) {
        this.mode = marqueeTextMode;
        if (mode == 2) {
            setDrawText();
        } else {
            drawText = text;

        }
    }

    public void setSize(int marqueeTextSize) {
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marqueeTextSize, context.getResources().getDisplayMetrics());
        paint.setTextSize(textSize);
        setDrawText();
    }

    public void setColor(int marqueeTextColor) {
        textColor = marqueeTextColor;
        paint.setColor(marqueeTextColor);
    }

    public void startColorful(int[] color) {
        // 自定义颜色
        setColorful(color);
    }

    public void startColorful() {
        // 默认使用七彩
        int[] color = new int[]{Color.parseColor("#FF0000"),
                Color.parseColor("#FF6600"), Color.parseColor("#FFFF66"),
                Color.parseColor("#00CC00"), Color.parseColor("#669999"),
                Color.parseColor("#0066CC"), Color.parseColor("#990099"),
        };
        setColorful(color);
    }

    private void setColorful(final int[] color) {
        colorfulIndex = 0;
        ese = Executors.newSingleThreadScheduledExecutor();
        ese.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                paint.setColor(color[colorfulIndex]);
                colorfulIndex++;
                if (colorfulIndex == color.length) {
                    colorfulIndex = 0;
                }
            }
        }, 200, 200, TimeUnit.MILLISECONDS);
    }

    public void stopColorful() {
        if (ese != null) {
            ese.shutdown();
        }
        paint.setColor(textColor);
    }

    public void setFont(Typeface marqueeTextFont) {
        paint.setTypeface(marqueeTextFont);
        setDrawText();
    }

    public void setSpeed(float marqueeTextSpeed) {
        textSpeed = marqueeTextSpeed;
    }

    public void setStep(int level) {
        if (mode == 2) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < level * 5; i++) {
                stringBuilder.append(" ");
            }
            step = stringBuilder.toString();
            setDrawText();
        }
    }

    private void setDrawText() {
        String str = text + step;
        moveMeasured = paint.measureText(str);
        int stepNum = (int) (width % moveMeasured);
        StringBuilder builder = new StringBuilder();
        builder.append(text);
        for (int i = 0; i < stepNum; i++) {
            builder.append(step).append(text);
        }
        drawText = builder.toString();
        System.gc();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.getTextBounds(drawText, 0, drawText.length(), rect);
        int height = getHeight() / 2;
        int measuredWidth = getMeasuredWidth();
        float y = height + (-paint.ascent() + paint.descent()) / 2 - paint.descent();
        canvas.drawText(drawText, measuredWidth - move, y, paint);
        move += textSpeed;
        if (mode == 2) {
            if (move >= measuredWidth + moveMeasured) {
                move = measuredWidth;
            }
        } else {
            if (move >= measuredWidth + rect.width()) {
                move = 0f;
            }
        }
        invalidate();
    }

}