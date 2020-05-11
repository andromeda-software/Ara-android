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

package com.andromeda.ara.client.reminders

import com.andromeda.ara.client.models.FeedModel
import com.andromeda.ara.client.models.RemindersModel
import com.andromeda.ara.client.util.*
import com.andromeda.ara.client.util.ServerUrl.url
import kotlinx.serialization.ImplicitReflectionSerializer

class Reminders {
    @ImplicitReflectionSerializer
    suspend fun get(id:String): RemindersModel {
        val reminderUrl = ServerUrl.getReminder(id)
        return JsonParse().reminder(ReadURL().get(reminderUrl))
    }
    @ImplicitReflectionSerializer
    suspend fun get(): ArrayList<FeedModel> {
        val remindersList = ServerUrl.getRemindersList("bmfbmfdlkbfldkng", "", "", "")
        println(remindersList)
        return ApiOutToFeed().main(JsonParse().outputModel(ReadURL().get(remindersList)))
    }
    suspend fun new(remindersModel: RemindersModel){
        val replace =
            "$url/remindernn/name=${remindersModel.header}&user=${User.id}&time=${remindersModel.time}&info=${remindersModel.body}".replace(
                " ",
                "%20"
            )
        println(replace)
        ReadURL().get(replace)
    }
    suspend fun delete(id: String){
        val replace =
            "$url/reminderd/user=${User.id}&id=$id".replace(
                " ",
                "%20"
            )
        ReadURL().get(replace)

    }

    suspend fun set(id: String, remindersModel: RemindersModel){
        val replace =
            "$url/remindere/name=$remindersModel&user=${User.id}&time=${remindersModel.time}&info=${remindersModel.body}".replace(
                " ",
                "%20"
            )
        ReadURL().get(replace)
    }
}