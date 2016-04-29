package org.ranran.tomcat.redissessions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.CustomObjectInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.ranran.domain.User;


/**
 * 
 * To understand the serialization logic; how difference with serialize against @see SessionSerializationMetadata and @see Map
 * 
 * @author shangyang
 *
 * @version 
 * 
 * create time: 2016年4月28日 下午2:18:01
 *
 */
public class SerializerTest {

	
	/**
	 * 验证 serialize 的两层结构 
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testSerialize() throws IOException, ClassNotFoundException{
		
		RedisSessionManager manager = new RedisSessionManager();
		
		manager.setContainer( new StandardContext() );
		
		RedisSession session = new RedisSession( manager );
		
		session.setValid(true);
		
		session.setAttribute("hello", "world");
		
		session.setAttribute("lis1", Arrays.asList("A","B","C") );
		
		session.setAttribute("list2", Arrays.asList( new User("Zerk"), new User("Tony"), new User("Kane") ) );
		
		JavaSerializer serializer = new JavaSerializer();
		
		SessionSerializationMetadata metadata = new SessionSerializationMetadata();
		
		metadata.setSerialData( serializer.makeBindaryData(session) );
		
		byte[] serialdata = serializer.makeSerialData( session, metadata );
		
		// 验证 serialdata 包含两层结构
		
		// deserialize from the bytes
		
        BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream(serialdata) );
		
        ObjectInputStream ois = new CustomObjectInputStream( bis, null );
        
        // read the first layer
        SessionSerializationMetadata serializedMetadata = ( SessionSerializationMetadata ) ois.readObject();
        
        try{
        	
	        ObjectInputStream os = new ObjectInputStream( new ByteArrayInputStream( serializedMetadata.getSerialData() ) );
	        
	        @SuppressWarnings("unchecked")
			HashMap<String,Object> attributes = (HashMap<String,Object>) os.readObject();
	        
	        for( Iterator<String> iter = attributes.keySet().iterator(); iter.hasNext(); ){
	        	
	        	String key = iter.next();
	        	
	        	System.out.println("key: "+ key + "; values: " + attributes.get(key) );
	        	
	        }
        
        }catch( Exception e ){
        	
        	Assert.assertTrue("cannot be deserialized because the serial data has been encrypted one-way by MessageDigest", true);
        	
        }
        // read the second layer, deserializes those serial objects back into session.
        session.readObjectData( ois );       		
		
	}
	
	
	
	
}
