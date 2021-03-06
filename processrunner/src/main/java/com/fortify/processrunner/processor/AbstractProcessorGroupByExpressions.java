package com.fortify.processrunner.processor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fortify.processrunner.context.Context;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.SimpleExpression;
import com.fortify.util.spring.expression.TemplateExpression;
import com.javamex.classmexer.MemoryUtil;

/**
 * <p>This {@link IProcessor} implementation allows for collecting
 * and grouping data stored in the {@link Context} during the 
 * {@link Phase#PROCESS} phase. The rootExpression property defines 
 * the expression used to retrieve each root object from the 
 * {@link Context}. The optional groupTemplateExpression is 
 * evaluated on each root object to identify the group that this
 * root object belongs to.</p>
 * 
 * <p>During the {@link Phase#POST_PROCESS}
 * phase, the configured group processor will be invoked for each 
 * individual group. The group processor can then access the root
 * objects contained in the current group using the 
 * {@link IContextGrouping#getCurrentGroup()} method.</p>
 * 
 * <p>If no grouping expression has been defined, the group processor
 * will be invoked immediately for every individual root object.
 * Just like grouped data, the group processor can then access this
 * root object using the {@link IContextGrouping#getCurrentGroup()} method.</p>
 * 
 * TODO Update JavaDoc as it is no longer valid
 */
public abstract class AbstractProcessorGroupByExpressions extends AbstractProcessor {
	private static final Log LOG = LogFactory.getLog(AbstractProcessorGroupByExpressions.class);
	private SimpleExpression rootExpression;
	private TemplateExpression groupTemplateExpression;
	private MultiValueMap<String, Object> groups;
	private int totalCount = 0;
	
	@Override
	protected final boolean preProcess(Context context) {
		groups = new LinkedMultiValueMap<String, Object>();
		totalCount = 0;
		return preProcessBeforeGrouping(context);
	}
	
	protected final boolean process(Context context) {
		totalCount++;
		SimpleExpression rootExpression = getRootExpression();
		TemplateExpression groupTemplateExpression = getGroupTemplateExpression();
		Object rootObject = SpringExpressionUtil.evaluateExpression(context, rootExpression, Object.class);
		
		if ( groupTemplateExpression == null ) {
			// If no group template expression is defined, we directly process the 
			// group since we do not need to group the data first.
			return processGroup(context, Arrays.asList(new Object[]{rootObject}));
		} else {
			// If a group template expression is defined, we collect the group
			// data and invoke the process() method of the group processor
			// in our postProcess() method once all data has been grouped.
			String groupKey = SpringExpressionUtil.evaluateExpression(rootObject, groupTemplateExpression, String.class);
			groups.add(groupKey, rootObject);
			return true;
		}
	}

	protected final boolean postProcess(Context context) {
		boolean result = true;
		if ( getGroupTemplateExpression() != null ) {
			// If a group template expression is defined, we call the
			// process() method on the group processor for every
			// group that we have collected.
			logStatistics();
			
			for ( Map.Entry<String, List<Object>> group : groups.entrySet() ) {
				if ( !processGroup(context, group.getValue()) ) {
					result = false; break; // Stop processing remainder of groups
				};
			}
			
		}
		groups.clear();
		return postProcessAfterProcessingGroups(context) && result;
	}
	
	// TODO Rename this method to a better name
	protected boolean preProcessBeforeGrouping(Context context) {
		return true;
	}
	
	// TODO Rename this method to a better name
	protected boolean postProcessAfterProcessingGroups(Context context) {
		return true;
	}
	
	protected abstract boolean processGroup(Context context, List<Object> currentGroup);
	
	protected void logStatistics() {
		logGroupsInfo();
		logMemoryUsage();
	}
	
	protected void logGroupsInfo() {
		LOG.info("[Process] Grouped "+totalCount+" items in "+(groups==null?0:groups.size())+" groups"); 
	}

	protected void logMemoryUsage() {
		if ( groups != null ) {
			try {
				LOG.info("[Process] Grouped data memory usage: "+MemoryUtil.deepMemoryUsageOf(groups)+" bytes");
			} catch ( IllegalStateException e ) {
				LOG.debug("[Process] Agent unavailable; memory information cannot be displayed.\n"
						+"To enable memory information, add -javaagent:path/to/classmexer-0.03.jar to Java command line.\n"
						+"Classmexer can be downloaded from http://www.javamex.com/classmexer/classmexer-0_03.zip");
			}
		}
	}
	
	public SimpleExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(SimpleExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public TemplateExpression getGroupTemplateExpression() {
		return groupTemplateExpression;
	}

	public void setGroupTemplateExpression(TemplateExpression groupTemplateExpression) {
		this.groupTemplateExpression = groupTemplateExpression;
	}
}
