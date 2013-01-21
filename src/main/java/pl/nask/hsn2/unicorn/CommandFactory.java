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

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Object.Attribute;
import pl.nask.hsn2.protobuff.Object.Attribute.Type;
import pl.nask.hsn2.unicorn.commands.CleanJobDataCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.commands.DumpCommand;
import pl.nask.hsn2.unicorn.commands.GetConfigCommand;
import pl.nask.hsn2.unicorn.commands.GetMessageCommand;
import pl.nask.hsn2.unicorn.commands.GetMessagesCommand;
import pl.nask.hsn2.unicorn.commands.GetObjectCommand;
import pl.nask.hsn2.unicorn.commands.GetWorkflowCommand;
import pl.nask.hsn2.unicorn.commands.ImportCommand;
import pl.nask.hsn2.unicorn.commands.JobDescriptorCommand;
import pl.nask.hsn2.unicorn.commands.JobDescriptorFloodCommand;
import pl.nask.hsn2.unicorn.commands.JobInfoCommand;
import pl.nask.hsn2.unicorn.commands.JobListCommand;
import pl.nask.hsn2.unicorn.commands.ListWorkflowCommand;
import pl.nask.hsn2.unicorn.commands.ObjectStoreCommand;
import pl.nask.hsn2.unicorn.commands.QueryAllCommand;
import pl.nask.hsn2.unicorn.commands.QueryNameCommand;
import pl.nask.hsn2.unicorn.commands.QueryValueCommand;
import pl.nask.hsn2.unicorn.commands.StatusCommand;
import pl.nask.hsn2.unicorn.commands.StreamMessagesCommand;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public final class CommandFactory {

	public CommandFactory() {
	}

	public static Command commandInstanceFor(CommandLineParams cmdParams) throws ConnectionException{
		CommandLine cmd = cmdParams.getCmd();
		if (cmd.hasOption("jd")){
			String[] options = cmd.getOptionValues("jd");
			String workflowName = options[0];
			String[] serviceParams = Arrays.copyOfRange(options, 1, options.length);
			return new JobDescriptorCommand(cmdParams.getFrameworkQueueName(), workflowName, serviceParams);
    	}
		else if (cmd.hasOption("jdf")){
			String[] options = cmd.getOptionValues("jdf");
			String workflowName = options[0];
			String filePath = options[1];
			return new JobDescriptorFloodCommand(cmdParams.getFrameworkQueueName(), workflowName, filePath);
    	}
    	else if (cmd.hasOption("jl")){
    		JobListCommand command = new JobListCommand(cmdParams.getFrameworkQueueName());
    		command.setBrief(cmdParams.isBrief());
    		return command;
    	}
    	else if (cmd.hasOption("ji")){
    		return new JobInfoCommand(cmdParams.getFrameworkQueueName(), new Long(cmd.getOptionValue("ji")));
    	}
    	else if (cmd.hasOption("status")){
    		StatusCommand command = new StatusCommand(cmdParams.getFrameworkQueueName(), new Long(cmd.getOptionValue("status")));
    		command.setVerbose(cmdParams.isVerbose());
    		return command;
    	}
    	else if (cmd.hasOption("lw")){
    		return new ListWorkflowCommand(cmdParams.getFrameworkQueueName());
    	}
    	else if (cmd.hasOption("gw")){
    		String[] options = cmd.getOptionValues("gw");
			String workflowName = options[0];
			String revision = options.length >1 ? options[1] : "";
    		return new GetWorkflowCommand(cmdParams.getFrameworkQueueName(), workflowName, revision);
    	}
    	else if (cmd.hasOption("gcr")){
    		return new GetConfigCommand(cmdParams.getFrameworkQueueName());
    	}
		else if (cmd.hasOption("gm")){
    		return new GetMessageCommand(cmd.getOptionValue("gm"));
    	}
		else if (cmd.hasOption("gms")){
			String[] options = cmd.getOptionValues("gms");
			String queueName = options[0];
			int numberOfMessages = Integer.parseInt(options[1]);
			return new GetMessagesCommand(queueName, numberOfMessages);
		}
    	else if (cmd.hasOption("sm")){
    		return new StreamMessagesCommand(cmd.getOptionValue("sm"));
    	}
	    else if (cmd.hasOption("osg")){
    		String[] options = cmd.getOptionValues("osg");
			Long job = new Long(options[0]);
			Long id = new Long(options[1]);
	    	return new GetObjectCommand(cmdParams.getOsQueueName(), job, id);
    	}
    	else if (cmd.hasOption("dump")){
    		Long jobId = new Long(cmd.getOptionValue("dump"));
			return new DumpCommand(cmdParams.getOsQueueName(), jobId);
    	}
		else if (cmd.hasOption("import")){
			String[] options = cmd.getOptionValues("import");
			String pathName = options[0];
			Long jobId = new Long(options[1]);
			return new ImportCommand(cmdParams.getOsQueueName(),jobId , pathName);
		}
    	else if (cmd.hasOption("osqa")){
    		Long jobId = new Long(cmd.getOptionValue("osqa"));
			ObjectStoreCommand command = new QueryAllCommand(cmdParams.getOsQueueName(), jobId);
    		command.setVerbose(cmdParams.isVerbose());
    		command.setBrief(cmdParams.isBrief());
			return command;
    	}
		else if (cmd.hasOption("osqn")){
			String[] options = cmd.getOptionValues("osqn");
			Long jobId = new Long(options[0]);
			String attributeName = options[1];
			ObjectStoreCommand command =  new QueryNameCommand(cmdParams.getOsQueueName(), jobId, attributeName);
			command.setVerbose(cmdParams.isVerbose());
			return command;
		}
		else if (cmd.hasOption("osqv")){
			String[] options = cmd.getOptionValues("osqv");
			Long jobId = new Long(options[0]);
			String attributeName = options[1];
			String type = options[2];
			String value = options[3];

			Attribute.Builder attribute = Attribute.newBuilder().setName(attributeName);
			if(type.equals("s")){
				attribute.setDataString(value);
				attribute.setType(Type.STRING);
			}
			else if(type.equals("i")){
				attribute.setDataInt(new Integer(value));
				attribute.setType(Type.INT);
			}
			else if(type.equals("b")){
				attribute.setDataBool(new Boolean(value));
				attribute.setType(Type.BOOL);
			}
			else if(type.equals("o")){
				attribute.setDataObject(new Long(value));
				attribute.setType(Type.OBJECT);
			}
			else{
				throw new IllegalStateException("Unknown value type: " + type);
			}
			ObjectStoreCommand command =  new QueryValueCommand(cmdParams.getOsQueueName(), jobId, attributeName, attribute.build());
			command.setVerbose(cmdParams.isVerbose());
			return command;
		} else if(cmd.hasOption("osjc")) {
			return new CleanJobDataCommand(cmdParams.getOsQueueName(), new Long(cmd.getOptionValue("osjc")));
		}
		else{
			throw new IllegalStateException("Unknown command");
		}
	}
}
