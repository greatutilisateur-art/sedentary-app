package org.sedentaryapp.data

import android.content.Context
import org.json.JSONObject
import org.sedentaryapp.engine.model.ActivityDefinition

data class CatalogActivity(
    val definition: ActivityDefinition,
    val title: String,
    val instruction: String,
    val preferredPosture: String,
    val compatiblePostures: Set<String>,
)

class CatalogRepository(
    private val context: Context,
) {

    fun loadActivities(): List<CatalogActivity> {
        val json = context.assets
            .open("activities_v1.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val activitiesJson = root.getJSONArray("activities")

        return buildList {
            for (index in 0 until activitiesJson.length()) {
                val item = activitiesJson.getJSONObject(index)

                val opportunityTypesJson =
                    item.getJSONArray("eligible_opportunity_types")

                val opportunityTypes = buildSet {
                    for (i in 0 until opportunityTypesJson.length()) {
                        add(opportunityTypesJson.getString(i))
                    }
                }

                val posturesJson =
                    item.getJSONArray("compatible_postures")

                val compatiblePostures = buildSet {
                    for (i in 0 until posturesJson.length()) {
                        add(posturesJson.getString(i))
                    }
                }

                add(
                    CatalogActivity(
                        definition = ActivityDefinition(
                            id = item.getString("id"),
                            status = item.getString("status"),
                            userCategory = item.getString("user_category"),
                            internalFamily = item.getString("internal_family"),
                            intensity = item.getString("intensity"),
                            durationSecondsMin =
                                item.getInt("duration_seconds_min"),
                            durationSecondsMax =
                                item.getInt("duration_seconds_max"),
                            standingRequired =
                                item.getBoolean("standing_required"),
                            walkingRequired =
                                item.getBoolean("walking_required"),
                            baseWeight =
                                item.getDouble("base_weight"),
                        ),
                        title = item.getString("title"),
                        instruction = item.getString("instruction"),
                        preferredPosture =
                            item.getString("preferred_posture"),
                        compatiblePostures =
                            compatiblePostures,
                    )
                )
            }
        }
    }
}