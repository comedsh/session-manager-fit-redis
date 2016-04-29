package org.ranran.tomcat.redissessions;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.ObjectStreamException;
import java.io.Serializable;


/**
 * 
 * This Class provides how serialize the session attributes. 
 * 
 * @version 
 * 
 * create time: 2016年4月28日 上午11:04:03
 *
 */
public class SessionSerializationMetadata implements Serializable {

  /**
	 * 
	 */
  private static final long serialVersionUID = -211346067829982622L;
  
  /*
   * keep the serial data of session's key-value pairs. 
   */
  private byte[] sessionAttributesSeriaData;

  public SessionSerializationMetadata() {
    this.sessionAttributesSeriaData = new byte[0];
  }

  public byte[] getSerialData() {
    return sessionAttributesSeriaData;
  }

  public void setSerialData( byte[] sessionAttributesSerialData ) {
	  
    this.sessionAttributesSeriaData = sessionAttributesSerialData;
  
  }

  /**
   * Notice that, when oos.writeObject( metadata ) from {@link JavaSerializer#makeSerialData(RedisSession, SessionSerializationMetadata) } <br>
   * it will invoke this private method to serialize the serial data.<br>
   * 
   * @see ObjectOutputStream#writeSerialData(Object obj, ObjectStreamClass desc) at line 1495 slotDesc.invokeWriteObject(obj, this); 
   * 
   * @param out
   * @throws IOException
   */
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	  
    out.writeInt( sessionAttributesSeriaData.length );
    
    out.write( this.sessionAttributesSeriaData );
    
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
	  
    int hashLength = in.readInt();
    
    byte[] serialData = new byte[hashLength];
    
    in.read(serialData, 0, hashLength);
    
    this.sessionAttributesSeriaData = serialData;
    
  }

  @SuppressWarnings("unused")
  private void readObjectNoData() throws ObjectStreamException {
	  
    this.sessionAttributesSeriaData = new byte[0];
  
  }

}
