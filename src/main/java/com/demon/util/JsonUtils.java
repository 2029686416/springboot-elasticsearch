package com.demon.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import java.util.*;

/**
 * 提供不同的结构类型之间的数据转换成JSON (XML,MAP,POJO ) -- >JSON
 * 
 * @author Administrator
 * 
 */
public class JsonUtils {

	/**
	 * 将Java对象转换为JSON格式的字符串
	 * 
	 * @param javaObj
	 *            POJO,例如日志的model
	 * @return JSON格式的String字符串
	 */
	public static String getJsonStringFromJavaPOJO(Object javaObj) {
		return JSONObject.fromObject(javaObj).toString(1);
	}

	/**
	 * 将Map转换为JSON字符串
	 * 
	 * @param map
	 * @return JSON字符串
	 */
	public static String getJsonStringFromMap(Map<?, ?> map) {
		JSONObject object = JSONObject.fromObject(map);
		return object.toString();
	}

	/**
	 * 将Map转换为JSON字符串
	 * 
	 * @param map
	 * @return JSON字符串
	 */
	public static String getJsonStringFromXml(Map<?, ?> map) {
		JSONObject object = JSONObject.fromObject(map);
		return object.toString();
	}

	/**
	 * 将Map转换为JSON字符串
	 * 
	 * @param map
	 * @param ignoreList
	 *            需要忽略的实体属性数组
	 * @return
	 */
	public static String getJsonStringFromXml(Map<?, ?> map, String[] ignoreList) {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setIgnoreDefaultExcludes(false);
		jsonConfig.setExcludes(ignoreList);
		JSONObject object = JSONObject.fromObject(map, jsonConfig);
		return object.toString();
	}

	/**
	 * 
	 * <p>
	 * json字符串转map
	 * </p>
	 *
	 *
	 * @since 1.0.0
	 *
	 * @param jsonStr
	 * @return
	 */
    public static Map<String, Object> parseJSON2Map(String jsonStr){  
        Map<String, Object> map = new HashMap<String, Object>();  
        //最外层解析  
        JSONObject json = JSONObject.fromObject(jsonStr);  
        for(Object k : json.keySet()){  
            Object v = json.get(k);   
            //如果内层还是数组的话，继续解析  
            if(v instanceof JSONArray){  
                List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();  
                Iterator<JSONObject> it = ((JSONArray)v).iterator();  
                while(it.hasNext()){  
                    JSONObject json2 = it.next();  
                    list.add(parseJSON2Map(json2.toString()));  
                }  
                map.put(k.toString(), list);  
            } else {  
                map.put(k.toString(), v);  
            }  
        }  
        return map;  
    } 
    /**
     * 
     * <p>
     * json字符串转list<map>
     * </p>
     *
     *
     * @since 1.0.0
     *
     * @param jsonStr
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<Map<String, Object>> parseJSON2List(String jsonStr){  
        JSONArray jsonArr = JSONArray.fromObject(jsonStr);  
        List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();  
        Iterator<JSONObject> it = jsonArr.iterator();  
        while(it.hasNext()){  
            JSONObject json2 = it.next();  
            list.add(parseJSON2Map(json2.toString()));  
        }  
        return list;  
    } 
    
	public static void main(String[] args) {
		String jsString = "[{\"merchantId\":\"13\",\"result\":\"0\",\"reason\":\"\"},{\"merchantId\":\"14\",\"result\":\"1\",\"reason\":\"余额不足\"}]";
		List<Map<String,Object>> ls = parseJSON2List(jsString);
		System.out.println(ls);
	}
}
