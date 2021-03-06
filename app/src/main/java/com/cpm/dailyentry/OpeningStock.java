package com.cpm.dailyentry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cpm.Constants.CommonString;

import com.cpm.database.GSKDatabase;
import com.cpm.delegates.CoverageBean;
import com.cpm.keyboard.BasicOnKeyboardActionListener;
import com.cpm.keyboard.CustomKeyboardView;
import com.cpm.meccain.R;
import com.cpm.xmlGetterSetter.DeepFreezerTypeGetterSetter;
import com.cpm.xmlGetterSetter.OpeningStockInsertDataGetterSetter;
import com.cpm.xmlGetterSetter.StockGetterSetter;

@SuppressLint("ClickableViewAccessibility")
public class OpeningStock extends Activity implements OnClickListener{

	boolean validate=true;
	boolean flagcoldroom=false;
	boolean flagmccain=false;
	boolean flagstoredf=false;
	int valHeadCount;
	int valChildCount;
	List<Integer> checkValidHeaderArray = new ArrayList<Integer>();
	List<Integer> checkValidChildArray = new ArrayList<Integer>();
	boolean checkflag=true;
	static int currentapiVersion = 1;
	List<Integer> checkHeaderArray = new ArrayList<Integer>();
	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	Button btnSave;
	TextView tvheader;
	int saveBtnFlag=0;
	int arrayEditText[]={R.id.etAs_Per_Mccain,R.id.etactual_listed,R.id.etOpening_Stock_Cold_Room,R.id.etOpening_Stock_Mccain_Df,R.id.etTotal_Facing_McCain_DF,R.id.etOpening_Stock_Store_DF,R.id.etTotal_Facing_Store_DF,R.id.etmaterial_wellness_thawed_quantity};

	List<OpeningStockInsertDataGetterSetter> listDataHeader;
	HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> listDataChild;

	private SharedPreferences preferences;
	String store_cd;
	ArrayList<OpeningStockInsertDataGetterSetter> brandData;
	ArrayList<StockGetterSetter> skuData;

	OpeningStockInsertDataGetterSetter insertData=new OpeningStockInsertDataGetterSetter();
	GSKDatabase db;
	String sku_cd;

	CustomKeyboardView mKeyboardView;
	Keyboard mKeyboard;

	String visit_date,username,intime;

	private ArrayList<StockGetterSetter> stockData = new ArrayList<StockGetterSetter>();
	boolean dataExists=false;

	boolean openmccaindfFlag=false;

	String Error_Message;

	boolean ischangedflag=false;

	ArrayList<DeepFreezerTypeGetterSetter> deepFreezlist=new ArrayList<DeepFreezerTypeGetterSetter>();

	boolean mccainFlag=false;
	boolean storeFlag=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opening_stock);

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

		deepFreezlist = db.getDFTypeData("McCain",store_cd);
		if(deepFreezlist.get(0).getStatus().equals("YES"))
		{
			mccainFlag=true;
		}

		deepFreezlist = db.getDFTypeData("Store",store_cd);
		if(deepFreezlist.get(0).getStatus().equals("YES"))
		{
			storeFlag=true;
		}

		// preparing list data
		prepareListData();

		openmccaindfFlag=preferences.getBoolean("opnestkmccaindf", false);

		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);

		btnSave.setOnClickListener(this);

		expListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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

				saveBtnFlag=1;

				return false;
			}
		});

	}

	/*
	 * Preparing the list data
	 */

	private void prepareListData() {
		listDataHeader = new ArrayList<OpeningStockInsertDataGetterSetter>();
		listDataChild = new HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>>();

		brandData=db.getStockAvailabilityData(store_cd);

		if(brandData.size()>0){

			// Adding child data

			for(int i=0;i<brandData.size();i++){
				listDataHeader.add(brandData.get(i));

				skuData=db.getOpeningStockDataFromDatabase(store_cd, brandData.get(i).getBrand_cd());
				if(!(skuData.size()>0) || (skuData.get(0).getOpen_stock_cold_room()==null) || (skuData.get(0).getOpen_stock_cold_room().equals(""))){
					skuData=db.getStockSkuData(brandData.get(i).getBrand_cd(),store_cd);
				}
				else{
					btnSave.setText("Update");
				}

				List<StockGetterSetter> skulist = new ArrayList<StockGetterSetter>();
				for(int j=0;j<skuData.size();j++){
					skulist.add(skuData.get(j));
				}

				listDataChild.put(listDataHeader.get(i), skulist); // Header, Child data
			}

		}

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

										setBlank(listDataChild,listDataHeader);

										dataExists=db.checkStock(store_cd);
										if(dataExists){
											db.UpdateOpeningStocklistData(store_cd, listDataChild, listDataHeader);
										}
										else{
											db.InsertOpeningStocklistData(store_cd, listDataChild, listDataHeader);
										}
										//db.deleteOpeningStockData(store_cd);


										Toast.makeText(getApplicationContext(), "Data has been saved", Toast.LENGTH_SHORT).show();

										finish();

										overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
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
				Toast.makeText(getApplicationContext(), Error_Message, Toast.LENGTH_SHORT).show();
			}

		}
	}

	public void hideSoftKeyboard(View view){
		InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}



	public class ExpandableListAdapter extends BaseExpandableListAdapter {

		private Context _context;
		private List<OpeningStockInsertDataGetterSetter> _listDataHeader; // header titles
		// child data in format of header title, child title
		private HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> _listDataChild;

		public ExpandableListAdapter(Context context, List<OpeningStockInsertDataGetterSetter> listDataHeader,
									 HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> listChildData) {
			this._context = context;
			this._listDataHeader = listDataHeader;
			this._listDataChild = listChildData;

		}

		@Override
		public Object getChild(int groupPosition, int childPosititon) {
			return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosititon);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@SuppressLint("NewApi")
		@Override
		public View getChildView(final int groupPosition, final int childPosition,
								 boolean isLastChild, View convertView, ViewGroup parent) {

			final StockGetterSetter childText = (StockGetterSetter) getChild(groupPosition, childPosition);

			ViewHolder holder=null;

			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.list_item, null);

				holder=new ViewHolder();

				holder.cardView=(CardView) convertView.findViewById(R.id.card_view);
				holder.etaspermcn=(TextView) convertView.findViewById(R.id.etAs_Per_Mccain);
				holder.etopnstkcldrm=(EditText) convertView.findViewById(R.id.etOpening_Stock_Cold_Room);
				holder.etopnstkmcndf=(EditText) convertView.findViewById(R.id.etOpening_Stock_Mccain_Df);
				holder.ettotalfacmcndf=(EditText) convertView.findViewById(R.id.etTotal_Facing_McCain_DF);
				holder.etopnstkstrdf=(EditText) convertView.findViewById(R.id.etOpening_Stock_Store_DF);
				holder.ettotalfacstrdf=(EditText) convertView.findViewById(R.id.etTotal_Facing_Store_DF);
				holder.etmatwell=(EditText) convertView.findViewById(R.id.etmaterial_wellness_thawed_quantity);

				holder.openmccaindf_layout=(LinearLayout) convertView.findViewById(R.id.openmccaindf_layaout);

				holder.tbactual = (ToggleButton) convertView.findViewById(R.id.tbactual_listed);
				convertView.setTag(holder);

			}
			else{
				holder = (ViewHolder) convertView.getTag();
			}




			if (currentapiVersion >= 11) {
				holder.etopnstkcldrm.setTextIsSelectable(true);
				holder.etopnstkmcndf.setTextIsSelectable(true);
				holder.ettotalfacmcndf.setTextIsSelectable(true);
				holder.etopnstkstrdf.setTextIsSelectable(true);
				holder.ettotalfacstrdf.setTextIsSelectable(true);
				holder.etmatwell.setTextIsSelectable(true);
				holder.etaspermcn.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.etopnstkcldrm.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.etopnstkmcndf.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.ettotalfacmcndf.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.etopnstkstrdf.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.ettotalfacstrdf.setRawInputType(InputType.TYPE_CLASS_TEXT);
				holder.etmatwell.setRawInputType(InputType.TYPE_CLASS_TEXT);
			} else {
				holder.etopnstkcldrm.setInputType(0);
				holder.etopnstkmcndf.setInputType(0);
				holder.ettotalfacmcndf.setInputType(0);
				holder.etopnstkstrdf.setInputType(0);
				holder.ettotalfacstrdf.setInputType(0);
				holder.etmatwell.setInputType(0);

			}


			if(!mccainFlag){
				holder.etopnstkmcndf.setEnabled(false);
				holder.ettotalfacmcndf.setEnabled(false);

				_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setOpen_stock_mccaindf("0");

				_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setTotalfacing_mccaindf("0");

				holder.etopnstkmcndf.setText("0");
				holder.ettotalfacmcndf.setText("0");


			}

			if(!storeFlag){
				holder.etopnstkstrdf.setEnabled(false);
				holder.ettotalfacstrdf.setEnabled(false);

				_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setOpen_stock_store_df("0");

				_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setTotal_facing_storedf("0");

				holder.etopnstkstrdf.setText("0");
				holder.ettotalfacstrdf.setText("0");

			}


			holder.tbactual.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					ischangedflag=true;
					String val = ((ToggleButton) v).getText().toString();
					_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setActual_listed(val);

					expListView.invalidateViews();
				}
			});

//----------------------------------
			holder.etopnstkcldrm.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_cold_room("");

						} else {
							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_cold_room(value1);

						}

					}

				}
			});

			holder.etopnstkmcndf.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_mccaindf("");

						} else {


							String temp=_listDataChild.get(listDataHeader.get(groupPosition)).get(position).getTotalfacing_mccaindf();
							int facing=0;
							if(!temp.equals("")){
								facing=Integer.parseInt(temp);
							}
							int mccaindf=Integer.parseInt(value1);

							if(facing>mccaindf){
								Toast.makeText(getApplicationContext(),
										"Facing cannot be greater than Mccain Opening Stock", Toast.LENGTH_SHORT).show();
							}
							else{

								ischangedflag=true;

								_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_mccaindf(value1);
							}

						}

					}
				}
			});

			holder.ettotalfacmcndf.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setTotalfacing_mccaindf("");

						} else {

							String temp=_listDataChild.get(listDataHeader.get(groupPosition)).get(position).getOpen_stock_mccaindf();
							int mccaindf=0;
							if(!temp.equals("")){
								mccaindf=Integer.parseInt(temp);
							}
							int facing=Integer.parseInt(value1);

							if(facing>mccaindf){
								Toast.makeText(getApplicationContext(),
										"Facing cannot be greater than Mccain Opening Stock", Toast.LENGTH_SHORT).show();
							}
							else{

								ischangedflag=true;

								_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setTotalfacing_mccaindf(value1);
							}


						}

					}

				}
			});

			holder.etopnstkstrdf.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}
					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_store_df("");

						} else {

							String temp=_listDataChild.get(listDataHeader.get(groupPosition)).get(position).getTotal_facing_storedf();
							int facing=0;
							if(!temp.equals("")){
								facing=Integer.parseInt(temp);
							}

							int storedf=Integer.parseInt(value1);

							if(facing>storedf){
								Toast.makeText(getApplicationContext(),
										"Facing cannot be greater than Store Opening Stock", Toast.LENGTH_SHORT).show();
							}
							else{

								ischangedflag=true;

								_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setOpen_stock_store_df(value1);

							}

						}

					}

				}
			});

			holder.ettotalfacstrdf.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {

					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setTotal_facing_storedf("");

						} else {

							String temp=_listDataChild.get(listDataHeader.get(groupPosition)).get(position).getOpen_stock_store_df();
							int storedf=0;
							if(!temp.equals("")){
								storedf=Integer.parseInt(temp);
							}

							int facing=Integer.parseInt(value1);

							if(facing>storedf){
								Toast.makeText(getApplicationContext(),
										"Facing cannot be greater than Store Opening Stock", Toast.LENGTH_SHORT).show();
							}
							else{

								ischangedflag=true;

								_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setTotal_facing_storedf(value1);
							}

						}

					}

				}
			});

			holder.etmatwell.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						showKeyboardWithAnimation();
					}

					if (!hasFocus) {

						hide();
						final int position = v.getId();
						final EditText Caption = (EditText) v;
						String value1 = Caption.getText().toString();
						if (value1.equals("")) {

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setMaterial_wellness("");

						} else {

							ischangedflag=true;

							_listDataChild.get(listDataHeader.get(groupPosition)).get(position).setMaterial_wellness(value1);

						}

					}

				}
			});

			holder.etopnstkcldrm.setId(childPosition);
			holder.etopnstkmcndf.setId(childPosition);
			holder.ettotalfacmcndf.setId(childPosition);
			holder.etopnstkstrdf.setId(childPosition);
			holder.ettotalfacstrdf.setId(childPosition);
			holder.etmatwell.setId(childPosition);

			holder.etaspermcn.setText(childText.getAs_per_meccain());
			holder.etopnstkcldrm.setText(childText.getOpen_stock_cold_room());
			holder.etopnstkmcndf.setText(childText.getOpen_stock_mccaindf());
			holder.ettotalfacmcndf.setText(childText.getTotalfacing_mccaindf());
			holder.etopnstkstrdf.setText(childText.getOpen_stock_store_df());
			holder.ettotalfacstrdf.setText(childText.getTotal_facing_storedf());
			holder.etmatwell.setText(childText.getMaterial_wellness());


			holder.tbactual.setChecked(_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getActual_listed().equals("YES"));


			_listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).setSku_cd(childText.getSku_cd());

			TextView txtListChild = (TextView) convertView.findViewById(R.id.lblListItem);




			txtListChild.setText(childText.getSku());

			if(!checkflag){

				boolean tempflag=false;

				if(holder.etopnstkcldrm.getText().toString().equals("")){
					//holder.etopnstkcldrm.setBackgroundColor(getResources().getColor(R.color.red));
					holder.etopnstkcldrm.setHintTextColor(getResources().getColor(R.color.red));
					holder.etopnstkcldrm.setHint("Empty");
					tempflag=true;
				}
				else{
					//holder.etopnstkcldrm.setBackgroundColor(getResources().getColor(R.color.white));
				}


				if(mccainFlag){
					if(holder.etopnstkmcndf.getText().toString().equals("")){
						//holder.etopnstkmcndf.setBackgroundColor(getResources().getColor(R.color.red));
						holder.etopnstkmcndf.setHintTextColor(getResources().getColor(R.color.red));
						holder.etopnstkmcndf.setHint("Empty");
						tempflag=true;
					}
					else{
						//holder.etopnstkmcndf.setBackgroundColor(getResources().getColor(R.color.white));
					}

					if(holder.ettotalfacmcndf.getText().toString().equals("")){
						//holder.ettotalfacmcndf.setBackgroundColor(getResources().getColor(R.color.red));
						holder.ettotalfacmcndf.setHintTextColor(getResources().getColor(R.color.red));
						holder.ettotalfacmcndf.setHint("Empty");
						tempflag=true;
					}
					else{
						//holder.ettotalfacmcndf.setBackgroundColor(getResources().getColor(R.color.white));
					}
				}

				if(storeFlag){
					if(holder.etopnstkstrdf.getText().toString().equals("")){
						//holder.etopnstkstrdf.setBackgroundColor(getResources().getColor(R.color.red));
						holder.etopnstkstrdf.setHintTextColor(getResources().getColor(R.color.red));
						holder.etopnstkstrdf.setHint("Empty");
						tempflag=true;
					}
					else{
						//holder.etopnstkstrdf.setBackgroundColor(getResources().getColor(R.color.white));
					}

					if(holder.ettotalfacstrdf.getText().toString().equals("")){
						//holder.ettotalfacstrdf.setBackgroundColor(getResources().getColor(R.color.red));
						holder.ettotalfacstrdf.setHintTextColor(getResources().getColor(R.color.red));
						holder.ettotalfacstrdf.setHint("Empty");
						tempflag=true;
					}
					else{
						//holder.ettotalfacstrdf.setBackgroundColor(getResources().getColor(R.color.white));
					}
				}



				if(holder.etmatwell.getText().toString().equals("")){
					//holder.etmatwell.setBackgroundColor(getResources().getColor(R.color.red));
					holder.etmatwell.setHintTextColor(getResources().getColor(R.color.red));
					holder.etmatwell.setHint("Empty");
					tempflag=true;
				}
				else{
					//holder.etmatwell.setBackgroundColor(getResources().getColor(R.color.white));
				}

				if(tempflag){

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.red));
				}
				else{

					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
				}

			}

			if(!validate){

				if(checkValidHeaderArray.contains(groupPosition)){

					if(checkValidChildArray.contains(childPosition)){
						boolean tempflag=false;

						if(flagcoldroom){
							holder.etopnstkcldrm.setTextColor(getResources().getColor(R.color.red));
							tempflag=true;
						}
						else{
							holder.etopnstkcldrm.setTextColor(getResources().getColor(R.color.teal_dark));
						}

						if(flagmccain){
							holder.etopnstkmcndf.setTextColor(getResources().getColor(R.color.red));
							tempflag=true;
						}
						else{
							holder.etopnstkmcndf.setTextColor(getResources().getColor(R.color.teal_dark));
						}

						if(flagstoredf){
							holder.etopnstkstrdf.setTextColor(getResources().getColor(R.color.red));
							tempflag=true;
						}
						else{
							holder.etopnstkstrdf.setTextColor(getResources().getColor(R.color.teal_dark));
						}

						if(!flagcoldroom && !flagmccain && !flagstoredf){
							holder.etopnstkcldrm.setTextColor(getResources().getColor(R.color.teal_dark));
							holder.etopnstkmcndf.setTextColor(getResources().getColor(R.color.teal_dark));
							holder.etopnstkstrdf.setTextColor(getResources().getColor(R.color.teal_dark));
							holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.red));
							tempflag=false;
						}
						else{

							/*holder.etclstkcold.setTextColor(getResources().getColor(R.color.red));
							holder.etclstkmcndf.setTextColor(getResources().getColor(R.color.red));
							holder.etclstkstrdf.setTextColor(getResources().getColor(R.color.red));*/
							holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.red));
							tempflag=true;
						}

					}
					else{
						holder.etopnstkcldrm.setTextColor(getResources().getColor(R.color.teal_dark));
						holder.etopnstkmcndf.setTextColor(getResources().getColor(R.color.teal_dark));
						holder.etopnstkstrdf.setTextColor(getResources().getColor(R.color.teal_dark));
						holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
					}

				}
				else{
					holder.etopnstkcldrm.setTextColor(getResources().getColor(R.color.teal_dark));
					holder.etopnstkmcndf.setTextColor(getResources().getColor(R.color.teal_dark));
					holder.etopnstkstrdf.setTextColor(getResources().getColor(R.color.teal_dark));
					holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
				}

			}

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
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
				convertView = infalInflater.inflate(R.layout.list_group, null);
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
			//up
			if(!validate){
				if(checkValidHeaderArray.contains(groupPosition)){
					lblListHeader.setBackgroundColor(getResources().getColor(R.color.red));
				}
				else{
					lblListHeader.setBackgroundColor(getResources().getColor(R.color.teal_dark));
				}
			}

			//convertView.setId(groupPosition);

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

		EditText etopnstkcldrm,etopnstkmcndf,ettotalfacmcndf,etopnstkstrdf,ettotalfacstrdf,etmatwell;
		ToggleButton tbactual;
		TextView etaspermcn;
		LinearLayout openmccaindf_layout;
		CardView cardView;

		public MutableWatcher mWatcher;
	}


	boolean validateData(
			HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> listDataChild2,
			List<OpeningStockInsertDataGetterSetter> listDataHeader2) {
		boolean flag = true;

		checkHeaderArray.clear();

		for (int i = 0; i < listDataHeader2.size(); i++) {
			for (int j = 0; j < listDataChild2.get(listDataHeader2.get(i))
					.size(); j++) {
				/*String aspermccain = listDataChild2.get(listDataHeader2.get(i)).get(j).getAs_per_meccain();*/
				String openstockcoldrm = listDataChild2.get(listDataHeader2.get(i)).get(j).getOpen_stock_cold_room();
				String openstkmccndf = listDataChild2.get(listDataHeader2.get(i)).get(j).getOpen_stock_mccaindf();
				String openstockstrdf = listDataChild2.get(listDataHeader2.get(i)).get(j).getOpen_stock_store_df();
				String totalstrdf = listDataChild2.get(listDataHeader2.get(i)).get(j).getTotal_facing_storedf();
				String totalmccndf = listDataChild2.get(listDataHeader2.get(i)).get(j).getTotalfacing_mccaindf();
				String matwell = listDataChild2.get(listDataHeader2.get(i)).get(j).getMaterial_wellness();

				int openstkmccndfint;
				int totalmccndfint;
				int openstockstrdfint;
				int totalstrdfint;


				if (openstockcoldrm.equalsIgnoreCase("") || (mccainFlag==true && openstkmccndf.equalsIgnoreCase("")) ||
						(storeFlag==true && openstockstrdf.equalsIgnoreCase("")) || (storeFlag==true && totalstrdf.equalsIgnoreCase("")) || (mccainFlag==true && totalmccndf.equalsIgnoreCase("")) || matwell.equalsIgnoreCase("")
						) {

					if(!checkHeaderArray.contains(i)){
						checkHeaderArray.add(i);
					}

					checkflag=false;

					flag = false;
					Error_Message="Please fill all the data";
					break;

				}
				else {

					if(mccainFlag){
						openstkmccndfint=Integer.parseInt(openstkmccndf);
						totalmccndfint=Integer.parseInt(totalmccndf);

						if(totalmccndfint>openstkmccndfint){
							if(!checkHeaderArray.contains(i)){
								checkHeaderArray.add(i);
							}

							checkflag=false;

							flag = false;
							Error_Message="Facing data should not be greater than Mccain opening Stock";
							break;
						}
						else{
							checkflag=true;
							flag = true;
						}
					}
					else{
						checkflag=true;
						flag = true;
					}

					if(storeFlag){

						openstockstrdfint=Integer.parseInt(openstockstrdf);
						totalstrdfint=Integer.parseInt(totalstrdf);


						if(totalstrdfint>openstockstrdfint){
							if(!checkHeaderArray.contains(i)){
								checkHeaderArray.add(i);
							}

							checkflag=false;

							flag = false;
							Error_Message="Facing data should not be greater than Store opening Stock";
							break;
						}
						else{
							checkflag=true;
							flag = true;
						}

					}
					else{
						checkflag=true;
						flag = true;
					}




				}

			}

			if(checkflag == false){
				break;
			}

		}

		//expListView.invalidate();
		listAdapter.notifyDataSetChanged();

		return checkflag;

	}


	void setBlank(
			HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> listDataChild2,
			List<OpeningStockInsertDataGetterSetter> listDataHeader2){

		for (int i = 0; i < listDataHeader2.size(); i++) {
			for (int j = 0; j < listDataChild2.get(listDataHeader2.get(i)).size(); j++) {

				if(!mccainFlag){
					listDataChild.get(listDataHeader2.get(i)).get(j).setOpen_stock_mccaindf("");
					listDataChild.get(listDataHeader2.get(i)).get(j).setTotalfacing_mccaindf("");
				}

				if(!storeFlag){
					listDataChild.get(listDataHeader2.get(i)).get(j).setOpen_stock_store_df("");
					listDataChild.get(listDataHeader2.get(i)).get(j).setTotal_facing_storedf("");
				}

			}
		}

	}

	/***
	 * Display the screen keyboard with an animated slide from bottom
	 */
	private void showKeyboardWithAnimation() {
		if (mKeyboardView.getVisibility() == View.GONE) {
			Animation animation = AnimationUtils
					.loadAnimation(OpeningStock.this,
							R.anim.slide_in_bottom);
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

				AlertDialog.Builder builder = new AlertDialog.Builder(
						OpeningStock.this);
				builder.setMessage(CommonString.ONBACK_ALERT_MESSAGE)
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialog, int id) {

										OpeningStock.this.finish();

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
				OpeningStock.this.finish();

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

	boolean validateStockData(
			HashMap<OpeningStockInsertDataGetterSetter, List<StockGetterSetter>> listDataChild2,
			List<OpeningStockInsertDataGetterSetter> listDataHeader2) {
		//boolean flag = true;

		if(brandData.size()>0){

			// Adding child data

			checkValidHeaderArray.clear();
			checkValidChildArray.clear();


			for(int i=0;i<brandData.size();i++){

				stockData=db.getClosingNMiddayStockDataFromDatabase(store_cd, brandData.get(i).getBrand_cd());

				for(int j=0;j<stockData.size();j++){

					String closing_coldroom = stockData.get(j).getClos_stock_cold_room();
					String mid_stock = stockData.get(j).getMidday_stock();
					String closing_mccain_df = stockData.get(j).getClos_stock_meccaindf();
					String closing_store_df = stockData.get(j).getClos_stock_storedf();

					String cold_room = listDataChild.get(listDataHeader.get(i)).get(j).getOpen_stock_cold_room();
					String mccain_df = listDataChild.get(listDataHeader.get(i)).get(j).getOpen_stock_mccaindf();
					String store_df = listDataChild.get(listDataHeader.get(i)).get(j).getOpen_stock_store_df();

					int midStock=Integer.parseInt(mid_stock);

					int opncold=Integer.parseInt(cold_room);
					int opnmccn=Integer.parseInt(mccain_df);
					int opnstore=+Integer.parseInt(store_df);

					int closecold=Integer.parseInt(closing_coldroom);
					int closemccn=Integer.parseInt(closing_mccain_df);
					int closestore=Integer.parseInt(closing_store_df);

					if(midStock==0){
						if(closecold>opncold){
							flagcoldroom=true;
							if(!checkValidChildArray.contains(j)){
								checkValidChildArray.add(j);
							}
							if(!checkValidHeaderArray.contains(i)){
								checkValidHeaderArray.add(i);
							}
						}
						if(closemccn>opnmccn){
							flagmccain=true;
							if(!checkValidChildArray.contains(j)){
								checkValidChildArray.add(j);
							}
							if(!checkValidHeaderArray.contains(i)){
								checkValidHeaderArray.add(i);
							}
						}
						if(closestore>opnstore){
							flagstoredf=true;
							if(!checkValidChildArray.contains(j)){
								checkValidChildArray.add(j);
							}
							if(!checkValidHeaderArray.contains(i)){
								checkValidHeaderArray.add(i);
							}
						}

						if(flagcoldroom == true || flagmccain == true || flagstoredf == true){
							validate=false;

							if(!checkValidChildArray.contains(j)){
								checkValidChildArray.add(j);
							}
							if(!checkValidHeaderArray.contains(i)){
								checkValidHeaderArray.add(i);
							}

							break;
						}
						else{
							validate=true;
						}

					}
					else{

						int total_stock = opncold + midStock + opnmccn + opnstore;
						int total_closing = closecold + closemccn + closestore;

						if ((total_stock>=total_closing)) {

							validate = true;
							//flag = true;

						} else {
							validate = false;
							valHeadCount=i;
							if(!checkValidChildArray.contains(j)){
								checkValidChildArray.add(j);
							}
							if(!checkValidHeaderArray.contains(i)){
								checkValidHeaderArray.add(i);
							}


							//flag = false;
							break;
						}

					}

				}

				if(validate==false){
					break;
				}

			}

		}

		return validate;
	}


	class MutableWatcher implements TextWatcher {

		private int mPosition;
		private boolean mActive;

		void setPosition(int position) {
			mPosition = position;
		}

		void setActive(boolean active) {
			mActive = active;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void afterTextChanged(Editable s) {
			if (mActive) {
				//  mUserDetails.set(mPosition, s.toString());
			}
		}

	}

}
