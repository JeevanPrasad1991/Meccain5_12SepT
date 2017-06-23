package com.cpm.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cpm.Constants.CommonString;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.meccain.R;
import com.cpm.message.AlertMessage;

import com.cpm.xmlGetterSetter.FailureGetterSetter;
import com.cpm.xmlHandler.FailureXMLHandler;

public class UploadAllImageActivity extends Activity {

	private Dialog dialog;
	private ProgressBar pb;
	private TextView percentage, message;
	private String visit_date;
	private SharedPreferences preferences;
	private GSKDatabase database;
	private int factor, k;
	private FailureGetterSetter failureGetterSetter = null;

	String result, username;
	String datacheck = "";
	String[] words;
	String validity, storename;
	String mid = "";
	String errormsg = "";
	static int counter = 1;
	
	String Path,status;
	
	private ArrayList<CoverageBean> coverageBeanlist = new ArrayList<CoverageBean>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainpage);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		visit_date = preferences.getString(CommonString.KEY_DATE, null);
		username = preferences.getString(CommonString.KEY_USERNAME, null);

		database = new GSKDatabase(this);
		database.open();

		Path= Environment.getExternalStorageDirectory()
				+ "/Mccain_Images/";
		
		new UploadTask(this).execute();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// database.close();
	}

	private class UploadTask extends AsyncTask<Void, Void, String> {
		private Context context;

		UploadTask(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			dialog = new Dialog(context);
			dialog.setContentView(R.layout.custom);
			dialog.setTitle("Uploading Image");
			dialog.setCancelable(false);
			dialog.show();
			pb = (ProgressBar) dialog.findViewById(R.id.progressBar1);
			percentage = (TextView) dialog.findViewById(R.id.percentage);
			message = (TextView) dialog.findViewById(R.id.message);
		}

		@Override
		protected String doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {

				coverageBeanlist=database.getCoverageData(visit_date);
				if(coverageBeanlist.size()>0){
					for(int i=0;i<coverageBeanlist.size();i++){
						
						status=coverageBeanlist.get(i).getStatus();
						if(status.equals(CommonString.STORE_STATUS_LEAVE)){
							
							String path=coverageBeanlist.get(i).getImage();
							if(path!=null && !path.equals("")){
								UploadImage(path);
							}
							
							
							
						}
						
					}
				}
				
				
		
		
		} catch (MalformedURLException e) {

				final AlertMessage message = new AlertMessage(
						UploadAllImageActivity.this,
						AlertMessage.MESSAGE_EXCEPTION, "download", e);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						message.showMessage();
					}
				});

			} catch (IOException e) {
				final AlertMessage message = new AlertMessage(
						UploadAllImageActivity.this,
						AlertMessage.MESSAGE_SOCKETEXCEPTION,
						"socket_uploadimagesall", e);
				counter++;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						message.showMessage();
						// TODO Auto-generated method stub
						/*
						 * if (counter < 3) { new
						 * UploadTask(UploadAllImageActivity.this).execute(); }
						 * else { message.showMessage(); counter = 1; }
						 */
					}
				});
			} catch (Exception e) {
				final AlertMessage message = new AlertMessage(
						UploadAllImageActivity.this,
						AlertMessage.MESSAGE_EXCEPTION, "download", e);
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

			dialog.dismiss();

			if (result.equals(CommonString.KEY_SUCCESS)) {
				database.open();
				database.deleteAllTables();

				AlertMessage message = new AlertMessage(
						UploadAllImageActivity.this,
						AlertMessage.MESSAGE_UPLOAD_IMAGE, "success", null);
				message.showMessage();

			} else if (!result.equals("")) {
				AlertMessage message = new AlertMessage(
						UploadAllImageActivity.this, result, "success", null);
				message.showMessage();
			}
		}

		

		public String UploadImage(String path) throws Exception {

			errormsg = "";
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(Path + path, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 1024;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;

			while (true) {
				if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			Bitmap bitmap = BitmapFactory.decodeFile(
					Path + path, o2);

			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
			byte[] ba = bao.toByteArray();
			String ba1 = Base64.encodeBytes(ba);

			SoapObject request = new SoapObject(CommonString.NAMESPACE,
					CommonString.METHOD_UPLOAD_IMAGE);

			String[] split = path.split("/");
			String path1 = split[split.length - 1];

			request.addProperty("img", ba1);
			request.addProperty("name", path1);
			request.addProperty("FolderName", "StoreImages");

			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);

			HttpTransportSE androidHttpTransport = new HttpTransportSE(
					CommonString.URL);

			androidHttpTransport
					.call(CommonString.SOAP_ACTION_UPLOAD_IMAGE,
							envelope);
			Object result = (Object) envelope.getResponse();

			if (!result.toString().equalsIgnoreCase(CommonString.KEY_SUCCESS)) {

				if (result.toString().equalsIgnoreCase(CommonString.KEY_FALSE)) {
					return CommonString.KEY_FALSE;
				}

				SAXParserFactory saxPF = SAXParserFactory.newInstance();
				SAXParser saxP = saxPF.newSAXParser();
				XMLReader xmlR = saxP.getXMLReader();

				// for failure
				FailureXMLHandler failureXMLHandler = new FailureXMLHandler();
				xmlR.setContentHandler(failureXMLHandler);

				InputSource is = new InputSource();
				is.setCharacterStream(new StringReader(result.toString()));
				xmlR.parse(is);

				failureGetterSetter = failureXMLHandler
						.getFailureGetterSetter();

				if (failureGetterSetter.getStatus().equalsIgnoreCase(
						CommonString.KEY_FAILURE)) {
					errormsg = failureGetterSetter.getErrorMsg();
					return CommonString.KEY_FAILURE;
				}
			} else {
				new File(Path + path).delete();
			}

			return "";
		}

	}

	
}
