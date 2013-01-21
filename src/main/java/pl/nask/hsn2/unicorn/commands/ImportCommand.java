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

import pl.nask.hsn2.unicorn.FailedCommandException;
import pl.nask.hsn2.unicorn.ObjectsDumper;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class ImportCommand extends ObjectStoreCommand {

	private String pathName;

	public ImportCommand(String queueName, Long jobId, String pathName) throws ConnectionException {
		super(queueName, jobId);
		this.pathName = pathName;
	}

	@Override
	protected void buildMessage() {
		message = ObjectsDumper.getObjectFromJsonFile(pathName, jobId);
	}

	@Override
	protected void executeSpecyficJob() throws ConnectionException, FailedCommandException {
		sendRequestAndReceiveOSResponse();
		LOGGER.info("Import result: " + osResponseToString());
	}

}
