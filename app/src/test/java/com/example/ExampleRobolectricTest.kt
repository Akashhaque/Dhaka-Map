package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.DhakaBoundaryData
import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dhaka Map", appName)
  }

  @Test
  fun `verify central dhaka is inside boundary`() {
    assertTrue(DhakaBoundaryData.isInsideDhaka(DhakaBoundaryData.CENTRAL_DHAKA))
  }

  @Test
  fun `verify outside point is rejected`() {
    // Chittagong coordinates
    assertFalse(DhakaBoundaryData.isInsideDhaka(LatLng(22.3569, 91.7832)))
  }
}
