/* **********************************************************************
 * WebBee Copyright 2010 Dmitriy Rogatkin. All rights reserved.
 * Web application building blocks library. 
 *************************************************************************/
package com.beegman.webbee.util;

public class gadget {
	public static String addGadget(String accessPath, String gadgetName, String parameters, int updateInterval,
			String contentGenerationFunction) {
		StringBuffer result = new StringBuffer(256);
		result.append("<div id=\"gadget_").append(gadgetName).append("\">\n</div>");
		result.append("<script>");

		result.append("\n   var ").append("timer").append(gadgetName);
		//result.append("; \n update").append(gadgetName).append("();");
		result.append("; \r\n if (gadgetHandlers != undefined) gadgetHandlers.push(").append("update").append(gadgetName).append(");");
		//result.append("=setTimeout(update").append(gadgetName)
			//	.append(", ").append(updateInterval).append(");");
		result.append("\n   var sta_").append(gadgetName).append(';');
		result.append("\n    function update").append(gadgetName).append("() {");
		result.append("\n      makeJSONAjaxCall('").append(accessPath).append('/').append(gadgetName).append("?random='+Math.random()").append(", ").append(
				parameters).append(", true,");
		result.append("\n        function(json) {");
		result.append("\n            getElement('gadget_").append(gadgetName).append("').innerHTML =");
		//json[0]+'('+json[1]+')';
		result.append(contentGenerationFunction).append(';');
		result.append("\n            if (sta_").append(gadgetName).append(") {");
		result.append("\n                getElement('status').innerHTML = '';");
		result.append("\n                sta_").append(gadgetName).append(" = false;");
		result.append("\n		            }\n		        },");
		result.append("\n        function(e) {");
		result.append("\n        	if (sta_").append(gadgetName).append(")");
		result.append("\n        	   return;");
		result.append("\n           getElement('status').innerHTML = 'Can\\'t connect to server';");
		result.append("\n           sta_").append(gadgetName).append(" = true;");
		result.append("\n		        });");
		result.append("\n      ").append("timer").append(gadgetName).append("=setTimeout(update").append(gadgetName).append(',').append(updateInterval).append(");");
		result.append("\n   }\n		</script>");
		return result.toString();
	}
}
