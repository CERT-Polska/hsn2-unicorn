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

package pl.nask.hsn2.unicorn.commands.framework;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoError;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;
import pl.nask.hsn2.unicorn.connector.UnicornUtils;

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
		StringBuilder displayResults = new StringBuilder("JOB INFO: " + jobId + "\n");
		if ("InfoData".equals(type)) {
			try {
				Set<String> displayValues = new TreeSet<>();
				InfoData data = InfoData.parseFrom(response.getBody());
				ObjectData objData = data.getData();
				for (Attribute attr : objData.getAttrsList()) {
					displayValues.add(attr.getName() + " = " + UnicornUtils.getValueStringRepresentation(attr));
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
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,	CommandLine cmd) throws ConnectionException {
			return new JobInfoCommand(cmdParams.getFrameworkQueueName(), Long.valueOf(cmd.getOptionValue("ji")));
		}
	}
}
