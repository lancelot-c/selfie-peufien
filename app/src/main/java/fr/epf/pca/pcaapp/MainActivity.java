package fr.epf.pca.pcaapp;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCamera = getCameraInstance(1);
        mCameraPreview = new CameraPreview(this, getApplicationContext(), mCamera);
        mCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1240));

        LinearLayout layout = (LinearLayout)findViewById(R.id.linear_layout);
        layout.addView(mCameraPreview, 0);
    }

    private Camera getCameraInstance(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {
            Toast.makeText(this, "Impossible d'accéder à la caméra", Toast.LENGTH_LONG).show();
        }
        return camera;
    }
}