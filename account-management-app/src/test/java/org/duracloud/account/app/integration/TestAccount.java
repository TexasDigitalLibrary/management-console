/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.app.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.selenium.SeleniumException;

/**
 * 
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
public class TestAccount extends AbstractIntegrationTest {
	private String accountId = null;

	
	/* (non-Javadoc)
	 * @see org.duracloud.account.app.integration.AbstractIntegrationTest#before()
	 */
	@Override
	@Before
	public void before() throws Exception {
		super.before();
		openUserProfilePage("admin");
		loginAdmin();
    	accountId = AccountTestHelper.createAccount(sc);
	}

	/* (non-Javadoc)
	 * @see org.duracloud.account.app.integration.AbstractIntegrationTest#after()
	 */
	@Override
	@After
	public void after() {
		logout();
		super.after();
	}

	@Test
	public void test(){
		Assert.assertNotNull(this.accountId);
		openAccountHome(this.accountId);
		Assert.assertTrue(sc.isElementPresent("id=account-id"));
	}
	@Test
	public void testFailedAuthorization(){
		logout();
		try {
	    	openAccountHome(accountId);
			login("user", "user");
			Assert.assertTrue(false);
		} catch (SeleniumException e) {
			Assert.assertTrue(e.getMessage().contains("403"));
		}
	}
	
}
