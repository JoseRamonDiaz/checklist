package com.jrda.checklist.dao.document;

import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

public class Checklist {
    @Id
    private String id;
    private String name;
    private List<Check> checkList;

    public Checklist() {
        checkList = new ArrayList<>();
    }

    public Checklist(String id, String name, List<Check> checkList) {
        this.id = id;
        this.name = name;
        this.checkList = checkList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Check> getCheckList() {
        return checkList;
    }

    public void setCheckList(List<Check> checkList) {
        this.checkList = checkList;
    }
}
