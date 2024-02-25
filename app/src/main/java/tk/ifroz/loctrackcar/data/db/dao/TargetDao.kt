package tk.ifroz.loctrackcar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import tk.ifroz.loctrackcar.data.db.entity.Target

@ExperimentalCoroutinesApi
@Dao
interface TargetDao {

    @Query("SELECT latitude, longitude FROM marker_car_target_table")
    fun getTarget(): Flow<Target?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(target: Target)

    @Query("DELETE FROM marker_car_target_table")
    suspend fun delete()
}