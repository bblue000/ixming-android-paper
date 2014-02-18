package org.ixming.android.paper;

import org.ixming.android.view.attrs.FloatRoute;
import org.ixming.android.view.attrs.ViewProperties;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

public class PagerContainer extends RelativeLayout {

	private final String TAG = "yytest";
	
	//TODO 缩放的限制
	private final float SCALE_MAXMUM = 1.0F;
	private final float SCALE_MINMUM = 1F - 0.618F;
	
	private float SCALE_TOUCH_AREA_HEIGHT = 50F;
	
	private float mDensity;
	
	private boolean mIsDragState = false;
	
	private ViewPager mViewPager;
	private WrappedPagerAdapter mWrappedPagerAdapter;
	
	private float mPageScale = SCALE_MAXMUM;
	private FloatRoute mTouchRoute = new FloatRoute();
	
	public PagerContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initPagerContainer();
	}

	public PagerContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPagerContainer();
	}

	public PagerContainer(Context context) {
		super(context);
		initPagerContainer();
	}

	private void initPagerContainer() {
		mDensity = getContext().getResources().getDisplayMetrics().density;
		SCALE_TOUCH_AREA_HEIGHT *= mDensity;
		
		mViewPager = new ViewPager(getContext());
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lp.addRule(ALIGN_PARENT_BOTTOM);
		mViewPager.setLayoutParams(lp);
		addView(mViewPager);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
			throw new UnsupportedOperationException("");
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		checkDragState(event);
		if (mIsDragState) {
			return true;
		}
		return super.onInterceptTouchEvent(event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		checkDragState(event);
		if (mIsDragState) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE: {
					Log.d(TAG, "onTouchEvent move");
					mTouchRoute.setCurrent(event.getX(), event.getY());
					
					float targetScale = 1F - mTouchRoute.getTotalDeltaY() / getHeight();
					targetScale = Math.max(SCALE_MINMUM, targetScale);
					targetScale = Math.min(SCALE_MAXMUM, targetScale);
					if (targetScale != mPageScale) {
						onPagerScaleChanged(targetScale);
					}
					break;
				}
				case MotionEvent.ACTION_UP: {
					float resultScale ;
					if (mPageScale < (SCALE_MAXMUM + SCALE_MINMUM) / 2F) {
						resultScale = SCALE_MINMUM;
					} else {
						resultScale = SCALE_MAXMUM;
					}
					PagerAnimation anim = new PagerAnimation(mPageScale, resultScale);
					anim.setDuration(400);
					anim.startRun();
//					anim.setInterpolator(new OvershootInterpolator(2));
//					startAnimation(anim);
					break;
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
	
	private void checkDragState(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.d(TAG, "checkDragState");
			mIsDragState = false;
			if (event.getY() < SCALE_TOUCH_AREA_HEIGHT) {
				mTouchRoute.reset();
				mTouchRoute.setDown(event.getX(), event.getY());
				mIsDragState = true;
			}
		}
	}
	
	private void onPagerScaleChanged(float newScale) {
		mPageScale = newScale;
		//TODO update page height
		LayoutParams lp = (LayoutParams) mViewPager.getLayoutParams();
		lp.height = ViewProperties.getAsInt(getHeight() * mPageScale);
		mViewPager.setLayoutParams(lp);
		
		if (null != mWrappedPagerAdapter) {
			PagerAdapter basePagerAdapter = mWrappedPagerAdapter.getBasePagerAdapter();
			int currentIndex = mViewPager.getCurrentItem();
			mViewPager.setAdapter(mWrappedPagerAdapter = nextWrappedPagerAdapter()
					.setBasePagerAdapter(basePagerAdapter));
			mViewPager.setCurrentItem(currentIndex);
		}
		
		//TODO redraw
		mViewPager.requestLayout();
		mViewPager.invalidate();
		invalidate();
	}
	
	public void setPagerAdapter(PagerAdapter adapter) {
		mViewPager.setAdapter(mWrappedPagerAdapter = nextWrappedPagerAdapter().setBasePagerAdapter(adapter));
		mViewPager.invalidate();
		invalidate();
	}
	
	public PagerAdapter getPagerAdapter() {
		return null == mWrappedPagerAdapter? null : mWrappedPagerAdapter.getBasePagerAdapter();
	}
	
	private int mCurIndex = 0;
	private WrappedPagerAdapter[] mWrappedPagerAdapterInstances = new WrappedPagerAdapter[]{
		new WrappedPagerAdapter(), new WrappedPagerAdapter()
	};
	private WrappedPagerAdapter nextWrappedPagerAdapter() {
		int newIndex = (mCurIndex + 1) % 2;
		WrappedPagerAdapter adapter = mWrappedPagerAdapterInstances[mCurIndex];
		mCurIndex = newIndex;
		return adapter;
	}
	private class WrappedPagerAdapter extends PagerAdapter {
		private PagerAdapter mBasePagerAdapter;
		public WrappedPagerAdapter() { }
		public WrappedPagerAdapter setBasePagerAdapter(PagerAdapter adapter) {
			mBasePagerAdapter = adapter;
			return this;
		}
		public PagerAdapter getBasePagerAdapter() {
			return mBasePagerAdapter;
		}
		
		private boolean hasBasePagerAdapter() {
			return null != mBasePagerAdapter;
		}
		
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		@Override
		public int getCount() {
			return hasBasePagerAdapter() ? mBasePagerAdapter.getCount() : 0;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return hasBasePagerAdapter() ? mBasePagerAdapter.instantiateItem(container, position) : null;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (hasBasePagerAdapter()){
				mBasePagerAdapter.destroyItem(container, position, object);
			}
		}
		
		@Override
		public float getPageWidth(int position) {
			return mPageScale;
		}
	}
	
	private class PagerAnimation extends Animation implements Runnable {
		private float mFromScale;
		private float mToScale;
		private float mCurrentPercent;
		public PagerAnimation(float fromScale, float toScale) {
			mFromScale = fromScale;
			mToScale = toScale;
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			float targetScale = mFromScale + (mToScale - mFromScale) * interpolatedTime;
			Log.d(TAG, "applyTransformation targetScale = " + targetScale);
			onPagerScaleChanged(targetScale);	
		}
		
		public void startRun() {
			post(this);
		}
		
		@Override
		public void run() {
			mCurrentPercent += 10;
			
			float targetScale = mFromScale + (mToScale - mFromScale) * (mCurrentPercent / 100F);
			onPagerScaleChanged(targetScale);
			if (mCurrentPercent == 100) {
				return ;
			}
			postDelayed(this, 15);
		}
	}
}
