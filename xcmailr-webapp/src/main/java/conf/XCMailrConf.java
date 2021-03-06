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
package conf;

import ninja.utils.NinjaProperties;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Thum, Xceptance Software Technologies GmbH, Germany
 */
@Singleton
public class XCMailrConf
{
    /**
     * specified with mbox.adminaddr
     */
    public final String ADMIN_ADDRESS;

    /**
     * specified with admin.pass
     */
    public final String ADMIN_PASSWORD;

    /**
     * specified with application.basedir
     */
    public final String APP_BASEPATH;

    /**
     * specified with application.url
     */
    public final String APP_HOME;

    /**
     * specified with application.languages
     */
    public final String[] APP_LANGS;

    /**
     * specified with application.name
     */
    public final String APP_NAME;

    /**
     * specified with application.default.entriesperpage
     * default value is 15
     */
    public final Integer APP_DEFAULT_ENTRYNO;
    
    /**
     * indicates the use of whitelisting for registration
     */
    public final Boolean APP_WHITELIST;

    /**
     * specified with application.session.expire_time_in_seconds
     */
    public final Integer COOKIE_EXPIRETIME;

    /**
     * specified with application.cookie.prefix
     */
    public final String COOKIE_PREFIX;

    /**
     * specified with confirm.period
     */
    public final Integer CONFIRMATION_PERIOD;

    /**
     * specified with mbox.dlist (simple string)
     */
    public final String D_LIST;

    /**
     * specified with mbox.dlist (string-array)
     */
    public final String[] DOMAIN_LIST;
    
    /**
     * specified with mbox.host
     */
    public final String MB_HOST;

    /**
     * specified with mbox.interval
     */
    public final Integer MB_INTERVAL;

    /**
     * specified with mbox.port
     */
    public final Integer MB_PORT;

    /**
     * specified with memcached.host
     */
    public final String MEMCA_HOST;

    /**
     * indicates whether the forward-message should be wrapped in a new mail <br/>
     * containing the original-message header in the body
     */
    public final Boolean MSG_REWRITE;
    
    /**
     * The number of MTXs as limit to display at the mtx-page specified with mailtransaction.displaylimit
     * Default value is 0 (no limit)
     */
    public final Integer MTX_LIMIT;
    
    /**
     * The number of MTXs as limit to display at the mtx-page specified with mailtransaction.maxage
     * Default value is -1 (no automatic deletion)
     */
    public final Integer MTX_MAX_AGE;


    /**
     * specified with mail.smtp.auth
     */
    public final Boolean OUT_SMTP_AUTH;

    /**
     * specified with mail.smtp.debug
     * default value is true
     */
    public final Boolean OUT_SMTP_DEBUG;

    /**
     * specified with mail.smtp.host
     */
    public final String OUT_SMTP_HOST;

    /**
     * specified with mail.smtp.pass
     */
    public final String OUT_SMTP_PASS;

    /**
     * specified with mail.smtp.port
     */
    public final Integer OUT_SMTP_PORT;

    /**
     * specified with mail.smtp.tls
     */
    public final Boolean OUT_SMTP_TLS;

    /**
     * specified with mail.smtp.user
     */
    public final String OUT_SMTP_USER;

    /**
     * specified with pw.length
     */
    public final Integer PW_LENGTH;

    /**
     * this is the cookie expiration-time in seconds as string<br/>
     * suffixed with "s" for ninjaCache
     */
    public final String SESSION_EXPIRETIME;

    @Inject
    public XCMailrConf(NinjaProperties ninjaProp)
    {
        APP_NAME = ninjaProp.getOrDie("application.name");
        APP_HOME = ninjaProp.getOrDie("application.url");
        APP_BASEPATH = ninjaProp.getOrDie("application.basedir");
        APP_LANGS = ninjaProp.getStringArray("application.languages");
        APP_DEFAULT_ENTRYNO = ninjaProp.getIntegerWithDefault("application.default.entriesperpage", 15);
        APP_WHITELIST = ninjaProp.getBooleanOrDie("application.whitelist");
        ADMIN_ADDRESS = ninjaProp.getOrDie("mbox.adminaddr");
        ADMIN_PASSWORD = ninjaProp.getOrDie("admin.pass");
        CONFIRMATION_PERIOD = ninjaProp.getIntegerOrDie("confirm.period");
        COOKIE_PREFIX = ninjaProp.getOrDie("application.cookie.prefix");
        COOKIE_EXPIRETIME = ninjaProp.getIntegerOrDie("application.session.expire_time_in_seconds");
        D_LIST = ninjaProp.getOrDie("mbox.dlist");
        DOMAIN_LIST = ninjaProp.getStringArray("mbox.dlist");
        MB_PORT = ninjaProp.getIntegerOrDie("mbox.port");
        MB_HOST = ninjaProp.getOrDie("mbox.host");
        MB_INTERVAL = ninjaProp.getIntegerOrDie("mbox.interval");
        MTX_LIMIT = ninjaProp.getIntegerWithDefault("mailtransaction.displaylimit", 0);
        MTX_MAX_AGE = ninjaProp.getIntegerWithDefault("mailtransaction.maxage", -1);
        MEMCA_HOST = ninjaProp.getOrDie("memcached.host");
        MSG_REWRITE = ninjaProp.getBooleanWithDefault("mail.msg.rewrite", false);
        OUT_SMTP_HOST = ninjaProp.getOrDie("mail.smtp.host");
        OUT_SMTP_PORT = ninjaProp.getIntegerOrDie("mail.smtp.port");
        OUT_SMTP_USER = ninjaProp.getOrDie("mail.smtp.user");
        OUT_SMTP_PASS = ninjaProp.getOrDie("mail.smtp.pass");
        OUT_SMTP_AUTH = ninjaProp.getBooleanOrDie("mail.smtp.auth");
        OUT_SMTP_TLS = ninjaProp.getBooleanOrDie("mail.smtp.tls");
        OUT_SMTP_DEBUG = ninjaProp.getBooleanWithDefault("mail.smtp.debug", true);
        PW_LENGTH = ninjaProp.getIntegerOrDie("pw.length");
        SESSION_EXPIRETIME = COOKIE_EXPIRETIME + "s";
        if (DOMAIN_LIST == null)
        {
            throw new RuntimeException("Key mbox.dlist does not exist. Please include it in your application.conf. "
                                       + "Otherwise this app will not work");
        }
    }
}
