package com.credentek.sme.limit.maintanance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.credentek.sme.common.action.Utils;
import com.credentek.sme.database.DAOBase;
import com.credentek.sme.eod.model.ServiceMaster;
import com.credentek.sme.framework.util.ApplicationGenParameter;
import com.credentek.sme.framework.util.AuditTrail;
import com.credentek.sme.framework.util.PathRead;
import com.opensymphony.xwork2.ActionContext;

public class ServiceMasterDAOImpl extends DAOBase implements ServiceMasterDAO {

	private static Logger log = LogManager.getLogger(ServiceMasterDAOImpl.class);

	private Date businessDate;
	private java.sql.Date businessDateSQL;
	private SimpleDateFormat dateFormat;
	private Map session = ActionContext.getContext().getSession();
	private String userID;
	private String userName;
	private String bankID;
	PathRead path = PathRead.getInstance();



	public ServiceMasterDAOImpl(){
		log.info("In ServiceMasterDAOImpl Constructor");
		//userID = session.get("userCode").toString();
		try
		{
		if(session!=null && session.get("userCode")!=null)
		{
				userID = String.valueOf(session.get("userCode"));
				log.info("userID="+userID);
				dateFormat = new SimpleDateFormat(ApplicationGenParameter.parameterMasterList.get("sessionDateFormat"));
				log.info("businessDate ==="+session.get("businessDate"));
				if(session.get("businessDate")!=null) {
				businessDate = dateFormat.parse(String.valueOf(session.get("businessDate")));
				businessDateSQL = new java.sql.Date(businessDate.getTime());
				}
				//bankID = session.get("bankID").toString();
				bankID = 	ApplicationGenParameter.parameterMasterList.get("BANK_ID");
				log.info("bankID "+bankID);
				dateFormat =new SimpleDateFormat(ApplicationGenParameter.parameterMasterList.get("sessionDateFormat"));

			}
		}
		catch (ParseException pe)
		{
			log.info("ParseException ::",pe);
		}
		catch (Exception e)
		{
			log.info("Exception ::",e);
		}
		}


	@Override
	public List<ServiceMaster> populateGridShowData(List<ServiceMaster> serviceMasterList, int from, int to)
	{
		return serviceMasterList.subList(from, to);
	}

	@Override
	public Integer getListCount(List<ServiceMaster> list)
	{
		return list.size();
	}


	@Override
	public List<ServiceMaster> buildList(String source)
	{

		List<ServiceMaster> serviceMasterMstsList = new ArrayList<ServiceMaster>();

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		String authorDate = "";
		String makerDate = "";

		try
		{
			connection = getConnection();
			if(source.equalsIgnoreCase("A"))
			{
				//SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T' FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' AND MAKER_CD <> ? UNION ALL SELECT SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'M' FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' AND MAKER_CD <> ?  FROM COM_SERVICE_MST WHERE ID NOT IN (SELECT ID FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R')
				//pStatement = connection.prepareStatement("SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T', DECODE(SERVICE_TYPE, 'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download','') FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' AND MAKER_CD <> ? UNION ALL SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'M', DECODE(SERVICE_TYPE,  'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download', '') FROM COM_SERVICE_MST WHERE ID NOT IN (SELECT ID FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R')");
				pStatement = connection.prepareStatement("SELECT A.SERVICE_CD, A.SERVICE_DESC, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',G.GROUP_NAME,G.GROUP_ID FROM COM_SERVICE_MST_TMP A,COM_SERVICEGROUP G WHERE A.SERVICE_TYPE=G.GROUP_ID AND A.AUTH_STATUS != 'R' AND A.MAKER_CD <> ? UNION ALL SELECT AA.SERVICE_CD, AA.SERVICE_DESC, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',GG.GROUP_NAME,GG.GROUP_ID FROM COM_SERVICE_MST AA,COM_SERVICEGROUP GG WHERE AA.SERVICE_TYPE=GG.GROUP_ID AND AA.ID NOT IN (SELECT AAA.ID FROM COM_SERVICE_MST_TMP AAA WHERE AAA.AUTH_STATUS != 'R')");
				pStatement.setString(1, userID);
			}
			else
			{
				//SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T' FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' UNION ALL SELECT SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'M' FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R'  FROM COM_SERVICE_MST WHERE ID NOT IN (SELECT ID FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R')
				//pStatement = connection.prepareStatement("SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T', DECODE(SERVICE_TYPE, 'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download','') FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' UNION ALL SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'M', DECODE(SERVICE_TYPE, 'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download', '') FROM COM_SERVICE_MST WHERE ID NOT IN (SELECT ID FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R')");
				pStatement = connection.prepareStatement("SELECT A.SERVICE_CD, A.SERVICE_DESC, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',G.GROUP_NAME,G.GROUP_ID FROM COM_SERVICE_MST_TMP A,COM_SERVICEGROUP G WHERE A.SERVICE_TYPE=G.GROUP_ID AND A.AUTH_STATUS != 'R' UNION ALL SELECT AA.SERVICE_CD, AA.SERVICE_DESC, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',GG.GROUP_NAME,GG.GROUP_ID FROM COM_SERVICE_MST AA,COM_SERVICEGROUP GG WHERE AA.SERVICE_TYPE=GG.GROUP_ID AND AA.ID NOT IN (SELECT AAA.ID FROM COM_SERVICE_MST_TMP AAA WHERE AAA.AUTH_STATUS != 'R')");
			}

			resultSet = pStatement.executeQuery();
			while (resultSet.next())
			{
				ServiceMaster serviceMaster = new ServiceMaster();

				serviceMaster.setServiceCD(resultSet.getString(1));
				serviceMaster.setServiceDescription(resultSet.getString(2));
				serviceMaster.setId(resultSet.getInt(3));
				serviceMaster.setStatus(resultSet.getString(4));
				serviceMaster.setMaker(resultSet.getString(5));
				serviceMaster.setMakerDate(Utils.formatDate(resultSet.getDate(6)) + " [" + Utils.formatDate(resultSet.getDate(7)) +  " " + Utils.formatDate(resultSet.getTime(7), "HH:mm") + "]");
				serviceMaster.setAuthor(resultSet.getString(8));
				serviceMaster.setAuthorDate(Utils.formatDate(resultSet.getDate(9)) + " [" + Utils.formatDate(resultSet.getDate(10)) +  " " + Utils.formatDate(resultSet.getTime(10), "HH:mm") + "]");
				serviceMaster.setActiveFlag(resultSet.getString(11));
				serviceMaster.setLastChangeId(resultSet.getInt(12));
				serviceMaster.setTableFlg(resultSet.getString(13));
				serviceMaster.setServiceType(resultSet.getString(14));
				serviceMaster.setServiceTypeKey(resultSet.getString(15));

				serviceMasterMstsList.add(serviceMaster);
			}
		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if(resultSet!=null){
					resultSet.close();
				}
				if(pStatement!=null){
					pStatement.close();
				}
				if(connection!=null){
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		return serviceMasterMstsList;
	}


	@Override
	public List<ServiceMaster> search(ServiceMaster serviceMaster, String source)	{

		log.info("serviceMaster"+serviceMaster.getServiceType()+":"+serviceMaster.getServiceDescription()+":"+serviceMaster.getStatus()+":"+serviceMaster.getServiceCD());
		List<ServiceMaster> matchRecords = new ArrayList<ServiceMaster>();

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		int varLocation = 0;
		int serviceCDPos = 0;
		int serviceTYPEPos = 0;
		int serviceDescriptionPos = 0;
		int statusPos = 0;

		int makerPos = 0;

		int serviceCDPos1 = 0;
		int serviceTYPEPos1 = 0;
		int serviceDescriptionPos1 = 0;
		int statusPos1 = 0;

		String sql = "";
		String authorDate = "";
		String makerDate = "";

		//Search From Temp Table
		//sql += " SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, DECODE(SERVICE_TYPE, 'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download', ''), 'T' FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R' ";
		sql += "SELECT A.SERVICE_CD, A.SERVICE_DESC, A.ID, DECODE(A.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), A.MAKER_CD, A.MAKER_DT, A.MAKER_CAL_DT, A.AUTHOR_CD, A.AUTHOR_DT, A.AUTHOR_CAL_DT, A.ACTIVE_FLAG, A.LAST_CHANGE_ID, 'T',G.GROUP_NAME,G.GROUP_ID FROM COM_SERVICE_MST_TMP A,COM_SERVICEGROUP G WHERE A.SERVICE_TYPE=G.GROUP_ID AND A.AUTH_STATUS != 'R' ";
		if (!"".equalsIgnoreCase(serviceMaster.getServiceType())) {
			varLocation++;
			serviceTYPEPos = varLocation;
			sql = sql + " AND UPPER(A.SERVICE_TYPE) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getServiceCD())) {
			varLocation++;
			serviceCDPos = varLocation;
			sql = sql + " AND UPPER(A.SERVICE_CD) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getServiceDescription())) {
			varLocation++;
			serviceDescriptionPos = varLocation;
			sql = sql + " AND UPPER(A.SERVICE_DESC) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getStatus())) {
			varLocation++;
			statusPos = varLocation;
			sql = sql + " AND UPPER(A.AUTH_STATUS) LIKE ? ";
		}

		if(source.equalsIgnoreCase("A"))
		{
			varLocation++;
			makerPos = varLocation;
			sql = sql + " AND A.MAKER_CD <> ? ";
		}

		sql = sql.trim();
		if (sql.endsWith("AND")) {
			sql = sql.substring(0, sql.lastIndexOf("AND"));
		}

		//Search From Main Table


		//sql = sql + " UNION ALL SELECT  SERVICE_CD,  SERVICE_DESC,  ID,  DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''),  MAKER_CD,  MAKER_DT,  MAKER_CAL_DT,  AUTHOR_CD,  AUTHOR_DT,  AUTHOR_CAL_DT,  ACTIVE_FLAG,  LAST_CHANGE_ID,  DECODE(SERVICE_TYPE, 'B', 'Financial Transaction-Bulk', 'N', 'Non-Financial Transaction', 'O', 'Financial Transaction-Onscreen', 'S', 'Salary Management', 'D', 'Document Download', ''), 'M' FROM COM_SERVICE_MST WHERE ID NOT IN (SELECT ID FROM COM_SERVICE_MST_TMP WHERE AUTH_STATUS != 'R') ";
		sql = sql + " UNION ALL SELECT AA.SERVICE_CD, AA.SERVICE_DESC, AA.ID, DECODE(AA.AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.MAKER_CD, AA.MAKER_DT, AA.MAKER_CAL_DT, AA.AUTHOR_CD, AA.AUTHOR_DT, AA.AUTHOR_CAL_DT, AA.ACTIVE_FLAG, AA.LAST_CHANGE_ID, 'M',GG.GROUP_NAME,GG.GROUP_ID FROM COM_SERVICE_MST AA,COM_SERVICEGROUP GG WHERE AA.SERVICE_TYPE=GG.GROUP_ID AND AA.ID NOT IN (SELECT AAA.ID FROM COM_SERVICE_MST_TMP AAA WHERE AAA.AUTH_STATUS != 'R') ";

		if (!"".equalsIgnoreCase(serviceMaster.getServiceType())) {
			varLocation++;
			serviceTYPEPos1 = varLocation;
			sql = sql + " AND UPPER(AA.SERVICE_TYPE) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getServiceCD())) {
			varLocation++;
			serviceCDPos1 = varLocation;
			sql = sql + " AND UPPER(AA.SERVICE_CD) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getServiceDescription())) {
			varLocation++;
			serviceDescriptionPos1 = varLocation;
			sql = sql + " AND UPPER(AA.SERVICE_DESC) LIKE ? ";
		}

		if (!"".equalsIgnoreCase(serviceMaster.getStatus())) {
			varLocation++;
			statusPos1 = varLocation;
			sql = sql + " AND UPPER(AA.AUTH_STATUS) LIKE ? ";
		}

		sql = sql.trim();

		if (sql.endsWith("WHERE"))
		{
			sql = sql.substring(0, sql.lastIndexOf("WHERE"));
		}

		if (sql.endsWith("AND")) {
			sql = sql.substring(0, sql.lastIndexOf("AND"));
		}

		log.info("ServiceMaster Search Query : " + sql);

		try {
			connection = getConnection();
			pStatement = connection.prepareStatement(sql);

			if(serviceTYPEPos > 0)
			{
				String serType = Utils.removeWildCardChar(serviceMaster.getServiceType());
				pStatement.setString(serviceTYPEPos,"%" + serType.toUpperCase() + "%");
				pStatement.setString(serviceTYPEPos1,"%" + serType.toUpperCase() + "%");
			}

			if(serviceCDPos > 0)
			{
				String serviceCD = Utils.removeWildCardChar(serviceMaster.getServiceCD());
				pStatement.setString(serviceCDPos,"%" + serviceCD.toUpperCase() + "%");
				pStatement.setString(serviceCDPos1,"%" + serviceCD.toUpperCase() + "%");
			}

			if(serviceDescriptionPos > 0)
			{
				String serviceDes = Utils.removeWildCardChar(serviceMaster.getServiceDescription());
				pStatement.setString(serviceDescriptionPos,"%" + serviceDes.toUpperCase() + "%");
				pStatement.setString(serviceDescriptionPos1,"%" + serviceDes.toUpperCase() + "%");
			}

			if(statusPos > 0)
			{
				String status = Utils.removeWildCardChar(serviceMaster.getStatus());
				pStatement.setString(statusPos,"%" + status.toUpperCase() + "%");
				pStatement.setString(statusPos1,"%" + status.toUpperCase() + "%");
			}

			if(makerPos > 0 )
			{
				pStatement.setString(makerPos, userID);
			}

			resultSet = pStatement.executeQuery();
			while (resultSet.next())
			{
				ServiceMaster serviceMaster1 = new ServiceMaster();

				serviceMaster1.setServiceCD(resultSet.getString(1));
				serviceMaster1.setServiceDescription(resultSet.getString(2));
				serviceMaster1.setId(resultSet.getInt(3));
				serviceMaster1.setStatus(resultSet.getString(4));
				serviceMaster1.setMaker(resultSet.getString(5));
				serviceMaster1.setMakerDate(Utils.formatDate(resultSet.getDate(6)) + " [" + Utils.formatDate(resultSet.getDate(7)) +  " " + Utils.formatDate(resultSet.getTime(7), "HH:mm") + "]");
				serviceMaster1.setAuthor(resultSet.getString(8));
				serviceMaster1.setAuthorDate(Utils.formatDate(resultSet.getDate(9)) + " [" + Utils.formatDate(resultSet.getDate(10)) +  " " + Utils.formatDate(resultSet.getTime(10), "HH:mm") + "]");
				serviceMaster1.setActiveFlag(resultSet.getString(11));
				serviceMaster1.setLastChangeId(resultSet.getInt(12));
				serviceMaster1.setTableFlg(resultSet.getString(13));
				serviceMaster1.setServiceType(resultSet.getString(14));
				serviceMaster1.setServiceTypeKey(resultSet.getString(15));
				matchRecords.add(serviceMaster1);
			}
		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if(resultSet!=null){
					resultSet.close();
				}
				if(pStatement!=null){
					pStatement.close();
				}
				if(connection!=null){
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		return matchRecords;
	}


	@Override
	public Map<String, String> save(ServiceMaster serviceMaster, String mode)	{

		Map<String, String> message = new LinkedHashMap<String, String>();
		log.info("Inside save ServiceMasterDAOImpl");

		Connection connection = null;
		ResultSet resultSet = null;
		PreparedStatement pStatement = null;
		String auth_cd="";
		String auth_dt="";

		int count = 0;
		try {
			connection = getConnection();
			if ("new".equalsIgnoreCase(mode)) {
				//SELECT COUNT(1) FROM COM_SERVICE_MST WHERE UPPER(SERVICE_CD) = ?
				pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_SELECT_COUNT"));
				pStatement.setString(1, serviceMaster.getServiceCD().trim().toUpperCase());
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					count = resultSet.getInt(1);
				}
				resultSet.close();
				pStatement.close();
				if(count != 0)
				{
					message.put("error", path.getMessage("E1029"));
					return message;
				}

				//SELECT COUNT(1) FROM COM_SERVICE_MST_TMP WHERE UPPER(SERVICE_CD) = ?
				pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_SELECT_COUNT"));
				pStatement.setString(1, serviceMaster.getServiceCD().trim().toUpperCase());
				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					count = resultSet.getInt(1);
				}
				resultSet.close();
				pStatement.close();
				if(count != 0)
				{
					message.put("error", path.getMessage("E1029"));
					return message;
				}

				long runningSeqNo = 0;

				//SELECT COM_SERVICE_MST_SEQ.NEXTVAL FROM DUAL
				pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_SEQ_NEXTVAL"));

				resultSet = pStatement.executeQuery();
				while (resultSet.next()) {
					runningSeqNo = resultSet.getLong(1);
				}
				resultSet.close();
				pStatement.close();

				// write code to get running seq no

				//COM_SERVICE_MST_TMP_INSERT=INSERT INTO COM_SERVICE_MST_TMP(SERVICE_CD, SERVICE_DESC, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,SERVICE_TYPE) VALUES(?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?)
				pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_INSERT"));
				pStatement.setString(1, serviceMaster.getServiceCD().trim());
				pStatement.setString(2, serviceMaster.getServiceDescription().trim());
				pStatement.setLong(3, runningSeqNo);
				pStatement.setString(4, "N");
				pStatement.setString(5, userID);
				pStatement.setString(6, null);
				pStatement.setDate(7, null);
				pStatement.setDate(8, null);
				pStatement.setString(9, serviceMaster.getActiveFlag().trim());
				pStatement.setInt(10, 0);
				pStatement.setString(11, bankID);
				pStatement.setString(12, serviceMaster.getServiceType());
				pStatement.executeUpdate();
				pStatement.close();
			}

			if ("update".equalsIgnoreCase(mode)) {

				// Check dependency of record
				//CHECK_DEPENDANCY_QUERY
				/*pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_CHECK_DEPENDANCY_QUERY"));

				pStatement.setString(1, serviceCD);

				boolean existsRecord = false;
				resultSet = pStatement.executeQuery();
				if(resultSet !=null && resultSet.next()) {
					if(resultSet.getInt(1) != 0)
						existsRecord = true;
				}
				if(existsRecord && "N".equalsIgnoreCase(serviceMaster.getActiveFlag())) {
					message.put("error", "You Can't Modify Active Status Of This Record, This COM_SERVICE_MST ID Has Been Used In Another Master");
					return message;
				}

				resultSet.close();
				pStatement.close();*/

			/*pStatement = connection.prepareStatement("SELECT AUTHOR_CD,AUTHOR_DT FROM COM_SERVICE_MST WHERE ID =?");
				pStatement.setLong(1, serviceMaster.getId());
				log.info("serviceMaster.getId() "+serviceMaster.getId());
				resultSet = pStatement.executeQuery();
					if(resultSet.next())
					{
						 auth_cd =resultSet.getString(1);
						 auth_dt =resultSet.getString(2);
						log.info("auth_cd ===="+auth_cd);

					}
					resultSet.close();
					pStatement.close();*/


				if("M".equalsIgnoreCase(serviceMaster.getTableFlg()))
				{
					log.info("update 1");
					//COM_SERVICE_MST_TMP_INSERT_AUTHOR=INSERT INTO COM_SERVICE_MST_TMP(SERVICE_CD, SERVICE_DESC, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,SERVICE_TYPE) VALUES(?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?,?,?)
					pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_INSERT_AUTHOR"));

					pStatement.setString(1, serviceMaster.getServiceCD().trim());
					pStatement.setString(2, serviceMaster.getServiceDescription().trim());
					pStatement.setInt(3, serviceMaster.getId());
					pStatement.setString(4, "M");
					pStatement.setString(5, userID);
					pStatement.setString(6, null);
					pStatement.setString(7, null);
					pStatement.setDate(8, null);
					pStatement.setString(9, serviceMaster.getActiveFlag().trim());
					pStatement.setInt(10, serviceMaster.getLastChangeId());
					log.info("serviceMaster.getLastChangeId() "+serviceMaster.getLastChangeId());
					pStatement.setString(11, bankID);
					pStatement.setString(12, serviceMaster.getServiceType());
					pStatement.executeUpdate();
					pStatement.close();

				}
				else
				{
					log.info("update 2");
					//COM_SERVICE_MST_TMP_UPDATE=UPDATE COM_SERVICE_MST_TMP SET SERVICE_DESC = ?, MAKER_CD = ?, MAKER_DT = sysdate, MAKER_CAL_DT = sysdate, ACTIVE_FLAG = ? , SERVICE_TYPE=? WHERE ID = ? AND BANK_ID = ?
					pStatement = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_UPDATE"));
					pStatement.setString(1, serviceMaster.getServiceDescription().trim());
					pStatement.setString(2, userID);
					pStatement.setString(3, serviceMaster.getActiveFlag().trim());
					pStatement.setString(4, serviceMaster.getServiceType().trim());
					pStatement.setInt(5, serviceMaster.getId());
					pStatement.setString(6, bankID);
					pStatement.executeUpdate();
					pStatement.close();
				}
			}
		} catch (SQLException ex) {
			log.info(ex);
			ex.printStackTrace();
			if (ex.getErrorCode() == 1) {
					message.put("error", path.getMessage("E1030"));
			} else {
				message.put("error", path.getMessage("E1010"));
			}
			return message;
		} catch (Exception e) {
			log.info(e);
			message.put("error", "Save Record Failed : " + e.toString());
			return message;
		} finally {
			try {
				if(resultSet!=null){
					resultSet.close();
				}
				if(pStatement!=null){
					pStatement.close();
				}
				if(connection!=null){
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
	public Map<String, String> author(String[] id, String mode)	{

		/*Map<String, String> message = new LinkedHashMap<String, String>();

		log.info("userID "+userID);
		 Update audit-trail
		for (String masterID : id)
		{
			AuditTrail.checkAuditTrail(Long.parseLong(masterID), "COM_SERVICE_MST", mode,userID);
		}*/

		Connection connection = null;

		int approvalID;
		PreparedStatement preStatement = null;
		ResultSet resultSet1 = null;

		Map<String, String> message = new HashMap<String, String>();

		try {
			connection = getConnection();
			preStatement = connection.prepareStatement("select MAKER_CD from COM_SERVICE_MST_TMP where ID=?");
			for (String authRowID : id) {
				try {
					approvalID = Integer.parseInt(authRowID);
					preStatement.setInt(1, approvalID);
					// log.info("In author() Of cityID ::"+cityID);
					resultSet1 = preStatement.executeQuery();
					if (resultSet1.next()) {
						if (resultSet1.getString(1).trim()
								.equalsIgnoreCase(userID)) {
							message.put("error",path.getMessage("E1011"));
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

				AuditTrail.checkAuditTrail(Long.parseLong(authRowID),"COM_SERVICE_MST", mode, userID);
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
			op = "Authorize ";
		} else if ("R".equals(mode)) {
			op = "Reject ";
		}

		//Connection connection = null;
		PreparedStatement pStatement1 = null;
		PreparedStatement pStatement2 = null;
		PreparedStatement pStatement3 = null;
		PreparedStatement pStatement4 = null;
		PreparedStatement pStatement5 = null;
		ResultSet resultSet = null;

		try {
			connection = getConnection();
			if("R".equalsIgnoreCase(mode))
			{
				//SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T' FROM COM_SERVICE_MST_TMP WHERE ID = ?
				pStatement1 = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_AUTHOR_SELECT"));
				//UPDATE COM_SERVICE_MST_TMP SET AUTH_STATUS = ? WHERE ID = ?
				pStatement5 = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_REJECT"));
				//DELETE FROM COM_SERVICE_MST_TMP WHERE ID = ?
				pStatement3 = connection.prepareStatement(getQuery("COM_SERVICE_MST_DELETE"));

				for (String authorID : id) {
					masterID = Integer.parseInt(authorID);

					pStatement1.setInt(1,masterID);
					pStatement1.setString(2, bankID);
					resultSet = pStatement1.executeQuery();
					if(resultSet.next())
					{
						pStatement5.setInt(1, resultSet.getInt(12)+1);         //LAST_CHANGE_ID
						pStatement5.setInt(2, masterID);
						pStatement5.setString(3, bankID);
						pStatement5.executeUpdate();
					}

					pStatement3.setInt(1, masterID);
					pStatement3.setString(2, bankID);
					pStatement3.executeUpdate();
				}
			}
			else if("A".equalsIgnoreCase(mode))
			{
				//SELECT SERVICE_CD, SERVICE_DESC, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', '') AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T' FROM COM_SERVICE_MST_TMP WHERE ID = ?
				pStatement1 = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_AUTHOR_SELECT"));

				for (String authorID : id) {
					masterID = Integer.parseInt(authorID);
					pStatement1.setInt(1, masterID);
					pStatement1.setString(2, bankID);
					resultSet = pStatement1.executeQuery();

					{
						if(resultSet.next())
						if("Modified".equals(resultSet.getString(4)))
						{
							//COM_SERVICE_MST_UPDATE=UPDATE COM_SERVICE_MST SET SERVICE_DESC = ?, MAKER_CD = ?, MAKER_DT = ?, MAKER_CAL_DT = ?, AUTHOR_CD = ?, AUTHOR_DT = sysdate, AUTHOR_CAL_DT = sysdate, ACTIVE_FLAG = ?, LAST_CHANGE_ID = ?, SERVICE_TYPE = ? WHERE ID = ? AND BANK_ID = ?
							pStatement2 = connection.prepareStatement(getQuery("COM_SERVICE_MST_UPDATE"));
							pStatement2.setString(1, resultSet.getString(2));
							pStatement2.setString(2, resultSet.getString(5));
							pStatement2.setDate(3, resultSet.getDate(6));
							pStatement2.setTimestamp(4, resultSet.getTimestamp(7));
							pStatement2.setString(5, userID);
							pStatement2.setString(6, resultSet.getString(11));
							int lastchangeID = resultSet.getInt(12);
							log.info("lastchangeID"+lastchangeID);
							int ADDlastchangeID = lastchangeID + 1;
							pStatement2.setInt(7, ADDlastchangeID);
							log.info("ADDlastchangeID "+ADDlastchangeID);
							pStatement2.setString(8, resultSet.getString(15));
							log.info("resultSet.getString(15)="+resultSet.getString(15));
							pStatement2.setInt(9, resultSet.getInt(3));
							pStatement2.setString(10, bankID);
							pStatement2.executeUpdate();
						}
						else if("New".equals(resultSet.getString(4)))
						{
							//COM_SERVICE_MST_INSERT_NEW=INSERT INTO COM_SERVICE_MST(SERVICE_CD, SERVICE_DESC, ID, AUTH_STATUS, MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID,BANK_ID,SERVICE_TYPE) VALUES(?, ?, ?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?,?,?)
							pStatement3 = connection.prepareStatement(getQuery("COM_SERVICE_MST_INSERT_NEW"));
							pStatement3.setString(1, resultSet.getString(1));
							pStatement3.setString(2, resultSet.getString(2));
							pStatement3.setLong(3, resultSet.getInt(3));
							pStatement3.setString(4, mode);
							pStatement3.setString(5, resultSet.getString(5));
							pStatement3.setDate(6, resultSet.getDate(6));
							pStatement3.setTimestamp(7, resultSet.getTimestamp(7));
							pStatement3.setString(8, userID);
							pStatement3.setString(9, resultSet.getString(11));
							int lastchangeID = resultSet.getInt(12);
							log.info("lastchangeID "+lastchangeID);
							int ADDlastchangeID = lastchangeID + 1;
							pStatement3.setInt(10, ADDlastchangeID);
							log.info("ADDlastchangeID "+ADDlastchangeID);
							pStatement3.setString(11, bankID);
							pStatement3.setString(12, resultSet.getString(15));
							pStatement3.executeUpdate();
						}
						else
						{
						}
						//DELETE FROM COM_SERVICE_MST_TMP WHERE ID = ?
						pStatement4 = connection.prepareStatement(getQuery("COM_SERVICE_MST_TMP_DELETE"));
						pStatement4.setInt(1, Integer.parseInt(authorID));
						pStatement4.setString(2, bankID);
						pStatement4.executeUpdate();
					}
				}
			}
		} catch (SQLException ex) {
			log.info(ex);
			if(ex.getMessage().toLowerCase().contains("unique constraint")){
				message.put("error", op + path.getMessage("E1055"));
			}else{
				message.put("error", op + path.getMessage("E1016"));
			}
			return message;
		} catch (Exception ex) {
			log.info(ex);
			message.put("error", op + " Record Failed : " + ex.toString());
			return message;
		} finally {
			try {
				if(resultSet!=null){
					resultSet.close();
				}
				if(pStatement1!=null){
					pStatement1.close();
				}
				if(pStatement2!=null){
					pStatement2.close();
				}
				if(pStatement3!=null){
					pStatement3.close();
				}
				if(pStatement4!=null){
					pStatement4.close();
				}
				if(connection!=null){
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		if ("A".equals(mode)) {
			message.put("success", id.length +" "+ path.getMessage("E1012"));
		} else if ("R".equals(mode)) {
			message.put("success", id.length +" "+ path.getMessage("E1013"));
		}

		return message;
	}




	@SuppressWarnings("unchecked")
	public Map<String, String> sortMap(Map map) {
		Map<String, String> treeMap = new TreeMap<String, String>(map);
		Iterator iterator = treeMap.keySet().iterator();
		while (iterator.hasNext())
		{
			Object key = iterator.next();
		}

		return treeMap;
	}


	/**
	 * @param businessDate the businessDate to set
	 */
	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}

	/**
	 * @param businessDateSQL the businessDateSQL to set
	 */
	public void setBusinessDateSQL(java.sql.Date businessDateSQL) {
		this.businessDateSQL = businessDateSQL;
	}

	/**
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(Map session) {
		this.session = session;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(String userID) {
		this.userID = userID;
	}

	/**
	 * @param userName the userName to set
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
	public Map<String, String> getgroupCDMapMap() {

		Map<String, String> serviceGroupMstsList = new HashMap<String,String>();

		Connection connection = null;
		PreparedStatement pStatement = null;
		ResultSet resultSet = null;

		String authorDate = "";
		String makerDate = "";

		try
		{
			connection = getConnection();
				//SELECT GROUP_NAME, GROUP_ID, LIMIT_REQUIRED, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'T' FROM COM_SERVICEGROUP_TMP WHERE AUTH_STATUS != 'R' UNION ALL SELECT SELECT GROUP_NAME, GROUP_ID, LIMIT_REQUIRED, ID, DECODE(AUTH_STATUS, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, AUTHOR_CAL_DT, ACTIVE_FLAG, LAST_CHANGE_ID, 'M' FROM COM_SERVICEGROUP_TMP WHERE AUTH_STATUS != 'R'  FROM COM_SERVICEGROUP WHERE ID NOT IN (SELECT ID FROM COM_SERVICEGROUP_TMP WHERE AUTH_STATUS != 'R')
			pStatement = connection.prepareStatement("SELECT GROUP_ID,GROUP_NAME FROM COM_SERVICEGROUP WHERE ACTIVE_FLAG='Y'");


			resultSet = pStatement.executeQuery();
			while (resultSet.next())
			{
				serviceGroupMstsList.put(resultSet.getString(1),resultSet.getString(2));
			}
		} catch (SQLException ex) {
			log.info(ex);
		} catch (Exception ex) {
			log.info(ex);
		} finally {
			try {
				if(resultSet!=null){
					resultSet.close();
				}
				if(pStatement!=null){
					pStatement.close();
				}
				if(connection!=null){
					connection.close();
				}
			} catch (SQLException ex) {
				log.info(ex);
			}
		}
		return serviceGroupMstsList;
	}


}