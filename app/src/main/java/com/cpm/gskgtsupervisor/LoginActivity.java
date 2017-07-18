package com.cpm.gskgtsupervisor;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cpm.Constants.CommonString;
import com.cpm.autoupdate.AutoupdateActivity;

import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.message.AlertMessage;
import com.cpm.web.ShowUpdateActivity;
import com.cpm.xmlGetterSetter.FailureGetterSetter;
import com.cpm.xmlGetterSetter.LoginGetterSetter;
import com.cpm.xmlHandler.XMLHandlers;
import com.cpm.meccain.R;


public class LoginActivity extends Activity implements OnClickListener,
		LocationListener {

	private EditText mUsername, mPassword;
	private Button mLogin;
	// private RelativeLayout login_remember;
	// private ImageView login_remembericon;

	private String username, password, p_username, p_password;
	private double latitude = 0.0, longitude = 0.0;
	private int versionCode;
	private boolean isChecked;

	private LocationManager locmanager = null;
	private SharedPreferences preferences = null;
	private SharedPreferences.Editor editor = null;
	private Intent intent = null;
	GSKDatabase database;
	static int counter = 1;
	// TextView login_version;
	String app_ver;
	int eventType;
	LoginGetterSetter lgs = null;
	private ArrayList<CoverageBean> coverageBeanlist = new ArrayList<CoverageBean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ContentResolver.setMasterSyncAutomatically(false);

		// login_version = (TextView) findViewById(R.id.login_versiontext);

		mUsername = (EditText) findViewById(R.id.login_usertextbox);
		mPassword = (EditText) findViewById(R.id.login_locktextbox);
		// login_remember = (RelativeLayout)
		// findViewById(R.id.login_rememberme);
		// login_remembericon = (ImageView)
		// findViewById(R.id.login_remembermeicon);
		
		/*mUsername.setText("testpro");
		mPassword.setText("cpm123");*/

		mLogin = (Button) findViewById(R.id.login_loginbtn);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = preferences.edit();

		p_username = preferences.getString(CommonString.KEY_USERNAME, null);
		p_password = preferences.getString(CommonString.KEY_PASSWORD, null);
		isChecked = preferences.getBoolean(CommonString.KEY_REMEMBER, false);

		try {
			app_ver =String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);

			// login_version.setText("Parinaam Version " + app_ver);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		database = new GSKDatabase(this);
//		database.open();

		if (!isChecked) {
			// login_remembericon.setImageResource(R.drawable.deactive_radio_box);
		} else {
			mUsername.setText(p_username);
			mPassword.setText(p_password);
		}

		mLogin.setOnClickListener(this);
		// login_remember.setOnClickListener(this);

		locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean enabled = locmanager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to
		// go to the settings
		if (!enabled) {

			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					LoginActivity.this);

			// Setting Dialog Title
			alertDialog.setTitle("GPS IS DISABLED...");

			// Setting Dialog Message
			alertDialog.setMessage("Click ok to enable GPS.");

			// Setting Positive "Yes" Button
			alertDialog.setPositiveButton("YES",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							intent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(intent);
						}
					});

			// Setting Negative "NO" Button
			alertDialog.setNegativeButton("NO",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Write your code here to invoke NO event

							dialog.cancel();
						}
					});

			// Showing Alert Message
			alertDialog.show();

		}

		// Create a Folder for Images
		File file = new File(Environment.getExternalStorageDirectory(),
				"Mccain_Images");
		if (!file.isDirectory()) {
			file.mkdir();
		}

		// mUsername.setText("testmer");
		// mPassword.setText("cpm123");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		locmanager.removeUpdates(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		username = mUsername.getText().toString().trim();
		password = mPassword.getText().toString().trim();

		switch (v.getId()) {
		case R.id.login_loginbtn:
			
			
			if (username.length() == 0) {
				showToast("Please enter username");
			} else if (password.length() == 0) {
				showToast("Please enter password");
			} else {

				p_username = preferences.getString(CommonString.KEY_USERNAME,
						null);
				p_password = preferences.getString(CommonString.KEY_PASSWORD,
						null);
				// If no preferences are stored
				if (p_username == null && p_password == null) {

					if (CheckNetAvailability()) {
						new AuthenticateTask().execute();

					} else {
						showToast("No Network and first login");
					}
				}
				// If preferences are stored
				else {
					if (username.equals(p_username)) {
						if (CheckNetAvailability()) {
							new AuthenticateTask().execute();
						} 
						else if (password.equals(p_password)) {
							intent = new Intent(this, MainActivity.class);
							startActivity(intent);
							this.finish();

							showToast("No Network and offline login");
						} else {
							showToast("Incorrect Password");
						}
					} else {
						showToast("Incorrect Username");
					}
				}
			}
			break;

		}
	}

	private class AuthenticateTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle("Login");
			dialog.setMessage("Authenticating....");
			dialog.setCancelable(false);
			dialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {

				versionCode = getPackageManager().getPackageInfo(
						getPackageName(), 0).versionCode;

				String userauth_xml = "[DATA]" + "[USER_DATA][USER_ID]"
						+ username + "[/USER_ID]" + "[Password]" + password
						+ "[/Password]" + "[IN_TIME]" + getCurrentTime()
						+ "[/IN_TIME]" + "[LATITUDE]" + latitude
						+ "[/LATITUDE]" + "[LONGITUDE]" + longitude
						+ "[/LONGITUDE]" + "[APP_VERSION]" + app_ver
						+ "[/APP_VERSION]" + "[ATT_MODE]OnLine[/ATT_MODE]"
						 + "[/USER_DATA][/DATA]";

				SoapObject request = new SoapObject(CommonString.NAMESPACE,
						CommonString.METHOD_LOGIN);
				
				request.addProperty("onXML", userauth_xml);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
						SoapEnvelope.VER11);
				envelope.dotNet = true;
				envelope.setOutputSoapObject(request);

				HttpTransportSE androidHttpTransport = new HttpTransportSE(
						CommonString.URL);

				androidHttpTransport.call(CommonString.SOAP_ACTION_LOGIN,
						envelope);

				Object result = (Object) envelope.getResponse();

				if (result.toString()
						.equalsIgnoreCase(CommonString.KEY_FAILURE)) {

					final AlertMessage message = new AlertMessage(
							LoginActivity.this, AlertMessage.MESSAGE_FAILURE,
							"login", null);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							message.showMessage();
						}
					});

				} else if (result.toString().equalsIgnoreCase(
						CommonString.KEY_FALSE)) {

					final AlertMessage message = new AlertMessage(
							LoginActivity.this, AlertMessage.MESSAGE_FALSE,
							"login", null);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							message.showMessage();
						}
					});

				} else if (result.toString().equalsIgnoreCase(
						CommonString.KEY_CHANGED)) {

					final AlertMessage message = new AlertMessage(
							LoginActivity.this, AlertMessage.MESSAGE_CHANGED,
							"login", null);
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							message.showMessage();
						}
					});

				} else {

					XmlPullParserFactory factory = XmlPullParserFactory
							.newInstance();
					factory.setNamespaceAware(true);
					XmlPullParser xpp = factory.newPullParser();

					xpp.setInput(new StringReader(result.toString()));
					xpp.next();
					eventType = xpp.getEventType();
					FailureGetterSetter failureGetterSetter = XMLHandlers
							.failureXMLHandler(xpp, eventType);

					if (failureGetterSetter.getStatus().equalsIgnoreCase(
							CommonString.KEY_FAILURE)) {
						final AlertMessage message = new AlertMessage(
								LoginActivity.this, CommonString.METHOD_LOGIN
										+ failureGetterSetter.getErrorMsg(),
								"login", null);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								message.showMessage();
							}
						});
					}

					else {

						try {
							// For String source

							xpp.setInput(new StringReader(result.toString()));
							xpp.next();
							eventType = xpp.getEventType();
							lgs = XMLHandlers.loginXMLHandler(xpp, eventType);

						} catch (XmlPullParserException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						// PUT IN PREFERENCES
						editor.putString(CommonString.KEY_USERNAME, username);
						editor.putString(CommonString.KEY_PASSWORD, password);
						editor.putString(CommonString.KEY_VERSION, lgs.getVERSION());
						editor.putString(CommonString.KEY_PATH, lgs.getPATH());
						editor.putString(CommonString.KEY_DATE, lgs.getDATE());
						editor.putString(CommonString.KEY_USER_TYPE, lgs.getRIGHTNAME());

						// PUT IN PREFERENCES
						editor.commit();

						return CommonString.KEY_SUCCESS;

					}
				}

				return "";

			} catch (MalformedURLException e) {

				final AlertMessage message = new AlertMessage(
						LoginActivity.this, AlertMessage.MESSAGE_EXCEPTION,
						"acra_login", e);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						message.showMessage();
					}
				});

			} catch (IOException e) {
				final AlertMessage message = new AlertMessage(
						LoginActivity.this,
						AlertMessage.MESSAGE_SOCKETEXCEPTION, "socket_login", e);

				counter++;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (counter < 3) {
							new AuthenticateTask().execute();
						} else {
							message.showMessage();
							counter = 1;
						}
					}
				});
			}

			catch (Exception e) {
				final AlertMessage message = new AlertMessage(
						LoginActivity.this, AlertMessage.MESSAGE_EXCEPTION,
						"acra_login", e);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						message.showMessage();
					}
				});
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			if (result.equals(CommonString.KEY_SUCCESS)) {
		
//				database.open();
				if (preferences.getString(CommonString.KEY_VERSION, "").equals(
						Integer.toString(versionCode))) {

					intent = new Intent(getBaseContext(),
							ShowUpdateActivity.class);
					startActivity(intent);

					finish();
				} else {
					intent = new Intent(getBaseContext(),
							AutoupdateActivity.class);

					intent.putExtra(CommonString.KEY_PATH,
							preferences.getString(CommonString.KEY_PATH, ""));
					startActivity(intent);
					finish();
				}

			}

			dialog.dismiss();
		}

	}

	public boolean CheckNetAvailability() {

		boolean connected = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState() == NetworkInfo.State.CONNECTED
				|| connectivityManager.getNetworkInfo(
						ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
			// we are connected to a network
			connected = true;
		}
		return connected;
	}

	private void showToast(String message) {
		Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
	}

	// for location
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	public void onButtonClick(View v) {

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);

	}

	public String getCurrentTime() {

		Calendar m_cal = Calendar.getInstance();

		String intime = m_cal.get(Calendar.HOUR_OF_DAY) + ":"
				+ m_cal.get(Calendar.MINUTE) + ":" + m_cal.get(Calendar.SECOND);

		return intime;

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);

	}
}
