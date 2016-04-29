/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.1.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
