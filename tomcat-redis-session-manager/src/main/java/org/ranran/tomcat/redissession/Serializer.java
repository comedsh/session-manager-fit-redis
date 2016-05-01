package org.ranran.tomcat.redissession;

import java.io.IOException;

public interface Serializer {
	
  void setClassLoader(ClassLoader loader);
  
  /**
   * 
   * Serialize the session attributes into bytes; <br>
   * 
   * The only usage of it is to provide the easiest way to compare the attributes key-values between the old attributes and the updated attributes <br>
   * 
   * @see RedisSessionManager#saveInternal(JedisAdapter, org.apache.catalina.Session, boolean), the code line as below <br>
   * 
   * Arrays.equals( originalSessionAttributesBytes, ( sessionAttributesBytes = serializer.makeBindaryData( redisSession ) ) ); <br>
   * 
   * if different, then synchronizes with redis 
   * 
   * @param session
   * @return
   * @throws IOException
   */
  byte[] makeBindaryData(RedisSession session) throws IOException;
  
  /**
   * 
   * Serialize the @see SessionSerializationMetadata which enwraps the binary data from @see JavaSerializer#makeBindaryData(RedisSession)
   * 
   * @param session
   * @param metadata
   * @return
   * @throws IOException
   */
  byte[] makeSerialData(RedisSession session, SessionSerializationMetadata metadata) throws IOException;
  
  /**
   * deserialize into session and metadata
   * 
   * @param data
   * @param session
   * @param metadata
   * @throws IOException
   * @throws ClassNotFoundException
   */
  void deserialize(byte[] data, RedisSession session, SessionSerializationMetadata metadata) throws IOException, ClassNotFoundException;
  
  /**
   * deserialize into metadata only
   * @param data
   * @param metadata
   * @throws IOException
   * @throws ClassNotFoundException
   */
  SessionSerializationMetadata deserialize( byte[] data ) throws IOException, ClassNotFoundException;
  
}
