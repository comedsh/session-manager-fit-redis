package org.ranran.tomcat.redissession;

import java.io.IOException;
import java.security.Principal;

import org.apache.catalina.session.StandardSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * 
 * Removes the Dirty Tracking model out. <br>
 * 
 * Redis is based on the memory and it is fast enough doesn't need the performance from Tracking Model. 
 * 
 * @author mac
 *
 */
public class RedisSession extends StandardSession {

  /**
	 * 
	 */
  private static final long serialVersionUID = -4490692022824250675L;

  @SuppressWarnings("unused")
  private final Log log = LogFactory.getLog(RedisSession.class);

  public RedisSession( RedisSessionManager manager ) {
    super( manager );
  }

  @Override
  public void setAttribute(String key, Object value) {
    
	  super.setAttribute(key, value);
    
	  try {
		  
		  ((RedisSessionManager) manager).save( this );
		  
	  } catch (IOException e) {
		  
		e.printStackTrace();
		
		throw new RuntimeException( "save to redis failed", e );
		
	  }  
   
  }

  @Override
  public void removeAttribute(String name) {
    
	  super.removeAttribute(name);
	  
	  try {
		  
		  ((RedisSessionManager) manager).save( this );
		  
	  } catch (IOException e) {
		
		e.printStackTrace();
		
		throw new RuntimeException( "save to redis failed", e );
		
	  }
  }

  @Override
  public void setId(String id) {
    // Specifically do not call super(): it's implementation does unexpected things
    // like calling manager.remove(session.id) and manager.add(session).

    this.id = id;
  }

  @Override
  public void setPrincipal(Principal principal) {

	  super.setPrincipal(principal);
  
  }

  @Override
  public void writeObjectData(java.io.ObjectOutputStream out) throws IOException {
  
	  super.writeObjectData(out);
    
	  out.writeLong(this.getCreationTime());
  
  }

  @Override
  public void readObjectData(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    
	  super.readObjectData(in);
    
	  this.setCreationTime(in.readLong());
  
  }

}
