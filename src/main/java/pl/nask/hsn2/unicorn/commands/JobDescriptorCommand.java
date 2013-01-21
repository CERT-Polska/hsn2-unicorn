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

import pl.nask.hsn2.protobuff.Jobs.JobDescriptor;
import pl.nask.hsn2.protobuff.Service.Parameter;
import pl.nask.hsn2.protobuff.Service.ServiceConfig;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class JobDescriptorCommand extends BasicRPCCommand {

	private final static String REQUEST_TYPE = "JobDescriptor";

	private String workflowName;
	private String[] serviceParams;

	public JobDescriptorCommand(String queueName, String workflowName, String[] serviceParams) throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.workflowName = workflowName;
		this.serviceParams = serviceParams;
	}

	@Override
	protected void buildMessage() {
		JobDescriptor.Builder jobDescriptorBuilder = JobDescriptor.newBuilder().setWorkflow(workflowName);
		addServiceParams(jobDescriptorBuilder);
		message = jobDescriptorBuilder.build().toByteArray();
	}
	
	private void addServiceParams(JobDescriptor.Builder jobDescriptorBuilder){
		for(String param : serviceParams){
			jobDescriptorBuilder.addConfig(prepareServiceConfig(param));
		}
	}
	
	private ServiceConfig.Builder prepareServiceConfig(String param){
		int commaSign = param.indexOf('.');
		int equalSign = param.indexOf('=');
		String serviceName = param.substring(0,commaSign);
		String paramName = param.substring(commaSign + 1, equalSign);
		String paramValue = param.substring(equalSign + 1);
		ServiceConfig.Builder configBuilder = ServiceConfig.newBuilder()
				.setServiceLabel(serviceName)
				.addParameters(Parameter.newBuilder()
						.setName(paramName)
						.setValue(paramValue));
		return configBuilder;
	}
}
