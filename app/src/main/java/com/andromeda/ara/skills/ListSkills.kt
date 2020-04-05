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

import com.andromeda.ara.constants.ServerUrl
import com.andromeda.ara.constants.User
import com.andromeda.ara.util.JsonParse
import com.andromeda.ara.util.FeedModel
import java.net.URL

class ListSkills {
    fun main(): ArrayList<FeedModel> {
        val toReturn = arrayListOf<FeedModel>()
        val skillsServerData = JsonParse().skillsServer(URL(ServerUrl.url + "user/" + User.id).readText())
        skillsServerData.forEach{
            toReturn.add(FeedModel("", it.index, it.name, "", "", false))
        }
        return toReturn
    }
}