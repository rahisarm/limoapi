package com.gateway.limoapi.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class DropdownModel {
    @Id

    private String docno;
    private String refname;

    public String getDocno() {
        return docno;
    }

    public void setDocno(String docno) {  
        this.docno = docno;
    }

    public String getRefname() {
        return refname;
    }

    public void setRefname(String refname) {
        this.refname = refname;
    }

    public DropdownModel() {
    }

    public DropdownModel(String docno, String refname) {
        this.docno = docno;
        this.refname = refname;
    }
}
