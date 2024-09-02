package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MovModel {
    @Id
    private int docno;
    private String movdocno,trancode,minfuel,minkm,mindate,mintime,brhid,locid,fleetno,movtype,delivery,collection;

    private String maxparent,maxmovdoc,totalmin,totalkm,totalfuel,idlemin;
    private String flname;

    private String clientname,paytype,cardtype;

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getPaytype() {
        return paytype;
    }

    public void setPaytype(String paytype) {
        this.paytype = paytype;
    }

    public String getCardtype() {
        return cardtype;
    }

    public void setCardtype(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getFlname() {
        return flname;
    }

    public void setFlname(String flname) {
        this.flname = flname;
    }

    public String getTotalmin() {
        return totalmin;
    }

    public void setTotalmin(String totalmin) {
        this.totalmin = totalmin;
    }

    public String getTotalkm() {
        return totalkm;
    }

    public void setTotalkm(String totalkm) {
        this.totalkm = totalkm;
    }

    public String getTotalfuel() {
        return totalfuel;
    }

    public void setTotalfuel(String totalfuel) {
        this.totalfuel = totalfuel;
    }

    public String getIdlemin() {
        return idlemin;
    }

    public void setIdlemin(String idlemin) {
        this.idlemin = idlemin;
    }

    public String getMaxparent() {
        return maxparent;
    }

    public void setMaxparent(String maxparent) {
        this.maxparent = maxparent;
    }

    public String getMaxmovdoc() {
        return maxmovdoc;
    }

    public void setMaxmovdoc(String maxmovdoc) {
        this.maxmovdoc = maxmovdoc;
    }

    public String getMovtype() {
        return movtype;
    }

    public void setMovtype(String movtype) {
        this.movtype = movtype;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getFleetno() {
        return fleetno;
    }

    public void setFleetno(String fleetno) {
        this.fleetno = fleetno;
    }

    public int getDocno() {
        return docno;
    }

    public void setDocno(int docno) {
        this.docno = docno;
    }

    public String getMovdocno() {
        return movdocno;
    }

    public void setMovdocno(String movdocno) {
        this.movdocno = movdocno;
    }

    public String getTrancode() {
        return trancode;
    }

    public void setTrancode(String trancode) {
        this.trancode = trancode;
    }

    public String getMinfuel() {
        return minfuel;
    }

    public void setMinfuel(String minfuel) {
        this.minfuel = minfuel;
    }

    public String getMinkm() {
        return minkm;
    }

    public void setMinkm(String minkm) {
        this.minkm = minkm;
    }

    public String getMindate() {
        return mindate;
    }

    public void setMindate(String mindate) {
        this.mindate = mindate;
    }

    public String getMintime() {
        return mintime;
    }

    public void setMintime(String mintime) {
        this.mintime = mintime;
    }

    public String getBrhid() {
        return brhid;
    }

    public void setBrhid(String brhid) {
        this.brhid = brhid;
    }

    public String getLocid() {
        return locid;
    }

    public void setLocid(String locid) {
        this.locid = locid;
    }

    public MovModel() {
    }

    public MovModel(int docno, String movdocno, String trancode, String minfuel, String minkm, String mindate, String mintime, String brhid, String locid, String fleetno, String movtype, String delivery, String collection, String maxparent, String maxmovdoc, String totalmin, String totalkm, String totalfuel, String idlemin, String flname, String clientname, String paytype, String cardtype) {
        this.docno = docno;
        this.movdocno = movdocno;
        this.trancode = trancode;
        this.minfuel = minfuel;
        this.minkm = minkm;
        this.mindate = mindate;
        this.mintime = mintime;
        this.brhid = brhid;
        this.locid = locid;
        this.fleetno = fleetno;
        this.movtype = movtype;
        this.delivery = delivery;
        this.collection = collection;
        this.maxparent = maxparent;
        this.maxmovdoc = maxmovdoc;
        this.totalmin = totalmin;
        this.totalkm = totalkm;
        this.totalfuel = totalfuel;
        this.idlemin = idlemin;
        this.flname = flname;
        this.clientname = clientname;
        this.paytype = paytype;
        this.cardtype = cardtype;
    }
}
