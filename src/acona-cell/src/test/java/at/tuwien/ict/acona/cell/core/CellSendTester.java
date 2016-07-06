package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.config.ActivatorConfig;
import at.tuwien.ict.acona.cell.config.BehaviourConfig;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.helpers.CustomTestCell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.core.Communicator;
import at.tuwien.ict.acona.communicator.core.CommunicatorImpl;
import at.tuwien.ict.acona.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class CellSendTester {

	private static Logger log = LoggerFactory.getLogger(CellServiceTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new CommunicatorImpl();
			comm.init();
			
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
		
		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		this.comm.shutDown();
	}


//	@Test
//	public void sendMessageOrderingtest() {
//		try {
//			String readAddress = "storageagent.data.value";
//			String triggerAddress = "readeragent.data.command";
//			String resultAddress = "data.result";
//			int databaseValue = 12345;
//			int expectedValue = 12345;
//			
//			//Create config JSON for reader agent
//			CellConfig cellreader = CellConfig.newConfig("SenderAgent", CustomTestCell.class.getName());
//			cellreader.setClass(CustomTestCell.class);
//			cellreader.addProperty("targetcell", "TesterAgent");
//			
//			//Create agent in the system
//			Object[] args = new Object[1];
//			args[0] = cellreader.toJsonObject();
//			AgentController senderAgent = this.util.createAgent(cellreader.getName(), cellreader.getClassToInvoke(), args, agentContainer);
//			
//			log.debug("State={}", senderAgent.getState());
//			
//						
//			//Create config JSON for storage agent
//			CellConfig cellstorage = CellConfig.newConfig("TesterAgent", CustomTestCell.class.toString());
//			cellstorage.setClass(InspectorCell.class);
//			
//			//Create cell inspector controller for the subscriber
//			InspectorCellClient externalController = new InspectorCellClient();
//			Object[] argsPublisher = new Object[2];
//			argsPublisher[0] = cellstorage.toJsonObject();
//			argsPublisher[1] = externalController;
//			//Create agent in the system
//			AgentController agentController = this.util.createAgent(cellstorage.getName(), cellstorage.getClassToInvoke(), argsPublisher, agentContainer);
//			
//			log.debug("State={}", agentController.getState());		
//			
//			//Write databasevalue directly into the storage
//			externalController.getCell().getDataStorage().write(Datapoint.newDatapoint(readAddress).setValue(new JsonPrimitive(databaseValue)), null);
//			
//			//=== Start the system ===//
//			this.comm.subscribeDatapoint("ReaderAgent", readAddress);
//			
//			//Send Write command
//			Message writeMessage = Message.newMessage()
//					.addReceiver("ReaderAgent")
//					.setContent(Datapoint.newDatapoint(triggerAddress).setValue("START").toJsonObject())
//					.setService(AconaService.WRITE);
//			
//			Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage, 10000);
//			log.debug("Tester: Acknowledge of cell writing recieved={}", ack);
//			
//			//Subscribe the result
//			double actualResult = this.comm.getDatapointFromAgent(20000, true).getValue().getAsInt();
//			//actualResult = this.comm.getDatapointFromAgent(20000, false).getValue().getAsInt();
//			
//			//this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(msg.getSender().getLocalName()).setService(AconaService.READ).setContent(Datapoint.newDatapoint("test"))));
//			
//			log.debug("correct value={}, actual value={}", expectedValue, actualResult);
//			
//			assertEquals(true, true);
//			log.info("Test passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}

}
