package pl.nask.hsn2.unicorn.commands.framework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.cli.CommandLine;

import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadRequest;
import pl.nask.hsn2.unicorn.CommandLineParams;
import pl.nask.hsn2.unicorn.commands.AbstractCommandBuilder;
import pl.nask.hsn2.unicorn.commands.BasicRPCCommand;
import pl.nask.hsn2.unicorn.commands.Command;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class UploadWorkflowCommand extends BasicRPCCommand {
	private final static String REQUEST_TYPE = "WorkflowUploadRequest";
	private String workflowFilePath;


	public UploadWorkflowCommand(String queueName, String workflowFilePath)
			throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.workflowFilePath = workflowFilePath;
	}

	@Override
	protected void buildMessage() {
		String workflowName = Paths.get(workflowFilePath).getFileName().toString();
		this.message = WorkflowUploadRequest.newBuilder().setName(workflowName).setContent(workflowContent()).setOverwrite(true).build().toByteArray();
	}

	private String workflowContent() {
		try {
			List<String> lines = Files.readAllLines(Paths.get(workflowFilePath), StandardCharsets.UTF_8);
			StringBuilder builder = new StringBuilder();
			for (String line: lines) {
				builder.append(line);
				builder.append("\n");
			}
			return builder.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Error reading workflow file: " + e.getMessage());
		}
	}
	
	public static class Builder extends AbstractCommandBuilder {
		@Override
		protected Command buildCommand(CommandLineParams cmdParams,
				CommandLine cmd) throws ConnectionException {
			String workflowFileName = cmd.getOptionValue("uw");
			return new UploadWorkflowCommand(cmdParams.getFrameworkQueueName(), workflowFileName);
		}
	}
}
