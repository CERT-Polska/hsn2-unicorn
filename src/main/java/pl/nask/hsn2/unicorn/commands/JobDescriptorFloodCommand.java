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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.commands.framework.JobDescriptorCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class JobDescriptorFloodCommand implements Command {


	private static final Logger LOGGER = LoggerFactory.getLogger(JobDescriptorFloodCommand.class);
	
	private String frameworkQueueName;
	private String workflowName;
	private String filePath;
	
	public JobDescriptorFloodCommand(String frameworkQueueName, String workflowName, String filePath) {
		this.frameworkQueueName = frameworkQueueName;
		this.workflowName = workflowName;
		this.filePath = filePath;
	}

	@Override
	public void execute() throws ConnectionException, FailedCommandException {
		int count = 0;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));){
			for (String url = reader.readLine(); url != null; url = reader.readLine()){
				//TODO:serviceName should be parameterized
				String[] serviceParams =  {"feeder.url=" + url};
				new JobDescriptorCommand(frameworkQueueName, workflowName, serviceParams).execute();
				count++;
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
		LOGGER.info("JobDescriptors sent: " + count);
	}
	
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			String[] options = cmd.getOptionValues("jdf");
			String workflowName = options[0];
			String filePath = options[1];
			return new JobDescriptorFloodCommand(cmdParams.getFrameworkQueueName(), workflowName, filePath);
		}
	}
}
