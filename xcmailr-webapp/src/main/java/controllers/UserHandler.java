/**  
 *  Copyright 2013 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package controllers;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import conf.XCMailrConf;
import models.Domain;
import models.UserFormData;
import models.User;
import ninja.Context;
import ninja.FilterWith;
import ninja.Result;
import ninja.Results;
import ninja.i18n.Lang;
import ninja.i18n.Messages;
import ninja.validation.JSR303Validation;
import ninja.validation.Validation;
import etc.HelperUtils;
import filters.SecureFilter;

/**
 * Handles the actions of the User-Object
 * 
 * @see User
 * @author Patrick Thum 2012 released under Apache 2.0 License
 */
@FilterWith(SecureFilter.class)
@Singleton
public class UserHandler
{
    @Inject
    CachingSessionHandler cachingSessionHandler;

    @Inject
    XCMailrConf xcmConfiguration;

    @Inject
    Messages msg;

    @Inject
    Lang lang;

    /**
     * Edits the {@link User}-Data <br/>
     * POST /user/edit
     * 
     * @param context
     *            the Context of this Request
     * @param userFormData
     *            the Data of the User-Edit-Form
     * @param validation
     *            Form validation
     * @return the Edit-Page again
     */
    public Result editUserProcess(Context context, @JSR303Validation UserFormData userFormData, Validation validation)
    {
        // TODO eventually rearrange the big if-else-block in a "positive" way (->error-cases at the end to aggregate
        // the clearpasswordfields()- and render-calls)

        Result result = Results.html().template("/views/UserHandler/editUserForm.ftl.html");
        // set the available languages again. in most cases this may not be necessary,
        // but if you send the post-request directly and have form violations or wrong passwords or sth.
        // then you would likely get a NullPointerException
        List<String[]> availableLanguages = HelperUtils.getLanguageList(xcmConfiguration.APP_LANGS, context, result, msg);
        result.render("available_langs", availableLanguages);

        User user = context.getAttribute("user", User.class);
        String oldMail = user.getMail();
        if (validation.hasViolations())
        { // the filled form has errors
            context.getFlashCookie().error("flash_FormError");
            return Results.redirect(context.getContextPath() + "/user/edit");
        }
        else
        { // the form is filled correctly

            // don't let the user register with one of our domains
            // (prevent mail-loops)
            String mailFromForm = userFormData.getMail();
            String domainPart = mailFromForm.split("@")[1];
            if (Arrays.asList(xcmConfiguration.DOMAIN_LIST).contains(domainPart))
            {
                context.getFlashCookie().error("flash_NoLoop");
                userFormData.setMail(user.getMail());
                userFormData.clearPasswordFields();
                return result.render(userFormData);
            }

            // block the editing, if the domain is not on the whitelist (and the whitelisting is active)
            if (xcmConfiguration.APP_WHITELIST)
            { // whitelisting is active
                if (!Domain.getAll().isEmpty() && !Domain.exists(domainPart))
                { // the domain is not in the whitelist and the whitelist is not empty
                    context.getFlashCookie().error("editUser_Flash_NotWhitelisted");
                    userFormData.clearPasswordFields();
                    return result.render(userFormData);
                }
            }

            if (user.checkPasswd(userFormData.getPassword()))
            { // the user authorized himself

                if (!mailFromForm.equals(oldMail))
                { // the user's mail-address changed
                    if (User.mailExists(mailFromForm))
                    {// return an error that the mail exists
                        context.getFlashCookie().error("flash_MailExists");
                        userFormData.clearPasswordFields();

                        return result.render(userFormData);
                    }
                    else
                    { // the address does not exist -> success!
                        user.setMail(userFormData.getMail());
                    }
                }
                // set the language
                if (Arrays.asList(xcmConfiguration.APP_LANGS).contains(userFormData.getLanguage()))
                { // set the selected language in the user-object and also in the application
                    user.setLanguage(userFormData.getLanguage());
                    lang.setLanguage(userFormData.getLanguage(), result);
                }

                // update the fore- and surname
                user.setForename(userFormData.getFirstName());
                user.setSurname(userFormData.getSurName());

                String password1 = userFormData.getPasswordNew1();
                String password2 = userFormData.getPasswordNew2();

                // check whether the new passwords are whitespace, null or empty strings
                if (!StringUtils.isBlank(password1) && !StringUtils.isBlank(password2))
                { // new password has been entered
                    if (password1.equals(password2))
                    { // the repetition is equal to the new pw
                        if (password1.length() < xcmConfiguration.PW_LENGTH)
                        { // the new password is too short
                            Optional<String> opt = Optional.of(user.getLanguage());
                            String tooShortPassword = msg.get("flash_PasswordTooShort", opt, xcmConfiguration.PW_LENGTH)
                                                         .get();
                            context.getFlashCookie().error(tooShortPassword);
                            userFormData.clearPasswordFields();

                            return result.render(userFormData);
                        }
                        user.hashPasswd(password2);
                    }
                    else
                    { // the passwords are not equal
                        context.getFlashCookie().error("flash_PasswordsUnequal");
                        userFormData.clearPasswordFields();
                        return result.render(userFormData);
                    }
                }

                // update the user
                user.update();
                
                // update the entries in the caching-server
                if (!oldMail.equals(mailFromForm))
                { // update the cached session-list
                    cachingSessionHandler.updateUsersSessionsOnChangedMail(oldMail, user.getMail());
                    // set the new mail if it has changed correctly
                    context.getSessionCookie().put("username", user.getMail());
                }
                // update all user objects for all sessions
                cachingSessionHandler.updateUsersSessions(user);
                
                // user-edit was successful
                context.getFlashCookie().success("flash_DataChangeSuccess");
                return result.redirect(context.getContextPath() + "/user/edit");
            }
            else
            { // the authorization-process failed
                userFormData.clearPasswordFields();
                context.getFlashCookie().error("flash_FormError");
                return result.redirect(context.getContextPath() + "/user/edit");
            }
        }
    }

    /**
     * Prepopulates the EditForm and show it <br/>
     * GET /user/edit
     * 
     * @param context
     *            the Context of this Request
     * @return the {@link User}-Edit-Form
     */

    public Result editUserForm(Context context)
    {
        Result result = Results.html();
        List<String[]> availableLanguageList = HelperUtils.getLanguageList(xcmConfiguration.APP_LANGS, context, result,
                                                                           msg);
        result.render("available_langs", availableLanguageList);
        User user = context.getAttribute("user", User.class);

        // handle the possibility, that the user has no language set (compatibility from updates of old versions, when
        // there was no language-attribute)
        if (user.getLanguage() == null || user.getLanguage() == "")
        {
            Optional<Result> opt = Optional.of(result);
            user.setLanguage(lang.getLanguage(context, opt).get());
            user.update();
            cachingSessionHandler.replace(context.getSessionCookie().getId(), xcmConfiguration.COOKIE_EXPIRETIME, user);
        }

        UserFormData userFormData = UserFormData.prepopulate(user);
        return result.render(userFormData);
    }

    /**
     * Handles the {@link models.User User}-Delete-Function <br/>
     * POST /user/delete
     * 
     * @param context
     *            the Context of this Request
     * @return the User-Overview-Page (/admin/users)
     */
    public Result deleteUserProcess(Context context)
    {
        String password = context.getParameter("password");
        User user = context.getAttribute("user", User.class);
        if (!StringUtils.isBlank(password))
        {
            if (user.checkPasswd(password))
            {
                if (!user.isLastAdmin())
                { // delete the session
                    context.getSessionCookie().clear();
                    cachingSessionHandler.deleteUsersSessions(user);
                    // delete the user-account
                    User.delete(user.getId());
                    context.getFlashCookie().success("deleteUser_Flash_Success");
                    return Results.redirect(context.getContextPath() + "/");
                }
                else
                { // can't delete the user, because he's the last admin
                    context.getFlashCookie().error("deleteUser_Flash_Failed");
                }
            }
            else
            { // the entered password was wrong
                context.getFlashCookie().error("deleteUser_Flash_WrongPassword");
            }
        }
        else
        { // no password entered
        }
        return Results.redirect(context.getContextPath() + "/user/edit");
    }
}
