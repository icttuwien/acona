package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class SimpleControllerService extends CellFunctionThreadImpl {

	private static final Logger log = LoggerFactory.getLogger(SimpleControllerService.class);

	private int delay = 200;

	// execute service 1

	// private ServiceState executeServiceById(String serviceNameId, String
	// agentNameId, int timeout) throws Exception {
	// return executeService(this.getConfig().getProperty(serviceNameId),
	// this.getConfig().getProperty(agentNameId),
	// timeout);
	// }
	//
	// private ServiceState executeService(String serviceName, String agentName,
	// int timeout) throws Exception {
	// String commandDatapoint = serviceName + ".command";
	// String resultDatapoint = serviceName + ".state";
	// Datapoint result1 = this.getCommunicator().query(
	// Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()),
	// agentName,
	// Datapoint.newDatapoint(resultDatapoint), agentName, timeout);
	//
	// return ServiceState.valueOf(result1.getValueAsString());
	// }

	@Override
	protected void executeFunction() throws Exception {
		try {
			String serviceName = this.getFunctionConfig().getProperty("servicename");
			String agentName = this.getFunctionConfig().getProperty("agentname");

			log.info("Start simple controller service to execute one service {} at agent {}", serviceName, agentName);

			Datapoint result = this.getCommunicator().queryDatapoints(agentName, serviceName + ".command", new JsonPrimitive(ControlCommand.START.toString()), agentName, serviceName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 10000);
			log.debug("Service executed with the result={}", result);
		} catch (Exception e) {
			log.error("Cannot execute simple controller service", e);
			throw new Exception(e.getMessage());
		}

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void shutDownThreadExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.delay = Integer.valueOf(this.getFunctionConfig().getProperty("delay", String.valueOf(this.delay)));

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		this.setServiceState(ServiceState.FINISHED);
		//this.getCommunicator().write(Datapoints.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(this.getCurrentState().toString()));

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
