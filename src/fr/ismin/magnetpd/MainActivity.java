/*
 * Created by Paul Tsnobiladzé, Johan Delouche and François Parra
 */

package fr.ismin.magnetpd;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class MainActivity extends FragmentActivity {

	private LayoutFragment layoutFragment;
	private SensorsManager sensorsManager;
	private PureDataManager pureDataManager;
	private RecordAudioManager recordAudioManager;

	private PowerManager.WakeLock wakeLock;
	private static final String TAG = "Theremin Test";

	private int min;
	private int sec;

	private boolean started = false;

	/************************************************************************/
	/** Manage life cycle ******************************************************/
	/***********************************************************************/
	/** called at activity creation. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
		initGUI();
		// Gérer les capteurs :
		sensorsManager = new SensorsManager(this);
		// PureData
		pureDataManager = new PureDataManager(this);
		// RecordAudioManager
		recordAudioManager = new RecordAudioManager(this);
	}

	@Override
	protected void onStart() {
		wakeLock.acquire();
		start();
		super.onStart();
	}

	public void start() {
		if (!started) {
			sensorsManager.onStart();
			started = true;
		}
	}

	@Override
	protected void onStop() {
		wakeLock.release();
		stop();
		super.onStop();
	}

	public void stop() {
		if (started) {
			sensorsManager.onStop();
			started = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		pureDataManager.cleanup();
	}

	/********************************************************************/
	/** GUI *************************************************/
	/********************************************************************/

	public void initGUI() {

		setContentView(R.layout.layout_main);
		layoutFragment = (LayoutFragment) getSupportFragmentManager()
				.findFragmentById(R.id.layout_fragment);
	}

	protected void redraw(double magneticStrength) {
		layoutFragment.redraw(magneticStrength);
	}

	protected void toast(final String msg) {
		layoutFragment.toast(msg);
	}

	protected void post(final String s) {
		layoutFragment.post(s);
	}

	public void record(View v) {
		recordAudioManager.record();
	}

	public void onOff(View v) {
		layoutFragment.onOff();
	}

	protected ArrayList<String> getPatchList() {
		return layoutFragment.getPatchList();
	}

	protected AssetManager getAssetManager() {
		return layoutFragment.getAssetManager();
	}

	void loadPatch(String s) {
		pureDataManager.loadPatch(s);
	}

	public void updateMin(int min) {
		this.min = min;
		layoutFragment.updateCount(this.min, this.sec);
	}

	public void updateSec(int sec) {
		this.sec = sec;
		layoutFragment.updateCount(this.min, this.sec);
	}

}
