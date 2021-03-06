package at.tuwien.ict.acona.mq.core.agentfunction;

import java.util.Map;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public interface AgentFunction {

	/**
	 * Initialize the cellfunction with the cell and a jsonobject with settings
	 * 
	 * @param settings:
	 *            Settings shall contain: functionname; subscriptions as a list of ID, Agent, datapointaddress and optional conditions; custom properties as json objects
	 * @param cell
	 * @return itself, in order to instantiate the cell and init at the same time
	 * @throws Exception
	 */
	public void init(FunctionConfig config, Cell cell) throws Exception;

	/**
	 * Return the name of the function, which has been specified in the config file
	 * 
	 * @return Name of the function
	 */
	public String getFunctionName();

	/**
	 * Get the name of the agent, which is holding the function
	 * 
	 * @return
	 */
	public String getAgentName();
	
	/**
	 * Get function root address
	 * 
	 * @return
	 */
	public String getFunctionRootAddress();

	/**
	 * Get the cell function configuration
	 * 
	 * @return
	 */
	public FunctionConfig getFunctionConfig();

//	/**
//	 * Get the defined type of function, e.g. base functions shall not be shown in the monitoring of an application, only codelets and threads.
//	 * 
//	 * @return
//	 */
//	public CellFunctionType getFunctionType();

	/**
	 * Use these datapoints for the activatorhandler
	 * 
	 * @return Subscriptions
	 */
	public Map<String, DatapointConfig> getSubscribedDatapoints();

	/**
	 * Perform an operation of this service. The actual method that is executed, is defined in the parameter data.
	 * 
	 * @param param
	 * @param caller
	 * @return
	 */
	public Response performOperation(String topic, Request param);

	/**
	 * Update subscribed data.
	 * 
	 * Warning: Do not put long blocking methods here because they may block another whole functions, which can cause the system to freeze. This function shall be primary used to provide data to the
	 * function.
	 * 
	 * @param datapoints,
	 *            which are subscribe
	 * @throws Exception
	 */
	public void updateSubscribedData(String topic, JsonElement data) throws Exception;

	/**
	 * Shut down function
	 */
	public void shutDownFunction();

	/**
	 * Get the current state of the function
	 *
	 * @return
	 */
	public ServiceState getCurrentState();

	public MqttCommunicator getCommunicator();

}
