package tk.ifroz.loctrackcar.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tk.ifroz.loctrackcar.db.entity.Target

@Dao
interface TargetDao {

    @Query("SELECT latitude, longitude FROM marker_car_target_table")
    fun getTarget(): LiveData<Target>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(target: Target)

    @Query("DELETE FROM marker_car_target_table")
    fun delete()
}