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

package pl.nask.hsn2.unicorn.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Info.InfoData;
import pl.nask.hsn2.protobuff.Info.InfoRequest;
import pl.nask.hsn2.protobuff.Info.InfoType;
import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.connector.ConnectionException;
import pl.nask.hsn2.unicorn.connector.Response;

import com.google.protobuf.InvalidProtocolBufferException;

public class StatusCommand extends RPCCommand {	
	private final static String REQUEST_TYPE = "InfoRequest";
	private Long jobId;
	private boolean verbose;
	
	public StatusCommand(String queueName, Long jobId) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.jobId = jobId;
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException {
		while(true){
			connector.send(request);
			Response response = connector.receive();
			if("InfoData".equals(response.getType())){
				ObjectData data;
				try {
					data = InfoData.parseFrom(response.getBody()).getData();
				} catch (InvalidProtocolBufferException e) {
					LOGGER.error(e.getMessage(),e);
					break;
				}
				
				Map<String,String> attributes = getAttributesMap(data.getAttrsList());
				if("PROCESSING".equals(attributes.get("job_status"))){
				
					StringBuilder msg = new StringBuilder("status: PROCESSING")
							.append(" time: ").append(attributes.get("job_processing_time_sec"))
							.append(" subProc: ").append(attributes.get("job_active_subprocess_count"));
				
					if(verbose){
						for (Entry<String, String> entry : attributes.entrySet()){
							if (entry.getKey().startsWith("task_count_")){
								msg.append("\n")
									.append(entry.getKey())
									.append(" : ")
									.append(entry.getValue());
							}
						}
					}
					LOGGER.info(msg.toString());
				}
				else{
					LOGGER.info(response.toString());
					break;
				}
			}
			else{
				LOGGER.error("Message type is not InfoData:" + response.getType() +"\n" + response.toString());
				break;
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOGGER.error(e.getMessage(),e);
				break;
			}
		}
	}

	@Override
	protected void buildMessage() {
		InfoRequest infoRequest = InfoRequest.newBuilder().setType(InfoType.JOB).setId(jobId).build();
		message = infoRequest.toByteArray();
	}

	private Map<String, String> getAttributesMap(List<Attribute> attrsList) {
		Map<String, String> attributes = new HashMap<>();
		for(Attribute attribute : attrsList){
			String value = "";
			switch (attribute.getType()) {
				case BOOL:
					value = String.valueOf(attribute.getDataBool());
					break;
				case STRING:
					value = String.valueOf(attribute.getDataString());
					break;
				case INT:
					value = String.valueOf(attribute.getDataInt());
					break;
				case TIME:
					value = String.valueOf(attribute.getDataTime());
					break;
				case FLOAT:
					value = String.valueOf(attribute.getDataFloat());
					break;
				default:
					break;
			}
			attributes.put(attribute.getName(), value);
		}
		return attributes;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;		
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			StatusCommand command = new StatusCommand(cmdParams.getFrameworkQueueName(), Long.valueOf(cmd.getOptionValue("status")));
			command.setVerbose(cmdParams.isVerbose());
			return command;
		}
	}
	
}
