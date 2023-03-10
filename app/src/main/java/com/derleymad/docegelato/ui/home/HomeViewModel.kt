package com.derleymad.docegelato.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.derleymad.docegelato.model.categorias.*
import com.derleymad.docegelato.model.pedidos.Pedido
import com.derleymad.docegelato.model.pedidos.Pedidos
import com.derleymad.docegelato.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HomeViewModel : ViewModel() {
    private var _categoriaLiveData = MutableLiveData<Categorias>()
    private var _nomedaruaLiveData = MutableLiveData<String>()
    private var _destaquesLiveData = MutableLiveData<ArrayList<Comida>>()
    private var _idLiveData = MutableLiveData<Int>()
    private var _nameLiveData = MutableLiveData<String>()
    var isAdmin = MutableLiveData(false)
    private var _quantityLiveData = MutableLiveData(1)
    private var _isPedidoFeitoLiveData = MutableLiveData<Boolean>(false)
    private var _listPedidoFeitoLiveData = MutableLiveData<Pedidos>()
    private var _obsLiveData = MutableLiveData<String>()
    private var _precoTotalLiveData = MutableLiveData<Float>(0f)
    var user = MutableLiveData<User>()
    private var _addressLiveData = MutableLiveData<Address>()
    private var addressLiveData = _addressLiveData
    var isLoadingContent = MutableLiveData(true)
    var precoAtual = MutableLiveData<Float?>()
    val comida_preco_tamanho = MutableLiveData<HashMap<String,Any>?>(null)
    val totalAdicionais = MutableLiveData(0)
    var adiconais = MutableLiveData<ArrayList<String>?>(null)


    var auth: FirebaseAuth
    val listPedidoFeitoLiveData = _listPedidoFeitoLiveData
    val precoTotalLiveData = _precoTotalLiveData
    val db = Firebase.firestore

    val setOfids = mutableSetOf<Int>()

    init {
        adiconais.value = ArrayList<String>()
        listPedidoFeitoLiveData.value = Pedidos(mutableListOf<Pedido>(), null, null, null,0f)
        auth = FirebaseAuth.getInstance()
        addressLiveData.value = Address()
        user.value = User(
            nome = auth.currentUser?.displayName.toString(),
            imagemPerfil = auth.currentUser?.photoUrl.toString(),
            uid = auth.currentUser?.uid.toString(),
            numero_celular = auth.currentUser?.phoneNumber.toString()
        )
    }

    fun getCategorias() {
        viewModelScope.launch {
            RetrofitInstance.api.getNovaCategorias().enqueue(object : Callback<Categorias> {
                override fun onResponse(call: Call<Categorias>, response: Response<Categorias>) {
                    if (response.body() != null) {
                        isLoadingContent.value = false
                        categoriaLiveData.value = response.body()
                        Log.i("qualovalor",categoriaLiveData.value?.last().toString())
                    } else {
                        return
                    }
                }
                override fun onFailure(call: Call<Categorias>, t: Throwable) {
                    Log.e("TAGAPI", t.message.toString())
                }
            })
        }
    }

    fun removeComidaDosPedidos(pedido: Pedido) {
        // ATUALIZA O PRECO TOTAL
        // REMOVE O PRODUTO DO LIVEDATA
        listPedidoFeitoLiveData.value?.pedidos?.remove(pedido)

        if(pedido.comida_tamanho_preco!=null){
            precoTotalLiveData.value = precoTotalLiveData.value?.minus(pedido.comida_tamanho_preco.values
                .first().toString().toFloat().times(pedido.quantity))
        }else{
            precoTotalLiveData.value =
                precoTotalLiveData.value?.minus(pedido.comida_preco?.times(pedido.quantity) ?: 0f)
        }
        listPedidoFeitoLiveData.value?.preco_total = precoTotalLiveData.value

        //SE POR ACASO APAGAR TUDO E NAO TIVER NENHUM PEDIDO
        if (listPedidoFeitoLiveData.value?.pedidos?.isEmpty() == true) {
            isPedidoFeitoLiveData.value = false
        }
    }

    fun diminuirQuantidade() {
        quantityLiveData.value = quantityLiveData.value?.minus(1)
    }

    fun aumentarQuantidade() {
        quantityLiveData.value = quantityLiveData.value?.plus(1)
    }

    fun destroyData(){
        obsLiveData.value = ""
        quantityLiveData.value = 1
        precoAtual.value = 0f
        totalAdicionais.value = 0
        idLiveData.value = null
        nameLiveData.value = null
        comida_preco_tamanho.value = null
        adiconais.value = ArrayList()
    }

    fun getUserFromFirebase(){
        db.collection("users")
            .document("clientes")
            .collection(auth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener{
            }
            .addOnFailureListener {
                Log.i("teste","Falha")
            }
    }

    fun setComidaToPedidos(comida: Comida, user: User, address: Address) {
        val currentDateTime = Calendar.getInstance()
        //GENERATE UNIQUE ID and ADD INTO SETS
        setOfids.add((0..1000).random())



        if(comida_preco_tamanho.value!=null){
            listPedidoFeitoLiveData.value?.preco_total = quantityLiveData.value?.times(
                comida_preco_tamanho.value!!.values.first().toString().toFloat())
            precoTotalLiveData.value = precoTotalLiveData.value!! + listPedidoFeitoLiveData.value?.preco_total!!
        }else{
            listPedidoFeitoLiveData.value?.preco_total = quantityLiveData.value?.times(comida.comida_preco!!)!!
            precoTotalLiveData.value = precoTotalLiveData.value!! + listPedidoFeitoLiveData.value?.preco_total!!
        }

        listPedidoFeitoLiveData.value?.address = address
        listPedidoFeitoLiveData.value?.user = user
        listPedidoFeitoLiveData.value?.date = currentDateTime.timeInMillis

        val pedido = Pedido(
            comida_desc = comida.comida_desc,
            comida_id = comida.comida_id,
            comida_unique_id = setOfids.last(),
            comida_preco = comida.comida_preco,
            comida_title = comida.comida_title,
            comida_tamanho_preco = comida_preco_tamanho.value,
            comida_desconto = comida.comida_desconto,
            image = comida.image,
            adicionais = adiconais.value,
            quantity = quantityLiveData.value ?: 0,
            obs = obsLiveData.value.toString()
        )

        listPedidoFeitoLiveData.value?.uid = auth.currentUser?.uid
        listPedidoFeitoLiveData.value?.pedidos?.add(pedido)
        listPedidoFeitoLiveData.value?.preco_total = precoTotalLiveData.value
    }

    fun getDestaques() {
        viewModelScope.launch {
            RetrofitInstance.api.getDestaques().enqueue(object : Callback<ArrayList<Comida>> {
                override fun onResponse(
                    call: Call<ArrayList<Comida>>,
                    response: Response<ArrayList<Comida>>
                ) {
                    if (response.body() != null) {
                        destaquesLiveData.value = response.body()
                        isLoadingContent.value = false
                    } else {
                        return
                    }
                }

                override fun onFailure(call: Call<ArrayList<Comida>>, t: Throwable) {
                }
            })
        }
    }

    fun clearPedidosAndPrices() {
        listPedidoFeitoLiveData.value?.pedidos?.clear()
        listPedidoFeitoLiveData.value?.preco_total = 0f
    }

    val nomedaruaLiveData = _nomedaruaLiveData
    val categoriaLiveData = _categoriaLiveData
    val destaquesLiveData = _destaquesLiveData
    val idLiveData = _idLiveData
    val nameLiveData = _nameLiveData
    val quantityLiveData = _quantityLiveData
    val isPedidoFeitoLiveData = _isPedidoFeitoLiveData
    val obsLiveData = _obsLiveData
    val address = _addressLiveData

}