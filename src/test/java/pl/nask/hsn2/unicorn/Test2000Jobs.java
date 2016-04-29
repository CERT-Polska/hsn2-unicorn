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

package pl.nask.hsn2.unicorn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.Connector;
import pl.nask.hsn2.unicorn.connector.ConnectorImpl;

/**
 * Test written for Redmine Bug #7432. Purpose of this test is to start job
 * described in a workflow many times in a row.
 */
public class Test2000Jobs {
	private static final Logger LOGGER = LoggerFactory.getLogger(Test2000Jobs.class);
	private static final int JOBS_REPEAT_NUMBER = 10;
	private static final String WORKFLOW = "w.xml";
	private static final String RABBITMQ_HOSTNAME = "127.0.0.1";

	public static void main(String[] args) throws Exception {
		String[] myArgs = { "-sa", RABBITMQ_HOSTNAME, "-jd", WORKFLOW };
		CommandLineParams cmdParams = new CommandLineParams(myArgs);
		Connector connector = ConnectorImpl.getInstance();
		connector.setServerAddress(cmdParams.getAddress());
		Command command = CommandFactory.commandInstanceFor(cmdParams);
		LOGGER.info("Jobs executing started.");
		for (int i = 0; i < JOBS_REPEAT_NUMBER; i++) {
			LOGGER.info("Job {} executed.", i);
			command.execute();
		}
		LOGGER.info("All jobs sent to broker.");
	}
}
