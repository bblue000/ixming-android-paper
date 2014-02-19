package org.ixming.android.paper;

import org.ixming.android.view.attrs.FloatPoint;
import org.ixming.android.view.attrs.FloatRoute;
import org.ixming.android.view.attrs.ViewProperties;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;

public class PagerViewContainer extends ViewGroup {

	private static final String TAG = "yytest";
	
	private final float SCALE_MAXIMUM = Float.MAX_VALUE;
	private final float SCALE_MINIMUM = 1F - 0.618F;
	private final float SCALE_INIT = 1F;
	private final float SCALE_THRESHOLD = (1F + SCALE_MINIMUM) / 2F;
	
	private float mDensity;
	private Matrix mScaleMatrix = new Matrix();
	
	private CustomPagerView mPagerView;
	
	private GestureDetector mGestureDetector;
	private PageViewScaleChangedListener mPageViewScaleChangedListener;
	public PagerViewContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPagerViewContainer();
	}

	public PagerViewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPagerViewContainer();
	}

	public PagerViewContainer(Context context) {
		super(context);
		initPagerViewContainer();
	}
	
	private void initPagerViewContainer() {
		mDensity = getResources().getDisplayMetrics().density;
		
		mGestureDetector = new GestureDetector(getContext(), new GestureListenerImpl());
		
		mPagerView = new CustomPagerView(getContext());
		LayoutParams pagerViewLP = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mPagerView.setLayoutParams(pagerViewLP);
		mPagerView.setPageSpacing(ViewProperties.getAsInt(5 * mDensity));
		addView(mPagerView);
	}
	
	public void setOnScaleChangedListener(PageViewScaleChangedListener listener) {
		mPageViewScaleChangedListener = listener;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		mPagerView.measure(widthMeasureSpec, heightMeasureSpec);
//		
//		LayoutParams lp = mPagerView.getLayoutParams();
//		int width = mPagerView.getMeasuredWidth();
//		int height = mPagerView.getMeasuredHeight();
//		lp.width = width;
//		lp.height = height;
//		mPagerView.setLayoutParams(lp);
		measureChild(mPagerView, widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		mPagerView.layout(getPaddingLeft(), getPaddingTop(),
				getPaddingLeft() + mPagerView.getMeasuredWidth(),
				getPaddingTop() + mPagerView.getMeasuredHeight());
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				//TODO 辅助，虽然基本没有作用，但是为了心里安慰...
				clearAnimation();
			}
			return super.onTouchEvent(event) | mGestureDetector.onTouchEvent(event);
		} finally {
			if (MotionEvent.ACTION_CANCEL == event.getAction() || MotionEvent.ACTION_UP == event.getAction()) {
				//TODO 
				checkScrollAndScaleBounds();
			}
		}
	}
	
	private final FloatPoint mCachePoint = new FloatPoint();
	private final float[] mMatrixValues = new float[9];
	private final FloatPoint mScalePoint = new FloatPoint();
	private void checkScrollAndScaleBounds() {
		mCachePoint.set(0, 0);
		float curScale = recalculateCurrentScale();
		float toScale;
		if (curScale < SCALE_THRESHOLD) {
			toScale = SCALE_MINIMUM;
		} else {
			toScale = SCALE_INIT;
		}
		
		int curScrollX = getScrollX();
		int toScrollX = curScrollX;
		if (toScrollX < mCachePoint.x) {
			toScrollX = ViewProperties.getAsInt(mCachePoint.x);
		}
		if (toScrollX > getMaxScrollX() + mCachePoint.x) {
			toScrollX = ViewProperties.getAsInt(getMaxScrollX() + mCachePoint.x);
		}
		int curScrollY = getScrollY();
		int toScrollY = curScrollY;
		AnimationSet animSet = new AnimationSet(true);
		animSet.setInterpolator(new AccelerateDecelerateInterpolator());
		ScaleAnim anim1 = new ScaleAnim(curScale, toScale, mScalePoint.x, mScalePoint.y);
		anim1.setDuration(400);
		animSet.addAnimation(anim1);
		ScrollAnim anim2 = new ScrollAnim(curScrollX, toScrollX, curScrollY, toScrollY);
		anim2.setDuration(400);
		animSet.addAnimation(anim2);
		startAnimation(animSet);
	}
	
	private float recalculateCurrentScale() {
		mScaleMatrix.getValues(mMatrixValues);
		
		mCachePoint.x = mMatrixValues[0] * mCachePoint.x + mMatrixValues[2];
		mCachePoint.y = mMatrixValues[4] * mCachePoint.y + mMatrixValues[5];
		return Math.max(mMatrixValues[0], mMatrixValues[4]);
	}
	
	private float getMaxScrollX() {
		return Math.max(0, mPagerView.getContentWidth() - mPagerView.getPageWidth());
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.concat(mScaleMatrix);
		super.dispatchDraw(canvas);
	}
	
	private class GestureListenerImpl extends SimpleOnGestureListener {
		/**
		 * 用于手势期间判断是否有过多点触摸，如果有，在下一次的单点触摸时，重新计算TouchRoute
		 */
		private boolean mIsLastMultiPointer = false;
		/**
		 * 描述手势路径
		 */
		private final FloatRoute mTouchRoute = new FloatRoute();
		private final FloatPoint mDownPoint = new FloatPoint();
		private final FloatPoint mDeltaPoint = new FloatPoint();
		
		private float mDownScale = SCALE_INIT;
		@Override
		public boolean onDown(MotionEvent e) {
			Log.d(TAG, "onDown (" + e.getX() + ", " + e.getY() + ")");
			onConsideredDown(e);
			return true;
		}
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (e2.getPointerCount() == 1) {
				float x = e2.getX();
				float y = e2.getY();
				if (mIsLastMultiPointer) {
					onConsideredDown(e2);
					mIsLastMultiPointer = false;
				} else {
					mTouchRoute.setCurrent(x, y);
					float dx = mTouchRoute.getDeltaX();
					
					float scale = mDownScale * calculateScale(mTouchRoute.getTotalDeltaY());
					tranferToScaleTo(scale, mScalePoint.x, mScalePoint.y, true);
					tranferToScrollBy(dx, 0);
				}
				return true;
			}
			mIsLastMultiPointer = true;
			return false;
		}
		
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return super.onFling(e1, e2, velocityX, velocityY);
		}
		
		private void onConsideredDown(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			mDownPoint.set(x, y);
			mTouchRoute.setDown(x, y);
			mScalePoint.set(x + getScrollX(), getHeight());
			
			mDownScale = recalculateCurrentScale();
		}
	}
	
	private void tranferToScrollBy(float dx, float dy) {
		scrollBy(ViewProperties.getAsInt(-dx), ViewProperties.getAsInt(-dy));
	}
	
	private void tranferToScrollTo(float x, float y) {
		scrollTo(ViewProperties.getAsInt(x), ViewProperties.getAsInt(y));
	}
	
	private void tranferToScaleTo(float scale, float px, float py, boolean checkBounds) {
		float oldScale = recalculateCurrentScale();
		if (checkBounds) {
			scale = Math.max(scale, SCALE_MINIMUM);
			scale = Math.min(scale, SCALE_MAXIMUM);
		}

		mScaleMatrix.reset();
		mScaleMatrix.setScale(scale, scale, px, py);
		
		if (null != mPageViewScaleChangedListener) {
			mPageViewScaleChangedListener.onScaleChanged(scale, oldScale);
		}

	}
	
	private float calculateScale(float yOffset) {
		float symbol = - Math.signum(yOffset);
		float scaleOffset = Math.abs(yOffset / getHeight());
		return 1.0F + symbol * scaleOffset;
	}
	
	private class ScaleAnim extends Animation {
		private float mFromScale;
		private float mToScale;
		private float mPx;
		private float mPy;
		
		public ScaleAnim(float fromScale, float toScale, float px, float py) {
			this.mFromScale = fromScale;
			this.mToScale = toScale;
			this.mPx = px;
			this.mPy = py;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			float scale = this.mFromScale + (this.mToScale - this.mFromScale) * interpolatedTime;
			tranferToScaleTo(scale, mPx, mPy, false);
			invalidate();
		}
	}
	
	private class ScrollAnim extends Animation {
		private float mFromScrollX;
		private float mToScrollX;
		private float mFromScrollY;
		private float mToScrollY;
		
		public ScrollAnim(float fromScrollX, float toScrollX,
				float fromScrollY, float toScrollY) {
			this.mFromScrollX = fromScrollX;
			this.mToScrollX = toScrollX;
			this.mFromScrollY = fromScrollY;
			this.mToScrollY = toScrollY;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			tranferToScrollTo(this.mFromScrollX + (this.mToScrollX - this.mFromScrollX) * interpolatedTime,
					this.mFromScrollY + (this.mToScrollY - this.mFromScrollY) * interpolatedTime);
		}
	}
}
