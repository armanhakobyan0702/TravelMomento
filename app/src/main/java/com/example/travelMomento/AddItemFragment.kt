package com.example.travelMomento

import android.content.Context.INPUT_METHOD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.travelMomento.data.Item
import com.example.travelMomento.databinding.FragmentAddItemBinding

/**
 * Fragment to add or update an item in the TravelMomento database.
 */
class AddItemFragment : Fragment() {

    // Use the 'by activityViewModels()' Kotlin property delegate from the fragment-ktx artifact
    // to share the ViewModel across fragments.
    private val viewModel: PostViewModel by activityViewModels {
        PostViewModelFactory(
            activity?.application as TravelMomentoApplication,
            (activity?.application as TravelMomentoApplication).database
                .itemDao()
        )
    }

    private var selectedImageUri: Uri? = null
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.apply {
                    selectedImageUri = uri
                    itemImageView.setImageURI(uri)
                    itemImageView.visibility = View.VISIBLE
                    itemImageButton.visibility = View.INVISIBLE
                }
            }
        }

    lateinit var item: Item

    // Binding object instance corresponding to the fragment_add_item.xml layout
    // This property is non-null between the onCreateView() and onDestroyView() lifecycle callbacks,
    // when the view hierarchy is attached to the fragment
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemTitle.text.toString(),
            binding.itemContent.text.toString()
        )
    }

    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(item: Item) {
        binding.apply {
            itemTitle.setText(item.itemTitle, TextView.BufferType.SPANNABLE)
            itemContent.setText(item.itemContent, TextView.BufferType.SPANNABLE)

            item.itemImage?.let {
                itemImageView.setImageBitmap(null)
                itemImageView.setImageBitmap(item.itemImage)
                itemImageView.visibility = View.VISIBLE
                itemImageButton.visibility = View.INVISIBLE
            }

            itemImageButton.setOnClickListener {
                selectImageFromGalleryResult.launch("image/*")
            }

            saveAction.setOnClickListener { updateItem(item) }
        }
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemTitle.text.toString(),
                binding.itemContent.text.toString(),
                selectedImageUri
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Updates an existing Item in the database and navigates up to list fragment.
     */
    private fun updateItem(item: Item) {
        if (isEntryValid()) {
            val imageBitmap = selectedImageUri?.let {
                viewModel.getBitmapFromUri(it)
            } ?: item.itemImage

            viewModel.updateItem(
                this.navigationArgs.itemId,
                this.binding.itemTitle.text.toString(),
                this.binding.itemContent.text.toString(),
                imageBitmap,
                item.itemCreatedDate
            )
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    /**
     * Called when the view is created.
     * The itemId Navigation argument determines the edit item  or add new item.
     * If the itemId is positive, this method retrieves the information from the database and
     * allows the user to update it.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.itemId
        if (id > 0) { //EDIT
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else { //ADD
            binding.apply {
                itemImageButton.setOnClickListener {
                    selectImageFromGalleryResult.launch("image/*")
                }

                saveAction.setOnClickListener {
                    addNewItem()
                }
            }
        }
    }

    /**
     * Called before fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}
