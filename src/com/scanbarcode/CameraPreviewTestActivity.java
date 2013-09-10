
package com.scanbarcode;

import com.crashlytics.android.Crashlytics;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CameraPreviewTestActivity extends Activity{
    private ResizableCameraPreview mPreview;
    private RelativeLayout mLayout;
    private int mCameraId = 0;
    private TextView mTextViewScan;
    private Button mButtonScan;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Crashlytics.start(this);
		setContentView(R.layout.test);
        
        mLayout = (RelativeLayout) findViewById(R.id.layout);
        mTextViewScan = ( TextView) findViewById(R.id.textViewScan);
        mButtonScan = (Button) findViewById(R.id.buttonScan);
       
    }

    @Override
    protected void onResume() {
        super.onResume();
        createCameraPreview();
        mPreview.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
        mLayout.removeView(mPreview);
        mPreview = null;
    }
    
    @Override
    protected void onDestroy() {
    	if (mPreview != null) {
    		mPreview.releaseCamera();
    	}
    	
    	super.onDestroy();
    }
    private void createCameraPreview() {

        mPreview = new ResizableCameraPreview(this, mButtonScan, mTextViewScan, mCameraId, CameraPreview.LayoutMode.FitToParent, false);
        LayoutParams previewLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        mLayout.addView(mPreview, 0, previewLayoutParams);

    	Rect rect = new Rect();
    	mLayout.getDrawingRect(rect);
    	mPreview.setPreviewSize(0, rect.width(), rect.height());
    }
    
}
