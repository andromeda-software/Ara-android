package com.andromeda.ara.client.models

data class Feed(val type:String, val action:ArrayList<SkillsModel>?, val voice:String?, var feed:ArrayList<FeedModel>)
