package com.mayra.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class MayraPersistentUploadQueueTest {
    @Test fun queueSurvivesRecreatedManagerAndApproval() {
        val prefs = TestSharedPreferences()
        MayraPersistentUploadQueue(prefs).enqueue("item-1", 123L)
        val recreated = MayraPersistentUploadQueue(prefs)
        assertEquals(1, recreated.all().size)
        assertEquals(false, recreated.all().first().ownerApproved)
        recreated.approve("item-1")
        assertEquals(true, MayraPersistentUploadQueue(prefs).all().first().ownerApproved)
    }
}
