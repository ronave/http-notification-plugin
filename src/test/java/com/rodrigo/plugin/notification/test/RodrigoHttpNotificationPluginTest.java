package com.rodrigo.plugin.notification.test;

import static org.junit.Assert.assertTrue;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import com.rodrigo.plugin.notification.RodrigoHttpNotificationPlugin;

public class RodrigoHttpNotificationPluginTest {

	protected RodrigoHttpNotificationPlugin plugin;
	
	@Test
	public void postNotificationTestPostJSON() {
		plugin = new RodrigoHttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"{\"id\": 3,\"content\": \"Hello, World!\"}", "application/json");
		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
	}
	
	@Test
	public void postNotificationTestPostXML() {
		plugin = new RodrigoHttpNotificationPlugin("POST", "http://localhost:8080/greeting", 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<greeting>\r\n" + 
				"   <content>Hello, World!</content>\r\n" + 
				"   <id>3</id>\r\n" + 
				"</greeting>", "application/xml");
		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
	}
	
	@Test
	public void postNotificationTestGET() {
		plugin = new RodrigoHttpNotificationPlugin("GET", "http://localhost:8080/greeting", null, null);
		assertTrue(plugin.postNotification(new String(), new HashedMap(), new HashedMap()));
	}
}
