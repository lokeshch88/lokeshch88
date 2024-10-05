package com.credentek.sme.limit.maintanance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.credentek.sme.bulkLimit.dao.BulkLimitDAOImpl;
import com.credentek.sme.common.action.Utils;
import com.credentek.sme.database.DAOBase;
import com.credentek.sme.eod.model.ApprovalMappingMaster;
import com.credentek.sme.framework.util.ApplicationGenParameter;
import com.credentek.sme.framework.util.AuditTrail;
import com.credentek.sme.framework.util.CommonUtility;
import com.credentek.sme.framework.util.PathRead;
import com.credentek.sme.limit.maintanance.thread.SendMailToCustForAppRej;
import com.opensymphony.xwork2.ActionContext;

public class ApprovalMappingMasterDAOImpl extends DAOBase implements
		ApprovalMappingMasterDAO {

	private static Logger log = LogManager
			.getLogger(ApprovalMappingMasterDAOImpl.class);

	private Date businessDate;
	private java.sql.Date businessDateSQL;
	private SimpleDateFormat dateFormat;
	private Map session=null;
	private String userID;
	private String userName;
	private String bankID;
	private Map<String, String> l_custIdMap = new HashMap<>();
	PathRead path = PathRead.getInstance();
	public ApprovalMappingMasterDAOImpl(String param)
	{
		log.info("In ApprovalMappingMasterDAOImpl Constructor Parameter :"+param);
	}
	public ApprovalMappingMasterDAOImpl() {

		log.info("In ApprovalMappingMasterDAOImpl Constructor");
		// userID = session.get("userCode").toString();
		try {
			session = ActionContext.getContext().getSession();
			if (session!=null && session.get("userCode")!=null) {
				userID = String.valueOf(session.get("userCode"));
				dateFormat = new SimpleDateFormat(
						ApplicationGenParameter.parameterMasterList
								.get("sessionDateFormat"));
				log.info("businessDate ==="
						+ String.valueOf(session.get("businessDate")));
				if (session.get("businessDate") != null) {
					businessDate = dateFormat.parse(String.valueOf(session
							.get("businessDate")));
					businessDateSQL = new java.sql.Date(businessDate.getTime());
				}
				bankID = ApplicationGenParameter.parameterMasterList
						.get("BANK_ID");
				log.info("bankID " + bankID);
				dateFormat = new SimpleDateFormat(
						ApplicationGenParameter.parameterMasterList
								.get("sessionDateFormat"));
			}
		} catch (ParseException pe) {
			log.info("ParseException ::", pe);
		} catch (Exception e) {
			log.info("Exception ::", e);
		}
	}

	@Override
	public List<ApprovalMappingMaster> populateGridShowData(
			List<ApprovalMappingMaster> approvalMappingMasterList, int from,
			int to) {
		return approvalMappingMasterList.subList(from, to);
	}

	@Override
	public Integer getListCount(List<ApprovalMappingMaster> list) {
		return list.size();
	}

	@Override
	public List<ApprovalMappingMaster> buildList(String source,
			Set<String> l_custidSet) {

		List<ApprovalMappingMaster> approvalMappingMasterMstsList = new ArrayList<>();

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		String l_iterator = "", l_custID = "";

		try {
			connection = getConnection();
			if (source.equalsIgnoreCase("A")) {
				// pStatement = connection.prepareStatement("SELECT
				// A.SERVICECDID, A.ACCOUNT_NO,
				// B.SERVICE_DESC, A.ID, DECODE(A.AUTH_STATUS, 'A',
				// 'Authorized', 'M',
				// 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD,
				// A.MAKER_DT,
				// A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT,
				// A.ACTIVE_FLAG,
				// A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.SERVICE_CD FROM
				// COM_APPROVAL_GRP_MST_TMP A,COM_SERVICE_MST B WHERE
				// A.AUTH_STATUS != 'R'
				// AND A.MAKER_CD <> ? AND A.SERVICE_CD=B.SERVICE_CD UNION ALL
				// SELECT
				// AA.SERVICECDID, AA.ACCOUNT_NO, BB.SERVICE_DESC, AA.ID,
				// DECODE(AA.AUTH_STATUS,
				// 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R',
				// 'Rejected', ''),
				// AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD,
				// AA.AUTHOR_DT,
				// AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID,
				// 'M',AA.OPERATION_MODE,BB.SERVICE_CD FROM COM_APPROVAL_GRP_MST
				// AA,COM_SERVICE_MST BB WHERE AA.AUTH_STATUS != 'R' AND
				// AA.SERVICE_CD=BB.SERVICE_CD AND AA.ID NOT IN (SELECT C.ID
				// FROM
				// COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
				pStatement = connection
						.prepareStatement("SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.MAKER_CD <> ? AND A.SERVICE_CD=B.GROUP_ID UNION ALL SELECT AA.SERVICECDID, AA.ACCOUNT_NO, BB.GROUP_NAME, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',AA.OPERATION_MODE,BB.GROUP_ID, AA.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST AA,COM_SERVICEGROUP BB WHERE AA.AUTH_STATUS != 'R' AND AA.SERVICE_CD=BB.GROUP_ID AND AA.ID NOT IN (SELECT C.ID FROM COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
				pStatement.setString(1, userID);
			}
			else if(source.equalsIgnoreCase("P")) 
			{
				source = "A";
				pStatement = connection
						.prepareStatement("SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.MAKER_CD <> ? AND A.SERVICE_CD=B.GROUP_ID");
				pStatement.setString(1, userID);
			}
			else {

				// pStatement = connection.prepareStatement("SELECT
				// A.SERVICECDID, A.ACCOUNT_NO,
				// B.SERVICE_DESC, A.ID, DECODE(A.AUTH_STATUS, 'A',
				// 'Authorized', 'M',
				// 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD,
				// A.MAKER_DT,
				// A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT,
				// A.ACTIVE_FLAG,
				// A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.SERVICE_CD FROM
				// COM_APPROVAL_GRP_MST_TMP A,COM_SERVICE_MST B WHERE
				// A.AUTH_STATUS != 'R'
				// AND A.SERVICE_CD=B.SERVICE_CD UNION ALL SELECT
				// AA.SERVICECDID, AA.ACCOUNT_NO,
				// BB.SERVICE_DESC, AA.ID, DECODE(AA.AUTH_STATUS, 'A',
				// 'Authorized', 'M',
				// 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD,
				// AA.MAKER_DT,
				// AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT,
				// AA.AUTHOR_CAL_DT,
				// AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID,
				// 'M',AA.OPERATION_MODE,BB.SERVICE_CD FROM
				// COM_APPROVAL_GRP_MST AA,COM_SERVICE_MST BB WHERE
				// AA.AUTH_STATUS != 'R'
				// AND AA.SERVICE_CD=BB.SERVICE_CD AND AA.ID NOT IN (SELECT C.ID
				// FROM
				// COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
				pStatement = connection
						.prepareStatement("SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.SERVICE_CD=B.GROUP_ID UNION ALL SELECT AA.SERVICECDID, AA.ACCOUNT_NO, BB.GROUP_NAME, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',AA.OPERATION_MODE,BB.GROUP_ID, AA.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST AA,COM_SERVICEGROUP BB WHERE AA.AUTH_STATUS != 'R' AND AA.SERVICE_CD=BB.GROUP_ID AND AA.ID NOT IN (SELECT C.ID FROM COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
			}
			resultSet = pStatement.executeQuery();

			/* Added By 2233 For Customer Name Show on Grid */
/*			l_custidSet = getapprovercustomerID(source);
			log.info(l_custidSet.size());
			if(l_custidSet.size() > 500)
			{
				l_custIdMap = getapproverNameForMaxCusId(l_custidSet);
			}
			else
			{
				Iterator<String> iterator = l_custidSet.iterator();
				while (iterator.hasNext()) {
					l_iterator = l_iterator + iterator.next() + ",";
				}
				l_custID = l_iterator.trim().replaceAll(",$", "");
				log.info("Customer ID :: " + l_custID.toString());
				l_custIdMap = getapproverName(l_custID.toString());
			}*/

			while (resultSet.next()) {
				ApprovalMappingMaster approvalMappingMaster = new ApprovalMappingMaster();
				approvalMappingMaster.setCustomerID(resultSet.getString(1));
				//approvalMappingMaster.setCustomerName(l_custIdMap.get(resultSet.getString(1)));
				approvalMappingMaster.setAccountNo(resultSet.getString(2));
				approvalMappingMaster.setServiceCD(resultSet.getString(16));
				approvalMappingMaster.setServiceName(resultSet.getString(3));
				approvalMappingMaster.setId(resultSet.getInt(4));
				approvalMappingMaster.setStatus(resultSet.getString(5));
				approvalMappingMaster.setMaker(resultSet.getString(6));
				approvalMappingMaster
						.setMakerDate(Utils.formatDate(resultSet.getDate(7))
								+ " ["
								+ Utils.formatDate(resultSet.getDate(8))
								+ " "
								+ Utils.formatDate(resultSet.getTime(8),
										"HH:mm") + "]");
				approvalMappingMaster.setAuthor(resultSet.getString(9));
				approvalMappingMaster.setAuthorDate(Utils.formatDate(resultSet
						.getDate(10))
						+ " ["
						+ Utils.formatDate(resultSet.getDate(11))
						+ " "
						+ Utils.formatDate(resultSet.getTime(11), "HH:mm")
						+ "]");
				approvalMappingMaster.setActiveFlag(resultSet.getString(12));
				approvalMappingMaster.setLastChangeId(resultSet.getInt(13));
				approvalMappingMaster.setTableFlg(resultSet.getString(14));
				approvalMappingMaster.setFileOptionH(resultSet.getString(15));
				approvalMappingMaster.setDailyLimitS(resultSet.getString(17));
				approvalMappingMasterMstsList.add(approvalMappingMaster);
			}

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		return approvalMappingMasterMstsList;
	}

	@Override
	public List<ApprovalMappingMaster> search(
			ApprovalMappingMaster approvalMappingMaster, String source,
			Set<String> l_custidSet) {

		List<ApprovalMappingMaster> matchRecords = new ArrayList<>();

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		String l_iterator = "", l_custID = "";

		int varLocation = 0;
		int customerIDPos = 0;
		int statusPos = 0;
		int servicePos = 0;
		int makerPos = 0;
		int customerIDPos1 = 0;
		int statusPos1 = 0;
		int accountPos = 0;
		int accountPos1 = 0;
		int servicePos1 = 0;
		String sql = "";
		String authorDate = "";
		String makerDate = "";

		
		/*
		 * if(source.equalsIgnoreCase("P")) { source = "A"; pStatement = connection
		 * .prepareStatement("SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.MAKER_CD <> ? AND A.SERVICE_CD=B.GROUP_ID"
		 * ); pStatement.setString(1, userID);
		 * 
		 * sql +=
		 * "SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.SERVICE_CD=B.GROUP_ID"
		 * ;
		 * 
		 * }
		 */
		
		// Search From Temp Table
		sql += "SELECT A.SERVICECDID, A.ACCOUNT_NO, B.GROUP_NAME, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',A.OPERATION_MODE,B.GROUP_ID, A.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.SERVICE_CD=B.GROUP_ID";
		log.info("approvalMappingMaster.getCustID() "
				+ approvalMappingMaster.getCustomerID()
				+ ",approvalMappingMaster.getServiceName()="
				+ approvalMappingMaster.getServiceName());
		if (approvalMappingMaster.getCustomerID() != null
				&& !"".equalsIgnoreCase(approvalMappingMaster.getCustomerID())) {
			varLocation++;
			customerIDPos = varLocation;
			sql = sql + " AND UPPER(A.SERVICECDID) LIKE ? ";
		}

		log.info("approvalMappingMaster.getAccountNo() "
				+ approvalMappingMaster.getAccountNo());
		if (approvalMappingMaster.getAccountNo() != null
				&& !"".equalsIgnoreCase(approvalMappingMaster.getAccountNo())) {
			varLocation++;
			accountPos = varLocation;
			sql = sql + " AND UPPER(A.ACCOUNT_NO) LIKE ? ";
		}

		if (null != approvalMappingMaster.getServiceName()
				&& !"".equalsIgnoreCase(approvalMappingMaster.getServiceName())) {
			varLocation++;
			servicePos = varLocation;
			sql = sql + " AND UPPER(B.GROUP_NAME) LIKE ? ";
		}

		if (null != approvalMappingMaster.getStatus()
				&& !"".equalsIgnoreCase(approvalMappingMaster.getStatus())) {
			varLocation++;
			statusPos = varLocation;
			sql = sql + " AND UPPER(A.AUTH_STATUS) LIKE ? ";
		}

		if (source.equalsIgnoreCase("A") || source.equalsIgnoreCase("P")) {
			varLocation++;
			makerPos = varLocation;
			sql = sql + " AND A.MAKER_CD <> ? ";
		}

		sql = sql.trim();
		if (sql.endsWith("AND")) {
			sql = sql.substring(0, sql.lastIndexOf("AND"));
		}

		// Search From Main Table

		if(!source.equalsIgnoreCase("P")) 
		{
			sql = sql
					+ " UNION ALL SELECT AA.SERVICECDID, AA.ACCOUNT_NO, BB.GROUP_NAME, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',AA.OPERATION_MODE,BB.GROUP_ID, AA.DAILY_LIMIT FROM COM_APPROVAL_GRP_MST AA,COM_SERVICEGROUP BB WHERE AA.AUTH_STATUS != 'R' AND AA.SERVICE_CD=BB.GROUP_ID AND AA.ID NOT IN (SELECT C.ID FROM COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')";

			if (null != approvalMappingMaster.getCustomerID()
					&& !"".equalsIgnoreCase(approvalMappingMaster.getCustomerID())) {
				varLocation++;
				customerIDPos1 = varLocation;
				sql = sql + " AND UPPER(AA.SERVICECDID) LIKE ? ";
			}

			if (null != approvalMappingMaster.getAccountNo()
					&& !"".equalsIgnoreCase(approvalMappingMaster.getAccountNo())) {
				varLocation++;
				accountPos1 = varLocation;
				sql = sql + " AND UPPER(AA.ACCOUNT_NO) LIKE ? ";
			}

			if (null != approvalMappingMaster.getServiceName()
					&& !"".equalsIgnoreCase(approvalMappingMaster.getServiceName())) {
				varLocation++;
				servicePos1 = varLocation;
				sql = sql + " AND UPPER(BB.GROUP_NAME) LIKE ? ";
			}

			if (null != approvalMappingMaster.getStatus()
					&& !"".equalsIgnoreCase(approvalMappingMaster.getStatus())) {
				varLocation++;
				statusPos1 = varLocation;
				sql = sql + " AND UPPER(AA.AUTH_STATUS) LIKE ? ";
			}

			sql = sql.trim();
			//log.info("sql Search " + sql);
			if (sql.endsWith("WHERE")) {
				sql = sql.substring(0, sql.lastIndexOf("WHERE"));
			}

			if (sql.endsWith("AND")) {
				sql = sql.substring(0, sql.lastIndexOf("AND"));
			}

		//	log.info("ApprovalMappingMaster Search Query : " + sql);
		}
		

		try {
			connection = getConnection();
			pStatement = connection.prepareStatement(sql);
			log.info("varLocation=" + varLocation);
			customerIDPos1 = varLocation;
			if (customerIDPos > 0) {
				String custID = Utils.removeWildCardChar(approvalMappingMaster
						.getCustomerID());
				pStatement.setString(customerIDPos, "%" + custID.toUpperCase()
						+ "%");
				pStatement.setString(customerIDPos1, "%" + custID.toUpperCase()
						+ "%");
			}

			if (statusPos > 0) {
				String status = Utils.removeWildCardChar(approvalMappingMaster
						.getStatus());
				pStatement.setString(statusPos, "%" + status.toUpperCase()
						+ "%");
			}

			if (statusPos1 > 0) {
				String status = Utils.removeWildCardChar(approvalMappingMaster
						.getStatus());
			
				pStatement.setString(statusPos1, "%" + status.toUpperCase()
						+ "%");
			}
			
			
			if (accountPos > 0) {
				String accNo = Utils.removeWildCardChar(approvalMappingMaster
						.getAccountNo());
				pStatement.setString(accountPos, "%" + accNo.toUpperCase()
						+ "%");
				pStatement.setString(accountPos1, "%" + accNo.toUpperCase()
						+ "%");
			}

			if (servicePos > 0) {
				String serviceCD = Utils
						.removeWildCardChar(approvalMappingMaster
								.getServiceName());
				pStatement.setString(servicePos, "%" + serviceCD.toUpperCase()
						+ "%");
				pStatement.setString(servicePos1, "%" + serviceCD.toUpperCase()
						+ "%");
			}

			if (makerPos > 0) {
				pStatement.setString(makerPos, userID);
			}
		
			resultSet = pStatement.executeQuery();
			/* Added By 2233 For Customer Name Show on Grid */
			/*l_custidSet = getapprovercustomerID(source);
			if(l_custidSet.size() > 500)
			{
				l_custIdMap = getapproverNameForMaxCusId(l_custidSet);
			}
			else
			{
				Iterator<String> iterator = l_custidSet.iterator();
				while (iterator.hasNext()) {
					l_iterator = l_iterator + iterator.next() + ",";
				}
				l_custID = l_iterator.trim().replaceAll(",$", "");
				log.info("Customer ID :: " + l_custID.toString());
				l_custIdMap = getapproverName(l_custID.toString());
			}


*/
			while (resultSet.next()) {
				ApprovalMappingMaster approvalMappingMaster1 = new ApprovalMappingMaster();
				approvalMappingMaster1.setCustomerID(resultSet.getString(1));
				//approvalMappingMaster1.setCustomerName(l_custIdMap.get(resultSet.getString(1)));
				approvalMappingMaster1.setAccountNo(resultSet.getString(2));
				approvalMappingMaster1.setServiceCD(resultSet.getString(16));
				approvalMappingMaster1.setServiceName(resultSet.getString(3));
				approvalMappingMaster1.setId(resultSet.getInt(4));
				approvalMappingMaster1.setStatus(resultSet.getString(5));
				approvalMappingMaster1.setMaker(resultSet.getString(6));
				approvalMappingMaster1
						.setMakerDate(Utils.formatDate(resultSet.getDate(7))
								+ " ["
								+ Utils.formatDate(resultSet.getDate(8))
								+ " "
								+ Utils.formatDate(resultSet.getTime(8),
										"HH:mm") + "]");
				approvalMappingMaster1.setAuthor(resultSet.getString(9));
				approvalMappingMaster1.setAuthorDate(Utils.formatDate(resultSet
						.getDate(10))
						+ " ["
						+ Utils.formatDate(resultSet.getDate(11))
						+ " "
						+ Utils.formatDate(resultSet.getTime(11), "HH:mm")
						+ "]");
				approvalMappingMaster1.setActiveFlag(resultSet.getString(12));
				approvalMappingMaster1.setLastChangeId(resultSet.getInt(13));
				approvalMappingMaster1.setTableFlg(resultSet.getString(14));
				approvalMappingMaster1.setFileOptionH(resultSet.getString(15));
				approvalMappingMaster1.setDailyLimitS(resultSet.getString(17));
				matchRecords.add(approvalMappingMaster1);
			}
		} catch (SQLException ex) {
			log.info("",ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		return matchRecords;
	}

	@Override
	public Map<String, String> save(
			ApprovalMappingMaster approvalMappingMaster, String mode,
			String selectedData, int totalScore) {

		Map<String, String> message = new LinkedHashMap<>();
		log.info("Inside save ApprovalMappingMasterDAOImpl" + selectedData + "totalScore" + totalScore + " mode=" + mode);

		Connection connection = null;
		ResultSet resultSet = null, rs = null;
		PreparedStatement pStatement = null, ps = null;

		int count = 0;

		try {
			connection = getConnection();
			selectedData = selectedData.replaceAll("@", "#");

			if ("new".equalsIgnoreCase(mode)) {

				pStatement = connection
						.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SELECT_COUNT"));
				pStatement.setString(1, approvalMappingMaster.getCustomerID()
						.trim().toUpperCase());
				pStatement.setString(2, approvalMappingMaster.getAccountNo()
						.trim().toUpperCase());
				pStatement.setString(3, approvalMappingMaster.getServiceCD()
						.toUpperCase());

				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					count = resultSet.getInt(1);
				}
				resultSet.close();
				pStatement.close();
				if (count != 0) {
					message.put("error", path.getMessage("E1021"));
					return message;
				}

				pStatement = connection
						.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_SELECT_COUNT"));
				pStatement.setString(1, approvalMappingMaster.getCustomerID()
						.trim().toUpperCase());
				pStatement.setString(2, approvalMappingMaster.getAccountNo()
						.trim().toUpperCase());
				pStatement.setString(3, approvalMappingMaster.getServiceCD()
						.toUpperCase());
				resultSet = pStatement.executeQuery();

				while (resultSet.next()) {
					count = resultSet.getInt(1);
				}
				resultSet.close();
				pStatement.close();
				if (count != 0) {
					message.put("error", path.getMessage("E1021"));
					return message;
				}

				long runningSeqNo = 0;

				// SELECT COM_APPROVAL_GRP_MST_SEQ.NEXTVAL FROM DUAL
				pStatement = connection
						.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SEQ_NEXTVAL"));

				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					runningSeqNo = resultSet.getLong(1);
				}
				resultSet.close();
				pStatement.close();

				// write code to get running seq no

				pStatement = connection
						.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?,?,?,?)");
				pStatement.setString(1, approvalMappingMaster.getCustomerID()
						.trim());
				pStatement.setString(2, approvalMappingMaster.getAccountNo()
						.trim());
				pStatement.setString(3, approvalMappingMaster.getServiceCD()
						.trim());
				pStatement.setLong(4, runningSeqNo);
				pStatement.setString(5, "N");
				pStatement.setString(6, userID);
				pStatement.setString(7, null);
				pStatement.setDate(8, null);
				pStatement.setDate(9, null);
				pStatement.setString(10, approvalMappingMaster.getActiveFlag().trim());
				pStatement.setInt(11, 0);
				pStatement.setString(12, selectedData);
				pStatement.setInt(13, totalScore);
				log.info("approvalMappingMaster.getFileOptionH() "+ approvalMappingMaster.getFileOptionH());
				pStatement.setString(14, approvalMappingMaster.getFileOptionH());
				pStatement.setString(15, bankID);
				pStatement.setString(16, approvalMappingMaster.getDailyLimitS());
				// pStatement.setString(14,approvalMappingMaster.getFileOptionH());
				pStatement.executeUpdate();
				pStatement.close();
			}

			if ("update".equalsIgnoreCase(mode)) {

				log.info("Table Flag ++++++++++"+ approvalMappingMaster.getTableFlg());
				if ("M".equalsIgnoreCase(approvalMappingMaster.getTableFlg())) {
					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVAL_GRP_MST WHERE ID=?");
					pStatement.setInt(1, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					String custCode = "";
					boolean flag = false;
					String serCd = "";
					log.info("approvalMappingMaster.getId() "+ approvalMappingMaster.getId());
					if (resultSet.next()) {
						custCode = resultSet.getString(1);
					}
					pStatement.close();
					resultSet.close();

					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD,DAILY_LIMIT FROM COM_APPROVAL_GRP_MST WHERE SERVICECDID=? and ID <> ?");
					pStatement.setString(1, custCode);
					pStatement.setInt(2, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					while (resultSet.next()) {
						log.info("approvalMappingMaster.getAccountNo() "+ approvalMappingMaster.getAccountNo());
						log.info("accno present: " + resultSet.getString(2));
						if ((approvalMappingMaster.getAccountNo()).equals(resultSet.getString(2))&& (approvalMappingMaster.getServiceCD()).equals(resultSet.getString(3))) {
							flag = true;
							break;
						}
					}
					resultSet.close();
					pStatement.close();

					if (flag) {
						message.put("error", path.getMessage("E1021"));
						return message;
					}

					// Changes made by rasmiranjan - 04122017
					custCode = "";
					flag = false;
					serCd = "";
					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVALMAPPING_MST WHERE ID=?");
					pStatement.setInt(1, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();

					log.info("approvalMappingMaster.getId() "+ approvalMappingMaster.getId());
					if (resultSet.next()) {
						custCode = resultSet.getString(1);

					}
					pStatement.close();
					resultSet.close();

					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVALMAPPING_MST WHERE SERVICECDID=? and ID <> ?");
					pStatement.setString(1, custCode);
					pStatement.setInt(2, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					while (resultSet.next()) {
						log.info("approvalMappingMaster.getAccountNo() "+ approvalMappingMaster.getAccountNo());
						log.info("accno present: " + resultSet.getString(2));
						if ((approvalMappingMaster.getAccountNo()).equals(resultSet.getString(2))&& (approvalMappingMaster.getServiceCD()).equals(resultSet.getString(3))) {
							flag = true;
							break;
						}
					}
					resultSet.close();
					pStatement.close();

					if (flag) {
						message.put("error", path.getMessage("E1021"));
						return message;
					}
					// connection.setAutoCommit(false);
					pStatement = connection.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?, ?, ?, ?,?,?)");
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
					pStatement.setString(2, approvalMappingMaster.getAccountNo().trim());
					pStatement.setString(3, approvalMappingMaster.getServiceCD().trim());
					log.info("approvalMappingMaster.getServiceCD()="+ approvalMappingMaster.getServiceCD());
					pStatement.setInt(4, approvalMappingMaster.getId());
					pStatement.setString(5, "M");
					pStatement.setString(6, userID);
					pStatement.setString(7, null);
					pStatement.setDate(8, null);
					pStatement.setDate(9, null);
					pStatement.setString(10, approvalMappingMaster.getActiveFlag().trim());
					pStatement.setInt(11,approvalMappingMaster.getLastChangeId());
					log.info(" Approver List in Modify " + selectedData);
					pStatement.setString(12, selectedData);
					pStatement.setInt(13, totalScore);
					log.info("approvalMappingMaster.getFileOptionH() "+ approvalMappingMaster.getFileOptionH().trim());
					pStatement.setString(14,approvalMappingMaster.getFileOptionH());
					pStatement.setString(15, bankID);
					pStatement.setString(16,approvalMappingMaster.getDailyLimitS());
					pStatement.executeUpdate();
					pStatement.close();
				} else {

					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVAL_GRP_MST_TMP WHERE ID=?");
					pStatement.setInt(1, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					String custCode = "";
					boolean flag = false;
					String serCd = "";
					log.info("approvalMappingMaster.getId() "
							+ approvalMappingMaster.getId());
					if (resultSet.next()) {
						custCode = resultSet.getString(1);

					}
					pStatement.close();
					resultSet.close();

					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVAL_GRP_MST_TMP WHERE SERVICECDID=? and ID <> ?");
					pStatement.setString(1, custCode);
					pStatement.setInt(2, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					while (resultSet.next()) {
						log.info("approvalMappingMaster.getAccountNo() "+ approvalMappingMaster.getAccountNo());
						log.info("accno present: " + resultSet.getString(2));
						if ((approvalMappingMaster.getAccountNo()).equals(resultSet.getString(2))&& (approvalMappingMaster.getServiceCD()).equals(resultSet.getString(3))) {
							flag = true;
							break;
						}
					}
					resultSet.close();
					pStatement.close();

					if (flag) {
						message.put("error", path.getMessage("E1021"));
						return message;
					}

					//Changes made by rasmiranjan - 04122017
					custCode = "";
					flag = false;
					serCd = "";
					pStatement = connection
							.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVALMAPPING_MST_TMP WHERE ID=?");
					pStatement.setInt(1, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();

					log.info("approvalMappingMaster.getId() "
							+ approvalMappingMaster.getId());
					if (resultSet.next()) {
						custCode = resultSet.getString(1);

					}
					pStatement.close();
					resultSet.close();

					pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD FROM COM_APPROVALMAPPING_MST_TMP WHERE SERVICECDID=? and ID=?");
					pStatement.setString(1, custCode);
					pStatement.setInt(2, approvalMappingMaster.getId());
					resultSet = pStatement.executeQuery();
					while (resultSet.next()) {
						log.info("approvalMappingMaster.getAccountNo() "+ approvalMappingMaster.getAccountNo());
						log.info("accno present: " + resultSet.getString(2));
						if ((approvalMappingMaster.getAccountNo()).equals(resultSet.getString(2))&& (approvalMappingMaster.getServiceCD()).equals(resultSet.getString(3))) {
							flag = true;
							break;
						}
					}
					resultSet.close();
					pStatement.close();

					if (flag) {
						message.put("error", path.getMessage("E1021"));
						return message;
					}

					// connection.setAutoCommit(false);

					pStatement = connection
							.prepareStatement("UPDATE COM_APPROVAL_GRP_MST_TMP SET ACCOUNT_NO = ?, SERVICE_CD = ?, MAKER_CD = ?, MAKER_DT = sysdate, MAKER_CAL_DT = sysdate, ACTIVE_FLAG = ?,APPROVER_LIST =?,TOTAL_SCORE=?,OPERATION_MODE =?,DAILY_LIMIT=? WHERE ID = ? AND BANK_ID = ?");
					pStatement.setString(1, approvalMappingMaster.getAccountNo().trim());
					pStatement.setString(2, approvalMappingMaster.getServiceCD().trim());
					pStatement.setString(3, userID);
					pStatement.setString(4, approvalMappingMaster.getActiveFlag().trim());
					log.info(" Approver List in Modify " + selectedData);
					pStatement.setString(5, selectedData);
					pStatement.setInt(6, totalScore);
					pStatement.setString(7,approvalMappingMaster.getFileOptionH());
					log.info("approvalMappingMaster.getFileOptionH() "+ approvalMappingMaster.getFileOptionH());
					pStatement.setString(8,approvalMappingMaster.getDailyLimitS());
					pStatement.setInt(9, approvalMappingMaster.getId());
					pStatement.setString(10, bankID);
					pStatement.executeUpdate();
					pStatement.close();
					connection.close();

				}
			}
		} catch (SQLException ex) {
			log.info(ex);
			ex.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.info("message=", e);
			}
			if (ex.getErrorCode() == 1) {
				message.put("error", path.getMessage("E1021"));
			} else {
				message.put("error", path.getMessage("E1010"));
			}
			return message;
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException ee) {
				// TODO Auto-generated catch block
				log.info("message=", ee);
			}
			log.info(e);
			log.info("message=", e);
			message.put("error", "Save Record Failed : " + e.toString());
			return message;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		if ("update".equalsIgnoreCase(mode)) {
			message.put("success", path.getMessage("E1008"));
		}
		if ("new".equalsIgnoreCase(mode)) {
			message.put("success", path.getMessage("E1007"));
		}

		return message;
	}

	@Override
	public Map<String, String> author(String[] id, String mode, String reason) {

		log.info("Inside author ApprovalMappingMasterDAOImpl Mode ::"+mode);
		Connection connection = null;
		int approvalID;
		PreparedStatement preStatement = null;
		ResultSet resultSet1 = null;
		Map<String, String> message = new HashMap<>();
		Set<String> approverIdSet = new HashSet<String>();
		
		try {
			connection = getConnection();
			preStatement = connection
					.prepareStatement("select MAKER_CD from COM_APPROVAL_GRP_MST_TMP where ID=?");// to
			for (String authRowID : id) {
				try {
					approvalID = Integer.parseInt(authRowID);
					preStatement.setInt(1, approvalID);
					resultSet1 = preStatement.executeQuery();
					if (resultSet1.next()) {
						if (resultSet1.getString(1).trim()
								.equalsIgnoreCase(userID)) {
							message.put("error", path.getMessage("E1011"));
							return message;
						}
					}
					resultSet1.close();

				} catch (SQLException e) {
					log.info(e);
				} catch (Exception e) {
					log.info(e);
				} finally {
					try {
						if (resultSet1 != null) {
							resultSet1.close();
						}
					} catch (Exception e) {
						log.info(e);
					}
				}

				
				
				
				AuditTrail.checkAuditTrail(Long.parseLong(authRowID),"COM_APPROVAL_GRP_MST", mode, userID);
			}
			preStatement.close();
		} catch (SQLException e) {
			log.info(e);
		} catch (Exception e) {
			log.info(e);
		} finally {
			try {
				if (preStatement != null) {
					preStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (Exception e) {
				log.info(e);
			}
		}

		int masterID;
		String op = "";
		if ("A".equals(mode)) {
			op = "Authorize";
		} else if ("R".equals(mode)) {
			op = "Reject";
		}

		PreparedStatement pStatement1 = null;
		PreparedStatement pStatement2 = null;
		PreparedStatement pStatement3 = null;
		PreparedStatement pStatement4 = null;
		PreparedStatement pStatement5 = null;
		PreparedStatement pStatement11 = null;
		PreparedStatement pStatement21 = null;
		PreparedStatement pStatement31 = null;

		ResultSet resultSet = null, resultSet2 = null, resultSet3 = null;

		try {
			connection = getConnection();
			if ("R".equalsIgnoreCase(mode)) {
				// connection.setAutoCommit(false);
				pStatement1 = connection
						.prepareStatement("SELECT SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T',APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ? AND BANK_ID = ?");
				pStatement5 = connection
						.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_REJECT"));
				pStatement3 = connection
						.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_DELETE"));

				for (String authorID : id) {
					log.info("Inside author ApprovalMappingMasterDAOImpl authorID ::"+authorID);
					masterID = Integer.parseInt(authorID);
					pStatement1.setInt(1, masterID);
					pStatement1.setString(2, bankID);
					resultSet = pStatement1.executeQuery();
					if (resultSet.next()) {
						pStatement5.setInt(1, resultSet.getInt(13) + 1);
						pStatement5.setInt(2, masterID);
						pStatement5.setString(3, bankID);
						pStatement5.executeUpdate();
						
						// Save data in SME_LIMIT_REQUEST_REJECT_BAK table for reports
						//"SELECT SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, 
						//MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T',APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID FROM COM_APPROVALMAPPING_MST_TMP WHERE ID = ? AND BANK_ID = ?"
						
						
						// Insert into SME_LIMIT_REQUEST_REJECT_BAK (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,AUTHOR_CD,AUTHOR_DT,ACTIVE_FLAG,APPROVER_LIST,
						//OPERATION_MODE,BANK_ID,DAILY_LIMIT,UPLOADED_BR_REF_NO,LAST_CHANGE_ID,REQUEST_SOURCE,TOTAL_SCORE,UPLOADED_BR_STATUS,UPLOADED_BR_PATH,BR_REJ_REASON) values (?,?,?,?,?,?,?,?,?,?,?,?,?,3,?,?,?,?,?,?,?);
						pStatement4 = connection.prepareStatement("Insert into SME_LIMIT_REQUEST_REJECT_BAK (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,AUTHOR_CD,AUTHOR_DT,ACTIVE_FLAG,APPROVER_LIST,OPERATION_MODE,BANK_ID,DAILY_LIMIT,UPLOADED_BR_REF_NO,LAST_CHANGE_ID,REQUEST_SOURCE,TOTAL_SCORE,UPLOADED_BR_STATUS,UPLOADED_BR_PATH,BR_REJ_REASON) values (?,?,?,?,?,?,?,?,sysdate,?,?,?,?,?,?,?,?,?,?,?,?)");
						pStatement4.setString(1, resultSet.getString("SERVICECDID"));
						pStatement4.setString(2, resultSet.getString("ACCOUNT_NO"));
						pStatement4.setString(3, resultSet.getString("SERVICE_CD"));
						pStatement4.setDouble(4, masterID);
						pStatement4.setString(5, "R");
						pStatement4.setString(6, resultSet.getString("MAKER_CD"));
						pStatement4.setDate(7, resultSet.getDate("MAKER_DT"));
						pStatement4.setString(8, userID);
						pStatement4.setString(9, resultSet.getString("ACTIVE_FLAG"));
						pStatement4.setString(10, resultSet.getString("APPROVER_LIST"));
						pStatement4.setString(11, resultSet.getString("OPERATION_MODE"));
						pStatement4.setString(12, resultSet.getString("BANK_ID"));
						pStatement4.setDouble(13, resultSet.getDouble("DAILY_LIMIT"));
						pStatement4.setString(14, "NA");
						pStatement4.setInt(15, resultSet.getInt("LAST_CHANGE_ID"));
						pStatement4.setString(16, "ADMIN");
						pStatement4.setInt(17, resultSet.getInt("TOTAL_SCORE"));
						pStatement4.setString(18, "");
						pStatement4.setString(19, "NA");
						pStatement4.setString(20, reason);
						pStatement4.executeUpdate();
						pStatement4.close();
						
						// Add all approverName in Set
						String[] approverListArr = resultSet.getString("APPROVER_LIST").split("\\|");
						for(String approverSet : approverListArr) 
						{
							approverIdSet.add(approverSet.split("\\#")[0]);
						}
						
						
						
					}

					pStatement3.setInt(1, masterID);
					pStatement3.setString(2, bankID);
					pStatement3.executeUpdate();
				}
				pStatement1.close();
				pStatement5.close();
				pStatement3.close();

				pStatement1 = connection
						.prepareStatement("SELECT SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T',APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID FROM COM_APPROVALMAPPING_MST_TMP WHERE ID = ? AND BANK_ID = ?");
				pStatement5 = connection
						.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_TMP_REJECT"));
				pStatement3 = connection
						.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_DELETE"));
				for (String authorID : id) {
					masterID = Integer.parseInt(authorID);
					pStatement1.setInt(1, masterID);
					pStatement1.setString(2, bankID);
					resultSet = pStatement1.executeQuery();
					if (resultSet.next()) {
						pStatement5.setInt(1, resultSet.getInt(13) + 1);
						pStatement5.setInt(2, masterID);
						pStatement5.setString(3, bankID);
						pStatement5.executeUpdate();
					}

					pStatement3.setInt(1, masterID);
					pStatement3.setString(2, bankID);
					pStatement3.executeUpdate();
				}
				pStatement5.close();
				pStatement3.close();
				pStatement1.close();
				resultSet.close();
				// connection.commit();

			} else if ("A".equalsIgnoreCase(mode)) {
				
				synchronized (this) { // start of synchronized block
					
				
				
				pStatement1 = connection
						.prepareStatement("SELECT SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T',APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ? AND BANK_ID = ?");
				// pStatement11 =
				// connection.prepareStatement("SELECT SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T',APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID FROM COM_APPROVALMAPPING_MST_TMP WHERE ID = ? AND BANK_ID = ?");

				for (String authorID : id) {
					
					log.info("authorID ="+authorID);
					masterID = Integer.parseInt(authorID);
					pStatement1.setInt(1, masterID);
					pStatement1.setString(2, bankID);
					resultSet = pStatement1.executeQuery();

					if (resultSet.next()) {
						log.info("authorize---------------------"+ resultSet.getString(5));
						if ("Modified".equals(resultSet.getString(5))) {
							
							// Add all approverName in Set
							String[] approverListArr = resultSet.getString("APPROVER_LIST").split("\\|");
							for(String approverSet : approverListArr) 
							{
								approverIdSet.add(approverSet.split("\\#")[0]);
							}
							
							// UPDATE COM_APPROVAL_GRP_MST SET ACCOUNT_NO =
							// ?, SERVICE_CD = ?, MAKER_CD = ?, MAKER_DT = ?,
							// MAKER_CAL_DT = ?, AUTHOR_CD = ?, AUTHOR_DT = ?,
							// AUTHOR_CAL_DT = sysdate, ACTIVE_FLAG = ?,
							// LAST_CHANGE_ID = ? WHERE ID = ?
							// connection.setAutoCommit(false);
							pStatement2 = connection.prepareStatement("UPDATE COM_APPROVAL_GRP_MST SET ACCOUNT_NO = ?, SERVICE_CD = ?, MAKER_CD = ?, MAKER_DT = ?, MAKER_CAL_DT = ?, AUTHOR_CD = ?, AUTHOR_DT = sysdate, AUTHOR_CAL_DT = sysdate, ACTIVE_FLAG = ?, LAST_CHANGE_ID = ?,APPROVER_LIST=?,OPERATION_MODE=?,TOTAL_SCORE=?,DAILY_LIMIT=?,DATA_SOURCE=? WHERE ID = ? AND BANK_ID = ?");
							pStatement2.setString(1, resultSet.getString(2));
							pStatement2.setString(2, resultSet.getString(3));
							pStatement2.setString(3, resultSet.getString(6));
							pStatement2.setDate(4, resultSet.getDate(7));
							pStatement2.setTimestamp(5,
									resultSet.getTimestamp(8));
							pStatement2.setString(6, userID);
							pStatement2.setString(7, resultSet.getString(12));
							pStatement2.setInt(8, resultSet.getInt(13) + 1);
							pStatement2.setString(9, resultSet.getString(15));
							pStatement2.setString(10, resultSet.getString(16));
							pStatement2.setInt(11, resultSet.getInt(17));
							pStatement2.setLong(12, resultSet.getLong(19));
							pStatement2.setString(13, resultSet.getString(20));
							pStatement2.setInt(14, resultSet.getInt(4));
							pStatement2.setString(15, bankID);
						//	log.info("Account No :: "+ resultSet.getString(2)+"  Service_Cd ::"+resultSet.getString(3)+" Approver_list ::"+resultSet.getString(15));
							pStatement2.executeUpdate();
							pStatement2.close();

							pStatement2 = connection.prepareStatement("SELECT SERVICE_CD from COM_SERVICE_MST WHERE SERVICE_TYPE=? AND ACTIVE_FLAG='Y'");
							pStatement2.setString(1, resultSet.getString(3));
							resultSet2 = pStatement2.executeQuery();
							while (resultSet2.next()) {
								pStatement21 = connection.prepareStatement("UPDATE COM_APPROVALMAPPING_MST SET ACCOUNT_NO = ?, SERVICE_CD = ?, MAKER_CD = ?, MAKER_DT = ?, MAKER_CAL_DT = ?, AUTHOR_CD = ?, AUTHOR_DT = sysdate, AUTHOR_CAL_DT = sysdate, ACTIVE_FLAG = ?, LAST_CHANGE_ID = ?,APPROVER_LIST=?,OPERATION_MODE=?,TOTAL_SCORE=?,DATA_SOURCE=? WHERE ACCOUNT_NO = ? AND BANK_ID = ? AND SERVICE_CD=? AND SERVICECDID=? ");
								pStatement21.setString(1,resultSet.getString(2));
								pStatement21.setString(2,resultSet2.getString(1));
								pStatement21.setString(3,resultSet.getString(6));
								pStatement21.setDate(4, resultSet.getDate(7));
								pStatement21.setTimestamp(5,resultSet.getTimestamp(8));
								pStatement21.setString(6, userID);
								pStatement21.setString(7,resultSet.getString(12));
								pStatement21.setInt(8, resultSet.getInt(13) + 1);
								pStatement21.setString(9,resultSet.getString(15));
								pStatement21.setString(10,resultSet.getString(16));
								pStatement21.setInt(11, resultSet.getInt(17));
								pStatement21.setString(12, resultSet.getString(20));
								pStatement21.setString(13,resultSet.getString(2));
								pStatement21.setString(14, bankID);
								pStatement21.setString(15, resultSet2.getString(1));
								pStatement21.setString(16, resultSet.getString(1));
							//	log.info("Account No :: "+ resultSet.getString(2)+"  Service_Cd ::"+resultSet2.getString(1)+" Approver_list ::"+resultSet.getString(15)+"  Servicecdid ::"+resultSet.getString(1));
								pStatement21.executeUpdate();
								pStatement21.close();
							}
							resultSet2.close();
							// pStatement31.close();
							// pStatement21.close();
							pStatement2.close();

							// connection.commit();

						} else if ("New".equals(resultSet.getString(5))) {
							
							int l_existCount = 0;
							// Add all approverName in Set
							String[] approverListArr = resultSet.getString("APPROVER_LIST").split("\\|");
							for(String approverSet : approverListArr) 
							{
								approverIdSet.add(approverSet.split("\\#")[0]);
							}
							
							//log.info("Account No :: "+ resultSet.getString(2)+"  Service_Cd ::"+resultSet.getString(3)+" Approver_list ::"+resultSet.getString(15)+" Servicecdid ::"+resultSet.getString(1));
							// For double entry check issues in sometimes added by 2157 on 25062020
							
							// SELECT count(*),ID,APPROVER_LIST from COM_APPROVAL_GRP_MST WHERE SERVICECDID='919191' AND ACCOUNT_NO='000190600001235' and SERVICE_CD = '7' and ACTIVE_FLAG = 'Y' group by ID,APPROVER_LIST
							pStatement3 = connection
									.prepareStatement("SELECT count(*),ID,APPROVER_LIST from COM_APPROVAL_GRP_MST WHERE SERVICECDID=? AND ACCOUNT_NO=? and SERVICE_CD = ? and ACTIVE_FLAG = 'Y' group by ID,APPROVER_LIST");
							pStatement3.setString(1, resultSet.getString(1));
							pStatement3.setString(2, resultSet.getString(2));
							pStatement3.setString(3, resultSet.getString(3));
							resultSet3 = pStatement3.executeQuery();
							if (resultSet3.next()) {
								l_existCount = resultSet3.getInt(1);
								log.info("l_existCount ="+l_existCount + " ID ="+ resultSet3.getLong(2) + " APPROVER_LIST ="+resultSet3.getString(3));
							}
							resultSet3.close();
							pStatement3.close();
							
							
							if(l_existCount == 0) 
							{
								log.info("No double entry found for ");
								log.info("No double entry found for "+ resultSet.getString(2));
								// INSERT INTO COM_APPROVAL_GRP_MST(SERVICECDID,
								// ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS,
								// MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD,
								// AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG,
								// LAST_CHANGE_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?,
								// ?, ?, ?, ?)
								// connection.setAutoCommit(false);
								pStatement3 = connection
										.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, sysdate, ?,?, ?, ?, ?, ?,?)");
								pStatement3.setString(1, resultSet.getString(1));
								pStatement3.setString(2, resultSet.getString(2));
								pStatement3.setString(3, resultSet.getString(3));
								pStatement3.setLong(4, resultSet.getInt(4));
								pStatement3.setString(5, mode);
								pStatement3.setString(6, resultSet.getString(6));
								pStatement3.setDate(7, resultSet.getDate(7));
								pStatement3.setTimestamp(8,
										resultSet.getTimestamp(8));
								pStatement3.setString(9, userID);
								pStatement3.setString(10, resultSet.getString(12));
								int lastchangeID = resultSet.getInt(13);
								int ADDlastchangeID = lastchangeID + 1;
								pStatement3.setInt(11, ADDlastchangeID);
								pStatement3.setString(12, resultSet.getString(15));
								pStatement3.setString(13, resultSet.getString(16));
								pStatement3.setInt(14, resultSet.getInt(17));
								pStatement3.setString(15, bankID);
								pStatement3.setLong(16, resultSet.getLong(19));

								pStatement3.executeUpdate();
								pStatement3.close();

								pStatement31 = connection
										.prepareStatement("SELECT SERVICE_CD from COM_SERVICE_MST WHERE SERVICE_TYPE=? AND ACTIVE_FLAG='Y'");
								pStatement31.setString(1, resultSet.getString(3));
								resultSet2 = pStatement31.executeQuery();
								long runningSeqNo1 = 0;
								while (resultSet2.next()) {
									pStatement11 = connection
											.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_SEQ_NEXTVAL"));

									resultSet3 = pStatement11.executeQuery();
									if (resultSet3.next()) {
										runningSeqNo1 = resultSet3.getLong(1);
									}
									resultSet3.close();
									pStatement11.close();

									pStatement3 = connection.prepareStatement("INSERT INTO COM_APPROVALMAPPING_MST(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?)");
									pStatement3.setString(1, resultSet.getString(1));
									pStatement3.setString(2, resultSet.getString(2));
									pStatement3.setString(3,resultSet2.getString(1));
									pStatement3.setLong(4, runningSeqNo1);
									pStatement3.setString(5, mode);
									pStatement3
											.setString(6, resultSet.getString(6));
									pStatement3.setDate(7, resultSet.getDate(7));
									pStatement3.setTimestamp(8,
											resultSet.getTimestamp(8));
									pStatement3.setString(9, userID);
									pStatement3.setString(10,resultSet.getString(12));
									int lastchangeID1 = resultSet.getInt(13);
									int ADDlastchangeID2 = lastchangeID1 + 1;
									pStatement3.setInt(11, ADDlastchangeID2);
									pStatement3.setString(12,resultSet.getString(15));
									pStatement3.setString(13,resultSet.getString(16));
									pStatement3.setInt(14, resultSet.getInt(17));
									pStatement3.setString(15, bankID);
									//log.info("Account No :: "+ resultSet.getString(2)+"  Service_Cd ::"+resultSet2.getString(1)+" Approver_list ::"+resultSet.getString(15)+" Servicecdid ::"+resultSet.getString(1));
									pStatement3.executeUpdate();
									pStatement3.close();

								}
								pStatement31.close();
								// pStatement21.close();
								// pStatement2.close();
								resultSet2.close();
								// connection.commit();
							}
							
							

						} else {
						}
						pStatement4 = connection
								.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_DELETE"));
						pStatement4.setInt(1, Integer.parseInt(authorID));
						pStatement4.setString(2, bankID);
						pStatement4.executeUpdate();
						pStatement4.close();

					}
						
				}
				
				// For Mail to respective customers for Approval and Rejection of Limit Maintenance
				
				}// End of synchronized block
			}
			
			//Thread thread=new Thread(new SendMailToCustForAppRej(approverIdSet,mode,reason));
           // thread.start();
			if(!"LRJ05".equals(reason))
			{
			Thread thread=new Thread(new SendMailToCustForAppRej(approverIdSet,mode,reason));
			           thread.start();
			}
			
			
		} catch (SQLException ex) {
			log.info("SQLException ",ex);
			message.put("error", op + path.getMessage("E1016"));
			return message;
		} catch (Exception ex) {
			log.info("",ex);
			message.put("error", op + " Record Failed : " + ex.toString());
			return message;
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (resultSet1 != null) {
					resultSet1.close();
				}
				if (pStatement1 != null) {
					pStatement1.close();
				}
				if (pStatement2 != null) {
					pStatement2.close();
				}
				if (pStatement3 != null) {
					pStatement3.close();
				}
				if (pStatement4 != null) {
					pStatement4.close();
				}

				if (pStatement11 != null) {
					pStatement11.close();
				}
				if (pStatement21 != null) {
					pStatement21.close();
				}
				if (pStatement31 != null) {
					pStatement31.close();
				}

				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		if ("A".equals(mode)) {
			log.info("success", id.length + " " + path.getMessage("E1012"));
			message.put("success", id.length + " " + path.getMessage("E1012"));
		} else if ("R".equals(mode)) {
			message.put("success", id.length + " " + path.getMessage("E1013"));
			log.info("success", id.length + " " + path.getMessage("E1013"));
		}

		return message;
	}

	@Override
	public Map<String, String> getServiceCDMap() {
		Map<String, String> serviceCDMap = new LinkedHashMap<>();
		log.info("Inside getServiceCDMap in ApprovalMappingMasterDAOImpl()");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection();

			// SERVICE_CD_PICKLIST2 = SELECT SERVICE_CD, 1 FROM COM_SERVICE_MST
			pStatement = connection
					.prepareStatement(getQuery("SERVICE_CD_PICKLIST1"));

			resultSet = pStatement.executeQuery();
			String current = "", previous = "", pattern = "";
			int count = 0;
			while (resultSet.next()) {
				current = "GRP"+resultSet.getString(3)+"~"+resultSet.getString(1);
				if (count == 0) {
					pattern = resultSet.getString(2) + ",";
				} else {
					if (!current.equalsIgnoreCase(previous)) {
						serviceCDMap.put(previous,
								pattern.substring(0, pattern.length() - 1));
						pattern = resultSet.getString(2) + ",";
					} else {
						pattern += resultSet.getString(2) + ",";
					}
				}
				count += 1;
				previous = current;
			}
			serviceCDMap.put(previous,
					pattern.substring(0, pattern.length() - 1));

			for (String key : serviceCDMap.keySet()) {
				log.info(key + ":" + serviceCDMap.get(key));
			}

			serviceCDMap = sortMap(serviceCDMap);

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
			log.info("serviceCDMap ="+serviceCDMap);
		return serviceCDMap;
	}

	@Override
	public Map<String, String> getGroupNames() {
		Map<String, String> serviceCDMap = new LinkedHashMap<>();
		log.info("Inside getServiceCDMap in ApprovalMappingMasterDAOImpl()");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection();

			// SERVICE_CD_PICKLIST2 = SELECT SERVICE_CD, 1 FROM COM_SERVICE_MST
			pStatement = connection
					.prepareStatement(getQuery("SERVICE_CD_PICKLIST2"));

			resultSet = pStatement.executeQuery();
			String listString = "";
			while (resultSet.next()) {
				serviceCDMap
						.put(resultSet.getString(1), resultSet.getString(2));
			}

			serviceCDMap = sortMap(serviceCDMap);

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		return serviceCDMap;
	}

	public Map<String, String> getBranchCodes() {
		Map<String, String> serviceCDMap = new LinkedHashMap<>();
		log.info("Inside getServiceCDMap in ApprovalMappingMasterDAOImpl()");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection();

			// SERVICE_CD_PICKLIST2 = SELECT SERVICE_CD, 1 FROM COM_SERVICE_MST
			pStatement = connection
					.prepareStatement(getQuery("SERVICE_CD_PICKLIST4"));

			resultSet = pStatement.executeQuery();
			String listString = "";
			while (resultSet.next()) {
				serviceCDMap
						.put(resultSet.getString(1), resultSet.getString(2));
			}

			serviceCDMap = sortMap(serviceCDMap);

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		return serviceCDMap;
	}

	@Override
	public Map<String, String> getAccountNumbers(String custId) {
		Map<String, String> serviceCDMap = new LinkedHashMap<>();
		log.info("Inside getAccountNumbers in ApprovalMappingMasterDAOImpl()");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection();

			//SERVICE_CD_PICKLIST3=SELECT DISTINCT(COD_ACCT_NO) AS ACCOUNT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST=? AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899))
			pStatement = connection.prepareStatement(getQuery("SERVICE_CD_PICKLIST3"));
			pStatement.setString(1, custId);
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				serviceCDMap.put(resultSet.getString(1).trim(), resultSet.getString(1).trim());
			}
			serviceCDMap = sortMap(serviceCDMap);

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		return serviceCDMap;
	}

	@Override
	public int getAccountNumberCount(String custId) {
		log.info("Inside getAccountNumberCount in ApprovalMappingMasterDAOImpl()");
		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		int l_accountnoCnt = 0;
		try {
			connection = getConnection();
			// SELECT COUNT(*) FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST=? AND COD_ACCT_CUST_REL='SOW' AND ((COD_PROD BETWEEN 501 AND 599)  OR (COD_PROD BETWEEN 901 AND 999) OR (COD_PROD BETWEEN 601 AND 899))
			pStatement = connection.prepareStatement(getQuery("SELECT_COUNT_ACCNO_VW_CH_ACCT_CUST_XREF"));
			pStatement.setString(1, custId);
			resultSet = pStatement.executeQuery();
			if (resultSet.next()) {
				l_accountnoCnt = resultSet.getInt(1);
			}
			log.info("ACCOUNT NUMBER CNT ::" + l_accountnoCnt);

		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}

		return l_accountnoCnt;
	}

	@Override
	@SuppressWarnings("null")
	public Map<String, String> buildGroupList(String Data, String custId,
			String tableFlg, String serviceCDId, String accno) {

		log.info("Inside buildGroupList() of UserMasterDAOImpl " + Data
				+ "tableFlg " + tableFlg + "serviceCDId :" + serviceCDId
				+ "accno :"  + ",custId ::" + custId);
		StringBuffer groupListString = new StringBuffer();
		String tempString = "";
		String moduleDetails = "";
		boolean isCheck = false;
		boolean sowFlag = false;
		int srNo = 0;
		String moduleID = "";
		String approver_list = "";
		int score = 0;
		Connection connection = null;
		PreparedStatement pStatement = null, pStatement1 = null;
		ResultSet rset = null, rset1 = null;
		Map<String, String> codCust = new HashMap<>();
		Map<String, String> l_approveNameMap = new HashMap<>();
		Map<String, Integer> approverMap = new HashMap<>();
		Map<String, String> output = new LinkedHashMap<>();
		String custIdList = "";
		String l_approveName = "";
		String l_approveRel = "";
		String query = "", sql = "", sql1 = "";

		if (accno == null || accno.trim().equalsIgnoreCase("")
				|| accno.trim().equalsIgnoreCase("0")) {
			output.put("error", "Please select Account Number to proceed.");
			output.put("module", moduleDetails);
		} else {
			try {
				/**
				 * First of all we have to check if all approver id exists in
				 * approver_mst or not. If not exists then we have to insert it.
				 * If no approver found for both custId and account number then
				 * we have to show a popup box
				 */
				connection = getConnection();
				/* Added By 2233 For Add Aprrove Name */
				l_approveNameMap = getapproverName(custId);

				pStatement = connection
						.prepareStatement("SELECT COD_CUST,COD_ACCT_CUST_REL FROM VW_CH_ACCT_CUST_XREF WHERE COD_ACCT_NO=cast(? as char(48))");
				pStatement.setString(1, accno);
				// pStatement.setString(2, custId);
				rset = pStatement.executeQuery();
				while (rset.next()) {
					codCust.put(rset.getString(1), rset.getString(2));
					custIdList = custIdList + "," + rset.getString(1);
				}
				log.info("CodCust And CodAcctCustRel ::" + codCust);
				rset.close();
				pStatement.close();

				System.out
						.println("--------------------------------------------------------");
				pStatement = connection
						.prepareStatement("SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ? ");
				pStatement.setString(1, "N");
				pStatement.setString(2, custId);
				rset = pStatement.executeQuery();
				while (rset.next()) {
					approverMap.put(rset.getString(1), rset.getInt(3));
				}
				rset.close();
				pStatement.close();

				if (custId != null) {
					sowFlag = codCust.get(custId) == "SOW";
				}

				log.info("sowFlag=" + sowFlag);
				if (codCust.size() == 0) {
					output.put("error", "No Customer ID found");
					output.put("module", "");
					return output;

				} else {
					log.info("Customer exists");

					if (approverMap.size() == 0) {
						if (codCust.get(custId) == "SOW") {
							int maxValue = 1;
							long runningSeqNo = 0;
							// insert into approver_mst table for all custID
							log.info("insert into approver_mst table");
							pStatement = connection
									.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

							rset = pStatement.executeQuery();
							while (rset.next()) {
								runningSeqNo = rset.getLong(1);
							}
							rset.close();
							pStatement.close();
							maxValue = maxValue * 2;
							// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
							pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
							pStatement.setString(1, custId);
							pStatement.setString(2, custId);
							pStatement.setString(3, getapproverAccRel(custId));
							pStatement.setLong(4, runningSeqNo);
							pStatement.setString(5, "A");
							pStatement.setString(6, userID);
							pStatement.setString(7, null);
							pStatement.setString(8, userID);
							pStatement.setString(9, "Y");
							pStatement.setInt(10, 0);
							pStatement.setString(11, bankID);
							if (maxValue == 0) {
								pStatement.setInt(12, 1);
								maxValue = 1;
							} else {
								pStatement.setInt(12, maxValue);
							}
							pStatement.executeUpdate();
							pStatement.close();

						} else {

							int maxValue = 1;
							for (String key : codCust.keySet()) {

								long runningSeqNo = 0;
								// insert into approver_mst table for all custID
								log.info("insert into approver_mst table");
								pStatement = connection
										.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

								rset = pStatement.executeQuery();
								while (rset.next()) {
									runningSeqNo = rset.getLong(1);
								}
								rset.close();
								pStatement.close();
								maxValue = maxValue * 2;
								// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
								pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
								pStatement.setString(1, custId);
								pStatement.setString(2, key);
								// pStatement.setString(3, codCust.get(key));
								pStatement.setString(3, getapproverAccRel(key));
								pStatement.setLong(4, runningSeqNo);
								pStatement.setString(5, "A");
								pStatement.setString(6, userID);
								pStatement.setString(7, null);
								pStatement.setString(8, userID);
								pStatement.setString(9, "Y");
								pStatement.setInt(10, 0);
								pStatement.setString(11, bankID);
								if (maxValue == 0) {
									pStatement.setInt(12, 1);
									maxValue = 1;
								} else {
									pStatement.setInt(12, maxValue);
								}
								pStatement.executeUpdate();
								pStatement.close();

							}
						}

					} else {
						// check if approver id exits then insert into
						// approver_mst
						// table
						log.info("Highest value found ="
								+ Collections.max(approverMap.values()));
						Iterator<String> itr1 = approverMap.keySet().iterator();
						while (itr1.hasNext()) {
							String var = itr1.next();
						}

						int maxValue = Collections.max(approverMap.values());
						Set<String> commonKeys = new HashSet<>(
								codCust.keySet());
						String tempKey = "";
						// commonKeys.retainAll(newMap.keySet());
						commonKeys.removeAll(approverMap.keySet());
						Iterator<String> itr = commonKeys.iterator();
						// traversing over HashSet

						while (itr.hasNext()) {
							log.info("New approver_id found");
							tempKey = itr.next();
							// insert into approve_mst table
							long runningSeqNo = 0;

							// SELECT COM_APPROVE_MST_SEQ.NEXTVAL FROM DUAL
							pStatement = connection
									.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

							rset = pStatement.executeQuery();
							while (rset.next()) {
								runningSeqNo = rset.getLong(1);
							}
							rset.close();
							pStatement.close();
							maxValue = maxValue * 2;
							// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
							pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
							pStatement.setString(1, custId);
							pStatement.setString(2, tempKey);
							pStatement.setString(3, getapproverAccRel(tempKey));
							pStatement.setLong(4, runningSeqNo);
							pStatement.setString(5, "A");
							pStatement.setString(6, userID);
							pStatement.setString(7, null);
							pStatement.setString(8, userID);
							pStatement.setString(9, "Y");
							pStatement.setInt(10, 0);
							pStatement.setString(11, bankID);
							if (maxValue == 0) {
								pStatement.setInt(12, 1);
								maxValue = 1;
							} else {
								pStatement.setInt(12, maxValue);
							}
							pStatement.executeUpdate();
							pStatement.close();

						}

					}

				}

				log.info("Data " + Data);
				l_approveNameMap = getapproverName(custId);
				sql = "SELECT APPROVER_LIST from COM_APPROVAL_GRP_MST WHERE SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";

				// query="SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from
				// COM_APPROVE_MST
				// WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID =
				// ?";
				if (sowFlag) {
					query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ? and APPROVER_NAME='SOW'";
				} else {
					query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ?";
				}

				if ("New".equals(Data)) {
					log.info("New----------------");
					pStatement = connection.prepareStatement(query);
					pStatement.setString(1, "N");
					pStatement.setString(2, custId);
					rset = pStatement.executeQuery();
					while (rset.next()) {
						if (custIdList.contains(rset.getString(1))) {
							srNo++;
							tempString = "";
							isCheck = false;
							// moduleID = rset.getInt(1);
							moduleID = rset.getString(1);
							log.info("moduleID " + moduleID);
							// score =score +rset.getInt(3);
							// log.info("score "+score);
							//log.info(rset.getString(2)+" "+accno);
							l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
							if(l_approveRel == "") {
								log.info("2330 true"+custId+" "+moduleID);
								int m=new BulkLimitDAOImpl().updateApproverNameinComApprove(custId, moduleID);
								log.info("2330 true");
								
							}
							tempString = "<tr>";
							if (Data != null && !Data.equals("")) {
								String[] userModuleListArray = Data.split(",");
								for (String newUserModuleListArray : userModuleListArray) {
									if (newUserModuleListArray
											.equalsIgnoreCase(rset.getString(1))) {
										/*
										 * tempString +=
										 * "<td align='center'><input name='groupCheck' checked='checked' id='"
										 * + rset.getString(1) + "' value='" +
										 * rset.getString(1)+
										 * "' class='checkbox' type='checkbox' ></td>"
										 * ;
										 */
										if("SOW".equalsIgnoreCase(l_approveRel)) 
										{

											tempString += "<td align='center'><input name='groupCheck"+accno+"' checked='checked' id='"
													+ accno+"~"+rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' value='"
													+ rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' class='checkbox' type='checkbox' disabled></td>";
										}
										else 
										{

											tempString += "<td align='center'><input name='groupCheck"+accno+"' checked='checked' id='"
													+ accno+"~"+rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' value='"
													+ rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' class='checkbox' type='checkbox' ></td>";
										}
										
										
										isCheck = true;
									}
								}

								if (isCheck == false) {
									
									if("SOW".equalsIgnoreCase(l_approveRel)) 
									{
										tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
												+ accno+"~"+rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' class='checkbox' type='checkbox' disabled></td>";
									}
									else 
									{
										tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
												+ accno+"~"+rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' class='checkbox' type='checkbox'></td>";
									}
									
									
									/*
									 * tempString +=
									 * "<td align='center'><input name='groupCheck' id='"
									 * + rset.getString(1)+ "' value='" +
									 * rset.getString(1) + "' value='" +
									 * rset.getString(1)+
									 * "' class='checkbox' type='checkbox'></td>"
									 * ;
									 */
								}
							} else {

								if("SOW".equalsIgnoreCase(l_approveRel)) 
								{
									tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
											+ accno+"~"+rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox' disabled></td>";
								}
								else 
								{
									tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
											+ accno+"~"+rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";
								}
								
								/*
								 * tempString +=
								 * "<td align='center'><input name='groupCheck' id='"
								 * + rset.getString(1) + "' value='" +
								 * rset.getString(1)+ "' value='" +
								 * rset.getString(1)+
								 * "' class='checkbox' type='checkbox'></td>";
								 */
							}
							/* Added By 2233 For Add Aprrove Name */
							l_approveName = l_approveNameMap.get(rset
									.getString(1).toString());
							if ("null".equals(l_approveName)
									|| l_approveName == null) {
								l_approveName = "";
							}
							if("SOW".equalsIgnoreCase(l_approveRel)) 
							{
								tempString += "<td align='left'>"
										+ rset.getString(1)
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveName
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveRel
										+ "</td>"
										+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+accno
										+ rset.getString(1)
										+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
										+ "</td>"
										+ "<td id=tool"+accno+rset.getString(1)+ " align='left'><input type='text' class='inputbox' id=words"+accno+rset.getString(1)
										/* + rset.getString(1) */
										+ " placeholder='0.0' maxlength='16'  disabled='disabled' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
										+ "</td>";
							}
							else 
							{
								tempString += "<td align='left'>"
										+ rset.getString(1)
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveName
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveRel
										+ "</td>"
										+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+accno
										+ rset.getString(1)
										+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;'> </input>"
										+ "</td>"
										+ "<td id=tool"+accno+rset.getString(1)+ " align='left'><input type='text' class='inputbox' id=words"+accno+rset.getString(1)
										/* + rset.getString(1) */
										+ " placeholder='0.0' maxlength='16'  disabled='disabled' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
										+ "</td>";
							}
							
							
							// style="display:none;"
							tempString += "</tr>";
							moduleDetails += tempString;

							/*
							 * if (Data != null) {
							 * 
							 * } else { Data = ""; }
							 */
							/*
							 * moduleDetails += "<tr style='display:none' id='moduleList'><td>" + Data +
							 * "</td></tr>";
							 */
						//	log.info("moduleDetails " + moduleDetails);
						}
					}

					rset.close();
					pStatement.close();
				}

				query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID FROM COM_APPROVE_MST WHERE AUTH_STATUS !=? AND CUSTOMER_ID=? AND ACTIVE_FLAG = 'Y'";

				// log.info("Data "+Data);

				// log.info("Table Flag"+approvalMappingMaster.getTableFlg());
				if ("Update".equals(Data)) {

					log.info("tableFlg=" + tableFlg);

					if ("T".equalsIgnoreCase(tableFlg)) {
						log.info("Modified Case");
						sql1 = "SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST_TMP WHERE SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, accno);
						pStatement.setString(3, serviceCDId);
						rset = pStatement.executeQuery();
						String approveID = "", approveLimit = "";
						String[] approverData;

						if (rset.next()) {

							approver_list = rset.getString(1);

							log.info("approver_list " + approver_list);

						}
						rset.close();
						pStatement.close();

						String apprLimit = "";
						pStatement = connection.prepareStatement(query);
						pStatement.setString(1, "N");
						pStatement.setString(2, custId);
						rset = pStatement.executeQuery();
						while (rset.next()) {
							if (custIdList.contains(rset.getString(1))) {
								srNo++;
								tempString = "";
								isCheck = false;
								moduleID = rset.getString(1);
								log.info("In While--------");
								/* Added By 2233 For Add Aprrove Name */
								l_approveName = l_approveNameMap.get(rset
										.getString(1).toString());
								if ("null".equals(l_approveName)
										|| l_approveName == null) {
									l_approveName = "";
								}
								l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
								tempString = "<tr>";
								log.info("rset.getString(1) "
										+ rset.getString(1));
								if (approver_list.contains(rset.getString(1))) {
									log.info("in if------" + rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' checked='checked' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";

									String[] list = approver_list.split("\\|");

									for (int i = 0; i < list.length; i++) {
										if (list[i].contains(rset.getString(1))) {
											apprLimit = list[i].split("\\#")[1];
											log.info("apprLimit " + apprLimit);
											break;
										}
									}
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' id='input"
											+ rset.getString(1)
											+ "' value='"
											+ apprLimit
											+ "'maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								} else {
									log.info("in else-------"
											+ rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' id='input"
											+ rset.getString(1)
											+ "' value='' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								}
								log.info("tempString " + tempString);

								moduleDetails += tempString;

								if (Data != null) {

								} else {
									Data = "";
								}
								/*
								 * moduleDetails += "<tr style='display:none' id='moduleList'><td>" + Data +
								 * "</td></tr>";
								 */
							}
						}

						rset.close();
						pStatement.close();
					}

					else {
						log.info("Update----------------mASTER");
						// sql="SELECT A.APPROVER_LIST from COM_APPROVAL_GRP_MST
						// A,COM_SERVICE_MST B WHERE A.ACTIVE_FLAG = 'Y' AND
						// A.SERVICECDID = ? AND
						// A.ACCOUNT_NO = ? AND B.SERVICE_DESC = ? AND
						// A.SERVICE_CD=B.SERVICE_CD";
						log.info("query1 " +  ",custId" + custId
								+ ",accno"  + ",serviceCDId="
								+ serviceCDId);
						pStatement = connection.prepareStatement(sql);
						pStatement.setString(1, custId);
						pStatement.setString(2, accno);
						pStatement.setString(3, serviceCDId);
						rset = pStatement.executeQuery();
						String approveID = "", approveLimit = "";
						String[] approverData;

						if (rset.next()) {

							approver_list = rset.getString(1);

							log.info("approver_list " + approver_list);

						}
						rset.close();
						pStatement.close();

						String apprLimit = "";
						pStatement = connection.prepareStatement(query);
						pStatement.setString(1, "N");
						pStatement.setString(2, custId);
						rset = pStatement.executeQuery();
						while (rset.next()) {
							if (custIdList.contains(rset.getString(1).trim())) {
								srNo++;
								tempString = "";
								isCheck = false;
								moduleID = rset.getString(1);
								log.info("In While--------");
								/* Added By 2233 For Add Aprrove Name */
								l_approveName = l_approveNameMap.get(rset
										.getString(1).toString());
								if ("null".equals(l_approveName)
										|| l_approveName == null) {
									l_approveName = "";
								}
								l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
								tempString = "<tr>";
								log.info("rset.getString(1) "
										+ rset.getString(1) + ":approver_list="
										+ approver_list);
								if (approver_list.contains(rset.getString(1)
										.trim() + "#")) {
									log.info("in if------" + rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' checked='checked' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";

									String[] list = approver_list.split("\\|");

									for (int i = 0; i < list.length; i++) {
										if (list[i].contains(rset.getString(1))) {
											apprLimit = list[i].split("\\#")[1];
											log.info("apprLimit " + apprLimit);
											break;
										}
									}

									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' id='input"
											+ rset.getString(1)
											+ "' value='"
											+ apprLimit
											+ "'maxlength='16' style='width: 98%;px;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								} else {
									log.info("in else-------"
											+ rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' class='inputbox' id='input"
											+ rset.getString(1)
											+ "' value='' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								}
								log.info("tempString " + tempString);

								moduleDetails += tempString;

								if (Data != null) {

								} else {
									Data = "";
								}
								/*
								 * moduleDetails += "<tr style='display:none' id='moduleList'><td>" + Data +
								 * "</td></tr>";
								 */
							}
						}
						rset.close();

						pStatement.close();
					}

				}

				if ("View".equals(Data)) {

					log.info("In view Case >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
							+ tableFlg);
					if ("T".equalsIgnoreCase(tableFlg)) {
						log.info("Modified Case");
						sql1 = "SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST_TMP WHERE SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";
						// sql1 =
						// "SELECT A.APPROVER_LIST from COM_APPROVAL_GRP_MST
						// A,COM_SERVICE_MST_TMP B WHERE A.ACTIVE_FLAG = 'Y' AND
						// A.SERVICECDID = ? AND
						// A.ACCOUNT_NO = ? AND B.SERVICE_DESC = ? AND
						// A.SERVICE_CD=B.SERVICE_CD";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, accno);
						pStatement.setString(3, serviceCDId);
						rset = pStatement.executeQuery();
						String approveID = "", approveLimit = "";
						String[] approverData;

						if (rset.next()) {

							approver_list = rset.getString(1);

							log.info("approver_list " + approver_list);
							/*
							 * approverData =approver_list.split("\\|");
							 *
							 * for(int i =0 ; i < approverData.length;i++) {
							 *
							 * approveID=
							 * approveID+approverData[i].split("\\#")[
							 * 0]+"~"+"|"; approveLimit=
							 * approveLimit+approverData[i].split("\\#")[1]+"|";
							 *
							 * }
							 */

							// tempString +=
							// "<td align='center'><input name='groupCheck' id='"
							// +
							// rset.getString(1) + "' value='" +
							// rset.getString(1)
							// +"' class='checkbox' type='checkbox'></td>";
							// log.info("approver_list1 "+approver_list1);

						}
						rset.close();
						pStatement.close();

						String apprLimit = "";
						pStatement = connection.prepareStatement(query);
						pStatement.setString(1, "N");
						pStatement.setString(2, custId);
						rset = pStatement.executeQuery();
						while (rset.next()) {
							if (custIdList.contains(rset.getString(1))) {
								srNo++;
								tempString = "";
								isCheck = false;
								moduleID = rset.getString(1);
								log.info("In While--------");
								/* Added By 2233 For Add Aprrove Name */
								l_approveName = l_approveNameMap.get(rset
										.getString(1).toString());
								if ("null".equals(l_approveName)
										|| l_approveName == null) {
									l_approveName = "";
								}
								l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
								tempString = "<tr>";
								log.info("rset.getString(1) "
										+ rset.getString(1));
								if (approver_list.contains(rset.getString(1))) {
									log.info("in if------" + rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' checked='checked' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";

									String[] list = approver_list.split("\\|");

									for (int i = 0; i < list.length; i++) {
										if (list[i].contains(rset.getString(1))) {
											apprLimit = list[i].split("\\#")[1];
											log.info("apprLimit " + apprLimit);
											break;
										}
									}
									tempString += "<td align='center' disabled='disabled'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveName
											+ "</td>"
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='"
											+ apprLimit
											+ "'maxlength='26' disabled='disabled' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								} else {

									log.info("in else-------"
											+ rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' disabled='disabled' type='checkbox'></td>";
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text'  disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;' > </input></td>";
									tempString += "</tr>";

								}
								log.info("tempString " + tempString);

								moduleDetails += tempString;

								if (Data != null) {

								} else {
									Data = "";
								}
								/*
								 * moduleDetails +=
								 * "<tr style='display:none' disabled='disabled' id='moduleList'><td>" + Data +
								 * "</td></tr>";
								 */
							}
						}

						rset.close();
						pStatement.close();
					}

					else {
						log.info("view Case");
						sql1 = "SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST WHERE  SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";
						// sql1 =SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST
						// WHERE AND
						// SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?
						// "SELECT A.APPROVER_LIST from COM_APPROVAL_GRP_MST
						// A,COM_SERVICE_MST_TMP B WHERE A.ACTIVE_FLAG = 'Y' AND
						// A.SERVICECDID = ? AND
						// A.ACCOUNT_NO = ? AND B.SERVICE_DESC = ? AND
						// A.SERVICE_CD=B.SERVICE_CD";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, accno);
						pStatement.setString(3, serviceCDId);
						rset = pStatement.executeQuery();
						String approveID = "", approveLimit = "";
						String[] approverData;

						if (rset.next()) {

							approver_list = rset.getString(1);

							log.info("approver_list " + approver_list);
							/*
							 * approverData =approver_list.split("\\|");
							 *
							 * for(int i =0 ; i < approverData.length;i++) {
							 *
							 * approveID=
							 * approveID+approverData[i].split("\\#")[
							 * 0]+"~"+"|"; approveLimit=
							 * approveLimit+approverData[i].split("\\#")[1]+"|";
							 *
							 * }
							 */

							// tempString +=
							// "<td align='center'><input name='groupCheck' id='"
							// +
							// rset.getString(1) + "' value='" +
							// rset.getString(1)
							// +"' class='checkbox' type='checkbox'></td>";
							// log.info("approver_list1 "+approver_list1);

						}
						rset.close();
						pStatement.close();

						String apprLimit = "";
						pStatement = connection.prepareStatement(query);
						pStatement.setString(1, "N");
						pStatement.setString(2, custId);
						rset = pStatement.executeQuery();
						while (rset.next()) {
							if (custIdList.contains(rset.getString(1))) {
								srNo++;
								tempString = "";
								isCheck = false;
								moduleID = rset.getString(1);
								log.info("In While--------");
								/* Added By 2233 For Add Aprrove Name */
								l_approveName = l_approveNameMap.get(rset
										.getString(1).toString());
								if ("null".equals(l_approveName)
										|| l_approveName == null) {
									l_approveName = "";
								}
								l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
								tempString = "<tr>";
								log.info("rset.getString(1) "
										+ rset.getString(1));
								if (approver_list.contains(rset.getString(1))) {
									log.info("in if------" + rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' checked='checked' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";

									String[] list = approver_list.split("\\|");

									for (int i = 0; i < list.length; i++) {
										if (list[i].contains(rset.getString(1))) {
											apprLimit = list[i].split("\\#")[1];
											log.info("apprLimit " + apprLimit);
											break;
										}
									}

									tempString += "<td align='center' disabled='disabled'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveName
											+ "</td>"
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='"
											+ apprLimit
											+ "'maxlength='26' disabled='disabled' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								} else {

									log.info("in else-------"
											+ rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' disabled='disabled' type='checkbox'></td>";
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text'  disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;' > </input></td>";
									tempString += "</tr>";

								}
								log.info("tempString " + tempString);

								moduleDetails += tempString;

								if (Data != null) {

								} else {
									Data = "";
								}
								/*
								 * moduleDetails +=
								 * "<tr style='display:none' disabled='disabled' id='moduleList'><td>" + Data +
								 * "</td></tr>";
								 */

							}
						}

						rset.close();
						pStatement.close();
					}

				}

			} catch (SQLException ex) {
				log.info("SQLException :" + ex);
				// ex.printStackTrace();
				output.put("error", "something went wrong");
				output.put("module", moduleDetails);
				return output;

			} catch (Exception ex) {
				log.info("Exception :", ex);
				// ex.printStackTrace();
				output.put("error", "something went wrong");
				output.put("module", moduleDetails);
				return output;
			} finally {
				try {
					if (rset != null)
						rset.close();

					if (pStatement != null)
						pStatement.close();

					if (connection != null)
						connection.close();
				} catch (SQLException ex) {
					log.info(ex);
				}
			}
			log.info("End of the buildGroupList method");
			output.put("success", "");
			output.put("module", moduleDetails);
		}

		return output;
	}

	
	
	@Override
	@SuppressWarnings("null")
	public Map<String, Object> buildGroupListForNew(String Data, String custId,
			String tableFlg, String serviceCDId, String accno, String status) {

		log.info("Inside buildGroupList() of UserMasterDAOImpl " + Data
				+ "tableFlg " + tableFlg + "serviceCDId :" + serviceCDId
				+ "accno :"  + ",custId ::" + custId);
		StringBuffer groupListString = new StringBuffer();
		String tempString = "";
		String moduleDetails = "";
		boolean isCheck = false;
		boolean sowFlag = false;
		int srNo = 0;
		String moduleID = "";
		String approver_list = "";
		int score = 0;
		Connection connection = null;
		PreparedStatement pStatement = null, pStatement1 = null, pStatement2 = null;
		ResultSet rset = null, rset1 = null,rset2 = null;
		Map<String, String> codCust = new HashMap<>();
		Map<String, String> l_groupIdNameMap = new HashMap<String,String>();
		Map<String, String> l_approveNameMap = new HashMap<>();
		Map<String, Integer> approverMap = new HashMap<>();
		Map<String, Object> output = new LinkedHashMap<>();
		String custIdList = "";
		String l_approveName = "";
		String l_approveRel = "";
		String query = "", sql = "", sql1 = "";

		if (accno == null || accno.trim().equalsIgnoreCase("")
				|| accno.trim().equalsIgnoreCase("0")) {
			output.put("error", "Please select Account Number to proceed.");
			output.put("module", moduleDetails);
		} else {
			try {
				/**
				 * First of all we have to check if all approver id exists in
				 * approver_mst or not. If not exists then we have to insert it.
				 * If no approver found for both custId and account number then
				 * we have to show a popup box
				 */
				connection = getConnection();
				
				//SELECT GROUP_ID,GROUP_NAME FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'
				pStatement = connection.prepareStatement("SELECT GROUP_ID,GROUP_NAME,LIMIT_REQUIRED FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'");
				rset = pStatement.executeQuery();
				while(rset.next()) 
				{
					l_groupIdNameMap.put(rset.getString(1), rset.getString(2));
				}
				rset.close();
				pStatement.close();
				
				/* Added By 2233 For Add Aprrove Name */
				//l_approveNameMap = getapproverName(custId);

				pStatement = connection
						.prepareStatement("SELECT COD_CUST,COD_ACCT_CUST_REL FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST=? AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899) or (cod_prod between 207 and 208) or cod_prod = 237)");
				pStatement.setString(1, custId);
				// pStatement.setString(2, custId);
				rset = pStatement.executeQuery();
				while (rset.next()) {
					codCust.put(rset.getString(1), rset.getString(2));
					custIdList = custIdList + "," + rset.getString(1);
				}
				log.info("CodCust And CodAcctCustRel ::" + codCust);
				rset.close();
				pStatement.close();

				System.out
						.println("--------------------------------------------------------");
				pStatement = connection
						.prepareStatement("SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ? ");
				pStatement.setString(1, "N");
				pStatement.setString(2, custId);
				rset = pStatement.executeQuery();
				while (rset.next()) {
					approverMap.put(rset.getString(1), rset.getInt(3));
				}
				rset.close();
				pStatement.close();

				if (custId != null) {
					sowFlag = codCust.get(custId) == "SOW";
				}

				log.info("sowFlag=" + sowFlag);
				if (codCust.size() == 0) {
					output.put("error", "No Customer ID found");
					output.put("module", "");
					return output;

				} else {
					log.info("Customer exists");

					if (approverMap.size() == 0) {
						if (codCust.get(custId) == "SOW") {
							int maxValue = 1;
							long runningSeqNo = 0;
							// insert into approver_mst table for all custID
							log.info("insert into approver_mst table");
							pStatement = connection
									.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

							rset = pStatement.executeQuery();
							while (rset.next()) {
								runningSeqNo = rset.getLong(1);
							}
							rset.close();
							pStatement.close();
							maxValue = maxValue * 2;
							// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
							pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
							pStatement.setString(1, custId);
							pStatement.setString(2, custId);
							pStatement.setString(3, getapproverAccRel(custId));
							pStatement.setLong(4, runningSeqNo);
							pStatement.setString(5, "A");
							pStatement.setString(6, userID);
							pStatement.setString(7, null);
							pStatement.setString(8, userID);
							pStatement.setString(9, "Y");
							pStatement.setInt(10, 0);
							pStatement.setString(11, bankID);
							if (maxValue == 0) {
								pStatement.setInt(12, 1);
								maxValue = 1;
							} else {
								pStatement.setInt(12, maxValue);
							}
							pStatement.executeUpdate();
							pStatement.close();

						} else {

							int maxValue = 1;
							for (String key : codCust.keySet()) {

								long runningSeqNo = 0;
								// insert into approver_mst table for all custID
								log.info("insert into approver_mst table");
								pStatement = connection
										.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

								rset = pStatement.executeQuery();
								while (rset.next()) {
									runningSeqNo = rset.getLong(1);
								}
								rset.close();
								pStatement.close();
								maxValue = maxValue * 2;
								// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
								pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
								pStatement.setString(1, custId);
								pStatement.setString(2, key);
								// pStatement.setString(3, codCust.get(key));
								pStatement.setString(3, getapproverAccRel(key));
								pStatement.setLong(4, runningSeqNo);
								pStatement.setString(5, "A");
								pStatement.setString(6, userID);
								pStatement.setString(7, null);
								pStatement.setString(8, userID);
								pStatement.setString(9, "Y");
								pStatement.setInt(10, 0);
								pStatement.setString(11, bankID);
								if (maxValue == 0) {
									pStatement.setInt(12, 1);
									maxValue = 1;
								} else {
									pStatement.setInt(12, maxValue);
								}
								pStatement.executeUpdate();
								pStatement.close();

							}
						}

					} else {
						// check if approver id exits then insert into
						// approver_mst
						// table
						log.info("Highest value found ="
								+ Collections.max(approverMap.values()));
						Iterator<String> itr1 = approverMap.keySet().iterator();
						while (itr1.hasNext()) {
							String var = itr1.next();
						}

						int maxValue = Collections.max(approverMap.values());
						Set<String> commonKeys = new HashSet<>(
								codCust.keySet());
						String tempKey = "";
						// commonKeys.retainAll(newMap.keySet());
						commonKeys.removeAll(approverMap.keySet());
						Iterator<String> itr = commonKeys.iterator();
						// traversing over HashSet

						while (itr.hasNext()) {
							log.info("New approver_id found");
							tempKey = itr.next();
							// insert into approve_mst table
							long runningSeqNo = 0;

							// SELECT COM_APPROVE_MST_SEQ.NEXTVAL FROM DUAL
							pStatement = connection
									.prepareStatement(getQuery("COM_APPROVE_MST_SEQ_NEXTVAL"));

							rset = pStatement.executeQuery();
							while (rset.next()) {
								runningSeqNo = rset.getLong(1);
							}
							rset.close();
							pStatement.close();
							maxValue = maxValue * 2;
							// INSERT INTO COM_APPROVE_MST(CUSTOMER_ID, APPROVER_CD, APPROVER_NAME, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,APPROVER_ID) VALUES(?, ?, ?, ?, ?, ?, sysdate, ?, ?, sysdate, sysdate, ?, ?,?,?)
							pStatement = connection.prepareStatement(getQuery("COM_APPROVE_MST_INSERT_NEW"));
							pStatement.setString(1, custId);
							pStatement.setString(2, tempKey);
							pStatement.setString(3, getapproverAccRel(tempKey));
							pStatement.setLong(4, runningSeqNo);
							pStatement.setString(5, "A");
							pStatement.setString(6, userID);
							pStatement.setString(7, null);
							pStatement.setString(8, userID);
							pStatement.setString(9, "Y");
							pStatement.setInt(10, 0);
							pStatement.setString(11, bankID);
							if (maxValue == 0) {
								pStatement.setInt(12, 1);
								maxValue = 1;
							} else {
								pStatement.setInt(12, maxValue);
							}
							pStatement.executeUpdate();
							pStatement.close();

						}

					}

				}

				log.info("Data " + Data);
				//added by 2330 to ensure first all the company id and asu id is inserted in com approve mst then only getapproverName is called so that no empty name is called
				l_approveNameMap = getapproverName(custId);
				sql = "SELECT APPROVER_LIST from COM_APPROVAL_GRP_MST WHERE SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";

				// query="SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from
				// COM_APPROVE_MST
				// WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID =
				// ?";
				if (sowFlag) {
					query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ? and APPROVER_NAME='SOW'";
				} else {
					query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ?";
				}

				if ("New".equals(Data)) {
					log.info("New----------------");
					pStatement = connection.prepareStatement(query);
					pStatement.setString(1, "N");
					pStatement.setString(2, custId);
					rset = pStatement.executeQuery();
					while (rset.next()) {
						if (custIdList.contains(rset.getString(1))) {
							srNo++;
							tempString = "";
							isCheck = false;
							// moduleID = rset.getInt(1);
							moduleID = rset.getString(1);
							log.info("moduleID " + moduleID);
							// score =score +rset.getInt(3);
							// log.info("score "+score);
							//log.info(rset.getString(2)+" "+accno);
							l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
							if(l_approveRel == "") {
								log.info("2330 true"+custId+" "+moduleID);
								int m=new BulkLimitDAOImpl().updateApproverNameinComApprove(custId, moduleID);
								log.info("2330 true");
								
							}
							tempString = "<tr>";
							if (Data != null && !Data.equals("")) {
								String[] userModuleListArray = Data.split(",");
								for (String newUserModuleListArray : userModuleListArray) {
									if (newUserModuleListArray
											.equalsIgnoreCase(rset.getString(1))) {
										/*
										 * tempString +=
										 * "<td align='center'><input name='groupCheck' checked='checked' id='"
										 * + rset.getString(1) + "' value='" +
										 * rset.getString(1)+
										 * "' class='checkbox' type='checkbox' ></td>"
										 * ;
										 */
										
										if("SOW".equalsIgnoreCase(l_approveRel)) 
										{
											tempString += "<td align='center'><input name='groupCheck"+accno+"' checked='checked' id='"
													+ accno+"~"+rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' value='"
													+ rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' class='checkbox' type='checkbox' disabled></td>";
										}
										else 
										{
											tempString += "<td align='center'><input name='groupCheck"+accno+"' checked='checked' id='"
													+ accno+"~"+rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' value='"
													+ rset.getString(1)
													+ "~"
													+ rset.getInt(3)
													+ "' class='checkbox' type='checkbox' ></td>";
										}
										
										
										isCheck = true;
									}
								}

								if (isCheck == false) {
									
									if("SOW".equalsIgnoreCase(l_approveRel)) 
									{
										tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
												+ accno+"~"+rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' class='checkbox' type='checkbox' disabled></td>";
									}
									else 
									{
										tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
												+ accno+"~"+rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' value='"
												+ rset.getString(1)
												+ "~"
												+ rset.getInt(3)
												+ "' class='checkbox' type='checkbox'></td>";
									}
									
									
									/*
									 * tempString +=
									 * "<td align='center'><input name='groupCheck' id='"
									 * + rset.getString(1)+ "' value='" +
									 * rset.getString(1) + "' value='" +
									 * rset.getString(1)+
									 * "' class='checkbox' type='checkbox'></td>"
									 * ;
									 */
								}
							} else {

								
								if("SOW".equalsIgnoreCase(l_approveRel)) 
								{
									tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
											+ accno+"~"+rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox' disabled></td>";
								}
								else 
								{
									tempString += "<td align='center'><input name='groupCheck"+accno+"' id='"
											+ accno+"~"+rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";
								}
								
								
								/*
								 * tempString +=
								 * "<td align='center'><input name='groupCheck' id='"
								 * + rset.getString(1) + "' value='" +
								 * rset.getString(1)+ "' value='" +
								 * rset.getString(1)+
								 * "' class='checkbox' type='checkbox'></td>";
								 */
							}
							/* Added By 2233 For Add Aprrove Name */
							l_approveName = l_approveNameMap.get(rset
									.getString(1).toString());
							if ("null".equals(l_approveName)
									|| l_approveName == null) {
								l_approveName = "";
							}

							// Disabled SOW relation as per Amit request on 27062020 by 2157
							if("SOW".equalsIgnoreCase(l_approveRel)) 
							{
								tempString += "<td align='left'>"
										+ rset.getString(1)
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveName
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveRel
										+ "</td>"
										+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+accno
										+ rset.getString(1)
										+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
										+ "</td>"
										+ "<td id=tool"+accno+rset.getString(1)+ " align='left'><input type='text' class='inputbox' id=words"+accno+rset.getString(1)
										/* + rset.getString(1) */
										+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
										+ "</td>";
							}
							else 
							{
								tempString += "<td align='left'>"
										+ rset.getString(1)
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveName
										+ "</td>"
										+ "<td align='left'>"
										+ l_approveRel
										+ "</td>"
										+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+accno
										+ rset.getString(1)
										+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;'> </input>"
										+ "</td>"
										+ "<td id=tool"+accno+rset.getString(1)+ " align='left'><input type='text' class='inputbox' id=words"+accno+rset.getString(1)
										/* + rset.getString(1) */
										+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
										+ "</td>";
							}
							
							// style="display:none;"
							tempString += "</tr>";
							moduleDetails += tempString;

							/*
							 * if (Data != null) {
							 * 
							 * } else { Data = ""; }
							 */
							/*
							 * moduleDetails += "<tr style='display:none' id='moduleList'><td>" + Data +
							 * "</td></tr>";
							 */
							log.info("moduleDetails " + moduleDetails);
						}
					}

					rset.close();
					pStatement.close();
				}

				query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID FROM COM_APPROVE_MST WHERE AUTH_STATUS !=? AND CUSTOMER_ID=? AND ACTIVE_FLAG = 'Y'";

				// log.info("Data "+Data);

				// log.info("Table Flag"+approvalMappingMaster.getTableFlg());
				if ("Update".equals(Data)) {

					log.info("In update Case >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
							+ tableFlg + "   "+accno);
					
					PreparedStatement pStatment1 = null;
					ResultSet rSet1 = null;
					if ("T".equalsIgnoreCase(tableFlg) || "M".equalsIgnoreCase(tableFlg)) {
						
						log.info("Modified Case");
						
						
						if("Authorized".equals(status)) 
						{
							// Go to main master table
							sql1 = "SELECT ACCOUNT_NO,SERVICE_CD,APPROVER_LIST,DAILY_LIMIT,OPERATION_MODE,ACTIVE_FLAG FROM COM_APPROVAL_GRP_MST WHERE SERVICECDID = ?"; //AND ACTIVE_FLAG = 'Y'"; //and ACCOUNT_NO = ?";
						}
						else 
						{
							// Go to temp table 
							sql1 = "SELECT ACCOUNT_NO,SERVICE_CD,APPROVER_LIST,DAILY_LIMIT,OPERATION_MODE,ACTIVE_FLAG FROM COM_APPROVAL_GRP_MST_TMP WHERE SERVICECDID = ?"; //and ACCOUNT_NO = ?";
						}
						
						
						
						Map<String,String> serviceDataMap = getServiceCDMap();
						Map<String,String> insertedAccountNum = new HashMap<String,String>(); 
						Map<String,String> insertedGroupIds = new HashMap<String,String>();
						Map<String,String> insertedGroupLimit = new HashMap<String,String>();
						Map<String,String> insertedApproverIdForGrp = new HashMap<String,String>();
						Map<String,String> insertedMopForGrp = new HashMap<String,String>();
						Map<String,String> insertedGrpHtml = new HashMap<String,String>();
						
						Map<String,String> exculudedGroupIds = new HashMap<String, String>();
						
						Map<String,String> unmappedAccountServiceMap = new HashMap<String, String>();
						Map<String,String> accountGroupActiveFlagMap = new HashMap<String, String>();
						
						Map<String,String> insertedMopFlagForGrp = new HashMap<String, String>();
						
						Map<String,String> insertedApproverFlagForGrp = new HashMap<String, String>();
						
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						//pStatement.setString(2, accno);
						rset = pStatement.executeQuery();
						
						
						String l_htmlStrForSerNAppFirst = "<table class='TFtable' style='width:100%;height:300px'>"+
								"<tr id='transactionDiv3'>"+
								"<td id='serviceCDList' style='width:50%;'>"+
								
									"<table class='TFtable' style='width:100%;height:300px'>"+
									"<thead><tr style='margin-bottom:3px;background-color:#00518F;height:20px;'>"+
									"<th></th>"+
									"<th style='color:white;text-align:center'>Group Service Name</th>"+
									"<th style='color:white;text-align:center;'>Daily Limit (In Rs.)</th>"+
									"<th style='color:white;text-align:center;'>Amount (In Words)</th>"+
									"</tr></thead>";
						/*
						 * String l_htmlStrForSerNAppLast = "</table>"+ "</td>"+
						 * 
						 * "<td id='approverCDList1'>"+ "<div id='approverCDList'>"+
						 * "<table style='border:1px solid grey;width:100%;height:194px'>"+ "<thead>"+
						 * "<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"
						 * + "<th style='color:white;text-align:center'>Approver ID</th>"+
						 * "<th style='color:white;text-align:center'>Approver Name</th>"+
						 * "<th style='color:white;text-align:center'>Approver Role</th>"+
						 * "<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
						 * "<th style='color:white;text-align:center'>Amount(In Words)</th>"+
						 * "</tr></thead>"+
						 * 
						 * "<tbody id=transbody>$lastChecked$"+ "</tbody>"+ "</table>"+ "</div>"+
						 * "</td>"+
						 * 
						 * "</tr>$l_mopApproverStr$"+ "</table>";
						 */
						

						
						
						while (rset.next()) {

							approver_list = rset.getString(3);

							log.info("approver_list " + approver_list);
							insertedAccountNum.put(rset.getString(1).trim(),rset.getString(1).trim());
							insertedGroupLimit.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(4));
					 		insertedApproverIdForGrp.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(3));
							accountGroupActiveFlagMap.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(6));
							String l_idData = rset.getString(1).trim()+"~"+rset.getString(2).trim();
							String l_mopApproverStr = "";
							String l_mopFlag = "";
							if("O".equalsIgnoreCase(rset.getString(5))) 
							{
								l_mopFlag = "O";
								l_mopApproverStr = 
										"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
										" class='fileOption' checked='checked'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
										" class='fileOption' /> <span style='margin-left: 6px;'><label for=Pattern2"+l_idData.replace("~", "GRP")+"><span name='All of the above'>All of the above</span></label></span></div></td>"+
										"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
										"</td>"
										;
							}
							else 
							{
								l_mopFlag = "J";
								l_mopApproverStr = 
										"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
										" class='fileOption'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></label></span></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
										" class='fileOption' checked='checked' /> <span style='margin-left: 6px;'><label for=Pattern2"+l_idData.replace("~", "GRP")+" checked ><span name='All of the above'>All of the above</span></label></span></div></td>"+
										"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
										"</td>"
										;
							}
							
							
							
							insertedMopForGrp.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),l_mopApproverStr);
							insertedMopFlagForGrp.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(), l_mopFlag);
							
							
							
							if(insertedGroupIds.containsKey(rset.getString(1).trim())) 
							{
								String getValue = insertedGroupIds.get(rset.getString(1).trim());
								insertedGroupIds.put(rset.getString(1).trim(),getValue+"~"+rset.getString(2).trim());
							}
							else 
							{
								insertedGroupIds.put(rset.getString(1).trim(),rset.getString(2).trim());
							}
							
							unmappedAccountServiceMap.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(), rset.getString(6));
							
						}
						
						rset.close();
						pStatement.close();
						
						
						Map<String,String> insertedAccountNonAddedMap = new HashMap<String,String>();
						// Code for set entry of non added account in first time
						sql1 = "SELECT DISTINCT(TRIM(COD_ACCT_NO)) AS ACCOUNT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST=? AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899) or (cod_prod between 207 and 208) or cod_prod = 237 ) AND TRIM(COD_ACCT_NO) not in (select distinct ACCOUNT_NO from COM_APPROVAL_GRP_MST where SERVICECDID = ?) ";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, custId);
						//pStatement.setString(2, accno);
						rset = pStatement.executeQuery();
						
						while(rset.next()) 
						{
							if(!insertedAccountNum.containsKey(rset.getString(1).trim())) 
							{
								insertedAccountNonAddedMap.put(rset.getString(1).trim(),rset.getString(1).trim());
								String l_idData = "";
								String l_mopApproverStr = "";
								String grpId = "";
								Iterator serviceIdsItr = serviceDataMap.entrySet().iterator();
								while(serviceIdsItr.hasNext())
								{
									Map.Entry<String, String> keyValueId = (Map.Entry<String, String>)serviceIdsItr.next();
									grpId = keyValueId.getKey().split("~")[0].replace("GRP", "");
									 l_idData = rset.getString(1).trim()+"~"+grpId;
									 l_mopApproverStr = 
											"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
											" class='fileOption' checked='checked'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
											" class='fileOption' /> <span style='margin-left: 6px;'><label for=Pattern2"+l_idData.replace("~", "GRP")+"><span name='All of the above'>All of the above</span></label></span></div></td>"+
											"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
											"</td>"
											;
									 
									 
									 	insertedMopForGrp.put(l_idData,l_mopApproverStr);
										insertedApproverIdForGrp.put(l_idData, "#");
									
								}	
								
								
								
								
							}
							
						}
						
						rset.close();
						pStatement.close();
						
						
						Iterator accountNoGrpItr1 = insertedGroupIds.entrySet().iterator();
						while(accountNoGrpItr1.hasNext()) 
						{
							Map.Entry<String, String> keyValueData = (Map.Entry<String, String>)accountNoGrpItr1.next();
							String l_groupIds = keyValueData.getValue();
							String getValue = "";
							String grpId = "";
							String l_idData = "";
							Iterator serviceIdsItr = serviceDataMap.entrySet().iterator();
							while(serviceIdsItr.hasNext())
							{
								Map.Entry<String, String> keyValueId = (Map.Entry<String, String>)serviceIdsItr.next();
								grpId = keyValueId.getKey().split("~")[0].replace("GRP", "");
								if(!l_groupIds.contains(grpId)) 
								{
									l_idData = keyValueData.getKey()+"~"+grpId;
									String l_mopApproverStr = 
											"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
											" class='fileOption' checked='checked'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
											" class='fileOption' /> <span style='margin-left: 6px;'><label for=Pattern2"+l_idData.replace("~", "GRP")+"><span name='All of the above'>All of the above</span></label></span></div></td>"+
											"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
											"</td>"
											;

									insertedMopForGrp.put(l_idData,l_mopApproverStr);
									insertedApproverIdForGrp.put(l_idData, "#");
									
									
									if(exculudedGroupIds.containsKey(keyValueData.getKey())) 
									{
										getValue = exculudedGroupIds.get(keyValueData.getKey());
										if(!getValue.contains(grpId)) 
										{
											getValue = getValue  +"~" + grpId;
										}
										
										exculudedGroupIds.put(keyValueData.getKey(),getValue);
									}
									else 
									{
										exculudedGroupIds.put(keyValueData.getKey(),grpId);
									}
									
									
									
								}
							
							}
							
							
							
						}
						
						
						insertedAccountNum = sortMap(insertedAccountNum);
						insertedGroupLimit = sortMap(insertedGroupLimit);
						insertedGroupIds = sortMap(insertedGroupIds);
						exculudedGroupIds = sortMap(exculudedGroupIds);
						
						//log.info("exculudedGroupIds ="+exculudedGroupIds);
						
						Map<String,String> approverDtlMap = new HashMap<String, String>();
						// To add approver for view data
						query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ?";
						pStatement1 = connection.prepareStatement(query);
						pStatement1.setString(1, "N");
						pStatement1.setString(2, custId);
						rset1 = pStatement1.executeQuery();
						while (rset1.next()) {
							approverDtlMap.put(rset1.getString(1), rset1.getString(2)+"~"+rset1.getString(3));
						}
						rset1.close();
						pStatement1.close();
						
						Iterator accountAppGrpItr = insertedApproverIdForGrp.entrySet().iterator();
						while(accountAppGrpItr.hasNext()) 
						{
							Map.Entry l_keyValueAppEntry = (Map.Entry)accountAppGrpItr.next(); 
							String l_accountGrpNo = String.valueOf(l_keyValueAppEntry.getKey());
							String l_approverList = String.valueOf(l_keyValueAppEntry.getValue());
							
							String[] l_appLimitArr =  l_approverList.split("\\|");
							
							String l_appHtmlStr = "";
							
							Iterator approverItr = approverDtlMap.entrySet().iterator();
							while(approverItr.hasNext()) 
							{
								 
								Map.Entry l_keyValueDbAppEntry = (Map.Entry)approverItr.next(); 
								l_approveRel = getapproverAccRelSplit(String.valueOf(l_keyValueDbAppEntry.getValue()).split("~")[0], l_accountGrpNo.split("~")[0]);
								l_approveName = l_approveNameMap.get(String.valueOf(l_keyValueDbAppEntry.getKey()));
								if(l_approverList.contains(String.valueOf(l_keyValueDbAppEntry.getKey())+"#")) 
								{
									l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
									
									// Add checked data here
									String l_appLimit = "";
									
									for(int i = 0;i<l_appLimitArr.length;i++) 
									{
										if(l_appLimitArr[i].contains(String.valueOf(l_keyValueDbAppEntry.getKey()))) 
										{
											l_appLimit = l_appLimitArr[i].split("\\#")[1];
										}
									}
									
									String l_amtInWords = CommonUtility.convertAmountToWords(l_appLimit+"$");
									l_appLimit = CommonUtility.convertAmtIntoComma(l_appLimit);
									
									
									
									
									if("SOW".equalsIgnoreCase(l_approveRel)) 
									{
										l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_accountGrpNo.split("~")[0]+" checked='checked' id='"
												+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
												+"~"
												+ l_accountGrpNo.split("~")[1]
												+ "' value='"
												+ l_keyValueDbAppEntry.getKey()
												+ "~"
												+ l_accountGrpNo.split("~")[1]
												+ "' class='checkbox' type='checkbox' disabled></td>";
										
										
										l_appHtmlStr += "<td align='left'>"
												+ l_keyValueDbAppEntry.getKey()
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveName
												+ "</td>"
												+ "<td align='left' >"
												+ l_approveRel
												+ "</td>"
												+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' name=words"+l_accountGrpNo.split("~")[0]+" onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
												+ l_keyValueDbAppEntry.getKey()
												+ "' value = "+l_appLimit+" placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
												+ "</td>"
												+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left' title='"+l_amtInWords+"'><input type='text' class='inputbox' name=words"+l_accountGrpNo.split("~")[0]+" id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
												/* + rset.getString(1) */
												+ " value='"+l_amtInWords+"' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
												+ "</td>";
									} 
									else 
									{
										l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_accountGrpNo.split("~")[0]+" checked='checked' id='"
												+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
												+"~"
												+ l_accountGrpNo.split("~")[1]
												+ "' value='"
												+ l_keyValueDbAppEntry.getKey()
												+ "~"
												+ l_accountGrpNo.split("~")[1]
												+ "' class='checkbox' type='checkbox' ></td>";
										
										
										l_appHtmlStr += "<td align='left'>"
												+ l_keyValueDbAppEntry.getKey()
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveName
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveRel
												+ "</td>"
												+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' name=words"+l_accountGrpNo.split("~")[0]+" onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
												+ l_keyValueDbAppEntry.getKey()
												+ "' value = "+l_appLimit+" placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' > </input>"
												+ "</td>"
												+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left' title='"+l_amtInWords+"'><input type='text' class='inputbox' name=words"+l_accountGrpNo.split("~")[0]+" id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
												/* + rset.getString(1) */
												+ " value='"+l_amtInWords+"' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
												+ "</td>";
									}
									
									
									// To add approver id with amount 
									insertedApproverFlagForGrp.put(l_accountGrpNo, l_keyValueDbAppEntry.getKey()+"|"+l_appLimit);
								}
								else
								{
									
									
									
								//	log.info("l_keyValueDbAppEntry.getValue()="+l_keyValueDbAppEntry.getValue());
								//	log.info("l_accountGrpNo.split()[0]="+l_accountGrpNo.split("~")[0]);
									// Add unchecked data
									// Add only unchecked approver id of that particular account
									if(String.valueOf(l_keyValueDbAppEntry.getValue()).contains(l_accountGrpNo.split("~")[0])) 
									{
										
										l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
										
										if("SOW".equalsIgnoreCase(l_approveRel)) 
										{
											l_appHtmlStr += "<td align='center'><input name='groupCheck"+l_accountGrpNo.split("~")[0]+"' id='"
													+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
													+ "~"
													+ l_accountGrpNo.split("~")[1]
													+ "' value='"
													+ l_keyValueDbAppEntry.getKey()
													+ "~"
													+ l_accountGrpNo.split("~")[1]
													+ "' class='checkbox' type='checkbox' disabled></td>";
											
											
											l_appHtmlStr += "<td align='left'>"
													+ l_keyValueDbAppEntry.getKey()
													+ "</td>"
													+ "<td align='left'>"
													+ l_approveName
													+ "</td>"
													+ "<td align='left' >"
													+ l_approveRel
													+ "</td>"
													+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
													+ l_keyValueDbAppEntry.getKey()
													+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
													+ "</td>"
													+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left'><input type='text' class='inputbox' id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
													/* + rset.getString(1) */
													+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
													+ "</td>";
										}
										else 
										{
											l_appHtmlStr += "<td align='center'><input name='groupCheck"+l_accountGrpNo.split("~")[0]+"' id='"
													+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
													+ "~"
													+ l_accountGrpNo.split("~")[1]
													+ "' value='"
													+ l_keyValueDbAppEntry.getKey()
													+ "~"
													+ l_accountGrpNo.split("~")[1]
													+ "' class='checkbox' type='checkbox'></td>";
											
											
											l_appHtmlStr += "<td align='left'>"
													+ l_keyValueDbAppEntry.getKey()
													+ "</td>"
													+ "<td align='left'>"
													+ l_approveName
													+ "</td>"
													+ "<td align='left'>"
													+ l_approveRel
													+ "</td>"
													+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
													+ l_keyValueDbAppEntry.getKey()
													+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;'> </input>"
													+ "</td>"
													+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left'><input type='text' class='inputbox' id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
													/* + rset.getString(1) */
													+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
													+ "</td>";
										}
										
										
										
										l_appHtmlStr += "</tr>";
									}
								}
								
								
							}
							
							
							
							insertedApproverIdForGrp.put(l_accountGrpNo, l_appHtmlStr);
							
						}
							
							// To add unchecked data for all new account number	
						
						//SELECT COD_CUST,COD_ACCT_CUST_REL from VW_CH_ACCT_CUST_XREF WHERE COD_ACCT_NO = cast('111381311111321' as char(48)) AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899))
						Iterator insertedAccountNonAddedMapItr = insertedAccountNonAddedMap.entrySet().iterator();
						while(insertedAccountNonAddedMapItr.hasNext()) 
						{
							Map.Entry l_keyValueAccEntry = (Map.Entry)insertedAccountNonAddedMapItr.next(); 
							
							String l_nonAddedAccNo = (String)l_keyValueAccEntry.getKey();
							
							String l_appHtmlStr = "";
							
							// To add approver for view data
							query = "SELECT COD_CUST,COD_ACCT_CUST_REL from VW_CH_ACCT_CUST_XREF WHERE COD_ACCT_NO = cast(? as char(48)) AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899) or (cod_prod between 207 and 208) or cod_prod = 237)";
							pStatement1 = connection.prepareStatement(query);
							pStatement1.setString(1, l_nonAddedAccNo);
							rset1 = pStatement1.executeQuery();
							while (rset1.next()) {
								l_approveRel = rset1.getString(2);
								l_approveName = l_approveNameMap.get(rset1.getString(1));
								
								String getValue = "";
								//SELECT GROUP_ID,GROUP_NAME FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'
								pStatement2 = connection.prepareStatement("SELECT GROUP_ID,GROUP_NAME,LIMIT_REQUIRED FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'");
								rset2 = pStatement2.executeQuery();
								while(rset2.next()) 
								{
									
									l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
									String l_groupCd = rset2.getString(1);
									// Add unchecked data
									
									
									
									if("SOW".equalsIgnoreCase(l_approveRel)) 
									{
										
										l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_nonAddedAccNo+" id='"
												+ l_nonAddedAccNo+"~"+l_nonAddedAccNo+l_groupCd
												+ "~"
												+ l_groupCd
												+ "' value='"
												+ l_nonAddedAccNo+l_groupCd
												+ "~"
												+ l_groupCd
												+ "' class='checkbox' type='checkbox' disabled></td>";
										
										l_appHtmlStr += "<td align='left'>"
												//+ l_nonAddedAccNo+l_groupCd
												+rset1.getString(2)
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveName
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveRel
												+ "</td>"
												+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_nonAddedAccNo
												+ l_nonAddedAccNo+l_groupCd
												+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
												+ "</td>"
												+ "<td id=tool"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd+ " align='left'><input type='text' class='inputbox' id=words"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd
												/* + rset.getString(1) */
												+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
												+ "</td>";
									} 
									else 
									{
										l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_nonAddedAccNo+" id='"
												+ l_nonAddedAccNo+"~"+l_nonAddedAccNo+l_groupCd
												+ "~"
												+ l_groupCd
												+ "' value='"
												+ l_nonAddedAccNo+l_groupCd
												+ "~"
												+ l_groupCd
												+ "' class='checkbox' type='checkbox'></td>";
										
										l_appHtmlStr += "<td align='left'>"
												//+ l_nonAddedAccNo+l_groupCd
												+rset1.getString(2)
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveName
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveRel
												+ "</td>"
												+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_nonAddedAccNo
												+ l_nonAddedAccNo+l_groupCd
												+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;'> </input>"
												+ "</td>"
												+ "<td id=tool"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd+ " align='left'><input type='text' class='inputbox' id=words"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd
												/* + rset.getString(1) */
												+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;'> </input>"
												+ "</td>";
									}
									
									
									
									l_appHtmlStr += "</tr>";
									
									insertedApproverIdForGrp.put(l_nonAddedAccNo+l_groupCd, l_appHtmlStr);
									
									
									if(exculudedGroupIds.containsKey(l_nonAddedAccNo)) 
									{
										getValue = exculudedGroupIds.get(l_nonAddedAccNo);
										if(!getValue.contains(l_groupCd)) 
										{
											getValue = getValue  +"~" + l_groupCd;
										}
										
										exculudedGroupIds.put(l_nonAddedAccNo,getValue);
										//insertedGroupIds.put(l_nonAddedAccNo,getValue);
									}
									else 
									{
										exculudedGroupIds.put(l_nonAddedAccNo,l_groupCd);
										//insertedGroupIds.put(l_nonAddedAccNo,l_groupCd);
									}
									
									
								}
									rset2.close();
									pStatement2.close();
									
								}
								
							rset1.close();
							pStatement1.close();
								
								
							}
						
								Iterator accountNoGrpItr = insertedGroupIds.entrySet().iterator();
								while(accountNoGrpItr.hasNext()) 
								{
									Map.Entry l_keyValueAccEntry = (Map.Entry)accountNoGrpItr.next(); 
										String l_accountNo = String.valueOf(l_keyValueAccEntry.getKey());
										String l_getGroupIds = String.valueOf(l_keyValueAccEntry.getValue());
										String 	l_htmlStrForSerNAppMid = "";
										//
										
							/*
							 * Map.Entry l_keyValueEntry = (Map.Entry)accountNoGrpItr.next(); String
							 * l_accountNo = String.valueOf(l_keyValueEntry.getKey()).split("~")[0]; String
							 * l_groupId = String.valueOf(l_keyValueEntry.getKey()).split("~")[1]; String
							 * l_dailyLimit = String.valueOf(l_keyValueEntry.getValue());
							 */
											
											Iterator getGrpNSerItr =  serviceDataMap.entrySet().iterator();
											while(getGrpNSerItr.hasNext()) 
											{
												Map.Entry l_keyValueEntry1 = (Map.Entry)getGrpNSerItr.next(); 
												String l_groupNameIdFromTable = String.valueOf(l_keyValueEntry1.getKey());
												String l_serviceNameFromTable = String.valueOf(l_keyValueEntry1.getValue());
												String activateFlagHtml = "";
												boolean isActivateFlag = true;
												if(("Y").equals(accountGroupActiveFlagMap.get(l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~")))) 
												{
													activateFlagHtml = "<span style='color:red;' value='Y' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag></span>";
												}
												else if(("N").equals(accountGroupActiveFlagMap.get(l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~"))))
												{
													activateFlagHtml = "<span style='color:red;' value='N' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag> -- Deactivated Service</span>";
													isActivateFlag = false;
												}
												else 
												{
													activateFlagHtml = "<span style='color:red;' value='Y' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag></span>";
												}
												
												if(l_getGroupIds.contains(l_groupNameIdFromTable.split("~")[0].replace("GRP", ""))) 
												{
													String l_dailyLimit = insertedGroupLimit.get(l_accountNo+"~"+l_groupNameIdFromTable.split("~")[0].replace("GRP", ""));
													String l_amtInWords = CommonUtility.convertAmountToWords(l_dailyLimit+"$");
													l_dailyLimit = CommonUtility.convertAmtIntoComma(l_dailyLimit);
													
													if(!isActivateFlag) 
													{
														l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
																"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
																"class='serviceCheckbox' "+
																"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ ">"+
																"</input></td>"+
															"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
																"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
															"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' value="+l_dailyLimit+" class='searchInputText'"+
															" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"+
															"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title='"+l_amtInWords+"'><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" value='"+l_amtInWords+"'  class='searchInputText'"+
															"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
														"</tr>";
													}
													else 
													{
														l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
																"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
																"class='serviceCheckbox' "+
																"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " checked >"+
																"</input></td>"+
															"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
																"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
															"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' value="+l_dailyLimit+" class='searchInputText'"+
															" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"+
															"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title='"+l_amtInWords+"'><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" value='"+l_amtInWords+"'  class='searchInputText'"+
															"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
														"</tr>";
													}
													
													
												}
												else 
												{
													l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
															"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
															"class='serviceCheckbox' "+
															"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " >"+
															"</input></td>"+
														"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
															"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
														"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' class='searchInputText'"+
														" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"+
														"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title=''><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
														"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
													"</tr>";
												}
											
										}
									
											
							/*
							 * getGrpNSerItr = serviceDataMap.entrySet().iterator();
							 * while(getGrpNSerItr.hasNext()) { Map.Entry l_keyValueEntry1 =
							 * (Map.Entry)getGrpNSerItr.next(); String l_groupNameIdFromTable =
							 * String.valueOf(l_keyValueEntry1.getKey()); String l_serviceNameFromTable =
							 * String.valueOf(l_keyValueEntry1.getValue()); String l_excludedIds =
							 * exculudedGroupIds.get(l_accountNo);
							 * if(l_excludedIds.contains(l_groupNameIdFromTable.split("~")[0].replace("GRP",
							 * ""))) { l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid +
							 * "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +"' onclick='setApprover(this.id);>"+
							 * "<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
							 * "class='serviceCheckbox' "+
							 * "type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split(
							 * "~")[0].replace("~",
							 * "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"
							 * +l_accountNo+ " >"+ "</input></td>"+
							 * "<td style='width: 60%;'><b>"+l_groupNameIdFromTable.split("~")[1]
							 * +"</b><br />"+
							 * "<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;'>"
							 * +l_serviceNameFromTable+"</span></td>"+
							 * "<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' id='"
							 * +l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +" name='approvalMappingMaster.dailyLimitS' class='searchInputText'"+
							 * " style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"
							 * + "<td id=tool'"+l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +" style='width: 35%;' ><input type='text' id=words"+l_accountNo+
							 * l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
							 * "  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"
							 * + "</tr>"; } }
							 */
											
											
											
											String l_htmlStrForSerNAppLast =
													"</table>"+
													"</td>"+
													
												"<td id='approverCDList1'>"+
													"<div id='approverCDList'>"+
													"<table class='TFtable' style='width:100%;height:300px'>"+
													"<thead>"+
													"<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"+
													"<th style='color:white;text-align:center'>Approver ID</th>"+
													"<th style='color:white;text-align:center'>Approver Name</th>"+
													"<th style='color:white;text-align:center'>Approver Role</th>"+
													"<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
													"<th style='color:white;text-align:center'>Amount(In Words)</th>"+
													"</tr></thead>"+
													
													"<tbody id='transbody"+String.valueOf(l_keyValueAccEntry.getKey())+"'>"+
													"</tbody>"+
													"</table>"+
													"</div>"+
												"</td>"+
												
												"</tr><tr id='l_mopApproverStr"+String.valueOf(l_keyValueAccEntry.getKey())+"'></tr>"+
												"</table>";	
											
									
										insertedGrpHtml.put(String.valueOf(l_keyValueAccEntry.getKey()), l_htmlStrForSerNAppFirst+l_htmlStrForSerNAppMid+l_htmlStrForSerNAppLast);
								}
								
								
								Iterator insertedAccountNonAddedMapItr1 = insertedAccountNonAddedMap.entrySet().iterator();
								while(insertedAccountNonAddedMapItr1.hasNext()) 
								{
									Map.Entry l_keyValueAccEntry1 = (Map.Entry)insertedAccountNonAddedMapItr1.next(); 
									
									String l_accountNo = String.valueOf(l_keyValueAccEntry1.getKey());
									String 	l_htmlStrForSerNAppMid = "";
									
									Iterator getGrpNSerItr =  serviceDataMap.entrySet().iterator();
									
									
									
									while(getGrpNSerItr.hasNext()) 
									{
										
										String activateFlagHtml = "";
										
										Map.Entry l_keyValueEntry1 = (Map.Entry)getGrpNSerItr.next(); 
										String l_groupNameIdFromTable = String.valueOf(l_keyValueEntry1.getKey());
										String l_serviceNameFromTable = String.valueOf(l_keyValueEntry1.getValue());
										
									
										
											activateFlagHtml = "<span style='color:red;' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~")+"FlagY></span>";
										
										
										
										l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
												"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
												"class='serviceCheckbox' "+
												"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " >"+
												"</input></td>"+
											"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
												"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
											"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' class='searchInputText'"+
											" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"+
											"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title=''><input type='text' id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
											"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
										"</tr>";
									
										
										
										
									}
									String l_htmlStrForSerNAppLast =
											"</table>"+
											"</td>"+
											
										"<td id='approverCDList1'>"+
											"<div id='approverCDList'>"+
											"<table class='TFtable' style='width:100%;height:300px'>"+
											"<thead>"+
											"<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"+
											"<th style='color:white;text-align:center'>Approver ID</th>"+
											"<th style='color:white;text-align:center'>Approver Name</th>"+
											"<th style='color:white;text-align:center'>Approver Role</th>"+
											"<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
											"<th style='color:white;text-align:center'>Amount(In Words)</th>"+
											"</tr></thead>"+
											
											"<tbody id='transbody"+l_accountNo+"'>"+
											"</tbody>"+
											"</table>"+
											"</div>"+
										"</td>"+
										
										"</tr><tr id='l_mopApproverStr"+l_accountNo+"'></tr>"+
										"</table>";	
									
							
											insertedGrpHtml.put(l_accountNo, l_htmlStrForSerNAppFirst+l_htmlStrForSerNAppMid+l_htmlStrForSerNAppLast);
										
									}
								
								
								
								
								
								insertedAccountNum.putAll(insertedAccountNonAddedMap);	
						
						output.put("accountNumberMap", insertedAccountNum);
						output.put("groupIdsHtmlMap", insertedGrpHtml);
						output.put("approverIdGrpMap", insertedApproverIdForGrp);
						output.put("groupIdMopMap", insertedMopForGrp);
						output.put("groupIdsMap", insertedGroupIds);
						output.put("exculudedGroupIds", exculudedGroupIds);
						output.put("groupIdNameMap", l_groupIdNameMap);
						output.put("groupIdMopFlagMap", insertedMopFlagForGrp);
						output.put("approverFlagForGrp", insertedApproverFlagForGrp);
						output.put("addedGroupLimit", insertedGroupLimit);
						
						log.info("insertedAccountNum ="+insertedAccountNum);
						
						rset.close();
						pStatement.close();
						
					}
					else {
						log.info("view Case");
						sql1 = "SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST WHERE  SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";
						
						
						
						// sql1 =SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST
						// WHERE AND
						// SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?
						// "SELECT A.APPROVER_LIST from COM_APPROVAL_GRP_MST
						// A,COM_SERVICE_MST_TMP B WHERE A.ACTIVE_FLAG = 'Y' AND
						// A.SERVICECDID = ? AND
						// A.ACCOUNT_NO = ? AND B.SERVICE_DESC = ? AND
						// A.SERVICE_CD=B.SERVICE_CD";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, accno);
						pStatement.setString(3, serviceCDId);
						rset = pStatement.executeQuery();
						String approveID = "", approveLimit = "";
						String[] approverData;

						if (rset.next()) {

							approver_list = rset.getString(1);

							log.info("approver_list " + approver_list);

						}
						rset.close();
						pStatement.close();

						
						String apprLimit = "";
						pStatement = connection.prepareStatement(query);
						pStatement.setString(1, "N");
						pStatement.setString(2, custId);
						rset = pStatement.executeQuery();
						while (rset.next()) {
							if (custIdList.contains(rset.getString(1))) {
								srNo++;
								tempString = "";
								isCheck = false;
								moduleID = rset.getString(1);
								log.info("In While--------");
								/* Added By 2233 For Add Aprrove Name */
								l_approveName = l_approveNameMap.get(rset
										.getString(1).toString());
								if ("null".equals(l_approveName)
										|| l_approveName == null) {
									l_approveName = "";
								}
								l_approveRel = getapproverAccRelSplit(rset.getString(2).toString(), accno);
								tempString = "<tr style='border:solid 2px #000000;'>";
								log.info("rset.getString(1) "
										+ rset.getString(1));
								if (approver_list.contains(rset.getString(1))) {
									log.info("in if------" + rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' checked='checked' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' type='checkbox'></td>";

									String[] list = approver_list.split("\\|");

									for (int i = 0; i < list.length; i++) {
										if (list[i].contains(rset.getString(1))) {
											apprLimit = list[i].split("\\#")[1];
											log.info("apprLimit " + apprLimit);
											break;
										}
									}

									tempString += "<td align='center' disabled='disabled'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveName
											+ "</td>"
											+ "</td>"
											+ "<td align='center' disabled='disabled'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='"
											+ apprLimit
											+ "'maxlength='26' disabled='disabled' style='width: 98%;margin-left: 0px;text-align: right;border: none;'> </input></td>";
									tempString += "</tr>";

								} else {

									log.info("in else-------"
											+ rset.getString(1));
									tempString += "<td align='center'><input name='groupCheck' disabled='disabled' id='"
											+ rset.getString(1)
											+ "' value='"
											+ rset.getString(1)
											+ "~"
											+ rset.getInt(3)
											+ "' class='checkbox' disabled='disabled' type='checkbox'></td>";
									tempString += "<td align='center'>"
											+ rset.getString(1)
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='center'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text'  disabled='disabled' id='input"
											+ rset.getString(1)
											+ "' value='' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;' > </input></td>";
									tempString += "</tr>";

								}
								log.info("tempString " + tempString);

								moduleDetails += tempString;

								if (Data != null) {

								} else {
									Data = "";
								}
								/*
								 * moduleDetails +=
								 * "<tr style='display:none' disabled='disabled' id='moduleList'><td>" + Data +
								 * "</td></tr>";
								 */

							}
						}

						rset.close();
						pStatement.close();
					}

				}

				if ("View".equals(Data)) {
					
					


					log.info("In View Case >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
							+ tableFlg + "   ");
					
					PreparedStatement pStatment1 = null;
					ResultSet rSet1 = null;
					if ("T".equalsIgnoreCase(tableFlg) || "M".equalsIgnoreCase(tableFlg)) {
						
						log.info("Modified Case");
						
						
						if("Authorized".equals(status)) 
						{
							// Go to main master table
							sql1 = "SELECT ACCOUNT_NO,SERVICE_CD,APPROVER_LIST,DAILY_LIMIT,OPERATION_MODE,ID,ACTIVE_FLAG FROM COM_APPROVAL_GRP_MST WHERE SERVICECDID = ?";// AND ACTIVE_FLAG = 'Y'";
						}
						else 
						{
							// Go to temp table 
							sql1 = "SELECT ACCOUNT_NO,SERVICE_CD,APPROVER_LIST,DAILY_LIMIT,OPERATION_MODE,ID,ACTIVE_FLAG FROM COM_APPROVAL_GRP_MST_TMP WHERE SERVICECDID = ?";
						}
						
						
						Map<String,String> serviceDataMap = getServiceCDMap();
						Map<String,String> insertedAccountNum = new HashMap<String,String>(); 
						Map<String,String> insertedGroupIds = new HashMap<String,String>();
						Map<String,String> insertedGroupLimit = new HashMap<String,String>();
						Map<String,String> insertedApproverIdForGrp = new HashMap<String,String>();
						Map<String,String> insertedMopForGrp = new HashMap<String,String>();
						Map<String,String> insertedGrpHtml = new HashMap<String,String>();
						
						Map<String,String> exculudedGroupIds = new HashMap<String, String>();
						
						Map<String,String> unmappedAccountServiceMap = new HashMap<String, String>();
						Map<String,String> accountGroupActiveFlagMap = new HashMap<String, String>();
						
						Set<Double> addedIDForView = new HashSet<Double>(); 
						List<String> rejReasonList = new ArrayList<String>();
						
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						//pStatement.setString(2, accno);
						rset = pStatement.executeQuery();
						
						
						String l_htmlStrForSerNAppFirst = "<table class='TFtable' style='width:100%;height:300px'>"+
								"<tr id='transactionDiv3'>"+
								"<td id='serviceCDList' style='width:50%;'>"+
								
									"<table class='TFtable' style='width:100%;height:300px'>"+
									"<thead><tr style='margin-bottom:3px;background-color:#00518F;height:20px;'>"+
									"<th></th>"+
									"<th style='color:white;text-align:center'>Group Service Name</th>"+
									"<th style='color:white;text-align:center;'>Daily Limit (In Rs.)</th>"+
									"<th style='color:white;text-align:center;'>Amount (In Words)</th>"+
									"</tr></thead>";
						/*
						 * String l_htmlStrForSerNAppLast = "</table>"+ "</td>"+
						 * 
						 * "<td id='approverCDList1'>"+ "<div id='approverCDList'>"+
						 * "<table style='border:1px solid grey;width:100%;height:194px'>"+ "<thead>"+
						 * "<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"
						 * + "<th style='color:white;text-align:center'>Approver ID</th>"+
						 * "<th style='color:white;text-align:center'>Approver Name</th>"+
						 * "<th style='color:white;text-align:center'>Approver Role</th>"+
						 * "<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
						 * "<th style='color:white;text-align:center'>Amount(In Words)</th>"+
						 * "</tr></thead>"+
						 * 
						 * "<tbody id=transbody>$lastChecked$"+ "</tbody>"+ "</table>"+ "</div>"+
						 * "</td>"+
						 * 
						 * "</tr>$l_mopApproverStr$"+ "</table>";
						 */
						

						
						
						while (rset.next()) {

							approver_list = rset.getString(3);
							addedIDForView.add(rset.getDouble(6));// To get Id for authorize and reject
							
							log.info("approver_list " + approver_list);
							insertedAccountNum.put(rset.getString(1).trim(),rset.getString(1).trim());
							insertedGroupLimit.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(4));
							insertedApproverIdForGrp.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(3));
							accountGroupActiveFlagMap.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),rset.getString(7));
							String l_idData = rset.getString(1).trim()+"~"+rset.getString(2).trim();
							String l_mopApproverStr = "";
							if("O".equalsIgnoreCase(rset.getString(5))) 
							{
								
								l_mopApproverStr = 
										"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+ " value='O'"+
										" class='fileOption' checked='checked' disabled /><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
										" class='fileOption' disabled /> <span style='margin-left: 6px;'><label for=Pattern2"+l_idData.replace("~", "GRP")+" disabled><span name='All of the above'>All of the above</span></label></span></div></td>"
										;
								
								/*
								 * l_mopApproverStr =
								 * "<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"
								 * +
								 * "<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"
								 * +l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~",
								 * "GRP")+" value='O'"+
								 * " class='fileOption' checked='checked'/><span style='margin-left: 6px;' > <label for=Pattern1"
								 * +l_idData.replace("~", "GRP")
								 * +"><span name='Any one of the above'>Any one of the above</span></label></span></div>"
								 * +
								 * "<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"
								 * +l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~",
								 * "GRP")+" value='J'"+
								 * " class='fileOption' /> <span style='margin-left: 6px;'><label for=Pattern2'+account+'><span name='All of the above'>All of the above</span></label></span></div></td>"
								 * +
								 * "<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"
								 * +l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"
								 * +
								 * "<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"
								 * +l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"
								 * +
								 * "<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"
								 * +l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"
								 * + "</td>" ;
								 */
							}
							else 
							{
								
								l_mopApproverStr = 
										"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
										" class='fileOption' disabled/><span style='margin-left: 6px;' disabled='true'> <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></label></span></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
										" class='fileOption' checked='checked' disabled /> <span style='margin-left: 6px;' disabled='true'><label for=Pattern2"+l_idData.replace("~", "GRP")+" checked disabled><span name='All of the above'>All of the above</span></label></span></div></td>"
										;
								
								/*l_mopApproverStr = 
										"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
										" class='fileOption'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></label></span></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
										" class='fileOption' checked='checked' /> <span style='margin-left: 6px;'><label for=Pattern2'+account+' checked ><span name='All of the above'>All of the above</span></label></span></div></td>"+
										"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
										"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
										"</td>"
										;*/
							}
							
							
							
							insertedMopForGrp.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(),l_mopApproverStr);
							if(insertedGroupIds.containsKey(rset.getString(1).trim())) 
							{
								String getValue = insertedGroupIds.get(rset.getString(1).trim());
								insertedGroupIds.put(rset.getString(1).trim(),getValue+"~"+rset.getString(2).trim());
							}
							else 
							{
								insertedGroupIds.put(rset.getString(1).trim(),rset.getString(2).trim());
							}
							
							unmappedAccountServiceMap.put(rset.getString(1).trim()+"~"+rset.getString(2).trim(), rset.getString(6));
							
						}
						
						rset.close();
						pStatement.close();
						
						
						Map<String,String> insertedAccountNonAddedMap = new HashMap<String,String>();
						// Code for set entry of non added account in first time
						sql1 = "SELECT DISTINCT(TRIM(COD_ACCT_NO)) AS ACCOUNT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST=? AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899) or (cod_prod between 207 and 208) or cod_prod = 237) AND TRIM(COD_ACCT_NO) not in (select distinct ACCOUNT_NO from COM_APPROVAL_GRP_MST where SERVICECDID = ?) ";
						pStatement = connection.prepareStatement(sql1);
						pStatement.setString(1, custId);
						pStatement.setString(2, custId);
						//pStatement.setString(2, accno);
						rset = pStatement.executeQuery();
						
						while(rset.next()) 
						{
							if(!insertedAccountNum.containsKey(rset.getString(1).trim())) 
							{
								insertedAccountNonAddedMap.put(rset.getString(1).trim(),rset.getString(1).trim());
								String l_idData = "";
								String l_mopApproverStr = "";
								String grpId = "";
								Iterator serviceIdsItr = serviceDataMap.entrySet().iterator();
								while(serviceIdsItr.hasNext())
								{
									Map.Entry<String, String> keyValueId = (Map.Entry<String, String>)serviceIdsItr.next();
									grpId = keyValueId.getKey().split("~")[0].replace("GRP", "");
									 l_idData = rset.getString(1).trim()+"~"+grpId;
									 l_mopApproverStr = 
											"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
											" class='fileOption' checked='checked' disabled/><span style='margin-left: 6px;' disabled='true' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
											" class='fileOption' disabled /> <span style='margin-left: 6px;' disabled='true'><label for=Pattern2"+l_idData.replace("~", "GRP")+" disabled><span name='All of the above'>All of the above</span></label></span></div></td>"
											
											;
									 
									 	insertedMopForGrp.put(l_idData,l_mopApproverStr);
										insertedApproverIdForGrp.put(l_idData, "#");
									
								}	
								
								
								
								
							}
							
						}
						
						rset.close();
						pStatement.close();
						
						
						Iterator accountNoGrpItr1 = insertedGroupIds.entrySet().iterator();
						while(accountNoGrpItr1.hasNext()) 
						{
							Map.Entry<String, String> keyValueData = (Map.Entry<String, String>)accountNoGrpItr1.next();
							String l_groupIds = keyValueData.getValue();
							String getValue = "";
							String grpId = "";
							String l_idData = "";
							Iterator serviceIdsItr = serviceDataMap.entrySet().iterator();
							while(serviceIdsItr.hasNext())
							{
								Map.Entry<String, String> keyValueId = (Map.Entry<String, String>)serviceIdsItr.next();
								grpId = keyValueId.getKey().split("~")[0].replace("GRP", "");
								if(!l_groupIds.contains(grpId)) 
								{
									l_idData = keyValueData.getKey()+"~"+grpId;
									
									String l_mopApproverStr = 
											"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
											" class='fileOption' checked='checked' disabled/><span style='margin-left: 6px;' disabled='true' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
											" class='fileOption' disabled /> <span style='margin-left: 6px;' disabled='true'><label for=Pattern2"+l_idData.replace("~", "GRP")+" disabled><span name='All of the above'>All of the above</span></label></span></div></td>"
											
											;
									
									/*String l_mopApproverStr = 
											"<td style='height:30px;'><span name='Select Operation Mode:'>Select Operation Mode:</span>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern1"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='O'"+
											" class='fileOption' checked='checked'/><span style='margin-left: 6px;' > <label for=Pattern1"+l_idData.replace("~", "GRP")+"><span name='Any one of the above'>Any one of the above</span></label></span></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='radio' id=Pattern2"+l_idData.replace("~", "GRP")+" name=radio"+l_idData.replace("~", "GRP")+" value='J'"+
											" class='fileOption' /> <span style='margin-left: 6px;'><label for=Pattern2'+account+'><span name='All of the above'>All of the above</span></label></span></div></td>"+
											"<td><div style='display: inline-flex;margin-left:20px;'><input type='button' id=add"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only buttonTd' onclick='addLocaldataUpdate(this.id);' cssclass='button' value='Add to list' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=copyLimit"+l_idData+" onclick='copyAmount(this.id);' class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' value='Copy transaction limit' /></div>"+
											"<div style='display: inline-flex;margin-left:20px;'><input type='button' id=existSer"+l_idData+" class='ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only' style='margin-right: 20px' onclick='addLocaldataUpdate(this.id);' value='Only new service' /></div>"+
											"</td>"
											;*/

									insertedMopForGrp.put(l_idData,l_mopApproverStr);
									insertedApproverIdForGrp.put(l_idData, "#");
									
									
									if(exculudedGroupIds.containsKey(keyValueData.getKey())) 
									{
										getValue = exculudedGroupIds.get(keyValueData.getKey());
										if(!getValue.contains(grpId)) 
										{
											getValue = getValue  +"~" + grpId;
										}
										
										exculudedGroupIds.put(keyValueData.getKey(),getValue);
									}
									else 
									{
										exculudedGroupIds.put(keyValueData.getKey(),grpId);
									}
									
									
									
								}
							
							}
							
							
							
						}
						
						
						insertedAccountNum = sortMap(insertedAccountNum);
						insertedGroupLimit = sortMap(insertedGroupLimit);
						insertedGroupIds = sortMap(insertedGroupIds);
						exculudedGroupIds = sortMap(exculudedGroupIds);
						
						//log.info("exculudedGroupIds ="+exculudedGroupIds);
						
						Map<String,String> approverDtlMap = new HashMap<String, String>();
						// To add approver for view data
						query = "SELECT APPROVER_CD, APPROVER_NAME,APPROVER_ID from COM_APPROVE_MST WHERE AUTH_STATUS !=? AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID = ?";
						pStatement1 = connection.prepareStatement(query);
						pStatement1.setString(1, "N");
						pStatement1.setString(2, custId);
						rset1 = pStatement1.executeQuery();
						while (rset1.next()) {
							approverDtlMap.put(rset1.getString(1), rset1.getString(2)+"~"+rset1.getString(3));
						}
						rset1.close();
						pStatement1.close();
						
						Iterator accountAppGrpItr = insertedApproverIdForGrp.entrySet().iterator();
						while(accountAppGrpItr.hasNext()) 
						{
							Map.Entry l_keyValueAppEntry = (Map.Entry)accountAppGrpItr.next(); 
							String l_accountGrpNo = String.valueOf(l_keyValueAppEntry.getKey());
							String l_approverList = String.valueOf(l_keyValueAppEntry.getValue());
							
							String[] l_appLimitArr =  l_approverList.split("\\|");
							
							String l_appHtmlStr = "";
							
							Iterator approverItr = approverDtlMap.entrySet().iterator();
							while(approverItr.hasNext()) 
							{
								 
								Map.Entry l_keyValueDbAppEntry = (Map.Entry)approverItr.next(); 
								l_approveRel = getapproverAccRelSplit(String.valueOf(l_keyValueDbAppEntry.getValue()).split("~")[0], l_accountGrpNo.split("~")[0]);
								l_approveName = l_approveNameMap.get(String.valueOf(l_keyValueDbAppEntry.getKey()));
								if(l_approverList.contains(String.valueOf(l_keyValueDbAppEntry.getKey())+"#")) 
								{
									l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
									
									// Add checked data here
									String l_appLimit = "";
									
									for(int i = 0;i<l_appLimitArr.length;i++) 
									{
										if(l_appLimitArr[i].contains(String.valueOf(l_keyValueDbAppEntry.getKey()))) 
										{
											l_appLimit = l_appLimitArr[i].split("\\#")[1];
										}
									}
									
									String l_amtInWords = CommonUtility.convertAmountToWords(l_appLimit+"$");
									l_appLimit = CommonUtility.convertAmtIntoComma(l_appLimit);
									
									l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_accountGrpNo.split("~")[0]+" checked='checked' id='"
											+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
											+"~"
											+ l_accountGrpNo.split("~")[1]
											+ "' value='"
											+ l_keyValueDbAppEntry.getKey()
											+ "~"
											+ l_accountGrpNo.split("~")[1]
											+ "' class='checkbox' type='checkbox' disabled></td>";
									l_appHtmlStr += "<td align='left'>"
											+ l_keyValueDbAppEntry.getKey()
											+ "</td>"
											+ "<td align='left'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='left'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' name=words"+l_accountGrpNo.split("~")[0]+" onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
											+ l_keyValueDbAppEntry.getKey()
											+ "' value = "+l_appLimit+" placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
											+ "</td>"
											+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left' title='"+l_amtInWords+"'><input type='text' class='inputbox' name=words"+l_accountGrpNo.split("~")[0]+" id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
											/* + rset.getString(1) */
											+ " value='"+l_amtInWords+"' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
											+ "</td>";
									
								}
								else
								{
									
									
									
								//	log.info("l_keyValueDbAppEntry.getValue()="+l_keyValueDbAppEntry.getValue());
								//	log.info("l_accountGrpNo.split()[0]="+l_accountGrpNo.split("~")[0]);
									// Add unchecked data
									// Add only unchecked approver id of that particular account
									if(String.valueOf(l_keyValueDbAppEntry.getValue()).contains(l_accountGrpNo.split("~")[0])) 
									{
										
										l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
										l_appHtmlStr += "<td align='center'><input name='groupCheck"+l_accountGrpNo.split("~")[0]+"' id='"
												+ l_accountGrpNo.split("~")[0]+"~"+l_keyValueDbAppEntry.getKey()
												+ "~"
												+ l_accountGrpNo.split("~")[1]
												+ "' value='"
												+ l_keyValueDbAppEntry.getKey()
												+ "~"
												+ l_accountGrpNo.split("~")[1]
												+ "' class='checkbox' type='checkbox' disabled></td>";
										l_appHtmlStr += "<td align='left'>"
												+ l_keyValueDbAppEntry.getKey()
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveName
												+ "</td>"
												+ "<td align='left'>"
												+ l_approveRel
												+ "</td>"
												+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_accountGrpNo.split("~")[0]
												+ l_keyValueDbAppEntry.getKey()
												+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
												+ "</td>"
												+ "<td id=tool"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()+ " align='left'><input type='text' class='inputbox' id=words"+l_accountGrpNo.split("~")[0]+l_keyValueDbAppEntry.getKey()
												/* + rset.getString(1) */
												+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
												+ "</td>";
										l_appHtmlStr += "</tr>";
									}
								}
								
								
							}
							
							
							
							insertedApproverIdForGrp.put(l_accountGrpNo, l_appHtmlStr);
							
						}
							
							// To add unchecked data for all new account number	
						
						//SELECT COD_CUST,COD_ACCT_CUST_REL from VW_CH_ACCT_CUST_XREF WHERE COD_ACCT_NO = cast('111381311111321' as char(48)) AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899))
						Iterator insertedAccountNonAddedMapItr = insertedAccountNonAddedMap.entrySet().iterator();
						while(insertedAccountNonAddedMapItr.hasNext()) 
						{
							Map.Entry l_keyValueAccEntry = (Map.Entry)insertedAccountNonAddedMapItr.next(); 
							
							String l_nonAddedAccNo = (String)l_keyValueAccEntry.getKey();
							
							String l_appHtmlStr = "";
							
							// To add approver for view data
							query = "SELECT COD_CUST,COD_ACCT_CUST_REL from VW_CH_ACCT_CUST_XREF WHERE COD_ACCT_NO = cast(? as char(48)) AND ((cod_prod between 501 and 599)  or (cod_prod between 901 and 999) or (cod_prod between 601 and 899) or (cod_prod between 207 and 208) or cod_prod = 237)";
							pStatement1 = connection.prepareStatement(query);
							pStatement1.setString(1, l_nonAddedAccNo);
							rset1 = pStatement1.executeQuery();
							while (rset1.next()) {
								l_approveRel = rset1.getString(2);
								l_approveName = l_approveNameMap.get(rset1.getString(1));
								
								String getValue = "";
								//SELECT GROUP_ID,GROUP_NAME FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'
								pStatement2 = connection.prepareStatement("SELECT GROUP_ID,GROUP_NAME,LIMIT_REQUIRED FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'");
								rset2 = pStatement2.executeQuery();
								while(rset2.next()) 
								{
									
									l_appHtmlStr += "<tr style='height:15px;border:solid 2px #000000;'>";
									String l_groupCd = rset2.getString(1);
									// Add unchecked data
									
									l_appHtmlStr += "<td align='center'><input name=groupCheck"+l_nonAddedAccNo+" id='"
											+ l_nonAddedAccNo+"~"+l_nonAddedAccNo+l_groupCd
											+ "~"
											+ l_groupCd
											+ "' value='"
											+ l_nonAddedAccNo+l_groupCd
											+ "~"
											+ l_groupCd
											+ "' class='checkbox' type='checkbox' disabled></td>";
									l_appHtmlStr += "<td align='left'>"
											//+ l_nonAddedAccNo+l_groupCd
											+rset1.getString(2)
											+ "</td>"
											+ "<td align='left'>"
											+ l_approveName
											+ "</td>"
											+ "<td align='left'>"
											+ l_approveRel
											+ "</td>"
											+ "<td align='left'><input type='text' class='inputbox' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='input"+l_nonAddedAccNo
											+ l_nonAddedAccNo+l_groupCd
											+ "' placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;' disabled> </input>"
											+ "</td>"
											+ "<td id=tool"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd+ " align='left'><input type='text' class='inputbox' id=words"+l_nonAddedAccNo+l_nonAddedAccNo+l_groupCd
											/* + rset.getString(1) */
											+ " placeholder='0.0' maxlength='16' style='width: 98%;margin-left: 0px;text-align: right;border: none;background-color: white;' disabled> </input>"
											+ "</td>";
									
									l_appHtmlStr += "</tr>";
									
									insertedApproverIdForGrp.put(l_nonAddedAccNo+l_groupCd, l_appHtmlStr);
									
									
									if(exculudedGroupIds.containsKey(l_nonAddedAccNo)) 
									{
										getValue = exculudedGroupIds.get(l_nonAddedAccNo);
										//getValue = getValue  +"~" + l_groupCd;
										if(!getValue.contains(l_groupCd)) 
										{
											getValue = getValue  +"~" + l_groupCd;
										}
										
										exculudedGroupIds.put(l_nonAddedAccNo,getValue);
										//insertedGroupIds.put(l_nonAddedAccNo,getValue);
									}
									else 
									{
										exculudedGroupIds.put(l_nonAddedAccNo,l_groupCd);
										//insertedGroupIds.put(l_nonAddedAccNo,l_groupCd);
									}
									
									
								}
									rset2.close();
									pStatement2.close();
									
								}
								
							rset1.close();
							pStatement1.close();
								
								
							}
						
								Iterator accountNoGrpItr = insertedGroupIds.entrySet().iterator();
								while(accountNoGrpItr.hasNext()) 
								{
									Map.Entry l_keyValueAccEntry = (Map.Entry)accountNoGrpItr.next(); 
										String l_accountNo = String.valueOf(l_keyValueAccEntry.getKey());
										String l_getGroupIds = String.valueOf(l_keyValueAccEntry.getValue());
										String 	l_htmlStrForSerNAppMid = "";
										//
										
							/*
							 * Map.Entry l_keyValueEntry = (Map.Entry)accountNoGrpItr.next(); String
							 * l_accountNo = String.valueOf(l_keyValueEntry.getKey()).split("~")[0]; String
							 * l_groupId = String.valueOf(l_keyValueEntry.getKey()).split("~")[1]; String
							 * l_dailyLimit = String.valueOf(l_keyValueEntry.getValue());
							 */
											
											Iterator getGrpNSerItr =  serviceDataMap.entrySet().iterator();
											while(getGrpNSerItr.hasNext()) 
											{
												Map.Entry l_keyValueEntry1 = (Map.Entry)getGrpNSerItr.next(); 
												String l_groupNameIdFromTable = String.valueOf(l_keyValueEntry1.getKey());
												String l_serviceNameFromTable = String.valueOf(l_keyValueEntry1.getValue());
												String activateFlagHtml = "";
												boolean isActivateFlag = true;
												if(("Y").equals(accountGroupActiveFlagMap.get(l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~")))) 
												{
													activateFlagHtml = "<span style='color:red;' value='Y' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag></span>";
												}
												else if(("N").equals(accountGroupActiveFlagMap.get(l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~"))))
												{
													activateFlagHtml = "<span style='color:red;' value='N' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag> -- Deactivated Service</span>";
													isActivateFlag = false;
												}
												else 
												{
													activateFlagHtml = "<span style='color:red;' value='Y' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"Flag></span>";
												}
												
												if(l_getGroupIds.contains(l_groupNameIdFromTable.split("~")[0].replace("GRP", ""))) 
												{
													String l_dailyLimit = insertedGroupLimit.get(l_accountNo+"~"+l_groupNameIdFromTable.split("~")[0].replace("GRP", ""));
													String l_amtInWords = CommonUtility.convertAmountToWords(l_dailyLimit+"$");
													l_dailyLimit = CommonUtility.convertAmtIntoComma(l_dailyLimit);
													
													if(!isActivateFlag) 
													{
														l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
																"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
																"class='serviceCheckbox' "+
																"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " disabled='true'>"+
																"</input></td>"+
															"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
																"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
															"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' value="+l_dailyLimit+" class='searchInputText'"+
															" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0' disabled='true'></input></td>"+
															"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title='"+l_amtInWords+"'><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" value='"+l_amtInWords+"'  class='searchInputText'"+
															"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
														"</tr>";
													}
													else 
													{
														l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
																"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
																"class='serviceCheckbox' "+
																"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " checked disabled='true'>"+
																"</input></td>"+
															"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
																"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
															"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' value="+l_dailyLimit+" class='searchInputText'"+
															" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0' disabled='true'></input></td>"+
															"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title='"+l_amtInWords+"'><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" value='"+l_amtInWords+"'  class='searchInputText'"+
															"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
														"</tr>";
													}
													
													
												}
												else 
												{
													l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
															"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
															"class='serviceCheckbox' "+
															"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " disabled='true'>"+
															"</input></td>"+
														"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
															"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
														"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' name=d_limit"+l_accountNo+" maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' class='searchInputText'"+
														" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0' disabled='true'></input></td>"+
														"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title=''><input type='text' name=words"+l_accountNo+" id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
														"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
													"</tr>";
												}
											
										}
									
											
							/*
							 * getGrpNSerItr = serviceDataMap.entrySet().iterator();
							 * while(getGrpNSerItr.hasNext()) { Map.Entry l_keyValueEntry1 =
							 * (Map.Entry)getGrpNSerItr.next(); String l_groupNameIdFromTable =
							 * String.valueOf(l_keyValueEntry1.getKey()); String l_serviceNameFromTable =
							 * String.valueOf(l_keyValueEntry1.getValue()); String l_excludedIds =
							 * exculudedGroupIds.get(l_accountNo);
							 * if(l_excludedIds.contains(l_groupNameIdFromTable.split("~")[0].replace("GRP",
							 * ""))) { l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid +
							 * "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +"' onclick='setApprover(this.id);>"+
							 * "<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
							 * "class='serviceCheckbox' "+
							 * "type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split(
							 * "~")[0].replace("~",
							 * "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"
							 * +l_accountNo+ " >"+ "</input></td>"+
							 * "<td style='width: 60%;'><b>"+l_groupNameIdFromTable.split("~")[1]
							 * +"</b><br />"+
							 * "<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;'>"
							 * +l_serviceNameFromTable+"</span></td>"+
							 * "<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' id='"
							 * +l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +" name='approvalMappingMaster.dailyLimitS' class='searchInputText'"+
							 * " style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"
							 * + "<td id=tool'"+l_accountNo+l_groupNameIdFromTable.split("~")[0]
							 * +" style='width: 35%;' ><input type='text' id=words"+l_accountNo+
							 * l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
							 * "  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"
							 * + "</tr>"; } }
							 */
											
											
											
											String l_htmlStrForSerNAppLast =
													"</table>"+
													"</td>"+
													
												"<td id='approverCDList1'>"+
													"<div id='approverCDList'>"+
													"<table class='TFtable' style='width:100%;height:300px'>"+
													"<thead>"+
													"<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"+
													"<th style='color:white;text-align:center'>Approver ID</th>"+
													"<th style='color:white;text-align:center'>Approver Name</th>"+
													"<th style='color:white;text-align:center'>Approver Role</th>"+
													"<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
													"<th style='color:white;text-align:center'>Amount(In Words)</th>"+
													"</tr></thead>"+
													
													"<tbody id='transbody"+String.valueOf(l_keyValueAccEntry.getKey())+"'>"+
													"</tbody>"+
													"</table>"+
													"</div>"+
												"</td>"+
												
												"</tr><tr id='l_mopApproverStr"+String.valueOf(l_keyValueAccEntry.getKey())+"'></tr>"+
												"</table>";	
											
									
										insertedGrpHtml.put(String.valueOf(l_keyValueAccEntry.getKey()), l_htmlStrForSerNAppFirst+l_htmlStrForSerNAppMid+l_htmlStrForSerNAppLast);
								}
								
								
								Iterator insertedAccountNonAddedMapItr1 = insertedAccountNonAddedMap.entrySet().iterator();
								while(insertedAccountNonAddedMapItr1.hasNext()) 
								{
									Map.Entry l_keyValueAccEntry1 = (Map.Entry)insertedAccountNonAddedMapItr1.next(); 
									
									String l_accountNo = String.valueOf(l_keyValueAccEntry1.getKey());
									String 	l_htmlStrForSerNAppMid = "";
									
									Iterator getGrpNSerItr =  serviceDataMap.entrySet().iterator();
									
									
									
									while(getGrpNSerItr.hasNext()) 
									{
										
										String activateFlagHtml = "";
										
										Map.Entry l_keyValueEntry1 = (Map.Entry)getGrpNSerItr.next(); 
										String l_groupNameIdFromTable = String.valueOf(l_keyValueEntry1.getKey());
										String l_serviceNameFromTable = String.valueOf(l_keyValueEntry1.getValue());
										
									
										
											activateFlagHtml = "<span style='color:red;' id="+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("GRP", "~")+"FlagY></span>";
										
										
										
										l_htmlStrForSerNAppMid = l_htmlStrForSerNAppMid + "<tr id='"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' onclick='setApprover(this.id);'>"+
												"<td style='text-align: left; width:4%;padding-top: .1em;'><input "+
												"class='serviceCheckbox' "+
												"type='checkbox' id=serviceCheck"+l_accountNo+l_groupNameIdFromTable.split("~")[0].replace("~", "GRP")+" value="+l_groupNameIdFromTable.split("~")[0]+" name=transService"+l_accountNo+ " >"+
												"</input></td>"+
											"<td style='width: 60%;cursor:pointer;'><b>"+l_groupNameIdFromTable.split("~")[1]+"</b>"+activateFlagHtml+"<br />"+
												"<span style='font-family: Calibri; font-style: normal; font-size: 1.0em;cursor:pointer;'>"+l_serviceNameFromTable+"</span></td>"+
											"<td style='width: 35%;'><input type='text' onkeypress='return isNumberKeyVali(event,this.id)' onkeyup='this.value=Comma(this.value,this.id);' maxlength='18' id='d_limit"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"' class='searchInputText'"+
											" style='width: 165px; text-align: right;' maxlength='30' placeholder='0.0'></input></td>"+
											"<td id=tool"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+" style='width: 35%;' title=''><input type='text' id=words"+l_accountNo+l_groupNameIdFromTable.split("~")[0]+"  class='searchInputText'"+
											"  style='width: 165px; text-align: right;border: none;background-color: white;' maxlength='30' placeholder='0.0' disabled='true' ></input></td>"+
										"</tr>";
									
										
										
										
									}
									String l_htmlStrForSerNAppLast =
											"</table>"+
											"</td>"+
											
										"<td id='approverCDList1'>"+
											"<div id='approverCDList'>"+
											"<table class='TFtable' style='width:100%;height:300px'>"+
											"<thead>"+
											"<tr style='margin-bottom:3px;background-color:#00518F;height:20px;'><th></th>"+
											"<th style='color:white;text-align:center'>Approver ID</th>"+
											"<th style='color:white;text-align:center'>Approver Name</th>"+
											"<th style='color:white;text-align:center'>Approver Role</th>"+
											"<th style='color:white;text-align:center'>Per Transaction Limit (In Rs.)</th>"+
											"<th style='color:white;text-align:center'>Amount(In Words)</th>"+
											"</tr></thead>"+
											
											"<tbody id='transbody"+l_accountNo+"'>"+
											"</tbody>"+
											"</table>"+
											"</div>"+
										"</td>"+
										
										"</tr><tr id='l_mopApproverStr"+l_accountNo+"'></tr>"+
										"</table>";	
									
							
											insertedGrpHtml.put(l_accountNo, l_htmlStrForSerNAppFirst+l_htmlStrForSerNAppMid+l_htmlStrForSerNAppLast);
										
									}
								
								
								
								
								
								insertedAccountNum.putAll(insertedAccountNonAddedMap);	
						
						output.put("accountNumberMap", insertedAccountNum);
						output.put("groupIdsHtmlMap", insertedGrpHtml);
						output.put("approverIdGrpMap", insertedApproverIdForGrp);
						output.put("groupIdMopMap", insertedMopForGrp);
						output.put("groupIdsMap", insertedGroupIds);
						output.put("exculudedGroupIds", exculudedGroupIds);
						output.put("groupIdNameMap", l_groupIdNameMap);
						output.put("addedIDForView", addedIDForView);
						output.put("addedGroupLimit", insertedGroupLimit);
						
						
						
						log.info("insertedAccountNum ="+insertedAccountNum);
						
						String[] rejectReasonArr = ApplicationGenParameter.parameterMasterList.get("PORTAL_LIMIT_REQ_REJECT_REASON").split("\\|");
						for(String rejStr : rejectReasonArr) 
						{
							rejReasonList.add(rejStr);
						}
						
						output.put("rejReasonList", rejReasonList);
						log.info("insertedApproverIdForGrp ="+insertedApproverIdForGrp);
						
						
						rset.close();
						pStatement.close();
						
					}
				

				
					
					
					
					/**********************************************************************************************************************************************************************/
				
				}

			} catch (SQLException ex) {
				log.info("SQLException :" , ex);
				// ex.printStackTrace();
				output.put("error", "something went wrong");
				output.put("module", moduleDetails);
				return output;

			} catch (Exception ex) {
				log.info("Exception :", ex);
				// ex.printStackTrace();
				output.put("error", "something went wrong");
				output.put("module", moduleDetails);
				return output;
			} finally {
				try {
					if (rset != null)
						rset.close();

					if (pStatement != null)
						pStatement.close();

					if (connection != null)
						connection.close();
				} catch (SQLException ex) {
					log.info(ex);
				}
			}
			log.info("End of the buildGroupList method");
			output.put("success", "");
			output.put("module", moduleDetails);
		}

		return output;
	}

	
	
	
	@SuppressWarnings("unchecked")
	public Map<String, String> sortMap(Map map) {
		Map<String, String> treeMap = new TreeMap<>(map);
		Iterator iterator = treeMap.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
		}

		return treeMap;
	}

	/**
	 * @param businessDate
	 *            the businessDate to set
	 */
	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * @param businessDateSQL
	 *            the businessDateSQL to set
	 */
	public void setBusinessDateSQL(java.sql.Date businessDateSQL) {
		this.businessDateSQL = businessDateSQL;
	}

	/**
	 * @param dateFormat
	 *            the dateFormat to set
	 */
	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @param session
	 *            the session to set
	 */
	public void setSession(Map session) {
		this.session = session;
	}

	/**
	 * @param userID
	 *            the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the businessDate
	 */
	public Date getBusinessDate() {
		return businessDate;
	}

	/**
	 * @return the businessDateSQL
	 */
	public java.sql.Date getBusinessDateSQL() {
		return businessDateSQL;
	}

	/**
	 * @return the dateFormat
	 */
	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	/**
	 * @return the session
	 */
	public Map getSession() {
		return session;
	}

	/**
	 * @return the userID
	 */
	public String getUserID() {
		return userID;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	@Override
	public Map<String, String> saveMultiple(
			ApprovalMappingMaster approvalMappingMaster, String mode,
			String selectedData, int totalScore, String serviceCDId,
			String acType, String accNumber) {
		log.info("serviceCDId=" + serviceCDId);

		Map<String, String> message = new LinkedHashMap<>();
		Connection connection = null;
		ResultSet resultSet = null, rs = null;
		PreparedStatement pStatement = null, ps = null, pStatement1 = null;

		String[] arr = serviceCDId.split("\\|");

		try {
			if (arr[0] != "" && arr[0] != null) {
				connection = getConnection();

				long dailyLimit = Long.parseLong(arr[0].split("@")[1]);

				for (int i = 0; i < arr.length; i++) {
					log.info("Inside saveMulti ApprovalMappingMasterDAOImpl"
							+ selectedData + "totalScore" + totalScore);
					ps = connection
							.prepareStatement("SELECT GROUP_ID,LIMIT_REQUIRED FROM COM_SERVICEGROUP WHERE GROUP_NAME=?");
					ps.setString(1, arr[i].split("@")[0]);
					rs = ps.executeQuery();
					int groupCd = 0;
					while (rs.next()) {
						groupCd = rs.getInt(1);
					}
					rs.close();
					ps.close();
					int count = 0;

					try {

						selectedData = selectedData.replaceAll("@", "#");
						if ("new".equalsIgnoreCase(mode)) {

							//SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
							pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SELECT_COUNT"));
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, approvalMappingMaster.getAccountNo().trim().toUpperCase());
							//log.info("getAccountNo()="+ approvalMappingMaster.getAccountNo().trim().toUpperCase());
							pStatement.setString(3, String.valueOf(groupCd));
							log.info("arr[i]=" + String.valueOf(groupCd));

							resultSet = pStatement.executeQuery();
							while (resultSet.next()) {
								count = resultSet.getInt(1);
								// serviceName=resultSet.getString(2);
							}
							resultSet.close();
							pStatement.close();
							if (count != 0) {
								message.put("error"," Company ID Already Exist for : "+ arr[i].split("@")[0]);
								return message;
							}

							pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_SELECT_COUNT"));
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, approvalMappingMaster.getAccountNo().trim().toUpperCase());
							pStatement.setString(3, String.valueOf(groupCd));
							resultSet = pStatement.executeQuery();

							while (resultSet.next()) {
								count = resultSet.getInt(1);
							}
							resultSet.close();
							pStatement.close();
							if (count != 0) {
								 message.put("error"," Company ID Already Exist for : "+arr[i].split("@")[0]);
								 return message;
							}
							// modified code by rasmiranjan -04122017
							pStatement = connection.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_SELECT_COUNT"));
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, approvalMappingMaster.getAccountNo().trim().toUpperCase());
							pStatement.setString(3, String.valueOf(groupCd));
							resultSet = pStatement.executeQuery();

							while (resultSet.next()) {
								count = resultSet.getInt(1);
							}
							resultSet.close();
							pStatement.close();

							if (count != 0) {
								message.put("error"," Company ID Already Exist for : "+ arr[i].split("@")[0]);
								return message;
							}
							pStatement = connection.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_TMP_SELECT_COUNT"));
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, approvalMappingMaster.getAccountNo().trim().toUpperCase());
							pStatement.setString(3, String.valueOf(groupCd));
							resultSet = pStatement.executeQuery();

							while (resultSet.next()) {
								count = resultSet.getInt(1);
							}
							resultSet.close();
							pStatement.close();
							if (count != 0) {
								message.put("error"," Company ID Already Exist for : " +arr[i].split("@")[0]);
								return message;
							}

							long runningSeqNo = 0, runningSeqNo1 = 0;

							// SELECT COM_APPROVAL_GRP_MST_SEQ.NEXTVAL FROM DUAL
							pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SEQ_NEXTVAL"));
							resultSet = pStatement.executeQuery();
							while (resultSet.next()) {
								runningSeqNo = resultSet.getLong(1);
							}
							resultSet.close();
							pStatement.close();
							// write code to get running seq no
							// connection.setAutoCommit(false);
							pStatement = connection.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?,?,?,?)");
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
							pStatement.setString(2, accNumber);
							pStatement.setString(3, String.valueOf(groupCd));
							pStatement.setLong(4, runningSeqNo);
							pStatement.setString(5, "N");
							pStatement.setString(6, userID);
							pStatement.setString(7, null);
							pStatement.setDate(8, null);
							pStatement.setDate(9, null);
							pStatement.setString(10, approvalMappingMaster.getActiveFlag().trim());
							pStatement.setInt(11, 0);
							pStatement.setString(12, selectedData);
							pStatement.setInt(13, totalScore);
							log.info("approvalMappingMaster.getFileOptionH() "+ approvalMappingMaster.getFileOptionH());
							pStatement.setString(14, acType);
							pStatement.setString(15, bankID);
							log.info("dailyLimit=" + arr[i].split("@")[1]);
							pStatement.setString(16, arr[i].split("@")[1]);
							pStatement.executeUpdate();
							pStatement.close();
							log.info("save to database success");
						}
					} catch (Exception e) {
						log.info(e);
						// connection.rollback();
						message.put("error", path.getMessage("E1003"));
						return message;
					}

				}
			}
		} catch (Exception e) {
			log.info("message=", e);
		} finally {
			try {

				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.info("message=", e);
			}
		}

		if ("new".equalsIgnoreCase(mode)) {
			message.put("success", path.getMessage("E1007"));
		}

		return message;
	}

	@Override
	public synchronized Map<String, String> saveMultipleInSingleGo(ApprovalMappingMaster approvalMappingMaster, String mode)
	{
		log.info(" Inside saveMultipleInSingleGo in ApprovalMappingMasterDAOImpl() p_custID ::"+mode);
		
		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		PreparedStatement pStatement1 = null;
		ResultSet resultSet1 = null;
		Map<String,String> groupIDNameMap = new HashMap<String, String>();
		Map<String, String> message = new LinkedHashMap<>();
		Map<String,Integer> approverIdScoreMap = new HashMap<String, Integer>();
		boolean l_presentFlag = false;
		try {
			connection = getConnection();
			
			//SELECT GROUP_ID,GROUP_NAME FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'
			pStatement = connection.prepareStatement("SELECT GROUP_ID,GROUP_NAME,LIMIT_REQUIRED FROM COM_SERVICEGROUP WHERE AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y'");
			resultSet = pStatement.executeQuery();
			while(resultSet.next()) 
			{
				groupIDNameMap.put(resultSet.getString(1), resultSet.getString(2));
			}
			resultSet.close();
			pStatement.close();
			
			//select APPROVER_CD,APPROVER_CD from COM_APPROVE_MST where CUSTOMER_ID=?
			pStatement = connection.prepareStatement("select APPROVER_CD,APPROVER_ID from COM_APPROVE_MST where CUSTOMER_ID=? and ACTIVE_FLAG = 'Y'");
			pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
			resultSet = pStatement.executeQuery();
			while(resultSet.next()) 
			{
				approverIdScoreMap.put(resultSet.getString(1), resultSet.getInt(2));
			}
			resultSet.close();
			pStatement.close();
			
			
			
			if ("new".equalsIgnoreCase(mode)) 
			{
				
				Iterator accountGroupMapItr = approvalMappingMaster.getApproverAccGroupDailyLimitMap().entrySet().iterator();
				int count = 0;
				
				try {
				
				while(accountGroupMapItr.hasNext()) 
				{
					Map.Entry l_keyValueEntry1 = (Map.Entry)accountGroupMapItr.next();
					
					//SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
					pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SELECT_COUNT"));
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
					log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
					pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					//log.info("getAccountNo()="+ String.valueOf(l_keyValueEntry1.getKey()).split("$")[0].trim().toUpperCase());
					pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					log.info("arr[i]=" + String.valueOf(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));

					resultSet = pStatement.executeQuery();
					if (resultSet.next()) {
						count = resultSet.getInt(1);
						// serviceName=resultSet.getString(2);
					}
					resultSet.close();
					pStatement.close();
					if (count != 0) {
						message.put("error"," Company ID Already Exist for : "+ groupIDNameMap.get(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));
						return message;
					}
					
					
					
//					SELECT COUNT(1) FROM COM_APPROVALMAPPING_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) IN (SELECT SERVICE_CD FROM COM_SERVICE_MST WHERE SERVICE_TYPE=?)
					//pStatement = connection.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_SELECT_COUNT"));
					pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_SELECT_COUNT"));//added by manish on 06 July 2020 found while unit testing
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
					pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					resultSet = pStatement.executeQuery();

					if (resultSet.next()) {
						count = resultSet.getInt(1);
					}
					resultSet.close();
					pStatement.close();

					if (count != 0) {
						message.put("error"," Company ID Already Exist for : "+ groupIDNameMap.get(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));
						return message;
					}
					
					//SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST_TMP  WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) IN (SELECT SERVICE_CD FROM COM_SERVICE_MST WHERE SERVICE_TYPE=?)
					pStatement = connection.prepareStatement(getQuery("COM_APPROVALMAPPING_MST_TMP_SELECT_COUNT"));
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
					pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					resultSet = pStatement.executeQuery();

					if (resultSet.next()) {
						count = resultSet.getInt(1);
					}
					resultSet.close();
					pStatement.close();
					if (count != 0) {
						message.put("error"," Company ID Already Exist for : "+ groupIDNameMap.get(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));
						return message;
					}
					
					// Added in temp table
					long runningSeqNo = 0;

					// SELECT COM_APPROVAL_GRP_MST_SEQ.NEXTVAL FROM DUAL
					pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SEQ_NEXTVAL"));
					resultSet = pStatement.executeQuery();
					if (resultSet.next()) {
						runningSeqNo = resultSet.getLong(1);
					}
					resultSet.close();
					pStatement.close();
					// write code to get running seq no
					log.info("runningSeqNo ="+runningSeqNo);
					// connection.setAutoCommit(false);
					pStatement = connection.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?,?,?,?)");
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
					pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					pStatement.setLong(4, runningSeqNo);
					pStatement.setString(5, "N");
					pStatement.setString(6, userID);
					pStatement.setString(7, null);
					pStatement.setDate(8, null);
					pStatement.setDate(9, null);
					pStatement.setString(10, approvalMappingMaster.getActiveFlag().trim());
					pStatement.setInt(11, 0);
					pStatement.setString(12, approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())));
					
					String[] approverIdArr = approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())).split("\\|");
					int totalScore = 0;
					for(String str : approverIdArr) 
					{
						totalScore = totalScore + approverIdScoreMap.get(str.split("\\#")[0]);
					}
					
					pStatement.setLong(13, totalScore);
					
					//log.info("approvalMappingMaster.getApproverAccountMOPMap() = "+approvalMappingMaster.getApproverAccountMOPMap());
					//log.info("l_keyValueEntry1.getKey() = "+l_keyValueEntry1.getKey());
					pStatement.setString(14, String.valueOf(approvalMappingMaster.getApproverAccountMOPMap().get(String.valueOf(l_keyValueEntry1.getKey()).replace("$", "~GRP"))));
					pStatement.setString(15, bankID);
					log.info("dailyLimit=" + l_keyValueEntry1.getValue());
					pStatement.setString(16, String.valueOf(l_keyValueEntry1.getValue()));
					pStatement.executeUpdate();
					pStatement.close();
					log.info("save to database success");
					
				}

				} catch (Exception e) {
					log.info("",e);
					// connection.rollback();
					message.put("error", path.getMessage("E1003"));
					return message;
				}
				
			}
			else if("update".equalsIgnoreCase(mode.replace(", ", ""))) 
			{

				
				// Delete or Active Status flag false if update request is remove account or group of any 
				if(approvalMappingMaster.getUnmappedAccAndSerArrList().size() > 0) 
				{
					ArrayList<String> uniqueAccAndGrpList = new ArrayList<String>(); 
					for(int index=0; index<approvalMappingMaster.getUnmappedAccAndSerArrList().size();index++) 
					{
						if(!uniqueAccAndGrpList.contains(approvalMappingMaster.getUnmappedAccAndSerArrList().get(index).split("GRP")[0])) 
						{
							uniqueAccAndGrpList.add(approvalMappingMaster.getUnmappedAccAndSerArrList().get(index));
						}
					}
					
				//	log.info("uniqueAccAndGrpList ="+uniqueAccAndGrpList);
					
					for(int index=0; index<uniqueAccAndGrpList.size();index++) 
					{
						
						boolean isExistFlag = false;
						long existId = 0;
						int existLastChangeId = 0;
						String l_accountSerId = uniqueAccAndGrpList.get(index);
						
						
						if(l_accountSerId.contains("GRP")) 
						{
							// If request from main master table
							pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, l_accountSerId.split("GRP")[0]);
							//log.info("getAccountNo()="+ l_accountSerId.split("GRP")[0]);
							pStatement.setString(3, l_accountSerId.split("GRP")[1]);
							//log.info("arr[i]=" + l_accountSerId.split("GRP")[1]);
							
							resultSet = pStatement.executeQuery();
							if (resultSet.next()) {
								isExistFlag = true;
								
								//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
								pStatement1 = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
								pStatement1.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
								log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
								pStatement1.setString(2, l_accountSerId.split("GRP")[0]);
								//log.info("getAccountNo()="+ l_accountSerId.split("GRP")[0]);
								pStatement1.setString(3, l_accountSerId.split("GRP")[1]);
								log.info("arr[i]=" + l_accountSerId.split("GRP")[1]);

								pStatement1.executeUpdate();
								pStatement1.close();
								
								
								//Insert into COM_APPROVAL_GRP_MST (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE) values (?,?,?,?,?,?,sysdate,?,null,null,null,?,?,?,?,?,?,?,"");
								pStatement1 = connection.prepareStatement("Insert into COM_APPROVAL_GRP_MST_TMP (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE) values (?,?,?,?,?,?,sysdate,?,null,null,null,?,?,?,?,?,?,?,'')");
								pStatement1.setString(1, resultSet.getString("SERVICECDID"));
								pStatement1.setString(2, resultSet.getString("ACCOUNT_NO"));
								pStatement1.setString(3, resultSet.getString("SERVICE_CD"));
								pStatement1.setLong(4, resultSet.getLong("ID"));
								pStatement1.setString(5, "M");
								pStatement1.setString(6, userID);
								pStatement1.setDate(7, businessDateSQL);
								pStatement1.setString(8, "N");
								pStatement1.setInt(9, resultSet.getInt("LAST_CHANGE_ID")+1);
								pStatement1.setString(10, resultSet.getString("APPROVER_LIST"));
								pStatement1.setString(11, resultSet.getString("OPERATION_MODE"));
								pStatement1.setString(12, resultSet.getString("TOTAL_SCORE"));
								pStatement1.setString(13, resultSet.getString("BANK_ID"));
								pStatement1.setString(14, resultSet.getString("DAILY_LIMIT"));
								pStatement1.executeUpdate();
								pStatement1.close();
								
							}
							
							resultSet.close();
							pStatement.close();
							
							// if request from temp table
							if(!isExistFlag) 
							{
								//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
								pStatement = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
								pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
								log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
								pStatement.setString(2, l_accountSerId.split("GRP")[0]);
							//	log.info("getAccountNo()="+ l_accountSerId.split("GRP")[0]);
								pStatement.setString(3, l_accountSerId.split("GRP")[1]);
								//log.info("arr[i]=" + l_accountSerId.split("GRP")[1]);

								pStatement.executeUpdate();
								pStatement.close();
							}
							
						}
						else if(l_accountSerId !=null && !"".equals(l_accountSerId))
						{
							pStatement = connection.prepareStatement("SELECT SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ?");
							pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
							log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
							pStatement.setString(2, l_accountSerId);
							//log.info("getAccountNo()="+ l_accountSerId);
							
							resultSet = pStatement.executeQuery();
							while (resultSet.next()) {
								isExistFlag = true;
								
								
								//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
								pStatement1 = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
								pStatement1.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
								log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
								pStatement1.setString(2, l_accountSerId.split("GRP")[0]);
								//log.info("getAccountNo()="+ l_accountSerId.split("GRP")[0]);
								pStatement1.setString(3, resultSet.getString("SERVICE_CD"));
								log.info("arr[i]=" + resultSet.getString("SERVICE_CD"));

								pStatement1.executeUpdate();
								pStatement1.close();
								
								//Insert into COM_APPROVAL_GRP_MST (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE) values (?,?,?,?,?,?,sysdate,?,null,null,null,?,?,?,?,?,?,?,"");
								pStatement1 = connection.prepareStatement("Insert into COM_APPROVAL_GRP_MST_TMP (SERVICECDID,ACCOUNT_NO,SERVICE_CD,ID,AUTH_STATUS,MAKER_CD,MAKER_DT,MAKER_CAL_DT,AUTHOR_CD,AUTHOR_DT,AUTHOR_CAL_DT,ACTIVE_FLAG,LAST_CHANGE_ID,APPROVER_LIST,OPERATION_MODE,TOTAL_SCORE,BANK_ID,DAILY_LIMIT,DATA_SOURCE) values (?,?,?,?,?,?,sysdate,?,null,null,null,?,?,?,?,?,?,?,'')");
								pStatement1.setString(1, resultSet.getString("SERVICECDID"));
								pStatement1.setString(2, resultSet.getString("ACCOUNT_NO"));
								pStatement1.setString(3, resultSet.getString("SERVICE_CD"));
								pStatement1.setLong(4, resultSet.getLong("ID"));
								pStatement1.setString(5, "M");
								pStatement1.setString(6, userID);
								pStatement1.setDate(7, businessDateSQL);
								pStatement1.setString(8, "N");
								pStatement1.setInt(9, resultSet.getInt("LAST_CHANGE_ID")+1);
								pStatement1.setString(10, resultSet.getString("APPROVER_LIST"));
								pStatement1.setString(11, resultSet.getString("OPERATION_MODE"));
								pStatement1.setString(12, resultSet.getString("TOTAL_SCORE"));
								pStatement1.setString(13, resultSet.getString("BANK_ID"));
								pStatement1.setString(14, resultSet.getString("DAILY_LIMIT"));
								pStatement1.executeUpdate();
								pStatement1.close();
								
							}
							
							resultSet.close();
							pStatement.close();
							
							
							// if request from temp table
							if(!isExistFlag) 
							{
								//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
								pStatement = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ?");
								pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
								log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
								pStatement.setString(2, l_accountSerId);
								//log.info("getAccountNo()="+ l_accountSerId);

								pStatement.executeUpdate();
								pStatement.close();
							}
							
						}
							
						}
						
					}
					
				// Added for if no change request comes 
				
				//{111381311111321$7=1, 111381311111321$3=1, 111381311111321$2=1}

				//[111381311111321GRP2, 111381311111321GRP3, 111381311111321GRP7]
				
				for(int index=0; index<approvalMappingMaster.getNoChangeAccSerArrList().size(); index++) 
				{
					if(approvalMappingMaster.getApproverAccGroupDailyLimitMap().containsKey(approvalMappingMaster.getNoChangeAccSerArrList().get(index).replace("GRP","$"))) 
					{
						approvalMappingMaster.getApproverAccGroupDailyLimitMap().remove(approvalMappingMaster.getNoChangeAccSerArrList().get(index).replace("GRP","$"));
					}
				}
				
				
				//log.info("approvalMappingMaster.getApproverAccGroupDailyLimitMap()="+approvalMappingMaster.getApproverAccGroupDailyLimitMap());
				
				Iterator accountGroupMapItr = approvalMappingMaster.getApproverAccGroupDailyLimitMap().entrySet().iterator();
				boolean isExistFlag = false;
				int existLastChangeId = 0;
				long existId = 0;
				
				
				
				try {
				
				while(accountGroupMapItr.hasNext()) 
				{
					isExistFlag = false;
					
					Map.Entry l_keyValueEntry1 = (Map.Entry)accountGroupMapItr.next();
					
					//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
					pStatement = connection.prepareStatement("SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
					pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
					log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
					pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					//log.info("getAccountNo()="+ String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
				//	log.info("arr[i]=" + String.valueOf(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));

					resultSet = pStatement.executeQuery();
					if (resultSet.next()) {
						isExistFlag = true;
						existId = resultSet.getLong(1);
						existLastChangeId = resultSet.getInt(2);
					}
					resultSet.close();
					pStatement.close();
						/*
						 * if (count != 0) { message.put("error"," Company ID Already Exist for : "+
						 * groupIDNameMap.get(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].
						 * trim().toUpperCase())); return message; }
						 */
					if(isExistFlag) 
					{
						isExistFlag = false;
						String serviceCd = "";
						//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
						pStatement = connection.prepareStatement("SELECT ID,LAST_CHANGE_ID,SERVICE_CD FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
						pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
						log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
						pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					//	log.info("getAccountNo()="+ String.valueOf(l_keyValueEntry1.getKey()).split("$")[0].trim().toUpperCase());
						pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					//	log.info("arr[i]=" + String.valueOf(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));

						resultSet = pStatement.executeQuery();
						while (resultSet.next()) {
							isExistFlag = true;
							existId = resultSet.getLong(1);
							serviceCd = resultSet.getString(3);
							//existLastChangeId = resultSet.getInt(2);
						}
						resultSet.close();
						pStatement.close();
						
						if(isExistFlag) 
						{
							//DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ?
							pStatement = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ?");
							pStatement.setLong(1, existId);
							pStatement.executeUpdate();
							pStatement.close();
						}
						
						
						
						
						// write code to get running seq no
						// connection.setAutoCommit(false);
						pStatement = connection.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?,?,?,?)");
						pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
						pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
						pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
						pStatement.setLong(4, existId);
						pStatement.setString(5, "M");
						pStatement.setString(6, userID);
						pStatement.setString(7, null);
						pStatement.setDate(8, null);
						pStatement.setDate(9, null);
						pStatement.setString(10, "Y");
						pStatement.setInt(11, existLastChangeId+1);
						pStatement.setString(12, approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())));
						String[] approverIdArr = approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())).split("\\|");
						int totalScore = 0;
						for(String str : approverIdArr) 
						{
							totalScore = totalScore + approverIdScoreMap.get(str.split("\\#")[0]);
						}
						
						pStatement.setLong(13, totalScore);
						
						//log.info("approvalMappingMaster.getApproverAccountMOPMap() = "+approvalMappingMaster.getApproverAccountMOPMap());
						//log.info("l_keyValueEntry1.getKey() = "+l_keyValueEntry1.getKey());
						pStatement.setString(14, String.valueOf(approvalMappingMaster.getApproverAccountMOPMap().get(String.valueOf(l_keyValueEntry1.getKey()).replace("$", "~GRP"))));
						pStatement.setString(15, bankID);
						log.info("dailyLimit=" + l_keyValueEntry1.getValue());
						pStatement.setString(16, String.valueOf(l_keyValueEntry1.getValue()));
						pStatement.executeUpdate();
						pStatement.close();
						log.info("save to database success");
					}
					else 
					{
						
						// Added in temp table
						long runningSeqNo = 0;
						
						//SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?
						pStatement = connection.prepareStatement("SELECT ID,LAST_CHANGE_ID FROM COM_APPROVAL_GRP_MST_TMP WHERE UPPER(SERVICECDID) = ? AND UPPER(ACCOUNT_NO) = ? AND UPPER(SERVICE_CD) = ?");
						pStatement.setString(1, approvalMappingMaster.getCustomerID().trim().toUpperCase());
						log.info("getCustomerID()="+ approvalMappingMaster.getCustomerID().trim().toUpperCase());
						pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
					//	log.info("getAccountNo()="+ String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
						pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
					//	log.info("arr[i]=" + String.valueOf(String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase()));

						resultSet = pStatement.executeQuery();
						while (resultSet.next()) {
							isExistFlag = true;
							existId = resultSet.getLong(1);
							//existLastChangeId = resultSet.getInt(2);
						}
						resultSet.close();
						pStatement.close();
						
						if(!isExistFlag) 
						{
							// SELECT COM_APPROVAL_GRP_MST_SEQ.NEXTVAL FROM DUAL
							pStatement = connection.prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SEQ_NEXTVAL"));
							resultSet = pStatement.executeQuery();
							while (resultSet.next()) {
								runningSeqNo = resultSet.getLong(1);
							}
							resultSet.close();
							pStatement.close();
						}
						else 
						{
							//DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ?
							pStatement = connection.prepareStatement("DELETE FROM COM_APPROVAL_GRP_MST_TMP WHERE ID = ?");
							pStatement.setLong(1, existId);
							pStatement.executeUpdate();
							pStatement.close();
							runningSeqNo = existId;
						}
						
						
						// write code to get running seq no
						// connection.setAutoCommit(false);
						pStatement = connection.prepareStatement("INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID,DAILY_LIMIT) VALUES(?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?,?,?,?)");
						pStatement.setString(1, approvalMappingMaster.getCustomerID().trim());
						pStatement.setString(2, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[0].trim().toUpperCase());
						pStatement.setString(3, String.valueOf(l_keyValueEntry1.getKey()).split("\\$")[1].trim().toUpperCase());
						pStatement.setLong(4, runningSeqNo);
						pStatement.setString(5, "N");
						pStatement.setString(6, userID);
						pStatement.setString(7, null);
						pStatement.setDate(8, null);
						pStatement.setDate(9, null);
						pStatement.setString(10, approvalMappingMaster.getActiveFlag().trim());
						pStatement.setInt(11, 0);
						pStatement.setString(12, approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())));
						String[] approverIdArr = approvalMappingMaster.getApproverAccountApproverLimitMap().get(String.valueOf(l_keyValueEntry1.getKey())).split("\\|");
						int totalScore = 0;
						for(String str : approverIdArr) 
						{
							totalScore = totalScore + approverIdScoreMap.get(str.split("\\#")[0]);
						}
						
						pStatement.setLong(13, totalScore);
						
						//log.info("approvalMappingMaster.getApproverAccountMOPMap() = "+approvalMappingMaster.getApproverAccountMOPMap());
					//	log.info("l_keyValueEntry1.getKey() = "+l_keyValueEntry1.getKey());
						pStatement.setString(14, String.valueOf(approvalMappingMaster.getApproverAccountMOPMap().get(String.valueOf(l_keyValueEntry1.getKey()).replace("$", "~GRP"))));
						pStatement.setString(15, bankID);
						log.info("dailyLimit=" + l_keyValueEntry1.getValue());
						pStatement.setString(16, String.valueOf(l_keyValueEntry1.getValue()));
						pStatement.executeUpdate();
						pStatement.close();
						log.info("save to database success");
					}
				
					
				}

				} catch (Exception e) {
					log.info("",e);
					// connection.rollback();
					message.put("error", path.getMessage("E1003"));
					return message;
				}
				
			
			}
			
			
		} catch (Exception e) {
			log.info("message=", e);
		} finally {
			try {

				if (resultSet1 != null) {
					resultSet1.close();
				}
				if (pStatement1 != null) {
					pStatement1.close();
				}
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				
				if (connection != null) {
					connection.close();
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.info("message=", e);
			}
		}

		if ("new".equalsIgnoreCase(mode)) {
			message.put("success", path.getMessage("E1007"));
		}
		else if ("update".equalsIgnoreCase(mode.replace(", ", ""))) {
			message.put("success", path.getMessage("E1008"));
		}

		return message;
	}
	
/*	@Override
	public Map<String, String> getapproverName(String p_custID) {
		log.info(" Inside getapproverName in ApprovalMappingMasterDAOImpl() p_custID ::"+ p_custID);

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		Map<String, String> l_approverData = new HashMap<String,String>();
		boolean l_presentFlag = false;
		try {
			connection = getConnection();
			// SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE
			// COD_CUST_ID IN (SELECT APPROVER_CD FROM COM_APPROVE_MST WHERE
			// AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID IN (?))
			if(p_custID.contains(",")) 
			{
				p_custID = p_custID.replace(",", "','");
			}
			String sql = getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + p_custID + "'))";
			//log.info("sql = "+sql);
			pStatement = connection.prepareStatement(sql);
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				if (resultSet.getString(1).length() > 0
						&& resultSet.getString(2).length() > 0) {
					//log.info("resultSet.getString(1)"+resultSet.getString(1));
					l_approverData.put(resultSet.getString(1).trim(), resultSet
							.getString(2).trim());
					l_presentFlag = true;
				}
			}
			pStatement.close();
			
			if (l_presentFlag == false) {
				
			
	
				// change for if only company id comes by 2157 on 27112019
				if(p_custID.contains(",")) 
				{
					pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "'))");
				}
				else 
				{
					////SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE COD_CUST_ID IN (select distinct COD_CUST from VW_CH_ACCT_CUST_XREF where COD_ACCT_NO in (SELECT COD_ACCT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST= '113500' AND ((COD_PROD BETWEEN 501 AND 599)  OR (COD_PROD BETWEEN 901 AND 999) OR (COD_PROD BETWEEN 601 AND 899))))
					pStatement = connection.prepareStatement("SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE COD_CUST_ID IN (select distinct COD_CUST from VW_CH_ACCT_CUST_XREF where COD_ACCT_NO in (SELECT COD_ACCT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST= ? AND ((COD_PROD BETWEEN 501 AND 599)  OR (COD_PROD BETWEEN 901 AND 999) OR (COD_PROD BETWEEN 601 AND 899))))");
					pStatement.setString(1, p_custID);
				}
			
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString(1).length() > 0
							&& resultSet.getString(2).length() > 0) {
						l_approverData.put(resultSet.getString(1).trim(),resultSet.getString(2).trim());
					}
				}
				pStatement.close();
				resultSet.close();

			}
			log.info("APPROVER CD AND APPROVER NAME ::" + l_approverData);
		} catch (SQLException ex) {
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}

		return l_approverData;
	}*/
	
	/*@Override
	public Map<String, String> getapproverName(String p_custID) {
		log.info(" Inside getapproverName in ApprovalMappingMasterDAOImpl() p_custID ::"+ p_custID);

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		Map<String, String> l_approverData = new HashMap<>();
		boolean l_presentFlag = false;
		try {
			connection = getConnection();
			// SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE
			// COD_CUST_ID IN (SELECT APPROVER_CD FROM COM_APPROVE_MST WHERE
			// AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID IN (?))

			if(!p_custID.contains(","))
			{
				p_custID = "'"+p_custID+"'";
			}
			log.info("Query From getapproverName........"+getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN (" + p_custID + "))");
			pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN (" + p_custID + "))");
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				if (resultSet.getString(1).length() > 0
						&& resultSet.getString(2).length() > 0) {
					l_approverData.put(resultSet.getString(1).trim(), resultSet
							.getString(2).trim());
					l_presentFlag = true;
				}
			}
			pStatement.close();
			resultSet.close();
			if (l_presentFlag == false) {
				log.info("Query From getapproverName........"+getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY") + " AND CUSTOMER_ID IN (" + p_custID + "))");
				
			pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "')");
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString(1).length() > 0
							&& resultSet.getString(2).length() > 0) {
						l_approverData.put(resultSet.getString(1).trim(),resultSet.getString(2).trim());
					}
				}
				pStatement.close();
				resultSet.close();

			}
			log.info("APPROVER CD AND APPROVER NAME ::" + l_approverData);
		} catch (SQLException ex) {
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}

		return l_approverData;
	}*/






	@Override

	public Map<String, String> getapproverName(String p_custID) {
		log.info(" Inside getapproverName in ApprovalMappingMasterDAOImpl() p_custID ::");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		Map<String, String> l_approverData = new HashMap<>();
		boolean l_presentFlag = false;
		try {
			String[] custList = p_custID.split(",");
			connection = getConnection();
			// SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE
			// COD_CUST_ID IN (SELECT APPROVER_CD FROM COM_APPROVE_MST WHERE
			// AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID IN (?))
			try {
				
				if(p_custID.contains(",")) 
				{
					p_custID = p_custID.replace(",", "','");
				}
				String sql = getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + p_custID + "'))";
				//log.info("sql = "+sql);
				
				//log.info(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN (" + p_custID + "))");
			pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + p_custID + "'))");
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				//log.info(resultSet.getString(1) +"and "+resultSet.getString(2));
				if (resultSet.getString(1).length() > 0
						&& resultSet.getString(2).length() > 0) {
					l_approverData.put(resultSet.getString(1).trim(), resultSet
							.getString(2).trim());
					l_presentFlag = true;
				}
			}
			pStatement.close();
			resultSet.close();
			}
			// try and catch added by 2330 on 15-06-2020
			catch(Exception e) {
				//l_presentFlag = false;
				log.info("Exception ",e);
			}
			//extra added by 2330 non 15-06
			if (l_presentFlag == false) {
				
				for (int i = 0;i<custList.length;i++) {
					log.info("SQL "+getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + custList[i] + "'))");
					pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + custList[i] + "'))");
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString(1).length() > 0
							&& resultSet.getString(2).length() > 0) {
						l_approverData.put(resultSet.getString(1).trim(),resultSet.getString(2).trim());
					}
				}
				pStatement.close();
				resultSet.close();

			}}
			//end
			
			if (l_presentFlag == false) {
				log.info("SQL "+getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "')");
			pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "')");
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					if (resultSet.getString(1).length() > 0
							&& resultSet.getString(2).length() > 0) {
						l_approverData.put(resultSet.getString(1).trim(),resultSet.getString(2).trim());
					}
				}
				pStatement.close();
				resultSet.close();

			}
			log.info("APPROVER CD AND APPROVER NAME ::" );
		} catch (SQLException ex) {
			
			
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}

		return l_approverData;
	}
	@Override
	public Map<String, String> getapproverNameForMaxCusId(Set<String> p_setList) {
		log.info("Inside in getapproverNameForMaxCusId() p_setList ::");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		Map<String, String> l_approverData = new HashMap<>();
		boolean l_presentFlag = false;
		double l_addCnt = 0;

		try {
			connection = getConnection();

			l_addCnt = p_setList.size() / 500.0;
			log.info("Customer Id set Divided Cnt :: "+l_addCnt);
			int l_rationalInt = (int)(Math.ceil(l_addCnt));
			log.info("Set Rational Int :: "+l_rationalInt);
			List<Set<String>> l_totalSetList = split(p_setList,l_rationalInt);
			log.info("l_totalSetList split Data ::" +" And Size :: "+l_totalSetList.size());

				for(int i = 0 ; i < l_totalSetList.size() ; i++)
				{
					String l_iterator = "";
					String p_custID = "";
					Set<String> l_newGeneratedSet = l_totalSetList.get(i);
					Iterator<String> iterator = l_newGeneratedSet.iterator();
					while (iterator.hasNext()) {
						l_iterator = l_iterator + iterator.next() + ",";
					}
					p_custID = l_iterator.trim().replaceAll(",$", "");
					
					log.info(p_custID.toString());
					// SELECT COD_CUST_ID,NAM_CUST_FULL FROM MV_CI_CUSTMAST_ALL WHERE
					// COD_CUST_ID IN (SELECT APPROVER_CD FROM COM_APPROVE_MST WHERE
					// AUTH_STATUS = 'A' AND ACTIVE_FLAG = 'Y' AND CUSTOMER_ID IN (?))
					if(p_custID.contains(",")) 
					{
						p_custID = p_custID.replace(",", "','");
					}
					String sql = getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + p_custID + "'))";
					//log.info("sql = "+sql);
					
					//log.info("SQL " +getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN (" + p_custID + "))");
					pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL") + " AND CUSTOMER_ID IN ('" + p_custID + "'))");
					resultSet = pStatement.executeQuery();
					while (resultSet.next()) {
						if (resultSet.getString(1).length() > 0 && resultSet.getString(2).length() > 0) {
							l_approverData.put(resultSet.getString(1).trim(), resultSet.getString(2).trim());
							l_presentFlag = true;
						}

					}
					pStatement.close();
					resultSet.close();
					if (l_presentFlag == false) {
						log.info("SQL "+getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "')");
						pStatement = connection.prepareStatement(getQuery("SELECT_APPROVER_NAME_MV_CI_CUSTMAST_ALL_ONLY")+ " ('" + p_custID + "')");
						resultSet = pStatement.executeQuery();
						while (resultSet.next()) {
							if (resultSet.getString(1).length() > 0 && resultSet.getString(2).length() > 0) {
								l_approverData.put(resultSet.getString(1).trim(),resultSet.getString(2).trim());
							}
						}

						pStatement.close();
						resultSet.close();

					}
				}
			log.info("APPROVER CD AND APPROVER NAME ::" );
		} catch (SQLException ex) {
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}

		return l_approverData;
	}


	public static <T> List<Set<T>> split(Set<T> original, int count) {
	    // Create a list of sets to return.
	    ArrayList<Set<T>> result = new ArrayList<>(count);

	    // Create an iterator for the original set.
	    Iterator<T> it = original.iterator();

	    // Calculate the required number of elements for each set.
	    int each = original.size() / count;

	    // Create each new set.
	    for (int i = 0; i < count; i++) {
	        HashSet<T> s = new HashSet<>(original.size() / count + 1);
	        result.add(s);
	        for (int j = 0; j < each && it.hasNext(); j++) {
	            s.add(it.next());
	        }
	    }
	    return result;
	}

	@Override
	public Set<String> getapprovercustomerID(String p_Source) {
		log.info("Inside getapprovercustomerID in ApprovalMappingMasterDAOImpl()");
		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		Set<String> l_custidSet = new HashSet<>();

		try {
			connection = getConnection();
			/* Added By 2233 For Customer Name Show on Grid */
			if (p_Source.equalsIgnoreCase("A")) {
				pStatement = connection.prepareStatement("SELECT A.SERVICECDID FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.MAKER_CD <> ? AND A.SERVICE_CD=B.GROUP_ID UNION ALL SELECT AA.SERVICECDID FROM COM_APPROVAL_GRP_MST AA,COM_SERVICEGROUP BB WHERE AA.AUTH_STATUS != 'R' AND AA.SERVICE_CD=BB.GROUP_ID AND AA.ID NOT IN (SELECT C.ID FROM COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
				pStatement.setString(1, userID);
			} else {
				pStatement = connection.prepareStatement("SELECT A.SERVICECDID FROM COM_APPROVAL_GRP_MST_TMP A,COM_SERVICEGROUP B WHERE A.AUTH_STATUS != 'R' AND A.SERVICE_CD=B.GROUP_ID UNION ALL SELECT AA.SERVICECDID FROM COM_APPROVAL_GRP_MST AA,COM_SERVICEGROUP BB WHERE AA.AUTH_STATUS != 'R' AND AA.SERVICE_CD=BB.GROUP_ID AND AA.ID NOT IN (SELECT C.ID FROM COM_APPROVAL_GRP_MST_TMP C WHERE C.AUTH_STATUS != 'R')");
			}
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				l_custidSet.add(resultSet.getString(1));
			}
		//	log.info("CUSTOMER APPROVER ID ::" + l_custidSet);
			resultSet.close();
			pStatement.close();

		} catch (SQLException ex) {
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}
		return l_custidSet;
	}

	/*
	 * FOR COD_CUST_REL issue added by Prabhat and Nana on 05-JUN-2018
	 */

	@Override
	public String getapproverAccRel(String p_approverId) {
		log.info("Inside getapproverAccRel in ApprovalMappingMasterDAOImpl()");

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;
		StringBuilder l_codrelBuilder = new StringBuilder();

		try {
			connection = getConnection();
			//SELECT_VW_CH_ACCT_CUST_XREF_CUST_REL_ACCT_NO=SELECT COD_ACCT_CUST_REL,COD_ACCT_NO FROM VW_CH_ACCT_CUST_XREF WHERE COD_CUST= ? AND ((COD_PROD BETWEEN 501 AND 599)  OR (COD_PROD BETWEEN 901 AND 999) OR (COD_PROD BETWEEN 601 AND 899))
			pStatement = connection.prepareStatement(getQuery("SELECT_VW_CH_ACCT_CUST_XREF_CUST_REL_ACCT_NO"));
			pStatement.setString(1, p_approverId);
			resultSet = pStatement.executeQuery();
			while (resultSet.next()) {
				if (!l_codrelBuilder.toString().contains(
						resultSet.getString(2).trim() + "#"
								+ resultSet.getString(1).trim()))
					l_codrelBuilder.append(resultSet.getString(2).trim() + "#"
							+ resultSet.getString(1).trim() + "|");
			}
		//	log.info("COD_REL BUILDER VALUE ::" + l_codrelBuilder.toString());
			resultSet.close();
			pStatement.close();

		} catch (SQLException ex) {
			log.info("SQLException ::", ex);
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (pStatement != null) {
					pStatement.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				log.info("SQLException ::", ex);
			}
		}
		return l_codrelBuilder.toString();
	}

	@Override
	public String getapproverAccRelSplit(String p_approverList, String p_accno) {
		//log.info(p_approverList);
		log.info("Inside getapproverAccRelSplit in ApprovalMappingMasterDAOImpl()");
		String l_approverName = "";
		String[] l_approvecodRel;
		try {
			if (p_approverList.length() > 3) {
				l_approvecodRel = p_approverList.toString().split("\\|");
				for (int i = 0; i < l_approvecodRel.length; i++) {
					if (l_approvecodRel[i].split("\\#")[0].equals(p_accno)) {
						l_approverName = l_approvecodRel[i].split("\\#")[1];
					}
				}
			} else {
				l_approverName = p_approverList;
			}
		} catch (Exception ex) {
			log.info("Exception ::", ex);
		}
		log.info("SPLIT APPROVER NAME ::" + l_approverName);
	
		return l_approverName;
	}
	// for future user
	/*
	 * public Map<String, String> saveMultiple( ApprovalMappingMaster
	 * approvalMappingMaster, String mode, String selectedData, int totalScore,
	 * String serviceCDId, String acType) { // TODO Auto-generated method stub
	 * log.info("serviceCDId=" + serviceCDId);
	 *
	 * Map<String, String> message = new LinkedHashMap<String, String>();
	 * Connection connection = null; ResultSet resultSet = null;
	 * PreparedStatement pStatement = null;
	 *
	 * String[] arr = serviceCDId.split("\\|");
	 *
	 * try { if (arr[0] != "" && arr[0] != null) { connection = getConnection();
	 * long dailyLimit=Long.parseLong(arr[0].split("@")[1]); String
	 * groupCd=arr[0].split("@")[0]; for (int i = 0; i < arr.length; i++) {
	 * log.info("Inside saveMulti ApprovalMappingMasterDAOImpl" + selectedData +
	 * "totalScore" + totalScore); int count = 0; String serviceName = "";
	 * String count2 = "";
	 *
	 * try {
	 *
	 * selectedData = selectedData.replaceAll("@", "#"); if
	 * ("new".equalsIgnoreCase(mode)) {
	 *
	 * pStatement = connection
	 * .prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SELECT_COUNT"));
	 * pStatement.setString(1,
	 * approvalMappingMaster.getCustomerID().trim().toUpperCase());
	 * log.info("getCustomerID()=" + approvalMappingMaster.getCustomerID()
	 * .trim().toUpperCase()); pStatement.setString(2, approvalMappingMaster
	 * .getAccountNo().trim().toUpperCase()); log.info("getAccountNo()=" +
	 * approvalMappingMaster.getAccountNo() .trim().toUpperCase());
	 * pStatement.setString(3, groupCd); log.info("arr[i]=" + arr[i]);
	 *
	 * resultSet = pStatement.executeQuery(); while (resultSet.next()) { count =
	 * resultSet.getInt(1); serviceName=resultSet.getString(2); }
	 * resultSet.close(); pStatement.close(); if (count != 0) {
	 * message.put("error","- Company ID Already Exist for "+serviceName);
	 * return message; }
	 *
	 * pStatement = connection
	 * .prepareStatement(getQuery("COM_APPROVAL_GRP_MST_TMP_SELECT_COUNT"));
	 * pStatement.setString(1, approvalMappingMaster
	 * .getCustomerID().trim().toUpperCase()); pStatement.setString(2,
	 * approvalMappingMaster .getAccountNo().trim().toUpperCase());
	 * pStatement.setString(3, arr[i]); resultSet = pStatement.executeQuery();
	 *
	 * while (resultSet.next()) { count = resultSet.getInt(1);
	 * serviceName=resultSet.getString(2); } resultSet.close();
	 * pStatement.close(); if (count != 0) {
	 * message.put("error","- Company ID Already Exist for "+serviceName);
	 * return message; }
	 *
	 * long runningSeqNo = 0;
	 *
	 * // SELECT COM_APPROVAL_GRP_MST_SEQ.NEXTVAL FROM // DUAL pStatement =
	 * connection
	 * .prepareStatement(getQuery("COM_APPROVAL_GRP_MST_SEQ_NEXTVAL"));
	 *
	 * resultSet = pStatement.executeQuery(); while (resultSet.next()) {
	 * runningSeqNo = resultSet.getLong(1); } resultSet.close();
	 * pStatement.close();
	 *
	 * // write code to get running seq no
	 *
	 * pStatement = connection .prepareStatement(
	 * "INSERT INTO COM_APPROVAL_GRP_MST_TMP(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,APPROVER_LIST,TOTAL_SCORE,OPERATION_MODE,BANK_ID) VALUES(?, ?, ?, ?, ?, ?, ?, sysdate, ?, ?, ?, ?, ?,?,?,?,?)"
	 * ); pStatement.setString(1, approvalMappingMaster
	 * .getCustomerID().trim()); pStatement.setString(2, approvalMappingMaster
	 * .getAccountNo().trim()); pStatement.setString(3, arr[i]);
	 * pStatement.setLong(4, runningSeqNo); pStatement.setString(5, "N");
	 * pStatement.setString(6, userID); pStatement.setDate(7, businessDateSQL);
	 * pStatement.setString(8, null); pStatement.setDate(9, null);
	 * pStatement.setDate(10, null); pStatement.setString(11,
	 * approvalMappingMaster .getActiveFlag().trim()); pStatement.setInt(12, 0);
	 * pStatement.setString(13, selectedData); pStatement.setInt(14,
	 * totalScore); log.info("approvalMappingMaster.getFileOptionH() " +
	 * approvalMappingMaster.getFileOptionH()); pStatement.setString(15,
	 * acType); pStatement.setString(16, bankID); //
	 * pStatement.setString(14,approvalMappingMaster.getFileOptionH());
	 * pStatement.executeUpdate(); pStatement.close();
	 * log.info("save to database success"); } } catch (Exception e) {
	 * log.error(e); message.put("error", path.getMessage("E1003")); return
	 * message; }
	 *
	 * } } } catch (Exception e) { log.error("message=" + e); } finally { try {
	 *
	 * if (resultSet != null) { resultSet.close(); } if (pStatement != null) {
	 * pStatement.close(); } if (connection != null) { connection.close(); }
	 *
	 * } catch (SQLException e) { // TODO Auto-generated catch block
	 * log.info("message=", e); } }
	 *
	 * if ("new".equalsIgnoreCase(mode)) { message.put("success",
	 * path.getMessage("E1007")); }
	 *
	 * return message; }
	 */


}
