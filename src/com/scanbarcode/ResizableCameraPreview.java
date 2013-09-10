package com.scanbarcode;

import java.util.List;

import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * CameraPreview class that is extended only for the purpose of testing CameraPreview class.
 * This class is added functionality to set arbitrary preview size, and removed automated retry function to start preview on exception.
 */
public class ResizableCameraPreview extends CameraPreview {
    private static boolean DEBUGGING = true;
    private static final String LOG_TAG = "ResizableCameraPreviewSample";
    
    private TextView mTextViewScan;
    private Button mButtonScan;
    private boolean barcodeScanned = true;

    /**
     * @param activity
     * @param adjustByAspectRatio
     * @param addReversedSizes is set to true to add reversed values of supported preview-sizes to the list.
     */
    public ResizableCameraPreview(Activity activity, Button buttonScan, TextView textViewScan, int cameraId, LayoutMode mode, boolean addReversedSizes) {
        super(activity, cameraId, mode);
        
        mTextViewScan = textViewScan;
        mButtonScan = buttonScan;
        if (addReversedSizes) {
            List<Camera.Size> sizes = mPreviewSizeList;
            int length = sizes.size();
            for (int i = 0; i < length; i++) {
                Camera.Size size = sizes.get(i);
                Camera.Size revSize = mCamera.new Size(size.height, size.width);
                sizes.add(revSize);
            }
        }
        
        mButtonScan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startScan();
			}
		});
    }
    

    
	final PreviewCallback previewCallback = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				camera.setPreviewCallback(null);
				camera.stopPreview();
				mButtonScan.setVisibility(View.VISIBLE);
				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					mTextViewScan.setText("barcode result " + sym.getData());
					barcodeScanned = true;
				}
			}
		}
	};

	
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        mCamera.stopPreview();
        
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();

        if (!mSurfaceConfiguring) {
            Camera.Size previewSize = determinePreviewSize(portrait, width, height);
            Camera.Size pictureSize = determinePictureSize(previewSize);
            if (DEBUGGING) { Log.v(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height); }
            mPreviewSize = previewSize;
            mPictureSize = pictureSize;
            mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (mSurfaceConfiguring) {
                return;
            }
        }

        configureCameraParameters(cameraParams, portrait);
        mSurfaceConfiguring = false;

    }

	public void startScan() {
		if (barcodeScanned) {
			barcodeScanned = false;
			mTextViewScan.setText("Scanning...");
			mButtonScan.setVisibility(View.GONE);
			try {
				// Setup auto-focus
				
				Camera.Parameters params = mCamera.getParameters();
				params.setFocusMode("continuous-picture");
				mCamera.setParameters(params);
				mCamera.setPreviewCallback(previewCallback);
				mCamera.startPreview();
			} catch (Exception e) {
				Toast.makeText(mActivity,
						"Failed to start preview: " + e.getMessage(),
						Toast.LENGTH_LONG).show();
				Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());
			}
		}
	}
    
	
    public void setPreviewSize(int index, int width, int height) {
        mCamera.stopPreview();
        
        Camera.Parameters cameraParams = mCamera.getParameters();
        boolean portrait = isPortrait();
        
        Camera.Size previewSize = mPreviewSizeList.get(index);
        Camera.Size pictureSize = determinePictureSize(previewSize);
        if (DEBUGGING) { Log.v(LOG_TAG, "Requested Preview Size - w: " + previewSize.width + ", h: " + previewSize.height); }
        mPreviewSize = previewSize;
        mPictureSize = pictureSize;
        boolean layoutChanged = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
        if (layoutChanged) {
            mSurfaceConfiguring = true;
            return;
        }

        configureCameraParameters(cameraParams, portrait);
        try {
        	mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mActivity, "Failed to satart preview: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mSurfaceConfiguring = false;

    }

}
