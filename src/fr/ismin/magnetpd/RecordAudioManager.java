package fr.ismin.magnetpd;

import java.io.File;

import org.puredata.core.PdBase;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;

public class RecordAudioManager {
	
	private static MainActivity mainActivity;

	private static boolean recording = false;
	private static float countRecord;
	private static SharedPreferences sharedPreferences;
	private static Button recordButton;
	private static File file;
	
	private RecordAudioManager() {}
	
	public static void init(MainActivity mA) {
		
		mainActivity = mA;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainActivity);
		//preferences are used to fill field, if any default value is used.
		countRecord = sharedPreferences.getFloat("count_record", (float)0);
		
		file = new File("/mnt/sdcard/pd_record/");
		file.mkdir();
		recordButton = (Button) mainActivity.findViewById(R.id.record);
		recordButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (recording) {
					countRecord++;
					sharedPreferences.edit().putFloat("count_record", countRecord).commit();
					PdBase.sendBang("stop");
					recordButton.setText(R.string.record);
					recording = !recording;
				} else {
					PdBase.sendFloat("countRecord", countRecord);
					PdBase.sendBang("start");
					recordButton.setText(R.string.no_record);
					recording = !recording;        	
				}
			}
		});
	}
	
}
