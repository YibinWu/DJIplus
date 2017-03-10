package com.example.djiplus;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import dji.common.camera.CameraSystemState;
import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.camera.DJICamera;
import dji.sdk.camera.DJICamera.CameraReceivedVideoDataCallback;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class MainActivity extends Activity {
    //一个类实现多个接口，需实现接口中的所有方法？
    private static final String TAG=MainActivity.class.getName();
    private Button start_but,save_but;
    private EditText save_file;
    private TextView show_text;
    private final String FOLDER="/DJI_FlightData/";
    private final String SUFFIX=".txt";
    private ArrayList<String> data=new ArrayList<String>();
    File targetFile;

    private DJIFlightController myFlightController;
    private DJIBaseProduct mProduct = DJISDKManager.getInstance().getDJIProduct();

    double altitude=0;
    double Latitude=0;
    double Longitude= 0;
    double vx = 0;
    double vy = 0;
    double vz = 0;
    float myheight=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    private void initUI() {
        start_but=(Button)findViewById(R.id.Start_button);
        save_but=(Button)findViewById(R.id.Save_button);
        save_file=(EditText)findViewById(R.id.Flight_file);
        show_text=(TextView)findViewById(R.id.show_view);


        start_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        initPreviewer();
                    }
                });
                Log.e(TAG,"get data");
                start_but.setEnabled(false);

            }
        });
        save_but.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (save_file.getText().toString().length()==0){
                    showToast("Please input the file name");
                }
                else {
                    createFile(save_file.getText().toString());
                    writeFile(data.toString(),targetFile);
                    Toast.makeText(getApplicationContext(),"Saved Successfully",Toast.LENGTH_SHORT).show();
                    data.clear();
                    //save_file.setText("");
                    start_but.setEnabled(true);
                    show_text.setText("");
                    show_text.setEnabled(false);
                }
            }
        });
    }

    private void initPreviewer() {

        DJIBaseProduct product = DJIplusApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        }
        if (mProduct!=null&&mProduct.isConnected()){
            if (mProduct instanceof DJIAircraft){
                myFlightController=((DJIAircraft)mProduct).getFlightController();
                showToast(mProduct.toString()+" is connected");
            }
        }
        if (myFlightController!=null){
            myFlightController.setUpdateSystemStateCallback(new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {
                @Override
                public void onResult(DJIFlightControllerCurrentState djiFlightControllerCurrentState) {
                    altitude = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();
                    Latitude = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    Longitude= djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    vx = djiFlightControllerCurrentState.getVelocityX();
                    vy = djiFlightControllerCurrentState.getVelocityY();
                    vz = djiFlightControllerCurrentState.getVelocityZ();
                    myheight=djiFlightControllerCurrentState.getUltrasonicHeight();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            show_text.setText("myheight:"+myheight+"\n"+"altitude:"+altitude+"\n"+"Longitude:"+Longitude+"\n"+"Latitude:"+Latitude+"\n"+"Vx:"+vx);
                            data.add(altitude+","+Latitude+","+Longitude+","+vx+","+vy+","+vz);
                        }
                    });
                    }
            });
        }
    }

    public void createFile(String name) {
        // 在SD卡中创建文件夹
        String foldername = Environment.getExternalStorageDirectory().getPath()
                + FOLDER;
        File folder = new File(foldername);
        if (folder != null && !folder.exists()) {
            if (!folder.mkdir() && !folder.isDirectory()) {
                Toast.makeText(MainActivity.this, "ERROR:Create File Failed", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // 创建.txt文件
        String targetPath = foldername + name + SUFFIX;
        targetFile = new File(targetPath);
        if (targetFile != null) {
            if (targetFile.exists()) {
                targetFile.delete();
            }
        }
    }
    public static void writeFile(String stringToWrite, File filePath) {
        // 写入文本文件
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath, true);
            fos.write(stringToWrite.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
