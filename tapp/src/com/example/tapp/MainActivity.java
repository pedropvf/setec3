package com.example.tapp;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /* Sensor Management */
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        /* sound management */
        //okaysound = MediaPlayer.create(this, R.raw.okay);
        mplayerShake = MediaPlayer.create(this, R.raw.shakesoundwav);
        alarm1 = MediaPlayer.create(this, R.raw.alarma);
        alarm2 = MediaPlayer.create(this, R.raw.alarmb);
        deadManAlarm = MediaPlayer.create(this, R.raw.gameover);
        
        /* user interface */
        radioAccelerometer = (RadioButton) findViewById(R.id.radioAcc);
    	radioProximity = (RadioButton) findViewById(R.id.radioProx);
    	vibrateThreshold = senAccelerometer.getMaximumRange() / 2;
        //initialize vibration
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        
        dx = (TextView) findViewById(R.id.xdelta_edit);
        xAcc = (TextView) findViewById(R.id.xacc_edit);
        dy = (TextView) findViewById(R.id.ydelta_edit);
        yAcc = (TextView) findViewById(R.id.yacc_edit);
        dz = (TextView) findViewById(R.id.zdelta_edit);
        zAcc = (TextView) findViewById(R.id.zacc_edit);
        s = (TextView) findViewById(R.id.speed_edit);
        obs = (TextView) findViewById(R.id.obs_edit);
        obs2 = (TextView) findViewById(R.id.obs2_edit);
        
        if(senAccelerometer != null){
	        /*	More sensor speeds (taken from api docs)
		    		SENSOR_DELAY_FASTEST get sensor data as fast as possible
		    		SENSOR_DELAY_GAME	rate suitable for games
		 			SENSOR_DELAY_NORMAL	rate (default) suitable for screen orientation changes
	         */
	        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL); //mudei para normal
	        	        
	        trackDeadMan=false;
	        trackDeadMan2=false;
	        trackDeadMan3=false;
	        radioAccelerometer.setChecked(true);
        } else {
        	radioAccelerometer.setChecked(false);
        }
        if(senProximity != null){
        	senSensorManager.registerListener(this, senProximity , SensorManager.SENSOR_DELAY_NORMAL);
        	radioProximity.setChecked(true);
        } else {
        	radioProximity.setChecked(false);
        }
        
        gravity[0] = 0;
        gravity[1] = 0;
        gravity[2] = 0;

    }
    
    public void updateDeltas(float dX, float dY, float dZ){   
    	dx.setText((dX>=0.1)?(Float.toString(dX)):(Float.toString(0)));
    	dy.setText((dY>=0.1)?(Float.toString(dY)):(Float.toString(0)));
    	dz.setText((dZ>=0.1)?(Float.toString(dZ)):(Float.toString(0)));
    }
    
    public void updateAccelerations(float XA, float YA, float ZA){   
    	xAcc.setText(Float.toString(XA));
    	yAcc.setText(Float.toString(YA));
    	zAcc.setText(Float.toString(ZA));
    }
    
    public void clearDeltas(){
    	dx.setText("");
    	dy.setText("");
    	dz.setText("");
    }
    
    public void updateSpeed(float S){
    	//String sd=String.format("%f m", s);
    	s.setText(Float.toString(S));
    }
    
    public void clearSpeed(){
    	s.setText("");
    }
    
	@Override
	public void onSensorChanged(SensorEvent event) {
		Sensor mySensor = event.sensor;
		 
	    if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
	    	
	    	//high pass filter to filter out gravity
	    	// alpha is calculated as t / (t + dT)

	        final float alpha = (float) 0.8;

	        Log.d("tapp" , String.valueOf(gravity[0]));
	        
	        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
	        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
	        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

	        float x = event.values[0] - gravity[0];
	        float y = event.values[1] - gravity[1];
	        float z = event.values[2] - gravity[2];
	    	
	    	/*old code
	    	float x = (float) (event.values[0]);
	    	float y = (float) (event.values[1]);
	    	float z = (float) (event.values[2]);
	        */
	          
	        long curTime = System.currentTimeMillis();
	        
	        if ((curTime - lastUpdate) > SAMPLE_PERIOD_MILIS) {
	            long diffTime = (curTime - lastUpdate);
	            lastUpdate = curTime;
	            
	            float deltaX = x-last_x;
	            float deltaY = y-last_y;
	            float deltaZ = z-last_z;
	            float abs_acc = (float) Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
	            Log.d("teste" , "y = " + String.valueOf(deltaY));
	            float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;
	            
	            updateAccelerations(x,y,z);
	            updateDeltas(deltaX,deltaY,deltaZ);
	            updateSpeed(speed);
	            
	            //tap detector
	            if((curTime-lastimpulse)>2000){
	            	flagTap=0;
	            }

	            if((Math.abs(deltaZ)>1) && ((Math.abs(deltaX)<0.8) && (Math.abs(deltaY)<0.8))){
	            	if((curTime-lastimpulse)>200 || flagTap==0){
	            		flagTap++;
		            	lastimpulse = curTime;
		            	Log.d("coisa" ,  String.valueOf(flagTap) + " taps detected");
		            	if(flagTap==2){
	            			Log.d("coisa" , "OK message");
		            	}if(flagTap==3){
		            		Log.d("coisa" , "SOS message");
		            	}
	            	}
	            }
	            
	            
	            //shake detector
	            if ( abs_acc > SHAKE_THRESHOLD){
	            	obs.setText("Shake Detected!");
	            	v.vibrate(500);
	            	try{
	                    if(!(mplayerShake.isPlaying())){
	                    	mplayerShake.start();
	                    }
	                }catch(Exception e){
	                    Log.e("tapp" , "error: " + e);
	                }
	            }
	            else{
	            	obs.setText("");
	            }
	            
	            /* dead man */
	            if (abs_acc < DEAD_MAN_THRESHOLD){
	            	if(trackDeadMan==false){
	            		deadManPhase=1;
		            	trackDeadMan=true;
		            	startDeadManTime=System.currentTimeMillis();
	            	} else {
	            		currentDeadManTime=System.currentTimeMillis();
	            		if((currentDeadManTime-startDeadManTime)>=deadManTimeMilis){
	            			Log.d("teste_dead" , "entrou no dead man");
	            			if( (deadManPhase==1) && (trackDeadMan2==false) ){
	            				trackDeadMan2=true;
	    		            	startDeadManTime=System.currentTimeMillis();
	    		            	deadManPhase=2;
	            			} else if((deadManPhase==2) && (trackDeadMan3==false)){
	            				trackDeadMan3=true;
	    		            	startDeadManTime=System.currentTimeMillis();
	    		            	deadManPhase=3;
	            			}
	            			else if((deadManPhase==3) && (trackDeadMan3==true)){
	    		            	deadManPhase=4;
	            			}
	            		}
	            	}
	            } else {
	            	trackDeadMan=false;
	            	trackDeadMan2=false;
	            	trackDeadMan3=false;
	            	deadManPhase=0;
	            }
	            switch (deadManPhase){
	            	case 1:
	            		obs.setText("Careful..");
	            	break;
	            	case 2:
	            		if(!(alarm1.isPlaying())){
	            			alarm1.start();
	                    }
	            		obs.setText("Alert!");
	            	break;
	            	case 3:
	            		if((alarm1.isPlaying())){
	            			alarm1.pause();
	                    }
	            		if(!(alarm2.isPlaying())){
	            			alarm2.start();
	                    }
	            		obs.setText("Almost there...");
	            	break;
	            	case 4:
	            		if((alarm2.isPlaying())){
	            			alarm2.pause();
	                    }
	            		if(!(deadManAlarm.isPlaying())){
	            			deadManAlarm.start();
	                    }
	            		obs.setText("Dead... sorry :)");
	            	break;
	            	case 0:
	            		if((alarm1.isPlaying())){
	            			alarm1.pause();
	                    }
	            		if((alarm2.isPlaying())){
	            			alarm2.pause();
	                    }
	            		if((deadManAlarm.isPlaying())){
	            			deadManAlarm.pause();
	                    }
	            		obs.setText("");
	            	break;
	            }
	 
	            last_x = x;
	            last_y = y;
	            last_z = z;
	        }
	    }else if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
	     float prox = event.values[0];
	     
	     obs2.setText((prox==0.0f)?("Near"):("Far"));
	    }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	   /* senSensorManager.unregisterListener(this);
	    if((alarm1.isPlaying())){
			alarm1.pause();
        }
		if((alarm2.isPlaying())){
			alarm2.pause();
        }
		if((deadManAlarm.isPlaying())){
			deadManAlarm.pause();
        }*/
	}
	
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    senSensorManager.unregisterListener(this);
	    if((alarm1.isPlaying())){
			alarm1.pause();
        }
		if((alarm2.isPlaying())){
			alarm2.pause();
        }
		if((deadManAlarm.isPlaying())){
			deadManAlarm.pause();
        }
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	   // senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/* Class Variables */
	/* Sensor stuff */
	private SensorManager senSensorManager;
	private Sensor senAccelerometer;
	private Sensor senProximity;
	private RadioButton radioAccelerometer;
	private RadioButton radioProximity;
	
	/* sound */
	MediaPlayer okaysound;
	MediaPlayer mplayerShake;
	MediaPlayer alarm1;
	MediaPlayer alarm2;
	MediaPlayer deadManAlarm;
	public Vibrator v;

	/* accelerometer */
	int flagTap = 0;
	long lastimpulse = 0;
	private float vibrateThreshold = 0;
	private long lastUpdate = 0;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 20;
	private static final int SAMPLE_PERIOD_MILIS = 40;
	private static final float DEAD_MAN_THRESHOLD = (float) 0.5;
	TextView dx,xAcc,dy,yAcc,dz,zAcc,s,obs, obs2;
	boolean trackDeadMan;
	boolean trackDeadMan2;
	boolean trackDeadMan3;
	int deadManPhase;
	private long startDeadManTime = 0;
	private long currentDeadManTime = 0;
	private long deadManTimeMilis = 30000;
	float[] gravity = new float[3];

}
