package at.tuwien.ict.acona.cell.core.cellfunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFIncrementService;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.LoopController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SequenceController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SimpleControllerService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;

public class CellCooperationTester {
	private static Logger log = LoggerFactory.getLogger(CellCooperationTester.class);
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}

		launcher.stopSystem();

		// Runtime runtime = Runtime.instance();
		// runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * 
	 */
	@Test
	public void learnertest() {
		try {
			// Memory
			// Data: Date, CO2, Temperature, Energy Consumption, Comfort
			// [20161028, 500, 24.0, 334, 0.87]

			// Convert to Datastructure

			// Put data structure in the memory, one state for each
			// address=multiple datapoints

			// Address: newstate.20161028: Starttime, stoptime,
			// Address: newstate.20161028.1, newstate.20161028.2...

			// Functions needed: getSubdatapointsAsOrderedList

			// Learner
			// Funktion: Trigger on new data: Get new data as main datapoint
			// Process states:
			// 1. init: create an episode with unknown length
			// 2. init: set counter to the start
			// 3. Load one state from the buffer (counter),
			// 4. Set state as current state at address currentstate
			// 5. Evaluate the state in the agent
			// 6. Evaluate regarding drives parallel: start drive function, give
			// address of current state send start to function, wait for
			// function finished notify
			// 7. Evaluate regarding rules parallel: start rule function, give
			// address of current state, increase counter
			// 8. If both are finished, state is evaluated, then add the current
			// state to the episode and start with new state in 3.

			String COMMANDDATAPOINTNAME = "command";
			String STATUSDATAPOINTNAME = "status";
			String INCREMENTATIONDATAPOINTNAME = "increment";

			String controllerFunctionName = "controller";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			String agentName1 = "AgentIncrementService1";
			String agentName2 = "AgentIncrementService2";
			String agentName3 = "AgentIncrementService3";

			String ServiceName = "Increment"; // The same name for all services

			String controllerAgentName = "IncrementController";
			String memoryAgentName = "MemoryAgent";

			// values
			double startValue = 0;
			int expectedResult = 3;

			// Memory
			CellGatewayImpl memoryAgent = this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			// Controller
			CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1000").addManagedDatapoint(DatapointConfig
									.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, SyncMode.SUBSCRIBEONLY)));
			CellGatewayImpl controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(memoryAgentName, DatapointBuilder.newDatapoint("Test"));
			// controller.subscribeForeignDatapoint(processDatapoint,
			// memoryAgentName);

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service3 = this.launcher.createAgent(serviceAgent3);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			memoryAgent.getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			memoryAgent.getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME,
					ControlCommand.START.toString(), controllerFunctionName + ".state",
					new JsonPrimitive(ServiceState.FINISHED.toString()).getAsString(), 10000);

			// Write the numbers in the database agents
			// client1.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			// client2.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
			//
			// //Query the service with start and then get the status
			// //Set default timeout to a high number to be able to debug
			// controlAgent.getCommunicator().setDefaultTimeout(100000);
			// log.debug("Execute query");
			// Datapoint resultState =
			// controlAgent.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive(ControlCommand.START.toString())),
			// additionAgentName, Datapoint.newDatapoint(STATUSDATAPOINTNAME),
			// additionAgentName, 100000);
			// log.debug("Query executed with result={}", resultState);
			//
			// double sum = controlAgent.getCommunicator().read(resultdatapoint,
			// outputmemoryAgentName).getValue().getAsJsonPrimitive().getAsDouble();
			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// synchronized (this) {
			// try {
			// this.wait(6000);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1+1)),
			// "nothing");
			// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2+2)),
			// "nothing");

			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// Get the result from the result receiver agent
			// String result =
			// client2.getCommunicator().read(resultdatapoint).getValueAsString();
			double result = memoryAgent.getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");

			// launcher.stopSystem();
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data
	 * is read from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an
	 * output agent The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void aconaServiceWithFullControlReadDatapointsTest() {
		try {
			String COMMANDDATAPOINTNAME = "command";
			String STATUSDATAPOINTNAME = "status";
			String INCREMENTATIONDATAPOINTNAME = "increment";

			String controllerFunctionName = "controller";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			String agentName1 = "AgentIncrementService1";
			String agentName2 = "AgentIncrementService2";
			String agentName3 = "AgentIncrementService3";

			String ServiceName = "Increment"; // The same name for all services

			String controllerAgentName = "IncrementController";
			String memoryAgentName = "MemoryAgent";

			// values
			double startValue = 0;
			int expectedResult = 3;

			// Create all agents
			CellGateway controller = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1")
							.addManagedDatapoint(DatapointConfig
									.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, SyncMode.SUBSCRIBEONLY))));
			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			this.launcher.createAgent(CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));

			// Use a system config to init the whole system
//			SystemConfig totalConfig = SystemConfig.newConfig().addController()
//					.addMemory()
//					.addService()
//					.addService()
//					.addService()
//					.setTopController(controllerAgentName);
//
//			// this.launcher.createDebugUserInterface();
//
//			this.launcher.init(totalConfig);

			// // Memory
			// CellGatewayImpl memoryAgent =
			// this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			//
			// // Controller
			// CellConfig controllerAgentConfig =
			// CellConfig.newConfig(controllerAgentName)
			// .addCellfunction(CellFunctionConfig.newConfig(SequenceController.class)
			// .setProperty("agent1", agentName1).setProperty("agent2",
			// agentName2)
			// .setProperty("agent3", agentName3).setProperty("servicename",
			// ServiceName)
			// .setProperty("delay", "1").addSyncDatapoint(
			// DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
			// COMMANDDATAPOINTNAME, SyncMode.push)));
			// CellGatewayImpl controller =
			// this.launcher.createAgent(controllerAgentConfig);
			//
			// controller.getCommunicator().write(Datapoint.newDatapoint("Test"),
			// memoryAgentName);
			// // controller.subscribeForeignDatapoint(processDatapoint,
			// // memoryAgentName);
			//
			// // Create services
			// CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint,
			// memoryAgentName, SyncMode.pull));
			// CellGatewayImpl service1 =
			// this.launcher.createAgent(serviceAgent1);
			//
			// CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
			// processDatapoint,
			// memoryAgentName, SyncMode.pull)));
			// CellGatewayImpl service2 =
			// this.launcher.createAgent(serviceAgent2);
			//
			// CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
			// processDatapoint,
			// memoryAgentName, SyncMode.pull)));
			// CellGatewayImpl service3 =
			// this.launcher.createAgent(serviceAgent3);

			// synchronized (this) {
			// try {
			// this.wait(1000);
			// } catch (InterruptedException e) {
			//
			// }
			// }
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// CellGateway controller = launcher.getTopController();

			// Test the wrapper for controllers too
			// ControllerCellGateway controllerCellGateway = new
			// ControllerWrapper(controller);

			Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME,
					ControlCommand.START.toString(), controllerFunctionName + ".state",
					new JsonPrimitive(ServiceState.FINISHED.toString()).getAsString(), 100000);

			// controllerCellGateway.executeService("", "controllerservice", new
			// JsonObject(), 10000);

			log.debug("Received state={}", state);

			// Write the numbers in the database agents
			// client1.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			// client2.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
			//
			// //Query the service with start and then get the status
			// //Set default timeout to a high number to be able to debug
			// controlAgent.getCommunicator().setDefaultTimeout(100000);
			// log.debug("Execute query");
			// Datapoint resultState =
			// controlAgent.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive(ControlCommand.START.toString())),
			// additionAgentName, Datapoint.newDatapoint(STATUSDATAPOINTNAME),
			// additionAgentName, 100000);
			// log.debug("Query executed with result={}", resultState);
			//
			// double sum = controlAgent.getCommunicator().read(resultdatapoint,
			// outputmemoryAgentName).getValue().getAsJsonPrimitive().getAsDouble();
			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// synchronized (this) {
			// try {
			// this.wait(6000);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue()
					.getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");

			// launcher.stopSystem();
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data
	 * is read from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an
	 * output agent The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void AconaServiceStartsAconaService() {
		try {
			log.info("=== Test AconaServiceStartsAconaService ===");

			final String INCREMENTATIONDATAPOINTNAME = "increment";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			// === Agent names ===//
			String serviceAgentName = "IncrementServiceAgent";
			String controllerAgentName = "ControllerAgent";
			String memoryAgentName = "MemoryAgent";

			// === Function names ===//
			String serviceName = "IncrementService"; // The same name for all
														// services
			String controllerServiceName = "controllerservice";

			// === Values ===//
			double startValue = 0;
			int expectedResult = 1;

			// === Config ===//
			CellGateway topController = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, SimpleControllerService.class)
							.setProperty("agentname", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("delay", "10")));
			// SystemConfig totalConfig = SystemConfig.newConfig();
			// totalConfig.addController();
			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			this.launcher.createAgent(CellConfig.newConfig(serviceAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));
			// totalConfig.addMemory();
			// totalConfig.setTopController(controllerAgentName);

			// totalConfig.addService();

			// === System initialization ===//

			// this.launcher.createDebugUserInterface();

			// this.launcher.init(totalConfig);
			// CellGateway topController = launcher.getTopController();
			topController.getCommunicator().setDefaultTimeout(100000);
			// Set start values
			launcher.getAgent(memoryAgentName).getCommunicator().write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));

			// }
			// log.info("=== All agents initialized ===");
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== System initialized ===");
			// === System operation ===//

			Datapoint resultState = topController.getCommunicator().queryDatapoints(controllerServiceName + ".command",
					ControlCommand.START.toString(), controllerServiceName + ".state",
					new JsonPrimitive(ServiceState.FINISHED.toString()).getAsString(), 100000);

			log.info("=== System operation finished. Extract results ===");
			// === Extract results ===//
			log.debug("Received state={}", resultState);

			// Read from memory
			Datapoint memoryDatapoint = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint);
			double result = memoryDatapoint.getValue().getAsDouble();

			log.info("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * In this test, one controller will start 100 increment services in a sequence. The incrementservices increases the number in the memory with +1. At the end the number in the memory shall be the same
	 * as the number of services in the system.
	 * 
	 */
	@Test
	public void aconaServiceIncrementorCountTo100() {
		try {
			log.info("=== Test AconaServiceStartsAconaService ===");

			// === Agent names ===//
			String serviceAgentName = "IncrementServiceAgent";
			String controllerAgentName = "ControllerAgent";
			String memoryAgentName = "MemoryAgent";

			// === Function names ===//
			String controllerServiceName = "controllerservice";

			String serviceName = "IncrementService"; // The same name for all
														// services
			final String IncrementFunctionDatapointID = "increment";

			// === Datappointnames ===//
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			// === Values ===//
			int numberOfAgents = 100;

			// values
			double startValue = 0;
			int expectedResult = numberOfAgents;

			// === Config ===//
			// Create total config
			// SystemConfig totalConfig = SystemConfig.newConfig();

			// Add controller
			CellGateway controller = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, LoopController.class)
							.setProperty("agentnameprefix", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("numberofagents", String.valueOf(numberOfAgents)).setProperty("delay", "10")));
			// totalConfig.addController();

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Add memory
			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			// totalConfig.addMemory();
			// totalConfig.setTopController(controllerAgentName);

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Add services
			for (int i = 1; i <= numberOfAgents; i++) {
				this.launcher.createAgent(CellConfig.newConfig(serviceAgentName + i)
						.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
								.addManagedDatapoint(IncrementFunctionDatapointID, processDatapoint, memoryAgentName,
										SyncMode.READWRITEBACK)));
				synchronized (this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {

					}
				}
			}

			// this.launcher.createDebugUserInterface();

			// this.launcher.init(totalConfig);

			// }

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}
			// log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator().write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way. Start system");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// this.launcher.getAgent("AgentIncrementService1").getCommunicator().write(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()));

			// CellGateway controller = launcher.getTopController();

			// controller.getCommunicator().query(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()),
			// agentName + 1, Datapoint.newDatapoint("Increment.state"),
			// agentName + 1, 10000);

			// controller.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName
			// + ".command").setValue(ControlCommand.START.toString()),
			// Datapoint.newDatapoint(controllerServiceName + ".state"), 10000);

			// Test the wrapper for controllers too
			// ControllerCellGateway controllerCellGateway = new
			// ControllerWrapper(controller);
			ServiceState state = controller.getCommunicator().executeServiceBlocking(controllerServiceName);

			log.debug("Received state={}", state);

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
