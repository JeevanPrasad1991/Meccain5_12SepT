package com.cpm.dailyentry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cpm.Constants.CommonString;
import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.keyboard.BasicOnKeyboardActionListener;
import com.cpm.keyboard.CustomKeyboardView;
import com.cpm.meccain.R;
import com.cpm.xmlGetterSetter.FoodStoreCompetitionGetterSetter;
import com.cpm.xmlGetterSetter.FoodStoreCompetitionGetterSetter;
import com.cpm.xmlGetterSetter.OpeningStockInsertDataGetterSetter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class FoodStoreComplete extends Activity implements OnClickListener{

	static int currentapiVersion = 1;
	boolean checkflag=true;
	List<Integer> checkHeaderArray = new ArrayList<Integer>();
	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	Button btnSave;
	TextView tvheader;
	List<OpeningStockInsertDataGetterSetter> listDataHeader;
	HashMap<OpeningStockInsertDataGetterSetter, List<FoodStoreCompetitionGetterSetter>> listDataChild;

	private SharedPreferences preferences;
	String store_cd;
	ArrayList<OpeningStockInsertDataGetterSetter> brandData;
	ArrayList<FoodStoreCompetitionGetterSetter> skuData;

	GSKDatabase db;
	String sku_cd;
	CustomKeyboardView mKeyboardView;
	Keyboard mKeyboard;
	String visit_date,username,intime;
	boolean ischangedflag=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.food_store_complete_layout);
		currentapiVersion = android.os.Build.VERSION.SDK_INT;
		// get the list view
		expListView = (ExpandableListView) findViewById(R.id.lvExp);
		btnSave=(Button) findViewById(R.id.save_btn);
		tvheader=(TextView) findViewById(R.id.txt_idealFor);
		mKeyboard = new Keyboard(this, R.xml.keyboard);
		mKeyboardView = (CustomKeyboardView) findViewById(R.id.keyboard_view);
		mKeyboardView.setKeyboard(mKeyboard);
		mKeyboardView.setOnKeyboardActionListener(new BasicOnKeyboardActionListener(this));

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
				expListView.invalidateViews();
				if (SCROLL_STATE_TOUCH_SCROLL == scrollState) {
					View currentFocus = getCurrentFocus();
					if (currentFocus != null) {
						currentFocus.clearFocus();
					}
				}
			}

		});

		// Listview Group click listener
		expListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
										int groupPosition, long id) {
				// Toast.makeText(getApplicationContext(),
				// "Group Clicked " + listDataHeader.get(groupPosition),
				// Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		// Listview Group expanded listener
		expListView.setOnGroupExpandListener(new OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {

				/*for(int i=0;i<listDataHeader.size();i++){
	                if(i==groupPosition){
	                    //do nothing
	                	}
	                    else{
	                    	expListView.collapseGroup(i);
	                    }
	                }*/

				/*Toast.makeText(getApplicationContext(),
						listDataHeader.get(groupPosition).getBrand() + " Expanded",
						Toast.LENGTH_SHORT).show();*/
			}
		});

		// Listview Group collasped listener
		expListView.setOnGroupCollapseListener(new OnGroupCollapseListener() {

			@Override
			public void onGroupCollapse(int groupPosition) {
				/*Toast.makeText(getApplicationContext(),
						listDataHeader.get(groupPosition).getBrand() + " Collapsed",
						Toast.LENGTH_SHORT).show();*/
				if (mKeyboardView.getVisibility() == View.VISIBLE) {
					mKeyboardView.setVisibility(View.INVISIBLE);
					/*mKeyboardView.requestFocusFromTouch();*/
				}

			}
		});

		// Listview on child click listener
		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				// TODO Auto-generated method stub
				Toast.makeText(
						getApplicationContext(),
						listDataHeader.get(groupPosition).getBrand()
								+ " : "
								+ listDataChild.get(
								listDataHeader.get(groupPosition)).get(
								childPosition).getSku(), Toast.LENGTH_SHORT)
						.show();


				findViewById(R.id.lvExp).setVisibility(View.INVISIBLE);
				findViewById(R.id.entry_data).setVisibility(View.VISIBLE);
				tvheader.setText(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getSku());
				sku_cd=listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getSku_cd();

				return false;
			}
		});
	}

	/*
	 * Preparing the list data
	 */

	private void prepareListData() {
		listDataHeader = new ArrayList<OpeningStockInsertDataGetterSetter>();
		listDataChild = new HashMap<OpeningStockInsertDataGetterSetter, List<FoodStoreCompetitionGetterSetter>>();

		brandData=db.getFoodStoreAvailabilityData(store_cd);

		if(brandData.size()>0){

			// Adding child data
			for(int i=0;i<brandData.size();i++){
				listDataHeader.add(brandData.get(i));

				skuData=db.getFoodStoreDataFromDatabase(store_cd, brandData.get(i).getBrand_cd());
				if(!(skuData.size()>0) || (skuData.get(0).getMccain_df()==null) || (skuData.get(0).getMccain_df().equals(""))){
					skuData=db.getFoodSkuData(brandData.get(i).getBrand_cd(),store_cd);
				}
				else{
					btnSave.setText("Update");
				}

				List<FoodStoreCompetitionGetterSetter> skulist = new ArrayList<FoodStoreCompetitionGetterSetter>();
				for(int j=0;j<skuData.size();j++){
					skulist.add(skuData.get(j));
				}

				listDataChild.put(listDataHeader.get(i), skulist); // Header, Child data
			}

		}

	}


	public class ExpandableListAdapter extends BaseExpandableListAdapter {

		private Context _context;
		private List<OpeningStockInsertDataGetterSetter> _listDataHeader; // header titles
		// child data in format of header title, child title
		private HashMap<OpeningStockInsertDataGetterSetter, List<FoodStoreCompetitionGetterSetter>> _listDataChild;

		public ExpandableListAdapter(Context context, List<OpeningStockInsertDataGetterSetter> listDataHeader,
									 HashMap<OpeningStockInsertDataGetterSetter, List<FoodStoreCompetitionGetterSetter>> listChildData) {
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

			final FoodStoreCompetitionGetterSetter childText = (FoodStoreCompetitionGetterSetter) getChild(groupPosition, childPosition);

			ViewHolder holder=null;

			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.food_complete_entry, null);

				holder=new ViewHolder();

				holder.cardView=(CardView) convertView.findViewById(R.id.card_view);
				
				holder.etmrp_mccan=(EditText) convertView.findViewById(R.id.etmrp_mccan);
				holder.et_slab1_mccain=(EditText) convertView.findViewById(R.id.et_slab1_mccain);
				holder.et_slab2_mccain=(EditText) convertView.findViewById(R.id.et_slab2_mccain);
				holder.et_slab3_mccain=(EditText) convertView.findViewById(R.id.et_slab3_mccain);

				holder.etmrp_hyphen=(EditText) convertView.findViewById(R.id.etmrp_hyphen);
				holder.et_slab1_hyphen=(EditText) convertView.findViewById(R.id.et_slab1_hyphen);
				holder.et_slab2_hyphen=(EditText) convertView.findViewById(R.id.et_slab2_hyphen);
				holder.et_slab3_hyphen=(EditText) convertView.findViewById(R.id.et_slab3_hyphen);

				holder.tv_mccain_sku=(TextView) convertView.findViewById(R.id.tv_mccain_sku);
				holder.tv_hyphen_sku=(TextView) convertView.findViewById(R.id.tv_hyphen_sku);

				convertView.setTag(holder);

			}
			holder = (ViewHolder) convertView.getTag();

			if (currentapiVersion >= 11) {
				//holder.etaspermcn.setTextIsSelectable(true);
				holder.etmrp_mccan.setTextIsSelectable(true);
				holder.et_slab1_mccain.setTextIsSelectable(true);
				holder.et_slab2_mccain.setTextIsSelectable(true);
				holder.et_slab3_mccain.setTextIsSelectable(true);

				holder.etmrp_hyphen.setTextIsSelectable(true);
				holder.et_slab1_hyphen.setTextIsSelectable(true);
				holder.et_slab2_hyphen.setTextIsSelectable(true);
				holder.et_slab3_hyphen.setTextIsSelectable(true);
				
				//holder.etaspermcn.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.etmrp_mccan.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab1_mccain.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab2_mccain.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab3_mccain.setRawInputType(InputType.TYPE_CLASS_TEXT);

				holder.etmrp_hyphen.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab1_hyphen.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab2_hyphen.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.et_slab3_hyphen.setRawInputType(InputType.TYPE_CLASS_TEXT);


			} else {
				//holder.etaspermcn.setInputType(0);
				holder.etmrp_mccan.setInputType(0);
				holder.et_slab1_mccain.setInputType(0);
				holder.et_slab2_mccain.setInputType(0);
				holder.et_slab3_mccain.setInputType(0);

				holder.etmrp_hyphen.setInputType(0);
				holder.et_slab1_hyphen.setInputType(0);
				holder.et_slab2_hyphen.setInputType(0);
				holder.et_slab3_hyphen.setInputType(0);

			}



			holder.etmrp_mccan.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setPrice("");

						} else {

							ischangedflag=true;
							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setPrice(value1);

						}

					}
				}
			});

			holder.et_slab1_mccain.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab1("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab1(value1);

						}

					}

				}
			});

			holder.et_slab2_mccain.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab2("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab2(value1);

						}

					}

				}
			});
			holder.et_slab3_mccain.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab3("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab3(value1);

						}

					}

				}
			});



			holder.etmrp_hyphen.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setPrice("");

						} else {

							ischangedflag=true;
							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setPrice(value1);

						}

					}
				}
			});

			holder.et_slab1_hyphen.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab1_hyphan("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab1_hyphan(value1);

						}

					}

				}
			});

			holder.et_slab2_hyphen.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab2_hyphan("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab2_hyphan(value1);

						}

					}

				}
			});
			holder.et_slab3_hyphen.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab3_hyphan("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSlab3_hyphan(value1);

						}

					}

				}
			});


			holder.etmrp_hyphen.setInputType(0);
			holder.et_slab1_hyphen.setInputType(0);
			holder.et_slab2_hyphen.setInputType(0);
			holder.et_slab3_hyphen.setInputType(0);

			holder.etmrp_mccan.setId(childPosition);
			holder.et_slab1_mccain.setId(childPosition);
			holder.et_slab2_mccain.setId(childPosition);
			holder.et_slab3_mccain.setId(childPosition);

			holder.etmrp_hyphen.setId(childPosition);
			holder.et_slab1_hyphen.setId(childPosition);
			holder.et_slab2_hyphen.setId(childPosition);
			holder.et_slab3_hyphen.setId(childPosition);

			//holder.etaspermcn.setText(childText.getAs_per_meccain());
			holder.etmrp_mccan.setText(childText.getPrice());
			holder.et_slab1_mccain.setText(childText.getSlab1());
			holder.et_slab2_mccain.setText(childText.getSlab2());
			holder.et_slab3_mccain.setText(childText.getSlab3());

			holder.etmrp_hyphen.setText(childText.getPrice_hyphan());
			holder.et_slab1_hyphen.setText(childText.getSlab1_hyphan());
			holder.et_slab2_hyphen.setText(childText.getSlab2_hyphan());
			holder.et_slab3_hyphen.setText(childText.getSlab3_hyphan());


			holder.tbactual.setChecked(_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getActual_listed().equals("YES"));

			_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSku_cd(childText.getSku_cd());

			TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);

			txtListChild.setText(childText.getSku());


			if(!checkflag){

				boolean tempflag=false;

				if(holder.etmrp_mccan.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.etmrp_mccan.setHintTextColor(getResources().getColor(R.color.red));
					holder.etmrp_mccan.setHint("Empty");
					tempflag=true;
				}

				if(holder.et_slab1_mccain.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab1_mccain.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab1_mccain.setHint("Empty");
					tempflag=true;
				}

				if(holder.et_slab2_mccain.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab2_mccain.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab2_mccain.setHint("Empty");
					tempflag=true;
				}
				if(holder.et_slab3_mccain.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab3_mccain.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab3_mccain.setHint("Empty");
					tempflag=true;
				}


				if(holder.etmrp_hyphen.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.etmrp_hyphen.setHintTextColor(getResources().getColor(R.color.red));
					holder.etmrp_hyphen.setHint("Empty");
					tempflag=true;
				}

				if(holder.et_slab1_hyphen.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab1_hyphen.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab1_hyphen.setHint("Empty");
					tempflag=true;
				}

				if(holder.et_slab2_hyphen.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab2_hyphen.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab2_hyphen.setHint("Empty");
					tempflag=true;
				}
				if(holder.et_slab3_hyphen.getText().toString().equals("")){
					//holder.etclstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
					holder.et_slab3_hyphen.setHintTextColor(getResources().getColor(R.color.red));
					holder.et_slab3_hyphen.setHint("Empty");
					tempflag=true;
				}
				if(tempflag){

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.red));
				}
				else{

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
				}

			}

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
			OpeningStockInsertDataGetterSetter headerTitle = (OpeningStockInsertDataGetterSetter) getGroup(groupPosition);
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.list_group_complete, null);
			}

			TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
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

		EditText etmrp_mccan,et_slab1_mccain,et_slab2_mccain,et_slab3_mccain;
		EditText etmrp_hyphen,et_slab1_hyphen,et_slab2_hyphen,et_slab3_hyphen;
		TextView tv_mccain_sku,tv_hyphen_sku;

		CardView cardView;
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

										db.deleteFoodStoreData(store_cd);
										db.InsertFoodStorelistData(store_cd, listDataChild, listDataHeader);
										Toast.makeText(getApplicationContext(),
												"Data has been saved", Toast.LENGTH_SHORT).show();

						/*Intent DailyEntryMenu = new Intent(
								FoodStore.this,
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

	/***
	 * Display the screen keyboard with an animated slide from bottom
	 */
	private void showKeyboardWithAnimation() {
		if (mKeyboardView.getVisibility() == View.GONE) {
			Animation animation = AnimationUtils.loadAnimation(FoodStoreComplete.this, R.anim.slide_in_bottom);
			mKeyboardView.showWithAnimation(animation);
		}
		else if(mKeyboardView.getVisibility() == View.INVISIBLE){
			mKeyboardView.setVisibility(View.VISIBLE);
		}
	}


	public void hideSoftKeyboard() {
		try {
			InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void hide() {
		mKeyboardView.setVisibility(View.INVISIBLE);
	/*	// mKeyboardView.clearFocus();
		mKeyboardView.requestFocusFromTouch();*/

	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		if (mKeyboardView.getVisibility() == View.VISIBLE) {
			mKeyboardView.setVisibility(View.INVISIBLE);
			/*mKeyboardView.requestFocusFromTouch();*/
		} else {

			if(ischangedflag){

				AlertDialog.Builder builder = new AlertDialog.Builder(FoodStoreComplete.this);
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

			}else{
				finish();
				overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
			}

		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mKeyboardView.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mKeyboardView.setKeyboard(mKeyboard);
		mKeyboardView.setVisibility(View.INVISIBLE);
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
			HashMap<OpeningStockInsertDataGetterSetter, List<FoodStoreCompetitionGetterSetter>> listDataChild2,
			List<OpeningStockInsertDataGetterSetter> listDataHeader2) {

		checkHeaderArray.clear();

		for (int i = 0; i < listDataHeader2.size(); i++) {
			for (int j = 0; j < listDataChild2.get(listDataHeader2.get(i))
					.size(); j++) {
				/*String aspermccain = listDataChild2.get(listDataHeader2.get(i)).get(j).getAs_per_meccain();*/
				String meccaindf = listDataChild2.get(listDataHeader2.get(i)).get(j).getMccain_df();
				//String mtdsales = listDataChild2.get(listDataHeader2.get(i)).get(j).getMtd_sales();
				String storedf = listDataChild2.get(listDataHeader2.get(i)).get(j).getStore_df();
				String packaging_size = listDataChild2.get(listDataHeader2.get(i)).get(j).getPacking_size();

				if (meccaindf.equalsIgnoreCase("") || storedf.equalsIgnoreCase("") ) {

					if(!checkHeaderArray.contains(i)){
						checkHeaderArray.add(i);
					}

					checkflag=false;
					break;

				} else{

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

}
