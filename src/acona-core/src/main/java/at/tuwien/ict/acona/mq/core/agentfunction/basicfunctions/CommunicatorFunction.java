package at.tuwien.ict.acona.mq.core.agentfunction.basicfunctions;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionImpl;

/**
 * The cell communicator function keeps a communicator, which is used in debug mode and unit tests to directly communicate with other cells and functions.
 * 
 * @author wendt
 *
 */
public class CommunicatorFunction extends AgentFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void agentFunctionInit() throws Exception {
		log.info("Initialized the cell communicator function");

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		log.debug("Closing cell communicator function");

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		log.warn("Nothing should be subscribed by this function, id {} and data {}", id, data);

	}

}
