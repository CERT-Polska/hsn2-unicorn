package pl.nask.hsn2.unicorn.connector;

import pl.nask.hsn2.protobuff.Object.Attribute;

public class UnicornUtils {
	private UnicornUtils() {
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
			s += a.getDataTime();
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
}
