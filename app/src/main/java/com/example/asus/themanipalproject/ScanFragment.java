package com.example.asus.themanipalproject;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import static android.content.Context.VIBRATOR_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScanFragment extends Fragment {
    SurfaceView sv;

    TextView tv;
    Vibrator myVib;
    private BarcodeDetector bd;
    private CameraSource cs;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    String intentData = "";
    int ad=0;
    int succ=0;


    public ScanFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v=inflater.inflate(R.layout.fragment_scan, container, false);
        tv = v.findViewById(R.id.tv);
        sv = v.findViewById(R.id.surfaceView);
        myVib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);




        initialiseDetectorsAndSources();



        return v;
    }

    private void initialiseDetectorsAndSources() {

        //   Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        bd = new BarcodeDetector.Builder(getActivity())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cs = new CameraSource.Builder(getActivity(), bd)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cs.start(sv.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //    Toast.makeText(getActivity(),"surfaceChng",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cs.stop();
             //   Toast.makeText(getActivity(),"surfaceDest",Toast.LENGTH_SHORT).show();
            }
        });


        bd.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            //    Toast.makeText(getActivity(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {




                    tv.post(new Runnable(){@Override
                    public void run()
                    {
                        cs.stop();
                        if (barcodes.valueAt(0).displayValue.substring(0,3).equals("id:")) {
                            // tv.removeCallbacks(null);
                            if(succ==0)myVib.vibrate(50);
                            int buff=succ;
                            succ=1;
                            intentData = barcodes.valueAt(0).displayValue.replace("id:","");

                          //  tv.setText("success");

                            if(buff==0)
                          cs.stop();


                          Intent i = new Intent(getActivity(),CheckInActivity.class);
                          i.putExtra("RCode",Integer.parseInt(intentData));
                          startActivity(i);


                        } else {


                            //   intentData = barcodes.valueAt(0).displayValue;






                        //    tv.setText("error");

                            if(ad==0)
                            {
                                myVib.vibrate(500);
                                makeAlert();
                            }


                            ad=1;











                        }
                    }});

                }
            }
        });


    }

    @Override
    public void onPause() {
        super.onPause();
        cs.stop();
        bd.release();
    }

    @Override
    public void onResume() {
        super.onResume();

        initialiseDetectorsAndSources();


    }
    void makeAlert()
    {
        ad=0;
        succ=0;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("An Error Occured While Scannig\nKindly Try Again");

                alertDialogBuilder.setPositiveButton("Retry",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        cs.start(sv.getHolder());
                                    } else {
                                        ActivityCompat.requestPermissions(getActivity(), new
                                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });



        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

}
