package org.duracloud.security.vote;

import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.vote.AbstractAccessDecisionManager;
import org.springframework.security.vote.AccessDecisionVoter;

import java.util.Iterator;

/**
 * This class polls its internal list of AccessDecisionVoters to come to an
 * AuthZ decision for the principal (authentication) and resource.
 *
 * @author Andrew Woods
 *         Date: Mar 12, 2010
 */
public class AccessDecisionManagerImpl extends AbstractAccessDecisionManager {

    /**
     * <pre>
     * This method implements to AccessDecisionManager interface.
     * Each voter in the internal list of AccessDecisionVoters is presented with
     * all of the configAttributes for the arg resource.
     * - If all voters abstain from voting, the decision goes to the default
     *     setting: allowIfAllAbstainDecisions
     * - If no voter denies the AuthZ vote and at least one voter grants, then
     *   AuthZ is granted.
     * </pre>
     *
     * @param authentication principal seeking AuthZ
     * @param resource       that is under protection
     * @param config         access-attributes defined on resource
     * @throws AccessDeniedException if AuthZ denied
     */
    public void decide(Authentication authentication,
                       Object resource,
                       ConfigAttributeDefinition config)
        throws AccessDeniedException {

        int grant = 0;
        Iterator voters = this.getDecisionVoters().iterator();
        while (voters.hasNext()) {
            AccessDecisionVoter voter = (AccessDecisionVoter) voters.next();

            int decision = voter.vote(authentication, resource, config);
            switch (decision) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    grant++;
                    break;

                case AccessDecisionVoter.ACCESS_DENIED:
                    throw new AccessDeniedException(messages.getMessage(
                        "AbstractAccessDecisionManager.accessDenied",
                        "Access is denied"));

                default:
                    break;
            }
        }

        // To get this far, there were no deny votes
        if (grant > 0) {
            return;
        }

        // To get this far, every AccessDecisionVoter abstained
        checkAllowIfAllAbstainDecisions();

    }

}