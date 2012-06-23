package com.efilogix.jira2gh.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issue {

    public String key; // JIRA: key

    public String title; // JIRA: summary

    public String body; // JIRA: description

    public String assignee; // JIRA: ??? (optional) 

    public int milestone = -1; // JIRA: version optional. -1 for none

    public boolean closed;

    public String resolution;

    public Map<String, List<String>> relations = new HashMap<String, List<String>>();
}
