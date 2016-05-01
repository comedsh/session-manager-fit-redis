package org.ranran.tomcat.redissession;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;
import redis.clients.util.Pool;


public class RedisSessionManager extends ManagerBase implements Lifecycle {

  protected byte[] NULL_SESSION_DATA = "null".getBytes();

  private final Log log = LogFactory.getLog(RedisSessionManager.class);

  protected String host = "localhost";
  protected int port = 6379;
  protected int database = 0;
  protected String password = null;
  protected int timeout = Protocol.DEFAULT_TIMEOUT;
  protected String sentinelMaster = null;
  Set<String> sentinelSet = null;

  protected Pool<Jedis> connectionPool;
  protected JedisPoolConfig connectionPoolConfig = new JedisPoolConfig();

  protected JedisCluster jedisCluster;
  protected Set<String> cluster;
  
  protected RedisSessionHandlerValve handlerValve;
  
  // I've think for a while that, to remove the currentSession and currentSessionId.. 
  // but, for reduce the communication between redis cluster server to retrieve the redis session, that worth not to remove it. 
  
  protected ThreadLocal<RedisSession> currentSession = new ThreadLocal<>();
  
  protected ThreadLocal<String> currentSessionId = new ThreadLocal<>();
  
  protected Serializer serializer;

  protected static String name = "RedisSessionManager";

  /**
   * The lifecycle event support for this component.
   */
  protected LifecycleSupport lifecycle = new LifecycleSupport(this);

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getDatabase() {
    return database;
  }

  public void setDatabase(int database) {
    this.database = database;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSentinels() {
    StringBuilder sentinels = new StringBuilder();
    for (Iterator<String> iter = this.sentinelSet.iterator(); iter.hasNext();) {
      sentinels.append(iter.next());
      if (iter.hasNext()) {
        sentinels.append(",");
      }
    }
    return sentinels.toString();
  }

  public void setSentinels(String sentinels) {
    if (null == sentinels) {
      sentinels = "";
    }

    String[] sentinelArray = sentinels.split(",");
    this.sentinelSet = new HashSet<String>(Arrays.asList(sentinelArray));
  }

  public void setCluster(String cluster){
	 
	  if(null == cluster){
	  
		  cluster = "";
	 
	  }
	 
	  String[] sentinelArray = cluster.split(",");
	 
	  this.cluster = new HashSet<String>(Arrays.asList(sentinelArray));
	 
  }
  
  public Set<String> getSentinelSet() {
    return this.sentinelSet;
  }

  public String getSentinelMaster() {
    return this.sentinelMaster;
  }

  public void setSentinelMaster(String master) {
    this.sentinelMaster = master;
  }

  @Override
  public int getRejectedSessions() {
    // Essentially do nothing.
    return 0;
  }

  public void setRejectedSessions(int i) {
    // Do nothing.
  }

  protected JedisAdapter acquireConnection() {

	if(connectionPool != null ){  
	    
	   Jedis jedis = connectionPool.getResource();
	
	   if (getDatabase() != 0) {
	     jedis.select(getDatabase());
	   }
	    
	   return new JedisAdapter(jedis);
	}
	
	if( jedisCluster != null ){
		return new JedisAdapter(jedisCluster);
	}

    throw new SessionManagerException("no expected connection retrived");
    
  }
  
  @SuppressWarnings("deprecation")
  protected void returnConnection(Jedis jedis, Boolean error) {
    if (error) {
      connectionPool.returnBrokenResource(jedis);
    } else {
      connectionPool.returnResource(jedis);
    }
  }

  protected void returnConnection(Jedis jedis) {
    returnConnection(jedis, false);
  }

  @Override
  public void load() throws ClassNotFoundException, IOException {

  }

  @Override
  public void unload() throws IOException {

  }

  /**
   * Add a lifecycle event listener to this component.
   *
   * @param listener The listener to add
   */
  @Override
  public void addLifecycleListener(LifecycleListener listener) {
    lifecycle.addLifecycleListener(listener);
  }

  /**
   * Get the lifecycle listeners associated with this lifecycle. If this
   * Lifecycle has no listeners registered, a zero-length array is returned.
   */
  @Override
  public LifecycleListener[] findLifecycleListeners() {
    return lifecycle.findLifecycleListeners();
  }


  /**
   * Remove a lifecycle event listener from this component.
   *
   * @param listener The listener to remove
   */
  @Override
  public void removeLifecycleListener(LifecycleListener listener) {
    lifecycle.removeLifecycleListener(listener);
  }

  /**
   * Start this component and implement the requirements
   * of {@link org.apache.catalina.util.LifecycleBase#startInternal()}.
   *
   * @exception LifecycleException if this component detects a fatal error
   *  that prevents this component from being used
   */
  @Override
  protected synchronized void startInternal() throws LifecycleException {
	  
    super.startInternal();

    setState(LifecycleState.STARTING);

    Boolean attachedToValve = false;
    for (Valve valve : getContainer().getPipeline().getValves()) {
      if (valve instanceof RedisSessionHandlerValve) {
        this.handlerValve = (RedisSessionHandlerValve) valve;
        this.handlerValve.setRedisSessionManager(this);
        log.info("Attached to RedisSessionHandlerValve");
        attachedToValve = true;
        break;
      }
    }

    if (!attachedToValve) {
      String error = "Unable to attach to session handling valve; sessions cannot be saved after the request without the valve starting properly.";
      log.fatal(error);
      throw new LifecycleException(error);
    }

    try {
      initializeSerializer();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      log.fatal("Unable to load serializer", e);
      throw new LifecycleException(e);
    }

    log.info("Will expire sessions after " + getMaxInactiveInterval() + " seconds");

    initializeDatabaseConnection();

    setDistributable(true);
    
  }


  /**
   * Stop this component and implement the requirements
   * of {@link org.apache.catalina.util.LifecycleBase#stopInternal()}.
   *
   * @exception LifecycleException if this component detects a fatal error
   *  that prevents this component from being used
   */
  @Override
  protected synchronized void stopInternal() throws LifecycleException {
    if (log.isDebugEnabled()) {
      log.debug("Stopping");
    }

    setState(LifecycleState.STOPPING);

    try {
      connectionPool.destroy();
    } catch(Exception e) {
      // Do nothing.
    }

    // Require a new random number generator if we are restarted
    super.stopInternal();
  }

  @Override
  public Session createSession(String requestedSessionId) {
	  
    RedisSession session = null;
    
    String sessionId = null;
    
    String jvmRoute = getJvmRoute();
    
    if( jvmRoute == null )
    	
    	throw new SessionManagerException("jvmRoute must set to avoid session duplication under tomcat cluster environment");
    
    Boolean error = true;
    
    JedisAdapter jedis = null;
    
    try {
    	
      jedis = acquireConnection();

      // if request session id != null, means the session id from cookie;
      // but <notice> that, because the incoming request session id is generated via redis-session managed container, so it will always appends the jvmRoute suffix
      // this is why I removed the code sessionIdWithJvmRoute...
      
      if ( null != requestedSessionId ) {
    	  
        // sessionId = sessionIdWithJvmRoute( requestedSessionId, jvmRoute );
        
        if( jedis.get( requestedSessionId.getBytes() ) != null ){
        	
        	currentSession.set(null);
        	
        	currentSessionId.set(null);
        	
        	return null; // the session get already created. find session should take over it.
        }
        
      } else {
        	
    	// generate the unique sessionid under tomcat clusters.  
        sessionId = sessionIdWithJvmRoute( generateSessionId(), jvmRoute );
        
      }

      /* Even though the key is set in Redis, we are not going to flag
         the current thread as having had the session persisted since
         the session isn't actually serialized to Redis yet.
         This ensures that the save(session) at the end of the request
         will serialize the session into Redis with 'set' instead of 'setnx'. */

      error = false;

      if (null != sessionId) {
    	  
        session = (RedisSession)createEmptySession();
        
        session.setNew(true);
        
        session.setValid(true);
        
        session.setCreationTime(System.currentTimeMillis());
        
        session.setMaxInactiveInterval(getMaxInactiveInterval());
        
        session.setId(sessionId);
        
        session.tellNew();
        
      }

      currentSession.set(session);
      
      currentSessionId.set(sessionId);

      if (null != session) {
    	  
        try {
        	
          error = saveInternal( jedis, session );
          
        } catch (IOException ex) {
        	
          log.error("Error saving newly created session: " + ex.getMessage());
          
          currentSession.set(null);
          
          currentSessionId.set(null);
          
          session = null;
          
        }
        
      }
      
    } finally {
    	
       // this logic is Jedis specific, to check connection pool is null or not. if it is null, then cluster env.	
       if (jedis != null && this.connectionPool !=null) {
      	  
     	   returnConnection( (Jedis) jedis.getAdapter(), error);
     	   
       }

    }

    return session;
  }

  private String sessionIdWithJvmRoute(String sessionId, String jvmRoute) {
    if (jvmRoute != null) {
      String jvmRoutePrefix = '.' + jvmRoute;
      return sessionId.endsWith(jvmRoutePrefix) ? sessionId : sessionId + jvmRoutePrefix;
    }
    return sessionId;
  }

  @Override
  public Session createEmptySession() {
    return new RedisSession(this);
  }

  @Override
  public void add(Session session) {
    try {
      save(session);
    } catch (IOException ex) {
      log.warn("Unable to add to session manager store: " + ex.getMessage());
      throw new SessionManagerException("Unable to add to session manager store.", ex);
    }
  }

  @Override
  public Session findSession(String id) throws IOException {
	  
    RedisSession session = null;
    
    if (null == id) {

      currentSession.set(null);

      currentSessionId.set(null);

    } else if (id.equals(currentSessionId.get())) {
    
    	session = currentSession.get();
    
    } else {
    	
      byte[] data = loadSessionDataFromRedis( id );
      
      if (data != null) {
    	  
        DeserializedSessionContainer container = sessionFromSerializedData(id, data);
        
        session = container.session;
        
        currentSession.set(session);
        
        currentSessionId.set(id);
        
      } else {
    	  
        currentSession.set(null);
        
        currentSessionId.set(null);
        
      }
    }

    return session;
  }

  public void clear() {
    JedisAdapter jedis = null;
    Boolean error = true;
    try {
      jedis = acquireConnection();
      jedis.flushDB();
      error = false;
    } finally {
        if (jedis != null && this.connectionPool !=null) {
        	  
      	   returnConnection( (Jedis) jedis.getAdapter(), error);
      	   
        }

    }
  }

  public int getSize() throws IOException {
	JedisAdapter jedis = null;
    Boolean error = true;
    try {
      jedis = acquireConnection();
      int size = jedis.dbSize().intValue();
      error = false;
      return size;
    } finally {
    	
        if (jedis != null && this.connectionPool !=null) {
        	  
      	   returnConnection( (Jedis) jedis.getAdapter(), error);
      	   
        }

    }
  }

  public String[] keys() throws IOException {
	JedisAdapter jedis = null;
    Boolean error = true;
    try {
      jedis = acquireConnection();
      Set<String> keySet = jedis.keys("*");
      error = false;
      return keySet.toArray(new String[keySet.size()]);
    } finally {
    	
        if (jedis != null && this.connectionPool !=null) {
      	  
       	   returnConnection( (Jedis) jedis.getAdapter(), error);
       	   
         }
        
    }
  }

  public byte[] loadSessionDataFromRedis(String id) throws IOException {
	  
	JedisAdapter jedis = null;
	
    Boolean error = true;

    try {
    	
      log.trace("Attempting to load session " + id + " from Redis");

      jedis = acquireConnection();
      
      byte[] data = jedis.get( id.getBytes() );
      
      error = false;

      if (data == null) {
        log.trace("Session " + id + " not found in Redis");
      }

      return data;
      
    } finally {
    	
        if (jedis != null && this.connectionPool !=null) {
        	
        	returnConnection( (Jedis) jedis.getAdapter(), error);
        	
        }
    }
  }

  /**
   * so bad smell get touched from my nose.<br>
   * 
   * DeserializedSessionContainer existed here for only one reason, return two objects, the SessionSerializationMetadata and RedisSession, back at one time.
   * 
   * @param id
   * @param data
   * @return
   * @throws IOException
   */
  public DeserializedSessionContainer sessionFromSerializedData(String id, byte[] data) throws IOException {
	  
    log.trace("Deserializing session " + id + " from Redis");

    if (Arrays.equals(NULL_SESSION_DATA, data)) {
      log.error("Encountered serialized session " + id + " with data equal to NULL_SESSION. This is a bug.");
      throw new IOException("Serialized session data was equal to NULL_SESSION");
    }

    RedisSession session = null;
    
    SessionSerializationMetadata metadata = new SessionSerializationMetadata();

    try {
    	
      session = (RedisSession)createEmptySession();

      serializer.deserialize( data, session, metadata );

      session.setId(id);
      session.setNew(false);
      session.setMaxInactiveInterval(getMaxInactiveInterval());
      session.access();
      session.setValid(true);

      if (log.isTraceEnabled()) {
        log.trace("Session Contents [" + id + "]:");
        Enumeration<?> en = session.getAttributeNames();
        while(en.hasMoreElements()) {
          log.trace("  " + en.nextElement());
        }
      }
    } catch (ClassNotFoundException ex) {
      log.fatal("Unable to deserialize into session", ex);
      throw new IOException("Unable to deserialize into session", ex);
    }

    return new DeserializedSessionContainer(session, metadata);
    
  }

  public void save( Session session ) throws IOException {
    
	JedisAdapter jedis = null;
    
    Boolean error = true;

    try {
    	
      jedis = acquireConnection();
      
      error = saveInternal( jedis, session );
      
    } catch (IOException e) {
    	
      throw e;
      
    } finally {
    	
        if (jedis != null && this.connectionPool !=null) {
        	
        	returnConnection( (Jedis) jedis.getAdapter(), error);
        	
        }
        
    }
  }

  @SuppressWarnings("finally")
  protected boolean saveInternal( JedisAdapter jedis, Session session ) throws IOException {
	  
    Boolean error = true;

    try {
    	
      log.trace("Saving session " + session + " into Redis");

      RedisSession redisSession = (RedisSession) session;

      if (log.isTraceEnabled()) {
        log.trace("Session Contents [" + redisSession.getId() + "]:");
        Enumeration<?> en = redisSession.getAttributeNames();
        while(en.hasMoreElements()) {
          log.trace("  " + en.nextElement());
        }
      }
      
      byte[] data = this.loadSessionDataFromRedis( redisSession.getId() ); 
      
      // 只有如下两种情况下，需要 save into redis.
      // #1 data is null means this session never get saved into redis before. 
      //    | 压根还没有序列化入 redis；
      // #2 compare between the key-value serial data from redis and tomcat jvm, if not the same, means dirty, needs synchronized. 
      //    | redis 存储的 session 值与 当前 tomcat 中的 session 值不匹配
      
      if ( data == null || !Arrays.equals( serializer.deserialize(data).getSerialData(), serializer.makeBindaryData( redisSession ) ) ) {

        log.trace("Save was determined to be necessary");

        byte[] sessionAttributesBytes = serializer.makeBindaryData( redisSession );        

        SessionSerializationMetadata updatedSerializationMetadata = new SessionSerializationMetadata();
        
        updatedSerializationMetadata.setSerialData( sessionAttributesBytes );
        
        jedis.set( redisSession.getId().getBytes(), serializer.makeSerialData( redisSession, updatedSerializationMetadata ) );
        
      } else {
        log.trace("Save was determined to be unnecessary");
      }

      log.trace("Setting expire timeout on session [" + redisSession.getId() + "] to " + getMaxInactiveInterval());
      
      jedis.expire( redisSession.getId(), getMaxInactiveInterval() );

      error = false;

      return error;
      
    } catch (IOException e) {
    	
      log.error(e.getMessage());
      
      // 之前莫名其妙的问题，怎么 session 不能将对象保存到 redis 里面了... 加上下面这一行的代码相当的关键.. 就知道原因了，是因为 User 对象没有 implements Serializable.
      e.printStackTrace();
      
      throw e;
      
    } finally {
    	
      return error;
      
    }
    
  }

  @Override
  public void remove(Session session) {
    remove(session, false);
  }

  @Override
  public void remove(Session session, boolean update) {
    JedisAdapter jedis = null;
    Boolean error = true;

    log.trace("Removing session ID : " + session.getId());

    try {
      
      jedis = acquireConnection();
      
      jedis.del(session.getId());
      
      error = false;
      
    } finally {
    	
      if (jedis != null && this.connectionPool !=null) {
    	  
    	   returnConnection( (Jedis) jedis.getAdapter(), error);
    	   
      }
      
    }
  }

  public void afterRequest() {
	  
    RedisSession redisSession = currentSession.get();
    
    if (redisSession != null) {
    	
      try {
    	  
        if ( redisSession.isValid() ) { // need always to check if the session get invalid.
        	
          log.trace("Request with session completed, saving session " + redisSession.getId());
          
          save( redisSession );
          
        } else {
        	
          log.trace("HTTP Session has been invalidated, removing :" + redisSession.getId());
          
          remove(redisSession);
          
        }
        
      } catch (Exception e) {
    	  
        log.error("Error storing/removing session", e);
        
      } finally {
    	  
        currentSession.remove();
        
        currentSessionId.remove();
        
        log.trace("Session removed from ThreadLocal :" + redisSession.getIdInternal());
        
      }
    }
  }

  @Override
  public void processExpires() {
    // We are going to use Redis's ability to expire keys for session expiration.

    // Do nothing.
  }

  protected void initializeDatabaseConnection() throws LifecycleException {
	  
    try {
      
      // Way 1: Master-Slave with Sentinel	
      if (getSentinelMaster() != null) {
        
    	Set<String> sentinelSet = getSentinelSet();
        
        if (sentinelSet != null && sentinelSet.size() > 0) {
          
        	connectionPool = new JedisSentinelPool(getSentinelMaster(), sentinelSet, this.connectionPoolConfig, getTimeout(), getPassword());
          
        }else {
        	
      		throw new LifecycleException("Error configuring Redis Sentinel connection pool: expected both `sentinelMaster` and `sentiels` to be configured");
        
        }
      
      // Way 2: Clusters   
      } else if( this.cluster != null ){

    	  Set<HostAndPort> connectionPoints = new HashSet<HostAndPort>();
    	  
    	  for( String s : cluster ){
    		  
    		  String[] ss = s.split(":");
    		  
    		  connectionPoints.add( new HostAndPort( ss[0], Integer.parseInt( ss[1]) ) );
    		  
    	  }
    	  
    	  jedisCluster = new JedisCluster( connectionPoints );
      
      // Way 3: Master-Slave	  
      } else {
      
    	  connectionPool = new JedisPool(this.connectionPoolConfig, getHost(), getPort(), getTimeout(), getPassword());
      
      }
      
    } catch (Exception e) {
    	
      e.printStackTrace();
      
      throw new LifecycleException("Error connecting to Redis", e);
      
    }
    
  }

  
  
  protected void initializeSerializer() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    
    serializer = new JavaSerializer();

    Loader loader = null;

    if (getContainer() != null) {
      loader = getContainer().getLoader();
    }

    ClassLoader classLoader = null;

    if (loader != null) {
      classLoader = loader.getClassLoader();
    }
    
    serializer.setClassLoader( classLoader );
    
  }


  // Connection Pool Config Accessors

  // - from org.apache.commons.pool2.impl.GenericObjectPoolConfig

  public int getConnectionPoolMaxTotal() {
    return this.connectionPoolConfig.getMaxTotal();
  }

  public void setConnectionPoolMaxTotal(int connectionPoolMaxTotal) {
    this.connectionPoolConfig.setMaxTotal(connectionPoolMaxTotal);
  }

  public int getConnectionPoolMaxIdle() {
    return this.connectionPoolConfig.getMaxIdle();
  }

  public void setConnectionPoolMaxIdle(int connectionPoolMaxIdle) {
    this.connectionPoolConfig.setMaxIdle(connectionPoolMaxIdle);
  }

  public int getConnectionPoolMinIdle() {
    return this.connectionPoolConfig.getMinIdle();
  }

  public void setConnectionPoolMinIdle(int connectionPoolMinIdle) {
    this.connectionPoolConfig.setMinIdle(connectionPoolMinIdle);
  }


  // - from org.apache.commons.pool2.impl.BaseObjectPoolConfig

  public boolean getLifo() {
    return this.connectionPoolConfig.getLifo();
  }
  public void setLifo(boolean lifo) {
    this.connectionPoolConfig.setLifo(lifo);
  }
  public long getMaxWaitMillis() {
    return this.connectionPoolConfig.getMaxWaitMillis();
  }

  public void setMaxWaitMillis(long maxWaitMillis) {
    this.connectionPoolConfig.setMaxWaitMillis(maxWaitMillis);
  }

  public long getMinEvictableIdleTimeMillis() {
    return this.connectionPoolConfig.getMinEvictableIdleTimeMillis();
  }

  public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
    this.connectionPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
  }

  public long getSoftMinEvictableIdleTimeMillis() {
    return this.connectionPoolConfig.getSoftMinEvictableIdleTimeMillis();
  }

  public void setSoftMinEvictableIdleTimeMillis(long softMinEvictableIdleTimeMillis) {
    this.connectionPoolConfig.setSoftMinEvictableIdleTimeMillis(softMinEvictableIdleTimeMillis);
  }

  public int getNumTestsPerEvictionRun() {
    return this.connectionPoolConfig.getNumTestsPerEvictionRun();
  }

  public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
    this.connectionPoolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
  }

  public boolean getTestOnCreate() {
    return this.connectionPoolConfig.getTestOnCreate();
  }

  public void setTestOnCreate(boolean testOnCreate) {
    this.connectionPoolConfig.setTestOnCreate(testOnCreate);
  }

  public boolean getTestOnBorrow() {
    return this.connectionPoolConfig.getTestOnBorrow();
  }

  public void setTestOnBorrow(boolean testOnBorrow) {
    this.connectionPoolConfig.setTestOnBorrow(testOnBorrow);
  }

  public boolean getTestOnReturn() {
    return this.connectionPoolConfig.getTestOnReturn();
  }

  public void setTestOnReturn(boolean testOnReturn) {
    this.connectionPoolConfig.setTestOnReturn(testOnReturn);
  }

  public boolean getTestWhileIdle() {
    return this.connectionPoolConfig.getTestWhileIdle();
  }

  public void setTestWhileIdle(boolean testWhileIdle) {
    this.connectionPoolConfig.setTestWhileIdle(testWhileIdle);
  }

  public long getTimeBetweenEvictionRunsMillis() {
    return this.connectionPoolConfig.getTimeBetweenEvictionRunsMillis();
  }

  public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
    this.connectionPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
  }

  public String getEvictionPolicyClassName() {
    return this.connectionPoolConfig.getEvictionPolicyClassName();
  }

  public void setEvictionPolicyClassName(String evictionPolicyClassName) {
    this.connectionPoolConfig.setEvictionPolicyClassName(evictionPolicyClassName);
  }

  public boolean getBlockWhenExhausted() {
    return this.connectionPoolConfig.getBlockWhenExhausted();
  }

  public void setBlockWhenExhausted(boolean blockWhenExhausted) {
    this.connectionPoolConfig.setBlockWhenExhausted(blockWhenExhausted);
  }

  public boolean getJmxEnabled() {
    return this.connectionPoolConfig.getJmxEnabled();
  }

  public void setJmxEnabled(boolean jmxEnabled) {
    this.connectionPoolConfig.setJmxEnabled(jmxEnabled);
  }
  public String getJmxNameBase() {
    return this.connectionPoolConfig.getJmxNameBase();
  }
  public void setJmxNameBase(String jmxNameBase) {
    this.connectionPoolConfig.setJmxNameBase(jmxNameBase);
  }

  public String getJmxNamePrefix() {
    return this.connectionPoolConfig.getJmxNamePrefix();
  }

  public void setJmxNamePrefix(String jmxNamePrefix) {
    this.connectionPoolConfig.setJmxNamePrefix(jmxNamePrefix);
  }
}

class DeserializedSessionContainer {
	
  public final RedisSession session;
  
  public final SessionSerializationMetadata metadata;
  
  public DeserializedSessionContainer(RedisSession session, SessionSerializationMetadata metadata) {
	  
    this.session = session;
    
    this.metadata = metadata;
    
  }
  
}
