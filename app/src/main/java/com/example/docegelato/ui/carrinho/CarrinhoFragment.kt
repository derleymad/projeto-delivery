package com.example.docegelato.ui.carrinho

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docegelato.R
import com.example.docegelato.databinding.FragmentCarrinhoBinding
import com.example.docegelato.ui.home.HomeViewModel
import com.example.docegelato.ui.home.adapters.PedidoAdapter
import com.example.docegelato.ui.pedido.PedidoViewModel
import com.example.docegelato.util.Utils
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CarrinhoFragment : Fragment() {

    private var _binding: FragmentCarrinhoBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapterPedidos : PedidoAdapter

    private val homeViewModel : HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarrinhoBinding.inflate(inflater, container, false)
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.INVISIBLE
        requireActivity().findViewById<CardView>(R.id.ln_carrinho_flutuante).visibility = View.INVISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareRecyclerView()
        startObservers()
        startEventClickListeners()
        clearBadges()
        val database= Firebase.database
        binding.btnFinalizarTudo.setOnClickListener {
            val myUid = homeViewModel.auth.currentUser?.uid!!.toString()
            val myRefUsers = database.getReference("users")
            val empId = myRefUsers.push().key!!
            myRefUsers.child(myUid).child(empId).setValue(homeViewModel.listPedidoFeitoLiveData.value)
            homeViewModel.isPedidoFeitoLiveData.value = false
            homeViewModel.clearPedidosAndPrices()
        }
    }

    private fun clearBadges() {
        val navBar  = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navBar.removeBadge(R.id.navigation_carrinho)
    }

    private fun startEventClickListeners(){
        binding.btnBackToBaseFragment.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun prepareRecyclerView() {
        adapterPedidos = PedidoAdapter{
            homeViewModel.removeComidaDosPedidos(it)
            adapterPedidos.removePedidoAndUpdateRecyclerView(it)
        }
        binding.rvPedidoFeito.adapter = adapterPedidos
        binding.rvPedidoFeito.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun startObservers(){
        homeViewModel.precoTotalLiveData.observe(viewLifecycleOwner){
            binding.tvTotalPedido.text = Utils().format(it)
        }
        homeViewModel.listPedidoFeitoLiveData.observe(viewLifecycleOwner){
            adapterPedidos.addPedidoToRecyclerViewList(it.pedidos!!)
        }
        homeViewModel.isPedidoFeitoLiveData.observe(viewLifecycleOwner){ it ->
            binding.apply {
                if (it){
                    lnImgCenter.visibility = View.INVISIBLE
                    lnBottomFinalizar.visibility = View.VISIBLE
                    rvPedidoFeito.visibility = View.VISIBLE
                }else{
                    lnImgCenter.visibility = View.VISIBLE
                    lnBottomFinalizar.visibility = View.INVISIBLE
                    rvPedidoFeito.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        requireActivity().findViewById<CardView>(R.id.ln_carrinho_flutuante).visibility = if(homeViewModel.isPedidoFeitoLiveData.value==true) View.VISIBLE else View.INVISIBLE
        _binding = null
    }
}