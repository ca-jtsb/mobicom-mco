package com.mobicom.s16.mco.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHandler (context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)  {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PokemonCardDatabase"
        const val CARD_TABLE = "card_table"

        const val ID = "id"
        const val CARD_IMG = "card_img"
        const val CARD_ID = "card_id"
        const val CARD_TITLE = "card_name"
        const val CARD_SET_ID = "card_set_id"
        const val CARD_SET_NAME = "card_set_name"
        const val CARD_NUMBER = "card_number"
        const val CARD_RARITY = "card_rarity"
        const val CARD_TYPE = "card_type"
        const val CARD_HP = "card_hp"
        const val CARD_STAGE = "card_stage"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_MEDIA_TABLE = "CREATE TABLE IF NOT EXISTS " + CARD_TABLE + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CARD_IMG + " TEXT, " +
                CARD_ID + " TEXT, " +
                CARD_TITLE + " TEXT, " +
                CARD_SET_ID + " TEXT, " +
                CARD_SET_NAME + " TEXT, " +
                CARD_NUMBER + " TEXT, " +
                CARD_RARITY + " TEXT, " +
                CARD_TYPE + " TEXT, " +
                CARD_HP + " INTEGER, " +
                CARD_STAGE + " TEXT)"
        db?.execSQL(CREATE_MEDIA_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $CARD_TABLE")
        onCreate(db)
    }
}