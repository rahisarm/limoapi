package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class InspectionModel {
    @Id
    private int docno;
    private String imagepath;
    private String refname,fleetno,vocno,date,reftype,flname,inspecttype,brhid;
    private String refdocno,refvocno,regdetails;
    private int filesrno;

    public int getFilesrno() {
        return filesrno;
    }  

    public void setFilesrno(int filesrno) {
        this.filesrno = filesrno;
    }

    public String getRefdocno() {
        return refdocno;
    }

    public void setRefdocno(String refdocno) {
        this.refdocno = refdocno;
    }

    public String getRefvocno() {
        return refvocno;
    }

    public void setRefvocno(String refvocno) {
        this.refvocno = refvocno;
    }

    public String getRegdetails() {
        return regdetails;
    }

    public void setRegdetails(String regdetails) {
        this.regdetails = regdetails;
    }

    public String getBrhid() {
        return brhid;
    }

    public void setBrhid(String brhid) {
        this.brhid = brhid;
    }

    public String getInspecttype() {
        return inspecttype;
    }

    public void setInspecttype(String inspecttype) {
        this.inspecttype = inspecttype;
    }

    public String getRefname() {
        return refname;
    }

    public void setRefname(String refname) {
        this.refname = refname;
    }

    public String getFleetno() {
        return fleetno;
    }

    public void setFleetno(String fleetno) {
        this.fleetno = fleetno;
    }

    public String getVocno() {
        return vocno;
    }

    public void setVocno(String vocno) {
        this.vocno = vocno;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReftype() {
        return reftype;
    }

    public void setReftype(String reftype) {
        this.reftype = reftype;
    }

    public String getFlname() {
        return flname;
    }

    public void setFlname(String flname) {
        this.flname = flname;
    }

    public int getDocno() {
        return docno;
    }

    public void setDocno(int docno) {
        this.docno = docno;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public InspectionModel() {
    }

    public InspectionModel(int docno, String imagepath, String refname, String fleetno, String vocno,String date, String reftype, String flname,String inspecttype,String brhid,String refdocno,String refvocno,String regdetails,int filesrno) {
        this.docno = docno;
        this.imagepath = imagepath;
        this.refname = refname;
        this.fleetno = fleetno;
        this.vocno = vocno;
        this.date = date;
        this.reftype = reftype;
        this.flname = flname;
        this.inspecttype=inspecttype;
        this.brhid=brhid;
        this.refdocno=refdocno;
        this.refvocno=refvocno;
        this.regdetails=regdetails;
        this.filesrno=filesrno;
    }
}
