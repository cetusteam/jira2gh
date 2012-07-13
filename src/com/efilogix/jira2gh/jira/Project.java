package com.efilogix.jira2gh.jira;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Project {

    public String key;

    public String name;

    public String description;

    public ArrayList<MileStone> milestones = new ArrayList<MileStone>();

    public List<Issue> issues = new LinkedList<Issue>();

    public Project(String key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description;
    }
}
