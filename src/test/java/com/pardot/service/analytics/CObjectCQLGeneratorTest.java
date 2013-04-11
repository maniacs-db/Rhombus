package com.pardot.service.analytics;

import com.pardot.service.analytics.helpers.TestHelpers;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.pardot.service.tools.cobject.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Pardot, An ExactTarget Company
 * User: robrighter
 * Date: 4/9/13
 */
public class CObjectCQLGeneratorTest  extends TestCase {

	public class Subject extends CObjectCQLGenerator {

		public void testMakeStaticTableCreate() throws CObjectParseException{
			String json = TestHelpers.readFileToString(this.getClass(), "CObjectCQLGeneratorTestData.js");
			CDefinition def = new CDefinition(json);
			String cql = Subject.makeStaticTableCreate(def);
			String expected = "CREATE TABLE testtype (id timeuuid PRIMARY KEY, filtered int,data1 varchar,data2 varchar,data3 varchar,instance bigint,type int,foreignid bigint);";
			assertEquals(expected, cql);
		}

		public void testMakeWideTableCreate() throws CObjectParseException{
			String json = TestHelpers.readFileToString(this.getClass(), "CObjectCQLGeneratorTestData.js");
			CDefinition def = new CDefinition(json);
			String cql1 = Subject.makeWideTableCreate(def, def.indexes.get("foreign"));
			String expected1 = "CREATE TABLE testtype__foreign (id timeuuid, filtered int,data1 varchar,data2 varchar,data3 varchar,instance bigint,type int,foreignid bigint, PRIMARY KEY ((foreignid),id) );";
			assertEquals(expected1, cql1);

			String cql2 = Subject.makeWideTableCreate(def, def.indexes.get("instance"));
			String expected2 = "CREATE TABLE testtype__instance (id timeuuid, filtered int,data1 varchar,data2 varchar,data3 varchar,instance bigint,type int,foreignid bigint, PRIMARY KEY ((type, instance),id) );";
			assertEquals(expected2, cql2);

			String cql3 = Subject.makeWideTableCreate(def, def.indexes.get("foreign_instance"));
			String expected3 = "CREATE TABLE testtype__foreign_instance (id timeuuid, filtered int,data1 varchar,data2 varchar,data3 varchar,instance bigint,type int,foreignid bigint, PRIMARY KEY ((foreignid, type, instance),id) );";
			assertEquals(expected3, cql3);
		}

		public void testMakeCQLforInsert() throws CQLGenerationException, CObjectParseException {
			String json = TestHelpers.readFileToString(this.getClass(), "CObjectCQLGeneratorTestData.js");
			CDefinition def = new CDefinition(json);
			HashMap<String,String> data = new HashMap<String, String>();
			data.put("type","5");
			data.put("instance", "222222");
			data.put("filtered", "1");
			data.put("foreignid", "777");
			data.put("data1","This is data one");
			data.put("data2","This is data two");
			data.put("data3","This is data three");
			ArrayList<String> actual = Subject.makeCQLforInsert(def,data);
			assertEquals("Should generate CQL statements for the static table plus all indexes except the filtered index", 4, actual.size());
			data.put("filtered", "0");
			actual = Subject.makeCQLforInsert(def,data);
			assertEquals("Should generate CQL statements for the static table plus all indexes including the filtered index", 5, actual.size());
			//static table
			assertEquals("INSERT INTO testtype (id, filtered, data1, data2, data3, instance, type, foreignid) VALUES (NOW(), 0, 'This is data one', 'This is data two', 'This is data three', 222222, 5, 777);",actual.get(0));
			//index 1
			assertEquals("INSERT INTO testtype__foreign_instance (id, filtered, data1, data2, data3, instance, type, foreignid) VALUES (NOW(), 0, 'This is data one', 'This is data two', 'This is data three', 222222, 5, 777);", actual.get(1));
			//index 2
			assertEquals("INSERT INTO testtype__instance (id, filtered, data1, data2, data3, instance, type, foreignid) VALUES (NOW(), 0, 'This is data one', 'This is data two', 'This is data three', 222222, 5, 777);",actual.get(2));
			//index 3
			assertEquals("INSERT INTO testtype__foreign (id, filtered, data1, data2, data3, instance, type, foreignid) VALUES (NOW(), 0, 'This is data one', 'This is data two', 'This is data three', 222222, 5, 777);",actual.get(3));
			//index 4
			assertEquals("INSERT INTO testtype__unfiltered_Instance (id, filtered, data1, data2, data3, instance, type, foreignid) VALUES (NOW(), 0, 'This is data one', 'This is data two', 'This is data three', 222222, 5, 777);",actual.get(4));
		}

		public void testMakeCQLforCreate() throws CObjectParseException {
			String json = TestHelpers.readFileToString(this.getClass(), "CObjectCQLGeneratorTestData.js");
			CDefinition def = new CDefinition(json);
			ArrayList<String> actual = Subject.makeCQLforCreate(def);
			assertEquals("Should generate CQL statements for the static table plus all indexes", 5, actual.size());
		}


	}

	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public CObjectCQLGeneratorTest( String testName ) {
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite( CObjectCQLGeneratorTest.class );
	}

	public void testMakeStaticTableCreate() throws CObjectParseException{
		Subject s = new Subject();
		s.testMakeStaticTableCreate();
	}

	public void testMakeWideTableCreate() throws CObjectParseException {
		Subject s = new Subject();
		s.testMakeWideTableCreate();
	}

	public void testMakeCQLforCreate() throws CObjectParseException {
		Subject s = new Subject();
		s.testMakeCQLforCreate();
	}

	public void testMakeCQLforInsert() throws CQLGenerationException, CObjectParseException {
		Subject s = new Subject();
		s.testMakeCQLforInsert();
	}


}
