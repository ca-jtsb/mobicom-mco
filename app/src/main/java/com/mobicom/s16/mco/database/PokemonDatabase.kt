package com.mobicom.s16.mco.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.mobicom.s16.mco.model.PokemonModel
import kotlin.text.insert

class PokemonDatabase (context: Context) {
    private var databaseHandler : DatabaseHandler = DatabaseHandler(context)

    fun addCard(card: PokemonModel){
        TODO("Provide code")
    }

    fun deleteCard(card: PokemonModel){
        TODO("Provide code")
    }

    fun getCardList(): ArrayList<PokemonModel>{
        val result = ArrayList<PokemonModel>()

        val db: SQLiteDatabase = databaseHandler.readableDatabase
        val c: Cursor = db.rawQuery("SELECT * FROM ${DatabaseHandler.CARD_TABLE}", null)

        while(c.moveToNext()){
            result.add(PokemonModel(
                TODO("insert parameters"),
                TODO("insert parameters")
            ))
        }
        c.close()
        db.close()


        return result
    }
}