package com.rodrigo.plugin.notification.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rodrigo.plugin.notification.HttpNotificationPlugin;
	
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpNotificationPlugin.class)
@PowerMockIgnore("javax.net.ssl.*")
public class HttpNotificationPluginTest {

	@Test
	public void notificationTestGET() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null, true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertTrue(postNotification);
	}
	
	@Test
	public void notificationTestGETWithError() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null, true, true, 2));
		PowerMockito.doReturn(false).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestGETWithError2() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null, true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		PowerMockito.doReturn(false).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestPOST() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"{\"id\": 3,\"content\": \"Hello, World!\"}", "application/json", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertTrue(postNotification);
	}
	
	@Test
	public void notificationTestPOSTReplaceTrigger() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"{\"id\": 3,\"content\": \"Hello, World! ${trigger} \"}", "application/json", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertTrue(postNotification);
	}
	
	@Test
	public void notificationTestPOSTNoBody() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"", "application/json", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestGETNoUrl() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("GET", null, 
				"", "application/json", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestPOSTNoUrl() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("POST", null, 
				"", "application/json", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void validateContentType() throws Exception {
		HttpNotificationPlugin spyObject = PowerMockito.spy(new HttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"{\"id\": 3,\"content\": \"Hello, World!\"}", "application/jsonasdasd", true, true, 2));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(HttpNotificationPlugin.class, "buildRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	//You can uncomment this in case you would like to run a test to a remote notification service
	//So you can use this as an example :)
//	@Test
//	public void postNotificationTestGET() {
//		HttpNotificationPlugin plugin = new HttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null, true, true, 2);
//		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
//	}
//	
//	@Test
//	public void postNotificationTestPostXML() {
//		HttpNotificationPlugin plugin = new HttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
//				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
//				"<greeting>\r\n" + 
//				"   <content>Hello, World!</content>\r\n" + 
//				"   <id>3</id>\r\n" + 
//				"</greeting>", "application/xml", true);
//		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
//	}
	
}
