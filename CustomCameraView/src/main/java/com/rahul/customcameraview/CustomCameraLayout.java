package com.rahul.customcameraview;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.AttributeSet;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Collections;

public class CustomCameraLayout extends FrameLayout {

    private TextureView cameraPreview;
    private FrameLayout overlayContainer;
    private Button captureButton;


    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;

    private String cameraId;

    private CaptureCallback captureCallback;


    public CustomCameraLayout(@NonNull Context context) {
        super(context);
    }

    public CustomCameraLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomCameraLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                              int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomCameraLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                              int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public interface CaptureCallback {
        void onFullScreenBitmap(Bitmap bitmap);
    }


    public void init(Context context, CaptureCallback captureCallback, View overlayView) {
        LayoutInflater.from(context).inflate(R.layout.custom_camera_view, this, true);

        cameraPreview = findViewById(R.id.camera_preview);
        overlayContainer = findViewById(R.id.overlayContainer);
        captureButton = findViewById(R.id.btn_capture);

        cameraPreview.setSurfaceTextureListener(textureListener);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        this.captureCallback = captureCallback;

        if (overlayView!=null) {
            overlayContainer.addView(overlayView);
        }

    }


    private final TextureView.SurfaceTextureListener textureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width,
                                                      int height) {
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width,
                                                        int height) {
                    // Transform your image captured size according to the surface width and height
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                }
            };

    private void openCamera() {
        CameraManager manager =
                (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = manager.getCameraIdList();
            if (cameraIdList.length==0) {
                // Handle no available camera
                return;
            }
            cameraId = cameraIdList[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] cameraSizes = map!=null ? map.getOutputSizes(SurfaceTexture.class):null;
            Size cameraSize = cameraSizes!=null && cameraSizes.length > 0 ? cameraSizes[0]:null;
            configureTransform(cameraPreview.getWidth(), cameraPreview.getHeight(), cameraSize);

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED) {
                return;
            }

            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            assert texture!=null;
            texture.setDefaultBufferSize(cameraPreview.getWidth(), cameraPreview.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice==null) return;
                            cameraCaptureSessions = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            // Handle failures
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (cameraDevice==null) return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configureTransform(int viewWidth, int viewHeight, Size cameraSize) {
        // Optional: Handle rotation and camera aspect ratio
        // You can implement the necessary transformations here based on the provided parameters
        // This is a placeholder method, so you would need to add the actual implementation
        // according to your requirements
    }

    private void takePicture() {
        if (cameraDevice==null) return;

        try {
            CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            assert texture!=null;
            texture.setDefaultBufferSize(cameraPreview.getWidth(), cameraPreview.getHeight());
            Surface surface = new Surface(texture);
            captureBuilder.addTarget(surface);

            cameraCaptureSessions.capture(
                    captureBuilder.build(),
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(
                                CameraCaptureSession session,
                                CaptureRequest request,
                                TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            Bitmap bmp = cameraPreview.getBitmap();
                            captureCallback.onFullScreenBitmap(bmp);
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public Button getCaptureButton() {
        return captureButton;
    }

}