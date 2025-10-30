package com.aseel.pos.data.migrations

import androidx.room.migration.Migration

val MIGRATION_2_3 = Migration(2, 3) { database ->
    // Add stockImpactJson column to transactions table
    database.execSQL("ALTER TABLE transactions ADD COLUMN stockImpactJson TEXT NOT NULL DEFAULT ''")
}