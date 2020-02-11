/*
 * Copyright (c) 2020. Fulton Browne
 *  This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.andromeda.ara.util

import android.R
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.andromeda.ara.constants.User
import com.microsoft.appcenter.auth.Auth
import com.microsoft.appcenter.auth.SignInResult
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.nimbusds.jwt.JWTParser
import net.minidev.json.JSONArray


class LogIn {
    var mFirstAccount: IAccount? = null

    fun logIn(mPrefs: SharedPreferences, ctx: Context) {

        Auth.setEnabled(true)
        Auth.signIn().thenAccept { signInResult: SignInResult ->
            if (signInResult.exception == null) { // Sign-in succeeded.
                try {
                    val accountId = signInResult.userInformation.accountId
                    val idToken = signInResult.userInformation.idToken
                    println(accountId)
                    val parsedToken = JWTParser.parse(idToken)
                    val claims = parsedToken.jwtClaimsSet.claims
                    print("check if null")
                    val emails = claims["emails"] as JSONArray?
                    val displayName = claims["given_name"] as String?
                    mPrefs.edit().putString("name", displayName).apply()
                    print(displayName)
                    if (emails != null && !emails.isEmpty()) {
                        User.name =displayName!!
                        User.id = accountId
                        User.email = emails[0].toString()
                        val firstEmail = emails[0].toString()
                        mPrefs.edit().putString("email", firstEmail).apply()
                        print(firstEmail)
                    } else print("emails null")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                signInResult.exception.printStackTrace()

            }
        }
    }
    fun logIn(act:Activity){
        val scopes = arrayOf("User.Read")
        var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null

        PublicClientApplication.createMultipleAccountPublicClientApplication(act,
                R.raw.msal_config,
                object : IMultipleAccountApplicationCreatedListener {
                    override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                        mMultipleAccountApp = application
                    }

                    override fun onError(exception: MsalException) { //Log Exception Here
                    }
                })
        mMultipleAccountApp?.acquireToken(act, scopes, this!!.getAuthInteractiveCallback()!!);
    }
    private fun getAuthInteractiveCallback(): AuthenticationCallback? {
        return object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) { /* Successfully got a token, use it to call a protected resource */
                val accessToken = authenticationResult.accessToken
                // Record account used to acquire token
                mFirstAccount = authenticationResult.account
            }

            override fun onError(exception: MsalException?) {
                if (exception is MsalClientException) { //And exception from the client (MSAL)
                } else if (exception is MsalServiceException) { //An exception from the server
                }
            }

            override fun onCancel() { /* User canceled the authentication */
            }
        }
    }
}