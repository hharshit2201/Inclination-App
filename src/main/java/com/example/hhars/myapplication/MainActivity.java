package com.example.hhars.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    public boolean stop = false;
    public File folder;
    static public final int permissionCheck=2;
    public Runnable r = new Runnable() {
        @Override
        public void run() {

            while (stop == false) {
                Log.v("tag", "in loop");
//                Toast.makeText(MainActivity.this,"runnable", Toast.LENGTH_SHORT).show();
                if (fos != null) {
                    Long timenow = Long.valueOf(0), prev_time;
                    prev_time = System.currentTimeMillis();
                    //loop provides 3ms delay for writing
                    while (System.currentTimeMillis() != prev_time + 100) {
                    }
                    timenow = System.currentTimeMillis();
                    String dd = recent_lightvalue + "," + recent_pitchvalue + "\n";
                    try {
                        Log.v("tag","writing");
                        fos.write(dd.getBytes());
                    } catch (IOException e) {
                        Log.v("tag","errorfound");
                        e.printStackTrace();
                    }

                }

                if (stop == true) {
                    Log.v("tag", "stopped");
                    break;
                }
            }
        }
    };

    public SensorManager sManager;
    public SensorManager lightsense;
    public LocationManager lm;
    public Sensor lsensor, asensor, msensor,locsensor;
    public Button start;
    public TextView lightview, pitchview, bearingview,latview,lonview;
    public Thread newthread;
    public FileOutputStream fos;
    public Double recent_lightvalue = 0.0;
    public Double recent_pitchvalue = 0.0;
    public float bearTo;
    public int counter=0;
    public ToneGenerator toneGen1;

    float I[] = null; //for magnetic rotational data
    float mGeomagnetic[] = new float[3];
    float mGravity[] = new float[3];
    float[] values = new float[3];
    float Rot[] = null;
    double pitch;

    double latitude, longitude, lastlatitude, lastlongitude;
    double bearing;
    double distance = 0;


    public SensorEventListener pit = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                mGravity = event.values;

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                mGeomagnetic = event.values;

            if (mGravity != null && mGeomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    pitch = orientation[1];
                    pitch = Math.toDegrees(pitch);
                    pitch = Double.parseDouble(String.format("%.0f",pitch));
                    recent_pitchvalue = -1*pitch;
                    pitch = -1*pitch;

//                    //BEEP SOUND AFTER 30 DEGREE
//                    if (pitch>30.0 || pitch<-5.0){
//                        toneGen1.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150);
//                    }
//                    //

                    pitchview.setText(recent_pitchvalue+"");
                    //Log.v("pitch ",pitch+"");

                }
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public LocationListener loc = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (lastlatitude != 0 && lastlongitude != 0 && latitude != 0 && longitude != 0)
            {
                Location location1 = new Location(""), location2 = new Location("");
                location1.setLatitude(latitude);
                latview.setText(latitude+"");
                location1.setLongitude(longitude);
                lonview.setText(longitude+"");
                location2.setLatitude(lastlatitude);
                location2.setLongitude(lastlongitude);
                if (location2.distanceTo(location1) > 1)
                {
                    bearing = location2.bearingTo(location1);
                    bearingview.setText(bearing+"");
                    if (latitude != lastlatitude || longitude != lastlongitude)
                    {
                        distance = distance + location2.distanceTo(location1);
                    }
                    lastlatitude = latitude;
                    lastlongitude = longitude;
                }

            }
            else
            {
                lastlatitude = latitude;
                lastlongitude = longitude;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };









    public SensorEventListener light = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            //Toast.makeText(MainActivity.this, "light value obtained", Toast.LENGTH_SHORT).show();
            recent_lightvalue = Double.valueOf(event.values[0]);
            lightview.setText(recent_lightvalue + " lux");

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightsense = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lsensor = lightsense.getDefaultSensor(Sensor.TYPE_LIGHT);
        asensor = sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        msensor = sManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        start = (Button) findViewById(R.id.start);
        lightview = (TextView) findViewById(R.id.lightview);
        pitchview = (TextView) findViewById(R.id.pitchview);
        bearingview = (TextView) findViewById(R.id.bearingview);
        latview  = (TextView) findViewById(R.id.latview);
        lonview = (TextView) findViewById(R.id.lonview);
        newthread=new Thread(r);
//        toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 80);
        lm = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},permissionCheck);
        }

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "First enable LOCATION ACCESS in settings.", Toast.LENGTH_LONG).show();
            return;
        }

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,1, (LocationListener) loc);




        File folder= new File(Environment.getExternalStorageDirectory(),"/Sensordata");
        if(folder.exists())
        {
            Toast.makeText(this, "folder already there", Toast.LENGTH_SHORT).show();
        }

        else {
            final boolean flag = folder.mkdirs();
            if (flag) {
                try {
                    File newfile = new File(folder, "pitchdata.csv");
                    if (newfile.exists())
                        Toast.makeText(this, "file already there", Toast.LENGTH_SHORT).show();
                    else {
                        newfile.createNewFile();
                        Toast.makeText(this, "pitchdata.csv created", Toast.LENGTH_SHORT).show();
                        fos = new FileOutputStream(newfile);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "gpsdata.txt not created", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onResume(){
        super.onResume();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter==0) {
                    lightsense.registerListener(light,lsensor,SensorManager.SENSOR_DELAY_NORMAL);
                    sManager.registerListener(pit, asensor, SensorManager.SENSOR_DELAY_NORMAL);
                    sManager.registerListener(pit, msensor, SensorManager.SENSOR_DELAY_NORMAL);

                    //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000,1, (LocationListener) loc);
                    Log.v("tag","clicked");
                    counter=1;
                    newthread.start();
                    start.setText("STOP");
                }
                else
                {
                    lightsense.unregisterListener(light);
                    sManager.unregisterListener(pit);
                    counter=0;
                    start.setText("START");
                    stop=true;
                }
            }
        });
    }

    @Override
    protected void onDestroy(){
        try {
            fos.close();
            Toast.makeText(this, "gpsdata.txt closed", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            //e.printStackTrace();
            super.onDestroy();
        }
        super.onDestroy();
    }

}
