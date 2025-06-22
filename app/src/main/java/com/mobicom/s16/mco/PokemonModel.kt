package com.mobicom.s16.mco

class PokemonModel(name: String, imageId: Int) {
    var name = name
        private set
    var imageId = imageId
        private set

    override fun toString(): String {
        return "com.mobdeve.lecturematerial_03_recyclerview.fakeadex.models.PokemonModel{" +
                "name='" + name + '\'' +
                ", imageId=" + imageId +
                '}'
    }
}