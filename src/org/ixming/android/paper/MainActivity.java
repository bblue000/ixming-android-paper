package org.ixming.android.paper;

import org.ixming.android.inject.InjectorUtils;
import org.ixming.android.inject.annotation.ViewInject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

	@ViewInject(id = R.id.root)
	private PagerContainer root;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		InjectorUtils.defaultInstance().inject(this);
		
		
		root.setPagerAdapter(new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return 10;
			}
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				TextView view = new TextView(MainActivity.this);
				view.setText("" + (position + 1));
				view.setBackgroundColor((0x0000FF << (position * 2)) | 0xFF << 24);
				container.addView(view, 0);
				return view;
			}
			
			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((View)object);
			}
			
		});
	}

}
