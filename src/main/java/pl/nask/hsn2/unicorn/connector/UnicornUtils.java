package pl.nask.hsn2.unicorn.connector;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Service.Parameter;
import pl.nask.hsn2.protobuff.Service.ServiceConfig;

public final class UnicornUtils {
	private UnicornUtils() {
	}
	
	public static String getBriefRepresentation(final Attribute a) {
		return a.getType().name() + " : " + a.getName() + " = " + getValueStringRepresentation(a);
	}

	public static String getValueStringRepresentation(final Attribute a) {
		String s = "";
		switch (a.getType()) {
		case BOOL:
			s += a.getDataBool();
			break;
		case INT:
			s += a.getDataInt();
			break;
		case TIME:
			s += (a.getDataTime() + " / " + formattedDateTime(a.getDataTime()));
			break;
		case FLOAT:
			s += a.getDataFloat();
			break;
		case STRING:
			s += a.getDataString();
			break;
		case OBJECT:
			s += a.getDataObject();
			break;
		case BYTES:
			s += a.getDataBytes();
			break;
		default:
			break;
		}
		return s;
	}

	public static String formattedDateTime(long timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");		
		return dateFormat.format(new Date(timestamp));
	}

	public static ServiceConfig.Builder prepareServiceConfig(String param) {
		int commaSign = param.indexOf('.');
		int equalSign = param.indexOf('=');
		String serviceName = param.substring(0, commaSign);
		String paramName = param.substring(commaSign + 1, equalSign);
		String paramValue = param.substring(equalSign + 1);
		ServiceConfig.Builder configBuilder = ServiceConfig.newBuilder().setServiceLabel(serviceName)
				.addParameters(Parameter.newBuilder().setName(paramName).setValue(paramValue));
		return configBuilder;
	}
	
	public static Map<String, String> getAttributesMap(List<Attribute> attrsList) {
		Map<String, String> attributes = new HashMap<>();
		for(Attribute attribute : attrsList){
			String value = getValueStringRepresentation(attribute);			
			attributes.put(attribute.getName(), value);
		}
		return attributes;
	}
}
