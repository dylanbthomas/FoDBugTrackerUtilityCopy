package com.fortify.processrunner.processor;

import java.util.LinkedHashMap;
import java.util.List;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.map.MapBuilder;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterAppendValuesFromExpressionMap;
import com.fortify.processrunner.util.map.MapBuilder.MapUpdaterPutValuesFromExpressionMap;
import com.fortify.util.spring.expression.TemplateExpression;

public abstract class AbstractProcessorBuildObjectMapFromGroupedObjects extends AbstractProcessorGroupByExpressions {
	private LinkedHashMap<String,TemplateExpression> fields;
	private LinkedHashMap<String,TemplateExpression> appendedFields;
	
	@Override
	protected boolean processGroup(Context context, List<Object> currentGroup) {
		LinkedHashMap<String, Object> map = new MapBuilder()
				.addMapUpdater(new MapUpdaterPutValuesFromExpressionMap(currentGroup.get(0), getFields()))
				.addMapUpdater(new MapUpdaterAppendValuesFromExpressionMap(currentGroup, getAppendedFields()))
				.build(new LinkedHashMap<String, Object>());
		return processMap(context, currentGroup, map);
	}
	
	protected abstract boolean processMap(Context context, List<Object> currentGroup, LinkedHashMap<String, Object> map);

	public LinkedHashMap<String,TemplateExpression> getFields() {
		return fields;
	}

	public void setFields(LinkedHashMap<String,TemplateExpression> fields) {
		this.fields = fields;
	}

	public LinkedHashMap<String,TemplateExpression> getAppendedFields() {
		return appendedFields;
	}

	public void setAppendedFields(LinkedHashMap<String,TemplateExpression> appendedFields) {
		this.appendedFields = appendedFields;
	}

}
