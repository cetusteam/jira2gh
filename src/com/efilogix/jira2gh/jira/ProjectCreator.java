package com.efilogix.jira2gh.jira;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ProjectCreator {

    Connection connection;

    List<Project> projects = new ArrayList<Project>();

    private int baseGhIssue = -1;

    private String dbUrl;

    public void run() throws Exception {
        connection = DriverManager.getConnection(dbUrl);
        // create projects
        PreparedStatement ps = connection
                .prepareStatement("select jg_key, jg_name, jg_description"
                        + " from t_project");
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            String key = rs.getString(1);
            String name = rs.getString(2);
            String description = rs.getString(3);
            Project p = new Project(key, name, description);
            projects.add(p);
        }
        ps.close();
        // create milestones
        ps = connection
                .prepareStatement("select p.jg_key, v.jg_name, v.jg_description, v.jg_released, v.jg_releasedate"
                        + " from t_project p, t_version v"
                        + " where p.jg_id = v.jg_project");
        ps.execute();
        rs = ps.getResultSet();
        while (rs.next()) {
            MileStone milestone = new MileStone();
            milestone.title = rs.getString(2);
            milestone.description = rs.getString(3);
            milestone.state = "true".equals(rs.getString(4)) ? "closed"
                    : "open";
            milestone.dueOn = rs.getString(5);
            Project p = findProject(rs.getString(1));
            p.milestones.add(milestone);
        }
        ps.close();
        // create issues
        for (Project p : projects) {
            ps = connection
                    .prepareStatement("select issue.jg_key, st.jg_name, issue.jg_summary, issue.jg_description, issue.jg_assignee "
                            + " from t_issue issue, t_project p, t_status st "
                            + " where issue.jg_project = p.jg_id and st.jg_id = issue.jg_status and p.jg_key = ?");
            ps.setString(1, p.key);
            ps.execute();
            rs = ps.getResultSet();
            while (rs.next()) {
                Issue issue = new Issue();
                p.issues.add(issue);
                issue.key = rs.getString(1);
                issue.title = rs.getString(3);
                issue.body = rs.getString(4);
                issue.assignee = rs.getString(5);
                issue.resolution = rs.getString(2);
                if (issue.resolution.equals("Closed")
                        || issue.resolution.equals("Resolved")) {
                    issue.closed = true;
                }
                findRelations(issue);
            }
        }
        ps.close();
        // attributes for each issue
        for (Project p : projects) {
            int i = -1;
            for (Issue issue : p.issues) {
                i++;
                issue.ghId = i + baseGhIssue;
                issue.comments = listComments(issue.key);
                issue.milestone = selectMilestones(p, issue.key);
            }
        }
        connection.close();
    }

    private void findRelations(Issue issue) throws SQLException {
        //        String sql = "select ltype.jg_outward, dest.jg_key "
        //                + "from t_issuelink link, t_issuelinktype ltype, t_issue source, t_issue dest "
        //                + "where link.jg_linktype=ltype.jg_id "
        //                + "  and link.jg_destination=dest.jg_id "
        //                + "  and link.jg_source=source.jg_id "
        //                + "  and source.jg_key = ?";
        String sql = "select linktype.jg_inward, linktype.jg_outward, src.jg_key, dest.jg_key "
                + "from t_issuelink link, t_issuelinktype linktype, t_issue src, t_issue dest "
                + "where link.jg_linktype    = linktype.jg_id "
                + "  and link.jg_destination = dest.jg_id "
                + "  and link.jg_source      = src.jg_id "
                + "  and (src.jg_key = ? or dest.jg_key = ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, issue.key);
        ps.setString(2, issue.key);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            String relIn = rs.getString(1);
            String relOut = rs.getString(2);
            String src = rs.getString(3);
            String dest = rs.getString(4);
            String rel;
            String key;
            if (StringUtils.equals(issue.key, src)) {
                rel = relOut;
                key = dest;
            } else {
                rel = relIn;
                key = src;
            }
            List<String> lst = issue.relations.get(rel);
            if (lst == null) {
                issue.relations.put(rel, lst = new ArrayList<String>());
            }
            lst.add(key);
        }
        ps.close();
    }

    private String selectMilestones(Project p, String key) throws SQLException {
        List<String> milestones = new ArrayList<String>();
        String sql = "select vers.jg_name "
                + "from t_version vers, t_issue issue, t_nodeassociation node "
                + "where jg_sourceNodeEntity='Issue' and jg_sinkNodeEntity='Version' and jg_associationType='IssueFixVersion' "
                + "and vers.jg_id = node.jg_sinkNodeId  "
                + "and issue.jg_id = node.jg_sourceNodeId "
                + "and issue.jg_key = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, key);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            milestones.add(rs.getString(1));
        }
        ps.close();
        return StringUtils.join(milestones, ",");
    }

    private List<Comment> listComments(String ticketKey) throws Exception {
        String sql = "select jg_created, com.jg_updateauthor, com.jg_body"
                + "  from t_action com,  t_issue issue"
                + " where jg_type='comment' and com.jg_issue = issue.jg_id and issue.jg_key = ?"
                + " order by jg_created";
        List<Comment> comments = new ArrayList<Comment>();
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, ticketKey);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            Comment c = new Comment();
            c.date = rs.getString(1);
            c.author = rs.getString(2);
            c.comment = rs.getString(3);
            comments.add(c);
        }
        ps.close();
        return comments;
    }

    private Project findProject(String prjKey) {
        for (Project p : projects) {
            if (p.key.equals(prjKey)) {
                return p;
            }
        }
        throw new RuntimeException("cannot find project with key:" + prjKey);
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setBaseGhIssue(int baseGhIssue) {
        this.baseGhIssue = baseGhIssue;
    }
}
