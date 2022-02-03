/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the Licence);
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 *     https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 */

package de.gematik.ti.erp.app.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.gematik.ti.erp.app.db.converter.TruststoreConverter
import de.gematik.ti.erp.app.di.RoomModule
import de.gematik.ti.erp.app.di.TruststoreModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    private val truststoreConverter = TruststoreConverter(TruststoreModule.provideTruststoreMoshi())

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    // Placeholder for future migrations
    @Test
    fun migratesFromVersion1ToVersionX() {
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }
        Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            TEST_DB
        )
            .addTypeConverter(truststoreConverter)
            .addMigrations(*RoomModule.migrations).build().apply {
                openHelper.writableDatabase
                close()
            }
    }

    @Test
    fun migratesFromVersion4ToVersion5() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO `medicationDispense` (`taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text`, `type`)" +
                    "VALUES ('test1', 'test2', 'test3', 1, 'test4', 'test5', 'test6', 'test7', 123)"
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5).use { db ->
            db.query("SELECT `taskId`, `patientIdentifier`, `uniqueIdentifier`, `wasSubstituted`, `dosageInstruction`, `performer`, `whenHandedOver`, `text`, `type` FROM `medicationDispense`")
                .let {
                    it.moveToFirst()
                    assertEquals("test1", it.getString((it.getColumnIndex("taskId"))))
                    assertEquals("test2", it.getString((it.getColumnIndex("patientIdentifier"))))
                    assertEquals("test3", it.getString((it.getColumnIndex("uniqueIdentifier"))))
                    assertEquals(1, it.getInt((it.getColumnIndex("wasSubstituted"))))
                    assertEquals("test4", it.getString((it.getColumnIndex("dosageInstruction"))))
                    assertEquals("test5", it.getString((it.getColumnIndex("performer"))))
                    assertEquals("test6", it.getString((it.getColumnIndex("whenHandedOver"))))
                    assertEquals("test7", it.getString((it.getColumnIndex("text"))))
                    assertEquals(null, it.getString((it.getColumnIndex("type"))))
                }
        }
    }

    @Test
    fun migratesFromVersion10ToVersion11() {
        helper.createDatabase(TEST_DB, 10).apply {
            execSQL(
                "INSERT INTO `communications` (`communicationId`, `profile`, `time`, `taskId`, `telematicsId`, `kbvUserId`, `payload`, `consumed`)" +
                    "VALUES ('1', '', '', 'TaskId/1', '', '', '', 0)"
            )
            execSQL(
                "INSERT INTO `communications` (`communicationId`, `profile`, `time`, `taskId`, `telematicsId`, `kbvUserId`, `payload`, `consumed`)" +
                    "VALUES ('2', '', '', 'TaskId/2', '', '', '', 0)"
            )
            execSQL(
                "INSERT INTO `communications` (`communicationId`, `profile`, `time`, `taskId`, `telematicsId`, `kbvUserId`, `payload`, `consumed`)" +
                    "VALUES ('3', '', '', 'TaskId/3', '', '', '', 0)"
            )
            execSQL(
                """
                    INSERT INTO `tasks` (
                        `taskId`,
                        `accessCode`,
                        `lastModified`,
                        `organization`,
                        `medicationText`,
                        `expiresOn`,
                        `acceptUntil`,
                        `authoredOn`,
                        `status`,
                        `scannedOn`,
                        `scanSessionEnd`,
                        `nrInScanSession`,
                        `scanSessionEnd`,
                        `scanSessionName`,
                        `redeemedOn`,
                        `rawKBVBundle`
                    ) VALUES (
                        'TaskId/2',
                        '1',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        NULL
                    )
                """.trimIndent()
            )
            execSQL(
                """
                    INSERT INTO `tasks` (
                        `taskId`,
                        `accessCode`,
                        `lastModified`,
                        `organization`,
                        `medicationText`,
                        `expiresOn`,
                        `acceptUntil`,
                        `authoredOn`,
                        `status`,
                        `scannedOn`,
                        `scanSessionEnd`,
                        `nrInScanSession`,
                        `scanSessionEnd`,
                        `scanSessionName`,
                        `redeemedOn`,
                        `rawKBVBundle`
                    ) VALUES (
                        'TaskId/8',
                        '1',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        '',
                        NULL
                    )
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(TEST_DB, 11, true, MIGRATION_10_11).use { db ->
            db.setForeignKeyConstraintsEnabled(true)
            db.assertForeignKeyConstraints("communications")
            db.assertForeignKeyConstraints("tasks")
            assertEquals(1, db.query("SELECT * FROM `communications`").count)
            assertEquals(2, db.query("SELECT * FROM `tasks`").count)
            db.query("SELECT taskId FROM `communications`").let {
                it.moveToFirst()
                assertEquals("TaskId/2", it.getString(0))
            }
            db.query("SELECT taskId FROM `tasks`").let {
                it.moveToFirst()
                assertEquals("TaskId/2", it.getString(0))
                it.moveToNext()
                assertEquals("TaskId/8", it.getString(0))
            }
        }
    }

    @Test
    fun migratesFromVersion15ToVersion16() {
        helper.createDatabase(TEST_DB, 15).apply {
            execSQL(
                "INSERT INTO `tasks` (`taskId`, `profileName`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionEnd`, `scanSessionName`, `redeemedOn`, `rawKBVBundle`)" +
                    "VALUES ('1', 'Test', '1', '', '', '', '', '', '', 'Wrong status', '', '', '', '', '', '', NULL)"
            )
            execSQL(
                "INSERT INTO `tasks` (`taskId`, `profileName`, `accessCode`, `lastModified`, `organization`, `medicationText`, `expiresOn`, `acceptUntil`, `authoredOn`, `status`, `scannedOn`, `scanSessionEnd`, `nrInScanSession`, `scanSessionEnd`, `scanSessionName`, `redeemedOn`, `rawKBVBundle`)" +
                    "VALUES ('2', 'Test', '2', '', '', '', '', '', '', NULL, '', '', '', '', '', '', NULL)"
            )
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 16, true, MIGRATION_15_16).use { db ->
            db.query("SELECT `status` FROM `tasks`")
                .let {
                    it.moveToFirst()
                    assertEquals("Other", it.getString((it.getColumnIndex("status"))))
                    it.moveToNext()
                    assertEquals(null, it.getString((it.getColumnIndex("status"))))
                }
        }
    }
}

fun SupportSQLiteDatabase.assertForeignKeyConstraints(table: String) {
    assertEquals(0, query("PRAGMA foreign_key_check(`$table`);").count)
}
