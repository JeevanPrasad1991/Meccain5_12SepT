package com.cpm.dailyentry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.cpm.Constants.CommonString;
import com.cpm.GpsTracker.GPSTracker;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
//import com.cpm.delegates.CoverageInfo;
//import com.cpm.delegates.attandance;
import com.cpm.meccain.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class VisitedStoreImage extends Activity implements OnClickListener {

	ImageView img_cam,img_clicked;
	ProgressBar progressbar;
	final static int CAMERA_OUTPUT = 0;
	Button btn;
	String _pathforcheck=null, _path=null,visit_date,store_cd,str,EMPid,latitud, longitud,username;
	private SharedPreferences preferences;
	File file,file1;
	Uri outputFileUri;
	AlertDialog alert;
	GSKDatabase database;
	String datacheck = "";
	String[] words;
	String validity;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.storeentryimage);

		database = new GSKDatabase(this);
		database.open();

		img_cam = (ImageView) findViewById(R.id.img_selfie);
		img_clicked = (ImageView) findViewById(R.id.img_cam_selfie);
		progressbar = (ProgressBar) findViewById(R.id.progressBar1);
		btn = (Button) findViewById(R.id.btn_save_selfie);

		str = Environment.getExternalStorageDirectory() + "/Mccain_Images/";

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		visit_date = preferences.getString(CommonString.KEY_DATE, null);
		store_cd = preferences.getString(CommonString.KEY_STORE_CD, null);
		EMPid = preferences.getString(CommonString.Key_EMPID, null);
		username = preferences.getString(CommonString.KEY_USERNAME, null);
		GPSTracker gps = new GPSTracker(VisitedStoreImage.this);
		double latitude = gps.getLatitude();
		double longitude = gps.getLongitude();

		latitud = String.valueOf(latitude);
		longitud = String.valueOf(longitude);

		btn.setOnClickListener(this);
		img_clicked.setOnClickListener(this);

	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		int id = v.getId();
		switch (id) {
			case R.id.img_cam_selfie:

				_pathforcheck = "attendanceimage" + store_cd + visit_date.replace("/", "")  + ".jpg";
				_path = Environment.getExternalStorageDirectory() + "/Mccain_Images/" + _pathforcheck;

				startCameraActivity();

				break;

			case R.id.btn_save_selfie:

				if (CheckNetAvailability()) {

					if ( file1!=null )
					{

						AlertDialog.Builder builder = new AlertDialog.Builder(
								VisitedStoreImage.this);
						builder.setMessage("Do you want to Check-In ")
								.setCancelable(false)
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {

												alert.getButton(
														AlertDialog.BUTTON_POSITIVE)
														.setEnabled(false);
												database.insertvisitedData(store_cd,visit_date,_pathforcheck );
												new UploadingTask().execute();

											}


										})
								.setNegativeButton("Cancel",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});

						alert = builder.create();
						alert.show();

						break;
					}

					else {

						Toast.makeText(getApplicationContext(), "Please Take A Selfie", Toast.LENGTH_SHORT).show();

					}

				}

				else {

					Toast.makeText(getApplicationContext(), "Please Check Network Connection ", Toast.LENGTH_SHORT).show();

				}
		}

	}

	protected void startCameraActivity() {

		file = new File(_path);
		outputFileUri = Uri.fromFile(file);

		Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, CAMERA_OUTPUT);

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i("MakeMachine", "resultCode: " + resultCode);
		switch (resultCode) {
			case 0:
				Log.i("MakeMachine", "User cancelled");
				break;

			case -1:

				img_clicked.setVisibility(ImageView.INVISIBLE);
				img_cam.setVisibility(ImageView.VISIBLE);

				if (_pathforcheck != null && !_pathforcheck.equals("")) {
					if (new File(str + _pathforcheck).exists()) {

						file1 = new File(str + _pathforcheck);

						Uri uri = Uri.fromFile(file1);
						Bitmap bitmap;

						try {
							bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
							bitmap = crupAndScale(bitmap, 800); // if you mind scaling
							img_cam.setImageBitmap(bitmap);

						}

						catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						img_clicked.setVisibility(ImageView.INVISIBLE);
						img_cam.setVisibility(ImageView.VISIBLE);

					}
				}

				break;

		}

		super.onActivityResult(requestCode, resultCode, data);
	}


	public static Bitmap crupAndScale(Bitmap source, int scale) {
		int factor = source.getHeight() <= source.getWidth() ? source.getHeight() : source.getWidth();
		int longer = source.getHeight() >= source.getWidth() ? source.getHeight() : source.getWidth();
		int x = source.getHeight() >= source.getWidth() ? 0 : (longer - factor) / 2;
		int y = source.getHeight() <= source.getWidth() ? 0 : (longer - factor) / 2;
		source = Bitmap.createBitmap(source, x, y, factor, factor);
		source = Bitmap.createScaledBitmap(source, scale, scale, false);
		return source;
	}

	private class UploadingTask extends AsyncTask<Void, String, String> {


		final ProgressDialog ringProgressDialog = ProgressDialog.show(VisitedStoreImage.this, "Please wait ...", "Uploading Data ...", true);

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			//ringProgressDialog.show();
			ringProgressDialog.setCancelable(true);

		}

		@Override
		protected String doInBackground(Void... params) {

			try {

				String onXML = "[DATA][USER_DATA][STORE_ID]"
						+ store_cd
						+ "[/STORE_ID]"

						+ "[LATTITUDE]"
						+ latitud
						+ "[/LATTITUDE]"

						+ "[LONGITUDE]"
						+ longitud
						+ "[/LONGITUDE]"

						+ "[IMAGE_URL]"
						+ _pathforcheck
						+ "[/IMAGE_URL]"

						+ "[USER_ID]" + username
						+ "[/USER_ID]"
						+ "[/USER_DATA][/DATA]";


				SoapObject request = new SoapObject(CommonString.NAMESPACE, CommonString.METHOD_UPLOAD_LATTITUTE_LONGITUDE);
				request.addProperty("onXML", onXML);

				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
				envelope.dotNet = true;
				envelope.setOutputSoapObject(request);

				HttpTransportSE androidHttpTransport = new HttpTransportSE(CommonString.URL);
				androidHttpTransport.call(CommonString.SOAP_ACTION+CommonString.METHOD_UPLOAD_LATTITUTE_LONGITUDE,envelope);
				Object result;

				result = (Object) envelope.getResponse();

				datacheck = result.toString();
				datacheck = datacheck.replace("\"", "");
				words = datacheck.split("\\;");
				validity = (words[0]);


				if (result.toString().equalsIgnoreCase(CommonString.KEY_FALSE)) {
					return CommonString.METHOD_UPLOAD_LATTITUTE_LONGITUDE;
				}
				if (result.toString().equalsIgnoreCase(
						CommonString.KEY_FAILURE)) {
					return CommonString.METHOD_UPLOAD_LATTITUTE_LONGITUDE;
				}


			} catch (SoapFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}



			return null;

		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			new Thread(new Runnable() {

				@Override

				public void run() {

					try {

						// Here you should write your time consuming task...

						// Let the progress ring for 10 seconds...

						Thread.sleep(5000);

					} catch (Exception e) {



					}

					ringProgressDialog.dismiss();

					Intent in  = new Intent(VisitedStoreImage.this, StoreEntry.class);
					startActivity(in);
					overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
					finish();


				}

			}).start();

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

}