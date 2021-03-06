package at.tuwien.ict.acona.launcher;

import at.tuwien.ict.acona.cell.core.CellGateway;

public interface SystemController {
	/**
	 * Interface between the external inputs to the system and the KORE system. Through the parameters, all user input is passed.
	 * 
	 * @param command
	 * @param parameter
	 */
	public void executeUserInput(String command, String parameter);

	/**
	 * Init with a system config
	 * 
	 * @param config
	 * @return
	 */
	// public SystemController init(SystemConfig config);

//	/**
//	 * Initialize the whole system from JSON config
//	 * 
//	 * @param config
//	 * @return
//	 * @throws Exception
//	 */
//	public SystemController init(JsonObject config) throws Exception;

//	/**
//	 * Initialize the whole system from a configuration file
//	 * 
//	 * @param filePath
//	 * @return
//	 */
//	public SystemController init(String filePath);

	/**
	 * Get the controller for an agent with a certain name
	 * 
	 * @param localName
	 * @return
	 */
	public CellGateway getAgent(String localName);

//	public CellGateway getControllerAgent(String localName);

//	/**
//	 * The system must use a top controller, in order to do anything at all. The topcontroller is either set in the config or extra through a method
//	 * 
//	 * @return
//	 */
//	public CellGateway getTopController();

//	/**
//	 * Set a new topcontroller
//	 * 
//	 * @param agentName
//	 */
//	public void setTopController(String agentName);

}
