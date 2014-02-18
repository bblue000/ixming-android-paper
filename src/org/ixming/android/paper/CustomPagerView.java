package org.ixming.android.paper;

import java.util.ArrayList;

import org.ixming.android.view.FilledInContainer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CustomPagerView extends FrameLayout {

	private LinearLayout mContentLayout;
	private ArrayList<View> mViewList = new ArrayList<View>();
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
		mViewList.add(inflater.inflate(R.layout.pager_item, this, false));
		mViewList.add(inflater.inflate(R.layout.pager_item, this, false));
		mViewList.add(inflater.inflate(R.layout.pager_item, this, false));
		mViewList.add(inflater.inflate(R.layout.pager_item, this, false));
		
		
		View currentView = inflater.inflate(R.layout.pager_item, this, false);
		addView(currentView);
	}
	
	private class PagePanel extends ViewGroup {

		public PagePanel(Context context) {
			super(context);
		}
		
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		
		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			if (!changed) {
				return ;
			}
		}
		
		public void setSpacing(int spacing) {
			
		}
	}
	
}
