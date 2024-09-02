package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class DriverModel {
    @Id
    private int drvdocno;
    private String drivername;
    private String driverdetail;

    public int getDrvdocno() {
        return drvdocno;
    }

    public void setDrvdocno(int drvdocno) {
        this.drvdocno = drvdocno;
    }

    public String getDrivername() {
        return drivername;
    }

    public void setDrivername(String drivername) {
        this.drivername = drivername;
    }

    public String getDriverdetail() {
        return driverdetail;
    }

    public void setDriverdetail(String driverdetail) {
        this.driverdetail = driverdetail;
    }

    public DriverModel() {
    }

    public DriverModel(int drvdocno, String drivername, String driverdetail) {
        this.drvdocno = drvdocno;
        this.drivername = drivername;
        this.driverdetail = driverdetail;
    }
}
