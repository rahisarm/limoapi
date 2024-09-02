package com.gateway.limoapi.model;

public class AgmtModel {

    private String tarifftypename,tarifftypetext,tariffamount;
    private String gid;

    public AgmtModel() {

    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getTarifftypename() {
        return tarifftypename;
    }

    public void setTarifftypename(String tarifftypename) {
        this.tarifftypename = tarifftypename;
    }

    public String getTarifftypetext() {
        return tarifftypetext;
    }

    public void setTarifftypetext(String tarifftypetext) {
        this.tarifftypetext = tarifftypetext;
    }

    public String getTariffamount() {
        return tariffamount;
    }

    public void setTariffamount(String tariffamount) {
        this.tariffamount = tariffamount;
    }

    public AgmtModel(String tarifftypename, String tarifftypetext, String tariffamount, String gid) {
        this.tarifftypename = tarifftypename;
        this.tarifftypetext = tarifftypetext;
        this.tariffamount = tariffamount;
        this.gid = gid;
    }
}
