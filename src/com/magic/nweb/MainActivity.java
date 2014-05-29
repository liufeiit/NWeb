package com.magic.nweb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	private static final String TAG = "NWeb";
	
	private static final String FULLSCREEN_PREF_ATTR = "is_fullscreen_pref";
	private static boolean DEFAULT_FULLSCREEN = true;
	private static final long BACK_PRESS_THRES_HOLD = 3500;
	private static final long TOUCH_THRES_HOLD = 2000;
	
	private long lastBackPress;
	private long lastTouch;
	private Toast pressBackToast;

	private WebView webView;
	private long userId;
	private String userNick;

	@SuppressLint({ "SetJavaScriptEnabled", "NewApi", "ShowToast", "SdCardPath", "JavascriptInterface" })
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		userId = 7;//tom
//		userId = 91757;//tom
//		userId = 268825;//辉哥
		userId = 215799;//飞哥
		userNick = "大飞哥儿";
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Build.VERSION.SDK_INT >= 11) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
		applyFullScreen(isFullScreen());
		boolean isOrientationEnabled = false;
		try {
			isOrientationEnabled = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1;
		} catch (SettingNotFoundException e) {
			Log.e(TAG, "System Setting Error.", e);
		}
		int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if ((screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE || screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE)
				&& isOrientationEnabled) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		}
		setContentView(R.layout.activity_main);
		webView = (WebView) findViewById(R.string.mview_name);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setDatabaseEnabled(true);
		webView.getSettings().setRenderPriority(RenderPriority.HIGH);
		webView.getSettings().setDatabasePath("/data/data/" + getPackageName() + "/databases");
		webView.setWebChromeClient(new WebChromeClient());
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		});
		webView.addJavascriptInterface(this, "callback");
		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl("file:///android_asset/index.html");
		}
		Toast.makeText(getApplication(), R.string.toggle_fullscreen, Toast.LENGTH_SHORT).show();
		webView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				long currentTime = System.currentTimeMillis();
				if (event.getAction() == MotionEvent.ACTION_UP && Math.abs(currentTime - lastTouch) > TOUCH_THRES_HOLD) {
					boolean toggledFullScreen = !isFullScreen();
					saveFullScreen(toggledFullScreen);
					applyFullScreen(toggledFullScreen);
				} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
					lastTouch = currentTime;
				}
				return false;
			}
		});
		pressBackToast = Toast.makeText(getApplicationContext(), R.string.press_back_again_to_exit, Toast.LENGTH_SHORT);
	}
	
	@JavascriptInterface
	public void callback(long score) {
		String message = String.format("恭喜您以%s获取了胜利！", score, getUserNick(), getUserId());
		Log.e(TAG, message);
		Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
		String url = "http://www.ruoogle.com.cn/nova/user/gamereward";
		Map<String, String> param = new HashMap<String, String>();
		param.put("game_type", "1");
		param.put("user_id", String.valueOf(getUserId()));
		param.put("token", "zhe@shi^wo*lai(ce)shi~le");
		param.put("sign", sign(param));
		sendPost(url, param);
		
		url = "http://www.ruoogle.com.cn/nova/user/gamescore";
		param = new HashMap<String, String>();
		param.put("game_type", "1");
		param.put("game_score", String.valueOf(score));
		param.put("user_id", String.valueOf(getUserId()));
		param.put("token", "zhe@shi^wo*lai(ce)shi~le");
		param.put("sign", sign(param));
		sendPost(url, param);
	}
	
	private String sign(Map<String, String> param) {
		Set<String> _keys = param.keySet();
		_keys.remove("sign");
		List<String> keys = new ArrayList<String>(_keys);
		Collections.sort(keys, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}});
		int size = keys.size();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			String key = keys.get(i);
			String val = param.get(key);
			if(val == null) {
				continue;
			}
			builder.append(key).append("=").append(val);
		}
		builder.append("1qaz$ESZ%TGB7ygv^THBbnmv*UKMbm)ILN");
		return DigestUtils.sign(builder.toString());
	}
	
	private String sendPost(String url, Map<String, String> param) {
		StringBuffer resultData = new StringBuffer();
		Log.i(TAG, "url : " + url);
		HttpPost request = new HttpPost(url);
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		for (Entry<String, String> e : param.entrySet()) {
			postParams.add(new BasicNameValuePair(e.getKey(), e.getValue()));
		}
		try {
			request.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
			HttpResponse response = new DefaultHttpClient().execute(request);
			resultData.append(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			Log.e(TAG, "sendPost", e);
		}
		Log.i(TAG, "result : " + resultData.toString());
		return resultData.toString();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
	}

	private void saveFullScreen(boolean isFullScreen) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putBoolean(FULLSCREEN_PREF_ATTR, isFullScreen);
		editor.commit();
	}

	private boolean isFullScreen() {
		return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(FULLSCREEN_PREF_ATTR, DEFAULT_FULLSCREEN);
	}

	private void applyFullScreen(boolean isFullScreen) {
		if (isFullScreen) {
			getWindow().clearFlags(LayoutParams.FLAG_FULLSCREEN);
		} else {
			getWindow()
					.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onBackPressed() {
		long currentTime = System.currentTimeMillis();
		if (Math.abs(currentTime - lastBackPress) > BACK_PRESS_THRES_HOLD) {
			pressBackToast.show();
			lastBackPress = currentTime;
		} else {
			pressBackToast.cancel();
			super.onBackPressed();
		}
	}

	public long getUserId() {
		return userId;
	}

	public String getUserNick() {
		return userNick;
	}
}