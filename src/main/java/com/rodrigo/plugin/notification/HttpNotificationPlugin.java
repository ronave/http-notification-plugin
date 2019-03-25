package com.rodrigo.plugin.notification;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;

/**
 * Developed by RoNaVe
 * Notification plugin that can handle POST or GET request.
 * Body for POST request are only xml or json
 */
@Plugin(service=ServiceNameConstants.Notification, name="HttpNotificationPlugin")
@PluginDescription(title="Http Notification", description="Http notification plugin.")
public class HttpNotificationPlugin implements NotificationPlugin {

    /**
     * Method types currently supported, it doesnt have much sense to add another
     */
	@PluginProperty(name="method", title="Method Type", 
			description="Http method type to be used on the notification",
			defaultValue="POST", required=true)
	@SelectValues(values={"POST","GET"}, freeSelect=false)
	private String methodType;

	@PluginProperty(name="url", title="URL", description="URL for the notification", required=true)
	private String url;

	@PluginProperty(name="body",title="Body", description="Mandatory when using POST request, otherwise will just be ignored")
	@TextArea
	private String body;
	
	@PluginProperty(name="contentType",title="Content Type", description="Mandatory when using POST request, otherwise will just be ignored", required=true)
	@SelectValues(values={"application/json", "application/xml"}, freeSelect=false)
	private String contentType;
	
	/**
     * If not selected, will only log errors
     */
	@PluginProperty(name="debugFlag",title="Debug Flag", description="Check to obtain more details about the execution", defaultValue="false")
	@com.dtolabs.rundeck.plugins.descriptions.SelectValues(values={"true"}, dynamicValues=true)
	private boolean debugFlag;
	
	@PluginProperty(name="retry",title="Retry Flag", description="Check to retry in case of a conectivity issue", defaultValue="false")
	@com.dtolabs.rundeck.plugins.descriptions.SelectValues(values={"true"}, dynamicValues=true)
	private boolean retry;
	
	@PluginProperty(name="maximumRetries",title="Max Retries", description="Sets the maximum number of retries", defaultValue="1")
	private int maximumRetries;
	
	private static final List<String> VALID_CONTENT_TYPES = Arrays.asList("application/json", "application/xml");
	
	
	/**
     * Constructor for better junit.
     * @param methodType {"POST","GET"}
     * @param url Remote url where the request will be send
     * @param body To be used along with the POST method
     * @param contentType To be used along with the POST method
     */
	public HttpNotificationPlugin(String methodType, String url, String body, String contentType, boolean debugFlag, boolean retry, int maximumRetries) {
		this.methodType = methodType;
		this.url = url;
		this.body = body;
		this.contentType = contentType;
		this.debugFlag = debugFlag;
		this.retry = retry;
		this.maximumRetries = maximumRetries;
	}

	public HttpNotificationPlugin() {
		
	}

	@SuppressWarnings("rawtypes")
	public boolean postNotification(String trigger, Map executionData, Map config) {
		sendToPrint("Event trigger : " + trigger);
		sendToPrint("Configuration : " + config);
		if(this.body != null) {
			this.body = this.body.replace("${trigger}", trigger);
		}
		boolean isOk = true;
		if(isOk = validateInputs()) {
			isOk = buildRequest();
		}
		return isOk;
	}

	/**
     * It validates when sending a POST, body and contentType cannot be empty
     * @return true if ok 
     */
	private boolean validateInputs() {
		boolean isValid = true;
		if("POST".equals(this.methodType)) {
			if(this.body == null || this.body.trim().isEmpty() || this.contentType == null || this.contentType.trim().isEmpty()) {
				System.err.println("Unable to send notification for POST method, content type and body are mandatory");
				isValid = false;
			}else if(!VALID_CONTENT_TYPES.stream().anyMatch(content -> content.equals(this.contentType))) {
				System.err.println("Unable to send notification for POST method, unsupported content type");
				isValid = false;
			}
		}
		if(this.url == null || this.url.trim().isEmpty()) {
			System.err.println("Url must be set in order to perform the request");
			isValid = false;
		}
			
		return isValid;
	}

	/**
     * Build the request to be send
     * @return true if ok
     */
	private boolean buildRequest() {
		HttpResponse response = null;
		if("POST".equals(this.methodType)) {
			HttpPost postRequest = new HttpPost(this.url);
			postRequest.addHeader("content-type", this.contentType.trim());
			try {
				postRequest.setEntity(new StringEntity(this.body));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.err.println("An error has occur while trying to build the entity for the post request, using body string : " + this.body);
				return false;
			}
			executeRequest(postRequest, 1);
		}else if("GET".equals(this.methodType)) {
			HttpGet getRequest = new HttpGet(this.url);
			executeRequest(getRequest, 1);
		}else {
			System.err.println("Unsupported method type : " + this.methodType);
			return false;
		}
		handleResponse(response);
		return true;
	}
	
	/**
     * Performs the request and also handles the retry if is set that way
     * @return void
     */
	private void executeRequest(HttpRequestBase request, int maximumRetries) {
			try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
				sendToPrint("Attempt number : " + maximumRetries);
				HttpResponse response = httpClient.execute(request);
				handleResponse(response);
			} catch (IOException e) {
				System.err.println("Exception while trying to perform the request");
				e.printStackTrace();
				if(this.retry && maximumRetries <= this.maximumRetries) {
					sendToPrint("Executin retry");
					executeRequest(request, maximumRetries+1);
				}
			}
	}

	/**
     * It handles the response. Any response status equal or greater than 200 and lower than 300 it will be considered as successful
     * @param response , response from the requested url
     * @return true if ok 
     */
	private boolean handleResponse(HttpResponse response) {
		if(response != null && HttpStatus.SC_OK <= response.getStatusLine().getStatusCode()
				&& response.getStatusLine().getStatusCode() < HttpStatus.SC_MULTIPLE_CHOICES) {
			sendToPrint("Notification succesfully sent");
			try {
				//This validation is due to the possibility that the response is actually empty, 
				//therefore there is no charset and the log of the body will not be attempted
				if(response.getEntity() != null) {
					String responseAsString = EntityUtils.toString(response.getEntity());
					sendToPrint("Response Body : " + responseAsString);
				}else {
					sendToPrint("Response entity is null, even though the response is acceptable");
				}
			} catch (Exception e) {
				sendToPrint("Response Body could not be logged, but the request itself was successful");
				e.printStackTrace();
			}
			return true;
		}else {
			if(response != null && response.getStatusLine() != null) {
				System.err.println("Error sending notification with status code : " + response.getStatusLine().getStatusCode());
			}else {
				System.err.println("Error sending notification and response object is null, no more data is available to describe the error");
			}
			return false;
		}
	}
	
	private void sendToPrint(String message) {
		if(this.debugFlag) {
			System.out.println(message);
		}
	}
	
}
