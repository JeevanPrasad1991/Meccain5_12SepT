package com.cpm.dailyentry;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cpm.Constants.CommonString;

import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.delegates.ReasonModel;
import com.cpm.delegates.StoreBean;
import com.cpm.meccain.R;
import com.cpm.xmlGetterSetter.NonWorkingReasonGetterSetter;


public class NonWorkingReason extends Activity implements OnItemSelectedListener, OnClickListener {

	ArrayList<NonWorkingReasonGetterSetter> reasondata = new ArrayList<NonWorkingReasonGetterSetter>();
	private Spinner reasonspinner;
	private GSKDatabase database;
	String reasonname, reasonid, image, entry, reason_reamrk, intime;
	Button save;
	private ArrayAdapter<CharSequence> reason_adapter;
	protected String _path, str;
	protected String _pathforcheck = "";
	private String image1 = "";
	private SharedPreferences preferences;
	String _UserId, visit_date, store_id;
	protected boolean status = true;
	EditText text;
	AlertDialog alert;
	ImageButton camera;
	RelativeLayout reason_lay,rel_cam;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nonworking);

		reasonspinner = (Spinner) findViewById(R.id.spinner2);
		camera = (ImageButton) findViewById(R.id.imgcam);
		save = (Button) findViewById(R.id.save);
		text = (EditText) findViewById(R.id.reasontxt);
		reason_lay=(RelativeLayout) findViewById(R.id.layout_reason);
		rel_cam=(RelativeLayout) findViewById(R.id.relimgcam);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		_UserId = preferences.getString(CommonString.KEY_USERNAME, "");
		visit_date = preferences.getString(CommonString.KEY_DATE, null);
		store_id = preferences.getString(CommonString.KEY_STORE_CD, "");

		database = new GSKDatabase(this);
		database.open();
		str = Environment.getExternalStorageDirectory() + "/Mccain_Images/";

		reasondata = database.getNonWorkingData();

		intime = getCurrentTime();

		camera.setOnClickListener(this);
		save.setOnClickListener(this);

		reason_adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item);

		reason_adapter.add("Select Reason");

		for (int i = 0; i < reasondata.size(); i++) {
			reason_adapter.add(reasondata.get(i).getReason().get(0));
		}

		reasonspinner.setAdapter(reason_adapter);

		reason_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		reasonspinner.setOnItemSelectedListener(this);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		finish();

		overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
							   long arg3) {
		// TODO Auto-generated method stub

		switch (arg0.getId()) {
			case R.id.spinner2:
				if (position != 0) {
					reasonname = reasondata.get(position - 1).getReason().get(0);
					reasonid = reasondata.get(position - 1).getReason_cd().get(0);

					if (reasonname.equalsIgnoreCase("Store Closed")) {
						rel_cam.setVisibility(View.VISIBLE);
						image="true";
					} else {
						rel_cam.setVisibility(View.GONE);
						image="false";
					}
					reason_reamrk="true";
					if (reason_reamrk.equalsIgnoreCase("true")) {
						reason_lay.setVisibility(View.VISIBLE);
					} else {
						reason_lay.setVisibility(View.GONE);
					}
				} else {
					reasonname = "";
					reasonid = "";
				}
				break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

	protected void startCameraActivity() {

		try {
			Log.i("MakeMachine", "startCameraActivity()");
			File file = new File(_path);
			Uri outputFileUri = Uri.fromFile(file);

			Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			startActivityForResult(intent, 0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("MakeMachine", "resultCode: " + resultCode);
		switch (resultCode) {
			case 0:
				Log.i("MakeMachine", "User cancelled");
				break;

			case -1:

				if (_pathforcheck != null && !_pathforcheck.equals("")) {
					if (new File(str + _pathforcheck).exists()) {

						camera.setImageDrawable(getResources().getDrawable(R.drawable.camera_list_tick));
						image1 = _pathforcheck;

					}
				}

				break;
		}

	}

	public boolean imageAllowed() {
		boolean result = true;

		if (image.equalsIgnoreCase("true")) {

			if (image1.equalsIgnoreCase("")) {

				result = false;

			}
		}

		return result;

	}

	public boolean textAllowed() {
		boolean result = true;

		if (text.getText().toString().trim().equals("")) {

			result = false;

		}

		return result;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.imgcam) {
			_pathforcheck = store_id + "NonWorking" + _UserId + ".jpg";

			_path = Environment.getExternalStorageDirectory() + "/Mccain_Images/" + _pathforcheck;

			startCameraActivity();
		}
		if (v.getId() == R.id.save) {

			if (validatedata()) {

				if (imageAllowed()) {

					if (textAllowed()) {

						AlertDialog.Builder builder = new AlertDialog.Builder(
								NonWorkingReason.this);
						builder.setMessage("Do you want to save the data ")
								.setCancelable(false)
								.setPositiveButton("OK",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {

												alert.getButton(
														AlertDialog.BUTTON_POSITIVE)
														.setEnabled(false);

												CoverageBean cdata = new CoverageBean();
												cdata.setStoreId(store_id);
												cdata.setVisitDate(visit_date);
												cdata.setUserId(_UserId);
												cdata.setInTime(intime);
												cdata.setOutTime(getCurrentTime());
												cdata.setReason(reasonname);
												cdata.setReasonid(reasonid);
												cdata.setLatitude("0.0");
												cdata.setLongitude("0.0");
												cdata.setImage(image1);

												cdata.setRemark(text
														.getText()
														.toString()
														.replaceAll(
																"[&^<>{}'$]",
																" "));
												cdata.setStatus(CommonString.STORE_STATUS_LEAVE);

												database.InsertCoverageData(cdata);

												database.updateStoreStatusOnLeave(
														store_id,
														visit_date,
														CommonString.STORE_STATUS_LEAVE);


												entry="true";
												if (entry
														.equalsIgnoreCase("true")) {

												} else {


												}
												SharedPreferences.Editor editor = preferences.edit();

												editor.putString(CommonString.KEY_STOREVISITED_STATUS+store_id, "No");
												editor.putString(CommonString.KEY_STOREVISITED_STATUS, "");
												editor.putString(CommonString.KEY_STORE_IN_TIME, "");
												editor.putString(CommonString.KEY_LATITUDE, "");
												editor.putString(CommonString.KEY_LONGITUDE, "");
												editor.commit();
												finish();
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

					} else {
						Toast.makeText(getApplicationContext(),
								"Please enter required remark reason",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(getApplicationContext(),
							"Please Capture Image", Toast.LENGTH_SHORT).show();
				}
			}

			else {
				Toast.makeText(getApplicationContext(),
						"Please Select a Reason", Toast.LENGTH_SHORT).show();

			}
		}

	}

	public boolean validatedata() {
		boolean result = false;
		if (reasonid != null && !reasonid.equalsIgnoreCase("")) {
			result = true;
		}

		return result;

	}

	public String getCurrentTime() {
		Calendar m_cal = Calendar.getInstance();
		String intime = m_cal.get(Calendar.HOUR_OF_DAY) + ":" + m_cal.get(Calendar.MINUTE) + ":" + m_cal.get(Calendar.SECOND);
		return intime;

	}
}
