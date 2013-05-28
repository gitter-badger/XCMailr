/**
 * Copyright (C) 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package conf;

import ninja.AssetsController;
import ninja.Router;
import ninja.application.ApplicationRoutes;
import controllers.AdminHandler;
import controllers.Application;
import controllers.BoxHandler;
import controllers.UserHandler;

public class Routes implements ApplicationRoutes
{

    @Override
    public void init(Router router)
    {

        router.GET().route("/").with(Application.class, "index");

        router.GET().route("/register").with(Application.class, "registerForm");
        router.POST().route("/register").with(Application.class, "postRegisterForm");

        router.GET().route("/login").with(Application.class, "loginForm");
        router.POST().route("/login").with(Application.class, "loggedInForm");

        router.GET().route("/pwresend").with(Application.class, "forgotPwForm");
        router.POST().route("/pwresend").with(Application.class, "pwResend");
        router.GET().route("/lostpw/{id}/{token}").with(Application.class, "lostPw");
        router.POST().route("/lostpw/{id}/{token}").with(Application.class, "changePw"); 
        router.GET().route("/verify/{id}/{token}").with(Application.class, "verifyActivation");

        router.GET().route("/logout").with(Application.class, "logout");

        router.GET().route("/user/edit").with(UserHandler.class, "editUserForm");
        router.POST().route("/user/edit").with(UserHandler.class, "editUser");

        router.GET().route("/mail").with(BoxHandler.class, "showBoxes");
        router.POST().route("/mail").with(BoxHandler.class, "showBoxes");
        router.GET().route("/mail/add").with(BoxHandler.class, "showAddBox");
        router.POST().route("/mail/add").with(BoxHandler.class, "addBox");
        router.POST().route("/mail/expire/{id}").with(BoxHandler.class, "expireBox");
        router.POST().route("/mail/delete/{id}").with(BoxHandler.class, "deleteBox");
        router.POST().route("/mail/reset/{id}").with(BoxHandler.class, "resetBoxCounters");
        router.GET().route("/mail/edit/{id}").with(BoxHandler.class, "showEditBox");
        router.POST().route("/mail/edit/{id}").with(BoxHandler.class, "editBox");


        router.POST().route("/admin/promote/{id}").with(AdminHandler.class, "promote");
        router.POST().route("/admin/activate/{id}").with(AdminHandler.class, "activate");
        router.POST().route("/admin/delete/{id}").with(AdminHandler.class, "deleteUser");
        router.GET().route("/admin").with(AdminHandler.class, "showAdmin");
        router.GET().route("/admin/users").with(AdminHandler.class, "showUsers");
        router.POST().route("/admin/users").with(AdminHandler.class, "showUsers");
        router.GET().route("/admin/summedtx").with(AdminHandler.class, "showSumTx");
        router.GET().route("/admin/mtxs").with(AdminHandler.class, "pagedMTX");
        router.POST().route("/admin/mtxs").with(AdminHandler.class, "pagedMTX");
        
        router.GET().route("/assets/.*").with(AssetsController.class, "serve");

    }

}