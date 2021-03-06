package com.fortify.processrunner.fod.processor.enrich;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.fod.context.IContextFoD;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * This class determines the FoD browser-viewable deep link for the current vulnerability,
 * and adds this link as the 'deepLink' property to the current vulnerability JSON object.
 */
public class FoDProcessorEnrichWithVulnDeepLink extends AbstractFoDProcessorEnrich {
	private TemplateExpression deepLinkUriExpression = SpringExpressionUtil.parseTemplateExpression("redirect/Issues/${vulnId}");

	@Override
	protected boolean enrich(Context context, JSONObject currentVulnerability) throws JSONException {
		String baseUrl = context.as(IContextFoD.class).getFoDConnectionRetriever().getBaseUrl();
		String deepLink = baseUrl + SpringExpressionUtil.evaluateExpression(currentVulnerability, deepLinkUriExpression, String.class);
		currentVulnerability.put("deepLink", deepLink);
		return true;
	}
}
