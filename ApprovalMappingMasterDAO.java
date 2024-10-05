package com.credentek.sme.limit.maintanance.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.credentek.sme.eod.model.ApprovalMappingMaster;

public interface ApprovalMappingMasterDAO {

	public List<ApprovalMappingMaster> populateGridShowData(List<ApprovalMappingMaster> approvalMappingMaster, int from, int to);

	public Integer getListCount(List<ApprovalMappingMaster> list);

	public List<ApprovalMappingMaster> buildList(String source,Set<String> l_custidSet);

	public List<ApprovalMappingMaster> search(ApprovalMappingMaster approvalMappingMaster, String source,Set<String> l_custidSet);

	public Map<String, String> save(ApprovalMappingMaster approvalMappingMaster, String mode,String selectedData,int totalScore);

	public Map<String, String> author(String[] id, String mode, String reason);

	public Map<String, String> getServiceCDMap();
	public Map<String, String> getGroupNames();
	public Map<String, String> getAccountNumbers(String custId);
	public Map<String, String> buildGroupList(String Data, String custId,String tableFlg,String serviceCDId, String accno);
	//public String buildGroupListUpdate(String Data);

	public Map<String, String> getapproverName(String p_custID);
	public Map<String, String> getapproverNameForMaxCusId(Set<String> p_setList);
	public Set<String> getapprovercustomerID(String source);
	public int getAccountNumberCount(String custId);
	public String getapproverAccRel(String p_approverId);
	public String getapproverAccRelSplit(String p_approverId, String p_accno);
	public Map<String, String> saveMultiple(ApprovalMappingMaster approvalMappingMaster, String mode,String selectedData, int totalScore, String serviceCDId,String acType, String accNumber);

	
	// Added by Prabhat for multiple Account details in one time on 05-08-2019
	
	 Map<String, String> saveMultipleInSingleGo(ApprovalMappingMaster approvalMappingMaster, String mode);
	 Map<String, Object> buildGroupListForNew(String Data, String custId,
				String tableFlg, String serviceCDId, String accno, String status);


}