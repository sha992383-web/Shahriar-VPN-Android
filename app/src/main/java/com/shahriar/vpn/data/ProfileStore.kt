package com.shahriar.vpn.data

import android.content.Context
import com.shahriar.vpn.core.Profile
import com.shahriar.vpn.util.Constants
import org.json.JSONArray
import org.json.JSONObject

/**
 * ⚡ SHAHRIAR VPN — local profile storage (SharedPreferences + JSON)
 */
object ProfileStore {

    fun loadAll(ctx: Context): MutableList<Profile> {
        val raw = prefs(ctx).getString(Constants.KEY_PROFILES, null) ?: return mutableListOf()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<Profile>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Profile(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        type = o.getString("type"),
                        address = o.getString("address"),
                        port = o.getInt("port"),
                        rawLink = o.optString("rawLink"),
                        details = o.getString("details")
                    )
                )
            }
            list
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun saveAll(ctx: Context, list: List<Profile>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("type", p.type)
                    .put("address", p.address)
                    .put("port", p.port)
                    .put("rawLink", p.rawLink)
                    .put("details", p.details)
            )
        }
        prefs(ctx).edit().putString(Constants.KEY_PROFILES, arr.toString()).apply()
    }

    fun add(ctx: Context, profile: Profile) {
        val list = loadAll(ctx)
        // Replace an identical entry if it already exists
        list.removeAll {
            it.address == profile.address && it.port == profile.port &&
                it.name == profile.name && it.type == profile.type
        }
        list.add(profile)
        saveAll(ctx, list)
    }

    fun remove(ctx: Context, id: String) {
        val list = loadAll(ctx)
        list.removeAll { it.id == id }
        saveAll(ctx, list)
        if (getSelectedId(ctx) == id) setSelectedId(ctx, null)
    }

    // ── Selected profile ─────────────────────────────

    fun getSelectedId(ctx: Context): String? =
        prefs(ctx).getString(Constants.KEY_SELECTED, null)

    fun setSelectedId(ctx: Context, id: String?) {
        prefs(ctx).edit().putString(Constants.KEY_SELECTED, id).apply()
    }

    fun getSelected(ctx: Context): Profile? {
        val id = getSelectedId(ctx) ?: return loadAll(ctx).firstOrNull()
        return loadAll(ctx).firstOrNull { it.id == id } ?: loadAll(ctx).firstOrNull()
    }

    // ── Settings ─────────────────────────────────────

    fun isBlockQuic(ctx: Context): Boolean =
        prefs(ctx).getBoolean(Constants.KEY_BLOCK_QUIC, true)

    fun setBlockQuic(ctx: Context, value: Boolean) {
        prefs(ctx).edit().putBoolean(Constants.KEY_BLOCK_QUIC, value).apply()
    }

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
}
