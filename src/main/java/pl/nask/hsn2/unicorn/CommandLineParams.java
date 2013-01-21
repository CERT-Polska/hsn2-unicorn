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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class CommandLineParams {
	private Options options = new Options();
	private CommandLine cmd;
	private String address = "127.0.0.1";
	private String osQueueName = "os:h";
	private String frameworkQueueName = "fw:h";
	private boolean verbose;
	private boolean dump;
	private boolean brief;

	public CommandLineParams(String[] args) {
		initOptions();
		parseParams(args);
	}

	private void initOptions() {
		OptionGroup optionGroup = new OptionGroup();
		optionGroup.setRequired(true);

		OptionBuilder.withArgName("workflowName [serviceParams]");
		OptionBuilder.withLongOpt("jobDescriptor");
		OptionBuilder.hasArgs();
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Job descriptor");
		optionGroup.addOption(OptionBuilder.create("jd"));

		OptionBuilder.withArgName("workflowName filePath");
		OptionBuilder.withLongOpt("jobDescriptorFlood");
		OptionBuilder.hasArgs();
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Job descriptor flood");
		optionGroup.addOption(OptionBuilder.create("jdf"));
		
		OptionBuilder.withLongOpt("jobList");
		OptionBuilder.withDescription("Job list");
		optionGroup.addOption(OptionBuilder.create("jl"));

		OptionBuilder.withArgName("jobId");
		OptionBuilder.withLongOpt("jobInfo");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Job info");
		optionGroup.addOption(OptionBuilder.create("ji"));

		OptionBuilder.withLongOpt("listWorkflow");
		OptionBuilder.withDescription("Workflow list");
		optionGroup.addOption(OptionBuilder.create("lw"));

		OptionBuilder.withArgName("workflowName revision");
		OptionBuilder.withLongOpt("getWorkflow");
		OptionBuilder.hasArgs();
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("get Workflow");
		optionGroup.addOption(OptionBuilder.create("gw"));

		OptionBuilder.withLongOpt("getConfig");
		OptionBuilder.withDescription("get config");
		optionGroup.addOption(OptionBuilder.create("gcr"));

		OptionBuilder.withArgName("queueName");
		OptionBuilder.withLongOpt("getMessage");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Print message from queue without consuming");
		optionGroup.addOption(OptionBuilder.create("gm"));

		OptionBuilder.withArgName("queueName messagesNumber");
		OptionBuilder.withLongOpt("getMessages");
		OptionBuilder.hasArgs(2);
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Print messages from queue without consuming");
		optionGroup.addOption(OptionBuilder.create("gms"));

		OptionBuilder.withArgName("queueName messagesNumber");
		OptionBuilder.withLongOpt("streamMessage");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("get and print messages from queue");
		optionGroup.addOption(OptionBuilder.create("sm"));

		OptionBuilder.withArgName("jobId objectId");
		OptionBuilder.withLongOpt("objectStoreGet");
		OptionBuilder.hasArgs(2);
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("return object having the same jobId and objectId");
		optionGroup.addOption(OptionBuilder.create("osg"));

		OptionBuilder.withArgName("jobId");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Make dump");
		optionGroup.addOption(OptionBuilder.create("dump"));

		OptionBuilder.withArgName("jobId");
		OptionBuilder.withLongOpt("objectStoreQueryAll");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Query for objectStore return ALL objects with jobId");
		optionGroup.addOption(OptionBuilder.create("osqa"));

		OptionBuilder.withArgName("jobId name");
		OptionBuilder.withLongOpt("objectStoreQueryName");
		OptionBuilder.hasArgs(2);
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Query for objectStore return objects having the same jobId and attribute name as given");
		optionGroup.addOption(OptionBuilder.create("osqn"));

		OptionBuilder.withArgName("jobId name type[s/i/b/o] value");
		OptionBuilder.withLongOpt("objectStoreQueryValue");
		OptionBuilder.hasArgs(4);
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Query for objectStore return objects having the same jobId attribute named and value as given.");
		optionGroup.addOption(OptionBuilder.create("osqv"));

		OptionBuilder.withArgName("jobId");
		OptionBuilder.withLongOpt("objectStoreJobClean");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Sends JobFinished message. As a result of this msg ObjectStore will clean all data with given jobId.");
		optionGroup.addOption(OptionBuilder.create("osjc"));

		OptionBuilder.withLongOpt("help");
		OptionBuilder.withDescription("Prints this help");
		optionGroup.addOption(OptionBuilder.create("h"));

		OptionBuilder.withArgName("fileName jobId");
		OptionBuilder.hasArgs(2);
		OptionBuilder.withValueSeparator(' ');
		OptionBuilder.withDescription("Import objects from file to os");
		optionGroup.addOption(OptionBuilder.create("import"));

		OptionBuilder.withArgName("jobId");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("checking status of job");
		optionGroup.addOption(OptionBuilder.create("status"));

		options.addOptionGroup(optionGroup);

		OptionBuilder.withArgName("address");
		OptionBuilder.withLongOpt("serverAddress");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Address for server, default: " + address);
		options.addOption(OptionBuilder.create("sa"));

		OptionBuilder.withArgName("name");
		OptionBuilder.withLongOpt("queueName");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Name for objestStore queue, default: " + osQueueName);
		options.addOption(OptionBuilder.create("qn"));

		OptionBuilder.withArgName("name");
		OptionBuilder.withLongOpt("fwQueueName");
		OptionBuilder.hasArg();
		OptionBuilder.withDescription("Name for framework queue, default: " + frameworkQueueName);
		options.addOption(OptionBuilder.create("fqn"));

		OptionBuilder.withLongOpt("verbose");
		OptionBuilder.withDescription("Whether to print objects in osqa, osqn and osqv, default: not enabled.");
		options.addOption(OptionBuilder.create("verb"));

		OptionBuilder.withLongOpt("brief");
		OptionBuilder.withDescription("Used with '-jl' and '-osqa' only. Shows brief info. Default: not enabled.");
		options.addOption(OptionBuilder.create("b"));
	}

	private void parseParams(String[] args) {
		try {
			this.cmd = new PosixParser().parse(options, args);
			if (cmd.hasOption("help")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.setWidth(110);
				formatter.printHelp("java -jar ...", options);
				System.exit(0);
			}
			if (cmd.hasOption("sa")) {
				address = cmd.getOptionValue("sa");
			}
			if (cmd.hasOption("qn")) {
				osQueueName = cmd.getOptionValue("qn");
			}
			if (cmd.hasOption("fqn")) {
				frameworkQueueName = cmd.getOptionValue("fqn");
			}
			if (cmd.hasOption("verb")) {
				verbose = true;
			}
			if (cmd.hasOption("b")) {
				brief = true;
			}
			if (cmd.hasOption("dump")) {
				dump = true;
			}

		} catch (ParseException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	public CommandLine getCmd() {
		return cmd;
	}

	public String getAddress() {
		return address;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public String getOsQueueName() {
		return osQueueName;
	}

	public String getFrameworkQueueName() {
		return frameworkQueueName;
	}

	public boolean isDump() {
		return dump;
	}

	public boolean isBrief() {
		return brief;
	}
}