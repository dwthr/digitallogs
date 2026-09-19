package io.github.dwthr.digitallogs.logs.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.dwthr.digitallogs.logs.data.database.dao.CategoryDao
import io.github.dwthr.digitallogs.logs.data.database.dao.EntryDao
import io.github.dwthr.digitallogs.logs.data.database.dao.LogDao
import io.github.dwthr.digitallogs.logs.data.database.dao.TagDao
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryFtsEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.CategoryTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.EntryFtsEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogFtsEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.LogTagCrossRef
import io.github.dwthr.digitallogs.logs.data.database.entities.MediaFileReferenceEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.TagEntity
import io.github.dwthr.digitallogs.logs.data.database.entities.TagFtsEntity

@Database(
    entities = [
        CategoryEntity::class,
        LogEntity::class,
        TagEntity::class,
        CategoryTagCrossRef::class,
        LogTagCrossRef::class,
        LogFtsEntity::class,
        CategoryFtsEntity::class,
        TagFtsEntity::class,
        MediaFileReferenceEntity::class,
        EntryEntity::class,
        EntryFtsEntity::class,
    ],
    autoMigrations = [],
    exportSchema = true,
    version = 1 //TODO: Ensure automigrations work or redesign
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    companion object {
        const val DB_NAME = "logs.db"
    }

    abstract val categoryDao: CategoryDao
    abstract val logDao: LogDao
    abstract val tagDao: TagDao
    abstract val entryDao: EntryDao
}

//val MIGRATION_1_2 = object : Migration(1,2) {
//    override fun migrate(db: SupportSQLiteDatabase) {
//        val json = Converters().autoNamingFormatToJson(listOf<TextTemplate>(Time.Hour(true),
//            InputString(":"), Time.Minute())
//        )
//        db.execSQL("UPDATE 'NoteEntity' SET 'loggerPrefixFormat' = '$json'")
//    }
//}