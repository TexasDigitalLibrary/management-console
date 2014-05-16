/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.security.vote;

import org.aopalliance.intercept.MethodInvocation;
import org.duracloud.account.db.model.DuracloudUser;
import org.duracloud.account.db.repo.DuracloudRepoMgr;
import org.duracloud.account.db.util.AccountService;
import org.duracloud.account.security.domain.SecuredRule;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import java.util.Collection;

/**
 * This class votes on calls to the AccountAccessService.
 *
 * @author Andrew Woods
 *         Date: 4/6/11
 */
public class AccountAccessDecisionVoter extends BaseAccessDecisionVoter {

    private Logger log = LoggerFactory.getLogger(AccountAccessDecisionVoter.class);

    public AccountAccessDecisionVoter(DuracloudRepoMgr repoMgr) {
        super(repoMgr);
    }

    @Override
    protected Class<?> getTargetService() {
        return AccountService.class;
    }

    @Override
    public int vote(Authentication authentication,
                    MethodInvocation invocation,
                    Collection<ConfigAttribute> configAttributes) {
        int decision = ACCESS_DENIED;

        // Collect target method invocation.
        if (!supportsTarget(invocation)) {
            return castVote(ACCESS_ABSTAIN, invocation);
        }

        // Collect user making the call.
        DuracloudUser user = getCurrentUser(authentication);

        // Collect security constraints on method.
        SecuredRule securedRule = getRule(configAttributes);
        String role = securedRule.getRole().name();
        SecuredRule.Scope scope = securedRule.getScope();

        if (scope.equals(SecuredRule.Scope.ANY)) {
            Collection<String> userRoles = getUserRoles(authentication);
            decision = super.voteHasRole(role, userRoles);

        } else if (scope.equals(SecuredRule.Scope.SELF_ACCT)) {
            Long acctId = getAcctId(invocation);
            decision = voteUserHasRoleOnAccount(user, role, acctId);

        } else {
            String err = "Invalid scope: " + scope;
            log.error(err);
            throw new DuraCloudRuntimeException(err);
        }

        return castVote(decision, invocation);
    }

    private Long getAcctId(MethodInvocation invocation) {
        AccountService acctService = (AccountService) invocation.getThis();
        return acctService.getAccountId();
    }

    private int castVote(int decision, MethodInvocation invocation) {
        String methodName = invocation.getMethod().getName();
        String className = invocation.getThis().getClass().getSimpleName();
        log.trace("{}.{}() = {}", new Object[]{className, methodName, asString(
            decision)});
        return decision;
    }

}
