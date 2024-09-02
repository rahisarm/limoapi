package com.gateway.limoapi.model;

public class MiscModel {
    private String srno;
    private String rdtype,rdocno,type,fleetno,inspecttype;
    private String cldocno,accountgroup,docno;
    private String erporigin;

    public String getErporigin() {
        return erporigin;
    }

    public void setErporigin(String erporigin) {
        this.erporigin = erporigin;
    }

    public String getDocno() {
        return docno;
    }  

    public void setDocno(String docno) {
        this.docno = docno;
    }

    public String getCldocno() {
        return cldocno;
    }

    public void setCldocno(String cldocno) {
        this.cldocno = cldocno;
    }

    public String getAccountgroup() {
        return accountgroup;
    }

    public void setAccountgroup(String accountgroup) {
        this.accountgroup = accountgroup;
    }

    public MiscModel(String srno, String rdtype, String rdocno, String type, String fleetno, String inspecttype, String cldocno, String accountgroup, String docno,String erporigin) {
        this.srno = srno;
        this.rdtype = rdtype;
        this.rdocno = rdocno;
        this.type = type;
        this.fleetno = fleetno;
        this.inspecttype = inspecttype;
        this.cldocno = cldocno;
        this.accountgroup = accountgroup;
        this.docno = docno;
        this.erporigin=erporigin;
    }

    public String getInspecttype() {
        return inspecttype;
    }

    public void setInspecttype(String inspecttype) {
        this.inspecttype = inspecttype;
    }

    public String getRdtype() {
        return rdtype;
    }

    public void setRdtype(String rdtype) {
        this.rdtype = rdtype;
    }

    public String getRdocno() {
        return rdocno;
    }

    public void setRdocno(String rdocno) {
        this.rdocno = rdocno;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFleetno() {
        return fleetno;
    }

    public void setFleetno(String fleetno) {
        this.fleetno = fleetno;
    }

    public String getSrno() {
        return srno;
    }

    public void setSrno(String srno) {
        this.srno = srno;
    }

    public MiscModel() {
    }

}
