package at.tuwien.ict.acona.evolutiondemo.launcher;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Evaluator;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.traderagent.EMAIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.evolutiondemo.webserver.EvolutionService;
import at.tuwien.ict.acona.evolutiondemo.webserver.JerseyRestServer;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

/**
 * This class manages the launching of the whole cognitive system
 * 
 * @author wendt
 *
 */
public class Launcher {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();
	
	private static Launcher launcher;

	public static void main(String[] args) {
		log.info("Welcome to the ACONA Stock Market Evolution Demonstrator");

		launcher = new Launcher();
		try {
			launcher.init();
		} catch (Exception e) {
			log.error("System initialization failed. Quit", e);
			System.exit(-1);
		}

	}

	private void init() throws Exception {
		try {
			// === General variables ===//
			String stockName = "Fingerprint";

			// === Controller ===//
			String controllerAgentName = "ControllerAgent";
			String controllerService = "controllerservice";

			// === Stock Market agent ===//
			String stockmarketAgentName = "StockMarketAgent";
			String stockmarketServiceName = "StockMarketService";

			// === Broker ===//
			String brokerAgentName = "BrokerAgent";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";

			String statisticsDatapointName = brokerAgentName + ":" + "stats";

			// === Traders ===//
			String traderAgentName = "TraderAgent";
			String signalService = "signal";
			
			// === Server === //
			String serverAgentName = "Server";
			

			// === Controller agent implementation === //
			Cell controllerAgent = this.controller.createAgent(AgentConfig.newConfig(controllerAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addFunction(FunctionConfig.newConfig(controllerService, CodeletHandlerImpl.class))
					// The codelet handler ist controller request receiver funtion
					.addFunction(FunctionConfig.newConfig("controller", ConsoleRequestReceiver.class)
							.setProperty(ConsoleRequestReceiver.ATTRIBUTECONTROLLERSERVICE, controllerService)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Broker ===//

			Cell brokerAgent = this.controller.createAgent(AgentConfig.newConfig(brokerAgentName)
					.addFunction(FunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName)
							.setProperty(Broker.PARAMPRICESOURCE, stockmarketAgentName + ":" + "data"))
					.addFunction(FunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, stockmarketAgentName + ":" + "data"))
					.addFunction(FunctionConfig.newConfig("EvaluatorService", Evaluator.class)
							.setProperty(Evaluator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Evaluator.ATTRIBUTEEXECUTIONORDER, 2)
							.setProperty(Evaluator.STATISTICSCOLLECTORSERVICENAME, brokerAgentName + ":" + statisticsService + "/" + StatisticsCollector.GETSTATISTICSSUFFIX)
							.setProperty(Evaluator.STATISTICSDATAPOINTNAME, statisticsDatapointName))
					.addFunction(FunctionConfig.newConfig("TypesGraph", DepotStaticticsGraphToolFunction.class)
							.addManagedDatapoint(statisticsDatapointName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Stock market ===//

			Cell stockMarketAgent = this.controller.createAgent(AgentConfig.newConfig(stockmarketAgentName)
					.addFunction(FunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0) 		// First, the stock market generates a price, run order 0
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)					//1=constant, 0=sin
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(FunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data); // Puts data on datapoint StockMarketAgent:data

			// === Traders ===//
			List<String> l = new ArrayList<String>();
			
			//Generate 50 agents that don't replicate or mutate but with different MA values
			for (int i = 1; i <= 50; i++) {
				
				int longMA = 0;
				int shortMA = 0;
				boolean breaker = true;
				String key = "LS";
				do {
					longMA = (int)(Math.random()*100);
					shortMA = (int)(Math.random()*longMA);
					key = "L" + longMA + "S" + shortMA;
					if (l.contains(key)==true) {
						log.info("L {}, S {} already exists", longMA, shortMA);
						breaker=false;
					} else {
						breaker=true;
						log.info("Added agent, L {}, S {}", longMA, shortMA);
						break;
					}
				} while (breaker==false);
				
				l.add(key);
				String traderType = key;
	
				Cell traderAgent = this.controller.createAgent(AgentConfig.newConfig(traderAgentName + "_" + traderType)
						.addFunction(FunctionConfig.newConfig("trader_" + traderType, Trader.class)
								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName)
								.setProperty(Trader.ATTRIBUTEMULTIPLY, false)
								.setProperty(Trader.ATTRIBUTEMUTATE, false))
						//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
						.addFunction(FunctionConfig.newConfig(signalService, EMAIndicator.class)
								.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(EMAIndicator.ATTRIBUTEEMALONG, longMA)
								.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, shortMA)));
			}
			
			//Jsersey server to receive commands
			AgentConfig server = AgentConfig.newConfig(serverAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addFunction(FunctionConfig.newConfig("jerseyserver", JerseyRestServer.class)
							.setProperty(EvolutionService.PARAMAGENTNAMES, controllerAgent.getName())
							.setProperty(EvolutionService.PARAMCONTROLLERADDRESS, controllerAgent + ":" + controllerService));

			//Cell serverCell = this.controller.createAgent(server);
					
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			
			JsonElement value = controllerAgent.getCommunicator().read(controllerAgent.getName() + ":" + controllerService + "/" + CodeletHandlerImpl.EXTENDEDSTATESUFFIX).getValue();
			log.info("Registered codelets: {}", value);
			
			log.info("=== All agents initialized ===");
			
			for (int i = 1; i <= 100000; i++) {
				log.info("run {}/{}", i, 100000);
				// Execute the codelet handler
				controllerAgent.getCommunicator().execute(controllerAgent.getName() + ":" + controllerService + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 200000);
			}

		} catch (Exception e) {
			log.error("Cannot initialize the system", e);
			throw new Exception(e.getMessage());
		}

	}

}
