package com.example.myapplication;

import static java.lang.Character.isDigit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.*;


public class MainActivity extends AppCompatActivity {
    Button button;
    TextView textView;
    TextRecognizer recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(bitmap);
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            Task<Text> result =
                    recognizer.process(image)
                            .addOnSuccessListener(new OnSuccessListener<Text>() {
                                @Override
                                public void onSuccess(Text visionText) {
                                    System.out.println(visionText.getText().split("\n")[0]);
                                    String res = parseInformation(new ArrayList<String>(Arrays.asList(visionText.getText().toLowerCase().split("\n"))));
                                    System.out.println(res);
                                    textView.setText(res);

                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });

        }
    }


    public String parseInformation(List<String> info) {
        String s = "";
        if (info.size() == 0) {
            return "Error when parsing! Try again";
        }

        if (info.get(0).equals("p")) {
            if (info.contains("avgift")) {
                s += "Fee\n";
            }
            String maxHours = getMaxHours(info);
            if (maxHours.length() > 0) {
                s += maxHours;
            }
            List<String> parkingTimes = new ArrayList<>();
            for (int i = 0; i < info.size(); i++) {
                char chs[] = info.get(i).toCharArray();
                for (int j = 0; j < chs.length - 2; j++) {
                    if (isDigit(chs[j]) && chs[j + 1] == '-' && isDigit(chs[j + 2])) {
                        parkingTimes.add(new String(chs));
                    }
                }
            }
            String parsedTimes = parseParkingTimes(parkingTimes);
            s += parsedTimes;
        } else {
            if info.contains("Boende"){
                s += "Boendeparkering";
            }
            if (info.get(0).equals("parkering förbjuden")) {
                return "Parking is not allowed.";
            }
        }
        return s;
    }

    public String getMaxHours(List<String> info) {
        for (String s : info) {
            char chs[] = s.replaceAll(" ", "").toLowerCase().toCharArray();
            if (isDigit(chs[0]) && chs[1] == 't' && chs[2] == 'i' && chs[3] == 'm') {
                return "Maximum " + chs[0] + " hours\n";
            }
        }
        return "";
    }

    public String parseParkingTimes(List<String> times) {
        String parsedTimes = "";
        if (times.size() == 0) {
            return "No times found.";
        } else if (times.size() == 1) {
            if (times.get(0).startsWith("(")) {
                parsedTimes += "Mon-Fri: Not allowed\n";
                parsedTimes += "Sat: " + times.get(0) + "\n";
                parsedTimes += "Sun: Not allowed";
            } else {
                parsedTimes += "Mon-Fri: " + times.get(0) + "\n";
                parsedTimes += "Sat: Not allowed\n";
                parsedTimes += "Sun: Not allowed";
            }

        } else if (times.size() == 2) {
            if (times.get(0).startsWith("(")) {
                parsedTimes += "Mon-Fri: Not allowed\n";
                parsedTimes += "Sat: " + times.get(0) + "\n";
                parsedTimes += "Sun: " + times.get(1);
            } else if (times.get(1).startsWith("(")) {
                parsedTimes += "Mon-Fri: " + times.get(0) + "\n";
                parsedTimes += "Sat: " + times.get(1) + "\n";
                parsedTimes += "Sun: Not allowed";
            } else {
                parsedTimes += "Mon-Fri: " + times.get(0) + "\n";
                parsedTimes += "Sat: Not allowed\n";
                parsedTimes += "Sun: " + times.get(1);
            }
        } else if (times.size() == 3) {
            parsedTimes += "Mon-Fri: " + times.get(0) + "\n";
            parsedTimes += "Sat: " + times.get(1) + "\n";
            parsedTimes += "Sun: " + times.get(2);
        }
        System.out.println(parsedTimes);
        return parsedTimes;
    }

}