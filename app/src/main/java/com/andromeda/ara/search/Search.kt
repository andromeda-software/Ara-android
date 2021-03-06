


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

package com.andromeda.ara.search

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.andromeda.ara.R
import com.andromeda.ara.client.models.FeedModel
import com.andromeda.ara.client.search.Actions
import com.andromeda.ara.client.search.SearchAra
import com.andromeda.ara.client.util.ServerUrl
import com.andromeda.ara.models.SkillsFromDB
import com.andromeda.ara.models.TabModel
import com.andromeda.ara.skills.Parse
import com.andromeda.ara.skills.RunActions
import com.andromeda.ara.skills.SearchFunctions
import com.andromeda.ara.util.ApiOutputToRssFeed
import com.andromeda.ara.util.JsonParse
import com.andromeda.ara.util.SetFeedData
import com.andromeda.ara.voice.TTS
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class Search {
fun main(mainval: String, act: Activity, searchFunctions: SearchFunctions, tts: TTS?, outputList: ArrayList<FeedModel>, actions: Actions, setFeedData: SetFeedData): ArrayList<FeedModel> {

        var done2 = false
        var lat = 0.0
        var log = 0.0
        outputList.clear()
        val locationManager = act.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location: Location? = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                lat = location.latitude
                log = location.longitude
            }
        }

                    val launch = GlobalScope.launch {
                            val feedData = SearchAra().search(
                                lat.toString(),
                                log.toString(),
                                mainval,
                                Locale.getDefault().country,
                                actions
                            )
                        act.runOnUiThread {
				tts?.start(act, feedData.voice)
                            val elements = TabModel("ara", ServerUrl.getStandardSearch(mainval, log.toString(), lat.toString(), Locale.getDefault().country))
                            val web = TabModel("web", ServerUrl.getWebSearch(mainval, log.toString(), lat.toString(), Locale.getDefault().country))
                            val image = TabModel("images", ServerUrl.getImageSearch(mainval, log.toString(), lat.toString(), Locale.getDefault().country))
                            val news = TabModel("news", ServerUrl.getNewsSearch(mainval, log.toString(), lat.toString(), Locale.getDefault().country))
                            val video = TabModel("video", ServerUrl.getVideoSearch(mainval, log.toString(), lat.toString(), Locale.getDefault().country))
                            val data = arrayListOf(elements, web, image, news,video)
                            searchFunctions.addTabData(data)

                        }
			println(feedData.feed)
			setFeedData.setData(feedData)
                    }



        return outputList
    }

    fun outputPing(mainval: String, ctx: Context, act: Activity, searchFunctions: SearchFunctions): ArrayList<FeedModel> {

        var outputList: ArrayList<FeedModel> = ArrayList()


        outputList.add(FeedModel("", "", "", "", "", false))
        //com.andromeda.ara.client.search ara server
        var searchMode1 = mainval.toLowerCase(Locale.getDefault())
        searchMode1 = searchMode1.replace(" ", "%20")
        val test1 = AraSearch().arrayOfOutputModels2(searchMode1)
        outputList = ApiOutputToRssFeed().main(test1)
        println(R.string.done_search)
        try {
            val parsed = Parse().parse(test1?.get(0)?.exes)
            val doIt = RunActions().doIt(parsed, mainval, ctx, act, searchFunctions)
            outputList.addAll(doIt)
        } catch (e: Exception) {
        }


        return outputList
    }
    private fun getSearch(act: Activity): java.util.ArrayList<SkillsFromDB>? {
        val sharedPreferences = act.getPreferences(0)
        val url = URL("http://ara-server.azurewebsites.net/getforcache")
        val update = Thread{
            try {
                sharedPreferences.edit().putString("store", url.readText()).apply()
            }
            catch (e:Exception){
                e.printStackTrace()
            }
        }
        if (sharedPreferences.contains("store")){
            update.start()
            return JsonParse().skills(sharedPreferences.getString("store", ""))
        }
        update.start()
        update.join()
        return JsonParse().skills(sharedPreferences.getString("store", ""))

    }



}
