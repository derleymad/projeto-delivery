package com.derleymad.docegelato.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

private var auth = FirebaseAuth.getInstance()
private var currentUser = auth.currentUser

fun getData(){
    val db = Firebase.firestore
}
//
//object FirebaseUtils {
//
//    private var auth = FirebaseAuth.getInstance()
//    fun getFirebaseLocation() : ArrayList<Pedidos>{
//        val db = Firebase.firestore
//        val data = ArrayList<Pedidos>()
//        db.collection("users")
//            .document("clientes")
//            .collection(auth.currentUser?.uid.toString())
//            .document("pedidos")
//            .collection("recente")
//            .get()
//            .addOnSuccessListener{
//                data.clear()
//                for(i in it.documents){
//                    data.add(i.toObject(Pedidos::class.java)!!)
//                }
////                pedidoViewModel.loadingProgressBar.value = false
////                adapter.setPedidoFeitoList(data)
//            }
//            .addOnFailureListener {
//                data.clear()
//            }
//        return data
//    }
//}