package com.demon.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;


public abstract class InfluxDBConnect {

	private String username;// 用户名
    private String password;// 密码
    private String openurl;// 连接地址
    private String database;// 数据库
    private String retentionPolicy;//保留策略

    private InfluxDB influxDB;
       
    /*public InfluxDBConnect(){
    	influxDbBuild();
        createRetentionPolicy();
    }*/

    /*public InfluxDBConnect(String username, String password, String openurl, String database) {
        this.username = username;
        this.password = password;
        this.openurl = openurl;
        this.database = database;
    }*/

    /** 连接时序数据库；获得InfluxDB **/
    public InfluxDB influxDbBuild() {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(openurl, username, password);
            influxDB.createDatabase(database);
            influxDB.enableBatch(20,200, TimeUnit.MILLISECONDS);
        }
        return influxDB;
    }

    /** 连接时序数据库；获得InfluxDB **/
    public InfluxDB influxDbBuild(String username, String password, String database) {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(openurl, username, password);
            influxDB.createDatabase(database);
            //influxDB.enableBatch(20,200, TimeUnit.MILLISECONDS);

        }
        return influxDB;
    }
    
    /** 连接时序数据库；获得InfluxDB **/
    public InfluxDB influxDbBuild(String openurl, String username, String password, String database) {
        if (influxDB == null) {
            influxDB = InfluxDBFactory.connect(openurl, username, password);
            influxDB.createDatabase(database);

        }
        return influxDB;
    }
    
    /**
	 * 创建自定义保留策略
	 * 
	 * @param policyName
	 *            策略名
	 * @param duration
	 *            保存天数，最小不能低于1h
	 * @param replication
	 *            保存副本数量
	 * @param isDefault
	 *            是否设为默认保留策略
	 */
    public void createRetentionPolicy(String policyName, String duration, int replication, Boolean isDefault) {
    	createRetentionPolicy(policyName,duration,"1h",replication,isDefault);
	}
    
    /**
     * 创建自定义保留策略
     * @param policyName
     * @param duration
     * @param shardGroupDuration
     * @param replication
     * @param isDefault
     */
    public void createRetentionPolicy(String policyName, String duration,String shardGroupDuration, int replication, Boolean isDefault) {
		String sql = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s SHARD DURATION "+shardGroupDuration, policyName,
				database, duration, replication);
		if (isDefault) {
			sql = sql + " DEFAULT";
		}
		this.query(sql);
	}

    
    /**
     * 设置数据保存策略 defalut 策略名 /database 数据库名/ 30d 数据保存时限30天/ 1 副本个数为1/ 结尾DEFAULT
     * 表示 设为默认的策略
     */
    public void createRetentionPolicy() {
        String command = String.format("CREATE RETENTION POLICY \"%s\" ON \"%s\" DURATION %s REPLICATION %s DEFAULT",
                "defalut", database, "0s", 1);
        this.query(command);
    }

    /**
     * 查询
     * 
     * @param command
     *            查询语句
     * @return
     */
    public QueryResult query(String command) {
        return influxDB.query(new Query(command, database));
    }

    /**
     * 插入
     * 
     * @param measurement
     *            表
     * @param tags
     *            标签
     * @param fields
     *            字段
     */
    public void insert(String measurement, Map<String, String> tags, Map<String, Object> fields) {
        Builder builder = Point.measurement(measurement);
        //builder.time(((long)fields.get("currentTime"))*1000000, TimeUnit.NANOSECONDS);
        if(fields.containsKey("time")) {
        	builder.time(Long.parseLong(fields.get("time").toString()),TimeUnit.MILLISECONDS);
        	fields.remove("time");
        }
        builder.tag(tags);
        builder.fields(fields);
        //
        influxDB.write(database, "", builder.build());
    }
    
    /**
     * 
     * @param measurement 表
     * @param retentionPolicy 保留策略
     * @param tags 标签
     * @param fields 字段
     */
    public void insertAddRetentionPolicy(String measurement,String retentionPolicy,Map<String, String> tags, Map<String, Object> fields) {
    	Builder builder = Point.measurement(measurement);
    	if(fields.containsKey("time")) {
        	builder.time(Long.parseLong(fields.get("time").toString()),TimeUnit.MILLISECONDS);
        	fields.remove("time");
        }
        builder.tag(tags);
        builder.fields(fields);
        builder.tag(tags);
        builder.fields(fields);
        influxDB.write(database, retentionPolicy, builder.build());
    }
    
    /**
     * 批量插入
     * 
     * @param measurement
     *            表
     * @param tags
     *            标签
     * @param fields
     *            字段
     */
    public void batchinsert(String measurement, List<Map<String, String>> tags, List<Map<String, Object>> fieldslist) {
    	BatchPoints batchPoints = BatchPoints.database(database).consistency(InfluxDB.ConsistencyLevel.ALL).tag("async", "true").build();

        for (int i = 0;i < fieldslist.size();i++) {
        	Map<String, Object> map = fieldslist.get(i);
            Builder builder = Point.measurement(measurement);
            if(map.containsKey("time")) {
            	builder.time(Long.parseLong(map.get("time").toString()),TimeUnit.MILLISECONDS);
            	map.remove("time");
            }
            builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

			for (Entry<String, Object> entry : map.entrySet()) {
				if(entry.getValue() instanceof Integer) {
					builder.addField(entry.getKey(),(Integer)(entry.getValue()));
				}else if(entry.getValue() instanceof Long){
					builder.addField(entry.getKey(),(Long)(entry.getValue()));
				}else if(entry.getValue() instanceof Double){
					builder.addField(entry.getKey(),(Double)(entry.getValue()));
				}else if(entry.getValue() instanceof String){
					builder.addField(entry.getKey(),entry.getValue().toString());
				}else if(entry.getValue() instanceof Float){
					Number number = new Float((Float)entry.getValue());
					builder.addField(String.valueOf(entry.getKey()),number);
				}else if(entry.getValue() instanceof Boolean){
					builder.addField(String.valueOf(entry.getKey()),(Boolean)entry.getValue());
				}
			}
            Map<String, String> tagsMap = tags.get(i);
            //Set<String>
            for (Entry<String, String> entry : tagsMap.entrySet()) {
				 builder.tag(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));

            }
            batchPoints.point(builder.build());
        }
        influxDB.write(batchPoints);
    }

    /**
     *
     * @param measurement：数据库
     * @param retentionPolicy：保存策略
     * @param tags
     * @param fieldslist
     */
    public void batchInsertAddRetentionPolicy(String measurement,String retentionPolicy ,List<Map<String, String>> tags, List<Map<String, Object>> fieldslist) {
    	BatchPoints batchPoints = BatchPoints.database(database).retentionPolicy(retentionPolicy).consistency(InfluxDB.ConsistencyLevel.ALL).tag("async", "true").build();
        
        for (int i = 0;i < fieldslist.size();i++) {
        	Map<String, Object> map = fieldslist.get(i);
            Builder builder = Point.measurement(measurement);
            if(map.containsKey("time")) {
            	builder.time(Long.parseLong(map.get("time").toString()),TimeUnit.MILLISECONDS);
            	map.remove("time");
            }
            builder.time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            
			for (Entry<String, Object> entry : map.entrySet()) {
				if(entry.getValue() instanceof Integer) {
					builder.addField(entry.getKey(),(Integer)(entry.getValue()));
				}else if(entry.getValue() instanceof Long){
					builder.addField(entry.getKey(),(Long)(entry.getValue()));
				}else if(entry.getValue() instanceof Double){
					builder.addField(entry.getKey(),(Double)(entry.getValue()));
				}else if(entry.getValue() instanceof String){
					builder.addField(entry.getKey(),entry.getValue().toString());
				}else if(entry.getValue() instanceof Float){
					Number number = new Float((Float)entry.getValue());
					builder.addField(String.valueOf(entry.getKey()),number);
				}else if(entry.getValue() instanceof Boolean){
					builder.addField(String.valueOf(entry.getKey()),(Boolean)entry.getValue());
				}
			}
            Map<String, String> tagsMap = tags.get(i);
            //Set<String> 
            for (Entry<String, String> entry : tagsMap.entrySet()) {
				 builder.tag(String.valueOf(entry.getKey()),String.valueOf(entry.getValue()));
				 
            }
            batchPoints.point(builder.build());
        }
        influxDB.write(batchPoints);
    }
    
    /**
     * 构建Point
     *
     * @param measurement
     * @param time
     * @param fields
     * @return
     */
    public Point pointBuilder(String measurement, long time, Map<String, String> tags, Map<String, Object> fields) {
        Point point = Point.measurement(measurement).time(time, TimeUnit.MILLISECONDS).tag(tags).fields(fields).build();
        return point;
    }


    /**
     * 删除
     * 
     * @param command
     *            删除语句
     * @return 返回错误信息
     */
    public String deleteMeasurementData(String command) {
        QueryResult result = influxDB.query(new Query(command, database));
        return result.getError();
    }

    /**
     * 创建数据库
     * 
     * @param dbName
     */
    public void createDB(String dbName) {
        influxDB.createDatabase(dbName);
    }

    /**
     * 删除数据库
     * 
     * @param dbName
     */
    public void deleteDB(String dbName) {
        influxDB.deleteDatabase(dbName);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOpenurl() {
        return openurl;
    }

    public void setOpenurl(String openurl) {
        this.openurl = openurl;
    }

    public void setDatabase(String database) {
        this.database = database;
    }
    
    public String getDatabase() {
        return database;
    }

	public String getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}
    
}
