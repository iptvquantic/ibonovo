package com.meuapp.iptvplayer.helper

import android.content.Context
import android.content.SharedPreferences
import com.meuapp.iptvplayer.apps.Constants

object PreferenceHelper {
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    fun setServerUrl(ctx: Context, url: String) = prefs(ctx).edit().putString(Constants.KEY_SERVER_URL, url).apply()
    fun getServerUrl(ctx: Context): String = prefs(ctx).getString(Constants.KEY_SERVER_URL, "") ?: ""

    fun setUsername(ctx: Context, v: String) = prefs(ctx).edit().putString(Constants.KEY_USERNAME, v).apply()
    fun getUsername(ctx: Context): String = prefs(ctx).getString(Constants.KEY_USERNAME, "") ?: ""

    fun setPassword(ctx: Context, v: String) = prefs(ctx).edit().putString(Constants.KEY_PASSWORD, v).apply()
    fun getPassword(ctx: Context): String = prefs(ctx).getString(Constants.KEY_PASSWORD, "") ?: ""

    fun setPlaylistName(ctx: Context, v: String) = prefs(ctx).edit().putString(Constants.KEY_PLAYLIST_NAME, v).apply()
    fun getPlaylistName(ctx: Context): String = prefs(ctx).getString(Constants.KEY_PLAYLIST_NAME, "Minha Lista") ?: "Minha Lista"

    fun isLoggedIn(ctx: Context): Boolean = getServerUrl(ctx).isNotEmpty() && getUsername(ctx).isNotEmpty()

    fun clearAll(ctx: Context) = prefs(ctx).edit().clear().apply()

    fun buildPlayerApiUrl(ctx: Context): String {
        val base = getServerUrl(ctx).trimEnd('/')
        return "$base/player_api.php"
    }
    fun buildStreamUrl(ctx: Context, streamId: Int, ext: String = "ts"): String {
        val base = getServerUrl(ctx).trimEnd('/')
        val u = getUsername(ctx); val p = getPassword(ctx)
        return "$base/live/$u/$p/$streamId.$ext"
    }
    fun buildMovieUrl(ctx: Context, streamId: Int, ext: String = "mp4"): String {
        val base = getServerUrl(ctx).trimEnd('/')
        val u = getUsername(ctx); val p = getPassword(ctx)
        return "$base/movie/$u/$p/$streamId.$ext"
    }
    fun buildEpisodeUrl(ctx: Context, streamId: String, ext: String = "mp4"): String {
        val base = getServerUrl(ctx).trimEnd('/')
        val u = getUsername(ctx); val p = getPassword(ctx)
        return "$base/series/$u/$p/$streamId.$ext"
    }
}
