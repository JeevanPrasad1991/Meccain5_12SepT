package com.cpm.gskgtsupervisor;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.cpm.Constants.CommonString;

import com.cpm.autoupdate.AutoupdateActivity;

import com.cpm.dailyentry.DailyEntryScreen;
import com.cpm.dailyentry.Performance;
import com.cpm.dailyentry.StoreEntry;
import com.cpm.dailyentry.UploadSelect;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;


import com.cpm.download.CompleteDownloadActivity;
import com.cpm.upload.CheckoutNUpload;
import com.cpm.upload.UploadDataActivity;

import com.cpm.xmlGetterSetter.JourneyPlanGetterSetter;
import com.cpm.meccain.R;
import com.cpm.message.AlertMessage;

public class MainActivity extends Activity implements OnClickListener {

	Button downloadBtn,dailyEntry,exit,download,upload,performance;
	
	GSKDatabase database;
	ArrayList<JourneyPlanGetterSetter> jcplist;
	private SharedPreferences preferences;
	private String date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainpage);
		
		upload = (Button)findViewById(R.id.upload);
		/*gatemeeting  = (Button)findViewById(R.id.gatemeeting);
     	myperformance = (Button)findViewById(R.id.myperformance);*/
		dailyEntry = (Button)findViewById(R.id.dash_dailyentry);
		download = (Button)findViewById(R.id.download);
		/*teamattendence = (Button)findViewById(R.id.teamattendence);
     	myattendence = (Button)findViewById(R.id.myattendence);*/
		exit = (Button)findViewById(R.id.exit);
		performance = (Button)findViewById(R.id.performance);
		
		database = new GSKDatabase(this);
		database.open();
		upload.setOnClickListener(this);
		/*gatemeeting.setOnClickListener(this);
		myperformance.setOnClickListener(this);*/
		dailyEntry.setOnClickListener(this);
		download.setOnClickListener(this);
		/*teamattendence.setOnClickListener(this);
		myattendence.setOnClickListener(this);
		 */		exit.setOnClickListener(this);
		performance.setOnClickListener(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		date = preferences.getString(CommonString.KEY_DATE, null);

	}

	@Override

	protected void onStart(){
		super.onStart();

	}

	@Override
	public void onClick(View v) {

		if(v.getId()==R.id.download){
			
			if(checkNetIsAvailable()){

	                if (database.isCoverageDataFilled(date)) {

	                  //  Toast.makeText(getBaseContext(), "Please Upload Data First", Toast.LENGTH_LONG).show();
	                    
	            		AlertDialog.Builder builder = new AlertDialog.Builder(this);
	            		builder.setTitle("Parinaam");
	            		builder.setMessage("Please Upload Previous Data First")
	            				.setCancelable(false)
	            				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	            					public void onClick(DialogInterface dialog, int id) {

	            						  Intent startUpload = 	new Intent(MainActivity.this,CheckoutNUpload.class);
	            		    				startActivity(startUpload);
	            		    				MainActivity.this.finish();

	            					}
	            				});
	            		AlertDialog alert = builder.create();

	            		alert.show();

	                } else {
	                	Intent startDownload = 	new Intent(MainActivity.this,CompleteDownloadActivity.class);
	    				startActivity(startDownload);
	    				MainActivity.this.finish();
	                }
				
			}
			else{
				Toast.makeText(getApplicationContext(), "No Network Available", Toast.LENGTH_SHORT).show();
			}
			
		}else if(v.getId()==R.id.dash_dailyentry){
			Intent startDownload = 	new Intent(MainActivity.this,DailyEntryScreen.class);
			startActivity(startDownload);
			
			overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
			
			//finish();
		}
		else if(v.getId()==R.id.gatemeeting){

		}

		else if(v.getId()==R.id.exit){
			Intent startDownload = 	new Intent(MainActivity.this,LoginActivity.class);
			startActivity(startDownload);
			MainActivity.this.finish();
			
		}
		else if(v.getId()==R.id.upload){
			if (checkNetIsAvailable()) {

				jcplist=database.getJCPData(date);

				if (jcplist.size() == 0) {
					Toast.makeText(getBaseContext(), "Please Download Data First", Toast.LENGTH_LONG).show();
				} else {

					if(preferences.getString(CommonString.KEY_STOREVISITED_STATUS, "").equals("Yes")){
						Toast.makeText(getApplicationContext(), "First checkout of store", Toast.LENGTH_SHORT).show();
					}
					else{
						
						ArrayList<CoverageBean> cdata = new ArrayList<CoverageBean>();

						cdata = database.getCoverageData(date);

						if (cdata.size() == 0) {

							Toast.makeText(getBaseContext(), AlertMessage.MESSAGE_NO_DATA,
									Toast.LENGTH_LONG).show();

						} else {
							Intent i = new Intent(getBaseContext(),
									UploadDataActivity.class);
							i.putExtra("UploadAll", false);
							startActivity(i);

					finish();

						}
						
					}

				}

			} else {
				Toast.makeText(getApplicationContext(), "No Network Available", Toast.LENGTH_SHORT).show();
			}
		}else if(v.getId()==R.id.performance){
			Intent startPerformance = 	new Intent(MainActivity.this,Performance.class);
			startActivity(startPerformance);
			 overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		}
	}

	
	
	@Override
	public void onBackPressed() {

	}
	
	public boolean checkNetIsAvailable(){
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.save_database:

			AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
			builder1.setMessage(
					"Are you sure you want to take the backup of your data")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
						@SuppressLint("SimpleDateFormat")
						@SuppressWarnings("resource")
						public void onClick(DialogInterface dialog,
								int id) {
							try {

								File file = new File(Environment.getExternalStorageDirectory(), "meccain_backup");
								if (!file.isDirectory()) {
									file.mkdir();
								}

								File sd = Environment.getExternalStorageDirectory();
								File data = Environment.getDataDirectory();

								if (sd.canWrite()) {
									long date = System.currentTimeMillis(); 

									SimpleDateFormat sdf = new SimpleDateFormat("MMM/MM/dd");
									String dateString = sdf.format(date);   
									
									String currentDBPath = "//data//com.cpm.meccain//databases//"+GSKDatabase.DATABASE_NAME;
									String backupDBPath = "meccain_backup" + dateString.replace('/', '-');

									String path=Environment.getExternalStorageDirectory().getPath();

									File currentDB = new File(data, currentDBPath);
									File backupDB = new File(path, backupDBPath);

									if (currentDB.exists()) {
										@SuppressWarnings("resource")
										FileChannel src = new FileInputStream(currentDB).getChannel();
										FileChannel dst = new FileOutputStream(backupDB).getChannel();
										dst.transferFrom(src, 0, src.size());
										src.close();
										dst.close();
									}
								}
							} catch (Exception e) {
								System.out.println(e.getMessage());
							}

						}
					})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert1 = builder1.create();
			alert1.show();


			break;

		}
		return super.onOptionsItemSelected(item);
	}
	
}
