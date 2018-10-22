package com.crutchbag.mks;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Helper {
	public static ObjectNode MapToJSON(Map<String, List<String>> map) { // Since we use JSON for communication we wont need objects in there, just strings (hopefully)
		final JsonNodeFactory factory = JsonNodeFactory.instance;
		final ObjectNode node = factory.objectNode();
		
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			// key is always present; if value is empty put quotes, if not convert array to string
			node.put(entry.getKey(),entry.getValue() == null ? "" : "\"" + String.join("\",\"", entry.getValue()) + "\"");
			//TODO quotes for strings not working properly	
		}
		
		return node;
	}
}
