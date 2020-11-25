package com.demon.dom;


import java.util.Date;

public class RobotAlertDomDTO extends RobotAlertDom implements java.io.Serializable{
	private static final long serialVersionUID = -1L;
		
	private int page;
	private int rows;
	private String orderby;

	
//	告警类型描述
	private String gjms;
	//浏览故障信息ids
	private String ids;
	private String gjlxmc;
	
	//告警消息时间
	//private String 
	
	private Date cjsjStart;
	private Date cjsjEnd;
	
	private Date gzkssjBegin;
	private Date gzkssjEnd;
	
	private String dlwz;
	private String dlbh;
	
	private String wdTotal;
	
	public String getWdTotal() {
		return wdTotal;
	}
	public void setWdTotal(String wdTotal) {
		this.wdTotal = wdTotal;
	}
	public String getGjms() {
		return gjms;
	}
	public void setGjms(String gjms) {
		this.gjms = gjms;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public void setOrderby(String orderby){
		this.orderby = orderby;
	}
	public String getOrderby(){
		return orderby;
	}
	public String getIds() {
		return ids;
	}
	public void setIds(String ids) {
		this.ids = ids;
	}
	public String getGjlxmc() {
		return gjlxmc;
	}
	public void setGjlxmc(String gjlxmc) {
		this.gjlxmc = gjlxmc;
	}
	public Date getCjsjStart() {
		return cjsjStart;
	}
	public void setCjsjStart(Date cjsjStart) {
		this.cjsjStart = cjsjStart;
	}
	public Date getCjsjEnd() {
		return cjsjEnd;
	}
	public void setCjsjEnd(Date cjsjEnd) {
		this.cjsjEnd = cjsjEnd;
	}
	public String getDlwz() {
		return dlwz;
	}
	public void setDlwz(String dlwz) {
		this.dlwz = dlwz;
	}
	public String getDlbh() {
		return dlbh;
	}
	public void setDlbh(String dlbh) {
		this.dlbh = dlbh;
	}
	public Date getGzkssjBegin() {
		return gzkssjBegin;
	}
	public void setGzkssjBegin(Date gzkssjBegin) {
		this.gzkssjBegin = gzkssjBegin;
	}
	public Date getGzkssjEnd() {
		return gzkssjEnd;
	}
	public void setGzkssjEnd(Date gzkssjEnd) {
		this.gzkssjEnd = gzkssjEnd;
	}
	
	
	
}
