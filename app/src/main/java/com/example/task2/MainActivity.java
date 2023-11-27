package com.example.task2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.text.DecimalFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_IMAGE_CAPTURE = 300;
    Button button_cam;
    InputImage input_img;
    FaceDetector faceDet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        button_cam = findViewById(R.id.open_cam);
        button_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(MainActivity.this,"Camera Not Working",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK || requestCode == 121) {

            Bundle extra = data.getExtras();
            Bitmap bitmap = (Bitmap) extra.get("data");
            detectFace(bitmap);
        }
    }

    private  void detectFace(Bitmap bitmap) {
        FaceDetectorOptions fdOptions =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .enableTracking()
                        .build();

        try {
            input_img = InputImage.fromBitmap(bitmap,0);
            faceDet = FaceDetection.getClient(fdOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        faceDet.process(input_img).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(@NonNull List<Face> faces) {
                String resultText = " ";
                int i = 0;
                DecimalFormat df = new DecimalFormat("#.00");
                for (Face face:faces) {

                    resultText = resultText.concat("\n\nFACE NUMBER " + i)
                            .concat(
                                    "\nSmile:   "
                                            + (df.format(face.getSmilingProbability()
                                            * 100))
                                            + "%")
                            .concat(
                                    "\nLeft Eye:  "
                                            + (df.format(face.getLeftEyeOpenProbability()
                                            * 100))
                                            + "%")
                            .concat(
                                    "\nRight Eye:  "
                                            + (df.format(face.getRightEyeOpenProbability()
                                            * 100))
                                            + "%");
                    i++;
                }

                resultText = resultText.concat("\n\n"+i+" Faces Detected");
                if (faces.size() == 0) {
                    Toast.makeText(MainActivity.this, "NO FACES DETECTED", Toast.LENGTH_LONG).show();


                }else {
                    Bundle bundle = new Bundle();
                    bundle.putString(FaceDetectionActivity.RESULT_TEXT,resultText);
                    DialogFragment resultDialog = new FaceResultActivity();
                    resultDialog.setArguments(bundle);
                    resultDialog.setCancelable(false);
                    resultDialog.show(getSupportFragmentManager(), FaceDetectionActivity.RESULT_DIALOG);

                }
            }
        });
    }

}
