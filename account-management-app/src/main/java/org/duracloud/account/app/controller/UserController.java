/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.app.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.duracloud.account.common.domain.AccountInfo;
import org.duracloud.account.common.domain.AccountInfo.AccountStatus;
import org.duracloud.account.common.domain.DuracloudAccount;
import org.duracloud.account.common.domain.DuracloudUser;
import org.duracloud.account.common.domain.InstanceType;
import org.duracloud.account.compute.error.DuracloudInstanceNotAvailableException;
import org.duracloud.account.db.error.DBNotFoundException;
import org.duracloud.account.util.AccountManagerService;
import org.duracloud.account.util.DuracloudInstanceManagerService;
import org.duracloud.account.util.DuracloudInstanceService;
import org.duracloud.account.util.DuracloudUserService;
import org.duracloud.account.util.error.InvalidPasswordException;
import org.duracloud.account.util.error.InvalidRedemptionCodeException;
import org.duracloud.account.util.error.UnsentEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * The default view for this application
 * 
 * @contributor dbernstein
 */
@Controller
@Lazy
@RequestMapping(AbstractController.USERS_MAPPING)
public class UserController extends AbstractController {

    public static final String USER_KEY = "user";

    public static final String NEW_USER_VIEW = "user-new";
    
    public static final String FORGOT_PASSWORD_VIEW = "forgot-password";

    public static final String USER_HOME = "user-home";
    
    public static final String FORGOT_PASSWORD_MAPPING = "/forgot-password";

    public static final String USER_ACCOUNTS = "user-accounts";

    public static final String USER_EDIT_MAPPING = USER_MAPPING + EDIT_MAPPING;

    public static final String USER_ACCOUNTS_MAPPING = USER_MAPPING
            + "/accounts";

    public static final String USER_EDIT_VIEW = "user-edit";

    public static final String CHANGE_PASSWORD_MAPPING = USER_MAPPING
            + "/change-password";
    public static final String CHANGE_PASSWORD_VIEW = "user-change-password";
    public static final String USER_PROFILE_FORM_KEY = "userProfileEditForm";
    public static final String CHANGE_PASSWORD_FORM_KEY = "changePasswordForm";
    public static final String FORGOT_PASSWORD_FORM_KEY = "forgotPasswordForm";
    public static final String NEW_USER_FORM_KEY = "newUserForm";
    public static final String NEW_INSTANCE_FORM = "instanceForm";
    @Autowired
    private AccountManagerService accountManagerService;

    @Autowired
    private DuracloudUserService userService;

    @Autowired(required = true)
    protected DuracloudInstanceManagerService instanceManagerService;

    /**
     * 
     * @param userService
     */
    public void setUserService(DuracloudUserService userService) {
        if (userService == null) {
            throw new NullPointerException("userService must be non-null");
        }

        this.userService = userService;
        log.info("new instance created: " + getClass().getName());
    }

    public DuracloudUserService getUserService() {
        return this.userService;
    }

    @RequestMapping(value = { NEW_MAPPING }, method = RequestMethod.GET)
    public ModelAndView getNewForm(HttpServletRequest request) {
        log.info("serving up NewUserForm");
        NewUserForm newUserForm = new NewUserForm();
        newUserForm.setRedemptionCode(removeRedemptionCodeFromSession(request));
        return new ModelAndView(NEW_USER_VIEW, NEW_USER_FORM_KEY, newUserForm);
    }

    @RequestMapping(value = { FORGOT_PASSWORD_MAPPING }, method = RequestMethod.GET)
    public ModelAndView getForgotPasswordForm(HttpServletRequest request) {
        log.info("serving up ForgotPasswordForm");
        ForgotPasswordForm forgotPasswordForm = new ForgotPasswordForm();
        return new ModelAndView(FORGOT_PASSWORD_VIEW,
                                FORGOT_PASSWORD_FORM_KEY,
                                forgotPasswordForm);
    }

    @ModelAttribute("instanceTypes")
    public InstanceType[] instanceTypes(){
        return InstanceType.values();
    }
    
    @RequestMapping(value = { "/profile" }, method = RequestMethod.GET)
    public ModelAndView profileRedirect() {
        Authentication auth = SecurityContextHolder.getContext()
            .getAuthentication();
        if(auth.isAuthenticated() && auth instanceof AnonymousAuthenticationToken){
            //this check is necessary because on logout the browser is getting directed here
            //I'm not sure why the request is getting through - everything seems properly configured
            //in security-config.xml
            auth.setAuthenticated(false);
            return new ModelAndView("redirect:/users/profile");
        }
        String username = auth.getName();
        return new ModelAndView(formatUserRedirect(username));
    }

    @RequestMapping(value = { USER_MAPPING }, method = RequestMethod.GET)
    public ModelAndView getUser(@PathVariable String username,
                                HttpServletRequest request)
        throws DBNotFoundException, DuracloudInstanceNotAvailableException {
        log.debug("getting user {}", username);
        // if there's a redemption code in the session, it means that
        // the user logged in in order to redeem an invitation
        String redemptionCode = removeRedemptionCodeFromSession(request);
        if (redemptionCode != null) {
            log.debug("redemption code found in session: {}", redemptionCode);
            DuracloudUser user = this.userService.loadDuracloudUserByUsername(username);
            try {
                this.userService.redeemAccountInvitation(user.getId(),
                                                         redemptionCode);
                user = this.userService.loadDuracloudUserByUsername(username);
            } catch (InvalidRedemptionCodeException e) {
                log.error("redemption failed for {} on redemption {}",
                          username,
                          redemptionCode);
                addRedemptionFailedMessage();
            }
        }

        return getUserAccounts(username);
    }

    /**
     * 
     */
    private void addRedemptionFailedMessage() {
        addErrorMessage("The redemption code you supplied is invalid. We are unable to add you to an account.");
    }

    /**
     * @param request
     * @return
     */
    private String removeRedemptionCodeFromSession(HttpServletRequest request) {
        String redemptionCode = (String) request.getSession()
                                                .getAttribute("redemptionCode");
        if (redemptionCode != null) {
            request.getSession().removeAttribute("redemptionCode");
        }
        return redemptionCode;
    }

    @RequestMapping(value = { USER_ACCOUNTS_MAPPING }, method = RequestMethod.GET)
    public ModelAndView getUserAccounts(@PathVariable String username)
        throws DBNotFoundException, DuracloudInstanceNotAvailableException {
        log.debug("getting user accounts for {}", username);
        ModelAndView mav = new ModelAndView(USER_ACCOUNTS);
        prepareModel(username, mav);
        return mav;
    }

    @RequestMapping(value = { USER_EDIT_MAPPING }, method = RequestMethod.GET)
    public String edit(@PathVariable String username, Model model) throws DBNotFoundException {
        log.debug("getting user accounts for {}", username);
        UserProfileEditForm form = new UserProfileEditForm();
        DuracloudUser user = this.userService.loadDuracloudUserByUsername(username);
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setSecurityQuestion(user.getSecurityQuestion());
        form.setSecurityAnswer(user.getSecurityAnswer());
        model.addAttribute(USER_PROFILE_FORM_KEY, form);

        model.addAttribute(CHANGE_PASSWORD_FORM_KEY, new ChangePasswordForm());

        addUserToModel(user, model);
        return USER_EDIT_VIEW;
    }

    @RequestMapping(value = { USER_EDIT_MAPPING }, method = RequestMethod.POST)
    public ModelAndView update(@PathVariable String username,
                         @ModelAttribute(USER_PROFILE_FORM_KEY) @Valid UserProfileEditForm form,
                         BindingResult result,
                         Model model) throws Exception {

        if (result.hasErrors()) {
            log.debug("profile form has errors for {}: returning...", username);
            model.addAttribute(CHANGE_PASSWORD_FORM_KEY,
                               new ChangePasswordForm());

            DuracloudUser user = this.userService.loadDuracloudUserByUsername(username);
            addUserToModel(user, model);

            return new ModelAndView(USER_EDIT_VIEW, model.asMap());
        }

        log.info("updating user profile for {}", username);
        int id = this.userService.loadDuracloudUserByUsername(username).getId();
        this.userService.storeUserDetails(id,
                                          form.getFirstName(),
                                          form.getLastName(),
                                          form.getEmail(),
                                          form.getSecurityQuestion(),
                                          form.getSecurityAnswer());

        return new ModelAndView(formatUserRedirect(username));
    }

    @RequestMapping(value = { CHANGE_PASSWORD_MAPPING }, method = RequestMethod.GET)
    public String changePassword(@PathVariable String username, Model model) throws DBNotFoundException {
        log.debug("opening change password form  for {}", username);
        model.addAttribute(CHANGE_PASSWORD_FORM_KEY, new ChangePasswordForm());
        // add related model objects
        addUserToModel(this.userService.loadDuracloudUserByUsername(username),
                       model);
        return CHANGE_PASSWORD_VIEW;
    }

    @RequestMapping(value = { CHANGE_PASSWORD_MAPPING }, method = RequestMethod.POST)
    public ModelAndView changePassword(@PathVariable String username,
                                 @ModelAttribute(CHANGE_PASSWORD_FORM_KEY) @Valid ChangePasswordForm form,
                                 BindingResult result,
                                 Model model) throws Exception {

        DuracloudUser user = this.userService.loadDuracloudUserByUsername(username);

        // check for errors
        if (!result.hasErrors()) {
            log.info("changing user password for {}", username);
            int id = user.getId();
            try {
                this.userService.changePassword(id,
                                                form.getOldPassword(),
                                                false,
                                                form.getPassword());
                return new ModelAndView(formatUserRedirect(username));
            } catch (InvalidPasswordException e) {
                result.addError(new FieldError(CHANGE_PASSWORD_FORM_KEY,
                                               "oldPassword",
                                               "The old password is not correct"));
            }
        }

        log.debug("password form has errors for {}: returning...", username);
        addUserToModel(user, model);

        UserProfileEditForm editForm = new UserProfileEditForm();

        editForm.setFirstName(user.getFirstName());
        editForm.setLastName(user.getLastName());
        editForm.setEmail(user.getEmail());
        editForm.setSecurityQuestion(user.getSecurityQuestion());
        editForm.setSecurityAnswer(user.getSecurityAnswer());
        model.addAttribute(USER_PROFILE_FORM_KEY, editForm);
        return new ModelAndView(USER_EDIT_VIEW, model.asMap());

    }

    /**
     * @param user
     * @param mav
     */
    private void prepareModel(DuracloudUser user, ModelAndView mav)
        throws DuracloudInstanceNotAvailableException {
        mav.addObject(USER_KEY, user);
        Set<AccountInfo> accounts = this.accountManagerService.findAccountsByUserId(user.getId());
        mav.addObject(NEW_INSTANCE_FORM, new AccountInstanceForm());

        List<DuracloudAccount> activeAccounts = new ArrayList<DuracloudAccount>();
        List<DuracloudAccount> inactiveAccounts = new ArrayList<DuracloudAccount>();
        List<DuracloudAccount> pendingAccounts = new ArrayList<DuracloudAccount>();
        List<DuracloudAccount> cancelledAccounts = new ArrayList<DuracloudAccount>();

        Iterator<AccountInfo> iterator = accounts.iterator();
        while (iterator.hasNext()) {
            AccountInfo acctInfo = iterator.next();
            DuracloudAccount duracloudAccount = loadAccountInstances(acctInfo,
                                                                     user);
            AccountStatus status = acctInfo.getStatus();
            if (AccountInfo.AccountStatus.ACTIVE.equals(status)) {
                activeAccounts.add(duracloudAccount);
            } else if (AccountInfo.AccountStatus.INACTIVE.equals(status)) {
                inactiveAccounts.add(duracloudAccount);
            } else if (AccountInfo.AccountStatus.PENDING.equals(status)) {
                pendingAccounts.add(duracloudAccount);
            } else if (AccountInfo.AccountStatus.CANCELLED.equals(status)) {
                cancelledAccounts.add(duracloudAccount);
            }
        }
        Collections.sort(activeAccounts);
        Collections.sort(inactiveAccounts);
        Collections.sort(pendingAccounts);
        Collections.sort(cancelledAccounts);
        mav.addObject("activeAccounts", activeAccounts);
        mav.addObject("inactiveAccounts", inactiveAccounts);
        mav.addObject("pendingAccounts", pendingAccounts);
        mav.addObject("cancelledAccounts", cancelledAccounts);

        mav.addObject("latestVersion", instanceManagerService.getLatestVersion());
    }

    private DuracloudAccount loadAccountInstances(AccountInfo accountInfo,
                                                  DuracloudUser user)
        throws DuracloudInstanceNotAvailableException {
        DuracloudAccount duracloudAccount = new DuracloudAccount();
        duracloudAccount.setAccountInfo(accountInfo);
        duracloudAccount.setUserRole(user.getRoleByAcct(accountInfo.getId()));

        Set<DuracloudInstanceService> instanceServices = instanceManagerService.getInstanceServices(accountInfo.getId());
        if (instanceServices.size() > 0) {
            // Handle only a single instance for the time being
            DuracloudInstanceService instanceService = instanceServices.iterator()
                                                                       .next();
            duracloudAccount.setInstance(instanceService.getInstanceInfo());
            duracloudAccount.setInstanceStatus(instanceService.getStatus());
            duracloudAccount.setInstanceVersion(instanceService.getInstanceVersion());
            duracloudAccount.setInstanceType(instanceService.getInstanceType());
        } else {
            AccountStatus accountStatus = accountInfo.getStatus();
            if (AccountInfo.AccountStatus.ACTIVE.equals(accountStatus)
                    || AccountInfo.AccountStatus.INACTIVE.equals(accountStatus)) {
                Set<String> versions = instanceManagerService.getVersions();
                duracloudAccount.setVersions(versions);
            }
        }
        return duracloudAccount;
    }

    private void addUserToModel(DuracloudUser user, Model model) {
        model.addAttribute(USER_KEY, user);
    }

    /**
     * @param mav
     */
    private void prepareModel(String username, ModelAndView mav)
        throws DBNotFoundException, DuracloudInstanceNotAvailableException {
        DuracloudUser user = this.userService.loadDuracloudUserByUsernameInternal(username);
        prepareModel(user, mav);
    }

    @RequestMapping(value = { NEW_MAPPING }, method = RequestMethod.POST)
    public ModelAndView add(@ModelAttribute(NEW_USER_FORM_KEY) @Valid NewUserForm newUserForm,
                      BindingResult result,
                      Model model, 
                      RedirectAttributes redirectAttributes) throws Exception {

        String name = null == newUserForm ? "null" : newUserForm.getUsername();
        log.debug("Add new user: {}", name);
        if (result.hasErrors()) {
            return new ModelAndView(NEW_USER_VIEW, model.asMap());
        }

        DuracloudUser user = this.userService.createNewUser(newUserForm.getUsername(),
                                                            newUserForm.getPassword(),
                                                            newUserForm.getFirstName(),
                                                            newUserForm.getLastName(),
                                                            newUserForm.getEmail(),
                                                            newUserForm.getSecurityQuestion(),
                                                            newUserForm.getSecurityAnswer());

        String redemptionCode = newUserForm.getRedemptionCode();
        int accountId = -1;
        if (!StringUtils.isEmpty(redemptionCode)) {
            try {
                accountId = this.userService.redeemAccountInvitation(user.getId(),
                                                                     redemptionCode);

            } catch (InvalidRedemptionCodeException ex) {
                addRedemptionFailedMessage();
            }
        }

        String userUrl = formatUserUrl(newUserForm.getUsername());

        if (accountId > -1) {
            userUrl += "?accountId=" + accountId;
        }

        ModelAndView mav = new ModelAndView("user-new-success");
        mav.addObject("userUrl", userUrl);
        return mav;
    }



    protected static RedirectView formatUserRedirect(String username) {
        String redirect = formatUserUrl(username);
        RedirectView view = new RedirectView(redirect, true);
        view.setExposeModelAttributes(false);
        return view;
    }

    @RequestMapping(value = { FORGOT_PASSWORD_MAPPING }, method = RequestMethod.POST)
    public String forgotPassword(@ModelAttribute(FORGOT_PASSWORD_FORM_KEY) @Valid ForgotPasswordForm forgotPasswordForm,
                                 BindingResult result,
                                 Model model,
                                 HttpServletRequest request) throws Exception {

        model.addAttribute(FORGOT_PASSWORD_FORM_KEY, forgotPasswordForm);

        if (!result.hasErrors()) {

            try {

                String username = forgotPasswordForm.getUsername();

                if (StringUtils.isEmpty(forgotPasswordForm.getSecurityQuestion())) {
                    DuracloudUser user = this.userService.loadDuracloudUserByUsernameInternal(username);
                    forgotPasswordForm.setSecurityQuestion(user.getSecurityQuestion());
                }

                if (StringUtils.isEmpty(forgotPasswordForm.getSecurityAnswer())) {
                    return FORGOT_PASSWORD_VIEW;
                }

                
                this.userService.forgotPassword(username,
                                                forgotPasswordForm.getSecurityQuestion(),
                                                forgotPasswordForm.getSecurityAnswer());
            } catch (DBNotFoundException e) {
                result.addError(new FieldError(FORGOT_PASSWORD_FORM_KEY,
                                               "username",
                                               "The username does not exist"));
                return FORGOT_PASSWORD_VIEW;
            } catch (InvalidPasswordException e) {
                result.addError(new FieldError(FORGOT_PASSWORD_FORM_KEY,
                                               "securityQuestion",
                                               "The security answer is not correct"));
                return FORGOT_PASSWORD_VIEW;
            } catch (UnsentEmailException ue) {
                result.addError(new ObjectError(FORGOT_PASSWORD_FORM_KEY,
                                                "Unable to send email to the address associated with the username"));
                return FORGOT_PASSWORD_VIEW;
            }

            String redirect = "redirect:/login";

            return redirect;
        }

        return FORGOT_PASSWORD_VIEW;
    }

    /**
     * @param string
     */
    private void addErrorMessage(String string) {
        // TODO Auto-generated method stub

    }

    @RequestMapping(value = { "/redeem/{redemptionCode}" }, method = RequestMethod.GET)
    public ModelAndView redeemUser(HttpServletRequest request,
                                   @PathVariable String redemptionCode) throws DBNotFoundException {
        log.info("getting redeem invitation {}", redemptionCode);

        // force logout
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        // add the redemption code to the session
        request.getSession(true).setAttribute("redemptionCode", redemptionCode);
        ModelAndView mav = new ModelAndView(HomeController.HOME_VIEW_ID);
        mav.addObject("redemptionCode", redemptionCode);
        return mav;
    }

    public void setAccountManagerService(AccountManagerService accountManagerService) {
        this.accountManagerService = accountManagerService;
    }

    public AccountManagerService getAccountManagerService() {
        return accountManagerService;
    }

    public DuracloudInstanceManagerService getInstanceManagerService() {
        return instanceManagerService;
    }

    public void setInstanceManagerService(DuracloudInstanceManagerService instanceManagerService) {
        this.instanceManagerService = instanceManagerService;
    }
}
