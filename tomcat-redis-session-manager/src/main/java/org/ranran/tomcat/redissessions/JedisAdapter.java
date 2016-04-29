package org.ranran.tomcat.redissessions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BitPosParams;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.geo.GeoRadiusParam;
import redis.clients.jedis.params.sortedset.ZAddParams;
import redis.clients.jedis.params.sortedset.ZIncrByParams;

/**
 * 
 * Adapter the JedisCluster and Jedis to provide the common interfaces.
 * 
 * 
 * @author shangyang
 * 
 * @ceateDate 2016-4-22
 *
 */

public class JedisAdapter implements JedisCommands{

	JedisCommands adapter;
	
	public JedisAdapter(JedisCommands jedis){
		
		this.adapter = jedis;
		
	}
	
	public JedisCommands getAdapter(){
		
		return this.adapter;
	}

	public String get(String key) {

		return this.adapter.get(key);
	}
	
	public byte[] get(byte[] key){
		
		if( this.adapter instanceof Jedis ){
			return ( (Jedis) adapter ).get( key );
		}
		
		if( this.adapter instanceof JedisCluster ){
			return ( (JedisCluster) adapter ).get( key );
		}		
		
		throw new RuntimeException("NOT SUPPORTED");		
		
	}

	public Boolean exists(String key) {
		
		return this.adapter.exists(key);
	}

	public Long persist(String key) {

		return this.adapter.persist(key);
	}

	public String type(String key) {

		return this.adapter.type(key);
	}

	public Long expire(String key, int seconds) {
		return this.adapter.expire(key, seconds);
	}
	
	public long expire(byte[] key, int seconds){
		
		if( this.adapter instanceof Jedis ){
			return ( (Jedis) adapter ).expire(key, seconds);
		}
		
		if( this.adapter instanceof JedisCluster ){
			return ( (JedisCluster) adapter ).expire(key, seconds);
		}		
		
		throw new RuntimeException("NOT SUPPORTED");
	}

	@Override
	public Long expireAt(String key, long unixTime) {
		
		return this.adapter.expireAt(key, unixTime);
		
	}

	@Override
	public Long ttl(String key) {

		return this.adapter.ttl(key);
	}

	@Override
	public Boolean setbit(String key, long offset, boolean value) {

		return this.adapter.setbit(key, offset, value);
		
	}

	@Override
	public Boolean setbit(String key, long offset, String value) {

		return this.adapter.setbit(key, offset, value);
	}

	@Override
	public Boolean getbit(String key, long offset) {

		return this.adapter.getbit(key, offset);
	}

	@Override
	public Long setrange(String key, long offset, String value) {

		return this.adapter.setrange(key, offset, value);
	}

	@Override
	public String getrange(String key, long startOffset, long endOffset) {

		return this.adapter.getrange(key, startOffset, endOffset);
	}

	@Override
	public String getSet(String key, String value) {

		return this.adapter.getSet(key, value);
	}

	@Override
	public Long setnx(String key, String value) {

		return this.adapter.setnx(key, value);
	}
	
	public Long setnx(byte[] key, byte[] value){
		
		if( this.adapter instanceof Jedis ){
			return ( (Jedis) adapter ).setnx(key, value);
		}
		
		if( this.adapter instanceof JedisCluster ){
			return ( (JedisCluster) adapter ).setnx(key, value);
		}
		
		throw new RuntimeException("unexpected error: BindaryJedisCluster or Jedis class needed.");
		
	}

	@Override
	public String setex(String key, int seconds, String value) {

		return this.adapter.setex(key, seconds, value);
	}

	@Override
	public Long decrBy(String key, long integer) {

		return this.adapter.decrBy(key, integer);
	}

	@Override
	public Long decr(String key) {

		return this.adapter.decr(key);
	}

	@Override
	public Long incrBy(String key, long integer) {

		return this.adapter.incrBy(key, integer);
	}

	@Override
	public Long incr(String key) {

		return this.adapter.incr(key);
	}

	@Override
	public Long append(String key, String value) {

		return this.adapter.append(key, value);
	}

	@Override
	public String substr(String key, int start, int end) {

		return this.adapter.substr(key, start, end);
	}

	@Override
	public Long hset(String key, String field, String value) {

		return this.adapter.hset(key, field, value);
	}

	@Override
	public String hget(String key, String field) {

		return this.adapter.hget(key, field);
	}

	@Override
	public Long hsetnx(String key, String field, String value) {

		return this.adapter.hsetnx(key, field, value);
	}

	@Override
	public String hmset(String key, Map<String, String> hash) {

		return this.adapter.hmset(key, hash);
	}

	@Override
	public List<String> hmget(String key, String... fields) {

		return this.adapter.hmget(key, fields);
	}

	@Override
	public Long hincrBy(String key, String field, long value) {

		return this.adapter.hincrBy(key, field, value);
	}

	@Override
	public Boolean hexists(String key, String field) {

		return this.adapter.hexists(key, field);
	}

	@Override
	public Long hdel(String key, String... field) {

		return this.adapter.hdel(key, field);
	}

	@Override
	public Long hlen(String key) {

		return this.adapter.hlen(key);
	}

	@Override
	public Set<String> hkeys(String key) {

		return this.adapter.hkeys(key);
	}

	@Override
	public List<String> hvals(String key) {

		return this.adapter.hvals(key);
	}

	@Override
	public Map<String, String> hgetAll(String key) {

		return this.adapter.hgetAll(key);
	}

	@Override
	public Long rpush(String key, String... string) {

		return this.adapter.rpush(key, string);
	}

	@Override
	public Long lpush(String key, String... string) {

		return this.adapter.lpush(key, string);
	}

	@Override
	public Long llen(String key) {

		return this.adapter.llen(key);
	}

	@Override
	public List<String> lrange(String key, long start, long end) {

		return this.adapter.lrange(key, start, end);
	}

	@Override
	public String ltrim(String key, long start, long end) {

		return this.adapter.ltrim(key, start, end);
	}

	@Override
	public String lindex(String key, long index) {

		return this.adapter.lindex(key, index);
	}

	@Override
	public String lset(String key, long index, String value) {

		return this.adapter.lset(key, index, value);
	}

	@Override
	public Long lrem(String key, long count, String value) {

		return this.adapter.lrem(key, count, value);
	}

	@Override
	public String lpop(String key) {

		return this.adapter.lpop(key);
	}

	@Override
	public String rpop(String key) {

		return this.adapter.rpop(key);
	}

	@Override
	public Long sadd(String key, String... member) {

		return this.adapter.sadd(key, member);
	}

	@Override
	public Set<String> smembers(String key) {

		return this.adapter.smembers(key);
	}

	@Override
	public Long srem(String key, String... member) {

		return this.adapter.srem(key, member);
	}

	@Override
	public String spop(String key) {

		return this.adapter.spop(key);
	}

	@Override
	public Long scard(String key) {

		return this.adapter.scard(key);
	}

	@Override
	public Boolean sismember(String key, String member) {

		return this.adapter.sismember(key, member);
	}

	@Override
	public String srandmember(String key) {

		return this.adapter.srandmember(key);
	}

	@Override
	public Long strlen(String key) {

		return this.adapter.strlen(key);
	}

	@Override
	public Long zadd(String key, double score, String member) {

		return this.adapter.zadd(key, score, member);
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers) {

		return this.adapter.zadd(key, scoreMembers);
	}

	@Override
	public Set<String> zrange(String key, long start, long end) {

		return this.adapter.zrange(key, start, end);
	}

	@Override
	public Long zrem(String key, String... member) {

		return this.adapter.zrem(key, member);
	}

	@Override
	public Double zincrby(String key, double score, String member) {

		return this.adapter.zincrby(key, score, member);
	}

	@Override
	public Long zrank(String key, String member) {

		return this.adapter.zrank(key, member);
	}

	@Override
	public Long zrevrank(String key, String member) {

		return this.adapter.zrevrank(key, member);
	}

	@Override
	public Set<String> zrevrange(String key, long start, long end) {

		return this.adapter.zrevrange(key, start, end);
	}

	@Override
	public Set<Tuple> zrangeWithScores(String key, long start, long end) {

		return this.adapter.zrangeWithScores(key, start, end);
	}

	@Override
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {

		return this.adapter.zrevrangeWithScores(key, start, end);
	}

	@Override
	public Long zcard(String key) {
		
		return this.adapter.zcard(key);
	}

	@Override
	public Double zscore(String key, String member) {
		
		return this.adapter.zscore(key, member);
	}

	@Override
	public List<String> sort(String key) {
		
		return this.adapter.sort(key);
	}

	@Override
	public List<String> sort(String key, SortingParams sortingParameters) {
		
		return this.adapter.sort(key, sortingParameters);
	}

	@Override
	public Long zcount(String key, double min, double max) {
		
		return this.adapter.zcount(key, min, max);
	}

	@Override
	public Long zcount(String key, String min, String max) {
		
		return this.adapter.zcount(key, min, max);
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max) {
		
		return this.adapter.zrangeByScore(key, min, max);
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max) {
		
		return this.adapter.zrangeByScore(key, min, max);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min) {
		
		return this.adapter.zrevrangeByScore(key, max, min);
	}

	@Override
	public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
		
		return this.adapter.zrangeByScore(key, min, max, offset, count);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min) {
		
		return this.adapter.zrevrangeByScore(key, max, min);
	}

	@Override
	public Set<String> zrangeByScore(String key, String min, String max, int offset, int count) {
		
		return this.adapter.zrangeByScore(key, min, max, offset, count);
	}

	@Override
	public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
		
		return this.adapter.zrevrangeByScore( key, max, min, offset, count );
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
		
		return this.adapter.zrangeByScoreWithScores( key, min, max );
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
		
		return this.adapter.zrevrangeByScoreWithScores( key, max, min );
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		
		return this.adapter.zrangeByScoreWithScores( key, min, max, offset, count );
	}

	@Override
	public Set<String> zrevrangeByScore(String key, String max, String min, int offset, int count) {
		
		return this.adapter.zrevrangeByScore( key, max, min, offset, count );
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
		
		return this.adapter.zrangeByScoreWithScores( key, min, max );
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min) {
		
		return this.adapter.zrevrangeByScoreWithScores( key, max, min );
	}

	@Override
	public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max, int offset, int count) {
		
		return this.adapter.zrangeByScoreWithScores( key, min, max, offset, count );
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
		
		return this.adapter.zrangeByScoreWithScores( key, max, min, offset, count );
	}

	@Override
	public Set<Tuple> zrevrangeByScoreWithScores(String key, String max, String min, int offset, int count) {
		
		return this.adapter.zrevrangeByScoreWithScores(key, max, min, offset, count);
	}

	@Override
	public Long zremrangeByRank(String key, long start, long end) {
		
		return this.adapter.zremrangeByRank( key, start, end );
	}

	@Override
	public Long zremrangeByScore(String key, double start, double end) {
		
		return this.adapter.zremrangeByScore( key, start, end );
	}

	@Override
	public Long zremrangeByScore(String key, String start, String end) {
		
		return this.adapter.zremrangeByScore( key, start, end );
	}

	@Override
	public Long linsert(String key, LIST_POSITION where, String pivot, String value) {
		
		return this.adapter.linsert(key, where, pivot, value);
	}

	@Override
	public Long lpushx(String key, String... string) {
		return this.adapter.lpushx(key, string);
	}

	@Override
	public Long rpushx(String key, String... string) {
		return this.adapter.rpushx(key, string);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<String> blpop(String arg) {
		return this.adapter.blpop(arg);
	}

	@SuppressWarnings("deprecation")
	public List<String> brpop(String arg) {
		return this.adapter.brpop(arg);
	}

	public Long del(String key) {
		return this.adapter.del(key);
	}

	@Override
	public String echo(String string) {
		
		return this.adapter.echo(string);
	}

	@Override
	public Long move(String key, int dbIndex) {
		
		return this.adapter.move(key, dbIndex);
	}

	@Override
	public Long bitcount(String key) {
		
		return this.adapter.bitcount(key);
	}

	@Override
	public Long bitcount(String key, long start, long end) {
		
		return this.adapter.bitcount(key, start, end);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
		
		return this.adapter.hscan(key, cursor);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ScanResult<String> sscan(String key, int cursor) {
		
		return this.adapter.sscan(key, cursor);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ScanResult<Tuple> zscan(String key, int cursor) {
		
		return this.adapter.zscan(key, cursor);
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor) {
		
		return this.adapter.hscan(key, cursor);
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor) {
		
		return this.adapter.sscan(key, cursor);
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor) {
		
		return this.adapter.zscan(key, cursor);
	}

	@Override
	public Long pfadd(String key, String... elements) {
		
		return this.adapter.pfadd(key, elements);
	}

	@Override
	public long pfcount(String key) {
		
		return this.adapter.pfcount(key);
	}

	@Override
	public String set(String key, String value, String nxxx) {
		
		return this.adapter.set(key, value, nxxx);
	}

	@Override
	public Long pexpire(String key, long milliseconds) {
		
		return this.adapter.pexpire(key, milliseconds);
	}

	@Override
	public Long pexpireAt(String key, long millisecondsTimestamp) {
		
		return this.adapter.pexpireAt(key, millisecondsTimestamp);
	}

	@Override
	public Long pttl(String key) {
		
		return this.adapter.pttl(key);
	}

	@Override
	public String psetex(String key, long milliseconds, String value) {
		
		return this.adapter.psetex(key, milliseconds, value);
	}

	@Override
	public Double incrByFloat(String key, double value) {
		
		return this.adapter.incrByFloat(key, value);
	}

	@Override
	public Double hincrByFloat(String key, String field, double value) {
		
		return this.adapter.hincrByFloat(key, field, value);
	}

	@Override
	public Set<String> spop(String key, long count) {
		
		return this.adapter.spop(key, count);
	}

	@Override
	public List<String> srandmember(String key, int count) {
		
		return this.adapter.srandmember(key, count);
	}

	@Override
	public Long zadd(String key, double score, String member, ZAddParams params) {
		
		return this.adapter.zadd( key, score, member, params );
	}

	@Override
	public Long zadd(String key, Map<String, Double> scoreMembers, ZAddParams params) {
		
		return this.adapter.zadd( key, scoreMembers, params);
	}

	@Override
	public Double zincrby(String key, double score, String member, ZIncrByParams params) {
		
		return this.adapter.zincrby(key, score, member);
	}

	@Override
	public Long zlexcount(String key, String min, String max) {
		
		return this.adapter.zlexcount(key, min, max);
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max) {
		
		return this.adapter.zrangeByLex(key, min, max);
	}

	@Override
	public Set<String> zrangeByLex(String key, String min, String max, int offset, int count) {
		
		return this.adapter.zrangeByLex(key, min, max, offset, count);
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min) {
		
		return this.adapter.zrevrangeByLex(key, max, min);
	}

	@Override
	public Set<String> zrevrangeByLex(String key, String max, String min, int offset, int count) {
		
		return this.adapter.zrevrangeByLex(key, max, min, offset, count);
	}

	@Override
	public Long zremrangeByLex(String key, String min, String max) {
		
		return this.adapter.zremrangeByLex(key, min, max);
	}

	@Override
	public List<String> blpop(int timeout, String key) {
		
		return this.adapter.blpop(timeout, key);
	}

	@Override
	public List<String> brpop(int timeout, String key) {
		
		return this.adapter.brpop(timeout, key);
	}

	@Override
	public Long bitpos(String key, boolean value) {
		
		return this.adapter.bitpos(key, value);
	}

	@Override
	public Long bitpos(String key, boolean value, BitPosParams params) {
		
		return this.adapter.bitpos(key, value, params);
	}

	@Override
	public ScanResult<Entry<String, String>> hscan(String key, String cursor, ScanParams params) {
		
		return this.adapter.hscan(key, cursor, params);
	}

	@Override
	public ScanResult<String> sscan(String key, String cursor, ScanParams params) {
		
		return this.adapter.sscan(key, cursor, params);
	}

	@Override
	public ScanResult<Tuple> zscan(String key, String cursor, ScanParams params) {
		
		return this.adapter.zscan(key, cursor, params);
	}

	@Override
	public Long geoadd(String key, double longitude, double latitude, String member) {
		
		return this.adapter.geoadd(key, longitude, latitude, member);
	}

	@Override
	public Long geoadd(String key, Map<String, GeoCoordinate> memberCoordinateMap) {
		
		return this.adapter.geoadd(key, memberCoordinateMap);
	}

	@Override
	public Double geodist(String key, String member1, String member2) {
		
		return this.adapter.geodist(key, member1, member2);
	}

	@Override
	public Double geodist(String key, String member1, String member2, GeoUnit unit) {
		
		return this.adapter.geodist(key, member1, member2, unit);
	}

	@Override
	public List<String> geohash(String key, String... members) {
		
		return this.adapter.geohash(key, members);
	}

	@Override
	public List<GeoCoordinate> geopos(String key, String... members) {
		
		return this.adapter.geopos(key, members);
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius,
			GeoUnit unit) {
		
		return this.adapter.georadius(key, longitude, latitude, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadius(String key, double longitude, double latitude, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return this.adapter.georadius( key, longitude, latitude, radius, unit, param);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit) {
		
		return this.adapter.georadiusByMember(key, member, radius, unit);
	}

	@Override
	public List<GeoRadiusResponse> georadiusByMember(String key, String member, double radius, GeoUnit unit,
			GeoRadiusParam param) {
		
		return this.adapter.georadiusByMember(key, member, radius, unit, param);
	}

	
	public String set(String key, String value) {
		
		return this.adapter.set(key, value);
	
	}
	
    public String set(final byte[] key, final byte[] value) {
    	
    	if( this.adapter instanceof Jedis ){
    		return ( (Jedis) adapter ).set( key, value );
    	}
    	
    	if( this.adapter instanceof JedisCluster ){
    		return ( (JedisCluster) adapter ).set( key, value );
    	}
    	
	    throw new RuntimeException("NOT SUPPORTED");
	    
	}

	public String set(String key, String value, String nxxx, String expx, long time) {
		
		return this.adapter.set(key, value, nxxx, expx, time);
	}

	public String flushDB(){
		
    	if( this.adapter instanceof Jedis ){
    		return ( (Jedis) adapter ).flushDB();
    	}
    	
    	if( this.adapter instanceof JedisCluster ){
    		return null; // do nothing
    	}
    	
	    throw new RuntimeException("NOT SUPPORTED");
		
	}

	public Long dbSize(){
		
    	if( this.adapter instanceof Jedis ){
    		return ( (Jedis) adapter ).dbSize();
    	}
    	
    	if( this.adapter instanceof JedisCluster ){
    		return 0L; // do nothing
    	}		
    	
    	throw new RuntimeException("NOT SUPPORTED");
    	
	}
	
	public Set<String> keys(String pattern){
		
    	if( this.adapter instanceof Jedis ){
    		return ( (Jedis) adapter ).keys( pattern );
    	}
    	
    	if( this.adapter instanceof JedisCluster ){
    		return Collections.emptySet(); // FIXME: to implement the logic of keys under clusters.
    	}		
    	
    	throw new RuntimeException("NOT SUPPORTED");		
		
	}
	
}
