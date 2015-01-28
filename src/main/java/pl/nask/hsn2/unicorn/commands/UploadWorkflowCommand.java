package pl.nask.hsn2.unicorn.commands;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import pl.nask.hsn2.protobuff.Workflows.WorkflowUploadRequest;
import pl.nask.hsn2.unicorn.connector.ConnectionException;

public class UploadWorkflowCommand extends BasicRPCCommand {
	private final static String REQUEST_TYPE = "WorkflowUploadRequest";
	private String workflowFileName;


	public UploadWorkflowCommand(String queueName, String workflowFileName)
			throws ConnectionException {
		super(REQUEST_TYPE, queueName);
		this.workflowFileName = workflowFileName;
	}

	@Override
	protected void buildMessage() {
		this.message = WorkflowUploadRequest.newBuilder().setName(workflowFileName).setContent(workflowContent()).setOverwrite(true).build().toByteArray();
	}

	private String workflowContent() {
		try {
			List<String> lines = Files.readAllLines(Paths.get(workflowFileName), StandardCharsets.UTF_8);
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
}
