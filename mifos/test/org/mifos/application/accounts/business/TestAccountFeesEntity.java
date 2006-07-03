package org.mifos.application.accounts.business;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import org.mifos.application.accounts.TestAccount;
import org.mifos.application.accounts.business.AccountFeesEntity;
import org.mifos.application.accounts.financial.util.helpers.FinancialInitializer;
import org.mifos.application.accounts.persistence.AccountPersistence;
import org.mifos.application.accounts.util.helpers.AccountConstants;
import org.mifos.application.configuration.business.MifosConfiguration;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.fees.business.FeesBO;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.repaymentschedule.RepaymentScheduleException;
import org.mifos.framework.components.scheduler.SchedulerException;
import org.mifos.framework.hibernate.HibernateStartUp;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.authorization.AuthorizationManager;
import org.mifos.framework.security.authorization.HierarchyManager;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.FilePaths;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

import junit.framework.TestCase;

public class TestAccountFeesEntity extends TestCase {
	protected AccountBO accountBO=null;
	protected CustomerBO center=null;
	protected CustomerBO group=null;
	protected AccountPersistence accountPersistence;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		accountPersistence = new AccountPersistence();
	}


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		TestObjectFactory.cleanUp(accountBO);
		TestObjectFactory.cleanUp(group);
		TestObjectFactory.cleanUp(center);
		accountPersistence = null;
		HibernateUtil.closeSession();
	}

	public AccountBO getLoanAccount()
	{ 
        MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getMeetingHelper(1,1,4,2));
        center=TestObjectFactory.createCenter("Center",Short.valueOf("13"),"1.1",meeting,new Date(System.currentTimeMillis()));
        group=TestObjectFactory.createGroup("Group",Short.valueOf("9"),"1.1.1",center,new Date(System.currentTimeMillis()));
        LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering("Loan",Short.valueOf("2"),
        		new Date(System.currentTimeMillis()),Short.valueOf("1"),300.0,1.2,Short.valueOf("3"),
        		Short.valueOf("1"),Short.valueOf("1"),Short.valueOf("1"),Short.valueOf("1"),Short.valueOf("1"),
        		meeting);
        return TestObjectFactory.createLoanAccount("42423142341",group,Short.valueOf("5"),new Date(System.currentTimeMillis()),loanOffering);
   }

	
	
	public void testChangeFeesStatus(){
		accountBO=getLoanAccount();
		Set<AccountFeesEntity> accountFeesEntitySet=accountBO.getAccountFees();
		for(AccountFeesEntity accountFeesEntity: accountFeesEntitySet){
			accountFeesEntity.changeFeesStatus(AccountConstants.INACTIVE_FEES,new Date(System.currentTimeMillis()));
			assertEquals(accountFeesEntity.getFeeStatus(),AccountConstants.INACTIVE_FEES);
		}
	}
	
	
	public void testIsApplicable() throws RepaymentScheduleException, SchedulerException{
		accountPersistence = new AccountPersistence();
		MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory.getMeetingHelper(1,1,4,2));
        center=TestObjectFactory.createCenter("Center",Short.valueOf("13"),"1.1",meeting,new Date(System.currentTimeMillis()));
        AccountFeesEntity accountPeriodicFee = new AccountFeesEntity();
		accountPeriodicFee.setAccount(center.getCustomerAccount());
		accountPeriodicFee.setAccountFeeAmount(new Money("100.0"));
		accountPeriodicFee.setFeeAmount(new Money("100.0"));
		FeesBO trainingFee = TestObjectFactory.createPeriodicFees("Training_Fee", 100.0, 1,
				2, 5);
		accountPeriodicFee.setFees(trainingFee);
		center.getCustomerAccount().getAccountFees().add(accountPeriodicFee);
        Date currentDate=DateUtils.getCurrentDateWithoutTimeStamp();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(currentDate);
        calendar.add(calendar.WEEK_OF_MONTH,-1);
        Date lastAppliedFeeDate = new Date(calendar.getTimeInMillis());
        for (AccountFeesEntity accountFeesEntity : center.getCustomerAccount().getAccountFees()) {
			accountFeesEntity.setLastAppliedDate(lastAppliedFeeDate);
		}
        TestObjectFactory.updateObject(center);
        TestObjectFactory.flushandCloseSession();
        group=TestObjectFactory.createGroup("Group",Short.valueOf("9"),"1.1.1",center,new Date(System.currentTimeMillis()));       		
		Set<AccountFeesEntity> accountFeeSet = group.getCustomerAccount().getAccountFees();
		assertEquals(1,accountFeeSet.size());
		for (AccountFeesEntity periodicFees : center.getCustomerAccount().getAccountFees()) {
			if(periodicFees.getFees().getFeeName().equalsIgnoreCase("Training_Fee"))
				assertEquals(false,periodicFees.isApplicable(currentDate));
			else
				assertEquals(true,periodicFees.isApplicable(currentDate));
		}
	}

}
