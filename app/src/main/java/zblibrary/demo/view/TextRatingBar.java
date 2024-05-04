package zblibrary.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import zblibrary.demo.interfaces.OnRatingListener;

/**
 * Created by rome753 on 2017/3/10
 */

public class TextRatingBar extends View {

    //paddingLeft
    private int mLeft;
    //paddingTop
    private int mTop;
    //当前rating
    private int mRating;
    //总raring数
    private int mCount;
    //rating文字
    private String[] texts = {"超小", "小", "默认", "中", "大", "超大"};
    //相邻raring的距离
    private int mUnitSize;
    //bar到底部的距离
    private int mYOffset;
    //小竖条的一半长度
    private int mMarkSize;

    Paint paint = new Paint();

    public TextRatingBar(Context context) {
        this(context, null);
    }

    public TextRatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCount = 6;
        mRating = 0;
        mMarkSize = 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("test", getMeasuredWidth() + " " + getMeasuredHeight());
        mLeft = (getPaddingLeft() + getPaddingRight()) / 2;
        mTop = getPaddingTop();
        int barWidth = getMeasuredWidth() - 2 * mLeft;
        mUnitSize = barWidth / (mCount - 1);
        mYOffset = getMeasuredHeight() - getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStrokeWidth(2);
        paint.setColor(Color.RED);
        canvas.drawLine(mLeft, mYOffset, mLeft + mRating * mUnitSize, mYOffset, paint);
        for (int i = 0; i < mCount; i++) {
            paint.setColor(Color.RED);
            canvas.drawLine(mLeft + i * mUnitSize, mYOffset - mMarkSize, mLeft + i * mUnitSize, mYOffset + mMarkSize, paint);
            paint.setColor(mRating == i ? Color.RED : Color.BLACK);
            paint.setTextSize(50);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(texts[i], mLeft + i * mUnitSize, mTop, paint);
        }
        paint.setColor(Color.GRAY);
        canvas.drawLine(mLeft + mRating * mUnitSize, mYOffset, mLeft + (mCount - 1) * mUnitSize, mYOffset, paint);
        canvas.drawCircle(mLeft + mRating * mUnitSize, mYOffset, 10, paint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            for (int i = 0; i < mCount; i++) {
                float distance = mLeft + i * mUnitSize - x;
                if (Math.abs(distance) < 100) {
                    setRating(i);
                    if (onRatingListener != null) {
                        onRatingListener.onRating(mRating);
                    }
                    break;
                }
            }
        }
        return true;
    }

    public void setRating(int rating) {
        mRating = rating;
        invalidate();
    }

    private OnRatingListener onRatingListener;

    public void setOnRatingListener(OnRatingListener onRatingListener) {
        this.onRatingListener = onRatingListener;
    }


}
