package com.efilogix.jira2gh.jira;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;

import com.efilogix.jira2gh.UploadToGithub;

public class Main {

    public static void main(String[] args) throws Exception {
        String xsltFile = IOUtils.toString(
                new AutoCloseInputStream(ClassLoader
                        .getSystemResourceAsStream("markdown.xslt")),
                "iso-8859-1");
        System.out.println("First chars of template: "
                + StringUtils.left(xsltFile, 50));
        //
        String xml = FileUtils.readFileToString(new File(args[0]));
        Reader xmlReader = new StringReader(xml);
        //
        int baseGhIssue = Integer.parseInt(args[1]);
        //
        ProjectCollector collector = new ProjectCollector();
        collector.setXsltFile(xsltFile);
        //
        RssReader rssReader = new RssReader();
        rssReader.setCollector(collector);
        rssReader.setXml(xmlReader);
        rssReader.run();
        //
        for (Project project : collector.getProjects()) {
            Converter conv = new Converter();
            conv.setProject(project);
            conv.setBaseGhIssue(baseGhIssue);
            conv.run();
        }
        // Upload the project data to github
        UploadToGithub github = new UploadToGithub();
        github.setCollector(collector);
        // github.run();
    }
}
