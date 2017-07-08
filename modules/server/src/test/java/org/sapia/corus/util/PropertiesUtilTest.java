package org.sapia.corus.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class PropertiesUtilTest {
	
	private Properties props;
	
	@Before
	public void setUp() {
		props = new Properties();
		props.setProperty("set1.prop1", "prop1");
		props.setProperty("set1.prop2", "prop2");
		props.setProperty("set2.prop1", "prop3");
		props.setProperty("set2.prop2", "prop4");		
	}

	@Test
	public void testFilter() {
		Properties filtered = PropertiesUtil.filter(props, PropertiesFilter.NamePrefixPropertiesFilter.createInstance("set1"));
		assertEquals("prop1", filtered.getProperty("set1.prop1"));
		assertEquals("prop2", filtered.getProperty("set1.prop2"));
		assertNull(filtered.getProperty("set2.prop1"));
		assertNull(filtered.getProperty("set2.prop1"));
	}

	@Test
	public void testCopy() {
		Properties copy = new Properties();
		PropertiesUtil.copy(props, copy);
		assertEquals("prop1", copy.getProperty("set1.prop1"));
		assertEquals("prop2", copy.getProperty("set1.prop2"));		
		assertEquals("prop3", copy.getProperty("set2.prop1"));
		assertEquals("prop4", copy.getProperty("set2.prop2"));		
	}

	@Test
	public void testTransform() {
		PropertiesUtil.transform(props, 
				PropertiesTransformer.MappedPropertiesTransformer.createInstance()
				.add("set1.prop1", "seta.prop1")
				.add("set1.prop2", "seta.prop2")
		);
		assertEquals("prop1", props.getProperty("seta.prop1"));
		assertEquals("prop2", props.getProperty("seta.prop2"));		
		
	}
	
	@Test
	public void testLoad() throws IOException {
		Properties toLoad = new Properties();
		PropertiesUtil.load(toLoad, new File(System.getProperty("user.dir") + File.separator + "etc/test/PropertiesUtilTest.props"));
		assertEquals(toLoad.getProperty("prop1"), "value1");
	}

	@Test
	public void testLoadIfExist() throws IOException {
		Properties toLoad = new Properties();
		PropertiesUtil.loadIfExist(toLoad, new File("foo/bar"));
		assertNull(toLoad.getProperty("prop1"));
	}
	

}
