package com.credentek.sme.limit.maintanance.dao;

import java.util.List;
import java.util.Map;

import com.credentek.sme.eod.model.ServiceMaster;


public interface ServiceMasterDAO {

	public List<ServiceMaster> populateGridShowData(List<ServiceMaster> serviceMaster, int from, int to);

	public Integer getListCount(List<ServiceMaster> list);

	public List<ServiceMaster> buildList(String source);

	public List<ServiceMaster> search(ServiceMaster serviceMaster, String source);

	public Map<String, String> save(ServiceMaster serviceMaster, String mode);

	public Map<String, String> author(String[] id, String mode);
	
	public Map<String, String> getgroupCDMapMap();



}