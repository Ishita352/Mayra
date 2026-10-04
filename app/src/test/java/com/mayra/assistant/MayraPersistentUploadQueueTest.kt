package com.mayra.assistant

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class MayraPersistentUploadQueueTest {
    @Test fun queueSurvivesRecreatedManagerAndApproval() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("mayra_upload_queue", Context.MODE_PRIVATE).edit().clear().apply()
        MayraPersistentUploadQueue(context).enqueue("item-1", 123L)
        val recreated = MayraPersistentUploadQueue(context)
        assertEquals(1, recreated.all().size)
        assertEquals(false, recreated.all().first().ownerApproved)
        recreated.approve("item-1")
        assertEquals(true, MayraPersistentUploadQueue(context).all().first().ownerApproved)
    }
}
