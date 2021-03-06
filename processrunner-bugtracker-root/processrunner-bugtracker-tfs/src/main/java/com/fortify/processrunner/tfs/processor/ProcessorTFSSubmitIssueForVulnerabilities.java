package com.fortify.processrunner.tfs.processor;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortify.processrunner.common.issue.SubmittedIssue;
import com.fortify.processrunner.common.processor.AbstractProcessorSubmitIssueForVulnerabilities;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.context.ContextProperty;
import com.fortify.processrunner.tfs.connection.TFSRestConnection;
import com.fortify.processrunner.tfs.context.IContextTFS;
import com.fortify.processrunner.tfs.util.TFSWorkItemJSONObjectBuilder;
import com.fortify.processrunner.tfs.util.WorkItemTypeToFieldRenamer;

/**
 * This {@link AbstractProcessorSubmitIssueForVulnerabilities} implementation
 * submits issues to TFS.
 */
public class ProcessorTFSSubmitIssueForVulnerabilities extends AbstractProcessorSubmitIssueForVulnerabilities {
	private static final TFSWorkItemJSONObjectBuilder MAP_TO_JSON = new TFSWorkItemJSONObjectBuilder("add");
	private String defaultWorkItemType = "Bug";
	private WorkItemTypeToFieldRenamer fieldRenamer = new WorkItemTypeToFieldRenamer();
	
	@Override
	public void addBugTrackerContextProperties(Collection<ContextProperty> contextProperties, Context context) {
		contextProperties.add(new ContextProperty("TFSCollection", "TFS collection containing the project to submit vulnerabilities to", context, null, true));
		contextProperties.add(new ContextProperty("TFSProject", "TFS project to submit vulnerabilities to", context, null, true));
		contextProperties.add(new ContextProperty("TFSWorkItemType", "TFS work item type", context, getDefaultWorkItemType(), false));
	}
	
	@Override
	protected String getBugTrackerName() {
		return "TFS";
	}
	
	@Override
	protected SubmittedIssue submitIssue(Context context, LinkedHashMap<String, Object> issueData) {
		IContextTFS contextTFS = context.as(IContextTFS.class);
		TFSRestConnection conn = contextTFS.getTFSConnectionRetriever().getConnection();
		issueData.put("System.Title", StringUtils.abbreviate((String)issueData.get("System.Title"), 254));
		String workItemType = getWorkItemType(contextTFS);
		fieldRenamer.renameFields(workItemType, issueData);
		JSONArray operations = MAP_TO_JSON.getJSONArray(issueData);
		return conn.submitIssue(contextTFS.getTFSCollection(), contextTFS.getTFSProject(), workItemType, operations);
	}
	
	protected String getWorkItemType(IContextTFS context) {
		String issueType = context.getTFSWorkItemType();
		return issueType!=null?issueType:getDefaultWorkItemType();
	}

	public String getDefaultWorkItemType() {
		return defaultWorkItemType;
	}

	public void setDefaultWorkItemType(String defaultWorkItemType) {
		this.defaultWorkItemType = defaultWorkItemType;
	}

	public WorkItemTypeToFieldRenamer getFieldRenamer() {
		return fieldRenamer;
	}

	@Autowired(required=false)
	public void setFieldRenamer(WorkItemTypeToFieldRenamer fieldRenamer) {
		this.fieldRenamer = fieldRenamer;
	}
}
