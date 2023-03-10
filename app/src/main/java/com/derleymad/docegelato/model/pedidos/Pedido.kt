package com.derleymad.docegelato.model.pedidos

data class Pedido(
    val comida_desc: String? = "",
    val comida_id: Int = 0,
    val comida_unique_id: Int = 0,
    val comida_preco: Float? = 0f,
    val comida_title: String = "",
    val comida_tamanho_preco : HashMap<String,Any>? = null,
    val comida_desconto: Float = 0f,
    val image: String = "",
    val adicionais : ArrayList<String>? = null,
    var quantity: Int = 0,
    val obs: String = "Nehuma observação"
)
