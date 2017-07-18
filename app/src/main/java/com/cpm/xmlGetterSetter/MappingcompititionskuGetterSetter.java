package com.cpm.xmlGetterSetter;

import java.util.ArrayList;

/**
 * Created by upendrak on 10-07-2017.
 */

public class MappingcompititionskuGetterSetter {
    String MAPPING_COMPETITION_SKU;
    ArrayList<String> compskucd = new ArrayList<String>();
    ArrayList<String> skucd = new ArrayList<String>();

    public String getMAPPING_COMPETITION_SKU() {
        return MAPPING_COMPETITION_SKU;
    }

    public void setMAPPING_COMPETITION_SKU(String MAPPING_COMPETITION_SKU) {
        this.MAPPING_COMPETITION_SKU = MAPPING_COMPETITION_SKU;
    }


    public ArrayList<String> getCompskucd() {
        return compskucd;
    }

    public void setCompskucd(String compskucd) {
        this.compskucd.add(compskucd);
    }

    public ArrayList<String> getSkucd() {
        return skucd;
    }

    public void setSkucd(String skucd) {
        this.skucd.add(skucd);
    }
}
