package cn.edu.sdust.silence.itransfer.ui.scan.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cn.edu.sdust.silence.itransfer.R;
import cn.edu.sdust.silence.itransfer.ui.scan.utils.DisplayUtils;


public class CircleView extends View {
    private Paint mPaint;
    private Bitmap mBitmap;
    private float radius = DisplayUtils.dp2px(getContext(), 9);//半径
    private float disX;//位置X
    private float disY;//位置Y
    private float angle;//旋转的角度
    private float proportion;//根据远近距离的不同计算得到的应该占的半径比例

    private String phoneName = "";
    private Paint textPaint;
    private int textSize = DisplayUtils.dp2px(getContext(), 10);

    public String getPhoneName() {
        return phoneName;
    }

    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }


    public float getProportion() {
        return proportion;
    }

    public void setProportion(float proportion) {
        this.proportion = proportion;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getDisX() {
        return disX;
    }

    public void setDisX(float disX) {
        this.disX = disX;
    }

    public float getDisY() {
        return disY;
    }

    public void setDisY(float disY) {
        this.disY = disY;
    }

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.bg_color_pink));
        mPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measureWidthSize(widthMeasureSpec), measureHeightSize(heightMeasureSpec));
    }

    private int measureHeightSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            Rect bounds = new Rect();
            textPaint.getTextBounds(phoneName, 0, phoneName.length(), bounds);
            int textHeight = bounds.height();
            result = DisplayUtils.dp2px(getContext(), 18) + 3 * textHeight;
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureWidthSize(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = DisplayUtils.dp2px(getContext(), 18);
            Rect bounds = new Rect();
            textPaint.getTextBounds(phoneName, 0, phoneName.length(), bounds);
            int textWidth = bounds.width();
            if (textWidth > result)
                result = textWidth + 10;

            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect bounds = new Rect();
        textPaint.getTextBounds(phoneName, 0, phoneName.length(), bounds);

        int textWidth = bounds.width() ;
        int textHeight = bounds.height();

        canvas.drawCircle( getMeasuredWidth() / 2, radius, radius, mPaint);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, null, new Rect((int) ( getMeasuredWidth()  - 2 * radius) / 2, 0, (int) (getMeasuredWidth()  + 2 * radius) / 2, 2 * (int) radius), mPaint);
        }
        canvas.drawText(phoneName, getMeasuredWidth() / 2 - textWidth / 2, 2 * (int) radius + 2 * textHeight, textPaint);
    }

    public void setPaintColor(int resId) {
        mPaint.setColor(resId);
        invalidate();
    }

    public void setPortraitIcon(int resId) {
        mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        invalidate();
    }

    public void clearPortaitIcon() {
        mBitmap = null;
        invalidate();
    }
}
