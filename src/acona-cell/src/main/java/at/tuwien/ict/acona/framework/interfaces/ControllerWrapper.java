package at.tuwien.ict.acona.framework.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.ServiceState;

public class ControllerWrapper implements ControllerCellGateway {
	private static String COMMANDDATAPOINTNAME = "command";
	private static String STATEDATAPOINTNAME = "state";
	private static String DESCRIPTIONDATAPOINTNAME = "description";
	private static String PARAMETERDATAPOINTNAME = "parameter";
	private static String CONFIGDATAPOINTNAME = "config";

	private final static int DEFAULTTIMEOUT = 10000;

	private final Communicator communicator;

	private static Logger log = LoggerFactory.getLogger(ControllerWrapper.class);

	public ControllerWrapper(CellGateway gateway) {
		this.communicator = gateway.getCommunicator();
	}

	public ControllerWrapper(Communicator communicator) {
		this.communicator = communicator;
	}

	@Override
	public ServiceState executeService(String serviceName, JsonObject parameters) {
		return executeService("", serviceName, parameters, DEFAULTTIMEOUT);
	}

	@Override
	public ServiceState executeService(String service, JsonObject parameters, int timeout) {
		return executeService("", service, parameters, timeout);
	}

	@Override
	public ServiceState executeService(String agent, String service, JsonObject parameters) {
		return executeService(agent, service, parameters, DEFAULTTIMEOUT);
	}

	@Override
	public ServiceState executeService(String agentName, String serviceName, JsonObject parameters, int timeout) {
		String servicecommand = serviceName + "." + COMMANDDATAPOINTNAME;
		String serviceresult = serviceName + "." + STATEDATAPOINTNAME;

		ServiceState result = ServiceState.ERROR;
		try {
			Datapoint dp = this.communicator.queryDatapoints(Datapoint.newDatapoint(servicecommand).setValue(ControlCommand.START.toString()), agentName, Datapoint.newDatapoint(serviceresult),
					agentName, timeout);
			result = ServiceState.valueOf(dp.getValueAsString());
		} catch (Exception e) {
			log.error("Cannot execute query or the service state is erroneus", e);
		}

		return result;
	}

	@Override
	public ServiceState queryState(String service) {
		return null;
	}

	@Override
	public ServiceState queryState(String service, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String service, String key, String object) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProperty(String key, String object) {
		// TODO Auto-generated method stub

	}

}