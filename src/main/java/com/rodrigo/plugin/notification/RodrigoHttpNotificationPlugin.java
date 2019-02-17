package com.rodrigo.plugin.notification;

import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.notification.NotificationPlugin;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * Developed by RoNaVe
 * Notification plugin that can handle POST ot GET request.
 * Body for POST request are only xml and json
 */
@Plugin(service=ServiceNameConstants.Notification, name="RodrigoNotificationPlugin")
@PluginDescription(title="Rodrigo Http Notification", description="Http notification plugin.")
public class RodrigoHttpNotificationPlugin implements NotificationPlugin {

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
     * Constructor for better junit.
     * @param methodType {"POST","GET"}
     * @param url Remote url where the request will be send
     * @param body To be used along with the POST method
     * @param contentType To be used along with the POST method
     */
	public RodrigoHttpNotificationPlugin(String methodType, String url, String body, String contentType) {
		this.methodType = methodType;
		this.url = url;
		this.body = body;
		this.contentType = contentType;
	}

	public RodrigoHttpNotificationPlugin() {
		
	}

	@SuppressWarnings("rawtypes")
	public boolean postNotification(String trigger, Map executionData, Map config) {
		System.out.println("Event trigger : " + trigger);
		System.out.println("Configuration : " + config);
		boolean isOk = true;
		if(isOk = validateInputs()) {
			isOk = sendRequest();
		}
		return isOk;
	}

	/**
     * It validates when sending a POST, body and contentType cannot be empty
     * @return true if ok 
     */
	private boolean validateInputs() {
		boolean isValid = true;
		if(this.methodType.equals(HttpMethod.POST.toString())) {
			if(this.body.trim().isEmpty() || this.contentType.trim().isEmpty()) {
				System.err.println("Unable to send notification for POST method, content type and body are mandatory");
				isValid = false;
			}
		}
		return isValid;
	}

	/**
     * Performs the request
     * @return true if ok 
     */
	private boolean sendRequest() {
		try {
			HttpResponse<JsonNode> response = null;
			if(this.methodType.equals(HttpMethod.POST.toString())) {
				response = Unirest.post(this.url).header("Content-Type", this.contentType).body(this.body).asJson();
			}else if(this.methodType.equals(HttpMethod.GET.toString())) {
				response = Unirest.get(url).asJson();
			}else {
				System.err.println("Unsupported method type : " + this.methodType);
				return false;
			}
			handleResponse(response);
		} catch (UnirestException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
     * It handles the response, anything but status 200 is considered an error and it will as such
     * @param response , response from the requested url
     * @return true if ok 
     */
	private boolean handleResponse(HttpResponse<JsonNode> response) {
		if(response != null && HttpStatus.SC_OK == response.getStatus()) {
			System.out.println("Notification succesfully sent");
			System.out.println("Response Body : " + response.getBody());
			return true;
		}else {
			System.err.println("Error sending notification : " + response.getBody());
			return false;
		}
	}
	
}
