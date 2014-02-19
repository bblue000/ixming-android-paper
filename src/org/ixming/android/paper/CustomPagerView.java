package org.ixming.android.paper;

import java.util.ArrayList;

import org.ixming.android.view.ViewUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CustomPagerView extends ViewGroup {

	private final String TAG = "yytest";
	
	private int mPageSpacing = 0;
	private int mPageWidth = 0;
	private int mPageHeight = 0;
	
	private int mTotalContentWidth = 0;
	private int mTotalContentHeight = 0;
	
	public CustomPagerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initCustomPagerView();
	}

	public CustomPagerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initCustomPagerView();
	}

	public CustomPagerView(Context context) {
		super(context);
		initCustomPagerView();
	}

	private void initCustomPagerView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		
		View currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
		
		currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
		
		currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
		
		currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
		
		currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
			throw new UnsupportedOperationException("");
		}
		
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		Log.d(TAG, "CustomPagerView onMeasure widthSize = " + widthSize);
		Log.d(TAG, "CustomPagerView onMeasure heightSize = " + heightSize);
		
		mPageWidth = Math.max(0, widthSize - getPaddingLeft() - getPaddingRight());
		mPageHeight = Math.max(0, heightSize - getPaddingTop() - getPaddingBottom());
		
		int desiredWidth = getPaddingLeft();
		int desiredHeight = heightSize;
		
		boolean atLeastOneChildShown = false;
		final int childCount = getChildCount();
		if (childCount > 0) {
			int childWidthAndMode = MeasureSpec.makeMeasureSpec(mPageWidth, MeasureSpec.EXACTLY);
			int childHeightAndMode = MeasureSpec.makeMeasureSpec(mPageHeight, MeasureSpec.EXACTLY);
			boolean calSpacing = false;
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (ViewUtils.isGone(child)) {
					continue;
				}
				measureChild(child, childWidthAndMode, childHeightAndMode);
				atLeastOneChildShown = true;
				if (calSpacing) {
					desiredWidth += mPageSpacing;
				}
				desiredWidth += mPageWidth;
				calSpacing = true;
			}
		}
		if (!atLeastOneChildShown) {
			desiredWidth += mPageWidth;
		}
		desiredWidth += getPaddingRight();
		
		mTotalContentWidth = desiredWidth;
		mTotalContentHeight = desiredHeight;
		
		super.onMeasure(MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY));
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (!changed) {
			return;
		}
		final int childCount = getChildCount();
		if (childCount > 0) {
			int top = getPaddingTop();
			int bottom = top + mPageHeight;
			int leftOffset = getPaddingLeft();
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				if (ViewUtils.isGone(child)) {
					continue;
				}
				child.layout(leftOffset, top, leftOffset + mPageWidth, bottom);
				leftOffset += mPageWidth;
				leftOffset += mPageSpacing;
			}
		}
	}
	
	public void setPageSpacing(int spacing) {
		if (spacing < 0) {
			mPageSpacing = 0;
		}
		mPageSpacing = spacing;
	}
	
	public int getPageSpacing() {
		return mPageSpacing;
	}
	
	public int getContentWidth() {
		return mTotalContentWidth;
	}
	
	public int getContentHeight() {
		return mTotalContentHeight;
	}
	
	public int getPageWidth() {
		return mPageWidth;
	}
	
	public int getPageHeight() {
		return mPageHeight;
	}
}
