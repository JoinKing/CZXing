package me.devilsen.czxing.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import me.devilsen.czxing.camera.CameraSurface;

/**
 * @author : dongSen
 * date : 2019-06-29 15:35
 * desc :
 */
class BarCoderView extends LinearLayout implements Camera.PreviewCallback {

    private static final int NO_CAMERA_ID = -1;

    protected Camera mCamera;
    private CameraSurface mCameraSurface;
    protected int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    private ScanListener mScanListener;

    public BarCoderView(Context context) {
        this(context, null);
    }

    public BarCoderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarCoderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        setBackground(null);
        mCameraSurface = new CameraSurface(context);

        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mCameraSurface, params);

        setOneShotPreviewCallback();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    public void setScanListener(ScanListener listener) {
        mScanListener = listener;
    }

    public void openCamera() {
        openCamera(mCameraId);
    }

    public void startScan() {
        openCamera();
        setOneShotPreviewCallback();
    }

    public void stopScan() {
        if (mCamera == null) {
            return;
        }
        try {
            mCamera.setOneShotPreviewCallback(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void openCamera(int cameraFacing) {
        if (mCamera != null || Camera.getNumberOfCameras() == 0) {
            return;
        }

        int ultimateCameraId = findCameraIdByFacing(cameraFacing);
        if (ultimateCameraId != NO_CAMERA_ID) {
            startCameraById(ultimateCameraId);
            return;
        }

        if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            ultimateCameraId = findCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } else if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            ultimateCameraId = findCameraIdByFacing(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (ultimateCameraId != NO_CAMERA_ID) {
            startCameraById(ultimateCameraId);
        }
    }

    private int findCameraIdByFacing(int cameraFacing) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraId = 0; cameraId < Camera.getNumberOfCameras(); cameraId++) {
            try {
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    return cameraId;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return NO_CAMERA_ID;
    }

    private void startCameraById(int cameraId) {
        try {
            mCameraId = cameraId;
            mCamera = Camera.open(cameraId);
            mCameraSurface.setCamera(mCamera);
        } catch (Exception e) {
            e.printStackTrace();
            if (mScanListener != null) {
                mScanListener.onOpenCameraError();
            }
        }
    }

    /**
     * 添加摄像头获取图像数据的回调
     */
    private void setOneShotPreviewCallback() {
        if (!mCameraSurface.isPreviewing()) {
            return;
        }

        try {
            mCamera.setOneShotPreviewCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭摄像头预览
     */
    public void closeCamera() {
        try {
            if (mCamera != null) {
                mCameraSurface.stopCameraPreview();
                mCameraSurface.setCamera(null);
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        stopScan();
        closeCamera();
        mScanListener = null;
    }

}