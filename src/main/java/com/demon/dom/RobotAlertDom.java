package com.demon.dom;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Document(indexName = "ipems_znxj_robotalert",type = "robotalert")
public class RobotAlertDom implements java.io.Serializable{

	private static final long serialVersionUID = 5454155825314635341L;
	
	@Id
	private Integer id;
	@Field(type = FieldType.Keyword)
	private String gjlx;
	@DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")
	@Field(type= FieldType.Date)
	private Date gzkssj;
	@DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")
	@Field(type= FieldType.Date)
	private Date gzjssj;
	@DateTimeFormat(pattern="yyyy-MM-dd hh:mm:ss")
	@Field(type = FieldType.Date)
	private Date timestamp;
	@Field(type = FieldType.Keyword)
	private String sbid;
	@Field(type = FieldType.Keyword)
	private String dlbh;
	@Field(type = FieldType.Keyword)
	private String dlwz;
	@Field(type = FieldType.Text)
	private String gjms;
	@Field(type = FieldType.Keyword)
	private String sbmc;
	@Field(type = FieldType.Keyword)
	private String cs;
	@Field(type = FieldType.Keyword)
	private String xldm;
	@Field(type = FieldType.Keyword)
	private String zddm;
	@Field(type = FieldType.Keyword)
	private String wd;
	
	public RobotAlertDom(){
	}

	public RobotAlertDom(
		Integer id
	){
		this.id = id;
	}
	
	public String getWd() {
		return wd;
	}

	public void setWd(String wd) {
		this.wd = wd;
	}

	public String getXldm() {
		return xldm;
	}

	public void setXldm(String xldm) {
		this.xldm = xldm;
	}

	public String getZddm() {
		return zddm;
	}

	public void setZddm(String zddm) {
		this.zddm = zddm;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getGjlx() {
		return gjlx;
	}

	public void setGjlx(String gjlx) {
		this.gjlx = gjlx;
	}

	public Date getGzkssj() {
		return gzkssj;
	}

	public void setGzkssj(Date gzkssj) {
		this.gzkssj = gzkssj;
	}

	public Date getGzjssj() {
		return gzjssj;
	}

	public void setGzjssj(Date gzjssj) {
		this.gzjssj = gzjssj;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getSbid() {
		return sbid;
	}

	public void setSbid(String sbid) {
		this.sbid = sbid;
	}

	public String getGjms() {
		return gjms;
	}

	public void setGjms(String gjms) {
		this.gjms = gjms;
	}
	
	public String getDlbh() {
		return dlbh;
	}

	public void setDlbh(String dlbh) {
		this.dlbh = dlbh;
	}

	public String getDlwz() {
		return dlwz;
	}

	public void setDlwz(String dlwz) {
		this.dlwz = dlwz;
	}
	
	public String getSbmc() {
		return sbmc;
	}

	public void setSbmc(String sbmc) {
		this.sbmc = sbmc;
	}

	public String getCs() {
		return cs;
	}

	public void setCs(String cs) {
		this.cs = cs;
	}

	/*
	 * @Override public String toString() { return "RobotAlertDom [id=" + id +
	 * ", gjlx=" + gjlx + ", gzkssj=" + gzkssj + ", gzjssj=" + gzjssj +
	 * ", timestamp=" + timestamp + ", sbid=" + sbid + ", dlbh=" + dlbh + ", dlwz="
	 * + dlwz + ", gjms=" + gjms + ", sbmc=" + sbmc + ", cs=" + cs + "]"; }
	 */

	public int hashCode() {
		return new HashCodeBuilder()
			.append(getId())
			.toHashCode();
	}
	
}
