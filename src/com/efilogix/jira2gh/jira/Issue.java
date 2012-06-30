package com.efilogix.jira2gh.jira;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Issue {

    public String key; // JIRA: key

    public String title; // JIRA: summary

    public String body; // JIRA: description

    public String assignee; // JIRA: ??? (optional) 

    public String milestone; // JIRA: comma separated, first is most recent

    public boolean closed;

    public String resolution;

    public List<Comment> comments = new ArrayList<Comment>();

    public Map<String, List<String>> relations = new HashMap<String, List<String>>();
}
