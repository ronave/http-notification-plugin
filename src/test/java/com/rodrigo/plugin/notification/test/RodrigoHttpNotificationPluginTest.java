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

import com.rodrigo.plugin.notification.RodrigoHttpNotificationPlugin;
	
@RunWith(PowerMockRunner.class)
@PrepareForTest(RodrigoHttpNotificationPlugin.class)
@PowerMockIgnore("javax.net.ssl.*")
public class RodrigoHttpNotificationPluginTest {

	@Test
	public void notificationTestGET() throws Exception {
		RodrigoHttpNotificationPlugin spyObject = PowerMockito.spy(new RodrigoHttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "sendRequest")).withNoArguments();
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertTrue(postNotification);
	}
	
	@Test
	public void notificationTestGETWithError() throws Exception {
		RodrigoHttpNotificationPlugin spyObject = PowerMockito.spy(new RodrigoHttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null));
		PowerMockito.doReturn(false).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "sendRequest")).withNoArguments();
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestGETWithError2() throws Exception {
		RodrigoHttpNotificationPlugin spyObject = PowerMockito.spy(new RodrigoHttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "sendRequest")).withNoArguments();
		PowerMockito.doReturn(false).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "validateInputs")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	@Test
	public void notificationTestPOST() throws Exception {
		RodrigoHttpNotificationPlugin spyObject = PowerMockito.spy(new RodrigoHttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"{\"id\": 3,\"content\": \"Hello, World!\"}", "application/json"));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "sendRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertTrue(postNotification);
	}
	
	@Test
	public void notificationTestPOSTNoBody() throws Exception {
		RodrigoHttpNotificationPlugin spyObject = PowerMockito.spy(new RodrigoHttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"", "application/json"));
		PowerMockito.doReturn(true).when(spyObject, PowerMockito.method(RodrigoHttpNotificationPlugin.class, "sendRequest")).withNoArguments();
		boolean postNotification = spyObject.postNotification("success", new HashMap<String, String>(), new HashMap<String, String>());
		assertFalse(postNotification);
	}
	
	//You can uncomment this in case you would like to run a test to a remote notification service
	//So you can use this as an example :)
//	@Test
//	public void postNotificationTestGET() {
//		RodrigoHttpNotificationPlugin plugin = new RodrigoHttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null);
//		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
//	}
//	
//	@Test
//	public void postNotificationTestPostXML() {
//		RodrigoHttpNotificationPlugin plugin = new RodrigoHttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
//				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
//				"<greeting>\r\n" + 
//				"   <content>Hello, World!</content>\r\n" + 
//				"   <id>3</id>\r\n" + 
//				"</greeting>", "application/xml");
//		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
//	}
	
}
