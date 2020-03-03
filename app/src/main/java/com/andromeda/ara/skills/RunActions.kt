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

package com.andromeda.ara.skills

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.andromeda.ara.services.AraActions
import com.andromeda.ara.util.RssFeedModel
import com.andromeda.ara.util.SkillsModel
import java.util.*


class RunActions {
    fun doIt(yaml: ArrayList<SkillsModel>?, searchTerm: String, ctx: Context, act: Activity, searchFunctions: SearchFunctions): ArrayList<RssFeedModel> {
        val returnedVal = ArrayList<RssFeedModel>()

        var arg1: String
        var arg2:String
        if (yaml != null) {
            for (i in yaml) {
                when (i.action) {
                    "OPEN_APP" -> {
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        OpenApp().openApp(arg1, ctx)
                    }
                    "CALL" -> {
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        Phone().call(arg1, ctx, act)

                    }
                    "TEXT" -> {
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        arg2 = if (i.arg2 == "INFO") respond(searchFunctions, "cxf")
                        else i.arg2
                        Text().sendText(arg1, arg2, ctx)
                    }
                    "TOG_MEDIA" -> {
                        Media().playPause(ctx)
                    }
                    "STOPTIMERS" -> {
                        AraActions.cancel()
                    }
                    "OUTPUT" -> {
                        returnedVal.add(RssFeedModel(i.arg2, "", i.arg1, "", "", true))
                    }
                    "SITE" -> {
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(arg1))
                        act.startActivity(browserIntent)
                    }
                    "MAPS" -> {
                        val uri = java.lang.String.format(Locale.ENGLISH, "geo:%f,%f", i.arg1.toInt(), i.arg2.toInt())
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        ctx.startActivity(intent)
                    }
                    "FLASH" -> {
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        if (arg1.equals("on", true)) FlashLight().on()
                        else FlashLight().off()
                    }
                    "TIMER" -> {
                        try{
                        arg1 = if (i.arg1 == "TERM") searchTerm
                        else i.arg1
                        val i = Intent(ctx, AraActions::class.java)
                        i.putExtra("type", 1)
                        i.putExtra("length", arg1.toInt())
                        act.startService(i)
                        }
                        catch (e:Exception){
                            e.printStackTrace()
                            returnedVal.add(RssFeedModel("Failed to start timer", "", "","", "", true))
                        }

                    }
                    "RESPOND" ->{
                        searchFunctions.callBack(i.arg1, i.arg2)
                    }
                }
            }
        }

        return returnedVal
    }
    fun respond(searchFunctions: SearchFunctions, m:String): String {
        return searchFunctions.callForString(m)
    }
}