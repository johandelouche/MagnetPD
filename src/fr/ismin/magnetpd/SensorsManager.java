package fr.ismin.magnetpd;

import java.util.List;

import org.puredata.core.PdBase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SensorsManager implements SensorEventListener {

	
	TelephonyManager telephonyManager;
	WifiManager wifiManager;
	BroadcastReceiver wifiRssiChanged;
	SensorManager sensorManager;
	Sensor emCaptor;
	Sensor accelerometerCaptor;

	private static final String TAG = "SensorManager";
	
	private MainActivity mainActivity;
	private float xMagnetic = 0;
	private float yMagnetic = 0;
	private float zMagnetic = 0;
	private double magneticStrength = 0;
	
	
	public SensorsManager(MainActivity mA){
		super();
		this.mainActivity = mA;
		initSensors();
	}

	/************************************************************************/
	/** Manage life cycle ******************************************************/
	/***********************************************************************/

	
	protected void initSensors(){

		telephonyManager = (TelephonyManager) mainActivity.getSystemService(Context.TELEPHONY_SERVICE);
		wifiManager = (WifiManager) mainActivity.getSystemService(Context.WIFI_SERVICE);
		sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
		emCaptor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		accelerometerCaptor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
		
		for (Sensor s : sensors) {
			Log.i(TAG, ""+s.getType());
		}
		
		telephonyManager.listen(new PhoneStateListener() {
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);
				if (signalStrength.isGsm()) {
					//Log.i(TAG, "gsm "+signalStrength.getGsmSignalStrength());
					PdBase.sendFloat("gsm", (float)signalStrength.getGsmSignalStrength());
				}
			}
		}, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		
		wifiRssiChanged = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				PdBase.sendFloat("wifi", (float)wifiManager.getConnectionInfo().getRssi());
				//Log.i(TAG, "wifi "+wifiManager.getConnectionInfo().getRssi());
			}
		};	
	}
	
	
	protected void onStop() {
		/*
		 *  unregister the sensors
		 */
		sensorManager.unregisterListener(this, emCaptor);
		sensorManager.unregisterListener(this, accelerometerCaptor);
		mainActivity.unregisterReceiver(wifiRssiChanged);
	}
	


	protected void onStart() {
		/*
		 *  register the sensors
		 */
		sensorManager.registerListener(this, emCaptor, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, accelerometerCaptor, SensorManager.SENSOR_DELAY_UI);
		mainActivity.registerReceiver(wifiRssiChanged, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

	}
	
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

			xMagnetic = event.values[0];
			yMagnetic = event.values[1];
			zMagnetic = event.values[2];

			magneticStrength=Math.sqrt((double)
					(xMagnetic*xMagnetic+
							yMagnetic*yMagnetic+
							zMagnetic*zMagnetic));

			mainActivity.redraw(magneticStrength);
			
			
			
			PdBase.sendFloat("magnetic",(float) (magneticStrength));
			
			//Log.i(TAG, "magnetic "+magneticStrength);
		}
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			
			float acceX = event.values[0];
            float acceY = event.values[1];
            float acceZ = event.values[2];
            
            PdBase.sendFloat("acceleroX", acceX);
            PdBase.sendFloat("acceleroY", acceY);
            PdBase.sendFloat("acceleroZ", acceZ);
		}

	}
	


}
