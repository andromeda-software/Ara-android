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

package com.andromeda.ara.feeds


import com.andromeda.ara.client.feeds.Food
import com.andromeda.ara.client.models.FeedModel
import com.andromeda.ara.constants.ServerUrl
import com.andromeda.ara.util.ApiOutputToRssFeed
import com.andromeda.ara.util.JsonParse
import java.net.URL
import java.util.*


class Food {
    suspend fun getFood(log: String, lat: String): ArrayList<FeedModel> {
        return Food().getAsFeed(log, lat)
    }

}
