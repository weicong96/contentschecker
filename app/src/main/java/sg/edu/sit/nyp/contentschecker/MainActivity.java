package sg.edu.sit.nyp.contentschecker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import models.Rotation;


public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private double lastValue;
    private Camera camera;


    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(),"You Are Not Allowed to Exit the App", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME){
            Toast.makeText(getApplicationContext(), "You Are Not Allowed to Exit the App", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        camera = null;
        try{
            int cameraId = -1;
            // Search for the front facing camera
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Log.d("Camera", "Camera found");
                    cameraId = i;
                    break;
                }
            }
            camera = Camera.open(cameraId);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
        }catch(Exception e){
            //cannot get camera
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement1
        if (id == R.id.action_settings) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Password Please");
            alert.setMessage("Enter your password");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    // Do something with value!
                    if(value.equals("weicong")) {
                        DBHelper helper = new DBHelper(MainActivity.this);
                        ArrayList<Rotation> list = helper.getAll();
                        for(Rotation r : list){
                            new PostDetailsTask().execute(r);
                        }

                        startActivity(new Intent(MainActivity.this, PreferenceActivity.class));
                    }
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println(Math.abs(event.values[2] - lastValue));
       if((Math.abs(event.values[2] - lastValue) > Double.parseDouble(prefs.getString("minvalue_record", "0")))){
           System.out.println(event.values[0]+ " , "+event.values[1] + " , "+event.values[2]);
           takePic(event.values[2]);
       }
        this.lastValue = event.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void takePic(final double _newValue){
        final double newValue = _newValue;
        if(camera == null){
            try{
                int cameraId = -1;
                // Search for the front facing camera
                int numberOfCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Log.d("Camera", "Camera found");
                        cameraId = i;
                        break;
                    }
                }
                camera = Camera.open(cameraId);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
            }catch(Exception e){
                //cannot get camera
                e.printStackTrace();
            }
        }
        camera.startPreview();
        Camera.PictureCallback mPicture = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File mediaStorageDir = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "MyCameraApp");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
                    }
                }
                // Create a media file name
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());
                File mediaFile;
                mediaFile = new File(mediaStorageDir.getPath() + File.separator
                        + "IMG_" + timeStamp + ".jpg");

                if(mediaFile == null){
                    return;
                }

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(mediaFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Rotation r = new Rotation();
                r.setChange(Math.abs(_newValue - lastValue));
                r.set_new(_newValue);
                r.setLast(lastValue);
                r.setPhoto("IMG_" + timeStamp + ".jpg");
                r.setTime(new Date().getTime());

                new PostDetailsTask().execute(r);
                Toast.makeText(MainActivity.this, "Picture taken!", Toast.LENGTH_LONG).show();
                if(camera != null) {
                    MainActivity.this.camera.stopPreview();
                    MainActivity.this.camera.release();
                    MainActivity.this.camera = null;
                }
            }
        };
        camera.takePicture(null, null, mPicture);
    }

    class PostDetailsTask extends AsyncTask<Rotation, Void, Rotation> {

        @Override
        protected Rotation doInBackground(Rotation... params) {
            final Rotation r = params[0];
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost post = new HttpPost("http://weicong.chickenkiller.com:3001/rotation");
                post.setHeader("Content-Type", "application/json");

                JSONObject object2 = new JSONObject();
                object2.put("last", String.valueOf(r.getChange()));
                object2.put("_new", String.valueOf(r.get_new()));
                object2.put("change", String.valueOf(r.getChange()));
                object2.put("time", String.valueOf(r.getTime()));

                post.setEntity(new StringEntity(object2.toString()));

                HttpResponse res = httpclient.execute(post);
                String response = EntityUtils.toString(res.getEntity(), "UTF-8");
                System.out.println(response);
                JSONObject object = new JSONObject(response);

                double last = object.getDouble("last");
                double _new = object.getDouble("_new");
                double change = object.getDouble("change");
                long time = object.getLong("time");

                Rotation res_r = new Rotation();
                res_r.setLast(last);
                res_r.set_new(_new);
                res_r.setChange(change);
                res_r.setTime(time);

                return res_r;
            } catch (Exception e){

                //insert if fails
                DBHelper helper = new DBHelper(MainActivity.this);
                helper.insertRotation(r);
            }
            return null;
        }
    }
}
