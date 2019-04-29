package tk.ifroz.loctrackcar.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import tk.ifroz.loctrackcar.db.entity.Description

@Dao
interface DescriptionDao {

    @Query("SELECT description FROM marker_car_description_table")
    fun getDescription(): LiveData<Description>

    @Insert
    fun insert(description: Description)

    @Query("DELETE FROM marker_car_description_table")
    fun delete()
}