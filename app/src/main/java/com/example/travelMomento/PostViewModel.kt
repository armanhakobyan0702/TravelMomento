package com.example.travelMomento

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.lifecycle.*
import com.example.travelMomento.data.Item
import com.example.travelMomento.data.ItemDao
import kotlinx.coroutines.launch
import java.util.*

/**
 * View Model to keep a reference to the TravelMomento repository and an up-to-date list of all items.
 *
 */
class PostViewModel(
    private val application: TravelMomentoApplication,
    private val itemDao: ItemDao
) : AndroidViewModel(application) {

    // Cache all items form the database using LiveData.
    val allItems: LiveData<List<Item>> = itemDao.getItems().asLiveData()

    /**
     * Updates an existing Item in the database.
     */
    fun updateItem(
        itemId: Int,
        itemTitle: String,
        itemContent: String,
        itemImage: Bitmap?,
        itemCreationDate: Date?
    ) {
        val updatedItem = getUpdatedItemEntry(itemId, itemTitle, itemContent, itemImage, itemCreationDate)
        updateItem(updatedItem)
    }


    /**
     * Launching a new coroutine to update an item in a non-blocking way
     */
    private fun updateItem(item: Item) {
        viewModelScope.launch {
            itemDao.update(item)
        }
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    /**
     * Inserts the new Item into database.
     */
    fun addNewItem(itemTitle: String, itemContent: String, imageUri: Uri?) {
        val newItem = getNewItemEntry(itemTitle, itemContent, imageUri)
        insertItem(newItem)
    }

    /**
     * Launching a new coroutine to insert an item in a non-blocking way
     */
    private fun insertItem(item: Item) {
        viewModelScope.launch {
            itemDao.insert(item)
        }
    }

    /**
     * Launching a new coroutine to delete an item in a non-blocking way
     */
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            itemDao.delete(item)
        }
    }

    /**
     * Retrieve an item from the repository.
     */
    fun retrieveItem(id: Int): LiveData<Item> {
        return itemDao.getItem(id).asLiveData()
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    fun isEntryValid(itemTitle: String, itemContent: String): Boolean {
        if (itemTitle.isBlank() || itemContent.isBlank()) {
            return false
        }
        return true
    }

    fun getBitmapFromUri(uri: Uri?): Bitmap? {
        return uri?.let {
            val source = ImageDecoder.createSource(application.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    /**
     * Returns an instance of the [Item] entity class with the item info entered by the user.
     * This will be used to add a new entry to the TravelMomento database.
     */
    private fun getNewItemEntry(itemTitle: String, itemContent: String, imageUri: Uri?): Item {
        val currentDate = Date()
        val imageBitmap = imageUri?.let {
            val source = ImageDecoder.createSource(application.contentResolver, imageUri)
            ImageDecoder.decodeBitmap(source)
        }

        return Item(
            itemTitle = itemTitle,
            itemContent = itemContent,
            itemCreatedDate = currentDate,
            itemModifiedDate = currentDate,
            itemImage = imageBitmap
        )
    }

    /**
     * Called to update an existing entry in the TravelMomento database.
     * Returns an instance of the [Item] entity class with the item info updated by the user.
     */
    private fun getUpdatedItemEntry(
        itemId: Int,
        itemTitle: String,
        itemContent: String,
        itemImage: Bitmap?,
        itemCreationDate: Date?
    ): Item {
        val currentDate = Date()

        return Item(
            id = itemId,
            itemTitle = itemTitle,
            itemContent = itemContent,
            itemImage = itemImage,
            itemCreatedDate = itemCreationDate,
            itemModifiedDate = currentDate
        )
    }
}

/**
 * Factory class to instantiate the [ViewModel] instance.
 */
class PostViewModelFactory(
    private val application: TravelMomentoApplication,
    private val itemDao: ItemDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(application, itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

