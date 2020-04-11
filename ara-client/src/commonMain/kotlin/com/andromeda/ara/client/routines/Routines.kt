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

package com.andromeda.ara.client.routines

import com.andromeda.ara.client.models.SkillsDBModel
import com.andromeda.ara.client.util.JsonParse
import com.andromeda.ara.client.util.ReadURL
import com.andromeda.ara.client.util.ServerUrl
import com.andromeda.ara.client.util.ServerUrl.url
import com.andromeda.ara.client.util.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ImplicitReflectionSerializer

class Routines {
    fun get(id:String){}
    @ImplicitReflectionSerializer
    suspend fun get(): ArrayList<SkillsDBModel> {
        val url = ServerUrl.url + "user/" + User.id
        val data = ReadURL().get(url)
        return JsonParse().any(data) as ArrayList<SkillsDBModel>

    }
    fun rename(id:String, name:String){
        val url = "${url}updateuserdata/user=${User.id}id=$id&prop=name&newval=${name}"
        GlobalScope.launch {
            ReadURL().get(url)
        }
    }
    fun new(){}
    fun delete(){}
}