package at.tuwien.ict.acona.mq.core.core;

import java.util.List;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;

/**
 * This class contains the instance references to all cell functions in a cell
 * 
 * @author wendt
 *
 */
public interface AgentFunctionHandler {

	/**
	 * Init the cell function handler with a cell.
	 * 
	 * @param caller
	 */
	public void init(Cell caller);

	/**
	 * Register an activator that is linked to datapoints through its activations
	 * 
	 * @param activatorInstance
	 * @throws Exception
	 */
	public void registerCellFunctionInstance(AgentFunction activatorInstance) throws Exception;

	/**
	 * Deregister an activator instance that is linked to datapoints through its activations
	 * 
	 * @param activatorInstanceName
	 * @throws Exception
	 */
	public void deregisterActivatorInstance(String activatorInstanceName) throws Exception;

	/**
	 * Get the actual cell function for a certain name
	 * 
	 * @param functionName
	 * @return
	 */
	public AgentFunction getCellFunction(String functionName);

	/**
	 * Get a list of all registered named
	 * 
	 * @return
	 */
	public List<String> getCellFunctionNames();

	/**
	 * Register a listener that gets activated every time a cell function has been registered or deregistered
	 * 
	 * @param listener
	 */
	public List<String> registerLister(AgentFunctionHandlerListener listener);

	// /**
	// *
	// *
	// * @param function
	// */
	// public void updateState(CellFunction function, ServiceState state);

	/**
	 * Unregister a listener for the cell function handler.
	 * 
	 * @param listener
	 */
	public void unregisterListener(AgentFunctionHandlerListener listener);

}
