package com.efilogix.jira2gh.jira;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/** reads all the data and rename issues */
public class Converter implements Runnable {

    private Project project;

    private List<Project> projects;

    private Pattern issuePattern;

    @Override
    public void run() {
        System.out.println("Converting project:" + project.name + " Key:"
                + project.key + " Issues:" + project.issues.size());
        issuePattern = Pattern.compile("(" + project.key + "-[0-9]+)([^0-9])");
        for (Issue issue : project.issues) {
            StringBuilder bodyBuilder = new StringBuilder(issue.body);
            // add information about related issue
            if (issue.relations.size() > 0) {
                bodyBuilder.append("\n\n");
                for (Map.Entry<String, List<String>> relationE : issue.relations
                        .entrySet()) {
                    bodyBuilder.append("this ticket " + relationE.getKey()
                            + ":\n");
                    for (String key : relationE.getValue()) {
                        bodyBuilder.append(" * " + key);
                        String relPrjKey = StringUtils
                                .substringBefore(key, "-");
                        Project relProj = findProject(relPrjKey);
                        bodyBuilder.append(" ").append(
                                findIssue(relProj, key).title);
                        bodyBuilder.append("\n");
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
            System.out.println("----------------------");
            System.out.println("issue #:" + issue.ghId + "   " + issue.key);
            System.out.println("body:" + issue.body);
        }
    }

    private Issue findIssue(String key) {
        Project p = findProject(StringUtils.substringBefore(key, "-"));
        return findIssue(p, key);
    }

    private Issue findIssue(Project relProj, String key) {
        for (Issue issue : relProj.issues) {
            if (issue.key.equals(key)) {
                return issue;
            }
        }
        return null;
    }

    private Project findProject(String relPrjKey) {
        for (Project p : projects) {
            if (p.key.startsWith(relPrjKey)) {
                return p;
            }
        }
        return null;
    }

    private String ticketReplacer(String body) {
        // map issues in the body
        Matcher m = issuePattern.matcher(body);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Issue issue = findIssue(key);
            m.appendReplacement(sb, "#" + issue.ghId + m.group(2));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
