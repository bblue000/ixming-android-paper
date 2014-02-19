package org.ixming.android.paper;

import org.ixming.android.inject.InjectorUtils;
import org.ixming.android.inject.annotation.ViewInject;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity2 extends Activity {

	@ViewInject(id = R.id.root)
	private PagerViewContainer root;
	@ViewInject(id = R.id.shadow_view)
	private View shadowView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		InjectorUtils.defaultInstance().inject(this);
		
		final Drawable drawable = shadowView.getBackground();
		
		root.setOnScaleChangedListener(new PageViewScaleChangedListener() {
			
			@Override
			public void onScaleChanged(float newScale, float oldScale) {
				if (newScale > 1.0F) {
					newScale = 1.0F;
				}
				drawable.setAlpha((int) (0xAA * newScale));
			}
		});
	}

}
