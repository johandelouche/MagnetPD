package fr.ismin.magnetpd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.puredata.core.PdBase;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.animation.RotateAnimation;

public class LayoutFragment extends Fragment {

	private MainActivity mainActivity;
	private View view;

	private Toast toast = null;

	private ImageView diode;
	private ImageView aiguille;
	private TextView count;
	// private TextView logs;
	private ToggleButton onOff;
	private Typeface typeface;
	private AssetManager assetMgr;
	private ArrayList<String> patchList;
	private float degrees;
	private int countRedraw = 0;
	/*
	 * private Spinner patchSelector; private ArrayAdapter<String>
	 * spinnerAdapter; 
	 */
	private static final String TAG = "LayoutFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mainActivity = (MainActivity) getActivity();
		view = inflater.inflate(R.layout.layout_fragment, container, false);
		initGUI();

		// Inflate the layout for this fragment
		return view;
	}

	public void initGUI() {

		diode = ((ImageView) view.findViewById(R.id.diode));
		aiguille = ((ImageView) view.findViewById(R.id.aiguille));
		count = ((TextView) view.findViewById(R.id.count));
		typeface = Typeface.createFromAsset(mainActivity.getAssets(), "fonts/DS-DIGI.TTF");
		count.setTypeface(typeface);
		onOff = ((ToggleButton) view.findViewById(R.id.on_off));
		/*
		 * logs = ((TextView) view.findViewById(R.id.logs));
		 * logs.setMovementMethod(new ScrollingMovementMethod()); //
		 */

		/*
		 * patchSelector = ((Spinner) view.findViewById(R.id.patchSelector));
		 * patchSelector.setOnItemSelectedListener(this); //
		 */
		assetMgr = mainActivity.getAssets();
		patchList = (ArrayList<String>) displayFiles(assetMgr, "patches");
	}

	public void redraw(double magneticStrength) {
		diode((int) magneticStrength);
		rotate((float) magneticStrength);
		countRedraw = (countRedraw + 1) % 10;
	}

	public void onOff() {
		if (onOff.isChecked()) {
			PdBase.sendFloat("on_off", 1);
			mainActivity.start();
		} else {
			PdBase.sendFloat("on_off", 0);
			mainActivity.stop();
		}
	}

	protected void rotate(float x) {

		float alpha = (float) (0.85 * x - 34);
		if (alpha > (float) 85) {
			alpha = (float) 85;
		}
		if (alpha < (float) -5) {
			alpha = (float) -5;
		}
		RotateAnimation rotation = new RotateAnimation((float) degrees,
				(float) alpha, RotateAnimation.RELATIVE_TO_SELF, (float) 0.5,
				RotateAnimation.RELATIVE_TO_SELF, (float) 1.0);
		rotation.setDuration((long) 100);
		aiguille.setAnimation(rotation);
		degrees = alpha;
	}

	protected void diode(int n) {
		if (n > 130) {
			diode.setVisibility(0);
		} else {
			diode.setVisibility(4);
		}
	}

	protected void updateCount(int min, int sec) {
		/*
		 * Need to be improve using android.widget.Chronometer class that
		 * extends android.widget.TextView class.
		 */
		String time;
		if (min < 10) {
			time = "0" + min + ":";
		} else {
			time = min + ":";
		}
		if (sec < 10) {
			time += "0" + sec + "";
		} else {
			time += sec + "";
		}
		final String t = time;
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				count.setText(t);
			}
		});
	}

	protected void toast(final String msg) {
		mainActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (toast == null) {
					toast = Toast.makeText(
							mainActivity.getApplicationContext(), "",
							Toast.LENGTH_SHORT);
				}
				toast.setText(TAG + ": " + msg);
				toast.show();
			}
		});
	}

	protected void post(final String s) {
		/*
		 * mainActivity.runOnUiThread(new Runnable() {
		 * 
		 * @Override public void run() { logs.append(s + ((s.endsWith("\n")) ?
		 * "" : "\n")); } }); //
		 */
	}

	private List<String> displayFiles(AssetManager mgr, String path) {
		List<String> patchList = null;
		try {
			String list[] = mgr.list(path);
			patchList = new ArrayList<String>(Arrays.asList(list));
			if (list != null)
				for (int i = 0; i < list.length; ++i) {
					Log.v("Assets:", path + "/" + list[i]);
					displayFiles(mgr, path + "/" + list[i]);
				}
		} catch (IOException e) {
			Log.v("List error:", "can't list" + path);
		}
		return patchList;
	}

	protected ArrayList<String> getPatchList() {
		return patchList;
	}

	protected AssetManager getAssetManager() {
		return assetMgr;
	}

}
