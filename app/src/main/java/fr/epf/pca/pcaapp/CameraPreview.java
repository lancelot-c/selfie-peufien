package fr.epf.pca.pcaapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private Context mContext;
    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private boolean ReconnaissanceEnCours = false;
    private Camera.Face[] visages;// = new Camera.Face[0];
    private TextView bouton;

    // Constructor that obtains context and camera
    @SuppressWarnings("deprecation")
    public CameraPreview(Activity activity, Context context, Camera camera) {
        super(context);
        this.mContext = context;
        this.mCamera = camera;

        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        bouton = (TextView)activity.findViewById(R.id.textView);
        bouton.setOnClickListener(clicBouton);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();

            mCamera.setFaceDetectionListener(faceDetectionListener);
            mCamera.startFaceDetection();
        } catch (IOException e) {
            Toast.makeText(mContext, "Erreur lors de la création de la preview", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        // start preview with new settings
        try {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            Camera.Size selected = sizes.get(0);
            params.setPreviewFrameRate(20);
            params.setPreviewSize(selected.width,selected.height);
            mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Toast.makeText(mContext, "Erreur lors de la mise à jour de la preview", Toast.LENGTH_LONG).show();
        }
    }

    private Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener() {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (ReconnaissanceEnCours)
                return;

            switch (faces.length)
            {
                case 0:
                    bouton.setText("Aucun visage détecté");
                    break;
                default:
                    bouton.setText("Lancer la reconnaissance");
            }

            visages = faces;
        }
    };

    private OnClickListener clicBouton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (visages.length == 0 || ReconnaissanceEnCours)
                return;

            mCamera.takePicture(null, null, mPicture);

            //mCamera.stopFaceDetection();
            //mCamera.stopPreview();
            ReconnaissanceEnCours = true;
            bouton.setText("Reconnaissance en cours...");
        }
    };

    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();

            //Toast.makeText(mContext, pictureFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            if (pictureFile == null) {
                return;
            }
            try {
                // Convert bite array to Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                // Applying trimming and rotation
                Matrix matrix = new Matrix();
                matrix.postRotate(270);

                Rect visage = visages[0].rect;
                int a = (visage.left+1000)*width/2000;
                int b = (visage.top+1000)*height/2000;
                int c = (visage.right+1000)*width/2000;
                int d = (visage.bottom+1000)*height/2000;

                int h = c-a;
                int w = d-b;

                bitmap = Bitmap.createBitmap(bitmap, a, b, w, h, matrix, true);

                /**********************
                // Convert to grayscale and creating new Visage
                int pixels[] = new int[width*height];

                for (int colonne = 0;colonne < width;colonne++)
                    for (int ligne = 0;ligne < height;ligne++)
                        pixels[ligne+colonne*height] = toGrayscale(bitmap.getPixel(colonne, ligne));

                Visage v = new Visage(pixels);
                // *********************/

                // Convert to grayscale
                bitmap = toGrayscale(bitmap);

                // Convert Bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                data = stream.toByteArray();

                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Toast.makeText(mContext, "Sauvegarde réussie", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                Toast.makeText(mContext, "Fichier non trouvé", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(mContext, "Erreur inconnue", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PCA");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Toast.makeText(mContext, "Impossible de créer le dossier", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public int toGrayscale(int c)
    {
        return (int)(0.21*(double)Color.red(c) + 0.72*(double)Color.green(c) + 0.07*(double)Color.blue(c));
    }
}