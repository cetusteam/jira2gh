package com.efilogix.jira2gh.jira;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Project {

    public String key;

    public String name;

    public ArrayList<String> milestones = new ArrayList<String>();

    public List<Issue> issues = new LinkedList<Issue>();
}
