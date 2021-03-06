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
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFDataStorageUpdate;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFAdditionCustomServiceSimple;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFDurationThreadTester;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;

public class CellExecutorWithCellTester {
	private static Logger log = LoggerFactory.getLogger(CellExecutorWithCellTester.class);
	// private final JadeContainerUtil util = new JadeContainerUtil();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();
	// private Gateway comm = commUtil.getJadeGateway();

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

		this.launcher.stopSystem();

		// Runtime runtime = Runtime.instance();
		// runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {

			}
		}
		// this.commUtil.shutDownJadeGateway();
	}

	/**
	 *
	 * 
	 * 
	 */
	@Test
	public void threadExecutorInCellTester() {
		try {
			String queryDatapoint = CFDurationThreadTester.queryDatapointID;
			String executeonceDatapoint = "datapoint.executeonce";
			String resultDatapointAddress = "datapoint.result";
			String testAgentName = "testagent";

			String expectedResult = "FINISHED";

			// Create Database agents 1-2
			CellConfig testagent = CellConfig.newConfig(testAgentName, CellImpl.class)
					.addCellfunction(CellFunctionConfig.newConfig("testExecutor", CFDurationThreadTester.class)
							.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.commandDatapointID,
									CFDurationThreadTester.commandDatapointID, SyncMode.SUBSCRIBEONLY))
							.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.queryDatapointID,
									queryDatapoint, SyncMode.SUBSCRIBEONLY))
							.addManagedDatapoint(
									DatapointConfig.newConfig(CFDurationThreadTester.executeonceDatapointID,
											executeonceDatapoint, SyncMode.SUBSCRIBEONLY))
							.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.resultDatapointID,
									resultDatapointAddress, SyncMode.WRITEONLY)));
			CellGatewayImpl testAgent = this.launcher.createAgent(testagent);

			testAgent.getCommunicator().setDefaultTimeout(100000);

			// Create inspector or the new gateway
			CellGatewayImpl cellControlSubscriber = this.launcher
					.createAgent(CellConfig.newConfig("subscriber", CellImpl.class)
							.addCellfunction(CellFunctionConfig.newConfig("updater", CFDataStorageUpdate.class)
									.addManagedDatapoint(resultDatapointAddress, resultDatapointAddress, testAgentName,
											SyncMode.SUBSCRIBEONLY)));
			cellControlSubscriber.getCommunicator().setDefaultTimeout(100000);

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Test initialized ===//
			log.info("=== Test initialized ===");

			// Write the numbers in the database agents

			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("testagent").setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			// cellControlSubscriber.subscribeForeignDatapoint(resultDatapointAddress,
			// "testagent");
			cellControlSubscriber.getCommunicator().write("testagent",
					DatapointBuilder.newDatapoint(queryDatapoint).setValue("SELECT * FILESERVER"));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			String result = cellControlSubscriber.getCommunicator().read(resultDatapointAddress).getValueAsString();

			log.debug("correct value={}, actual value={}", "FINISHED", result);

			assertEquals(expectedResult, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	// /**
	// * Execute a cell function as a blocking class. It will block the cell as
	// * updateDatapoint is executed and blocked by the function
	// *
	// *
	// */
	// @Test
	// public void blockingExecutorInCellTester() {
	// try {
	// String commandDatapoint = "datapoint.command";
	// String queryDatapoint = "datapoint.query";
	// String executeonceDatapoint = "datapoint.executeonce";
	// String resultDatapoint = "datapoint.result";
	//
	// String expectedResult = "FINISHED";
	//
	// // Create Database agents 1-2
	// CellConfig testagent = CellConfig.newConfig("testagent", CellImpl.class)
	// .addCellfunction(CellFunctionConfig.newConfig("testExecutor",
	// CFDurationBlockingTester.class)
	// .addManagedDatapoint(DatapointConfig.newConfig("command", commandDatapoint,
	// SyncMode.SUBSCRIBEONLY))
	// .addManagedDatapoint(DatapointConfig.newConfig("query", queryDatapoint,
	// SyncMode.SUBSCRIBEONLY))
	// .addManagedDatapoint(
	// DatapointConfig.newConfig("executeonce", executeonceDatapoint,
	// SyncMode.SUBSCRIBEONLY))
	// .setProperty("result", resultDatapoint));
	// CellGatewayImpl testAgent = this.launcher.createAgent(testagent);
	//
	// testAgent.getCommunicator().setDefaultTimeout(100000);
	//
	// // Create inspector or the new gateway
	// CellGatewayImpl cellControlSubscriber = this.launcher
	// .createAgent(CellConfig.newConfig("subscriber", CellImpl.class)
	// .addCellfunction(CellFunctionConfig.newConfig("updater",
	// CFDataStorageUpdate.class)
	// .addManagedDatapoint(resultDatapoint, resultDatapoint, "testagent",
	// SyncMode.SUBSCRIBEONLY)));
	// cellControlSubscriber.getCommunicator().setDefaultTimeout(100000);
	//
	// // Write the numbers in the database agents
	//
	// //
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("testagent").setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
	// // JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
	// //cellControlSubscriber.subscribeForeignDatapoint("datapoint.result",
	// "testagent");
	// synchronized (this) {
	// try {
	// this.wait(200);
	// } catch (InterruptedException e) {
	//
	// }
	// }
	// // Write is blocking the communicator until an answer is received
	// cellControlSubscriber.getCommunicator()
	// .write(Datapoint.newDatapoint(queryDatapoint).setValue("SELECT *
	// FILESERVER"), "testagent");
	//
	// // synchronized (this) {
	// // try {
	// // this.wait(2000);
	// // } catch (InterruptedException e) {
	// //
	// // }
	// // }
	// synchronized (this) {
	// try {
	// this.wait(2000);
	// } catch (InterruptedException e) {
	//
	// }
	// }
	//
	// String result =
	// cellControlSubscriber.readLocalDatapoint(resultDatapoint).getValueAsString();
	//
	// log.debug("correct value={}, actual value={}", "FINISHED", result);
	//
	// assertEquals(result, expectedResult);
	// log.info("Test passed");
	// } catch (Exception e) {
	// log.error("Error testing system", e);
	// fail("Error");
	// }
	//
	// }

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data
	 * is read from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an
	 * output agent The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void externalControllerWithDatabaseCellsAndAdditionCellTest() {
		try {
			String COMMANDDATAPOINTNAME = "command";
			String STATUSDATAPOINTNAME = "status";
			String OPERAND1 = "operand1";
			String OPERAND2 = "operand2";
			String RESULT = "result";

			// define all datapoints that shall be used
			String memorydatapoint1 = "inputmemory.variable1"; // put into
																// memory mock
																// agent
			String memorydatapoint2 = "inputmemory.variable2"; // put into
																// memory mock
																// agent

			// drivetrack data
			String commandDatapoint = "drivetrack.controller.command";
			String statedatapoint = "drivetrack.controller.mode";
			String executeinterval = "drivetrack.controller.executioninterval";

			// Output memory agent
			String resultdatapoint = "outputmemory.result";

			// Define results
			int value1 = 12;
			int value2 = 13;
			int expectedResult = 25;

			// Define agent names and info
			String inputMemoryAgentName1 = "InputBufferAgent1";
			String inputMemoryAgentName2 = "InputBufferAgent2";
			String outputmemoryAgentName = "OutputBufferAgent";
			String additionAgentName = "AdditionAgent";
			String controllerAgentName = "controller";

			// Create Database agents 1 and 2
			CellConfig inputMemoryAgent1 = CellConfig.newConfig(inputMemoryAgentName1);
			CellGatewayImpl client1 = this.launcher.createAgent(inputMemoryAgent1);
			CellConfig inputMemoryAgent2 = CellConfig.newConfig(inputMemoryAgentName2);
			CellGatewayImpl client2 = this.launcher.createAgent(inputMemoryAgent2);

			// Create resultagent
			CellConfig outputMemoryAgent = CellConfig.newConfig(outputmemoryAgentName);
			CellGatewayImpl outputagent = this.launcher.createAgent(outputMemoryAgent);

			// Create the addition agent with the addition function that reads
			// from 2 different datapoints at 2 agents, calculates and then puts
			// the values in a 3rd output agent
			CellConfig additionAgent = CellConfig.newConfig(additionAgentName)
					.addCellfunction(
							CellFunctionConfig.newConfig(CFAdditionCustomServiceSimple.class)
									.addManagedDatapoint(DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
											commandDatapoint, SyncMode.SUBSCRIBEONLY))
									.setProperty(STATUSDATAPOINTNAME, statedatapoint)
									.setProperty(OPERAND1,
											DatapointConfig.newConfig(OPERAND1, memorydatapoint1, inputMemoryAgentName1,
													SyncMode.READONLY).toJsonObject())
									.setProperty(OPERAND2,
											DatapointConfig.newConfig(OPERAND2, memorydatapoint2, inputMemoryAgentName2,
													SyncMode.READONLY).toJsonObject())
									.setProperty(RESULT, DatapointConfig.newConfig(RESULT, resultdatapoint,
											outputmemoryAgentName, SyncMode.READONLY).toJsonObject()));
			this.launcher.createAgent(additionAgent);

			// Control agent
			CellConfig controller = CellConfig.newConfig(controllerAgentName);
			CellGatewayImpl controlAgent = this.launcher.createAgent(controller);

			log.info("=== All agents initialized ===");
			// Write the numbers in the database agents
			client1.getCommunicator()
					.write(DatapointBuilder.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			client2.getCommunicator()
					.write(DatapointBuilder.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));

			// Query the service with start and then get the status
			// Set default timeout to a high number to be able to debug
			controlAgent.getCommunicator().setDefaultTimeout(100000);
			log.debug("Execute query");
			Datapoint resultState = controlAgent.getCommunicator().queryDatapoints(additionAgentName, commandDatapoint,
					new JsonPrimitive(ControlCommand.START.toString()), additionAgentName, STATUSDATAPOINTNAME,
					new JsonPrimitive(ServiceState.FINISHED.toString()), 1000);
			// Datapoint resultState = CFQuery.newQuery(additionAgentName, commandDatapoint,
			// new JsonPrimitive(ControlCommand.START.toString()), additionAgentName,
			// STATUSDATAPOINTNAME, 10000, controlAgent.getCell());
			// Datapoint resultState = controlAgent.getCommunicator()
			// .query(Datapoint.newDatapoint(commandDatapoint)
			// .setValue(new JsonPrimitive(ControlCommand.START.toString())),
			// additionAgentName,
			// Datapoint.newDatapoint(STATUSDATAPOINTNAME), additionAgentName, 1000);

			log.debug("Query executed with result={}", resultState);

			Datapoint dpsum = controlAgent.getCommunicator().read(outputmemoryAgentName, resultdatapoint);
			log.debug("Result={}", dpsum);
			double sum = controlAgent.getCommunicator().read(outputmemoryAgentName, resultdatapoint).getValue()
					.getAsJsonPrimitive().getAsDouble();
			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

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

			log.debug("correct value={}, actual value={}", expectedResult, sum);

			assertEquals(sum, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	// // @Test
	// public void
	// externalControllerWithDatabaseCellsAndAdditionCellblockingFunctionTest() {
	// try {
	// String COMMANDDATAPOINTNAME = "command";
	// String STATUSDATAPOINTNAME = "status";
	// String OPERAND1 = "operand1";
	// String OPERAND2 = "operand2";
	// String RESULT = "result";
	//
	// // define all datapoints that shall be used
	// String memorydatapoint1 = "inputmemory.variable1"; // put into
	// // memory mock
	// // agent
	// String memorydatapoint2 = "inputmemory.variable2"; // put into
	// // memory mock
	// // agent
	//
	// // drivetrack data
	// String commandDatapoint = "drivetrack.controller.command";
	// String statedatapoint = "drivetrack.controller.mode";
	// String executeinterval = "drivetrack.controller.executioninterval";
	//
	// // Output memory agent
	// String resultdatapoint = "outputmemory.result";
	//
	// // Define results
	// int value1 = 12;
	// int value2 = 13;
	// int expectedResult = 25;
	//
	// // Define agent names and info
	// String inputMemoryAgentName1 = "InputBufferAgent1";
	// String inputMemoryAgentName2 = "InputBufferAgent2";
	// String outputmemoryAgentName = "OutputBufferAgent";
	// String additionAgentName = "AdditionAgent";
	// String controllerAgentName = "controller";
	//
	// // Create Database agents 1 and 2
	// CellConfig inputMemoryAgent1 = CellConfig.newConfig(inputMemoryAgentName1);
	// CellGatewayImpl client1 = this.launcher.createAgent(inputMemoryAgent1);
	// CellConfig inputMemoryAgent2 = CellConfig.newConfig(inputMemoryAgentName2);
	// CellGatewayImpl client2 = this.launcher.createAgent(inputMemoryAgent2);
	//
	// // Create resultagent
	// CellConfig outputMemoryAgent = CellConfig.newConfig(outputmemoryAgentName);
	// CellGatewayImpl outputagent = this.launcher.createAgent(outputMemoryAgent);
	//
	// // Create the addition agent with the addition function that reads
	// // from 2 different datapoints at 2 agents, calculates and then puts
	// // the values in a 3rd output agent
	// CellConfig additionAgent = CellConfig.newConfig(additionAgentName)
	// .addCellfunction(
	// CellFunctionConfig.newConfig(CFAdditionServiceBlockingSimple.class)
	// .addManagedDatapoint(DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
	// commandDatapoint,
	// SyncMode.SUBSCRIBEONLY))
	// .setProperty(STATUSDATAPOINTNAME, statedatapoint)
	// .setProperty(OPERAND1,
	// DatapointConfig.newConfig(OPERAND1, memorydatapoint1, inputMemoryAgentName1,
	// SyncMode.READONLY).toJsonObject())
	// .setProperty(OPERAND2,
	// DatapointConfig.newConfig(OPERAND2, memorydatapoint2, inputMemoryAgentName2,
	// SyncMode.READONLY).toJsonObject())
	// .setProperty(RESULT, DatapointConfig
	// .newConfig(RESULT, resultdatapoint, outputmemoryAgentName, SyncMode.READONLY)
	// .toJsonObject()));
	// this.launcher.createAgent(additionAgent);
	//
	// // Control agent
	// CellConfig controller = CellConfig.newConfig(controllerAgentName);
	// CellGatewayImpl controlAgent = this.launcher.createAgent(controller);
	//
	// log.info("=== All agents initialized ===");
	// // Write the numbers in the database agents
	// client1.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
	// client2.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
	//
	// // Query the service with start and then get the status
	// // Set default timeout to a high number to be able to debug
	// controlAgent.getCommunicator().setDefaultTimeout(100000);
	// log.debug("Execute query");
	// Datapoint resultState =
	// controlAgent.getCommunicator().queryDatapoints(commandDatapoint, new
	// JsonPrimitive(ControlCommand.START.toString()), additionAgentName,
	// STATUSDATAPOINTNAME, additionAgentName, 100000);
	// log.debug("Query executed with result={}", resultState);
	//
	// double sum = controlAgent.getCommunicator().read(resultdatapoint,
	// outputmemoryAgentName).getValue()
	// .getAsJsonPrimitive().getAsDouble();
	// //
	// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
	// // JsonPrimitive("START")), drivetrackAgentName);
	// //
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
	// // JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
	//
	// //
	// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1+1)),
	// // "nothing");
	// //
	// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2+2)),
	// // "nothing");
	//
	// //
	// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
	// // JsonPrimitive("START")), drivetrackAgentName);
	// //
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
	// // JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
	//
	// // Get the result from the result receiver agent
	// // String result =
	// // client2.getCommunicator().read(resultdatapoint).getValueAsString();
	//
	// log.debug("correct value={}, actual value={}", expectedResult, sum);
	//
	// assertEquals(sum, expectedResult, 0.0);
	// log.info("Test passed");
	// } catch (Exception e) {
	// log.error("Error testing system", e);
	// fail("Error");
	// }
	//
	// }

}
