package com.rodrigo.plugin.notification;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
		if("POST".equals(this.methodType)) {
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
		DefaultHttpClient httpClient = null;
		try {
			httpClient = new DefaultHttpClient();
			HttpResponse response = null;
			if("POST".equals(this.methodType)) {
				HttpPost postRequest = new HttpPost(this.url);
				postRequest.addHeader("content-type", this.contentType);
				postRequest.setEntity(new StringEntity(this.body));
				response = httpClient.execute(postRequest);
			}else if("GET".equals(this.methodType)) {
				HttpGet getRequest = new HttpGet(this.url);
				response = httpClient.execute(getRequest);
			}else {
				System.err.println("Unsupported method type : " + this.methodType);
				return false;
			}
			handleResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}finally {
			httpClient.getConnectionManager().shutdown();
		}
		return true;
	}

	/**
     * It handles the response, anything but status 200 is considered an error and it will as such
     * @param response , response from the requested url
     * @return true if ok 
     */
	private boolean handleResponse(HttpResponse response) {
		if(response != null && HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
			System.out.println("Notification succesfully sent");
			try {
				//This validation is due to the possibility that the response is actually empty, 
				//therefore there is no charset and the log of the body will not be attempted
				if(response.getEntity().getContentType() != null) {
					String charset = EntityUtils.getContentCharSet(response.getEntity());
					System.out.println("Response Body : " + convert(response.getEntity().getContent(), Charset.forName(charset)));
				}
			} catch (Exception e) {
				System.out.println("Response Body could not be logged, but the request itself was successful");
				e.printStackTrace();
			}
			return true;
		}else {
			System.err.println("Error sending notification with status code : " + response.getStatusLine().getStatusCode());
			return false;
		}
	}
	
	/**
     * Used to convert the body content to string
     * @param inputStream , stream to be converted to string for logging purposes
     * @param charset , charset
     * @return String body content
     */
	private String convert(InputStream inputStream, Charset charset) throws IOException {
		 
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		return stringBuilder.toString();
	}
	
}
