package com.banglalink.toffee.showcase_view

import android.content.Context

class PrefsManager(private var context: Context?, showcaseID: String?) {
    private var showcaseID: String? = null
    
    /***
     * METHODS FOR INDIVIDUAL SHOWCASE VIEWS
     */
    fun hasFired(): Boolean {
        val status = sequenceStatus
        return status == SEQUENCE_FINISHED
    }
    
    fun setFired() {
        sequenceStatus = SEQUENCE_FINISHED
    }
    
    /***
     * METHODS FOR SHOWCASE SEQUENCES
     */
    var sequenceStatus: Int
        get() = context
            ?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.getInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED)!!
        set(status) {
            val internal = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            internal.edit().putInt(STATUS + showcaseID, status).apply()
        }
    
    fun resetShowcase() {
        resetShowcase(context, showcaseID)
    }
    
    fun close() {
        context = null
    }
    
    companion object {
        var SEQUENCE_NEVER_STARTED = 0
        var SEQUENCE_FINISHED = -1
        private const val PREFS_NAME = "material_showcaseview_prefs"
        private const val STATUS = "status_"
        fun resetShowcase(context: Context?, showcaseID: String?) {
            val internal = context!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            internal.edit().putInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED).apply()
        }
        
        fun resetAll(context: Context) {
            val internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            internal.edit().clear().apply()
        }
    }
    
    init {
        this.showcaseID = showcaseID
    }
}