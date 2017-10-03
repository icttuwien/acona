package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.demowebservice.cellfunctions.weather.Weather;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;

/**
 * This is a class that reads the weather from the internet and presents it as datapoints on a certain address
 * 
 * @author wendt
 *
 */
public class WeatherService extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(WeatherService.class);
	
	public final static String CITYNAME = "cityname";
	public final static String USERID = "userid";
	
	//Address to publish on
	public final static String WEATHERADDRESSID = "weatheraddress";
	
	private String cityName="";
	private String userid="";
	
	//private static final String REST_URI = "http://api.openweathermap.org/data/2.5/weather?q=vienna&APPID=5bac1f7f2b67f3fb3452350c23401903";
	private static final String REST_URI = "http://api.openweathermap.org/data/2.5/weather?";
	private Client client = ClientBuilder.newClient();

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(false);
		this.setExecuteRate(5000);
		
		//WebTarget webTarget = client.target("http://localhost:8082/spring-jersey");
		//WebTarget employeeWebTarget  = webTarget.path("resources/employees");
		//Invocation.Builder invocationBuilder = employeeWebTarget.request(MediaType.APPLICATION_JSON);
		
		//Call: http://api.openweathermap.org/data/2.5/weather?q=vienna&APPID=5bac1f7f2b67f3fb3452350c23401903
		
		this.cityName = this.getFunctionConfig().getProperty(CITYNAME);
		this.userid = this.getFunctionConfig().getProperty(USERID);
		
		log.info("Weather service initialized");
	}
	
	private Response createJsonRequest() {
	    return client
	      .target(REST_URI).queryParam("q", this.cityName).queryParam("APPID" , this.userid)  
	      .request(MediaType.APPLICATION_JSON)
	      .get(); //post(Entity.entity(emp, MediaType.APPLICATION_JSON));
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		
		return null;
	}

	@Override
	protected void executeFunction() throws Exception {
		
		Response resp = this.createJsonRequest();
		//WebTarget webTarget = client.target(REST_URI);
		//Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
		//Response resp = invocationBuilder.get();
		
		
		if (resp.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + resp.getStatus());
		}
		
		log.info("Received weather data={}", resp);
		
		//Weather w = resp.readEntity(Weather.class);
		//double lat = w.coord.lat;
		
		//log.info("Received Json={}", resp.readEntity(String.class));
		String s = resp.readEntity(String.class);
		//Weather weather = resp.readEntity(Weather.class);
		
		Weather object = (new Gson()).fromJson(s, Weather.class);
		log.info("Got json= {}", object);
		
		Chunk result = Chunk.newChunk(this.getFunctionName() + "_result", "WeatherData")
				.setValue("City", object.name)
				.setValue("Temperature", object.main.temp-273.15);
		
		this.getValueMap().get(WEATHERADDRESSID).setValue(result.toJsonObject());
		log.debug("Written value={}", result);
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
