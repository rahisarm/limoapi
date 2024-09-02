package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class DriverTasks implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private int rdocno;
    private String rdtype;
    private String clientname;
    private String fleetname;
    private String tripmode;
    private String mindate,mintime,minkm,minfuel;
    private String repno,repstage;
    private String fleetno;
    private String drvtripstatus;
    private String tripstartdate;
    private String tripstarttime;
    private String voctype,vocno;

    
    public String getVoctype() {
		return voctype;
	}

	public void setVoctype(String voctype) {
		this.voctype = voctype;
	}

	public String getVocno() {
		return vocno;
	}

	public void setVocno(String vocno) {
		this.vocno = vocno;
	}

	public String getDrvtripstatus() {
        return drvtripstatus;
    }

    public void setDrvtripstatus(String drvtripstatus) {
        this.drvtripstatus = drvtripstatus;
    }

    public String getFleetno() {
        return fleetno;
    }

    public void setFleetno(String fleetno) {
        this.fleetno = fleetno;
    }

    public String getTripstartdate() {
        return tripstartdate;
    }

    public void setTripstartdate(String tripstartdate) {
        this.tripstartdate = tripstartdate;
    }

    public String getTripstarttime() {
        return tripstarttime;
    }

    public void setTripstarttime(String tripstarttime) {
        this.tripstarttime = tripstarttime;
    }

    public DriverTasks() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getRdocno() {
        return rdocno;
    }

    public void setRdocno(int rdocno) {
        this.rdocno = rdocno;
    }

    public String getRdtype() {
        return rdtype;
    }

    public void setRdtype(String rdtype) {
        this.rdtype = rdtype;
    }

    public String getClientname() {
        return clientname;
    }

    public void setClientname(String clientname) {
        this.clientname = clientname;
    }

    public String getFleetname() {
        return fleetname;
    }

    public void setFleetname(String fleetname) {
        this.fleetname = fleetname;
    }

    public String getTripmode() {
        return tripmode;
    }

    public void setTripmode(String tripmode) {
        this.tripmode = tripmode;
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

    public String getMinkm() {
        return minkm;
    }

    public void setMinkm(String minkm) {
        this.minkm = minkm;
    }

    public String getMinfuel() {
        return minfuel;
    }

    public void setMinfuel(String minfuel) {
        this.minfuel = minfuel;
    }

    public String getRepno() {
        return repno;
    }

    public void setRepno(String repno) {
        this.repno = repno;
    }

    public String getRepstage() {
        return repstage;
    }

    public void setRepstage(String repstage) {
        this.repstage = repstage;
    }

    public DriverTasks(Long id, int rdocno, String rdtype, String clientname, String fleetname, String tripmode, String mindate, String mintime, String minkm, String minfuel, String repno, String repstage, String fleetno,String drvtripstatus,String tripstartdate,String tripstarttime,String voctype,String vocno) {
        this.id = id;
        this.rdocno = rdocno;
        this.rdtype = rdtype;
        this.clientname = clientname;
        this.fleetname = fleetname;
        this.tripmode = tripmode;
        this.mindate = mindate;
        this.mintime = mintime;
        this.minkm = minkm;
        this.minfuel = minfuel;
        this.repno = repno;
        this.repstage = repstage;
        this.fleetno = fleetno;
        this.drvtripstatus=drvtripstatus;
        this.tripstartdate=tripstartdate;
        this.tripstarttime=tripstarttime;
        this.voctype=voctype;
        this.vocno=vocno;
    }

    @Override
    public String toString() {
        return "DriverTasks{" +
                "id=" + id +
                ", rdocno=" + rdocno +
                ", rdtype='" + rdtype + '\'' +
                ", clientname='" + clientname + '\'' +
                ", fleetname='" + fleetname + '\'' +
                ", tripmode='" + tripmode + '\'' +
                ", mindate='" + mindate + '\'' +
                ", mintime='" + mintime + '\'' +
                ", minkm='" + minkm + '\'' +
                ", minfuel='" + minfuel + '\'' +
                ", repno='" + repno + '\'' +
                ", repstage='" + repstage + '\'' +
                '}';
    }
}
