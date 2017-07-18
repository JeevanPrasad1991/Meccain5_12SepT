package com.cpm.xmlGetterSetter;

import java.util.ArrayList;

/**
 * Created by upendrak on 10-07-2017.
 */

public class CompetitionSkuMasterGetterSetter {
    String Complete_sku_master_table;

    public String getSku_master_table() {
        return Complete_sku_master_table;
    }


    public void setSku_master_table(String sku_master_table) {
        this.Complete_sku_master_table = sku_master_table;
    }

    ArrayList<String> sku_cd = new ArrayList<String>();
    ArrayList<String> sku = new ArrayList<String>();
    ArrayList<String> brand_cd = new ArrayList<String>();
    ArrayList<String> brand = new ArrayList<String>();
    ArrayList<String> category_cd = new ArrayList<String>();
    ArrayList<String> category = new ArrayList<String>();
    ArrayList<String> mrp = new ArrayList<String>();
    ArrayList<String> category_type = new ArrayList<String>();
    ArrayList<String> packing_size = new ArrayList<String>();
}
