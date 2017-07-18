package com.cpm.xmlGetterSetter;

import java.util.ArrayList;

/**
 * Created by upendrak on 10-07-2017.
 */

public class ComprtitionskumasterGetterSetter {

    String COMPETITION_SKU_MASTER;
    ArrayList<String> COMP_SKU_CD = new ArrayList<String>();
    ArrayList<String> COMP_SKU = new ArrayList<String>();

  /*  public void setCategory_type(String category_type) {
        this.category_type.add(category_type);
    }

    public ArrayList<String> getCategory() {
        return category;
    }*/

    public ArrayList<String> getCOMP_SKU() {
        return COMP_SKU;
    }

    public void setCOMP_SKU(String COMP_SKU) {
        this.COMP_SKU.add(COMP_SKU);
    }

    public ArrayList<String> getCOMP_SKU_CD() {
        return COMP_SKU_CD;
    }

    public void setCOMP_SKU_CD(String COMP_SKU_CD) {
        this.COMP_SKU_CD.add(COMP_SKU_CD);
    }

    public String getCOMPETITION_SKU_MASTER() {
        return COMPETITION_SKU_MASTER;
    }

    public void setCOMPETITION_SKU_MASTER(String COMPETITION_SKU_MASTER) {
        this.COMPETITION_SKU_MASTER = COMPETITION_SKU_MASTER;
    }



}
