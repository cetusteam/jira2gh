package com.efilogix.jira2gh.jira;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** reads all the data and rename issues */
public class Converter implements Runnable {

    private Project project;

    private int baseGhIssue = -1;

    Pattern p = Pattern.compile("(" + project.key + "-[0-9]+)([^0-9])");

    private Map<String, Integer> issuesMap = new HashMap<String, Integer>();

    @Override
    public void run() {
        System.out.println("Converting project:" + project.name + " Key:"
                + project.key + " Issues:" + project.issues.size());
        mapIssues();
        for (Issue issue : project.issues) {
            StringBuilder bodyBuilder = new StringBuilder(issue.body);
            // add information about related issue
            if (issue.relations.size() > 0) {
                bodyBuilder.append("\n\n");
                for (Map.Entry<String, List<String>> relationE : issue.relations
                        .entrySet()) {
                    bodyBuilder.append(relationE.getKey() + ":\n");
                    for (String key : relationE.getValue()) {
                        bodyBuilder.append(" * " + key + "\n");
                    }
                }
            }
            // add resolution information
            if (issue.closed && issue.resolution.equals("FIXED")) {
                bodyBuilder.append("\n\nResolution: " + issue.resolution);
            }
            // replaces body
            issue.body = bodyBuilder.toString();
            issue.body = ticketReplacer(issue.body);
            for (Comment comment : issue.comments) {
                comment.comment = ticketReplacer(comment.comment);
            }
        }
    }

    private String ticketReplacer(String body) {
        // map issues in the body
        Matcher m = p.matcher(body);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Integer ghId = issuesMap.get(key);
            if (ghId == null) {
                throw new RuntimeException("Issue " + key
                        + " not found in the project. List of issues:"
                        + issuesMap.keySet());
            }
            m.appendReplacement(sb, "#" + ghId + m.group(2));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private void mapIssues() {
        int ghId = baseGhIssue;
        for (Issue issue : project.issues) {
            issuesMap.put(issue.key, ghId);
            ghId++;
        }
    }

    public void setBaseGhIssue(int baseGhIssue) {
        this.baseGhIssue = baseGhIssue;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
