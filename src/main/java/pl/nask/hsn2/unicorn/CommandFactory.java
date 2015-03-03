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

package pl.nask.hsn2.unicorn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.unicorn.commands.*;
import pl.nask.hsn2.unicorn.commands.framework.*;
import pl.nask.hsn2.unicorn.commands.objectstore.*;
import pl.nask.hsn2.unicorn.commands.queue.*;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public final class CommandFactory {
	private static final ConcurrentMap<String, CommandBuilder> registeredBuilders = new ConcurrentHashMap<>();

	static {
		register("jc", JobCancelCommand.Builder.class);
		register("jl", JobListCommand.Builder.class);
		register("jlq", JobListQueryCommand.Builder.class);
		register("jd", JobDescriptorCommand.Builder.class);
		register("jdl", JobDescriptorLoopedCommand.Builder.class);
		register("ji", JobInfoCommand.Builder.class);
		register("lw", ListWorkflowCommand.Builder.class);
		register("jdf", JobDescriptorFloodCommand.Builder.class);
		register("status", StatusCommand.Builder.class);
		register("gw", GetWorkflowCommand.Builder.class);
		register("uw", UploadWorkflowCommand.Builder.class);
		register("gcr", GetConfigCommand.Builder.class);
		register("gm", GetMessageCommand.Builder.class);
		register("gms", GetMessagesCommand.Builder.class);
		register("sm", StreamMessagesCommand.Builder.class);
		register("osg", GetObjectCommand.Builder.class);
		register("dump", DumpCommand.Builder.class);
		register("import", ImportCommand.Builder.class);
		register("osqa", QueryAllCommand.Builder.class);
		register("osqn", QueryNameCommand.Builder.class);
		register("osqv", QueryValueCommand.Builder.class);
		register("osjc", CleanJobDataCommand.Builder.class);
	}

	private static void register(String cmdOptionName, Class<? extends CommandBuilder> commandBuilderClass) {
		registeredBuilders.put(cmdOptionName, newInstance(commandBuilderClass));
	}

	private static CommandBuilder newInstance(Class<? extends CommandBuilder> commandBuilderClass) {
		try {
			return commandBuilderClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Command commandInstanceFor(CommandLineParams cmdParams) throws ConnectionException {
		Command commandFromBuilder = getBuilderFor(cmdParams).build(cmdParams);
		if (commandFromBuilder == null)
			throw new IllegalStateException("Unknown command");
		return commandFromBuilder;																					
	}

	private static CommandBuilder getBuilderFor(CommandLineParams cmdParams) {
		CommandLine cmd = cmdParams.getCmd();
		
		for (Map.Entry<String, CommandBuilder> entry: registeredBuilders.entrySet()) {
			if (cmd.hasOption(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;	
	}

	private CommandFactory() {
	}
}
