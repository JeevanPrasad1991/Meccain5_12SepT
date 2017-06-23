package com.cpm.dailyentry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.cpm.Constants.CommonString;
import com.cpm.dailyentry.PromotionActivity.ExpandableListAdapter;
import com.cpm.dailyentry.PromotionActivity.ViewHolder;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.meccain.R;
import com.cpm.xmlGetterSetter.AssetInsertdataGetterSetter;
import com.cpm.xmlGetterSetter.OpeningStockInsertDataGetterSetter;
import com.cpm.xmlGetterSetter.PromotionInsertDataGetterSetter;
import com.cpm.xmlGetterSetter.StockGetterSetter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AbsListView.OnScrollListener;

public class AssetActivity extends Activity implements OnClickListener{

	boolean checkflag=true;
	List<Integer> checkHeaderArray = new ArrayList<Integer>();

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	Button btnSave;

	List<AssetInsertdataGetterSetter> listDataHeader;
	HashMap<AssetInsertdataGetterSetter, List<AssetInsertdataGetterSetter>> listDataChild;

	ArrayList<AssetInsertdataGetterSetter> brandData;
	ArrayList<AssetInsertdataGetterSetter> skuData;

	AssetInsertdataGetterSetter insertData=new AssetInsertdataGetterSetter();

	GSKDatabase db;

	private SharedPreferences preferences;

	String store_cd,visit_date,username,intime;

	ImageView img;
	
	boolean ischangedflag=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.asset_layout);

		expListView = (ExpandableListView) findViewById(R.id.lvExp);

		btnSave=(Button) findViewById(R.id.save_btn);

		img=(ImageView) findViewById(R.id.imgnodata);

		db=new GSKDatabase(getApplicationContext());
		db.open();

		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		store_cd = preferences.getString(CommonString.KEY_STORE_CD, null);

		visit_date = preferences.getString(CommonString.KEY_DATE, null);
		username= preferences.getString(CommonString.KEY_USERNAME, null);
		intime=getCurrentTime();

		// preparing list data
		prepareListData();

		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);

		btnSave.setOnClickListener(this);

		expListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int scrollState) {
				if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						currentFocus.clearFocus();
					}
				}
			//	expListView.invalidateViews();
			}

		});

	}

	private void prepareListData() {
		listDataHeader = new ArrayList<AssetInsertdataGetterSetter>();
		listDataChild = new HashMap<AssetInsertdataGetterSetter, List<AssetInsertdataGetterSetter>>();

		/*brandData=db.getAssetUpload(store_cd);

		if(brandData.size()<0){

		}*/



		brandData=db.getAssetBrandData(store_cd);

		if(brandData.size()>0){

			// Adding child data

			for(int i=0;i<brandData.size();i++){
				listDataHeader.add(brandData.get(i));

				skuData=db.getAssetDataFromdatabase(store_cd, brandData.get(i).getBrand_cd());
				if(!(skuData.size()>0) || (skuData.get(0).getAsset()==null) || (skuData.get(0).getAsset().equals(""))){
					skuData=db.getAssetData(brandData.get(i).getBrand_cd(),store_cd);
				}
				else{
					btnSave.setText("Update");
				}

				List<AssetInsertdataGetterSetter> skulist = new ArrayList<AssetInsertdataGetterSetter>();
				for(int j=0;j<skuData.size();j++){
					skulist.add(skuData.get(j));
				}

				listDataChild.put(listDataHeader.get(i), skulist); // Header, Child data
			}

		}
		else{
			expListView.setVisibility(View.GONE);
			btnSave.setVisibility(View.INVISIBLE);
			img.setVisibility(View.VISIBLE);
		}




	}



	public class ExpandableListAdapter extends BaseExpandableListAdapter {

		private Context _context;
		private List<AssetInsertdataGetterSetter> _listDataHeader; // header titles
		// child data in format of header title, child title
		private HashMap<AssetInsertdataGetterSetter, List<AssetInsertdataGetterSetter>> _listDataChild;

		public ExpandableListAdapter(Context context, List<AssetInsertdataGetterSetter> listDataHeader,
				HashMap<AssetInsertdataGetterSetter, List<AssetInsertdataGetterSetter>> listChildData) {
			this._context = context;
			this._listDataHeader = listDataHeader;
			this._listDataChild = listChildData;

		}

		@Override
		public Object getChild(int groupPosition, int childPosititon) {
			return this._listDataChild.get(this._listDataHeader.get(groupPosition))
					.get(childPosititon);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@SuppressLint("NewApi")
		@Override
		public View getChildView(final int groupPosition, final int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {

			final AssetInsertdataGetterSetter childText = (AssetInsertdataGetterSetter) getChild(groupPosition, childPosition);

			ViewHolder holder=null;

			if (convertView == null) {



				LayoutInflater infalInflater = (LayoutInflater) this._context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.asset_entry, null);

				holder=new ViewHolder();

				holder.cardView=(CardView) convertView.findViewById(R.id.card_view);
				holder.etremark=(EditText) convertView.findViewById(R.id.etremarks);
				holder.tbpresent=(ToggleButton) convertView.findViewById(R.id.tbpresent);
				//holder.tvpromo=(TextView) convertView.findViewById(R.id.tvpromotion_txt);

				convertView.setTag(holder);

			}

			holder = (ViewHolder) convertView.getTag();

			holder.tbpresent.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					ischangedflag=true;
					String val = ((ToggleButton) v).getText().toString();
					_listDataChild.get(listDataHeader.get(groupPosition))
					.get(childPosition).setPresent(val);

					expListView.invalidateViews();
				}
			});

			holder.etremark.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {



					if (!hasFocus) {
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString().replaceAll("[&^<>{}'$]", "");
						if (value1.equals("")) {

							_listDataChild
							.get(listDataHeader.get(groupPosition))
							.get(childPosition).setRemark("");

						} else {
							ischangedflag=true;
							_listDataChild
							.get(listDataHeader.get(groupPosition))
							.get(childPosition).setRemark(value1);

						}

					}

				}
			});



			holder.etremark.setText(childText.getRemark());

			holder.tbpresent.setChecked(_listDataChild
					.get(listDataHeader.get(groupPosition))
					.get(childPosition).getPresent().equals("YES"));

			if(_listDataChild
					.get(listDataHeader.get(groupPosition))
					.get(childPosition).getPresent().equals("YES")){
				holder.etremark.setVisibility(View.INVISIBLE);
			}else if(_listDataChild
					.get(listDataHeader.get(groupPosition))
					.get(childPosition).getPresent().equals("NO")){
				holder.etremark.setVisibility(View.VISIBLE);
			}

			TextView txtListChild = (TextView) convertView
					.findViewById(R.id.lblListItem);

			txtListChild.setText(childText.getAsset());

			if(!checkflag){
				boolean tempflag=false;
				if(_listDataChild
					.get(listDataHeader.get(groupPosition))
					.get(childPosition).getPresent().equals("NO"))
				if(holder.etremark.getText().toString().equals("")){
					//holder.etclstkcold.setBackgroundColor(getResources().getColor(R.color.red));
					holder.etremark.setHintTextColor(getResources().getColor(R.color.red));
					holder.etremark.setHint("Empty");
					tempflag=true;
				}

				if(tempflag){

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.red));
				}
				else{

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
				}

			}

			//holder.tvpromo.setText(childText.getAsset());
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this._listDataChild.get(this._listDataHeader.get(groupPosition))
					.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return this._listDataHeader.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return this._listDataHeader.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			AssetInsertdataGetterSetter headerTitle = (AssetInsertdataGetterSetter) getGroup(groupPosition);
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.list_group, null);
			}

			TextView lblListHeader = (TextView) convertView
					.findViewById(R.id.lblListHeader);
			lblListHeader.setTypeface(null, Typeface.BOLD);
			lblListHeader.setText(headerTitle.getBrand());

			if(!checkflag){
				if(checkHeaderArray.contains(groupPosition)){
					lblListHeader.setBackgroundColor(getResources().getColor(R.color.red));
				}
				else{
					lblListHeader.setBackgroundColor(getResources().getColor(R.color.teal_dark));
				}
			}

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}


	public class ViewHolder{

		EditText etremark;
		ToggleButton tbpresent;
		CardView cardView;
		//TextView tvpromo;
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id=v.getId();

		if(id==R.id.save_btn){
			expListView.clearFocus();
			if(validateData(listDataChild, listDataHeader)){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Are you sure you want to save")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int id) {

						db.open();

						getMid();

						db.deleteAssetData(store_cd);
						db.InsertAssetData(
								store_cd, listDataChild,
								listDataHeader);

						Toast.makeText(getApplicationContext(),
								"Data has been saved", Toast.LENGTH_SHORT).show();

						/*Intent DailyEntryMenu = new Intent(
								AssetActivity.this,
								StoreEntry.class);
						startActivity(DailyEntryMenu);*/

						finish();
					}
				})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();

				alert.show();

			}
			else{
				listAdapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
			}

		}
	}

	public long checkMid() {
		return db.CheckMid(visit_date, store_cd);
	}

	public long getMid() {

		long mid = 0;

		mid = checkMid();

		if (mid == 0) {
			CoverageBean cdata = new CoverageBean();
			cdata.setStoreId(store_cd);
			cdata.setVisitDate(visit_date);
			cdata.setUserId(username);
			cdata.setInTime(intime);
			cdata.setOutTime(getCurrentTime());
			cdata.setReason("");
			cdata.setReasonid("0");
			cdata.setLatitude("0.0");
			cdata.setLongitude("0.0");
			cdata.setStatus(CommonString.KEY_CHECK_IN);
			mid = db.InsertCoverageData(cdata);

		}

		return mid;
	}

	public String getCurrentTime() {

		Calendar m_cal = Calendar.getInstance();

		String intime = m_cal.get(Calendar.HOUR_OF_DAY) + ":"
				+ m_cal.get(Calendar.MINUTE) + ":" + m_cal.get(Calendar.SECOND);

		return intime;

	}

	boolean validateData(
			HashMap<AssetInsertdataGetterSetter, List<AssetInsertdataGetterSetter>> listDataChild2,
			List<AssetInsertdataGetterSetter> listDataHeader2) {
		//boolean flag = true;
		checkHeaderArray.clear();

		for (int i = 0; i < listDataHeader2.size(); i++) {
			for (int j = 0; j < listDataChild2.get(listDataHeader2.get(i))
					.size(); j++) {
				/*String aspermccain = listDataChild2.get(listDataHeader2.get(i)).get(j).getAs_per_meccain();*/
				String present= listDataChild2.get(listDataHeader2.get(i)).get(j).getPresent();
				String remark = listDataChild2.get(listDataHeader2.get(i)).get(j).getRemark();

				if(present.equalsIgnoreCase("NO")){
					if (remark.equalsIgnoreCase("")) {

						if(!checkHeaderArray.contains(i)){
							checkHeaderArray.add(i);
						}

						checkflag=false;

						//flag = false;
						break;

					} else{
						checkflag=true;
						//flag = true;
					}
				}
				else{
					checkflag=true;
				}


			}

			if(checkflag == false){
				break;
			}

		}

		//expListView.invalidate();

		return checkflag;

	}

	@Override
	public void onBackPressed() {
		/*Intent i = new Intent(this, StoreEntry.class);
		startActivity(i);*/
		
		if(ischangedflag){
			AlertDialog.Builder builder = new AlertDialog.Builder(
					AssetActivity.this);
			builder.setMessage(CommonString.ONBACK_ALERT_MESSAGE)
			.setCancelable(false)
			.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
				public void onClick(
						DialogInterface dialog, int id) {
				
					finish();

					overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
				}
			})
			.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
				public void onClick(
						DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		else{
			finish();

			overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
		}
		
	
		
	}

}
