/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
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

package pl.nask.hsn2.unicorn.commands;

import java.util.Set;
import java.util.TreeSet;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class JobInfoCommand extends BasicRPCCommand {

	private final static String REQUEST_TYPE = "InfoRequest";
	private Long jobId;

	public JobInfoCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}
	
	@Override
	protected void executeSpecyficJob() throws ConnectionException {
		super.executeSpecyficJob();
	}

	@Override
	protected void buildMessage() {
		InfoRequest infoRequest = InfoRequest.newBuilder().setType(InfoType.JOB).setId(jobId).build();
		message = infoRequest.toByteArray();
	}

	@Override
	protected void displayResults(Response response) {
		String type = response.getType();
		StringBuilder displayResults = new StringBuilder("JOB INFO\n");
		if ("InfoData".equals(type)) {
			try {
				Set<String> displayValues = new TreeSet<>();
				InfoData data = InfoData.parseFrom(response.getBody());
				ObjectData objData = data.getData();
				for (Attribute attr : objData.getAttrsList()) {
					displayValues.add(attr.getName() + " = " + getValueStringRepresentation(attr));
				}
				for (String s : displayValues) {
					displayResults.append(s).append("\n");
				}
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} else if ("InfoError".equals(type)) {
			try {
				InfoError error = InfoError.parseFrom(response.getBody());
				displayResults.append("Could not get job info: ").append(error.getReason());
			} catch (InvalidProtocolBufferException e) {
				// Should never happen.
				displayResults.append("Could not deserialize response message").append(e);
			}
		} else {
			displayResults.append("WRONG MSG TYPE: ").append(type);
		}
		super.displayResults(displayResults.toString());
	}

	private String getValueStringRepresentation(final Attribute a) {
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
