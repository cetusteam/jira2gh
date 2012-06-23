package com.efilogix.jira2gh.jira;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Element;

/**
 *
 */
public class ProjectCollector {

    private List<Project> projects = new ArrayList<Project>();

    private String xsltFile;

    public void feed(Element child) {
        // List<Element> children = ticket.elements();
        //for (Element child : children) {
        Project project = project(child.element("project"));
        Issue issue = new Issue();
        project.issues.add(issue);
        issue.key = child.element("key").getTextTrim();
        issue.title = child.element("summary").getTextTrim();
        // convert body from html to text
        issue.body = processBody(child.element("description").getTextTrim());
        issue.assignee = child.element("assignee").getText();
        String resolution = child.element("resolution").getText();
        issue.resolution = resolution;
        issue.closed = Integer.parseInt(child.element("resolution")
                .attributeValue("id")) > 0;
        // find milestone
        if (child.elements().contains("fixVersion")) {
            String milestone = child.element("fixVersion").getText();
            int milestoneIdx = project.milestones.indexOf(milestone);
            if (milestoneIdx == -1) {
                project.milestones.add("milestone");
                milestoneIdx = project.milestones.size() - 1;
            }
            issue.milestone = milestoneIdx;
        }
        List<Element> relates = child
                .selectNodes("issuelinks/issuelink/*/issuekey");
        if (relates != null) {
            for (Element relatedTo : relates) {
                String relationType = relatedTo.getParent().attributeValue(
                        "description");
                String key = relatedTo.getText();
                addToMap(issue.relations, relationType, key);
            }
        }
        // }
    }

    private String processBody(String text) {
        try {
            System.out.println("----\ntext:\n" + text);
            // text = text.replace("https://efilogix.atlassian.net/browse/", "");
            text = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><body>" + text
                    + "</body></html>";
            Source xmlSource = new StreamSource(new StringReader(text));
            Source xsltSource = new StreamSource(new StringReader(xsltFile));
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans;
            trans = transFact.newTransformer(xsltSource);
            StringWriter result = new StringWriter();
            // System.out.println("processing: " + text);
            trans.transform(xmlSource, new StreamResult(result));
            String res = result.toString();
            System.out.println("--\ntransformed:\n" + res);
            return res;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Error when converting file to markdown", e);
        }
    }

    private void addToMap(Map<String, List<String>> relations,
            String relationType, String key) {
        List<String> rels = relations.get(relationType);
        if (rels == null) {
            rels = new ArrayList<String>();
            relations.put(relationType, rels);
        }
        rels.add(key);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setXsltFile(String xsltFile) {
        this.xsltFile = xsltFile;
    }

    private Project project(Element projectXml) {
        String key = projectXml.attributeValue("key");
        for (Project p : projects) {
            if (p.key.equals(key)) {
                return p;
            }
        }
        Project p = new Project();
        projects.add(p);
        p.key = key;
        p.name = projectXml.getText();
        return p;
    }
}
