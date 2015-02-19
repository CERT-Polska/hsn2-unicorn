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
