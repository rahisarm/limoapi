package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class RepMoveModel {
    @Id
    private int repno;

    private String outdate,outtime,outkm,outfuel;
    private String collectdate,collecttime,collectkm,collectfuel;
    private String deldate,deltime,delkm,delfuel;
    private String indate,intime,inkm,infuel,inbrhid,inlocid;
    private String rdocno,rdtype,rfleetno,ofleetno,rbrhid,rlocid;
    private String obrhid,olocid;
    private String cldocno,agmttrancode;

    private String maxparent,maxmovdoc;
    private String totalmin,totalkm,totalfuel,idletime;

    private int onedayextraconfig,monthcalmethod,monthcalvalue,invtype;
    private String invtodate,rentaltype;

    public String getRentaltype() {
        return rentaltype;
    }

    public void setRentaltype(String rentaltype) {
        this.rentaltype = rentaltype;
    }

    public String getInvtodate() {
        return invtodate;
    }

    public void setInvtodate(String invtodate) {
        this.invtodate = invtodate;
    }

    public int getInvtype() {

        return invtype;
    }

    public void setInvtype(int invtype) {
        this.invtype = invtype;
    }

    public int getMonthcalmethod() {
        return monthcalmethod;
    }

    public void setMonthcalmethod(int monthcalmethod) {
        this.monthcalmethod = monthcalmethod;
    }

    public int getMonthcalvalue() {
        return monthcalvalue;
    }

    public void setMonthcalvalue(int monthcalvalue) {
        this.monthcalvalue = monthcalvalue;
    }

    public int getOnedayextraconfig() {
        return onedayextraconfig;
    }

    public void setOnedayextraconfig(int onedayextraconfig) {
        this.onedayextraconfig = onedayextraconfig;
    }

    public String getIdletime() {
        return idletime;
    }

    public void setIdletime(String idletime) {
        this.idletime = idletime;
    }

    public String getMaxmovdoc() {
        return maxmovdoc;
    }

    public void setMaxmovdoc(String maxmovdoc) {
        this.maxmovdoc = maxmovdoc;
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

    public String getMaxparent() {
        return maxparent;
    }

    public void setMaxparent(String maxparent) {
        this.maxparent = maxparent;
    }

    public String getObrhid() {
        return obrhid;
    }

    public void setObrhid(String obrhid) {
        this.obrhid = obrhid;
    }

    public String getOlocid() {
        return olocid;
    }

    public void setOlocid(String olocid) {
        this.olocid = olocid;
    }

    public String getCldocno() {
        return cldocno;
    }

    public void setCldocno(String cldocno) {
        this.cldocno = cldocno;
    }

    public String getAgmttrancode() {
        return agmttrancode;
    }

    public void setAgmttrancode(String agmttrancode) {
        this.agmttrancode = agmttrancode;
    }

    public String getRbrhid() {
        return rbrhid;
    }

    public void setRbrhid(String rbrhid) {
        this.rbrhid = rbrhid;
    }

    public String getRlocid() {
        return rlocid;
    }

    public void setRlocid(String rlocid) {
        this.rlocid = rlocid;
    }

    public String getRfleetno() {
        return rfleetno;
    }

    public void setRfleetno(String rfleetno) {
        this.rfleetno = rfleetno;
    }

    public String getOfleetno() {
        return ofleetno;
    }

    public void setOfleetno(String ofleetno) {
        this.ofleetno = ofleetno;
    }

    public String getOutdate() {
        return outdate;
    }

    public void setOutdate(String outdate) {
        this.outdate = outdate;
    }

    public String getOuttime() {
        return outtime;
    }

    public void setOuttime(String outtime) {
        this.outtime = outtime;
    }

    public String getOutkm() {
        return outkm;
    }

    public void setOutkm(String outkm) {
        this.outkm = outkm;
    }

    public String getOutfuel() {
        return outfuel;
    }

    public void setOutfuel(String outfuel) {
        this.outfuel = outfuel;
    }

    public String getInbrhid() {
        return inbrhid;
    }

    public void setInbrhid(String inbrhid) {
        this.inbrhid = inbrhid;
    }

    public String getInlocid() {
        return inlocid;
    }

    public void setInlocid(String inlocid) {
        this.inlocid = inlocid;
    }

    public int getRepno() {
        return repno;
    }

    public void setRepno(int repno) {
        this.repno = repno;
    }

    public String getCollectdate() {
        return collectdate;
    }

    public void setCollectdate(String collectdate) {
        this.collectdate = collectdate;
    }

    public String getCollecttime() {
        return collecttime;
    }

    public void setCollecttime(String collecttime) {
        this.collecttime = collecttime;
    }

    public String getCollectkm() {
        return collectkm;
    }

    public void setCollectkm(String collectkm) {
        this.collectkm = collectkm;
    }

    public String getCollectfuel() {
        return collectfuel;
    }

    public void setCollectfuel(String collectfuel) {
        this.collectfuel = collectfuel;
    }

    public String getDeldate() {
        return deldate;
    }

    public void setDeldate(String deldate) {
        this.deldate = deldate;
    }

    public String getDeltime() {
        return deltime;
    }

    public void setDeltime(String deltime) {
        this.deltime = deltime;
    }

    public String getDelkm() {
        return delkm;
    }

    public void setDelkm(String delkm) {
        this.delkm = delkm;
    }

    public String getDelfuel() {
        return delfuel;
    }

    public void setDelfuel(String delfuel) {
        this.delfuel = delfuel;
    }

    public String getIndate() {
        return indate;
    }

    public void setIndate(String indate) {
        this.indate = indate;
    }

    public String getIntime() {
        return intime;
    }

    public void setIntime(String intime) {
        this.intime = intime;
    }

    public String getInkm() {
        return inkm;
    }

    public void setInkm(String inkm) {
        this.inkm = inkm;
    }

    public String getInfuel() {
        return infuel;
    }

    public void setInfuel(String infuel) {
        this.infuel = infuel;
    }

    public String getRdocno() {
        return rdocno;
    }

    public void setRdocno(String rdocno) {
        this.rdocno = rdocno;
    }

    public String getRdtype() {
        return rdtype;
    }

    public void setRdtype(String rdtype) {
        this.rdtype = rdtype;
    }

    public RepMoveModel() {
    }

    public RepMoveModel(int repno, String outdate, String outtime, String outkm, String outfuel, String collectdate, String collecttime, String collectkm, String collectfuel, String deldate, String deltime, String delkm, String delfuel, String indate, String intime, String inkm, String infuel, String inbrhid, String inlocid, String rdocno, String rdtype, String rfleetno, String ofleetno, String rbrhid, String rlocid, String obrhid, String olocid, String cldocno, String agmttrancode, String maxparent, String maxmovdoc, String totalmin, String totalkm, String totalfuel, String idletime,int onedayextraconfig) {
        this.onedayextraconfig=onedayextraconfig;
        this.repno = repno;
        this.outdate = outdate;
        this.outtime = outtime;
        this.outkm = outkm;
        this.outfuel = outfuel;
        this.collectdate = collectdate;
        this.collecttime = collecttime;
        this.collectkm = collectkm;
        this.collectfuel = collectfuel;
        this.deldate = deldate;
        this.deltime = deltime;
        this.delkm = delkm;
        this.delfuel = delfuel;
        this.indate = indate;
        this.intime = intime;
        this.inkm = inkm;
        this.infuel = infuel;
        this.inbrhid = inbrhid;
        this.inlocid = inlocid;
        this.rdocno = rdocno;
        this.rdtype = rdtype;
        this.rfleetno = rfleetno;
        this.ofleetno = ofleetno;
        this.rbrhid = rbrhid;
        this.rlocid = rlocid;
        this.obrhid = obrhid;
        this.olocid = olocid;
        this.cldocno = cldocno;
        this.agmttrancode = agmttrancode;
        this.maxparent = maxparent;
        this.maxmovdoc = maxmovdoc;
        this.totalmin = totalmin;
        this.totalkm = totalkm;
        this.totalfuel = totalfuel;
        this.idletime = idletime;
    }
}
