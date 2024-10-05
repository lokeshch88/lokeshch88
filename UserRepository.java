package com.example.demo.repo;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.ApprovalGroup;
import com.example.demo.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

@Repository
public class UserRepository {

	@Autowired
	EntityManager entityManager;

	// Optional<User> findByUsernameAndPassword(String username, String password);

	public List<UserDto> getAllUsersDetails() {
		String query = "SELECT A.serviceCdId, A.accountNo, B.groupName, A.id, DECODE(A.authStatus, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''),\n"
				+ "A.makerCd, A.makerDt, A.makerCalDt, A.authorCd, A.authorDt, "
				+ "A.authorCalDt, A.activeFlag, A.lastChangeId, 'T',A.operationMode, B.groupId, A.dailyLimit \n"
				+ "FROM ApprovalGroupTmp A, ServiceGroup B \n"
				+ "WHERE A.authStatus != 'R' AND A.makerCd <> 'User02' AND A.serviceCd=B.groupId \n" + "UNION ALL\n"
				+ "SELECT AA.serviceCdId, AA.accountNo, BB.groupName, AA.id, \n"
				+ "DECODE(AA.authStatus, 'A', 'Authorized', 'M', 'Modified', 'N', 'New', 'R', 'Rejected', ''), AA.makerCd, AA.makerDt, AA.makerCalDt,\n"
				+ "AA.authorCd, AA.authorDt, AA.authorCalDt, AA.activeFlag, AA.lastChangeId, 'M',AA.operationMode, BB.groupId, AA.dailyLimit \n"
				+ "FROM ApprovalGroup AA, ServiceGroup BB \n"
				+ "WHERE AA.authStatus != 'R' AND AA.serviceCd=BB.groupId AND AA.id NOT IN (SELECT C.id FROM ApprovalGroupTmp C WHERE C.authStatus != 'R')";

		// Query list = entityManager.createQuery(query, UserDto.class);
		// return list;
		List<Object[]> results = entityManager.createQuery(query).getResultList();
		return results.stream()
				.map(result -> new UserDto((String) result[0], (String) result[1], (String) result[2], (Long) result[3],
						(String) result[4], (String) result[5], (Date) result[6], (Date) result[7], (String) result[8],
						(Date) result[9], (Date) result[10], (String) result[11], (Long) result[12],
						(String) result[13], (String) result[14], (String) result[15]))
				.toList();

	}

	public void getUserDetails(String custId, String status) {
		// getaccount numbers
		String sql1;
		// for approvr list
		sql1 = "SELECT APPROVER_LIST FROM COM_APPROVAL_GRP_MST WHERE  SERVICECDID = ? AND ACCOUNT_NO =? AND SERVICE_CD = ?";
		if ("Authorized".equals(status)) {
			// Go to main master table
			sql1 = "SELECT accountNo,serviceCd,approverList,dailyLimit,operationMode,id,activeFlag FROM ApprovalGroup WHERE serviceCdId = ?";// AND
																																				// =
																																				// 'Y'";
		} else {
			// Go to temp table
			sql1 = "SELECT accountNo,serviceCd,approverList,dailyLimit,operationMode,id,activeFlag FROM ApprovalGroupTmp WHERE serviceCdId = ?";
		}

		entityManager.createQuery(sql1).setParameter(1, custId).getResultList();

	}

	// SAVE USER---------------------------

	@Transactional
	public String saveUser(UserDto entity) {
		Long count;

		// group map
		String groupQuery = "SELECT g.groupId, g.groupName FROM ServiceGroup g WHERE g.authStatus = 'A' AND g.activeFlag = 'Y'";
		Map<String, String> groupResultsMap = new HashMap<>();

		List<Object[]> results = entityManager.createQuery(groupQuery).getResultList();

		for (Object[] row : results) {
			String groupId = (String) row[0];
			String groupName = (String) row[1];

			groupResultsMap.put(groupId, groupName);
		}

		// approvr map
//		String approverQueryStr = "SELECT a.approverCd, a.approverId FROM ComApproveMst a WHERE a.customerId = :customerId AND a.activeFlag = 'Y'";
//		Query approverQuery = entityManager.createQuery(approverQueryStr);
//		approverQuery.setParameter("customerId", entity.getServiceCdId());
//
//		List<Object[]> approverResults = approverQuery.getResultList();
//
//		for (Object[] row : approverResults) {
//			String approverCd = (String) row[0];
//			Integer approverId = (Integer) row[1];
//
//		}

		try {
			String sql = "SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST " +
		              "WHERE UPPER(TRIM(SERVICECDID)) = UPPER(TRIM(:serviceCdId)) " +
		              "AND UPPER(TRIM(ACCOUNT_NO)) = UPPER(TRIM(:accountNo)) " +
		              "AND UPPER(TRIM(SERVICE_CD)) = UPPER(TRIM(:serviceCd))";

			Query query = entityManager.createNativeQuery(sql);
			query.setParameter("serviceCdId", entity.getServiceCdId());
			query.setParameter("accountNo", entity.getAccountNo());
			query.setParameter("serviceCd", entity.getServiceCd());
			
			BigDecimal countBigDecimal = (BigDecimal) query.getSingleResult();
			 count = countBigDecimal.longValue();

			//count = (Long) query.getSingleResult();
			if (count != 0) {
				return "Company id already register for " + groupResultsMap.get(entity.getServiceCd());
			}
			System.out.println("count 1 query execute");

			
			String sql1="SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST_TMP "
					+"WHERE UPPER(SERVICECDID) = UPPER(:serviceCdId) AND "
					+ "UPPER(ACCOUNT_NO) = UPPER(:accountNo) AND UPPER(SERVICE_CD) = UPPER(:serviceType)";
//			String sql1 = "SELECT COUNT(1) FROM COM_APPROVALMAPPING_MST "
//					+ "WHERE UPPER(SERVICECDID) = UPPER(:serviceCdId) " + "AND UPPER(ACCOUNT_NO) = UPPER(:serviceCdId) "
//					+ "AND UPPER(SERVICE_CD) IN (SELECT UPPER(SERVICE_CD) FROM COM_SERVICE_MST "
//					+ "WHERE UPPER(SERVICE_TYPE) = UPPER(:serviceType))";

			Query query1 = entityManager.createNativeQuery(sql1);
			query1.setParameter("serviceCdId", entity.getServiceCdId());
			query1.setParameter("accountNo", entity.getAccountNo());
			query1.setParameter("serviceType", entity.getServiceCd());
			
			BigDecimal countBigDecimal1 = (BigDecimal) query1.getSingleResult();
			 count = countBigDecimal1.longValue();
			if (count != 0) {
				return "Company id already register for " + groupResultsMap.get(entity.getServiceCd());
			}
			System.out.println("count 2 query execute");
			
			
			
			String sql2 = "SELECT COUNT(1) FROM COM_APPROVAL_GRP_MST_TMP "
					+ "WHERE UPPER(SERVICECDID) = UPPER(:serviceCdId) " + "AND UPPER(ACCOUNT_NO) = UPPER(:accountNo) "
					+ "AND UPPER(SERVICE_CD) IN (SELECT UPPER(SERVICE_CD) FROM COM_SERVICE_MST WHERE UPPER(SERVICE_TYPE) = UPPER(:serviceType))";

			Query query2 = entityManager.createNativeQuery(sql2);
			query2.setParameter("serviceCdId", entity.getServiceCdId());
			query2.setParameter("accountNo", entity.getAccountNo());
			query2.setParameter("serviceType", entity.getServiceCd());
			
			BigDecimal countBigDecimal2 = (BigDecimal) query2.getSingleResult();
			 count = countBigDecimal2.longValue();
			if (count != 0) {
				return "Company id already register for " + groupResultsMap.get(entity.getServiceCd());
			}
			System.out.println("count 3 query execute");
			
			
			// id from seqn
			String idQuery = "SELECT COM_APPROVALMAPPING_GRP_SEQ.NEXTVAL FROM DUAL";
			Query query3 = entityManager.createNativeQuery(idQuery);
			Long runningSeqNo = ((Number) query3.getSingleResult()).longValue();
//			 BigDecimal countBigDecimal3 = (BigDecimal) query3.getSingleResult();
//			 Long runningSeqNo = countBigDecimal.longValue();
			//Long runningSeqNo = (Long) query3.getSingleResult();
			
			System.out.println("seq id query execute");
			
			
			//insert q
			String insertQuery = "INSERT INTO COM_APPROVAL_GRP_MST_TMP "
					+ "(SERVICECDID, ACCOUNT_NO, SERVICE_CD, ID, AUTH_STATUS, "
					+ "MAKER_CD, MAKER_DT, MAKER_CAL_DT, AUTHOR_CD, AUTHOR_DT, " + "AUTHOR_CAL_DT, "
					+ "ACTIVE_FLAG, LAST_CHANGE_ID, APPROVER_LIST, TOTAL_SCORE, " + "OPERATION_MODE, "
					+ "BANK_ID, DAILY_LIMIT) "
					+ "VALUES (?, ?, ?, ?, ?, ?, sysdate, sysdate, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			Query insert = entityManager.createNativeQuery(insertQuery);
			
			insert.setParameter(1, entity.getServiceCdId().trim()) // servicecd
					.setParameter(2, entity.getAccountNo());// ACC NO
			insert.setParameter(3, entity.getServiceCd().trim().toUpperCase());// SERV CD
			insert.setParameter(4, runningSeqNo);// ID
			insert.setParameter(5, "N"); // AUTH_STATUS
			insert.setParameter(6, entity.getMakerCd()); // maker CD
			insert.setParameter(7, null); // AUTHOR CD
			insert.setParameter(8, null); // AUTHOR DT
			insert.setParameter(9, null); // AUTHER CAL DT
			insert.setParameter(10, entity.getActiveFlag().trim()); // ACTIVE FLAG
			insert.setParameter(11, 0); // LASTCHANGE ID
			insert.setParameter(12, ""); // APPROVER LIST
			int totalScore = 16;
			// int totalScore = calculateTotalScore(approvalMappingMaster, accountNo);

			insert.setParameter(13, totalScore);
			insert.setParameter(14,"0");
			String bankID="YESBANK";
			insert.setParameter(15, bankID); // bank ID
			insert.setParameter(16, entity.getDailyLimit()); // daily limit

			insert.executeUpdate();
			System.out.println("User data saved successfully");
		} catch (Exception e) {
			System.out.println(e);
			return "Error occured "+ e;
		}
		return "saved";

	}

	
	//get accounts
	public ResponseEntity<List<String>> getAccounts(String custId) {

		String sql = "SELECT DISTINCT COD_ACCT_NO AS ACCOUNT_NO " + "FROM VW_CH_ACCT_CUST_XREF "
				+ "WHERE COD_CUST = :custId AND " + "((cod_prod BETWEEN 501 AND 599) OR "
				+ "(cod_prod BETWEEN 901 AND 999) OR " + "(cod_prod BETWEEN 601 AND 899))";

		Query query = entityManager.createNativeQuery(sql);
		query.setParameter("custId", custId);

		List<String> results = query.getResultList();
		System.out.println("accounts fetched against company id");
		return ResponseEntity.ok(results);
	}

	//get approverr
	public ResponseEntity<Map<String, String>> getApproverList(String custId) {
		Map<String, String> approverDataMap = new HashMap<>();
        boolean l_presentFlag = false;

        String[] custList = custId.split(",");

      
        String sql = "SELECT c.COD_CUST_ID, c.NAM_CUST_FULL " +
                     "FROM MV_CI_CUSTMAST_ALL c " +
                     "WHERE c.COD_CUST_ID IN (" +
                     "SELECT a.APPROVER_CD FROM COM_APPROVE_MST a " +
                     "WHERE a.AUTH_STATUS = 'A' AND a.ACTIVE_FLAG = 'Y' " +
                     "AND a.CUSTOMER_ID IN (:custIds))";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("custIds", Arrays.asList(custList));
        
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
        	BigDecimal codCustId = (BigDecimal) result[0];
            String namCustFull = (String) result[1];

            if (codCustId!=null && !namCustFull.isEmpty()) {
            	approverDataMap.put(codCustId.toString().trim(), namCustFull.trim());
                l_presentFlag = true;
            }
        }

        // Repeat 
        if (!l_presentFlag) {
            for (String cust : custList) {
                String individualSql = "SELECT c.COD_CUST_ID, c.NAM_CUST_FULL " +
                                       "FROM MV_CI_CUSTMAST_ALL c " +
                                       "WHERE c.CUSTOMER_ID = :custId";
                Query individualQuery = entityManager.createNativeQuery(individualSql);
                individualQuery.setParameter("custId", cust);

                List<Object[]> individualResults = individualQuery.getResultList();
                for (Object[] individualResult : individualResults) {
                    String codCustId = (String) individualResult[0];
                    String namCustFull = (String) individualResult[1];

                    if (!codCustId.isEmpty() && !namCustFull.isEmpty()) {
                    	approverDataMap.put(codCustId.trim(), namCustFull.trim());
                    }
                }
            }
        }
        System.out.println("approver data fetched ");

        return ResponseEntity.ok( approverDataMap);
		
	}

}
